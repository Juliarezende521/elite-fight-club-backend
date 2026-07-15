package Tic.Implementacao.service;

import Tic.Implementacao.dto.graduacao.ElegibilidadeGraduacaoResponse;
import Tic.Implementacao.dto.graduacao.certificado.*;
import Tic.Implementacao.model.*;
import Tic.Implementacao.repository.*;
import Tic.Implementacao.security.PermissaoService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CertificadoGraduacaoService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final CertificadoGraduacaoRepository certificadoRepository;
    private final AvaliacaoGraduacaoRepository avaliacaoRepository;
    private final HistoricoGraduacaoRepository historicoRepository;
    private final AlunoRepository alunoRepository;
    private final CheckInRepository checkInRepository;
    private final GraduacaoAlunoService graduacaoAlunoService;
    private final PermissaoService permissaoService;
    private final Clock clock;

    public CertificadoGraduacaoService(CertificadoGraduacaoRepository certificadoRepository,
                                       AvaliacaoGraduacaoRepository avaliacaoRepository,
                                       HistoricoGraduacaoRepository historicoRepository,
                                       AlunoRepository alunoRepository,
                                       CheckInRepository checkInRepository,
                                       GraduacaoAlunoService graduacaoAlunoService,
                                       PermissaoService permissaoService,
                                       Clock clock) {
        this.certificadoRepository = certificadoRepository;
        this.avaliacaoRepository = avaliacaoRepository;
        this.historicoRepository = historicoRepository;
        this.alunoRepository = alunoRepository;
        this.checkInRepository = checkInRepository;
        this.graduacaoAlunoService = graduacaoAlunoService;
        this.permissaoService = permissaoService;
        this.clock = clock;
    }

    @Transactional
    public List<CertificadoGraduacaoResponse> emitirCertificadosEventoConcluido(EventoGraduacao evento) {
        if (evento.getStatus() != StatusEventoGraduacao.CONCLUIDO) {
            return List.of();
        }
        return avaliacaoRepository.findByAcademiaIdAndEventoId(evento.getAcademia().getId(), evento.getId())
                .stream()
                .map(this::emitirSeElegivel)
                .flatMap(Optional::stream)
                .map(CertificadoGraduacaoResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public CertificadoGraduacaoResponse buscarCertificado(Long id) {
        Usuario usuario = permissaoService.usuarioAtual();
        CertificadoGraduacao certificado = certificadoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Certificado não encontrado com ID: " + id));
        validarAcessoCertificado(usuario, certificado);
        return CertificadoGraduacaoResponse.from(certificado);
    }

    @Transactional(readOnly = true)
    public List<CertificadoGraduacaoResponse> listarCertificadosDoAlunoAtual() {
        Usuario usuario = exigirAlunoAtual();
        return certificadoRepository.findByAcademiaIdAndAlunoIdOrderByDataEmissaoDesc(
                        permissaoService.academiaAtualId(), usuario.getAluno().getId())
                .stream()
                .map(CertificadoGraduacaoResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public CertificadoValidacaoResponse validarPublicamente(String codigo) {
        return certificadoRepository.findByCodigoValidacao(codigo)
                .map(certificado -> {
                    boolean ativo = Boolean.TRUE.equals(certificado.getAtivo());
                    return new CertificadoValidacaoResponse(ativo, ativo ? "VALIDO" : "CANCELADO",
                            certificado.getAluno().getNome(), certificado.getAcademia().getNome(),
                            certificado.getEvento().getTitulo(), certificado.getGraduacaoNova().getNome(),
                            certificado.getDataEmissao());
                })
                .orElseGet(() -> new CertificadoValidacaoResponse(false, "INVALIDO", null, null, null, null, null));
    }

    @Transactional
    public CertificadoGraduacaoResponse cancelarCertificado(Long id, String motivo) {
        permissaoService.exigirAdmin();
        if (motivo == null || motivo.isBlank()) {
            throw new RuntimeException("Motivo do cancelamento é obrigatório");
        }
        CertificadoGraduacao certificado = certificadoRepository.findByIdAndAcademiaId(id, permissaoService.academiaAtualId())
                .orElseThrow(() -> new RuntimeException("Certificado não encontrado com ID: " + id));
        certificado.setAtivo(false);
        certificado.setMotivoCancelamento(motivo);
        certificado.setCanceladoEm(LocalDateTime.now(clock));
        certificado.setCanceladoPor(permissaoService.usuarioAtual());
        return CertificadoGraduacaoResponse.from(certificadoRepository.save(certificado));
    }

    @Transactional(readOnly = true)
    public List<HistoricoGraduacaoCompletoResponse> historicoCompletoAluno(Long alunoId) {
        Usuario usuario = permissaoService.usuarioAtual();
        Long academiaId = permissaoService.academiaAtualId();
        if (usuario.getPerfil() == PerfilUsuario.ALUNO) {
            if (usuario.getAluno() == null || !usuario.getAluno().getId().equals(alunoId)) {
                throw new AccessDeniedException("Aluno pode consultar apenas o próprio histórico");
            }
        } else {
            permissaoService.exigirAdminOuProfessor();
        }
        buscarAluno(alunoId, academiaId);
        List<CertificadoGraduacao> certificados = certificadoRepository.findByAcademiaIdAndAlunoIdOrderByDataEmissaoDesc(academiaId, alunoId);
        Map<Long, CertificadoGraduacao> certificadoPorHistorico = certificados.stream()
                .collect(Collectors.toMap(c -> c.getHistorico().getId(), Function.identity(), (a, b) -> a));
        Map<Long, AvaliacaoGraduacao> avaliacaoPorHistorico = certificados.stream()
                .collect(Collectors.toMap(c -> c.getHistorico().getId(), CertificadoGraduacao::getAvaliacao, (a, b) -> a));
        return historicoRepository.findByAcademiaIdAndAlunoIdOrderByAlteradoEmAsc(academiaId, alunoId)
                .stream()
                .map(historico -> historicoCompleto(historico, certificadoPorHistorico.get(historico.getId()),
                        avaliacaoPorHistorico.get(historico.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public GraduacaoAlunoResumoResponse graduacoesDoAlunoAtual() {
        Usuario usuario = exigirAlunoAtual();
        Long academiaId = permissaoService.academiaAtualId();
        Aluno aluno = buscarAluno(usuario.getAluno().getId(), academiaId);
        ElegibilidadeGraduacaoResponse elegibilidade = graduacaoAlunoService.calcularElegibilidade(aluno);
        List<HistoricoGraduacaoCompletoResponse> historico = historicoCompletoAluno(aluno.getId());
        List<CertificadoGraduacaoResponse> certificados = listarCertificadosDoAlunoAtual();
        CertificadoGraduacaoResponse ultimo = certificados.isEmpty() ? null : certificados.getFirst();
        LocalDate entrada = elegibilidade.getDataEntradaGraduacaoAtual();
        int dias = entrada == null ? 0 : (int) ChronoUnit.DAYS.between(entrada, LocalDate.now(clock));
        LocalDateTime ultimaPromocao = historicoRepository.findByAcademiaIdAndAlunoIdOrderByAlteradoEmDesc(academiaId, aluno.getId())
                .stream()
                .filter(h -> Boolean.TRUE.equals(h.getPromocaoPorAvaliacao()))
                .map(HistoricoGraduacao::getAlteradoEm)
                .findFirst()
                .orElse(null);
        return new GraduacaoAlunoResumoResponse(
                aluno.getGraduacaoEntidade() == null ? null : aluno.getGraduacaoEntidade().getNome(),
                elegibilidade.getProximaGraduacaoNome(),
                elegibilidade.getCheckInsAtuais(),
                elegibilidade.getCheckInsFaltantes(),
                dias,
                entrada,
                ultimaPromocao,
                ultimo,
                historico,
                certificados
        );
    }

    @Transactional(readOnly = true)
    public DashboardProfessorGraduacaoResponse dashboardProfessor() {
        permissaoService.exigirAdminOuProfessor();
        Long academiaId = permissaoService.academiaAtualId();
        List<CertificadoGraduacaoResponse> ultimas = certificadoRepository.findTop5ByAcademiaIdAndAtivoTrueOrderByDataEmissaoDesc(academiaId)
                .stream()
                .map(CertificadoGraduacaoResponse::from)
                .toList();
        int aguardando = alunosAguardandoCertificado(academiaId).size();
        return new DashboardProfessorGraduacaoResponse(
                certificadoRepository.countByAcademiaId(academiaId),
                certificadoRepository.countByAcademiaIdAndAtivoFalse(academiaId),
                aguardando,
                ultimas
        );
    }

    @Transactional(readOnly = true)
    public byte[] exportarHistoricoCompletoCsv(Long alunoId) {
        StringBuilder csv = new StringBuilder();
        csv.append("Graduação anterior,Graduação nova,Evento,Professor responsável,Data,Nota,Observações,Certificado emitido,Número do certificado\n");
        historicoCompletoAluno(alunoId).forEach(h -> csv.append(String.join(",",
                csv(h.getGraduacaoAnterior()), csv(h.getGraduacaoNova()), csv(h.getEventoTitulo()),
                csv(h.getProfessorResponsavel()), csv(h.getData()), csv(h.getNota()), csv(h.getObservacoes()),
                csv(h.getCertificadoEmitido()), csv(h.getNumeroCertificado()))).append('\n'));
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Transactional(readOnly = true)
    public byte[] exportarCertificadosCsv() {
        Usuario usuario = permissaoService.usuarioAtual();
        List<CertificadoGraduacao> certificados;
        if (usuario.getPerfil() == PerfilUsuario.ALUNO) {
            if (usuario.getAluno() == null) {
                throw new AccessDeniedException("Aluno autenticado sem cadastro vinculado");
            }
            certificados = certificadoRepository.findByAcademiaIdAndAlunoIdOrderByDataEmissaoDesc(
                    permissaoService.academiaAtualId(), usuario.getAluno().getId());
        } else {
            permissaoService.exigirAdminOuProfessor();
            certificados = certificadoRepository.findByAcademiaIdOrderByDataEmissaoDesc(permissaoService.academiaAtualId());
        }
        StringBuilder csv = new StringBuilder();
        csv.append("Número,Código,Aluno,Evento,Graduação anterior,Graduação nova,Data emissão,Ativo\n");
        certificados.forEach(c -> csv.append(String.join(",",
                csv(c.getNumeroCertificado()), csv(c.getCodigoValidacao()), csv(c.getAluno().getNome()),
                csv(c.getEvento().getTitulo()), csv(c.getGraduacaoAnterior() == null ? null : c.getGraduacaoAnterior().getNome()),
                csv(c.getGraduacaoNova().getNome()), csv(c.getDataEmissao()), csv(c.getAtivo()))).append('\n'));
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private Optional<CertificadoGraduacao> emitirSeElegivel(AvaliacaoGraduacao avaliacao) {
        if (avaliacao.getId() != null && certificadoRepository.existsByAvaliacaoId(avaliacao.getId())) {
            return certificadoRepository.findByAvaliacaoId(avaliacao.getId());
        }
        if (!elegivelParaCertificado(avaliacao)) {
            return Optional.empty();
        }
        LocalDateTime agora = LocalDateTime.now(clock);
        CertificadoGraduacao certificado = new CertificadoGraduacao();
        certificado.setAcademia(avaliacao.getAcademia());
        certificado.setAluno(avaliacao.getAluno());
        certificado.setEvento(avaliacao.getEvento());
        certificado.setGraduacaoAnterior(avaliacao.getGraduacaoAtual());
        certificado.setGraduacaoNova(avaliacao.getGraduacaoDestino());
        certificado.setAvaliacao(avaliacao);
        certificado.setHistorico(avaliacao.getHistoricoGerado());
        certificado.setNumeroCertificado(gerarNumero(agora.getYear()));
        certificado.setCodigoValidacao(gerarCodigoValidacao());
        certificado.setDataEmissao(agora);
        certificado.setEmitidoPor(avaliacao.getPromovidoPor() == null
                ? avaliacao.getHistoricoGerado().getAlteradoPor()
                : avaliacao.getPromovidoPor());
        certificado.setObservacoes(avaliacao.getObservacaoPromocao());
        certificado.setAtivo(true);
        return Optional.of(certificadoRepository.save(certificado));
    }

    private boolean elegivelParaCertificado(AvaliacaoGraduacao avaliacao) {
        if (avaliacao.getEvento().getStatus() != StatusEventoGraduacao.CONCLUIDO) return false;
        if (!Boolean.TRUE.equals(avaliacao.getPromocaoConfirmada())) return false;
        if (avaliacao.getHistoricoGerado() == null) return false;
        if (avaliacao.getStatusPresenca() != StatusPresencaGraduacao.PRESENTE) return false;
        if (avaliacao.getResultado() != ResultadoAvaliacaoGraduacao.APROVADO) return false;
        InscricaoGraduacao inscricao = avaliacao.getInscricao();
        if (inscricao == null) return false;
        if (inscricao.getStatus() == StatusInscricaoGraduacao.CANCELADA || inscricao.getStatus() == StatusInscricaoGraduacao.EXPIRADA) return false;
        if (inscricao.getValor() == null || inscricao.getValor().signum() == 0) return true;
        return inscricao.getPagamento() != null && inscricao.getPagamento().getStatus() == StatusPagamento.PAGO;
    }

    private String gerarNumero(int ano) {
        String prefixo = "EFC-" + ano + "-";
        int tentativas = 0;
        while (tentativas++ < 20) {
            int proximo = certificadoRepository.findFirstByNumeroCertificadoStartingWithOrderByNumeroCertificadoDesc(prefixo)
                    .map(CertificadoGraduacao::getNumeroCertificado)
                    .map(numero -> Integer.parseInt(numero.substring(numero.lastIndexOf('-') + 1)))
                    .orElse(0) + tentativas;
            String numero = prefixo + String.format(Locale.ROOT, "%06d", proximo);
            if (!certificadoRepository.existsByNumeroCertificado(numero)) {
                return numero;
            }
        }
        throw new RuntimeException("Não foi possível gerar número único de certificado");
    }

    private String gerarCodigoValidacao() {
        int tentativas = 0;
        while (tentativas++ < 50) {
            String codigo = blocoHex() + "-" + blocoHex() + "-" + blocoHex();
            if (!certificadoRepository.existsByCodigoValidacao(codigo)) {
                return codigo;
            }
        }
        throw new RuntimeException("Não foi possível gerar código único de validação");
    }

    private String blocoHex() {
        return String.format(Locale.ROOT, "%04X", RANDOM.nextInt(0x10000));
    }

    private void validarAcessoCertificado(Usuario usuario, CertificadoGraduacao certificado) {
        if (usuario.getPerfil() == PerfilUsuario.ALUNO) {
            if (usuario.getAluno() == null || !usuario.getAluno().getId().equals(certificado.getAluno().getId())) {
                throw new AccessDeniedException("Aluno pode consultar apenas os próprios certificados");
            }
            return;
        }
        permissaoService.exigirAdminOuProfessor();
        if (certificado.getAcademia() == null || !certificado.getAcademia().getId().equals(permissaoService.academiaAtualId())) {
            throw new AccessDeniedException("Certificado pertence a outra academia");
        }
    }

    private HistoricoGraduacaoCompletoResponse historicoCompleto(HistoricoGraduacao historico,
                                                                 CertificadoGraduacao certificado,
                                                                 AvaliacaoGraduacao avaliacao) {
        return new HistoricoGraduacaoCompletoResponse(
                historico.getId(),
                historico.getGraduacaoAnterior() == null ? null : historico.getGraduacaoAnterior().getNome(),
                historico.getNovaGraduacao().getNome(),
                certificado == null ? null : certificado.getEvento().getId(),
                certificado == null ? null : certificado.getEvento().getTitulo(),
                historico.getAlteradoPor().getNome(),
                historico.getAlteradoEm(),
                avaliacao == null ? null : avaliacao.getNotaTecnica(),
                historico.getObservacao(),
                certificado != null,
                certificado == null ? null : certificado.getId(),
                certificado == null ? null : certificado.getNumeroCertificado()
        );
    }

    private List<AvaliacaoGraduacao> alunosAguardandoCertificado(Long academiaId) {
        return avaliacaoRepository.findByAcademiaIdAndPromocaoConfirmadaTrueAndEventoStatus(
                        academiaId, StatusEventoGraduacao.CONCLUIDO)
                .stream()
                .filter(this::elegivelParaCertificado)
                .filter(avaliacao -> !certificadoRepository.existsByAvaliacaoId(avaliacao.getId()))
                .toList();
    }

    private Usuario exigirAlunoAtual() {
        Usuario usuario = permissaoService.usuarioAtual();
        if (usuario.getPerfil() != PerfilUsuario.ALUNO || usuario.getAluno() == null) {
            throw new AccessDeniedException("Acesso permitido apenas para aluno autenticado");
        }
        return usuario;
    }

    private Aluno buscarAluno(Long alunoId, Long academiaId) {
        return alunoRepository.findByIdAndAcademiaId(alunoId, academiaId)
                .orElseThrow(() -> new RuntimeException("Aluno não encontrado com ID: " + alunoId));
    }

    private String csv(Object valor) {
        String texto = valor == null ? "" : valor.toString();
        return "\"" + texto.replace("\"", "\"\"") + "\"";
    }
}
