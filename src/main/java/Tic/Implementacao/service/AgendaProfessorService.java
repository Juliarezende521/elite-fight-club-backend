package Tic.Implementacao.service;

import Tic.Implementacao.dto.agenda.professor.*;
import Tic.Implementacao.model.*;
import Tic.Implementacao.repository.AulaPersonalRepository;
import Tic.Implementacao.repository.AulaTurmaRepository;
import Tic.Implementacao.repository.ProfessorRepository;
import Tic.Implementacao.repository.ReservaAulaRepository;
import Tic.Implementacao.security.PermissaoService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AgendaProfessorService {

    private final AulaTurmaRepository aulaTurmaRepository;
    private final AulaPersonalRepository aulaPersonalRepository;
    private final ReservaAulaRepository reservaAulaRepository;
    private final ProfessorRepository professorRepository;
    private final PermissaoService permissaoService;

    public AgendaProfessorService(AulaTurmaRepository aulaTurmaRepository,
                                  AulaPersonalRepository aulaPersonalRepository,
                                  ReservaAulaRepository reservaAulaRepository,
                                  ProfessorRepository professorRepository,
                                  PermissaoService permissaoService) {
        this.aulaTurmaRepository = aulaTurmaRepository;
        this.aulaPersonalRepository = aulaPersonalRepository;
        this.reservaAulaRepository = reservaAulaRepository;
        this.professorRepository = professorRepository;
        this.permissaoService = permissaoService;
    }

    @Transactional(readOnly = true)
    public ResumoAgendaProfessorResponse buscarAgendaDiaria(Long professorId, LocalDate data) {
        Long academiaId = validarAcessoProfessor(professorId);
        List<AulaTurma> turmas = aulaTurmaRepository.findByAcademiaIdAndProfessorIdAndData(academiaId, professorId, data);
        List<AulaPersonal> pessoais = aulaPersonalRepository.findByAcademiaIdAndProfessorIdAndData(academiaId, professorId, data);
        return montarResumo(professorId, data, turmas, pessoais, ocupacoesPorAula(academiaId, turmas));
    }

    @Transactional(readOnly = true)
    public AgendaSemanalProfessorResponse buscarAgendaSemanal(Long professorId, LocalDate inicio) {
        Long academiaId = validarAcessoProfessor(professorId);
        LocalDate segunda = inicio.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate domingo = segunda.plusDays(6);

        List<AulaTurma> turmas = aulaTurmaRepository.findByAcademiaIdAndProfessorIdAndDataBetween(academiaId, professorId, segunda, domingo);
        List<AulaPersonal> pessoais = aulaPersonalRepository.findByAcademiaIdAndProfessorIdAndDataBetween(academiaId, professorId, segunda, domingo);
        Map<Long, Long> ocupacoes = ocupacoesPorAula(academiaId, turmas);

        Map<LocalDate, List<AulaTurma>> turmasPorDia = turmas.stream().collect(Collectors.groupingBy(AulaTurma::getData));
        Map<LocalDate, List<AulaPersonal>> pessoaisPorDia = pessoais.stream().collect(Collectors.groupingBy(AulaPersonal::getData));

        List<ResumoAgendaProfessorResponse> dias = new ArrayList<>();
        LocalDate data = segunda;
        while (!data.isAfter(domingo)) {
            dias.add(montarResumo(
                    professorId,
                    data,
                    turmasPorDia.getOrDefault(data, List.of()),
                    pessoaisPorDia.getOrDefault(data, List.of()),
                    ocupacoes
            ));
            data = data.plusDays(1);
        }

        int totalTurmas = dias.stream().mapToInt(ResumoAgendaProfessorResponse::getTotalTurmas).sum();
        int totalPersonais = dias.stream().mapToInt(ResumoAgendaProfessorResponse::getTotalPersonais).sum();
        int totalAulas = dias.stream().mapToInt(ResumoAgendaProfessorResponse::getTotalAulas).sum();
        int totalConfirmados = dias.stream().mapToInt(ResumoAgendaProfessorResponse::getTotalConfirmados).sum();

        return new AgendaSemanalProfessorResponse(professorId, segunda, domingo, totalTurmas, totalPersonais,
                totalAulas, totalConfirmados, dias);
    }

    @Transactional(readOnly = true)
    public DetalheAulaTurmaProfessorResponse buscarDetalheTurma(Long aulaId) {
        Long academiaId = permissaoService.academiaAtualId();
        AulaTurma aula = aulaTurmaRepository.buscarDetalhePorId(aulaId, academiaId)
                .orElseThrow(() -> new RuntimeException("Aula de turma não encontrada com ID: " + aulaId));
        validarAcessoProfessor(aula.getProfessor().getId());

        List<ReservaAula> reservas = reservaAulaRepository.buscarConfirmadasComAluno(academiaId, aulaId, StatusReserva.CONFIRMADA);
        List<AlunoReservaResponse> alunos = reservas.stream()
                .map(reserva -> new AlunoReservaResponse(reserva.getAluno().getId(), reserva.getAluno().getNome(), reserva.getReservadoEm()))
                .toList();

        int confirmados = reservas.size();
        int vagas = vagasDisponiveis(aula.getCapacidadeMaxima(), confirmados);
        return new DetalheAulaTurmaProfessorResponse(
                aula.getId(),
                aula.getData(),
                aula.getHoraInicio(),
                aula.getHoraFim(),
                aula.getStatus(),
                aula.getProfessor().getId(),
                aula.getProfessor().getNome(),
                aula.getModalidade().getNome(),
                aula.getCapacidadeMaxima(),
                confirmados,
                vagas,
                confirmados >= aula.getCapacidadeMaxima(),
                aula.getAulaOriginal() == null ? null : aula.getAulaOriginal().getId(),
                alunos
        );
    }

    @Transactional(readOnly = true)
    public DetalheAulaPersonalProfessorResponse buscarDetalhePersonal(Long aulaId) {
        Long academiaId = permissaoService.academiaAtualId();
        AulaPersonal aula = aulaPersonalRepository.buscarDetalhePorId(aulaId, academiaId)
                .orElseThrow(() -> new RuntimeException("Aula personal não encontrada com ID: " + aulaId));
        validarAcessoProfessor(aula.getProfessor().getId());

        return new DetalheAulaPersonalProfessorResponse(
                aula.getId(),
                aula.getAgendaPersonal().getId(),
                aula.getData(),
                aula.getHoraInicio(),
                aula.getHoraFim(),
                aula.getStatus(),
                aula.getProfessor().getId(),
                aula.getProfessor().getNome(),
                aula.getAluno().getId(),
                aula.getAluno().getNome(),
                aula.getAulaOriginal() == null ? null : aula.getAulaOriginal().getId()
        );
    }

    private Long validarAcessoProfessor(Long professorId) {
        permissaoService.exigirAdminOuProfessor();
        Usuario usuario = permissaoService.usuarioAtual();
        Long academiaId = permissaoService.academiaAtualId();
        professorRepository.findByIdAndAcademiaId(professorId, academiaId)
                .orElseThrow(() -> new RuntimeException("Professor não encontrado com ID: " + professorId));

        if (usuario.getPerfil() == PerfilUsuario.PROFESSOR) {
            if (usuario.getProfessor() == null || !professorId.equals(usuario.getProfessor().getId())) {
                throw new AccessDeniedException("Professor só pode consultar a própria agenda");
            }
        }
        return academiaId;
    }

    private ResumoAgendaProfessorResponse montarResumo(Long professorId, LocalDate data, List<AulaTurma> turmas,
                                                       List<AulaPersonal> pessoais, Map<Long, Long> ocupacoes) {
        List<ItemAgendaProfessorResponse> itens = new ArrayList<>();
        turmas.forEach(aula -> itens.add(toItem(aula, ocupacoes.getOrDefault(aula.getId(), 0L))));
        pessoais.forEach(aula -> itens.add(toItem(aula)));
        itens.sort(Comparator.comparing(ItemAgendaProfessorResponse::getData)
                .thenComparing(ItemAgendaProfessorResponse::getHoraInicio)
                .thenComparing(ItemAgendaProfessorResponse::getHoraFim));

        int totalConfirmados = itens.stream()
                .filter(item -> item.getTipo() == TipoItemAgenda.TURMA)
                .mapToInt(item -> item.getConfirmados() == null ? 0 : item.getConfirmados())
                .sum();
        int totalVagas = itens.stream()
                .filter(item -> item.getTipo() == TipoItemAgenda.TURMA)
                .mapToInt(item -> item.getVagasDisponiveis() == null ? 0 : item.getVagasDisponiveis())
                .sum();
        int turmasLotadas = (int) itens.stream()
                .filter(item -> item.getTipo() == TipoItemAgenda.TURMA && Boolean.TRUE.equals(item.getLotada()))
                .count();

        return new ResumoAgendaProfessorResponse(professorId, data, turmas.size(), pessoais.size(), itens.size(),
                totalConfirmados, totalVagas, turmasLotadas, itens);
    }

    private Map<Long, Long> ocupacoesPorAula(Long academiaId, List<AulaTurma> turmas) {
        if (turmas.isEmpty()) {
            return Map.of();
        }
        List<Long> aulaIds = turmas.stream().map(AulaTurma::getId).toList();
        return reservaAulaRepository.contarConfirmadasPorAulas(academiaId, aulaIds, StatusReserva.CONFIRMADA)
                .stream()
                .collect(Collectors.toMap(ReservaAulaRepository.OcupacaoAulaProjection::getAulaId,
                        ReservaAulaRepository.OcupacaoAulaProjection::getConfirmados));
    }

    private ItemAgendaProfessorResponse toItem(AulaTurma aula, long confirmadosLong) {
        int confirmados = Math.toIntExact(confirmadosLong);
        int vagas = vagasDisponiveis(aula.getCapacidadeMaxima(), confirmados);
        ItemAgendaProfessorResponse item = itemBase(aula.getId(), TipoItemAgenda.TURMA, aula.getData(),
                aula.getHoraInicio(), aula.getHoraFim(), aula.getStatus(), aula.getProfessor(), aula.getAcademia(),
                aula.getAulaOriginal() == null ? null : aula.getAulaOriginal().getId());
        item.setModalidadeId(aula.getModalidade().getId());
        item.setModalidadeNome(aula.getModalidade().getNome());
        item.setCapacidadeMaxima(aula.getCapacidadeMaxima());
        item.setConfirmados(confirmados);
        item.setVagasDisponiveis(vagas);
        item.setLotada(confirmados >= aula.getCapacidadeMaxima());
        return item;
    }

    private ItemAgendaProfessorResponse toItem(AulaPersonal aula) {
        ItemAgendaProfessorResponse item = itemBase(aula.getId(), TipoItemAgenda.PERSONAL, aula.getData(),
                aula.getHoraInicio(), aula.getHoraFim(), aula.getStatus(), aula.getProfessor(), aula.getAcademia(),
                aula.getAulaOriginal() == null ? null : aula.getAulaOriginal().getId());
        item.setAlunoId(aula.getAluno().getId());
        item.setAlunoNome(aula.getAluno().getNome());
        return item;
    }

    private ItemAgendaProfessorResponse itemBase(Long aulaId, TipoItemAgenda tipo, LocalDate data,
                                                 java.time.LocalTime horaInicio, java.time.LocalTime horaFim,
                                                 StatusAula status, Professor professor, Academia academia,
                                                 Long aulaOriginalId) {
        ItemAgendaProfessorResponse item = new ItemAgendaProfessorResponse();
        item.setAulaId(aulaId);
        item.setTipo(tipo);
        item.setData(data);
        item.setHoraInicio(horaInicio);
        item.setHoraFim(horaFim);
        item.setStatus(status);
        item.setProfessorId(professor.getId());
        item.setProfessorNome(professor.getNome());
        item.setAcademiaId(academia.getId());
        item.setAulaOriginalId(aulaOriginalId);
        return item;
    }

    private int vagasDisponiveis(Integer capacidade, int confirmados) {
        return Math.max(0, capacidade - confirmados);
    }
}
