package Tic.Implementacao.service;

import Tic.Implementacao.dto.checkinqr.*;
import Tic.Implementacao.dto.presenca.PresencaAlunoResponse;
import Tic.Implementacao.model.*;
import Tic.Implementacao.repository.*;
import Tic.Implementacao.security.PermissaoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class CheckInQrService {

    private final AcademiaRepository academiaRepository;
    private final SessaoCheckInRepository sessaoRepository;
    private final AulaTurmaRepository aulaTurmaRepository;
    private final AulaPersonalRepository aulaPersonalRepository;
    private final ReservaAulaRepository reservaRepository;
    private final AlunoRepository alunoRepository;
    private final CheckInRepository checkInRepository;
    private final PresencaAulaService presencaAulaService;
    private final PermissaoService permissaoService;
    private final Clock clock;
    private final int minutesBefore;
    private final int minutesAfter;
    private final String frontendBaseUrl;

    public CheckInQrService(AcademiaRepository academiaRepository,
                            SessaoCheckInRepository sessaoRepository,
                            AulaTurmaRepository aulaTurmaRepository,
                            AulaPersonalRepository aulaPersonalRepository,
                            ReservaAulaRepository reservaRepository,
                            AlunoRepository alunoRepository,
                            CheckInRepository checkInRepository,
                            PresencaAulaService presencaAulaService,
                            PermissaoService permissaoService,
                            Clock clock,
                            @Value("${checkin.minutes-before:15}") int minutesBefore,
                            @Value("${checkin.minutes-after:15}") int minutesAfter,
                            @Value("${checkin.frontend-base-url:http://localhost:3000}") String frontendBaseUrl) {
        this.academiaRepository = academiaRepository;
        this.sessaoRepository = sessaoRepository;
        this.aulaTurmaRepository = aulaTurmaRepository;
        this.aulaPersonalRepository = aulaPersonalRepository;
        this.reservaRepository = reservaRepository;
        this.alunoRepository = alunoRepository;
        this.checkInRepository = checkInRepository;
        this.presencaAulaService = presencaAulaService;
        this.permissaoService = permissaoService;
        this.clock = clock;
        this.minutesBefore = minutesBefore;
        this.minutesAfter = minutesAfter;
        this.frontendBaseUrl = frontendBaseUrl;
    }

    @Transactional
    public QrAcademiaResponse consultarQrAcademia(Long academiaId) {
        permissaoService.exigirAdminOuProfessor();
        validarAcademiaAtual(academiaId);
        Academia academia = academiaRepository.findById(academiaId)
                .orElseThrow(() -> new RuntimeException("Academia não encontrada com ID: " + academiaId));
        garantirCodigoPublico(academia);
        return qrResponse(academia);
    }

    @Transactional
    public QrAcademiaResponse regenerarQrAcademia(Long academiaId) {
        permissaoService.exigirAdmin();
        validarAcademiaAtual(academiaId);
        Academia academia = academiaRepository.findById(academiaId)
                .orElseThrow(() -> new RuntimeException("Academia não encontrada com ID: " + academiaId));
        academia.setCodigoCheckInPublico(gerarCodigoUnico());
        academiaRepository.save(academia);
        return qrResponse(academia);
    }

    @Transactional
    public SessaoCheckInResponse abrirSessaoTurma(Long aulaId) {
        Usuario usuario = usuarioAdminOuProfessor();
        Long academiaId = permissaoService.academiaAtualId();
        AulaTurma aula = aulaTurmaRepository.buscarDetalhePorId(aulaId, academiaId)
                .orElseThrow(() -> new RuntimeException("Aula de turma não encontrada com ID: " + aulaId));
        validarAcessoProfessor(aula.getProfessor(), usuario);
        validarAulaParaSessao(aula.getStatus());
        validarJanelaAbertura(aula.getData().atTime(aula.getHoraInicio()));
        validarSessaoDuplicada(academiaId, "TURMA:" + aulaId);

        SessaoCheckIn sessao = novaSessao(aula.getAcademia(), usuario, aula.getData().atTime(aula.getHoraInicio()), "TURMA:" + aulaId);
        sessao.setAulaTurma(aula);
        return toResponse(sessaoRepository.save(sessao));
    }

    @Transactional
    public SessaoCheckInResponse abrirSessaoPersonal(Long aulaId) {
        Usuario usuario = usuarioAdminOuProfessor();
        Long academiaId = permissaoService.academiaAtualId();
        AulaPersonal aula = aulaPersonalRepository.buscarDetalhePorId(aulaId, academiaId)
                .orElseThrow(() -> new RuntimeException("Aula personal não encontrada com ID: " + aulaId));
        validarAcessoProfessor(aula.getProfessor(), usuario);
        validarAulaParaSessao(aula.getStatus());
        validarJanelaAbertura(aula.getData().atTime(aula.getHoraInicio()));
        validarSessaoDuplicada(academiaId, "PERSONAL:" + aulaId);

        SessaoCheckIn sessao = novaSessao(aula.getAcademia(), usuario, aula.getData().atTime(aula.getHoraInicio()), "PERSONAL:" + aulaId);
        sessao.setAulaPersonal(aula);
        return toResponse(sessaoRepository.save(sessao));
    }

    @Transactional
    public SessaoCheckInResponse encerrarSessao(Long sessaoId) {
        Usuario usuario = usuarioAdminOuProfessor();
        Long academiaId = permissaoService.academiaAtualId();
        SessaoCheckIn sessao = sessaoRepository.findByIdAndAcademiaIdForUpdate(sessaoId, academiaId)
                .orElseThrow(() -> new RuntimeException("Sessão de check-in não encontrada com ID: " + sessaoId));
        validarAcessoSessao(sessao, usuario);
        sessao.setStatus(StatusSessaoCheckIn.ENCERRADA);
        sessao.setEncerradaEm(LocalDateTime.now(clock));
        sessao.setChaveSessaoAtiva(null);
        return toResponse(sessaoRepository.save(sessao));
    }

    @Transactional
    public SessaoCheckInResponse consultarSessaoTurma(Long aulaId) {
        Long academiaId = permissaoService.academiaAtualId();
        return sessaoRepository.findByAcademiaIdAndAulaTurmaIdAndStatus(academiaId, aulaId, StatusSessaoCheckIn.ABERTA)
                .map(this::atualizarExpirada)
                .map(this::toResponse)
                .orElseThrow(() -> new RuntimeException("Sessão aberta não encontrada para a aula de turma"));
    }

    @Transactional
    public SessaoCheckInResponse consultarSessaoPersonal(Long aulaId) {
        Long academiaId = permissaoService.academiaAtualId();
        return sessaoRepository.findByAcademiaIdAndAulaPersonalIdAndStatus(academiaId, aulaId, StatusSessaoCheckIn.ABERTA)
                .map(this::atualizarExpirada)
                .map(this::toResponse)
                .orElseThrow(() -> new RuntimeException("Sessão aberta não encontrada para a aula personal"));
    }

    @Transactional
    public OpcoesCheckInQrResponse listarOpcoes(String codigoPublico) {
        Academia academia = academiaRepository.findByCodigoCheckInPublico(codigoPublico)
                .orElseThrow(() -> new RuntimeException("QR Code inválido"));
        Aluno aluno = alunoAutenticadoParaQr(academia);
        List<OpcaoCheckInResponse> opcoes = sessoesValidasDaAcademia(academia.getId()).stream()
                .filter(sessao -> alunoPodeUsarSessao(aluno, sessao))
                .map(this::toOpcao)
                .toList();
        return new OpcoesCheckInQrResponse(academia.getNome(), opcoes);
    }

    @Transactional
    public ConfirmarCheckInQrResponse confirmar(String codigoPublico, ConfirmarCheckInQrRequest request) {
        Academia academia = academiaRepository.findByCodigoCheckInPublico(codigoPublico)
                .orElseThrow(() -> new RuntimeException("QR Code inválido"));
        Usuario usuario = permissaoService.usuarioAtual();
        Aluno aluno = alunoAutenticadoParaQr(academia);

        SessaoCheckIn sessao = sessaoRepository.findByIdAndAcademiaIdForUpdate(request.getSessaoId(), academia.getId())
                .orElseThrow(() -> new RuntimeException("Sessão de check-in não encontrada"));
        validarSessaoAbertaValida(sessao);
        if (!alunoPodeUsarSessao(aluno, sessao)) {
            throw new RuntimeException("Aluno não possui acesso a esta sessão de check-in");
        }

        String tipo = sessao.getAulaTurma() == null ? "PERSONAL" : "TURMA";
        Long aulaId = sessao.getAulaTurma() == null ? sessao.getAulaPersonal().getId() : sessao.getAulaTurma().getId();
        String chave = tipo + ":" + aulaId + ":" + aluno.getId();
        CheckIn existente = checkInRepository.findByAcademiaIdAndChaveCheckInAtivoAndAtivoTrue(academia.getId(), chave)
                .orElse(null);
        if (existente != null) {
            return respostaConfirmacao("Check-in já realizado", existente, aluno, sessao);
        }

        Aluno alunoTravado = alunoRepository.findByIdAndAcademiaIdForUpdate(aluno.getId(), academia.getId())
                .orElseThrow(() -> new RuntimeException("Aluno não encontrado com ID: " + aluno.getId()));
        PresencaAlunoResponse presenca = presencaAulaService.registrarPresencaECheckIn(
                alunoTravado,
                sessao.getAulaTurma(),
                sessao.getAulaPersonal(),
                OrigemCheckIn.QR_CODE,
                usuario,
                "Check-in via QR Code",
                false
        );
        CheckIn checkIn = checkInRepository.findByAcademiaIdAndChaveCheckInAtivoAndAtivoTrue(academia.getId(), chave)
                .orElseThrow(() -> new RuntimeException("Check-in não encontrado após confirmação"));
        return new ConfirmarCheckInQrResponse(true, "Check-in realizado com sucesso", presenca.getCheckInId(),
                aulaId, tipoItem(sessao), modalidade(sessao), alunoTravado.getQuantidadeCheckins());
    }

    private List<SessaoCheckIn> sessoesValidasDaAcademia(Long academiaId) {
        return sessaoRepository.buscarAbertasValidas(academiaId, StatusSessaoCheckIn.ABERTA, LocalDateTime.now(clock))
                .stream()
                .map(this::atualizarExpirada)
                .filter(sessao -> sessao.getStatus() == StatusSessaoCheckIn.ABERTA)
                .toList();
    }

    private Aluno alunoAutenticadoParaQr(Academia academia) {
        Usuario usuario = permissaoService.usuarioAtual();
        if (usuario.getPerfil() != PerfilUsuario.ALUNO || usuario.getAluno() == null) {
            throw new AccessDeniedException("Check-in via QR Code permitido apenas para aluno autenticado");
        }
        Aluno aluno = usuario.getAluno();
        if (aluno.getAcademia() == null || !academia.getId().equals(aluno.getAcademia().getId())) {
            throw new RuntimeException("Aluno não pertence à academia do QR Code");
        }
        if (!Boolean.TRUE.equals(aluno.getAtivo())) {
            throw new RuntimeException("Aluno inativo não pode realizar check-in");
        }
        if (aluno.getPlano() == null || !Boolean.TRUE.equals(aluno.getPlano().getAtivo())) {
            throw new RuntimeException("Aluno não possui plano ativo");
        }
        return aluno;
    }

    private boolean alunoPodeUsarSessao(Aluno aluno, SessaoCheckIn sessao) {
        if (sessao.getStatus() != StatusSessaoCheckIn.ABERTA || LocalDateTime.now(clock).isAfter(sessao.getExpiraEm())) {
            return false;
        }
        if (sessao.getAulaTurma() != null) {
            AulaTurma aula = sessao.getAulaTurma();
            return aula.getStatus() == StatusAula.AGENDADA
                    && aluno.getPlano() != null
                    && aluno.getPlano().getTipo() == TipoPlano.TURMA
                    && Boolean.TRUE.equals(aluno.getPlano().getAtivo())
                    && reservaRepository.existsByAcademiaIdAndAulaTurmaIdAndAlunoIdAndStatus(
                    sessao.getAcademia().getId(), aula.getId(), aluno.getId(), StatusReserva.CONFIRMADA);
        }
        AulaPersonal aula = sessao.getAulaPersonal();
        return aula.getStatus() == StatusAula.AGENDADA
                && aula.getAluno() != null
                && aula.getAluno().getId().equals(aluno.getId());
    }

    private void validarSessaoAbertaValida(SessaoCheckIn sessao) {
        atualizarExpirada(sessao);
        if (sessao.getStatus() != StatusSessaoCheckIn.ABERTA) {
            throw new RuntimeException("Sessão de check-in não está aberta");
        }
    }

    private SessaoCheckIn atualizarExpirada(SessaoCheckIn sessao) {
        if (sessao.getStatus() == StatusSessaoCheckIn.ABERTA && LocalDateTime.now(clock).isAfter(sessao.getExpiraEm())) {
            sessao.setStatus(StatusSessaoCheckIn.EXPIRADA);
            sessao.setChaveSessaoAtiva(null);
            sessaoRepository.save(sessao);
        }
        return sessao;
    }

    private void validarSessaoDuplicada(Long academiaId, String chaveSessao) {
        sessaoRepository.findByAcademiaIdAndChaveSessaoAtivaAndStatus(academiaId, chaveSessao, StatusSessaoCheckIn.ABERTA)
                .map(this::atualizarExpirada)
                .filter(sessao -> sessao.getStatus() == StatusSessaoCheckIn.ABERTA)
                .ifPresent(sessao -> {
                    throw new RuntimeException("Já existe sessão aberta para esta aula");
                });
    }

    private SessaoCheckIn novaSessao(Academia academia, Usuario usuario, LocalDateTime inicioAula, String chave) {
        SessaoCheckIn sessao = new SessaoCheckIn();
        sessao.setAcademia(academia);
        sessao.setStatus(StatusSessaoCheckIn.ABERTA);
        sessao.setAbertaEm(LocalDateTime.now(clock));
        sessao.setExpiraEm(inicioAula.plusMinutes(minutesAfter));
        sessao.setAbertaPor(usuario);
        sessao.setChaveSessaoAtiva(chave);
        return sessao;
    }

    private void validarJanelaAbertura(LocalDateTime inicioAula) {
        LocalDateTime agora = LocalDateTime.now(clock);
        if (agora.isBefore(inicioAula.minusMinutes(minutesBefore)) || agora.isAfter(inicioAula.plusMinutes(minutesAfter))) {
            throw new RuntimeException("Sessão fora da janela permitida de check-in");
        }
    }

    private void validarAulaParaSessao(StatusAula status) {
        if (status != StatusAula.AGENDADA) {
            throw new RuntimeException("Sessão de check-in permitida apenas para aula agendada");
        }
    }

    private Usuario usuarioAdminOuProfessor() {
        permissaoService.exigirAdminOuProfessor();
        return permissaoService.usuarioAtual();
    }

    private void validarAcessoProfessor(Professor professor, Usuario usuario) {
        if (usuario.getPerfil() == PerfilUsuario.PROFESSOR) {
            if (usuario.getProfessor() == null || !professor.getId().equals(usuario.getProfessor().getId())) {
                throw new AccessDeniedException("Professor só pode abrir sessão das próprias aulas");
            }
        }
    }

    private void validarAcessoSessao(SessaoCheckIn sessao, Usuario usuario) {
        Professor professor = sessao.getAulaTurma() == null ? sessao.getAulaPersonal().getProfessor() : sessao.getAulaTurma().getProfessor();
        validarAcessoProfessor(professor, usuario);
    }

    private void validarAcademiaAtual(Long academiaId) {
        if (!permissaoService.academiaAtualId().equals(academiaId)) {
            throw new AccessDeniedException("Acesso permitido apenas para a própria academia");
        }
    }

    private void garantirCodigoPublico(Academia academia) {
        if (academia.getCodigoCheckInPublico() == null || academia.getCodigoCheckInPublico().isBlank()) {
            academia.setCodigoCheckInPublico(gerarCodigoUnico());
            academiaRepository.save(academia);
        }
    }

    private String gerarCodigoUnico() {
        String codigo;
        do {
            codigo = UUID.randomUUID().toString().replace("-", "");
        } while (academiaRepository.existsByCodigoCheckInPublico(codigo));
        return codigo;
    }

    private QrAcademiaResponse qrResponse(Academia academia) {
        String base = frontendBaseUrl.endsWith("/") ? frontendBaseUrl.substring(0, frontendBaseUrl.length() - 1) : frontendBaseUrl;
        return new QrAcademiaResponse(academia.getId(), academia.getCodigoCheckInPublico(),
                base + "/check-in/" + academia.getCodigoCheckInPublico());
    }

    private SessaoCheckInResponse toResponse(SessaoCheckIn sessao) {
        return new SessaoCheckInResponse(sessao.getId(), sessao.getAcademia().getId(), tipoItem(sessao), aulaId(sessao),
                sessao.getStatus(), sessao.getAbertaEm(), sessao.getExpiraEm(), sessao.getEncerradaEm());
    }

    private OpcaoCheckInResponse toOpcao(SessaoCheckIn sessao) {
        if (sessao.getAulaTurma() != null) {
            AulaTurma aula = sessao.getAulaTurma();
            return new OpcaoCheckInResponse(sessao.getId(), TipoItemAgenda.TURMA, aula.getId(),
                    aula.getModalidade().getNome(), null, aula.getHoraInicio(), aula.getHoraFim());
        }
        AulaPersonal aula = sessao.getAulaPersonal();
        return new OpcaoCheckInResponse(sessao.getId(), TipoItemAgenda.PERSONAL, aula.getId(),
                null, aula.getAluno().getNome(), aula.getHoraInicio(), aula.getHoraFim());
    }

    private ConfirmarCheckInQrResponse respostaConfirmacao(String mensagem, CheckIn checkIn, Aluno aluno, SessaoCheckIn sessao) {
        return new ConfirmarCheckInQrResponse(true, mensagem, checkIn.getId(), aulaId(sessao), tipoItem(sessao),
                modalidade(sessao), aluno.getQuantidadeCheckins());
    }

    private TipoItemAgenda tipoItem(SessaoCheckIn sessao) {
        return sessao.getAulaTurma() == null ? TipoItemAgenda.PERSONAL : TipoItemAgenda.TURMA;
    }

    private Long aulaId(SessaoCheckIn sessao) {
        return sessao.getAulaTurma() == null ? sessao.getAulaPersonal().getId() : sessao.getAulaTurma().getId();
    }

    private String modalidade(SessaoCheckIn sessao) {
        return sessao.getAulaTurma() == null ? null : sessao.getAulaTurma().getModalidade().getNome();
    }
}
