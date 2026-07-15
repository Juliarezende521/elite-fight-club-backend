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
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AvaliacaoCorporalServiceTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 7, 15);
    private static final Clock CLOCK = Clock.fixed(
            HOJE.atTime(10, 0).atZone(ZoneId.systemDefault()).toInstant(),
            ZoneId.systemDefault()
    );

    @Test
    void cadastraAvaliacaoSomenteComDadosBasicos() {
        Fixture fx = fixture(PerfilUsuario.PROFESSOR);
        when(fx.avaliacaoRepository.save(any(AvaliacaoCorporal.class))).thenAnswer(inv -> withId(inv.getArgument(0), 1L));

        AvaliacaoCorporalResponse response = fx.service.cadastrar(20L, requestBasico());

        assertEquals(1L, response.getId());
        assertEquals(new BigDecimal("1.69"), response.getAlturaMetros());
        assertNull(response.getMedidas());
        assertNull(response.getBioimpedancia());
        verify(fx.permissaoService).exigirAdminOuProfessor();
    }

    @Test
    void cadastraComMedidasBioimpedanciaEOpcionalidade() {
        Fixture fx = fixture(PerfilUsuario.PROFESSOR);
        when(fx.avaliacaoRepository.save(any(AvaliacaoCorporal.class))).thenAnswer(inv -> withId(inv.getArgument(0), 2L));
        when(fx.medidasRepository.save(any(MedidasCorporais.class))).thenAnswer(inv -> inv.getArgument(0));
        when(fx.bioRepository.save(any(Bioimpedancia.class))).thenAnswer(inv -> inv.getArgument(0));

        CriarAvaliacaoCorporalRequest request = requestCompleto();
        AvaliacaoCorporalResponse response = fx.service.cadastrar(20L, request);

        assertEquals(new BigDecimal("82.00"), response.getMedidas().getCintura());
        assertEquals(new BigDecimal("28.50"), response.getBioimpedancia().getPercentualGordura());
    }

    @Test
    void validaPesoAlturaPercentuaisDataFuturaEDuplicidade() {
        Fixture fx = fixture(PerfilUsuario.PROFESSOR);
        CriarAvaliacaoCorporalRequest pesoInvalido = requestBasico();
        pesoInvalido.setPesoRegistrado(new BigDecimal("501"));
        assertThrows(RuntimeException.class, () -> fx.service.cadastrar(20L, pesoInvalido));

        CriarAvaliacaoCorporalRequest alturaInvalida = requestBasico();
        alturaInvalida.setAlturaMetros(new BigDecimal("0"));
        assertThrows(RuntimeException.class, () -> fx.service.cadastrar(20L, alturaInvalida));

        CriarAvaliacaoCorporalRequest futuro = requestBasico();
        futuro.setDataAvaliacao(HOJE.plusDays(1));
        assertThrows(RuntimeException.class, () -> fx.service.cadastrar(20L, futuro));

        CriarAvaliacaoCorporalRequest percentual = requestCompleto();
        percentual.getBioimpedancia().setPercentualGordura(new BigDecimal("101"));
        assertThrows(RuntimeException.class, () -> fx.service.cadastrar(20L, percentual));

        when(fx.avaliacaoRepository.existsByAcademiaIdAndAlunoIdAndDataAvaliacaoAndAtivaTrue(1L, 20L, HOJE)).thenReturn(true);
        assertThrows(ConflitoException.class, () -> fx.service.cadastrar(20L, requestBasico()));
    }

    @Test
    void editaEExcluiLogicamenteSemApagarMedidas() {
        Fixture fx = fixture(PerfilUsuario.ADMIN);
        AvaliacaoCorporal avaliacao = avaliacao(fx, 5L, HOJE.minusDays(1));
        when(fx.avaliacaoRepository.findByIdAndAcademiaIdAndAlunoIdAndAtivaTrue(5L, 1L, 20L)).thenReturn(Optional.of(avaliacao));
        when(fx.avaliacaoRepository.save(any(AvaliacaoCorporal.class))).thenAnswer(inv -> inv.getArgument(0));
        when(fx.medidasRepository.findByAvaliacaoId(5L)).thenReturn(Optional.of(medidas(avaliacao)));
        when(fx.medidasRepository.save(any(MedidasCorporais.class))).thenAnswer(inv -> inv.getArgument(0));

        AtualizarAvaliacaoCorporalRequest update = new AtualizarAvaliacaoCorporalRequest();
        update.setDataAvaliacao(HOJE);
        update.setPesoRegistrado(new BigDecimal("77.2"));
        update.setMedidas(medidasRequest());
        AvaliacaoCorporalResponse response = fx.service.atualizar(20L, 5L, update);

        assertEquals(HOJE, response.getDataAvaliacao());
        assertNotNull(avaliacao.getAtualizadaEm());

        fx.service.excluirLogicamente(20L, 5L);
        assertFalse(avaliacao.getAtiva());
        verify(fx.avaliacaoRepository, never()).delete(any());
        verify(fx.medidasRepository, never()).delete(any());
    }

    @Test
    void consultasDoAlunoSomentePropriasEInativasNaoListadas() {
        Fixture fx = fixture(PerfilUsuario.ALUNO);
        AvaliacaoCorporal avaliacao = avaliacao(fx, 6L, HOJE);
        when(fx.avaliacaoRepository.findByAcademiaIdAndAlunoIdAndAtivaTrueOrderByDataAvaliacaoDescCriadaEmDesc(1L, 20L))
                .thenReturn(List.of(avaliacao));
        when(fx.avaliacaoRepository.findByIdAndAcademiaIdAndAlunoIdAndAtivaTrue(6L, 1L, 20L)).thenReturn(Optional.of(avaliacao));

        assertEquals(1, fx.service.listarDoAlunoAtual().size());
        assertEquals(6L, fx.service.buscarDoAlunoAtual(6L).getId());

        when(fx.avaliacaoRepository.findByIdAndAcademiaIdAndAlunoIdAndAtivaTrue(99L, 1L, 20L)).thenReturn(Optional.empty());
        assertThrows(AccessDeniedException.class, () -> fx.service.buscarDoAlunoAtual(99L));
    }

    @Test
    void professorOutraAcademiaBloqueadoEAdminPermitido() {
        Fixture professor = fixture(PerfilUsuario.PROFESSOR);
        when(professor.alunoRepository.findByIdAndAcademiaId(20L, 1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> professor.service.listar(20L));

        Fixture admin = fixture(PerfilUsuario.ADMIN);
        when(admin.avaliacaoRepository.findByAcademiaIdAndAlunoIdAndAtivaTrueOrderByDataAvaliacaoDescCriadaEmDesc(1L, 20L))
                .thenReturn(List.of());
        assertTrue(admin.service.listar(20L).isEmpty());
        verify(admin.permissaoService).exigirAdminOuProfessor();
    }

    @Test
    void resumoMaisRecenteEDashboard() {
        Fixture fx = fixture(PerfilUsuario.ALUNO);
        AvaliacaoCorporal recente = avaliacao(fx, 7L, HOJE);
        when(fx.avaliacaoRepository.findFirstByAcademiaIdAndAlunoIdAndAtivaTrueOrderByDataAvaliacaoDescCriadaEmDesc(1L, 20L))
                .thenReturn(Optional.of(recente));
        when(fx.medidasRepository.findByAvaliacaoId(7L)).thenReturn(Optional.of(medidas(recente)));
        when(fx.bioRepository.findByAvaliacaoId(7L)).thenReturn(Optional.of(bio(recente)));

        ResumoAvaliacaoCorporalResponse resumo = fx.service.resumoDoAlunoAtual();
        ResumoAvaliacaoCorporalDashboardResponse dashboard = fx.service.buscarResumoDashboard(20L);

        assertEquals(7L, resumo.getAvaliacaoId());
        assertTrue(resumo.getPossuiMedidas());
        assertEquals(new BigDecimal("28.50"), dashboard.getPercentualGordura());

        when(fx.avaliacaoRepository.findFirstByAcademiaIdAndAlunoIdAndAtivaTrueOrderByDataAvaliacaoDescCriadaEmDesc(1L, 20L))
                .thenReturn(Optional.empty());
        assertNull(fx.service.buscarResumoDashboard(20L));
    }

    @Test
    void comparativoSomenteCamposPresentesNasDuasAvaliacoesSemInterpretar() {
        Fixture fx = fixture(PerfilUsuario.ALUNO);
        AvaliacaoCorporal inicial = avaliacao(fx, 1L, HOJE.minusMonths(1));
        inicial.setPesoRegistrado(new BigDecimal("80.00"));
        AvaliacaoCorporal fim = avaliacao(fx, 2L, HOJE);
        fim.setPesoRegistrado(new BigDecimal("78.00"));
        when(fx.avaliacaoRepository.findByIdAndAcademiaIdAndAlunoIdAndAtivaTrue(1L, 1L, 20L)).thenReturn(Optional.of(inicial));
        when(fx.avaliacaoRepository.findByIdAndAcademiaIdAndAlunoIdAndAtivaTrue(2L, 1L, 20L)).thenReturn(Optional.of(fim));
        MedidasCorporais medidasInicial = medidas(inicial);
        medidasInicial.setQuadril(null);
        MedidasCorporais medidasFinal = medidas(fim);
        when(fx.medidasRepository.findByAvaliacaoId(1L)).thenReturn(Optional.of(medidasInicial));
        when(fx.medidasRepository.findByAvaliacaoId(2L)).thenReturn(Optional.of(medidasFinal));

        ComparativoAvaliacoesResponse response = fx.service.compararDoAlunoAtual(1L, 2L);

        assertEquals(new BigDecimal("-2.00"), response.getDiferencas().get("pesoRegistrado"));
        assertTrue(response.getDiferencas().containsKey("cintura"));
        assertFalse(response.getDiferencas().containsKey("quadril"));
    }

    @Test
    void consultasNaoAlteramDadosENaoUsamEntidadeAntigaEvolucao() {
        Fixture fx = fixture(PerfilUsuario.ALUNO);
        when(fx.avaliacaoRepository.findByAcademiaIdAndAlunoIdAndAtivaTrueOrderByDataAvaliacaoDescCriadaEmDesc(1L, 20L))
                .thenReturn(List.of());

        fx.service.listarDoAlunoAtual();

        verify(fx.avaliacaoRepository, never()).save(any());
        verify(fx.medidasRepository, never()).save(any());
        verify(fx.bioRepository, never()).save(any());
    }

    private Fixture fixture(PerfilUsuario perfil) {
        AvaliacaoCorporalRepository avaliacaoRepository = mock(AvaliacaoCorporalRepository.class);
        MedidasCorporaisRepository medidasRepository = mock(MedidasCorporaisRepository.class);
        BioimpedanciaRepository bioRepository = mock(BioimpedanciaRepository.class);
        AlunoRepository alunoRepository = mock(AlunoRepository.class);
        PermissaoService permissaoService = mock(PermissaoService.class);
        Academia academia = new Academia();
        academia.setId(1L);
        academia.setNome("Elite");
        Aluno aluno = new Aluno();
        aluno.setId(20L);
        aluno.setNome("Julia");
        aluno.setAcademia(academia);
        aluno.setAtivo(true);
        Usuario usuario = new Usuario();
        usuario.setId(10L);
        usuario.setNome("Usuário");
        usuario.setPerfil(perfil);
        usuario.setAcademia(academia);
        if (perfil == PerfilUsuario.ALUNO) {
            usuario.setAluno(aluno);
        }
        when(permissaoService.usuarioAtual()).thenReturn(usuario);
        when(permissaoService.academiaAtualId()).thenReturn(1L);
        when(alunoRepository.findByIdAndAcademiaId(20L, 1L)).thenReturn(Optional.of(aluno));
        AvaliacaoCorporalService service = new AvaliacaoCorporalService(avaliacaoRepository, medidasRepository,
                bioRepository, alunoRepository, permissaoService, CLOCK);
        return new Fixture(service, avaliacaoRepository, medidasRepository, bioRepository, alunoRepository,
                permissaoService, academia, aluno, usuario);
    }

    private CriarAvaliacaoCorporalRequest requestBasico() {
        CriarAvaliacaoCorporalRequest request = new CriarAvaliacaoCorporalRequest();
        request.setDataAvaliacao(HOJE);
        request.setAlturaMetros(new BigDecimal("1.69"));
        request.setPesoRegistrado(new BigDecimal("78.50"));
        request.setObservacao("Avaliação trimestral");
        return request;
    }

    private CriarAvaliacaoCorporalRequest requestCompleto() {
        CriarAvaliacaoCorporalRequest request = requestBasico();
        request.setMedidas(medidasRequest());
        request.setBioimpedancia(bioRequest());
        return request;
    }

    private MedidasCorporaisRequest medidasRequest() {
        MedidasCorporaisRequest medidas = new MedidasCorporaisRequest();
        medidas.setCintura(new BigDecimal("82"));
        medidas.setQuadril(new BigDecimal("102"));
        medidas.setBracoDireito(new BigDecimal("31.5"));
        medidas.setBracoEsquerdo(new BigDecimal("31"));
        return medidas;
    }

    private BioimpedanciaRequest bioRequest() {
        BioimpedanciaRequest bio = new BioimpedanciaRequest();
        bio.setPercentualGordura(new BigDecimal("28.5"));
        bio.setMassaMagraKg(new BigDecimal("56.13"));
        bio.setMassaMuscularKg(new BigDecimal("41"));
        bio.setMetabolismoBasalKcal(new BigDecimal("1540"));
        return bio;
    }

    private AvaliacaoCorporal avaliacao(Fixture fx, Long id, LocalDate data) {
        AvaliacaoCorporal avaliacao = new AvaliacaoCorporal();
        avaliacao.setId(id);
        avaliacao.setAcademia(fx.academia);
        avaliacao.setAluno(fx.aluno);
        avaliacao.setDataAvaliacao(data);
        avaliacao.setAlturaMetros(new BigDecimal("1.69"));
        avaliacao.setPesoRegistrado(new BigDecimal("78.50"));
        avaliacao.setCriadaEm(data.atTime(9, 0));
        avaliacao.setCriadaPor(fx.usuario);
        avaliacao.setAtiva(true);
        return avaliacao;
    }

    private MedidasCorporais medidas(AvaliacaoCorporal avaliacao) {
        MedidasCorporais medidas = new MedidasCorporais();
        medidas.setId(30L);
        medidas.setAvaliacao(avaliacao);
        medidas.setCintura(new BigDecimal("82.00"));
        medidas.setQuadril(new BigDecimal("102.00"));
        return medidas;
    }

    private Bioimpedancia bio(AvaliacaoCorporal avaliacao) {
        Bioimpedancia bio = new Bioimpedancia();
        bio.setId(40L);
        bio.setAvaliacao(avaliacao);
        bio.setPercentualGordura(new BigDecimal("28.50"));
        bio.setMassaMagraKg(new BigDecimal("56.13"));
        bio.setMassaMuscularKg(new BigDecimal("41.00"));
        return bio;
    }

    private AvaliacaoCorporal withId(AvaliacaoCorporal avaliacao, Long id) {
        avaliacao.setId(id);
        return avaliacao;
    }

    private record Fixture(AvaliacaoCorporalService service,
                           AvaliacaoCorporalRepository avaliacaoRepository,
                           MedidasCorporaisRepository medidasRepository,
                           BioimpedanciaRepository bioRepository,
                           AlunoRepository alunoRepository,
                           PermissaoService permissaoService,
                           Academia academia,
                           Aluno aluno,
                           Usuario usuario) {
    }
}
