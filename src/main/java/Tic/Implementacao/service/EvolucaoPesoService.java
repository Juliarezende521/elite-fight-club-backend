package Tic.Implementacao.service;

import Tic.Implementacao.dto.dashboard.aluno.ResumoPesoDashboardResponse;
import Tic.Implementacao.dto.evolucaofisica.AtualizarPesoRequest;
import Tic.Implementacao.dto.evolucaofisica.EvolucaoPesoResponse;
import Tic.Implementacao.dto.evolucaofisica.RegistrarPesoRequest;
import Tic.Implementacao.dto.evolucaofisica.ResumoEvolucaoPesoResponse;
import Tic.Implementacao.exception.ConflitoException;
import Tic.Implementacao.model.Aluno;
import Tic.Implementacao.model.EvolucaoPeso;
import Tic.Implementacao.model.PerfilUsuario;
import Tic.Implementacao.model.Usuario;
import Tic.Implementacao.repository.AlunoRepository;
import Tic.Implementacao.repository.EvolucaoPesoRepository;
import Tic.Implementacao.security.PermissaoService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class EvolucaoPesoService {

    private static final BigDecimal PESO_MINIMO = new BigDecimal("0.01");
    private static final BigDecimal PESO_MAXIMO = new BigDecimal("500.00");

    private final EvolucaoPesoRepository evolucaoPesoRepository;
    private final AlunoRepository alunoRepository;
    private final PermissaoService permissaoService;
    private final MetaPesoService metaPesoService;
    private final Clock clock;

    public EvolucaoPesoService(EvolucaoPesoRepository evolucaoPesoRepository,
                               AlunoRepository alunoRepository,
                               PermissaoService permissaoService,
                               MetaPesoService metaPesoService,
                               Clock clock) {
        this.evolucaoPesoRepository = evolucaoPesoRepository;
        this.alunoRepository = alunoRepository;
        this.permissaoService = permissaoService;
        this.metaPesoService = metaPesoService;
        this.clock = clock;
    }

    @Transactional
    public EvolucaoPesoResponse registrarPesoDoAlunoAtual(RegistrarPesoRequest request) {
        Usuario usuario = alunoAtual();
        return registrarPesoInterno(usuario.getAluno().getId(), request, usuario);
    }

    @Transactional
    public EvolucaoPesoResponse registrarPeso(Long alunoId, RegistrarPesoRequest request) {
        permissaoService.exigirAdminOuProfessor();
        return registrarPesoInterno(alunoId, request, permissaoService.usuarioAtual());
    }

    @Transactional(readOnly = true)
    public List<EvolucaoPesoResponse> listarHistoricoDoAlunoAtual(LocalDate inicio, LocalDate fim) {
        Usuario usuario = alunoAtual();
        return listarHistoricoInterno(usuario.getAluno().getId(), inicio, fim);
    }

    @Transactional(readOnly = true)
    public List<EvolucaoPesoResponse> listarHistorico(Long alunoId, LocalDate inicio, LocalDate fim) {
        permissaoService.exigirAdminOuProfessor();
        return listarHistoricoInterno(alunoId, inicio, fim);
    }

    private List<EvolucaoPesoResponse> listarHistoricoInterno(Long alunoId, LocalDate inicio, LocalDate fim) {
        Aluno aluno = buscarAlunoAtivoDaAcademia(alunoId);
        validarPeriodo(inicio, fim);
        Long academiaId = permissaoService.academiaAtualId();
        List<EvolucaoPeso> registros = inicio == null || fim == null
                ? evolucaoPesoRepository.findByAcademiaIdAndAlunoIdAndAtivoTrueOrderByDataRegistroDescCriadoEmDesc(academiaId, aluno.getId())
                : evolucaoPesoRepository.findByAcademiaIdAndAlunoIdAndAtivoTrueAndDataRegistroBetweenOrderByDataRegistroDescCriadoEmDesc(
                academiaId, aluno.getId(), inicio, fim);
        return registros.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ResumoEvolucaoPesoResponse buscarResumoDoAlunoAtual() {
        Usuario usuario = alunoAtual();
        return buscarResumoInterno(usuario.getAluno().getId());
    }

    @Transactional(readOnly = true)
    public ResumoEvolucaoPesoResponse buscarResumo(Long alunoId) {
        permissaoService.exigirAdminOuProfessor();
        return buscarResumoInterno(alunoId);
    }

    @Transactional(readOnly = true)
    public ResumoPesoDashboardResponse buscarResumoDashboard(Long alunoId) {
        Aluno aluno = buscarAlunoAtivoDaAcademia(alunoId);
        ResumoEvolucaoPesoResponse resumo = montarResumo(aluno.getId());
        if (resumo.getTotalRegistros() == 0) {
            return null;
        }
        return new ResumoPesoDashboardResponse(resumo.getPesoAtual(), resumo.getDataPesoAtual(),
                resumo.getDiferencaPeso(), resumo.getMenorPeso(), resumo.getMaiorPeso(), resumo.getTotalRegistros());
    }

    private ResumoEvolucaoPesoResponse buscarResumoInterno(Long alunoId) {
        Aluno aluno = buscarAlunoAtivoDaAcademia(alunoId);
        return montarResumo(aluno.getId());
    }

    @Transactional
    public EvolucaoPesoResponse atualizarPesoDoAlunoAtual(Long registroId, AtualizarPesoRequest request) {
        Usuario usuario = alunoAtual();
        return atualizarPesoInterno(usuario.getAluno().getId(), registroId, request, usuario);
    }

    @Transactional
    public EvolucaoPesoResponse atualizarPeso(Long alunoId, Long registroId, AtualizarPesoRequest request) {
        permissaoService.exigirAdminOuProfessor();
        return atualizarPesoInterno(alunoId, registroId, request, permissaoService.usuarioAtual());
    }

    @Transactional
    public void excluirLogicamenteDoAlunoAtual(Long registroId) {
        Usuario usuario = alunoAtual();
        excluirLogicamenteInterno(usuario.getAluno().getId(), registroId, usuario);
    }

    @Transactional
    public void excluirLogicamente(Long alunoId, Long registroId) {
        permissaoService.exigirAdminOuProfessor();
        excluirLogicamenteInterno(alunoId, registroId, permissaoService.usuarioAtual());
    }

    private EvolucaoPesoResponse registrarPesoInterno(Long alunoId, RegistrarPesoRequest request, Usuario usuario) {
        validarRegistro(request.getPeso(), request.getDataRegistro());
        Aluno aluno = buscarAlunoAtivoDaAcademia(alunoId);
        Long academiaId = permissaoService.academiaAtualId();
        if (evolucaoPesoRepository.existsByAcademiaIdAndAlunoIdAndDataRegistroAndAtivoTrue(
                academiaId, aluno.getId(), request.getDataRegistro())) {
            throw new ConflitoException("Já existe registro de peso ativo para este aluno nesta data");
        }

        EvolucaoPeso registro = new EvolucaoPeso();
        registro.setAcademia(aluno.getAcademia());
        registro.setAluno(aluno);
        registro.setPeso(normalizarPeso(request.getPeso()));
        registro.setDataRegistro(request.getDataRegistro());
        registro.setObservacao(request.getObservacao());
        registro.setCriadoEm(LocalDateTime.now(clock));
        registro.setCriadoPor(usuario);
        registro.setAtivo(true);
        EvolucaoPeso salvo = evolucaoPesoRepository.save(registro);
        metaPesoService.concluirMetaAtivaSeAtingida(aluno.getId(), usuario);
        return toResponse(salvo);
    }

    private EvolucaoPesoResponse atualizarPesoInterno(Long alunoId, Long registroId, AtualizarPesoRequest request, Usuario usuario) {
        validarRegistro(request.getPeso(), request.getDataRegistro());
        Aluno aluno = buscarAlunoAtivoDaAcademia(alunoId);
        Long academiaId = permissaoService.academiaAtualId();
        EvolucaoPeso registro = buscarRegistroAtivo(registroId, academiaId, aluno.getId());
        if (evolucaoPesoRepository.existsByAcademiaIdAndAlunoIdAndDataRegistroAndAtivoTrueAndIdNot(
                academiaId, aluno.getId(), request.getDataRegistro(), registro.getId())) {
            throw new ConflitoException("Já existe outro registro de peso ativo para este aluno nesta data");
        }
        registro.setPeso(normalizarPeso(request.getPeso()));
        registro.setDataRegistro(request.getDataRegistro());
        registro.setObservacao(request.getObservacao());
        registro.setAtualizadoEm(LocalDateTime.now(clock));
        registro.setAtualizadoPor(usuario);
        EvolucaoPeso salvo = evolucaoPesoRepository.save(registro);
        metaPesoService.concluirMetaAtivaSeAtingida(aluno.getId(), usuario);
        return toResponse(salvo);
    }

    private void excluirLogicamenteInterno(Long alunoId, Long registroId, Usuario usuario) {
        Aluno aluno = buscarAlunoAtivoDaAcademia(alunoId);
        Long academiaId = permissaoService.academiaAtualId();
        EvolucaoPeso registro = buscarRegistroAtivo(registroId, academiaId, aluno.getId());
        registro.setAtivo(false);
        registro.setAtualizadoEm(LocalDateTime.now(clock));
        registro.setAtualizadoPor(usuario);
        evolucaoPesoRepository.save(registro);
    }

    private ResumoEvolucaoPesoResponse montarResumo(Long alunoId) {
        Long academiaId = permissaoService.academiaAtualId();
        long total = evolucaoPesoRepository.countByAcademiaIdAndAlunoIdAndAtivoTrue(academiaId, alunoId);
        if (total == 0) {
            return new ResumoEvolucaoPesoResponse(null, null, null, null, null, null, null, 0L);
        }
        EvolucaoPeso inicial = evolucaoPesoRepository
                .findFirstByAcademiaIdAndAlunoIdAndAtivoTrueOrderByDataRegistroAscCriadoEmAsc(academiaId, alunoId)
                .orElseThrow();
        EvolucaoPeso atual = evolucaoPesoRepository
                .findFirstByAcademiaIdAndAlunoIdAndAtivoTrueOrderByDataRegistroDescCriadoEmDesc(academiaId, alunoId)
                .orElseThrow();
        BigDecimal diferenca = atual.getPeso().subtract(inicial.getPeso());
        return new ResumoEvolucaoPesoResponse(inicial.getPeso(), inicial.getDataRegistro(), atual.getPeso(),
                atual.getDataRegistro(), diferenca, evolucaoPesoRepository.menorPeso(academiaId, alunoId),
                evolucaoPesoRepository.maiorPeso(academiaId, alunoId), total);
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
            throw new RuntimeException("Aluno inativo não pode receber registro de peso");
        }
        return aluno;
    }

    private EvolucaoPeso buscarRegistroAtivo(Long registroId, Long academiaId, Long alunoId) {
        return evolucaoPesoRepository.findByIdAndAcademiaIdAndAlunoIdAndAtivoTrue(registroId, academiaId, alunoId)
                .orElseThrow(() -> new AccessDeniedException("Registro de peso não encontrado para este aluno"));
    }

    private void validarRegistro(BigDecimal peso, LocalDate dataRegistro) {
        if (peso == null) {
            throw new RuntimeException("Peso é obrigatório");
        }
        if (peso.compareTo(PESO_MINIMO) < 0 || peso.compareTo(PESO_MAXIMO) > 0) {
            throw new RuntimeException("Peso deve ser maior que zero e menor ou igual a 500");
        }
        if (dataRegistro == null) {
            throw new RuntimeException("Data do registro é obrigatória");
        }
        if (dataRegistro.isAfter(LocalDate.now(clock))) {
            throw new RuntimeException("Data do registro não pode ser futura");
        }
    }

    private void validarPeriodo(LocalDate inicio, LocalDate fim) {
        if ((inicio == null && fim != null) || (inicio != null && fim == null)) {
            throw new RuntimeException("Informe início e fim para filtrar o período");
        }
        if (inicio != null && fim.isBefore(inicio)) {
            throw new RuntimeException("Data final não pode ser anterior à inicial");
        }
    }

    private BigDecimal normalizarPeso(BigDecimal peso) {
        return peso.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private EvolucaoPesoResponse toResponse(EvolucaoPeso registro) {
        return new EvolucaoPesoResponse(registro.getId(), registro.getAluno().getId(), registro.getAluno().getNome(),
                registro.getPeso(), registro.getDataRegistro(), registro.getObservacao(),
                registro.getCriadoEm(), registro.getAtualizadoEm());
    }
}
