package Tic.Implementacao.service;

import Tic.Implementacao.dto.evolucaofisica.AtualizarPesoRequest;
import Tic.Implementacao.dto.evolucaofisica.EvolucaoPesoResponse;
import Tic.Implementacao.dto.evolucaofisica.RegistrarPesoRequest;
import Tic.Implementacao.dto.evolucaofisica.ResumoEvolucaoPesoResponse;
import Tic.Implementacao.exception.ConflitoException;
import Tic.Implementacao.model.*;
import Tic.Implementacao.repository.AlunoRepository;
import Tic.Implementacao.repository.EvolucaoPesoRepository;
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

class EvolucaoPesoServiceTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 7, 15);
    private static final Clock CLOCK = Clock.fixed(
            HOJE.atTime(10, 0).atZone(ZoneId.systemDefault()).toInstant(),
            ZoneId.systemDefault()
    );

    @Test
    void alunoRegistraProprioPesoComAuditoria() {
        Fixture fx = fixture(PerfilUsuario.ALUNO);
        when(fx.evolucaoPesoRepository.save(any(EvolucaoPeso.class))).thenAnswer(invocation -> withId(invocation.getArgument(0), 1L));

        EvolucaoPesoResponse response = fx.service.registrarPesoDoAlunoAtual(registrar("70.55", HOJE, "Depois do treino"));

        assertEquals(1L, response.getId());
        assertEquals(20L, response.getAlunoId());
        assertEquals(new BigDecimal("70.55"), response.getPeso());
        verify(fx.evolucaoPesoRepository).save(argThat(registro ->
                registro.getCriadoPor() == fx.usuario && Boolean.TRUE.equals(registro.getAtivo())));
        verify(fx.metaPesoService).concluirMetaAtivaSeAtingida(20L, fx.usuario);
    }

    @Test
    void professorRegistraPesoDoAlunoDaAcademia() {
        Fixture fx = fixture(PerfilUsuario.PROFESSOR);
        when(fx.evolucaoPesoRepository.save(any(EvolucaoPeso.class))).thenAnswer(invocation -> withId(invocation.getArgument(0), 2L));

        EvolucaoPesoResponse response = fx.service.registrarPeso(20L, registrar("71", HOJE.minusDays(1), null));

        assertEquals(2L, response.getId());
        verify(fx.permissaoService).exigirAdminOuProfessor();
    }

    @Test
    void alunoNaoUsaEndpointGerencialParaOutroAluno() {
        Fixture fx = fixture(PerfilUsuario.ALUNO);
        doThrow(new AccessDeniedException("Acesso permitido apenas para ADMIN ou PROFESSOR"))
                .when(fx.permissaoService).exigirAdminOuProfessor();

        assertThrows(AccessDeniedException.class, () -> fx.service.registrarPeso(99L, registrar("70", HOJE, null)));
        assertThrows(AccessDeniedException.class, () -> fx.service.listarHistorico(99L, null, null));
        assertThrows(AccessDeniedException.class, () -> fx.service.buscarResumo(99L));
    }

    @Test
    void bloqueiaAlunoInativoOuDeOutraAcademia() {
        Fixture fx = fixture(PerfilUsuario.PROFESSOR);
        fx.aluno.setAtivo(false);
        assertThrows(RuntimeException.class, () -> fx.service.registrarPeso(20L, registrar("70", HOJE, null)));

        Fixture outraAcademia = fixture(PerfilUsuario.PROFESSOR);
        when(outraAcademia.alunoRepository.findByIdAndAcademiaId(20L, 1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> outraAcademia.service.registrarPeso(20L, registrar("70", HOJE, null)));
    }

    @Test
    void validaPesoObrigatorioLimitesEDataFutura() {
        Fixture fx = fixture(PerfilUsuario.ALUNO);

        assertThrows(RuntimeException.class, () -> fx.service.registrarPesoDoAlunoAtual(registrar(null, HOJE, null)));
        assertThrows(RuntimeException.class, () -> fx.service.registrarPesoDoAlunoAtual(registrar("0", HOJE, null)));
        assertThrows(RuntimeException.class, () -> fx.service.registrarPesoDoAlunoAtual(registrar("-1", HOJE, null)));
        assertThrows(RuntimeException.class, () -> fx.service.registrarPesoDoAlunoAtual(registrar("500.01", HOJE, null)));
        assertThrows(RuntimeException.class, () -> fx.service.registrarPesoDoAlunoAtual(registrar("70", HOJE.plusDays(1), null)));
    }

    @Test
    void impedeRegistroDuplicadoNoMesmoDia() {
        Fixture fx = fixture(PerfilUsuario.ALUNO);
        when(fx.evolucaoPesoRepository.existsByAcademiaIdAndAlunoIdAndDataRegistroAndAtivoTrue(1L, 20L, HOJE))
                .thenReturn(true);

        assertThrows(ConflitoException.class, () -> fx.service.registrarPesoDoAlunoAtual(registrar("70", HOJE, null)));
        verify(fx.evolucaoPesoRepository, never()).save(any());
    }

    @Test
    void listaHistoricoAtivoOrdenadoComFiltroDePeriodo() {
        Fixture fx = fixture(PerfilUsuario.PROFESSOR);
        EvolucaoPeso atual = registro(fx, 1L, "70", HOJE);
        EvolucaoPeso anterior = registro(fx, 2L, "72", HOJE.minusDays(10));
        when(fx.evolucaoPesoRepository.findByAcademiaIdAndAlunoIdAndAtivoTrueAndDataRegistroBetweenOrderByDataRegistroDescCriadoEmDesc(
                1L, 20L, HOJE.minusMonths(1), HOJE)).thenReturn(List.of(atual, anterior));

        List<EvolucaoPesoResponse> historico = fx.service.listarHistorico(20L, HOJE.minusMonths(1), HOJE);

        assertEquals(2, historico.size());
        assertEquals(HOJE, historico.get(0).getDataRegistro());
        verify(fx.evolucaoPesoRepository, never()).save(any());
    }

    @Test
    void bloqueiaPeriodoIncompletoOuInvertido() {
        Fixture fx = fixture(PerfilUsuario.PROFESSOR);

        assertThrows(RuntimeException.class, () -> fx.service.listarHistorico(20L, HOJE.minusDays(1), null));
        assertThrows(RuntimeException.class, () -> fx.service.listarHistorico(20L, HOJE, HOJE.minusDays(1)));
    }

    @Test
    void atualizaRegistroComAuditoriaEImpedeDuplicidadeEmOutraData() {
        Fixture fx = fixture(PerfilUsuario.PROFESSOR);
        EvolucaoPeso registro = registro(fx, 7L, "72", HOJE.minusDays(2));
        when(fx.evolucaoPesoRepository.findByIdAndAcademiaIdAndAlunoIdAndAtivoTrue(7L, 1L, 20L))
                .thenReturn(Optional.of(registro));
        when(fx.evolucaoPesoRepository.save(any(EvolucaoPeso.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EvolucaoPesoResponse response = fx.service.atualizarPeso(20L, 7L, atualizar("71.10", HOJE.minusDays(1), "corrigido"));

        assertEquals(new BigDecimal("71.10"), response.getPeso());
        assertEquals(fx.usuario, registro.getAtualizadoPor());
        assertNotNull(registro.getAtualizadoEm());
        verify(fx.metaPesoService).concluirMetaAtivaSeAtingida(20L, fx.usuario);

        when(fx.evolucaoPesoRepository.existsByAcademiaIdAndAlunoIdAndDataRegistroAndAtivoTrueAndIdNot(
                1L, 20L, HOJE, 7L)).thenReturn(true);
        assertThrows(ConflitoException.class, () -> fx.service.atualizarPeso(20L, 7L, atualizar("71", HOJE, null)));
    }

    @Test
    void alunoNaoAtualizaNemExcluiRegistroDeOutroAluno() {
        Fixture fx = fixture(PerfilUsuario.ALUNO);
        when(fx.evolucaoPesoRepository.findByIdAndAcademiaIdAndAlunoIdAndAtivoTrue(99L, 1L, 20L))
                .thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class,
                () -> fx.service.atualizarPesoDoAlunoAtual(99L, atualizar("70", HOJE, null)));
        assertThrows(AccessDeniedException.class,
                () -> fx.service.excluirLogicamenteDoAlunoAtual(99L));
    }

    @Test
    void excluiLogicamenteSemApagarRegistro() {
        Fixture fx = fixture(PerfilUsuario.ALUNO);
        EvolucaoPeso registro = registro(fx, 8L, "70", HOJE);
        when(fx.evolucaoPesoRepository.findByIdAndAcademiaIdAndAlunoIdAndAtivoTrue(8L, 1L, 20L))
                .thenReturn(Optional.of(registro));

        fx.service.excluirLogicamenteDoAlunoAtual(8L);

        assertFalse(registro.getAtivo());
        assertEquals(fx.usuario, registro.getAtualizadoPor());
        verify(fx.evolucaoPesoRepository).save(registro);
        verify(fx.evolucaoPesoRepository, never()).delete(any());
        verify(fx.metaPesoService, never()).concluirMetaAtivaSeAtingida(anyLong(), any());
    }

    @Test
    void resumoCalculaInicialAtualDiferencaMenorMaiorETotal() {
        Fixture fx = fixture(PerfilUsuario.ALUNO);
        EvolucaoPeso inicial = registro(fx, 1L, "75.00", HOJE.minusMonths(2));
        EvolucaoPeso atual = registro(fx, 2L, "70.50", HOJE);
        when(fx.evolucaoPesoRepository.countByAcademiaIdAndAlunoIdAndAtivoTrue(1L, 20L)).thenReturn(2L);
        when(fx.evolucaoPesoRepository.findFirstByAcademiaIdAndAlunoIdAndAtivoTrueOrderByDataRegistroAscCriadoEmAsc(1L, 20L))
                .thenReturn(Optional.of(inicial));
        when(fx.evolucaoPesoRepository.findFirstByAcademiaIdAndAlunoIdAndAtivoTrueOrderByDataRegistroDescCriadoEmDesc(1L, 20L))
                .thenReturn(Optional.of(atual));
        when(fx.evolucaoPesoRepository.menorPeso(1L, 20L)).thenReturn(new BigDecimal("70.50"));
        when(fx.evolucaoPesoRepository.maiorPeso(1L, 20L)).thenReturn(new BigDecimal("75.00"));

        ResumoEvolucaoPesoResponse resumo = fx.service.buscarResumoDoAlunoAtual();

        assertEquals(new BigDecimal("-4.50"), resumo.getDiferencaPeso());
        assertEquals(new BigDecimal("70.50"), resumo.getPesoAtual());
        assertEquals(2L, resumo.getTotalRegistros());
    }

    @Test
    void resumoSemRegistrosRetornaVazioEDashboardNull() {
        Fixture fx = fixture(PerfilUsuario.ALUNO);
        when(fx.evolucaoPesoRepository.countByAcademiaIdAndAlunoIdAndAtivoTrue(1L, 20L)).thenReturn(0L);

        ResumoEvolucaoPesoResponse resumo = fx.service.buscarResumoDoAlunoAtual();

        assertNull(resumo.getPesoAtual());
        assertEquals(0L, resumo.getTotalRegistros());
        assertNull(fx.service.buscarResumoDashboard(20L));
    }

    private Fixture fixture(PerfilUsuario perfil) {
        EvolucaoPesoRepository evolucaoPesoRepository = mock(EvolucaoPesoRepository.class);
        AlunoRepository alunoRepository = mock(AlunoRepository.class);
        PermissaoService permissaoService = mock(PermissaoService.class);
        MetaPesoService metaPesoService = mock(MetaPesoService.class);
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

        EvolucaoPesoService service = new EvolucaoPesoService(evolucaoPesoRepository, alunoRepository,
                permissaoService, metaPesoService, CLOCK);
        return new Fixture(service, evolucaoPesoRepository, alunoRepository, permissaoService, metaPesoService,
                academia, aluno, usuario);
    }

    private RegistrarPesoRequest registrar(String peso, LocalDate data, String observacao) {
        RegistrarPesoRequest request = new RegistrarPesoRequest();
        request.setPeso(peso == null ? null : new BigDecimal(peso));
        request.setDataRegistro(data);
        request.setObservacao(observacao);
        return request;
    }

    private AtualizarPesoRequest atualizar(String peso, LocalDate data, String observacao) {
        AtualizarPesoRequest request = new AtualizarPesoRequest();
        request.setPeso(new BigDecimal(peso));
        request.setDataRegistro(data);
        request.setObservacao(observacao);
        return request;
    }

    private EvolucaoPeso registro(Fixture fx, Long id, String peso, LocalDate data) {
        EvolucaoPeso registro = new EvolucaoPeso();
        registro.setId(id);
        registro.setAcademia(fx.academia);
        registro.setAluno(fx.aluno);
        registro.setPeso(new BigDecimal(peso));
        registro.setDataRegistro(data);
        registro.setCriadoEm(data.atTime(8, 0));
        registro.setCriadoPor(fx.usuario);
        registro.setAtivo(true);
        return registro;
    }

    private EvolucaoPeso withId(EvolucaoPeso registro, Long id) {
        registro.setId(id);
        return registro;
    }

    private record Fixture(EvolucaoPesoService service,
                           EvolucaoPesoRepository evolucaoPesoRepository,
                           AlunoRepository alunoRepository,
                           PermissaoService permissaoService,
                           MetaPesoService metaPesoService,
                           Academia academia,
                           Aluno aluno,
                           Usuario usuario) {
    }
}
