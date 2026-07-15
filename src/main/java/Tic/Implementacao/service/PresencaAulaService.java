package Tic.Implementacao.service;

import Tic.Implementacao.dto.presenca.*;
import Tic.Implementacao.model.*;
import Tic.Implementacao.repository.*;
import Tic.Implementacao.security.PermissaoService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PresencaAulaService {

    private final PresencaAulaRepository presencaRepository;
    private final CheckInRepository checkInRepository;
    private final AulaTurmaRepository aulaTurmaRepository;
    private final AulaPersonalRepository aulaPersonalRepository;
    private final ReservaAulaRepository reservaRepository;
    private final AlunoRepository alunoRepository;
    private final PermissaoService permissaoService;
    private final Clock clock;

    public PresencaAulaService(PresencaAulaRepository presencaRepository,
                               CheckInRepository checkInRepository,
                               AulaTurmaRepository aulaTurmaRepository,
                               AulaPersonalRepository aulaPersonalRepository,
                               ReservaAulaRepository reservaRepository,
                               AlunoRepository alunoRepository,
                               PermissaoService permissaoService,
                               Clock clock) {
        this.presencaRepository = presencaRepository;
        this.checkInRepository = checkInRepository;
        this.aulaTurmaRepository = aulaTurmaRepository;
        this.aulaPersonalRepository = aulaPersonalRepository;
        this.reservaRepository = reservaRepository;
        this.alunoRepository = alunoRepository;
        this.permissaoService = permissaoService;
        this.clock = clock;
    }

    @Transactional
    public PresencaAlunoResponse registrarPresencaTurma(Long aulaId, Long alunoId, RegistrarPresencaRequest request) {
        Usuario usuario = usuarioAdminOuProfessor();
        Long academiaId = permissaoService.academiaAtualId();
        AulaTurma aula = aulaTurmaRepository.buscarDetalhePorId(aulaId, academiaId)
                .orElseThrow(() -> new RuntimeException("Aula de turma não encontrada com ID: " + aulaId));
        validarAcessoAula(aula.getProfessor(), usuario);
        validarAulaPermitePresenca(aula.getStatus());
        validarInicioAula(aula.getData(), aula.getHoraInicio());

        ReservaAula reserva = reservaRepository
                .findByAcademiaIdAndAulaTurmaIdAndAlunoIdAndStatus(academiaId, aulaId, alunoId, StatusReserva.CONFIRMADA)
                .orElseThrow(() -> new RuntimeException("Aluno não possui reserva confirmada para esta aula"));
        Aluno aluno = alunoRepository.findByIdAndAcademiaIdForUpdate(alunoId, academiaId)
                .orElseThrow(() -> new RuntimeException("Aluno não encontrado com ID: " + alunoId));

        PresencaAula presenca = salvarPresencaTurma(aula, aluno, usuario, request);
        CheckIn checkIn = sincronizarCheckIn(presenca, aluno, aula, null, origemManual(usuario), usuario, request.getObservacao());
        return toResponse(presenca, reserva.getId(), checkIn);
    }

    @Transactional
    public PresencaAlunoResponse registrarPresencaPersonal(Long aulaId, RegistrarPresencaRequest request) {
        Usuario usuario = usuarioAdminOuProfessor();
        Long academiaId = permissaoService.academiaAtualId();
        AulaPersonal aula = aulaPersonalRepository.buscarDetalhePorId(aulaId, academiaId)
                .orElseThrow(() -> new RuntimeException("Aula personal não encontrada com ID: " + aulaId));
        validarAcessoAula(aula.getProfessor(), usuario);
        validarAulaPermitePresenca(aula.getStatus());
        validarInicioAula(aula.getData(), aula.getHoraInicio());

        Aluno aluno = alunoRepository.findByIdAndAcademiaIdForUpdate(aula.getAluno().getId(), academiaId)
                .orElseThrow(() -> new RuntimeException("Aluno não encontrado com ID: " + aula.getAluno().getId()));

        PresencaAula presenca = salvarPresencaPersonal(aula, aluno, usuario, request);
        CheckIn checkIn = sincronizarCheckIn(presenca, aluno, null, aula, origemManual(usuario), usuario, request.getObservacao());
        return toResponse(presenca, null, checkIn);
    }

    @Transactional
    public PresencaAlunoResponse registrarPresencaECheckIn(Aluno aluno, AulaTurma aulaTurma, AulaPersonal aulaPersonal,
                                                           OrigemCheckIn origem, Usuario confirmadoPor,
                                                           String observacao, boolean validarInicio) {
        if ((aulaTurma == null && aulaPersonal == null) || (aulaTurma != null && aulaPersonal != null)) {
            throw new RuntimeException("Informe apenas uma aula para registrar presença");
        }
        StatusAula status = aulaTurma == null ? aulaPersonal.getStatus() : aulaTurma.getStatus();
        validarAulaPermitePresenca(status);
        if (validarInicio) {
            if (aulaTurma != null) {
                validarInicioAula(aulaTurma.getData(), aulaTurma.getHoraInicio());
            } else {
                validarInicioAula(aulaPersonal.getData(), aulaPersonal.getHoraInicio());
            }
        }

        RegistrarPresencaRequest request = new RegistrarPresencaRequest();
        request.setStatus(StatusPresenca.PRESENTE);
        request.setObservacao(observacao);

        PresencaAula presenca = aulaTurma == null
                ? salvarPresencaPersonal(aulaPersonal, aluno, confirmadoPor, request)
                : salvarPresencaTurma(aulaTurma, aluno, confirmadoPor, request);
        CheckIn checkIn = sincronizarCheckIn(presenca, aluno, aulaTurma, aulaPersonal, origem, confirmadoPor, observacao);
        return toResponse(presenca, null, checkIn);
    }

    @Transactional(readOnly = true)
    public ListaPresencasAulaResponse listarPresencasTurma(Long aulaId) {
        Usuario usuario = usuarioAdminOuProfessor();
        Long academiaId = permissaoService.academiaAtualId();
        AulaTurma aula = aulaTurmaRepository.buscarDetalhePorId(aulaId, academiaId)
                .orElseThrow(() -> new RuntimeException("Aula de turma não encontrada com ID: " + aulaId));
        validarAcessoAula(aula.getProfessor(), usuario);

        List<ReservaAula> reservas = reservaRepository.buscarConfirmadasComAluno(academiaId, aulaId, StatusReserva.CONFIRMADA);
        Map<Long, PresencaAula> presencas = presencaRepository.findAllByAcademiaIdAndAulaTurmaId(academiaId, aulaId)
                .stream()
                .collect(Collectors.toMap(p -> p.getAluno().getId(), Function.identity()));

        List<PresencaAlunoResponse> alunos = reservas.stream()
                .map(reserva -> {
                    PresencaAula presenca = presencas.get(reserva.getAluno().getId());
                    CheckIn checkIn = checkInRepository
                            .findByAcademiaIdAndChaveCheckInAtivoAndAtivoTrue(academiaId, chave("TURMA", aulaId, reserva.getAluno().getId()))
                            .orElse(null);
                    return presenca == null
                            ? new PresencaAlunoResponse(reserva.getAluno().getId(), reserva.getAluno().getNome(), reserva.getId(),
                            StatusPresenca.PENDENTE, false, null, null, null)
                            : toResponse(presenca, reserva.getId(), checkIn);
                })
                .toList();

        int presentes = contar(alunos, StatusPresenca.PRESENTE);
        int ausentes = contar(alunos, StatusPresenca.AUSENTE);
        int pendentes = contar(alunos, StatusPresenca.PENDENTE);
        return new ListaPresencasAulaResponse(aulaId, reservas.size(), presentes, ausentes, pendentes, alunos);
    }

    @Transactional(readOnly = true)
    public PresencaAlunoResponse consultarPresencaPersonal(Long aulaId) {
        Usuario usuario = usuarioAdminOuProfessor();
        Long academiaId = permissaoService.academiaAtualId();
        AulaPersonal aula = aulaPersonalRepository.buscarDetalhePorId(aulaId, academiaId)
                .orElseThrow(() -> new RuntimeException("Aula personal não encontrada com ID: " + aulaId));
        validarAcessoAula(aula.getProfessor(), usuario);

        PresencaAula presenca = presencaRepository.findByAcademiaIdAndAulaPersonalId(academiaId, aulaId).orElse(null);
        CheckIn checkIn = checkInRepository
                .findByAcademiaIdAndChaveCheckInAtivoAndAtivoTrue(academiaId, chave("PERSONAL", aulaId, aula.getAluno().getId()))
                .orElse(null);
        return presenca == null
                ? new PresencaAlunoResponse(aula.getAluno().getId(), aula.getAluno().getNome(), null,
                StatusPresenca.PENDENTE, false, null, null, null)
                : toResponse(presenca, null, checkIn);
    }

    @Transactional(readOnly = true)
    public HistoricoCheckInAlunoResponse listarHistoricoAluno(Long alunoId, LocalDate inicio, LocalDate fim) {
        Usuario usuario = permissaoService.usuarioAtual();
        Long academiaId = permissaoService.academiaAtualId();
        if (usuario.getPerfil() == PerfilUsuario.ALUNO) {
            if (usuario.getAluno() == null || !alunoId.equals(usuario.getAluno().getId())) {
                throw new AccessDeniedException("Aluno só pode consultar o próprio histórico");
            }
        } else {
            permissaoService.exigirAdminOuProfessor();
        }
        alunoRepository.findByIdAndAcademiaId(alunoId, academiaId)
                .orElseThrow(() -> new RuntimeException("Aluno não encontrado com ID: " + alunoId));
        List<CheckInResponse> checkIns = checkInRepository
                .findByAcademiaIdAndAlunoIdAndAtivoTrueAndDataHoraBetweenOrderByDataHoraDesc(
                        academiaId, alunoId, inicio.atStartOfDay(), fim.plusDays(1).atStartOfDay())
                .stream()
                .map(this::toCheckInResponse)
                .toList();
        return new HistoricoCheckInAlunoResponse(alunoId, checkIns.size(), checkIns);
    }

    @Transactional(readOnly = true)
    public HistoricoCheckInAlunoResponse listarMeuHistorico(LocalDate inicio, LocalDate fim) {
        Usuario usuario = permissaoService.usuarioAtual();
        if (usuario.getPerfil() != PerfilUsuario.ALUNO || usuario.getAluno() == null) {
            throw new AccessDeniedException("Endpoint disponível apenas para aluno autenticado");
        }
        return listarHistoricoAluno(usuario.getAluno().getId(), inicio, fim);
    }

    private Usuario usuarioAdminOuProfessor() {
        permissaoService.exigirAdminOuProfessor();
        return permissaoService.usuarioAtual();
    }

    private void validarAcessoAula(Professor professor, Usuario usuario) {
        if (usuario.getPerfil() == PerfilUsuario.PROFESSOR) {
            if (usuario.getProfessor() == null || !professor.getId().equals(usuario.getProfessor().getId())) {
                throw new AccessDeniedException("Professor só pode alterar presença das próprias aulas");
            }
        }
    }

    private void validarAulaPermitePresenca(StatusAula status) {
        if (status != StatusAula.AGENDADA && status != StatusAula.REALIZADA) {
            throw new RuntimeException("Presença permitida apenas para aula agendada ou realizada");
        }
    }

    private void validarInicioAula(LocalDate data, java.time.LocalTime horaInicio) {
        LocalDateTime inicio = LocalDateTime.of(data, horaInicio);
        if (LocalDateTime.now(clock).isBefore(inicio)) {
            throw new RuntimeException("Presença só pode ser registrada a partir do início da aula");
        }
    }

    private PresencaAula salvarPresencaTurma(AulaTurma aula, Aluno aluno, Usuario usuario, RegistrarPresencaRequest request) {
        String chave = chave("TURMA", aula.getId(), aluno.getId());
        PresencaAula presenca = presencaRepository.findByAcademiaIdAndChavePresenca(aula.getAcademia().getId(), chave)
                .orElseGet(PresencaAula::new);
        if (presenca.getId() == null) {
            presenca.setAcademia(aula.getAcademia());
            presenca.setAulaTurma(aula);
            presenca.setAluno(aluno);
            presenca.setChavePresenca(chave);
        }
        aplicarPresenca(presenca, usuario, request);
        return presencaRepository.save(presenca);
    }

    private PresencaAula salvarPresencaPersonal(AulaPersonal aula, Aluno aluno, Usuario usuario, RegistrarPresencaRequest request) {
        String chave = chave("PERSONAL", aula.getId(), aluno.getId());
        PresencaAula presenca = presencaRepository.findByAcademiaIdAndChavePresenca(aula.getAcademia().getId(), chave)
                .orElseGet(PresencaAula::new);
        if (presenca.getId() == null) {
            presenca.setAcademia(aula.getAcademia());
            presenca.setAulaPersonal(aula);
            presenca.setAluno(aluno);
            presenca.setChavePresenca(chave);
        }
        aplicarPresenca(presenca, usuario, request);
        return presencaRepository.save(presenca);
    }

    private void aplicarPresenca(PresencaAula presenca, Usuario usuario, RegistrarPresencaRequest request) {
        presenca.setStatusAnterior(presenca.getStatus());
        presenca.setStatus(request.getStatus());
        presenca.setObservacao(request.getObservacao());
        presenca.setRegistradoEm(LocalDateTime.now(clock));
        presenca.setRegistradoPor(usuario);
    }

    private CheckIn sincronizarCheckIn(PresencaAula presenca, Aluno aluno, AulaTurma aulaTurma, AulaPersonal aulaPersonal,
                                      OrigemCheckIn origem, Usuario usuario, String observacao) {
        String tipo = aulaTurma == null ? "PERSONAL" : "TURMA";
        Long aulaId = aulaTurma == null ? aulaPersonal.getId() : aulaTurma.getId();
        String chave = chave(tipo, aulaId, aluno.getId());
        CheckIn ativo = checkInRepository.findByAcademiaIdAndChaveCheckInAtivoAndAtivoTrue(aluno.getAcademia().getId(), chave)
                .orElse(null);

        if (presenca.getStatus() == StatusPresenca.PRESENTE) {
            if (ativo != null) {
                return ativo;
            }
            CheckIn checkIn = new CheckIn();
            checkIn.setAcademia(aluno.getAcademia());
            checkIn.setAluno(aluno);
            checkIn.setAulaTurma(aulaTurma);
            checkIn.setAulaPersonal(aulaPersonal);
            checkIn.setProfessor(aulaTurma == null ? aulaPersonal.getProfessor() : aulaTurma.getProfessor());
            checkIn.setDataHora(LocalDateTime.now(clock));
            checkIn.setOrigem(origem);
            checkIn.setConfirmadoPor(usuario);
            checkIn.setAtivo(true);
            checkIn.setObservacao(observacao);
            checkIn.setChaveCheckInAtivo(chave);
            aluno.setQuantidadeCheckins((aluno.getQuantidadeCheckins() == null ? 0 : aluno.getQuantidadeCheckins()) + 1);
            alunoRepository.save(aluno);
            return checkInRepository.save(checkIn);
        }

        if (ativo != null) {
            ativo.setAtivo(false);
            ativo.setInvalidadoEm(LocalDateTime.now(clock));
            ativo.setChaveCheckInAtivo(null);
            ativo.setObservacao(observacao);
            aluno.setQuantidadeCheckins(Math.max(0, aluno.getQuantidadeCheckins() == null ? 0 : aluno.getQuantidadeCheckins() - 1));
            alunoRepository.save(aluno);
            checkInRepository.save(ativo);
        }
        return null;
    }

    private PresencaAlunoResponse toResponse(PresencaAula presenca, Long reservaId, CheckIn checkIn) {
        String registradoPor = presenca.getRegistradoPor() == null ? null : presenca.getRegistradoPor().getNome();
        return new PresencaAlunoResponse(
                presenca.getAluno().getId(),
                presenca.getAluno().getNome(),
                reservaId,
                presenca.getStatus(),
                checkIn != null && Boolean.TRUE.equals(checkIn.getAtivo()),
                checkIn == null ? null : checkIn.getId(),
                presenca.getRegistradoEm(),
                registradoPor
        );
    }

    private CheckInResponse toCheckInResponse(CheckIn checkIn) {
        return new CheckInResponse(
                checkIn.getId(),
                checkIn.getAluno().getId(),
                checkIn.getAluno().getNome(),
                checkIn.getAulaTurma() == null ? null : checkIn.getAulaTurma().getId(),
                checkIn.getAulaPersonal() == null ? null : checkIn.getAulaPersonal().getId(),
                checkIn.getDataHora(),
                checkIn.getOrigem(),
                checkIn.getConfirmadoPor() == null ? null : checkIn.getConfirmadoPor().getNome()
        );
    }

    private int contar(List<PresencaAlunoResponse> alunos, StatusPresenca status) {
        return (int) alunos.stream().filter(aluno -> aluno.getStatusPresenca() == status).count();
    }

    private OrigemCheckIn origemManual(Usuario usuario) {
        return usuario.getPerfil() == PerfilUsuario.ADMIN ? OrigemCheckIn.ADMIN : OrigemCheckIn.PROFESSOR;
    }

    private String chave(String tipo, Long aulaId, Long alunoId) {
        return tipo + ":" + aulaId + ":" + alunoId;
    }
}
