package Tic.Implementacao.service;

import Tic.Implementacao.dto.reserva.OcupacaoAulaResponse;
import Tic.Implementacao.dto.reserva.ReservaAulaResponse;
import Tic.Implementacao.model.*;
import Tic.Implementacao.repository.AulaTurmaRepository;
import Tic.Implementacao.repository.ReservaAulaRepository;
import Tic.Implementacao.security.PermissaoService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
public class ReservaAulaService {

    private final ReservaAulaRepository reservaRepository;
    private final AulaTurmaRepository aulaTurmaRepository;
    private final PermissaoService permissaoService;
    private final Clock clock;

    public ReservaAulaService(ReservaAulaRepository reservaRepository,
                              AulaTurmaRepository aulaTurmaRepository,
                              PermissaoService permissaoService,
                              Clock clock) {
        this.reservaRepository = reservaRepository;
        this.aulaTurmaRepository = aulaTurmaRepository;
        this.permissaoService = permissaoService;
        this.clock = clock;
    }

    @Transactional
    public ReservaAula reservarMinhaVaga(Long aulaId) {
        Usuario usuario = permissaoService.usuarioAtual();
        Aluno aluno = alunoAtual(usuario);
        Long academiaId = permissaoService.academiaAtualId();

        AulaTurma aula = aulaTurmaRepository.findByIdAndAcademiaIdForUpdate(aulaId, academiaId)
                .orElseThrow(() -> new RuntimeException("Aula de turma não encontrada com ID: " + aulaId));

        validarMesmaAcademia(aluno, aula, academiaId);
        validarPlano(aluno);
        validarAulaReservavel(aula);
        validarHorarioReserva(aula);
        validarDuplicidade(academiaId, aulaId, aluno.getId());
        validarLimiteSemanal(academiaId, aluno, aula.getData());
        validarCapacidade(academiaId, aula);

        LocalDateTime agora = LocalDateTime.now(clock);
        ReservaAula reserva = new ReservaAula();
        reserva.setAcademia(aula.getAcademia());
        reserva.setAulaTurma(aula);
        reserva.setAluno(aluno);
        reserva.setStatus(StatusReserva.CONFIRMADA);
        reserva.setReservadoEm(agora);
        reserva.setChaveReservaAtiva(chaveAtiva(aula.getId(), aluno.getId()));
        return reservaRepository.save(reserva);
    }

    @Transactional
    public ReservaAula cancelarMinhaReserva(Long aulaId) {
        Usuario usuario = permissaoService.usuarioAtual();
        Aluno aluno = alunoAtual(usuario);
        Long academiaId = permissaoService.academiaAtualId();

        AulaTurma aula = aulaTurmaRepository.findByIdAndAcademiaIdForUpdate(aulaId, academiaId)
                .orElseThrow(() -> new RuntimeException("Aula de turma não encontrada com ID: " + aulaId));
        validarMesmaAcademia(aluno, aula, academiaId);
        validarCancelamentoAntesDoInicio(aula);

        ReservaAula reserva = reservaRepository
                .findByAcademiaIdAndAulaTurmaIdAndAlunoIdAndStatus(academiaId, aulaId, aluno.getId(), StatusReserva.CONFIRMADA)
                .orElseThrow(() -> new RuntimeException("Reserva confirmada não encontrada para esta aula"));

        reserva.setStatus(StatusReserva.CANCELADA);
        reserva.setCanceladoEm(LocalDateTime.now(clock));
        reserva.setChaveReservaAtiva(null);
        return reservaRepository.save(reserva);
    }

