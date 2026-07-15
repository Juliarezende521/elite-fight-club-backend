package Tic.Implementacao.service;

import Tic.Implementacao.dto.dashboard.aluno.ProgressoMetaPesoDashboardResponse;
import Tic.Implementacao.dto.evolucaofisica.meta.*;
import Tic.Implementacao.exception.ConflitoException;
import Tic.Implementacao.model.*;
import Tic.Implementacao.repository.AlunoRepository;
import Tic.Implementacao.repository.EvolucaoPesoRepository;
import Tic.Implementacao.repository.MetaPesoRepository;
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

class MetaPesoServiceTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 7, 15);
    private static final Clock CLOCK = Clock.fixed(
            HOJE.atTime(10, 0).atZone(ZoneId.systemDefault()).toInstant(),
            ZoneId.systemDefault()
    );

    @Test
    void criaMetaComPesoRecenteDoHistorico() {
        Fixture fx = fixture(PerfilUsuario.ALUNO);
        when(fx.evolucaoPesoRepository.findFirstByAcademiaIdAndAlunoIdAndAtivoTrueOrderByDataRegistroDescCriadoEmDesc(1L, 20L))
                .thenReturn(Optional.of(peso(fx, "72.30", HOJE.minusDays(1))));
        when(fx.metaPesoRepository.save(any(MetaPeso.class))).thenAnswer(invocation -> withId(invocation.getArgument(0), 1L));

        MetaPesoResponse response = fx.service.criarMetaDoAlunoAtual(criar("68", null, HOJE.plusMonths(2), "Meta"));

        assertEquals(new BigDecimal("72.30"), response.getPesoInicial());
        assertFalse(response.getPesoInicialManual());
        assertEquals("REDUCAO", response.getDirecao());
        verify(fx.metaPesoRepository).save(argThat(meta -> meta.getCriadaPor() == fx.usuario && meta.getStatus() == StatusMetaPeso.ATIVA));
    }

    @Test
    void criaMetaSemHistoricoComPesoInicialManual() {
        Fixture fx = fixture(PerfilUsuario.PROFESSOR);
        when(fx.evolucaoPesoRepository.findFirstByAcademiaIdAndAlunoIdAndAtivoTrueOrderByDataRegistroDescCriadoEmDesc(1L, 20L))
                .thenReturn(Optional.empty());
        when(fx.metaPesoRepository.save(any(MetaPeso.class))).thenAnswer(invocation -> withId(invocation.getArgument(0), 2L));

        CriarMetaPesoRequest request = criar("80", HOJE, HOJE.plusMonths(1), null);
        request.setPesoInicialManual(new BigDecimal("75"));
        MetaPesoResponse response = fx.service.criarMeta(20L, request);

        assertEquals(new BigDecimal("75.00"), response.getPesoInicial());
        assertTrue(response.getPesoInicialManual());
        assertEquals("AUMENTO", response.getDirecao());
        verify(fx.permissaoService).exigirAdminOuProfessor();
    }

    @Test
    void bloqueiaCriacaoSemPesoInicialDisponivel() {
        Fixture fx = fixture(PerfilUsuario.ALUNO);
        when(fx.evolucaoPesoRepository.findFirstByAcademiaIdAndAlunoIdAndAtivoTrueOrderByDataRegistroDescCriadoEmDesc(1L, 20L))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> fx.service.criarMetaDoAlunoAtual(criar("70", HOJE, null, null)));
    }

    @Test
    void validaPesosDatasAlvoIgualEAlunoInativo() {
        Fixture fx = fixture(PerfilUsuario.ALUNO);
        when(fx.evolucaoPesoRepository.findFirstByAcademiaIdAndAlunoIdAndAtivoTrueOrderByDataRegistroDescCriadoEmDesc(1L, 20L))
                .thenReturn(Optional.of(peso(fx, "70", HOJE)));

        assertThrows(RuntimeException.class, () -> fx.service.criarMetaDoAlunoAtual(criar(null, HOJE, null, null)));
        assertThrows(RuntimeException.class, () -> fx.service.criarMetaDoAlunoAtual(criar("0", HOJE, null, null)));
        assertThrows(RuntimeException.class, () -> fx.service.criarMetaDoAlunoAtual(criar("500.01", HOJE, null, null)));
        assertThrows(RuntimeException.class, () -> fx.service.criarMetaDoAlunoAtual(criar("70", HOJE, null, null)));
        assertThrows(RuntimeException.class, () -> fx.service.criarMetaDoAlunoAtual(criar("68", HOJE.plusDays(1), null, null)));
        assertThrows(RuntimeException.class, () -> fx.service.criarMetaDoAlunoAtual(criar("68", HOJE, HOJE, null)));

        fx.aluno.setAtivo(false);
        assertThrows(RuntimeException.class, () -> fx.service.criarMetaDoAlunoAtual(criar("68", HOJE, null, null)));
    }

    @Test
    void segundaMetaAtivaBloqueada() {
        Fixture fx = fixture(PerfilUsuario.ALUNO);
        when(fx.metaPesoRepository.existsByAcademiaIdAndAlunoIdAndStatus(1L, 20L, StatusMetaPeso.ATIVA)).thenReturn(true);

        assertThrows(ConflitoException.class, () -> fx.service.criarMetaDoAlunoAtual(criar("68", HOJE, null, null)));
        verify(fx.metaPesoRepository, never()).save(any());
    }

    @Test
    void calculaProgressoReducaoAumentoEAfastamentoComPercentualLimitado() {
        Fixture fx = fixture(PerfilUsuario.ALUNO);
        MetaPeso reducao = meta(fx, 1L, "80", "70", StatusMetaPeso.ATIVA);
        when(fx.metaPesoRepository.findFirstByAcademiaIdAndAlunoIdAndStatusOrderByCriadaEmDesc(1L, 20L, StatusMetaPeso.ATIVA))
                .thenReturn(Optional.of(reducao));
        when(fx.evolucaoPesoRepository.findFirstByAcademiaIdAndAlunoIdAndAtivoTrueOrderByDataRegistroDescCriadoEmDesc(1L, 20L))
                .thenReturn(Optional.of(peso(fx, "75", HOJE)));

        ProgressoMetaPesoResponse progresso = fx.service.buscarMetaAtivaDoAlunoAtual();

        assertEquals(new BigDecimal("5.00"), progresso.getVariacaoDesdeInicio());
        assertEquals(new BigDecimal("5.00"), progresso.getQuantidadeRestante());
        assertEquals(new BigDecimal("50.00"), progresso.getPercentualProgresso());
        assertFalse(progresso.getMetaAtingida());

        when(fx.evolucaoPesoRepository.findFirstByAcademiaIdAndAlunoIdAndAtivoTrueOrderByDataRegistroDescCriadoEmDesc(1L, 20L))
                .thenReturn(Optional.of(peso(fx, "85", HOJE)));
        assertEquals(new BigDecimal("0.00"), fx.service.buscarMetaAtivaDoAlunoAtual().getPercentualProgresso());

        MetaPeso aumento = meta(fx, 2L, "70", "80", StatusMetaPeso.ATIVA);
        when(fx.metaPesoRepository.findFirstByAcademiaIdAndAlunoIdAndStatusOrderByCriadaEmDesc(1L, 20L, StatusMetaPeso.ATIVA))
                .thenReturn(Optional.of(aumento));
        when(fx.evolucaoPesoRepository.findFirstByAcademiaIdAndAlunoIdAndAtivoTrueOrderByDataRegistroDescCriadoEmDesc(1L, 20L))
                .thenReturn(Optional.of(peso(fx, "82", HOJE)));

        ProgressoMetaPesoResponse atingida = fx.service.buscarMetaAtivaDoAlunoAtual();
        assertEquals(new BigDecimal("100.00"), atingida.getPercentualProgresso());
        assertTrue(atingida.getMetaAtingida());
    }

    @Test
    void metaSemPesoAtualRetornaEstadoClaro() {
        Fixture fx = fixture(PerfilUsuario.ALUNO);
        when(fx.metaPesoRepository.findFirstByAcademiaIdAndAlunoIdAndStatusOrderByCriadaEmDesc(1L, 20L, StatusMetaPeso.ATIVA))
                .thenReturn(Optional.of(meta(fx, 1L, "80", "70", StatusMetaPeso.ATIVA)));
        when(fx.evolucaoPesoRepository.findFirstByAcademiaIdAndAlunoIdAndAtivoTrueOrderByDataRegistroDescCriadoEmDesc(1L, 20L))
                .thenReturn(Optional.empty());

        ProgressoMetaPesoResponse progresso = fx.service.buscarMetaAtivaDoAlunoAtual();

        assertNull(progresso.getPesoAtual());
        assertFalse(progresso.getMetaAtingida());
        assertEquals(new BigDecimal("0.00"), progresso.getPercentualProgresso());
    }

    @Test
    void concluiMetaAtivaAposNovoPesoSemReabrirDepois() {
        Fixture fx = fixture(PerfilUsuario.ALUNO);
        MetaPeso meta = meta(fx, 1L, "80", "70", StatusMetaPeso.ATIVA);
        when(fx.metaPesoRepository.findAtivasForUpdate(1L, 20L, StatusMetaPeso.ATIVA)).thenReturn(List.of(meta));
        when(fx.evolucaoPesoRepository.findFirstByAcademiaIdAndAlunoIdAndAtivoTrueOrderByDataRegistroDescCriadoEmDesc(1L, 20L))
                .thenReturn(Optional.of(peso(fx, "69.8", HOJE)));

        fx.service.concluirMetaAtivaSeAtingida(20L, fx.usuario);

        assertEquals(StatusMetaPeso.CONCLUIDA, meta.getStatus());
        assertNotNull(meta.getEncerradaEm());
        verify(fx.metaPesoRepository).save(meta);

        when(fx.metaPesoRepository.findAtivasForUpdate(1L, 20L, StatusMetaPeso.ATIVA)).thenReturn(List.of());
        fx.service.concluirMetaAtivaSeAtingida(20L, fx.usuario);
        assertEquals(StatusMetaPeso.CONCLUIDA, meta.getStatus());
    }

    @Test
    void cancelaSubstituiEPreservaHistorico() {
        Fixture fx = fixture(PerfilUsuario.ALUNO);
        MetaPeso atual = meta(fx, 1L, "80", "70", StatusMetaPeso.ATIVA);
        when(fx.metaPesoRepository.findByIdAndAcademiaIdAndAlunoId(1L, 1L, 20L)).thenReturn(Optional.of(atual));
        when(fx.metaPesoRepository.save(any(MetaPeso.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MetaPesoResponse cancelada = fx.service.cancelarMetaDoAlunoAtual(1L, encerrar("Pausada", null));

        assertEquals(StatusMetaPeso.CANCELADA, cancelada.getStatus());
        assertNotNull(atual.getEncerradaEm());

        MetaPeso substituida = meta(fx, 2L, "80", "70", StatusMetaPeso.ATIVA);
        when(fx.metaPesoRepository.findByIdAndAcademiaIdAndAlunoId(2L, 1L, 20L)).thenReturn(Optional.of(substituida));
        when(fx.evolucaoPesoRepository.findFirstByAcademiaIdAndAlunoIdAndAtivoTrueOrderByDataRegistroDescCriadoEmDesc(1L, 20L))
                .thenReturn(Optional.of(peso(fx, "78", HOJE)));
        EncerrarMetaPesoRequest troca = encerrar("Nova estratégia", criar("74", HOJE, null, "Nova"));
        when(fx.metaPesoRepository.save(argThat(meta -> meta.getStatus() == StatusMetaPeso.ATIVA)))
                .thenAnswer(invocation -> withId(invocation.getArgument(0), 3L));

        MetaPesoResponse nova = fx.service.substituirMetaDoAlunoAtual(2L, troca);

        assertEquals(StatusMetaPeso.SUBSTITUIDA, substituida.getStatus());
        assertEquals(3L, nova.getId());
        when(fx.metaPesoRepository.findByAcademiaIdAndAlunoIdOrderByCriadaEmDesc(1L, 20L)).thenReturn(List.of(novaMeta(fx), substituida, atual));
        assertEquals(3, fx.service.listarMetasDoAlunoAtual().size());
    }

    @Test
    void atualizaMetaAtivaEBloqueiaConcluida() {
        Fixture fx = fixture(PerfilUsuario.PROFESSOR);
        MetaPeso ativa = meta(fx, 1L, "80", "70", StatusMetaPeso.ATIVA);
        when(fx.metaPesoRepository.findByIdAndAcademiaIdAndAlunoId(1L, 1L, 20L)).thenReturn(Optional.of(ativa));
        when(fx.metaPesoRepository.save(any(MetaPeso.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MetaPesoResponse response = fx.service.atualizarMeta(20L, 1L, atualizar("72", HOJE, HOJE.plusMonths(1)));

        assertEquals(new BigDecimal("72.00"), response.getPesoAlvo());
        assertNotNull(ativa.getAtualizadaEm());

        ativa.setStatus(StatusMetaPeso.CONCLUIDA);
        assertThrows(RuntimeException.class, () -> fx.service.atualizarMeta(20L, 1L, atualizar("73", HOJE, null)));
    }

    @Test
    void alunoNaoAcessaMetaDeOutroProfessorOutraAcademiaEAdminPermitido() {
        Fixture aluno = fixture(PerfilUsuario.ALUNO);
        when(aluno.metaPesoRepository.findByIdAndAcademiaIdAndAlunoId(99L, 1L, 20L)).thenReturn(Optional.empty());
        assertThrows(AccessDeniedException.class, () -> aluno.service.buscarMetaDoAlunoAtual(99L));

        Fixture professorOutra = fixture(PerfilUsuario.PROFESSOR);
        when(professorOutra.alunoRepository.findByIdAndAcademiaId(20L, 1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> professorOutra.service.listarMetas(20L));

        Fixture admin = fixture(PerfilUsuario.ADMIN);
        when(admin.metaPesoRepository.findByAcademiaIdAndAlunoIdOrderByCriadaEmDesc(1L, 20L)).thenReturn(List.of());
        assertTrue(admin.service.listarMetas(20L).isEmpty());
        verify(admin.permissaoService).exigirAdminOuProfessor();
    }

    @Test
    void dashboardComMetaESemMeta() {
        Fixture fx = fixture(PerfilUsuario.ALUNO);
        when(fx.metaPesoRepository.findFirstByAcademiaIdAndAlunoIdAndStatusOrderByCriadaEmDesc(1L, 20L, StatusMetaPeso.ATIVA))
                .thenReturn(Optional.of(meta(fx, 1L, "80", "70", StatusMetaPeso.ATIVA)));
        when(fx.evolucaoPesoRepository.findFirstByAcademiaIdAndAlunoIdAndAtivoTrueOrderByDataRegistroDescCriadoEmDesc(1L, 20L))
                .thenReturn(Optional.of(peso(fx, "75", HOJE)));

        ProgressoMetaPesoDashboardResponse dashboard = fx.service.buscarResumoDashboard(20L);

        assertEquals(1L, dashboard.getMetaId());
        assertEquals(new BigDecimal("50.00"), dashboard.getPercentualProgresso());

        when(fx.metaPesoRepository.findFirstByAcademiaIdAndAlunoIdAndStatusOrderByCriadaEmDesc(1L, 20L, StatusMetaPeso.ATIVA))
                .thenReturn(Optional.empty());
        assertNull(fx.service.buscarResumoDashboard(20L));
    }

    private Fixture fixture(PerfilUsuario perfil) {
        MetaPesoRepository metaPesoRepository = mock(MetaPesoRepository.class);
        EvolucaoPesoRepository evolucaoPesoRepository = mock(EvolucaoPesoRepository.class);
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

        MetaPesoService service = new MetaPesoService(metaPesoRepository, evolucaoPesoRepository,
                alunoRepository, permissaoService, CLOCK);
        return new Fixture(service, metaPesoRepository, evolucaoPesoRepository, alunoRepository,
                permissaoService, academia, aluno, usuario);
    }

    private CriarMetaPesoRequest criar(String alvo, LocalDate inicio, LocalDate limite, String obs) {
        CriarMetaPesoRequest request = new CriarMetaPesoRequest();
        request.setPesoAlvo(alvo == null ? null : new BigDecimal(alvo));
        request.setDataInicio(inicio);
        request.setDataLimite(limite);
        request.setObservacao(obs);
        return request;
    }

    private AtualizarMetaPesoRequest atualizar(String alvo, LocalDate inicio, LocalDate limite) {
        AtualizarMetaPesoRequest request = new AtualizarMetaPesoRequest();
        request.setPesoAlvo(new BigDecimal(alvo));
        request.setDataInicio(inicio);
        request.setDataLimite(limite);
        return request;
    }

    private EncerrarMetaPesoRequest encerrar(String motivo, CriarMetaPesoRequest novaMeta) {
        EncerrarMetaPesoRequest request = new EncerrarMetaPesoRequest();
        request.setMotivo(motivo);
        request.setNovaMeta(novaMeta);
        return request;
    }

    private EvolucaoPeso peso(Fixture fx, String valor, LocalDate data) {
        EvolucaoPeso peso = new EvolucaoPeso();
        peso.setId(30L);
        peso.setAcademia(fx.academia);
        peso.setAluno(fx.aluno);
        peso.setPeso(new BigDecimal(valor));
        peso.setDataRegistro(data);
        peso.setCriadoEm(data.atTime(8, 0));
        peso.setCriadoPor(fx.usuario);
        peso.setAtivo(true);
        return peso;
    }

    private MetaPeso meta(Fixture fx, Long id, String inicial, String alvo, StatusMetaPeso status) {
        MetaPeso meta = new MetaPeso();
        meta.setId(id);
        meta.setAcademia(fx.academia);
        meta.setAluno(fx.aluno);
        meta.setPesoInicial(new BigDecimal(inicial));
        meta.setPesoAlvo(new BigDecimal(alvo));
        meta.setPesoInicialManual(false);
        meta.setDataInicio(HOJE.minusDays(10));
        meta.setDataLimite(HOJE.plusDays(20));
        meta.setStatus(status);
        meta.setCriadaEm(HOJE.minusDays(10).atTime(9, 0));
        meta.setCriadaPor(fx.usuario);
        return meta;
    }

    private MetaPeso novaMeta(Fixture fx) {
        return meta(fx, 3L, "78", "74", StatusMetaPeso.ATIVA);
    }

    private MetaPeso withId(MetaPeso meta, Long id) {
        meta.setId(id);
        return meta;
    }

    private record Fixture(MetaPesoService service,
                           MetaPesoRepository metaPesoRepository,
                           EvolucaoPesoRepository evolucaoPesoRepository,
                           AlunoRepository alunoRepository,
                           PermissaoService permissaoService,
                           Academia academia,
                           Aluno aluno,
                           Usuario usuario) {
    }
}
