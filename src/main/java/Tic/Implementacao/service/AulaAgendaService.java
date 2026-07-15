package Tic.Implementacao.service;

import Tic.Implementacao.dto.aula.ItemAulaProfessorResponse;
import Tic.Implementacao.dto.aula.RemarcarAulaRequest;
import Tic.Implementacao.model.*;
import Tic.Implementacao.repository.*;
import Tic.Implementacao.security.PermissaoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class AulaAgendaService {

    private final AulaTurmaRepository aulaTurmaRepository;
    private final AulaPersonalRepository aulaPersonalRepository;
    private final HorarioTurmaRepository horarioTurmaRepository;
    private final AgendaPersonalRepository agendaPersonalRepository;
    private final ProfessorRepository professorRepository;
    private final PermissaoService permissaoService;

    public AulaAgendaService(AulaTurmaRepository aulaTurmaRepository,
                             AulaPersonalRepository aulaPersonalRepository,
                             HorarioTurmaRepository horarioTurmaRepository,
                             AgendaPersonalRepository agendaPersonalRepository,
                             ProfessorRepository professorRepository,
                             PermissaoService permissaoService) {
        this.aulaTurmaRepository = aulaTurmaRepository;
        this.aulaPersonalRepository = aulaPersonalRepository;
        this.horarioTurmaRepository = horarioTurmaRepository;
        this.agendaPersonalRepository = agendaPersonalRepository;
        this.professorRepository = professorRepository;
        this.permissaoService = permissaoService;
    }

    @Transactional
    public AulaTurma gerarAulaTurma(Long horarioTurmaId, LocalDate data) {
        permissaoService.exigirAdminOuProfessor();
        Long academiaId = permissaoService.academiaAtualId();
        HorarioTurma horario = horarioTurmaRepository.findByIdAndAcademiaId(horarioTurmaId, academiaId)
                .orElseThrow(() -> new RuntimeException("Horário de turma não encontrado com ID: " + horarioTurmaId));
        validarHorarioAtivo(horario.getAtivo());
        validarDiaSemana(horario.getDiaSemana(), data);

        return aulaTurmaRepository.findByAcademiaIdAndHorarioTurmaIdAndData(academiaId, horarioTurmaId, data)
                .orElseGet(() -> criarAulaTurma(horario, data, null, null, null));
    }

    @Transactional
    public AulaPersonal gerarAulaPersonal(Long agendaPersonalId, LocalDate data) {
        permissaoService.exigirAdminOuProfessor();
        Long academiaId = permissaoService.academiaAtualId();
        AgendaPersonal agenda = agendaPersonalRepository.findByIdAndAcademiaId(agendaPersonalId, academiaId)
                .orElseThrow(() -> new RuntimeException("Agenda personal não encontrada com ID: " + agendaPersonalId));
        validarHorarioAtivo(agenda.getAtivo());
        validarDiaSemana(agenda.getDiaSemana(), data);
        if (data.isBefore(agenda.getDataInicio())) {
            throw new RuntimeException("Data anterior ao início da agenda personal");
        }

        return aulaPersonalRepository.findByAcademiaIdAndAgendaPersonalIdAndData(academiaId, agendaPersonalId, data)
                .orElseGet(() -> criarAulaPersonal(agenda, data, null, null, null));
    }

    @Transactional
    public void gerarAulasDoDia(LocalDate data) {
        permissaoService.exigirAdminOuProfessor();
        Long academiaId = permissaoService.academiaAtualId();
        horarioTurmaRepository.findByAcademiaIdAndDiaSemanaAndAtivoTrue(academiaId, data.getDayOfWeek())
                .forEach(horario -> aulaTurmaRepository.findByAcademiaIdAndHorarioTurmaIdAndData(academiaId, horario.getId(), data)
                        .orElseGet(() -> criarAulaTurma(horario, data, null, null, null)));
        agendaPersonalRepository.findByAcademiaIdAndDiaSemanaAndAtivoTrueAndDataInicioLessThanEqual(academiaId, data.getDayOfWeek(), data)
                .forEach(agenda -> aulaPersonalRepository.findByAcademiaIdAndAgendaPersonalIdAndData(academiaId, agenda.getId(), data)
                        .orElseGet(() -> criarAulaPersonal(agenda, data, null, null, null)));
    }

    @Transactional
    public void gerarAulasDoPeriodo(LocalDate inicio, LocalDate fim) {
        permissaoService.exigirAdminOuProfessor();
        if (inicio.isAfter(fim)) {
            throw new RuntimeException("Data inicial deve ser menor ou igual à data final");
        }
        LocalDate data = inicio;
        while (!data.isAfter(fim)) {
            gerarAulasDoDia(data);
            data = data.plusDays(1);
        }
    }

    public List<ItemAulaProfessorResponse> listarAulasProfessorPorData(Long professorId, LocalDate data) {
        Long academiaId = permissaoService.academiaAtualId();
        professorRepository.findByIdAndAcademiaId(professorId, academiaId)
                .orElseThrow(() -> new RuntimeException("Professor não encontrado com ID: " + professorId));

        List<ItemAulaProfessorResponse> itens = new ArrayList<>();
        aulaTurmaRepository.findByAcademiaIdAndProfessorIdAndData(academiaId, professorId, data)
                .forEach(aula -> itens.add(toResponse(aula)));
        aulaPersonalRepository.findByAcademiaIdAndProfessorIdAndData(academiaId, professorId, data)
                .forEach(aula -> itens.add(toResponse(aula)));
        itens.sort(Comparator.comparing(ItemAulaProfessorResponse::getHoraInicio));
        return itens;
    }

    public List<ItemAulaProfessorResponse> listarAulasProfessorSemana(Long professorId, LocalDate inicio) {
        Long academiaId = permissaoService.academiaAtualId();
        professorRepository.findByIdAndAcademiaId(professorId, academiaId)
                .orElseThrow(() -> new RuntimeException("Professor não encontrado com ID: " + professorId));
        LocalDate fim = inicio.plusDays(6);

        List<ItemAulaProfessorResponse> itens = new ArrayList<>();
        aulaTurmaRepository.findByAcademiaIdAndProfessorIdAndDataBetween(academiaId, professorId, inicio, fim)
                .forEach(aula -> itens.add(toResponse(aula)));
        aulaPersonalRepository.findByAcademiaIdAndProfessorIdAndDataBetween(academiaId, professorId, inicio, fim)
                .forEach(aula -> itens.add(toResponse(aula)));
        itens.sort(Comparator.comparing(ItemAulaProfessorResponse::getData).thenComparing(ItemAulaProfessorResponse::getHoraInicio));
        return itens;
    }

    @Transactional
    public AulaTurma cancelarAulaTurma(Long id, String observacao) {
        permissaoService.exigirAdminOuProfessor();
        AulaTurma aula = buscarAulaTurma(id);
        validarPodeAlterar(aula.getStatus());
        aula.setStatus(StatusAula.CANCELADA);
        aula.setObservacao(observacao);
        aula.setAtualizadoEm(LocalDateTime.now());
        return aulaTurmaRepository.save(aula);
    }

    @Transactional
    public AulaPersonal cancelarAulaPersonal(Long id, String observacao) {
        permissaoService.exigirAdminOuProfessor();
        AulaPersonal aula = buscarAulaPersonal(id);
        validarPodeAlterar(aula.getStatus());
        aula.setStatus(StatusAula.CANCELADA);
        aula.setObservacao(observacao);
        aula.setAtualizadoEm(LocalDateTime.now());
        return aulaPersonalRepository.save(aula);
    }

    @Transactional
    public AulaTurma remarcarAulaTurma(Long id, RemarcarAulaRequest request) {
        permissaoService.exigirAdminOuProfessor();
        AulaTurma aula = buscarAulaTurma(id);
        validarPodeAlterar(aula.getStatus());

        aula.setStatus(StatusAula.REMARCADA);
        aula.setObservacao(request.getObservacao());
        aula.setAtualizadoEm(LocalDateTime.now());

        LocalTime inicio = request.getNovaHoraInicio() == null ? aula.getHoraInicio() : request.getNovaHoraInicio();
        LocalTime fim = request.getNovaHoraFim() == null ? aula.getHoraFim() : request.getNovaHoraFim();
        return criarAulaTurma(aula.getHorarioTurma(), request.getNovaData(), inicio, fim, aula);
    }

    @Transactional
    public AulaPersonal remarcarAulaPersonal(Long id, RemarcarAulaRequest request) {
        permissaoService.exigirAdminOuProfessor();
        AulaPersonal aula = buscarAulaPersonal(id);
        validarPodeAlterar(aula.getStatus());

        aula.setStatus(StatusAula.REMARCADA);
        aula.setObservacao(request.getObservacao());
        aula.setAtualizadoEm(LocalDateTime.now());

        LocalTime inicio = request.getNovaHoraInicio() == null ? aula.getHoraInicio() : request.getNovaHoraInicio();
        LocalTime fim = request.getNovaHoraFim() == null ? aula.getHoraFim() : request.getNovaHoraFim();
        return criarAulaPersonal(aula.getAgendaPersonal(), request.getNovaData(), inicio, fim, aula);
    }

    @Transactional
    public boolean removerAulaTurma(Long id) {
        permissaoService.exigirAdminOuProfessor();
        AulaTurma aula = buscarAulaTurma(id);
        if (aula.getStatus() == StatusAula.REALIZADA) {
            throw new RuntimeException("Aulas realizadas não podem ser excluídas");
        }
        aulaTurmaRepository.delete(aula);
        return true;
    }

    @Transactional
    public boolean removerAulaPersonal(Long id) {
        permissaoService.exigirAdminOuProfessor();
        AulaPersonal aula = buscarAulaPersonal(id);
        if (aula.getStatus() == StatusAula.REALIZADA) {
            throw new RuntimeException("Aulas realizadas não podem ser excluídas");
        }
        aulaPersonalRepository.delete(aula);
        return true;
    }

    private AulaTurma criarAulaTurma(HorarioTurma horario, LocalDate data, LocalTime horaInicio, LocalTime horaFim, AulaTurma original) {
        AulaTurma existente = aulaTurmaRepository
                .findByAcademiaIdAndHorarioTurmaIdAndData(horario.getAcademia().getId(), horario.getId(), data)
                .orElse(null);
        if (existente != null) {
            if (original != null && existente.getAulaOriginal() == null) {
                existente.setAulaOriginal(original);
                existente.setAtualizadoEm(LocalDateTime.now());
                return aulaTurmaRepository.save(existente);
            }
            return existente;
        }
        LocalDateTime agora = LocalDateTime.now();
        AulaTurma aula = new AulaTurma();
        aula.setAcademia(horario.getAcademia());
        aula.setHorarioTurma(horario);
        aula.setProfessor(horario.getProfessor());
        aula.setModalidade(horario.getModalidade());
        aula.setData(data);
        aula.setHoraInicio(horaInicio == null ? horario.getHoraInicial() : horaInicio);
        aula.setHoraFim(horaFim == null ? horario.getHoraFinal() : horaFim);
        aula.setCapacidadeMaxima(horario.getCapacidadeMaxima());
        aula.setStatus(StatusAula.AGENDADA);
        aula.setAulaOriginal(original);
        aula.setCriadoEm(agora);
        aula.setAtualizadoEm(agora);
        return aulaTurmaRepository.save(aula);
    }

    private AulaPersonal criarAulaPersonal(AgendaPersonal agenda, LocalDate data, LocalTime horaInicio, LocalTime horaFim, AulaPersonal original) {
        AulaPersonal existente = aulaPersonalRepository
                .findByAcademiaIdAndAgendaPersonalIdAndData(agenda.getAcademia().getId(), agenda.getId(), data)
                .orElse(null);
        if (existente != null) {
            if (original != null && existente.getAulaOriginal() == null) {
                existente.setAulaOriginal(original);
                existente.setAtualizadoEm(LocalDateTime.now());
                return aulaPersonalRepository.save(existente);
            }
            return existente;
        }
        LocalDateTime agora = LocalDateTime.now();
        AulaPersonal aula = new AulaPersonal();
        aula.setAcademia(agenda.getAcademia());
        aula.setAgendaPersonal(agenda);
        aula.setAluno(agenda.getAluno());
        aula.setProfessor(agenda.getProfessor());
        aula.setData(data);
        aula.setHoraInicio(horaInicio == null ? agenda.getHoraInicial() : horaInicio);
        aula.setHoraFim(horaFim == null ? agenda.getHoraFinal() : horaFim);
        aula.setStatus(StatusAula.AGENDADA);
        aula.setAulaOriginal(original);
        aula.setCriadoEm(agora);
        aula.setAtualizadoEm(agora);
        return aulaPersonalRepository.save(aula);
    }

    private AulaTurma buscarAulaTurma(Long id) {
        return aulaTurmaRepository.findByIdAndAcademiaId(id, permissaoService.academiaAtualId())
                .orElseThrow(() -> new RuntimeException("Aula de turma não encontrada com ID: " + id));
    }

    private AulaPersonal buscarAulaPersonal(Long id) {
        return aulaPersonalRepository.findByIdAndAcademiaId(id, permissaoService.academiaAtualId())
                .orElseThrow(() -> new RuntimeException("Aula personal não encontrada com ID: " + id));
    }

    private void validarHorarioAtivo(Boolean ativo) {
        if (!Boolean.TRUE.equals(ativo)) {
            throw new RuntimeException("Horário recorrente inativo não gera aula");
        }
    }

    private void validarDiaSemana(java.time.DayOfWeek diaSemana, LocalDate data) {
        if (data == null || diaSemana == null || data.getDayOfWeek() != diaSemana) {
            throw new RuntimeException("Data não corresponde ao dia da semana configurado");
        }
    }

    private void validarPodeAlterar(StatusAula status) {
        if (status == StatusAula.REALIZADA) {
            throw new RuntimeException("Aulas realizadas não podem ser alteradas");
        }
    }

    private ItemAulaProfessorResponse toResponse(AulaTurma aula) {
        return new ItemAulaProfessorResponse(
                "TURMA",
                aula.getId(),
                aula.getHorarioTurma().getId(),
                null,
                aula.getModalidade().getNome(),
                aula.getData(),
                aula.getHoraInicio(),
                aula.getHoraFim(),
                aula.getStatus()
        );
    }

    private ItemAulaProfessorResponse toResponse(AulaPersonal aula) {
        return new ItemAulaProfessorResponse(
                "PERSONAL",
                aula.getId(),
                aula.getAgendaPersonal().getId(),
                aula.getAluno().getNome(),
                null,
                aula.getData(),
                aula.getHoraInicio(),
                aula.getHoraFim(),
                aula.getStatus()
        );
    }
}