    public List<ReservaAulaResponse> listarReservasDaAula(Long aulaId) {
        permissaoService.exigirAdminOuProfessor();
        Long academiaId = permissaoService.academiaAtualId();
        aulaTurmaRepository.findByIdAndAcademiaId(aulaId, academiaId)
                .orElseThrow(() -> new RuntimeException("Aula de turma não encontrada com ID: " + aulaId));
        return reservaRepository.findAllByAcademiaIdAndAulaTurmaIdOrderByReservadoEmAsc(academiaId, aulaId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<ReservaAulaResponse> listarMinhasReservas(LocalDate inicio, LocalDate fim) {
        Usuario usuario = permissaoService.usuarioAtual();
        Aluno aluno = alunoAtual(usuario);
        Long academiaId = permissaoService.academiaAtualId();
        if (inicio.isAfter(fim)) {
            throw new RuntimeException("Data inicial deve ser menor ou igual à data final");
        }
        return reservaRepository.listarReservasAlunoNoPeriodo(academiaId, aluno.getId(), inicio, fim)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public OcupacaoAulaResponse consultarOcupacao(Long aulaId) {
        Long academiaId = permissaoService.academiaAtualId();
        AulaTurma aula = aulaTurmaRepository.findByIdAndAcademiaId(aulaId, academiaId)
                .orElseThrow(() -> new RuntimeException("Aula de turma não encontrada com ID: " + aulaId));
        long confirmados = reservaRepository.countByAcademiaIdAndAulaTurmaIdAndStatus(academiaId, aulaId, StatusReserva.CONFIRMADA);
        return new OcupacaoAulaResponse(aula.getId(), confirmados, aula.getCapacidadeMaxima());
    }

    private Aluno alunoAtual(Usuario usuario) {
        if (usuario.getPerfil() != PerfilUsuario.ALUNO || usuario.getAluno() == null) {
            throw new AccessDeniedException("Reserva permitida apenas para o próprio aluno autenticado");
        }
        return usuario.getAluno();
    }

    private void validarPlano(Aluno aluno) {
        if (aluno.getTipoAluno() != TipoAluno.TURMA) {
            throw new RuntimeException("Aluno personal não pode reservar vaga em turma por este fluxo");
        }
        Plano plano = aluno.getPlano();
        if (plano == null) {
            throw new RuntimeException("Aluno não possui plano vinculado");
        }
        if (!Boolean.TRUE.equals(plano.getAtivo())) {
            throw new RuntimeException("Plano do aluno está inativo");
        }
        if (plano.getTipo() != TipoPlano.TURMA) {
            throw new RuntimeException("Apenas alunos com plano de turma podem reservar vaga em aula de turma");
        }
        if (plano.getQuantidadeAulasSemana() == null || plano.getQuantidadeAulasSemana() <= 0) {
            throw new RuntimeException("Plano do aluno não possui aulas semanais disponíveis");
        }
    }

    private void validarAulaReservavel(AulaTurma aula) {
        if (aula.getStatus() != StatusAula.AGENDADA) {
            throw new RuntimeException("Reserva permitida apenas para aula agendada");
        }
    }

    private void validarHorarioReserva(AulaTurma aula) {
        LocalDate hoje = LocalDate.now(clock);
        LocalTime agora = LocalTime.now(clock);
        if (!aula.getData().equals(hoje)) {
            throw new RuntimeException("Reserva permitida somente no dia da aula");
        }
        if (!agora.isBefore(aula.getHoraInicio())) {
            throw new RuntimeException("Reserva não permitida após o início da aula");
        }
    }

    private void validarCancelamentoAntesDoInicio(AulaTurma aula) {
        LocalDate hoje = LocalDate.now(clock);
        LocalTime agora = LocalTime.now(clock);
        if (hoje.isAfter(aula.getData()) || (hoje.equals(aula.getData()) && !agora.isBefore(aula.getHoraInicio()))) {
            throw new RuntimeException("Cancelamento permitido apenas antes do início da aula");
        }
    }

    private void validarDuplicidade(Long academiaId, Long aulaId, Long alunoId) {
        reservaRepository.findByAcademiaIdAndAulaTurmaIdAndAlunoIdAndStatus(academiaId, aulaId, alunoId, StatusReserva.CONFIRMADA)
                .ifPresent(reserva -> {
                    throw new RuntimeException("Aluno já possui reserva confirmada para esta aula");
                });
    }

    private void validarLimiteSemanal(Long academiaId, Aluno aluno, LocalDate dataAula) {
        LocalDate inicioSemana = dataAula.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        LocalDate fimSemana = dataAula.with(TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY));
        long reservasSemana = reservaRepository.countReservasConfirmadasNaSemana(
                academiaId, aluno.getId(), StatusReserva.CONFIRMADA, inicioSemana, fimSemana);
        if (reservasSemana >= aluno.getPlano().getQuantidadeAulasSemana()) {
            throw new RuntimeException("Limite semanal de aulas do plano atingido");
        }
    }

    private void validarCapacidade(Long academiaId, AulaTurma aula) {
        long confirmados = reservaRepository.countByAcademiaIdAndAulaTurmaIdAndStatus(
                academiaId, aula.getId(), StatusReserva.CONFIRMADA);
        if (confirmados >= aula.getCapacidadeMaxima()) {
            throw new RuntimeException("Turma lotada");
        }
    }

    private void validarMesmaAcademia(Aluno aluno, AulaTurma aula, Long academiaId) {
        if (aluno.getAcademia() == null || !academiaId.equals(aluno.getAcademia().getId())) {
            throw new RuntimeException("Aluno não pertence à academia atual");
        }
        if (aula.getAcademia() == null || !academiaId.equals(aula.getAcademia().getId())) {
            throw new RuntimeException("Aula não pertence à academia atual");
        }
        Plano plano = aluno.getPlano();
        if (plano != null && (plano.getAcademia() == null || !academiaId.equals(plano.getAcademia().getId()))) {
            throw new RuntimeException("Plano não pertence à academia atual");
        }
        if (aula.getProfessor() != null && aula.getProfessor().getAcademia() != null
                && !academiaId.equals(aula.getProfessor().getAcademia().getId())) {
            throw new RuntimeException("Professor da aula não pertence à academia atual");
        }
    }

    private String chaveAtiva(Long aulaId, Long alunoId) {
        return aulaId + ":" + alunoId;
    }

    private ReservaAulaResponse toResponse(ReservaAula reserva) {
        AulaTurma aula = reserva.getAulaTurma();
        Aluno aluno = reserva.getAluno();
        return new ReservaAulaResponse(
                reserva.getId(),
                aula.getId(),
                aluno.getId(),
                aluno.getNome(),
                aula.getData(),
                aula.getHoraInicio(),
                aula.getHoraFim(),
                reserva.getStatus(),
                reserva.getReservadoEm(),
                reserva.getCanceladoEm()
        );
    }
}
