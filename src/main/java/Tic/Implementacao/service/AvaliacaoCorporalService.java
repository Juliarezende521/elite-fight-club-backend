package Tic.Implementacao.service;

import Tic.Implementacao.dto.dashboard.aluno.ResumoAvaliacaoCorporalDashboardResponse;
import Tic.Implementacao.dto.evolucaofisica.avaliacao.*;
import Tic.Implementacao.exception.ConflitoException;
import Tic.Implementacao.model.*;
import Tic.Implementacao.repository.AlunoRepository;
import Tic.Implementacao.repository.AvaliacaoCorporalRepository;
import Tic.Implementacao.repository.BioimpedanciaRepository;
import Tic.Implementacao.repository.MedidasCorporaisRepository;
import Tic.Implementacao.security.PermissaoService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AvaliacaoCorporalService {

    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2);
    private static final BigDecimal CEM = new BigDecimal("100.00");
    private static final BigDecimal ALTURA_MAXIMA = new BigDecimal("3.00");
    private static final BigDecimal PESO_MAXIMO = new BigDecimal("500.00");
    private static final BigDecimal MEDIDA_MAXIMA = new BigDecimal("300.00");

    private final AvaliacaoCorporalRepository avaliacaoRepository;
    private final MedidasCorporaisRepository medidasRepository;
    private final BioimpedanciaRepository bioimpedanciaRepository;
    private final AlunoRepository alunoRepository;
    private final PermissaoService permissaoService;
    private final Clock clock;

    public AvaliacaoCorporalService(AvaliacaoCorporalRepository avaliacaoRepository,
                                    MedidasCorporaisRepository medidasRepository,
                                    BioimpedanciaRepository bioimpedanciaRepository,
                                    AlunoRepository alunoRepository,
                                    PermissaoService permissaoService,
                                    Clock clock) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.medidasRepository = medidasRepository;
        this.bioimpedanciaRepository = bioimpedanciaRepository;
        this.alunoRepository = alunoRepository;
        this.permissaoService = permissaoService;
        this.clock = clock;
    }

    @Transactional
    public AvaliacaoCorporalResponse cadastrar(Long alunoId, CriarAvaliacaoCorporalRequest request) {
        permissaoService.exigirAdminOuProfessor();
        validarRequest(request);
        Usuario usuario = permissaoService.usuarioAtual();
        Aluno aluno = buscarAlunoAtivo(alunoId);
        Long academiaId = permissaoService.academiaAtualId();
        if (avaliacaoRepository.existsByAcademiaIdAndAlunoIdAndDataAvaliacaoAndAtivaTrue(academiaId, aluno.getId(), request.getDataAvaliacao())) {
            throw new ConflitoException("Já existe avaliação corporal ativa para este aluno nesta data");
        }

        AvaliacaoCorporal avaliacao = new AvaliacaoCorporal();
        avaliacao.setAcademia(aluno.getAcademia());
        avaliacao.setAluno(aluno);
        aplicarDadosBasicos(avaliacao, request);
        avaliacao.setCriadaEm(LocalDateTime.now(clock));
        avaliacao.setCriadaPor(usuario);
        avaliacao.setAtiva(true);
        AvaliacaoCorporal salva = avaliacaoRepository.save(avaliacao);
        MedidasCorporais medidas = salvarMedidas(salva, request.getMedidas(), null);
        Bioimpedancia bio = salvarBioimpedancia(salva, request.getBioimpedancia(), null);
        return toResponse(salva, medidas, bio);
    }

    @Transactional
    public AvaliacaoCorporalResponse atualizar(Long alunoId, Long avaliacaoId, AtualizarAvaliacaoCorporalRequest request) {
        permissaoService.exigirAdminOuProfessor();
        validarRequest(request);
        Usuario usuario = permissaoService.usuarioAtual();
        Aluno aluno = buscarAlunoAtivo(alunoId);
        Long academiaId = permissaoService.academiaAtualId();
        AvaliacaoCorporal avaliacao = buscarAvaliacaoAtiva(aluno.getId(), avaliacaoId);
        if (avaliacaoRepository.existsByAcademiaIdAndAlunoIdAndDataAvaliacaoAndAtivaTrueAndIdNot(
                academiaId, aluno.getId(), request.getDataAvaliacao(), avaliacao.getId())) {
            throw new ConflitoException("Já existe outra avaliação corporal ativa para este aluno nesta data");
        }

        aplicarDadosBasicos(avaliacao, request);
        avaliacao.setAtualizadaEm(LocalDateTime.now(clock));
        avaliacao.setAtualizadaPor(usuario);
        AvaliacaoCorporal salva = avaliacaoRepository.save(avaliacao);
        MedidasCorporais medidas = salvarMedidas(salva, request.getMedidas(), medidasRepository.findByAvaliacaoId(salva.getId()).orElse(null));
        Bioimpedancia bio = salvarBioimpedancia(salva, request.getBioimpedancia(), bioimpedanciaRepository.findByAvaliacaoId(salva.getId()).orElse(null));
        return toResponse(salva, medidas, bio);
    }

    @Transactional
    public void excluirLogicamente(Long alunoId, Long avaliacaoId) {
        permissaoService.exigirAdminOuProfessor();
        Usuario usuario = permissaoService.usuarioAtual();
        Aluno aluno = buscarAlunoAtivo(alunoId);
        AvaliacaoCorporal avaliacao = buscarAvaliacaoAtiva(aluno.getId(), avaliacaoId);
        avaliacao.setAtiva(false);
        avaliacao.setAtualizadaEm(LocalDateTime.now(clock));
        avaliacao.setAtualizadaPor(usuario);
        avaliacaoRepository.save(avaliacao);
    }

    @Transactional(readOnly = true)
    public List<AvaliacaoCorporalResponse> listarDoAlunoAtual() {
        Usuario usuario = alunoAtual();
        return listarInterno(usuario.getAluno().getId());
    }

    @Transactional(readOnly = true)
    public List<AvaliacaoCorporalResponse> listar(Long alunoId) {
        permissaoService.exigirAdminOuProfessor();
        return listarInterno(alunoId);
    }

    @Transactional(readOnly = true)
    public AvaliacaoCorporalResponse buscarDoAlunoAtual(Long avaliacaoId) {
        Usuario usuario = alunoAtual();
        return buscarInterno(usuario.getAluno().getId(), avaliacaoId);
    }

    @Transactional(readOnly = true)
    public AvaliacaoCorporalResponse buscar(Long alunoId, Long avaliacaoId) {
        permissaoService.exigirAdminOuProfessor();
        return buscarInterno(alunoId, avaliacaoId);
    }

    @Transactional(readOnly = true)
    public ResumoAvaliacaoCorporalResponse resumoDoAlunoAtual() {
        Usuario usuario = alunoAtual();
        return resumoInterno(usuario.getAluno().getId());
    }

    @Transactional(readOnly = true)
    public ResumoAvaliacaoCorporalResponse resumo(Long alunoId) {
        permissaoService.exigirAdminOuProfessor();
        return resumoInterno(alunoId);
    }

    @Transactional(readOnly = true)
    public ComparativoAvaliacoesResponse compararDoAlunoAtual(Long avaliacaoInicialId, Long avaliacaoFinalId) {
        Usuario usuario = alunoAtual();
        return compararInterno(usuario.getAluno().getId(), avaliacaoInicialId, avaliacaoFinalId);
    }

    @Transactional(readOnly = true)
    public ComparativoAvaliacoesResponse comparar(Long alunoId, Long avaliacaoInicialId, Long avaliacaoFinalId) {
        permissaoService.exigirAdminOuProfessor();
        return compararInterno(alunoId, avaliacaoInicialId, avaliacaoFinalId);
    }

    @Transactional(readOnly = true)
    public ResumoAvaliacaoCorporalDashboardResponse buscarResumoDashboard(Long alunoId) {
        ResumoAvaliacaoCorporalResponse resumo = resumoInterno(alunoId);
        if (resumo == null) {
            return null;
        }
        return new ResumoAvaliacaoCorporalDashboardResponse(resumo.getAvaliacaoId(), resumo.getDataAvaliacao(),
                resumo.getPercentualGordura(), resumo.getMassaMuscularKg(), resumo.getCintura(),
                resumo.getPossuiBioimpedancia(), resumo.getPossuiMedidas());
    }

    private List<AvaliacaoCorporalResponse> listarInterno(Long alunoId) {
        Aluno aluno = buscarAlunoAtivo(alunoId);
        List<AvaliacaoCorporal> avaliacoes = avaliacaoRepository
                .findByAcademiaIdAndAlunoIdAndAtivaTrueOrderByDataAvaliacaoDescCriadaEmDesc(permissaoService.academiaAtualId(), aluno.getId());
        List<Long> ids = avaliacoes.stream().map(AvaliacaoCorporal::getId).toList();
        Map<Long, MedidasCorporais> medidas = ids.isEmpty() ? Map.of() : medidasRepository.findByAvaliacaoIdIn(ids).stream()
                .collect(Collectors.toMap(m -> m.getAvaliacao().getId(), Function.identity()));
        Map<Long, Bioimpedancia> bios = ids.isEmpty() ? Map.of() : bioimpedanciaRepository.findByAvaliacaoIdIn(ids).stream()
                .collect(Collectors.toMap(b -> b.getAvaliacao().getId(), Function.identity()));
        return avaliacoes.stream().map(a -> toResponse(a, medidas.get(a.getId()), bios.get(a.getId()))).toList();
    }

    private AvaliacaoCorporalResponse buscarInterno(Long alunoId, Long avaliacaoId) {
        AvaliacaoCorporal avaliacao = buscarAvaliacaoAtiva(buscarAlunoAtivo(alunoId).getId(), avaliacaoId);
        return toResponse(avaliacao, medidasRepository.findByAvaliacaoId(avaliacao.getId()).orElse(null),
                bioimpedanciaRepository.findByAvaliacaoId(avaliacao.getId()).orElse(null));
    }

    private ResumoAvaliacaoCorporalResponse resumoInterno(Long alunoId) {
        Aluno aluno = buscarAlunoAtivo(alunoId);
        return avaliacaoRepository.findFirstByAcademiaIdAndAlunoIdAndAtivaTrueOrderByDataAvaliacaoDescCriadaEmDesc(
                        permissaoService.academiaAtualId(), aluno.getId())
                .map(a -> toResumo(a, medidasRepository.findByAvaliacaoId(a.getId()).orElse(null),
                        bioimpedanciaRepository.findByAvaliacaoId(a.getId()).orElse(null)))
                .orElse(null);
    }

    private ComparativoAvaliacoesResponse compararInterno(Long alunoId, Long inicialId, Long finalId) {
        if (inicialId == null || finalId == null) {
            throw new RuntimeException("Informe as duas avaliações para comparação");
        }
        AvaliacaoCorporal inicial = buscarAvaliacaoAtiva(buscarAlunoAtivo(alunoId).getId(), inicialId);
        AvaliacaoCorporal fim = buscarAvaliacaoAtiva(alunoId, finalId);
        Map<String, BigDecimal> valoresInicial = valoresComparaveis(inicial, medidasRepository.findByAvaliacaoId(inicial.getId()).orElse(null),
                bioimpedanciaRepository.findByAvaliacaoId(inicial.getId()).orElse(null));
        Map<String, BigDecimal> valoresFinal = valoresComparaveis(fim, medidasRepository.findByAvaliacaoId(fim.getId()).orElse(null),
                bioimpedanciaRepository.findByAvaliacaoId(fim.getId()).orElse(null));
        Map<String, BigDecimal> comunsInicial = new LinkedHashMap<>();
        Map<String, BigDecimal> comunsFinal = new LinkedHashMap<>();
        Map<String, BigDecimal> diferencas = new LinkedHashMap<>();
        valoresInicial.forEach((campo, valorInicial) -> {
            BigDecimal valorFinal = valoresFinal.get(campo);
            if (valorFinal != null) {
                comunsInicial.put(campo, valorInicial);
                comunsFinal.put(campo, valorFinal);
                diferencas.put(campo, valorFinal.subtract(valorInicial).setScale(2, RoundingMode.HALF_UP));
            }
        });
        return new ComparativoAvaliacoesResponse(inicial.getId(), fim.getId(), inicial.getDataAvaliacao(), fim.getDataAvaliacao(),
                comunsInicial, comunsFinal, diferencas);
    }

    private void aplicarDadosBasicos(AvaliacaoCorporal avaliacao, CriarAvaliacaoCorporalRequest request) {
        avaliacao.setDataAvaliacao(request.getDataAvaliacao());
        avaliacao.setAlturaMetros(normalizar(request.getAlturaMetros()));
        avaliacao.setPesoRegistrado(normalizar(request.getPesoRegistrado()));
        avaliacao.setObservacao(request.getObservacao());
    }

    private MedidasCorporais salvarMedidas(AvaliacaoCorporal avaliacao, MedidasCorporaisRequest request, MedidasCorporais existente) {
        if (request == null) {
            return existente;
        }
        validarMedidas(request);
        MedidasCorporais medidas = existente == null ? new MedidasCorporais() : existente;
        medidas.setAvaliacao(avaliacao);
        copiarMedidas(request, medidas);
        return medidasRepository.save(medidas);
    }

    private Bioimpedancia salvarBioimpedancia(AvaliacaoCorporal avaliacao, BioimpedanciaRequest request, Bioimpedancia existente) {
        if (request == null) {
            return existente;
        }
        validarBioimpedancia(request);
        Bioimpedancia bio = existente == null ? new Bioimpedancia() : existente;
        bio.setAvaliacao(avaliacao);
        copiarBio(request, bio);
        return bioimpedanciaRepository.save(bio);
    }

    private void validarRequest(CriarAvaliacaoCorporalRequest request) {
        if (request == null) {
            throw new RuntimeException("Dados da avaliação são obrigatórios");
        }
        if (request.getDataAvaliacao() == null) {
            throw new RuntimeException("Data da avaliação é obrigatória");
        }
        if (request.getDataAvaliacao().isAfter(LocalDate.now(clock))) {
            throw new RuntimeException("Data da avaliação não pode ser futura");
        }
        validarOpcionalPositivo(request.getAlturaMetros(), ALTURA_MAXIMA, "Altura");
        validarOpcionalPositivo(request.getPesoRegistrado(), PESO_MAXIMO, "Peso registrado");
        validarMedidas(request.getMedidas());
        validarBioimpedancia(request.getBioimpedancia());
    }

    private void validarMedidas(MedidasCorporaisRequest medidas) {
        if (medidas == null) return;
        medidasMap(medidas).forEach((campo, valor) -> validarOpcionalPositivo(valor, MEDIDA_MAXIMA, campo));
    }

    private void validarBioimpedancia(BioimpedanciaRequest bio) {
        if (bio == null) return;
        validarPercentual(bio.getPercentualGordura(), "percentualGordura");
        validarPercentual(bio.getAguaCorporalPercentual(), "aguaCorporalPercentual");
        bioMap(bio).forEach((campo, valor) -> {
            if (!campo.equals("percentualGordura") && !campo.equals("aguaCorporalPercentual")) {
                validarNaoNegativo(valor, campo);
            }
        });
    }

    private void validarOpcionalPositivo(BigDecimal valor, BigDecimal maximo, String campo) {
        if (valor == null) return;
        if (valor.compareTo(BigDecimal.ZERO) <= 0 || valor.compareTo(maximo) > 0) {
            throw new RuntimeException(campo + " deve ser maior que zero e estar dentro do limite permitido");
        }
    }

    private void validarPercentual(BigDecimal valor, String campo) {
        if (valor == null) return;
        if (valor.compareTo(BigDecimal.ZERO) < 0 || valor.compareTo(CEM) > 0) {
            throw new RuntimeException(campo + " deve estar entre 0 e 100");
        }
    }

    private void validarNaoNegativo(BigDecimal valor, String campo) {
        if (valor != null && valor.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException(campo + " não pode ser negativo");
        }
    }

    private Aluno buscarAlunoAtivo(Long alunoId) {
        Aluno aluno = alunoRepository.findByIdAndAcademiaId(alunoId, permissaoService.academiaAtualId())
                .orElseThrow(() -> new RuntimeException("Aluno não encontrado com ID: " + alunoId));
        if (!Boolean.TRUE.equals(aluno.getAtivo())) {
            throw new RuntimeException("Aluno inativo não pode possuir avaliação corporal");
        }
        return aluno;
    }

    private AvaliacaoCorporal buscarAvaliacaoAtiva(Long alunoId, Long avaliacaoId) {
        return avaliacaoRepository.findByIdAndAcademiaIdAndAlunoIdAndAtivaTrue(avaliacaoId, permissaoService.academiaAtualId(), alunoId)
                .orElseThrow(() -> new AccessDeniedException("Avaliação corporal não encontrada para este aluno"));
    }

    private Usuario alunoAtual() {
        Usuario usuario = permissaoService.usuarioAtual();
        if (usuario.getPerfil() != PerfilUsuario.ALUNO || usuario.getAluno() == null) {
            throw new AccessDeniedException("Acesso permitido apenas para aluno autenticado");
        }
        return usuario;
    }

    private AvaliacaoCorporalResponse toResponse(AvaliacaoCorporal a, MedidasCorporais m, Bioimpedancia b) {
        return new AvaliacaoCorporalResponse(a.getId(), a.getAluno().getId(), a.getAluno().getNome(), a.getDataAvaliacao(),
                a.getAlturaMetros(), a.getPesoRegistrado(), a.getObservacao(), a.getAtiva(), a.getCriadaEm(),
                a.getAtualizadaEm(), toMedidasRequest(m), toBioRequest(b));
    }

    private ResumoAvaliacaoCorporalResponse toResumo(AvaliacaoCorporal a, MedidasCorporais m, Bioimpedancia b) {
        return new ResumoAvaliacaoCorporalResponse(a.getId(), a.getDataAvaliacao(), a.getAlturaMetros(), a.getPesoRegistrado(),
                b == null ? null : b.getPercentualGordura(), b == null ? null : b.getMassaMagraKg(),
                b == null ? null : b.getMassaMuscularKg(), m == null ? null : m.getCintura(),
                m == null ? null : m.getQuadril(), m != null, b != null);
    }

    private Map<String, BigDecimal> valoresComparaveis(AvaliacaoCorporal a, MedidasCorporais m, Bioimpedancia b) {
        Map<String, BigDecimal> valores = new LinkedHashMap<>();
        put(valores, "alturaMetros", a.getAlturaMetros());
        put(valores, "pesoRegistrado", a.getPesoRegistrado());
        if (m != null) medidasMap(toMedidasRequest(m)).forEach((k, v) -> put(valores, k, v));
        if (b != null) bioMap(toBioRequest(b)).forEach((k, v) -> put(valores, k, v));
        return valores;
    }

    private void copiarMedidas(MedidasCorporaisRequest r, MedidasCorporais m) {
        m.setPescoco(normalizar(r.getPescoco()));
        m.setOmbros(normalizar(r.getOmbros()));
        m.setTorax(normalizar(r.getTorax()));
        m.setCintura(normalizar(r.getCintura()));
        m.setAbdomen(normalizar(r.getAbdomen()));
        m.setQuadril(normalizar(r.getQuadril()));
        m.setBracoDireito(normalizar(r.getBracoDireito()));
        m.setBracoEsquerdo(normalizar(r.getBracoEsquerdo()));
        m.setAntebracoDireito(normalizar(r.getAntebracoDireito()));
        m.setAntebracoEsquerdo(normalizar(r.getAntebracoEsquerdo()));
        m.setCoxaDireita(normalizar(r.getCoxaDireita()));
        m.setCoxaEsquerda(normalizar(r.getCoxaEsquerda()));
        m.setPanturrilhaDireita(normalizar(r.getPanturrilhaDireita()));
        m.setPanturrilhaEsquerda(normalizar(r.getPanturrilhaEsquerda()));
    }

    private void copiarBio(BioimpedanciaRequest r, Bioimpedancia b) {
        b.setPercentualGordura(normalizar(r.getPercentualGordura()));
        b.setMassaGordaKg(normalizar(r.getMassaGordaKg()));
        b.setMassaMagraKg(normalizar(r.getMassaMagraKg()));
        b.setMassaMuscularKg(normalizar(r.getMassaMuscularKg()));
        b.setAguaCorporalPercentual(normalizar(r.getAguaCorporalPercentual()));
        b.setGorduraVisceral(normalizar(r.getGorduraVisceral()));
        b.setMassaOsseaKg(normalizar(r.getMassaOsseaKg()));
        b.setMetabolismoBasalKcal(normalizar(r.getMetabolismoBasalKcal()));
        b.setIdadeMetabolica(normalizar(r.getIdadeMetabolica()));
    }

    private MedidasCorporaisRequest toMedidasRequest(MedidasCorporais m) {
        if (m == null) return null;
        MedidasCorporaisRequest r = new MedidasCorporaisRequest();
        r.setPescoco(m.getPescoco()); r.setOmbros(m.getOmbros()); r.setTorax(m.getTorax());
        r.setCintura(m.getCintura()); r.setAbdomen(m.getAbdomen()); r.setQuadril(m.getQuadril());
        r.setBracoDireito(m.getBracoDireito()); r.setBracoEsquerdo(m.getBracoEsquerdo());
        r.setAntebracoDireito(m.getAntebracoDireito()); r.setAntebracoEsquerdo(m.getAntebracoEsquerdo());
        r.setCoxaDireita(m.getCoxaDireita()); r.setCoxaEsquerda(m.getCoxaEsquerda());
        r.setPanturrilhaDireita(m.getPanturrilhaDireita()); r.setPanturrilhaEsquerda(m.getPanturrilhaEsquerda());
        return r;
    }

    private BioimpedanciaRequest toBioRequest(Bioimpedancia b) {
        if (b == null) return null;
        BioimpedanciaRequest r = new BioimpedanciaRequest();
        r.setPercentualGordura(b.getPercentualGordura()); r.setMassaGordaKg(b.getMassaGordaKg());
        r.setMassaMagraKg(b.getMassaMagraKg()); r.setMassaMuscularKg(b.getMassaMuscularKg());
        r.setAguaCorporalPercentual(b.getAguaCorporalPercentual()); r.setGorduraVisceral(b.getGorduraVisceral());
        r.setMassaOsseaKg(b.getMassaOsseaKg()); r.setMetabolismoBasalKcal(b.getMetabolismoBasalKcal());
        r.setIdadeMetabolica(b.getIdadeMetabolica());
        return r;
    }

    private Map<String, BigDecimal> medidasMap(MedidasCorporaisRequest r) {
        Map<String, BigDecimal> map = new LinkedHashMap<>();
        put(map, "pescoco", r.getPescoco()); put(map, "ombros", r.getOmbros()); put(map, "torax", r.getTorax());
        put(map, "cintura", r.getCintura()); put(map, "abdomen", r.getAbdomen()); put(map, "quadril", r.getQuadril());
        put(map, "bracoDireito", r.getBracoDireito()); put(map, "bracoEsquerdo", r.getBracoEsquerdo());
        put(map, "antebracoDireito", r.getAntebracoDireito()); put(map, "antebracoEsquerdo", r.getAntebracoEsquerdo());
        put(map, "coxaDireita", r.getCoxaDireita()); put(map, "coxaEsquerda", r.getCoxaEsquerda());
        put(map, "panturrilhaDireita", r.getPanturrilhaDireita()); put(map, "panturrilhaEsquerda", r.getPanturrilhaEsquerda());
        return map;
    }

    private Map<String, BigDecimal> bioMap(BioimpedanciaRequest r) {
        Map<String, BigDecimal> map = new LinkedHashMap<>();
        put(map, "percentualGordura", r.getPercentualGordura()); put(map, "massaGordaKg", r.getMassaGordaKg());
        put(map, "massaMagraKg", r.getMassaMagraKg()); put(map, "massaMuscularKg", r.getMassaMuscularKg());
        put(map, "aguaCorporalPercentual", r.getAguaCorporalPercentual()); put(map, "gorduraVisceral", r.getGorduraVisceral());
        put(map, "massaOsseaKg", r.getMassaOsseaKg()); put(map, "metabolismoBasalKcal", r.getMetabolismoBasalKcal());
        put(map, "idadeMetabolica", r.getIdadeMetabolica());
        return map;
    }

    private void put(Map<String, BigDecimal> map, String key, BigDecimal value) {
        if (value != null) map.put(key, value);
    }

    private BigDecimal normalizar(BigDecimal valor) {
        return valor == null ? null : valor.setScale(2, RoundingMode.HALF_UP);
    }
}
