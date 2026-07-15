package Tic.Implementacao.service;

import Tic.Implementacao.dto.graduacao.*;
import Tic.Implementacao.model.*;
import Tic.Implementacao.repository.*;
import Tic.Implementacao.security.PermissaoService;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.time.*;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class GraduacaoAlunoServiceTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 7, 14);

    @Test
    void alunoAbaixoDaMetaNaoElegivel() {
        Fixture fx = fixture();
        when(fx.checkInRepository.countByAcademiaIdAndAlunoIdAndAtivoTrueAndDataHoraBetween(eq(1L), eq(20L), any(), any()))
                .thenReturn(4L);

        ElegibilidadeGraduacaoResponse response = fx.service.calcularElegibilidade(20L);

        assertFalse(response.getElegivelParaAvaliacao());
        assertFalse(response.getRequisitoCheckInsAtingido());
        assertEquals(6, response.getCheckInsFaltantes());
    }

    @Test
    void metaAtingidaMasTempoMinimoNaoAtingido() {
        Fixture fx = fixture();
        fx.proxima.setTempoMinimoMeses(3);
        when(fx.historicoRepository.findFirstByAcademiaIdAndAlunoIdAndNovaGraduacaoIdOrderByAlteradoEmDesc(1L, 20L, 1L))
                .thenReturn(Optional.of(historico(fx, null, fx.atual, HOJE.minusMonths(1).atStartOfDay(), false)));
        when(fx.checkInRepository.countByAcademiaIdAndAlunoIdAndAtivoTrueAndDataHoraBetween(eq(1L), eq(20L), any(), any()))
                .thenReturn(10L);

        ElegibilidadeGraduacaoResponse response = fx.service.calcularElegibilidade(20L);

        assertTrue(response.getRequisitoCheckInsAtingido());
        assertFalse(response.getRequisitoTempoAtingido());
        assertFalse(response.getElegivelParaAvaliacao());
    }

    @Test
    void todosOsRequisitosAtingidos() {
        Fixture fx = fixture();
        fx.proxima.setTempoMinimoMeses(3);
        when(fx.historicoRepository.findFirstByAcademiaIdAndAlunoIdAndNovaGraduacaoIdOrderByAlteradoEmDesc(1L, 20L, 1L))
                .thenReturn(Optional.of(historico(fx, null, fx.atual, HOJE.minusMonths(4).atStartOfDay(), false)));
        when(fx.checkInRepository.countByAcademiaIdAndAlunoIdAndAtivoTrueAndDataHoraBetween(eq(1L), eq(20L), any(), any()))
                .thenReturn(10L);

        ElegibilidadeGraduacaoResponse response = fx.service.calcularElegibilidade(20L);

        assertTrue(response.getRequisitoCheckInsAtingido());
        assertTrue(response.getRequisitoTempoAtingido());
        assertTrue(response.getElegivelParaAvaliacao());
    }

    @Test
    void alunoNaGraduacaoMaisAltaNaoElegivel() {
        Fixture fx = fixture();
        when(fx.graduacaoService.buscarProximaGraduacao(1L, fx.atual)).thenReturn(Optional.empty());

        ElegibilidadeGraduacaoResponse response = fx.service.calcularElegibilidade(20L);

        assertFalse(response.getElegivelParaAvaliacao());
        assertNull(response.getProximaGraduacaoId());
        assertEquals("Graduação mais alta", response.getMotivo());
    }

    @Test
    void checkInInativoNaoContadoPorqueUsaContadorAtivo() {
        Fixture fx = fixture();
        fx.service.calcularElegibilidade(20L);

        verify(fx.checkInRepository).countByAcademiaIdAndAlunoIdAndAtivoTrueAndDataHoraBetween(
                eq(1L), eq(20L), any(), any());
    }

    @Test
    void promocaoValidaCriaHistoricoEAlteraAluno() {
        Fixture fx = fixture();
        when(fx.checkInRepository.countByAcademiaIdAndAlunoIdAndAtivoTrueAndDataHoraBetween(eq(1L), eq(20L), any(), any()))
                .thenReturn(10L);
        when(fx.historicoRepository.save(any(HistoricoGraduacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        HistoricoGraduacaoResponse response = fx.service.promoverParaProximaGraduacao(20L, promover("Aprovado"));

        assertEquals("Azul", fx.aluno.getGraduacaoEntidade().getNome());
        assertTrue(response.getPromocaoPorAvaliacao());
        verify(fx.historicoRepository).save(argThat(h -> h.getGraduacaoAnterior().equals(fx.atual)
                && h.getNovaGraduacao().equals(fx.proxima)));
        verify(fx.alunoRepository).save(fx.aluno);
    }

    @Test
    void promocaoSemElegibilidadeBloqueada() {
        Fixture fx = fixture();
        when(fx.checkInRepository.countByAcademiaIdAndAlunoIdAndAtivoTrueAndDataHoraBetween(eq(1L), eq(20L), any(), any()))
                .thenReturn(1L);

        assertThrows(RuntimeException.class, () -> fx.service.promoverParaProximaGraduacao(20L, promover("Aprovado")));
        verify(fx.historicoRepository, never()).save(any());
    }

    @Test
    void alteracaoAdministrativaPermiteAvancoManualComHistorico() {
        Fixture fx = fixture();
        when(fx.historicoRepository.save(any(HistoricoGraduacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        HistoricoGraduacaoResponse response = fx.service.alterarGraduacao(20L,
                alterar(2L, MotivoAlteracaoGraduacao.AJUSTE_ADMINISTRATIVO, "Ajuste"));

        assertEquals("Azul", response.getNovaGraduacaoNome());
        assertFalse(response.getPromocaoPorAvaliacao());
        verify(fx.alunoRepository).save(fx.aluno);
    }

    @Test
    void rebaixamentoComMotivoEObservacao() {
        Fixture fx = fixture();
        fx.aluno.setGraduacaoEntidade(fx.proxima);
        fx.aluno.setGraduacao("Azul");
        when(fx.graduacaoRepository.findByIdAndAcademiaId(1L, 1L)).thenReturn(Optional.of(fx.atual));
        when(fx.historicoRepository.save(any(HistoricoGraduacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        HistoricoGraduacaoResponse response = fx.service.alterarGraduacao(20L,
                alterar(1L, MotivoAlteracaoGraduacao.REBAIXAMENTO, "Correção disciplinar"));

        assertEquals("Branco", response.getNovaGraduacaoNome());
    }

    @Test
    void rebaixamentoSemObservacaoBloqueado() {
        Fixture fx = fixture();
        fx.aluno.setGraduacaoEntidade(fx.proxima);
        when(fx.graduacaoRepository.findByIdAndAcademiaId(1L, 1L)).thenReturn(Optional.of(fx.atual));

        assertThrows(RuntimeException.class, () -> fx.service.alterarGraduacao(20L,
                alterar(1L, MotivoAlteracaoGraduacao.REBAIXAMENTO, "")));
        verify(fx.historicoRepository, never()).save(any());
    }

    @Test
    void graduacaoDeOutraAcademiaBloqueada() {
        Fixture fx = fixture();
        when(fx.graduacaoRepository.findByIdAndAcademiaId(99L, 1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> fx.service.alterarGraduacao(20L,
                alterar(99L, MotivoAlteracaoGraduacao.AJUSTE_ADMINISTRATIVO, "Outra academia")));
    }

    @Test
    void professorNaoAutorizadoOuAlunoTentandoAlterarBloqueia() {
        Fixture fx = fixture();
        doThrow(new AccessDeniedException("Acesso permitido apenas para ADMIN ou PROFESSOR"))
                .when(fx.permissaoService).exigirAdminOuProfessor();

        assertThrows(AccessDeniedException.class, () -> fx.service.alterarGraduacao(20L,
                alterar(2L, MotivoAlteracaoGraduacao.AJUSTE_ADMINISTRATIVO, "Ajuste")));
    }

    @Test
    void alteracaoRepetidaBloqueada() {
        Fixture fx = fixture();

        assertThrows(RuntimeException.class, () -> fx.service.alterarGraduacao(20L,
                alterar(1L, MotivoAlteracaoGraduacao.AJUSTE_ADMINISTRATIVO, "Repetida")));
        verify(fx.historicoRepository, never()).save(any());
    }

    @Test
    void listaElegiveisComFiltros() {
        Fixture fx = fixture();
        when(fx.checkInRepository.countByAcademiaIdAndAlunoIdAndAtivoTrueAndDataHoraBetween(eq(1L), eq(20L), any(), any()))
                .thenReturn(10L);

        List<AlunoElegivelGraduacaoResponse> response = fx.service.listarElegiveis(1L, 2L, null, true);

        assertEquals(1, response.size());
        assertTrue(response.get(0).getElegivel());
    }

    @Test
    void consultaHistoricoProprio() {
        Fixture fx = fixture();
        fx.usuario.setPerfil(PerfilUsuario.ALUNO);
        fx.usuario.setAluno(fx.aluno);
        when(fx.historicoRepository.findByAcademiaIdAndAlunoIdOrderByAlteradoEmDesc(1L, 20L))
                .thenReturn(List.of(historico(fx, fx.atual, fx.proxima, HOJE.atStartOfDay(), true)));

        List<HistoricoGraduacaoResponse> response = fx.service.listarHistoricoAlunoAtual();

        assertEquals(1, response.size());
        assertEquals("Azul", response.get(0).getNovaGraduacaoNome());
    }

    @Test
    void isolamentoPorAcademiaNoHistorico() {
        Fixture fx = fixture();
        when(fx.alunoRepository.findByIdAndAcademiaId(20L, 1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> fx.service.listarHistoricoAluno(20L));
    }

    private Fixture fixture() {
        AlunoRepository alunoRepository = mock(AlunoRepository.class);
        GraduacaoRepository graduacaoRepository = mock(GraduacaoRepository.class);
        HistoricoGraduacaoRepository historicoRepository = mock(HistoricoGraduacaoRepository.class);
        CheckInRepository checkInRepository = mock(CheckInRepository.class);
        ProfessorRepository professorRepository = mock(ProfessorRepository.class);
        GraduacaoService graduacaoService = mock(GraduacaoService.class);
        PermissaoService permissaoService = mock(PermissaoService.class);
        Clock clock = Clock.fixed(HOJE.atTime(12, 0).atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());

        Academia academia = academia();
        Graduacao atual = graduacao(1L, "Branco", 1, 0, 0, academia);
        Graduacao proxima = graduacao(2L, "Azul", 2, 10, 0, academia);
        Aluno aluno = aluno(academia, atual);
        Usuario usuario = usuario(academia, PerfilUsuario.PROFESSOR, aluno);

        when(permissaoService.academiaAtualId()).thenReturn(1L);
        when(permissaoService.usuarioAtual()).thenReturn(usuario);
        when(alunoRepository.findByIdAndAcademiaId(20L, 1L)).thenReturn(Optional.of(aluno));
        when(alunoRepository.findByAtivoTrueAndAcademiaId(1L)).thenReturn(List.of(aluno));
        when(graduacaoRepository.findByIdAndAcademiaId(1L, 1L)).thenReturn(Optional.of(atual));
        when(graduacaoRepository.findByIdAndAcademiaId(2L, 1L)).thenReturn(Optional.of(proxima));
        when(graduacaoService.buscarProximaGraduacao(1L, atual)).thenReturn(Optional.of(proxima));
        when(historicoRepository.findFirstByAcademiaIdAndAlunoIdAndNovaGraduacaoIdOrderByAlteradoEmDesc(1L, 20L, 1L))
                .thenReturn(Optional.of(historico(new Fixture(alunoRepository, graduacaoRepository, historicoRepository,
                        checkInRepository, professorRepository, graduacaoService, permissaoService, null, academia, aluno,
                        atual, proxima, usuario), null, atual, HOJE.minusMonths(3).atStartOfDay(), false)));
        when(checkInRepository.countByAcademiaIdAndAlunoIdAndAtivoTrueAndDataHoraBetween(anyLong(), anyLong(), any(), any()))
                .thenReturn(0L);

        GraduacaoAlunoService service = new GraduacaoAlunoService(alunoRepository, graduacaoRepository,
                historicoRepository, checkInRepository, professorRepository, graduacaoService, permissaoService, clock);
        return new Fixture(alunoRepository, graduacaoRepository, historicoRepository, checkInRepository,
                professorRepository, graduacaoService, permissaoService, service, academia, aluno, atual, proxima, usuario);
    }

    private AlterarGraduacaoAlunoRequest alterar(Long graduacaoId, MotivoAlteracaoGraduacao motivo, String observacao) {
        AlterarGraduacaoAlunoRequest request = new AlterarGraduacaoAlunoRequest();
        request.setNovaGraduacaoId(graduacaoId);
        request.setMotivo(motivo);
        request.setObservacao(observacao);
        return request;
    }

    private PromoverGraduacaoAlunoRequest promover(String observacao) {
        PromoverGraduacaoAlunoRequest request = new PromoverGraduacaoAlunoRequest();
        request.setObservacao(observacao);
        return request;
    }

    private Academia academia() {
        Academia academia = new Academia();
        academia.setId(1L);
        academia.setNome("Elite");
        return academia;
    }

    private Graduacao graduacao(Long id, String nome, Integer ordem, Integer meta, Integer meses, Academia academia) {
        Graduacao graduacao = new Graduacao();
        graduacao.setId(id);
        graduacao.setNome(nome);
        graduacao.setCorHex("#FFFFFF");
        graduacao.setOrdem(ordem);
        graduacao.setCheckinsNecessarios(meta);
        graduacao.setTempoMinimoMeses(meses);
        graduacao.setAtiva(true);
        graduacao.setAcademia(academia);
        return graduacao;
    }

    private Aluno aluno(Academia academia, Graduacao graduacao) {
        Aluno aluno = new Aluno();
        aluno.setId(20L);
        aluno.setNome("Aluno");
        aluno.setAtivo(true);
        aluno.setAcademia(academia);
        aluno.setGraduacaoEntidade(graduacao);
        return aluno;
    }

    private Usuario usuario(Academia academia, PerfilUsuario perfil, Aluno aluno) {
        Usuario usuario = new Usuario();
        usuario.setId(30L);
        usuario.setNome("Responsável");
        usuario.setEmail("resp@email.com");
        usuario.setPerfil(perfil);
        usuario.setAcademia(academia);
        if (perfil == PerfilUsuario.ALUNO) {
            usuario.setAluno(aluno);
        }
        return usuario;
    }

    private HistoricoGraduacao historico(Fixture fx, Graduacao anterior, Graduacao nova,
                                         LocalDateTime data, boolean promocao) {
        HistoricoGraduacao historico = new HistoricoGraduacao();
        historico.setId(40L);
        historico.setAcademia(fx.academia);
        historico.setAluno(fx.aluno);
        historico.setGraduacaoAnterior(anterior);
        historico.setNovaGraduacao(nova);
        historico.setAlteradoEm(data);
        historico.setAlteradoPor(fx.usuario);
        historico.setMotivo(promocao ? MotivoAlteracaoGraduacao.APROVACAO_AVALIACAO : MotivoAlteracaoGraduacao.CADASTRO_INICIAL);
        historico.setPromocaoPorAvaliacao(promocao);
        return historico;
    }

    private record Fixture(AlunoRepository alunoRepository,
                           GraduacaoRepository graduacaoRepository,
                           HistoricoGraduacaoRepository historicoRepository,
                           CheckInRepository checkInRepository,
                           ProfessorRepository professorRepository,
                           GraduacaoService graduacaoService,
                           PermissaoService permissaoService,
                           GraduacaoAlunoService service,
                           Academia academia,
                           Aluno aluno,
                           Graduacao atual,
                           Graduacao proxima,
                           Usuario usuario) {
    }
}
