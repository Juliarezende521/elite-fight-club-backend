package Tic.Implementacao.service;

import Tic.Implementacao.dto.dashboard.aluno.ProgressoMetaPesoDashboardResponse;
import Tic.Implementacao.dto.evolucaofisica.meta.*;
import Tic.Implementacao.exception.ConflitoException;
import Tic.Implementacao.model.*;
import Tic.Implementacao.repository.AlunoRepository;
import Tic.Implementacao.repository.EvolucaoPesoRepository;
import Tic.Implementacao.repository.MetaPesoRepository;
import Tic.Implementacao.security.PermissaoService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class MetaPesoService {

    private static final BigDecimal PESO_MINIMO = new BigDecimal("0.01");
    private static final BigDecimal PESO_MAXIMO = new BigDecimal("500.00");
    private static final BigDecimal CEM = new BigDecimal("100.00");

    private final MetaPesoRepository metaPesoRepository;
    private final EvolucaoPesoRepository evolucaoPesoRepository;
    private final AlunoRepository alunoRepository;
    private final PermissaoService permissaoService;
    private final Clock clock;

    public MetaPesoService(MetaPesoRepository metaPesoRepository,
                           EvolucaoPesoRepository evolucaoPesoRepository,
                           AlunoRepository alunoRepository,
                           PermissaoService permissaoService,
                           Clock clock) {
        this.metaPesoRepository = metaPesoRepository;
        this.evolucaoPesoRepository = evolucaoPesoRepository;
        this.alunoRepository = alunoRepository;
        this.permissaoService = permissaoService;
        this.clock = clock;
    }

    @Transactional
    public MetaPesoResponse criarMetaDoAlunoAtual(CriarMetaPesoRequest request) {
        Usuario usuario = alunoAtual();
        return criarMetaInterno(usuario.getAluno().getId(), request, usuario, false);
    }

    @Transactional
    public MetaPesoResponse criarMeta(Long alunoId, CriarMetaPesoRequest request) {
        permissaoService.exigirAdminOuProfessor();
        return criarMetaInterno(alunoId, request, permissaoService.usuarioAtual(), false);
    }

    @Transactional(readOnly = true)
    public List<MetaPesoResponse> listarMetasDoAlunoAtual() {
        Usuario usuario = alunoAtual();
        return listarMetasInterno(usuario.getAluno().getId());
    }

    @Transactional(readOnly = true)
    public List<MetaPesoResponse> listarMetas(Long alunoId) {
        permissaoService.exigirAdminOuProfessor();
        return listarMetasInterno(alunoId);
    }

    @Transactional(readOnly = true)
    public ProgressoMetaPesoResponse buscarMetaAtivaDoAlunoAtual() {
        Usuario usuario = alunoAtual();
        return buscarMetaAtivaInterno(usuario.getAluno().getId());
    }

    @Transactional(readOnly = true)
    public ProgressoMetaPesoResponse buscarMetaAtiva(Long alunoId) {
        permissaoService.exigirAdminOuProfessor();
        return buscarMetaAtivaInterno(alunoId);
    }

    @Transactional(readOnly = true)
    public MetaPesoResponse buscarMetaDoAlunoAtual(Long metaId) {
        Usuario usuario = alunoAtual();
        return toResponse(buscarMetaInterno(usuario.getAluno().getId(), metaId));
    }

    @Transactional(readOnly = true)
    public MetaPesoResponse buscarMeta(Long alunoId, Long metaId) {
        permissaoService.exigirAdminOuProfessor();
        return toResponse(buscarMetaInterno(alunoId, metaId));
    }

    @Transactional
    public MetaPesoResponse atualizarMetaDoAlunoAtual(Long metaId, AtualizarMetaPesoRequest request) {
        Usuario usuario = alunoAtual();
        return toResponse(atualizarMetaInterno(usuario.getAluno().getId(), metaId, request, usuario));
    }

    @Transactional
    public MetaPesoResponse atualizarMeta(Long alunoId, Long metaId, AtualizarMetaPesoRequest request) {
        permissaoService.exigirAdminOuProfessor();
        return toResponse(atualizarMetaInterno(alunoId, metaId, request, permissaoService.usuarioAtual()));
    }

    @Transactional
    public MetaPesoResponse cancelarMetaDoAlunoAtual(Long metaId, EncerrarMetaPesoRequest request) {
        Usuario usuario = alunoAtual();
        return cancelarMetaInterno(usuario.getAluno().getId(), metaId, usuario, request == null ? null : request.getMotivo());
    }

    @Transactional
    public MetaPesoResponse cancelarMeta(Long alunoId, Long metaId, EncerrarMetaPesoRequest request) {
        permissaoService.exigirAdminOuProfessor();
        return cancelarMetaInterno(alunoId, metaId, permissaoService.usuarioAtual(), request == null ? null : request.getMotivo());
    }

    @Transactional
    public MetaPesoResponse substituirMetaDoAlunoAtual(Long metaId, EncerrarMetaPesoRequest request) {
        Usuario usuario = alunoAtual();
        return substituirMetaInterno(usuario.getAluno().getId(), metaId, usuario, request);
    }

    @Transactional
    public MetaPesoResponse substituirMeta(Long alunoId, Long metaId, EncerrarMetaPesoRequest request) {
        permissaoService.exigirAdminOuProfessor();
        return substituirMetaInterno(alunoId, metaId, permissaoService.usuarioAtual(), request);
    }

    @Transactional(readOnly = true)
    public ProgressoMetaPesoDashboardResponse buscarResumoDashboard(Long alunoId) {
        ProgressoMetaPesoResponse progresso = buscarMetaAtivaInterno(alunoId);
        if (progresso == null) {
            return null;
        }
        return new ProgressoMetaPesoDashboardResponse(progresso.getMetaId(), progresso.getPesoAtual(),
                progresso.getPesoAlvo(), progresso.getQuantidadeRestante(), progresso.getPercentualProgresso(),
                progresso.getDirecao(), progresso.getMetaAtingida(), progresso.getStatus());
    }

    @Transactional
    public void concluirMetaAtivaSeAtingida(Long alunoId, Usuario usuarioResponsavel) {
        Long academiaId = permissaoService.academiaAtualId();
        Optional<MetaPeso> metaOpt = metaPesoRepository.findAtivasForUpdate(academiaId, alunoId, StatusMetaPeso.ATIVA)
                .stream()
                .findFirst();
        if (metaOpt.isEmpty()) {
            return;
        }
        MetaPeso meta = metaOpt.get();
        ProgressoMetaPesoResponse progresso = calcularProgresso(meta);
        if (Boolean.TRUE.equals(progresso.getMetaAtingida())) {
            encerrar(meta, StatusMetaPeso.CONCLUIDA, usuarioResponsavel, null);
            metaPesoRepository.save(meta);
        }
    }

    private MetaPesoResponse criarMetaInterno(Long alunoId, CriarMetaPesoRequest request, Usuario usuario, boolean substituindo) {
        validarRequestCriacao(request);
        Aluno aluno = buscarAlunoAtivoDaAcademia(alunoId);
        Long academiaId = permissaoService.academiaAtualId();
        if (!substituindo && metaPesoRepository.existsByAcademiaIdAndAlunoIdAndStatus(academiaId, aluno.getId(), StatusMetaPeso.ATIVA)) {
            throw new ConflitoException("Aluno já possui uma meta de peso ativa");
        }

        PesoInicialResolvido pesoInicial = resolverPesoInicial(academiaId, aluno.getId(), request);
        BigDecimal pesoAlvo = normalizar(request.getPesoAlvo());
        validarPesoAlvoDiferente(pesoInicial.valor(), pesoAlvo);

        MetaPeso meta = new MetaPeso();
        meta.setAcademia(aluno.getAcademia());
        meta.setAluno(aluno);
        meta.setPesoInicial(pesoInicial.valor());
        meta.setPesoInicialManual(pesoInicial.manual());
        meta.setPesoAlvo(pesoAlvo);
        meta.setDataInicio(dataInicioOuHoje(request.getDataInicio()));
        meta.setDataLimite(request.getDataLimite());
        meta.setStatus(StatusMetaPeso.ATIVA);
        meta.setObservacao(request.getObservacao());
        meta.setCriadaEm(LocalDateTime.now(clock));
        meta.setCriadaPor(usuario);
        validarDatas(meta.getDataInicio(), meta.getDataLimite());
        return toResponse(metaPesoRepository.save(meta));
    }

    private List<MetaPesoResponse> listarMetasInterno(Long alunoId) {
        Aluno aluno = buscarAlunoAtivoDaAcademia(alunoId);
        return metaPesoRepository.findByAcademiaIdAndAlunoIdOrderByCriadaEmDesc(permissaoService.academiaAtualId(), aluno.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private ProgressoMetaPesoResponse buscarMetaAtivaInterno(Long alunoId) {
        Aluno aluno = buscarAlunoAtivoDaAcademia(alunoId);
        return metaPesoRepository.findFirstByAcademiaIdAndAlunoIdAndStatusOrderByCriadaEmDesc(
                        permissaoService.academiaAtualId(), aluno.getId(), StatusMetaPeso.ATIVA)
                .map(this::calcularProgresso)
                .orElse(null);
    }

    private MetaPeso atualizarMetaInterno(Long alunoId, Long metaId, AtualizarMetaPesoRequest request, Usuario usuario) {
        validarRequestAtualizacao(request);
        MetaPeso meta = buscarMetaEditavel(alunoId, metaId);
        BigDecimal pesoAlvo = normalizar(request.getPesoAlvo());
        validarPesoAlvoDiferente(meta.getPesoInicial(), pesoAlvo);
        LocalDate dataInicio = dataInicioOuHoje(request.getDataInicio());
        validarDatas(dataInicio, request.getDataLimite());
        meta.setPesoAlvo(pesoAlvo);
        meta.setDataInicio(dataInicio);
        meta.setDataLimite(request.getDataLimite());
        meta.setObservacao(request.getObservacao());
        meta.setAtualizadaEm(LocalDateTime.now(clock));
        meta.setAtualizadaPor(usuario);
        return metaPesoRepository.save(meta);
    }

    private MetaPesoResponse cancelarMetaInterno(Long alunoId, Long metaId, Usuario usuario, String motivo) {
        MetaPeso meta = buscarMetaEditavel(alunoId, metaId);
        encerrar(meta, StatusMetaPeso.CANCELADA, usuario, motivo);
        return toResponse(metaPesoRepository.save(meta));
    }

    private MetaPesoResponse substituirMetaInterno(Long alunoId, Long metaId, Usuario usuario, EncerrarMetaPesoRequest request) {
        if (request == null || request.getNovaMeta() == null) {
            throw new RuntimeException("Nova meta é obrigatória para substituição");
        }
        MetaPeso metaAtual = buscarMetaEditavel(alunoId, metaId);
        encerrar(metaAtual, StatusMetaPeso.SUBSTITUIDA, usuario, request.getMotivo());
        metaPesoRepository.save(metaAtual);
        return criarMetaInterno(alunoId, request.getNovaMeta(), usuario, true);
    }

    private MetaPeso buscarMetaInterno(Long alunoId, Long metaId) {
        Aluno aluno = buscarAlunoAtivoDaAcademia(alunoId);
        return metaPesoRepository.findByIdAndAcademiaIdAndAlunoId(metaId, permissaoService.academiaAtualId(), aluno.getId())
                .orElseThrow(() -> new AccessDeniedException("Meta de peso não encontrada para este aluno"));
    }

    private MetaPeso buscarMetaEditavel(Long alunoId, Long metaId) {
        MetaPeso meta = buscarMetaInterno(alunoId, metaId);
        if (meta.getStatus() != StatusMetaPeso.ATIVA) {
            throw new RuntimeException("Somente metas ativas podem ser alteradas");
        }
        return meta;
    }

    private ProgressoMetaPesoResponse calcularProgresso(MetaPeso meta) {
        Long academiaId = meta.getAcademia().getId();
        Optional<EvolucaoPeso> atualOpt = evolucaoPesoRepository
                .findFirstByAcademiaIdAndAlunoIdAndAtivoTrueOrderByDataRegistroDescCriadoEmDesc(academiaId, meta.getAluno().getId());
        BigDecimal pesoAtual = atualOpt.map(EvolucaoPeso::getPeso).orElse(null);
        DirecaoMetaPeso direcao = direcao(meta);
        BigDecimal variacao = BigDecimal.ZERO.setScale(2);
        BigDecimal restante = meta.getPesoAlvo().subtract(meta.getPesoInicial()).abs().setScale(2);
        BigDecimal percentual = BigDecimal.ZERO.setScale(2);
        boolean atingida = false;

        if (pesoAtual != null) {
            if (direcao == DirecaoMetaPeso.REDUCAO) {
                variacao = meta.getPesoInicial().subtract(pesoAtual).setScale(2, RoundingMode.HALF_UP);
                restante = pesoAtual.subtract(meta.getPesoAlvo()).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
                atingida = pesoAtual.compareTo(meta.getPesoAlvo()) <= 0;
            } else {
                variacao = pesoAtual.subtract(meta.getPesoInicial()).setScale(2, RoundingMode.HALF_UP);
                restante = meta.getPesoAlvo().subtract(pesoAtual).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
                atingida = pesoAtual.compareTo(meta.getPesoAlvo()) >= 0;
            }
            BigDecimal totalNecessario = meta.getPesoAlvo().subtract(meta.getPesoInicial()).abs();
            if (totalNecessario.compareTo(BigDecimal.ZERO) > 0) {
                percentual = variacao.max(BigDecimal.ZERO)
                        .multiply(CEM)
                        .divide(totalNecessario, 2, RoundingMode.HALF_UP)
                        .min(CEM);
            }
        }

        Integer diasRestantes = meta.getDataLimite() == null
                ? null
                : Math.toIntExact(ChronoUnit.DAYS.between(LocalDate.now(clock), meta.getDataLimite()));
        return new ProgressoMetaPesoResponse(meta.getId(), meta.getAluno().getId(), meta.getPesoInicial(), pesoAtual,
                meta.getPesoAlvo(), direcao.name(), variacao, restante, percentual, atingida, meta.getStatus(),
                meta.getDataInicio(), meta.getDataLimite(), diasRestantes);
    }

    private MetaPesoResponse toResponse(MetaPeso meta) {
        return new MetaPesoResponse(meta.getId(), meta.getAluno().getId(), meta.getAluno().getNome(), meta.getPesoInicial(),
                meta.getPesoAlvo(), meta.getPesoInicialManual(), direcao(meta).name(), meta.getDataInicio(),
                meta.getDataLimite(), meta.getStatus(), meta.getObservacao(), meta.getCriadaEm(),
                meta.getAtualizadaEm(), meta.getEncerradaEm());
    }

    private void encerrar(MetaPeso meta, StatusMetaPeso status, Usuario usuario, String motivo) {
        meta.setStatus(status);
        meta.setEncerradaEm(LocalDateTime.now(clock));
        meta.setEncerradaPor(usuario);
        meta.setAtualizadaEm(LocalDateTime.now(clock));
        meta.setAtualizadaPor(usuario);
        if (motivo != null && !motivo.isBlank()) {
            meta.setObservacao(juntarObservacao(meta.getObservacao(), motivo));
        }
    }

    private String juntarObservacao(String atual, String motivo) {
        if (atual == null || atual.isBlank()) {
            return motivo;
        }
        return atual + "\n" + motivo;
    }

    private PesoInicialResolvido resolverPesoInicial(Long academiaId, Long alunoId, CriarMetaPesoRequest request) {
        Optional<EvolucaoPeso> ultimoPeso = evolucaoPesoRepository
                .findFirstByAcademiaIdAndAlunoIdAndAtivoTrueOrderByDataRegistroDescCriadoEmDesc(academiaId, alunoId);
        if (ultimoPeso.isPresent()) {
            return new PesoInicialResolvido(ultimoPeso.get().getPeso(), false);
        }
        if (request.getPesoInicialManual() == null) {
            throw new RuntimeException("Peso inicial manual é obrigatório quando o aluno ainda não possui histórico de peso");
        }
        validarPeso(request.getPesoInicialManual(), "Peso inicial manual");
        return new PesoInicialResolvido(normalizar(request.getPesoInicialManual()), true);
    }

    private Usuario alunoAtual() {
        Usuario usuario = permissaoService.usuarioAtual();
        if (usuario.getPerfil() != PerfilUsuario.ALUNO || usuario.getAluno() == null) {
            throw new AccessDeniedException("Acesso permitido apenas para aluno autenticado");
        }
        return usuario;
    }

    private Aluno buscarAlunoAtivoDaAcademia(Long alunoId) {
        Long academiaId = permissaoService.academiaAtualId();
        Aluno aluno = alunoRepository.findByIdAndAcademiaId(alunoId, academiaId)
                .orElseThrow(() -> new RuntimeException("Aluno não encontrado com ID: " + alunoId));
        if (!Boolean.TRUE.equals(aluno.getAtivo())) {
            throw new RuntimeException("Aluno inativo não pode receber meta de peso");
        }
        return aluno;
    }

    private void validarRequestCriacao(CriarMetaPesoRequest request) {
        if (request == null) {
            throw new RuntimeException("Dados da meta são obrigatórios");
        }
        validarPeso(request.getPesoAlvo(), "Peso alvo");
        LocalDate inicio = dataInicioOuHoje(request.getDataInicio());
        validarDatas(inicio, request.getDataLimite());
    }

    private void validarRequestAtualizacao(AtualizarMetaPesoRequest request) {
        if (request == null) {
            throw new RuntimeException("Dados da meta são obrigatórios");
        }
        validarPeso(request.getPesoAlvo(), "Peso alvo");
        LocalDate inicio = dataInicioOuHoje(request.getDataInicio());
        validarDatas(inicio, request.getDataLimite());
    }

    private void validarPeso(BigDecimal peso, String campo) {
        if (peso == null) {
            throw new RuntimeException(campo + " é obrigatório");
        }
        if (peso.compareTo(PESO_MINIMO) < 0 || peso.compareTo(PESO_MAXIMO) > 0) {
            throw new RuntimeException(campo + " deve ser maior que zero e menor ou igual a 500");
        }
    }

    private void validarPesoAlvoDiferente(BigDecimal inicial, BigDecimal alvo) {
        if (inicial.compareTo(alvo) == 0) {
            throw new RuntimeException("Peso alvo deve ser diferente do peso inicial");
        }
    }

    private void validarDatas(LocalDate inicio, LocalDate limite) {
        if (inicio == null) {
            throw new RuntimeException("Data de início é obrigatória");
        }
        if (inicio.isAfter(LocalDate.now(clock))) {
            throw new RuntimeException("Data de início não pode ser futura");
        }
        if (limite != null && !limite.isAfter(inicio)) {
            throw new RuntimeException("Data limite deve ser posterior à data de início");
        }
    }

    private LocalDate dataInicioOuHoje(LocalDate dataInicio) {
        return dataInicio == null ? LocalDate.now(clock) : dataInicio;
    }

    private BigDecimal normalizar(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }

    private DirecaoMetaPeso direcao(MetaPeso meta) {
        return meta.getPesoAlvo().compareTo(meta.getPesoInicial()) < 0
                ? DirecaoMetaPeso.REDUCAO
                : DirecaoMetaPeso.AUMENTO;
    }

    private record PesoInicialResolvido(BigDecimal valor, boolean manual) {
    }
}
