package Tic.Implementacao.service;

import Tic.Implementacao.dto.notificacao.NotificacaoResponse;
import Tic.Implementacao.model.*;
import Tic.Implementacao.repository.*;
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

class NotificacaoServiceTest {

    private static final Clock CLOCK = Clock.fixed(
            LocalDateTime.of(2026, 7, 15, 10, 0).atZone(ZoneId.systemDefault()).toInstant(),
            ZoneId.systemDefault()
    );

    @Test
    void conviteManualCriaNotificacaoEConviteDuplicadoNaoDuplica() {
        Fixture fx = fixture();
        ConviteGraduacao convite = convite(fx);
        when(fx.notificacaoRepository.save(any(Notificacao.class))).thenAnswer(invocation -> withId(invocation.getArgument(0), 1L));

        Notificacao primeira = fx.service.notificarConviteGraduacao(convite);
        when(fx.notificacaoRepository.findByChaveIdempotencia(primeira.getChaveIdempotencia())).thenReturn(Optional.of(primeira));
        Notificacao segunda = fx.service.notificarConviteGraduacao(convite);

        assertEquals(TipoNotificacao.CONVITE_GRADUACAO_RECEBIDO, primeira.getTipoNotificacao());
        assertEquals("EVENTO_GRADUACAO", primeira.getReferenciaTipo());
        assertSame(primeira, segunda);
        verify(fx.notificacaoRepository, times(1)).save(any(Notificacao.class));
    }

    @Test
    void inscricaoGratuitaEInscricaoPagaGeramTiposCorretos() {
        Fixture fx = fixture();
        when(fx.notificacaoRepository.save(any(Notificacao.class))).thenAnswer(invocation -> invocation.getArgument(0));
        InscricaoGraduacao gratuita = inscricao(fx, BigDecimal.ZERO, StatusInscricaoGraduacao.CONFIRMADA);
        InscricaoGraduacao paga = inscricao(fx, BigDecimal.valueOf(120), StatusInscricaoGraduacao.PENDENTE_PAGAMENTO);

        Notificacao confirmada = fx.service.notificarInscricaoGraduacao(gratuita);
        Notificacao pendente = fx.service.notificarInscricaoGraduacao(paga);

        assertEquals(TipoNotificacao.INSCRICAO_GRADUACAO_CONFIRMADA, confirmada.getTipoNotificacao());
        assertEquals(TipoNotificacao.PAGAMENTO_GRADUACAO_PENDENTE, pendente.getTipoNotificacao());
    }

    @Test
    void pagamentoConfirmadoWebhookRepetidoPagamentoVencidoEEstornoGeramSemDuplicar() {
        Fixture fx = fixture();
        InscricaoGraduacao inscricao = inscricao(fx, BigDecimal.valueOf(120), StatusInscricaoGraduacao.PENDENTE_PAGAMENTO);
        Pagamento pagamento = pagamento(inscricao);
        when(fx.notificacaoRepository.save(any(Notificacao.class))).thenAnswer(invocation -> withId(invocation.getArgument(0), 2L));

        Notificacao confirmada = fx.service.notificarPagamentoGraduacao(pagamento, StatusPagamento.PAGO);
        when(fx.notificacaoRepository.findByChaveIdempotencia(confirmada.getChaveIdempotencia())).thenReturn(Optional.of(confirmada));
        Notificacao repetida = fx.service.notificarPagamentoGraduacao(pagamento, StatusPagamento.PAGO);

        assertEquals(TipoNotificacao.PAGAMENTO_GRADUACAO_CONFIRMADO, confirmada.getTipoNotificacao());
        assertSame(confirmada, repetida);

        when(fx.notificacaoRepository.findByChaveIdempotencia(anyString())).thenReturn(Optional.empty());
        assertEquals(TipoNotificacao.PAGAMENTO_GRADUACAO_FALHOU,
                fx.service.notificarPagamentoGraduacao(pagamento, StatusPagamento.FALHOU).getTipoNotificacao());
        assertEquals(TipoNotificacao.PAGAMENTO_GRADUACAO_PENDENTE,
                fx.service.notificarPagamentoGraduacao(pagamento, StatusPagamento.ESTORNADO).getTipoNotificacao());
    }

    @Test
    void eventoCanceladoNotificaConvidadosEInscritosSemRepetirAlunoECancelaFuturas() {
        Fixture fx = fixture();
        ConviteGraduacao convite = convite(fx);
        InscricaoGraduacao inscricao = inscricao(fx, BigDecimal.ZERO, StatusInscricaoGraduacao.CONFIRMADA);
        when(fx.conviteRepository.findByAcademiaIdAndEventoIdAndStatusIn(eq(1L), eq(100L), anyList())).thenReturn(List.of(convite));
        when(fx.inscricaoRepository.findByAcademiaIdAndEventoIdOrderByInscritoEmDesc(1L, 100L)).thenReturn(List.of(inscricao));
        when(fx.notificacaoRepository.save(any(Notificacao.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Notificacao futura = new Notificacao(TipoNotificacao.LEMBRETE_EVENTO_GRADUACAO, "Lembrete");
        futura.setStatus(StatusNotificacao.PENDENTE);
        when(fx.notificacaoRepository.buscarRelatorio(eq(1L), isNull(), isNull(), isNull(), eq(StatusNotificacao.PENDENTE), isNull(), eq(100L)))
                .thenReturn(List.of(futura));

        fx.service.notificarEventoCancelado(fx.evento);

        verify(fx.notificacaoRepository, times(2)).save(any(Notificacao.class));
        assertEquals(StatusNotificacao.CANCELADA, futura.getStatus());
    }

    @Test
    void avaliacaoDisponivelEPromocaoConfirmadaGeramNotificacoes() {
        Fixture fx = fixture();
        AvaliacaoGraduacao avaliacao = avaliacao(fx);
        when(fx.notificacaoRepository.save(any(Notificacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Notificacao resultado = fx.service.notificarResultadoAvaliacao(avaliacao);
        Notificacao promocao = fx.service.notificarPromocaoConfirmada(avaliacao, fx.atual, fx.destino);

        assertEquals(TipoNotificacao.RESULTADO_AVALIACAO_DISPONIVEL, resultado.getTipoNotificacao());
        assertEquals(TipoNotificacao.PROMOCAO_GRADUACAO_CONFIRMADA, promocao.getTipoNotificacao());
        assertTrue(promocao.getMensagem().contains("Branco"));
        assertTrue(promocao.getMensagem().contains("Azul"));
    }

    @Test
    void alunoListaFiltraEMarcaPropriasNotificacoes() {
        Fixture fx = fixture();
        Notificacao naoLida = notificacaoAluno(fx, 1L, false);
        Notificacao outra = notificacaoAluno(fx, 2L, false);
        when(fx.permissaoService.usuarioAtual()).thenReturn(fx.usuario);
        when(fx.permissaoService.academiaAtualId()).thenReturn(1L);
        when(fx.notificacaoRepository.findByDestinatarioIdAndAcademiaIdOrderByCriadaEmDesc(10L, 1L)).thenReturn(List.of(naoLida, outra));
        when(fx.notificacaoRepository.findByDestinatarioIdAndAcademiaIdAndLidaFalseOrderByCriadaEmDesc(10L, 1L)).thenReturn(List.of(naoLida));
        when(fx.notificacaoRepository.findByIdAndDestinatarioIdAndAcademiaId(1L, 10L, 1L)).thenReturn(Optional.of(naoLida));
        when(fx.notificacaoRepository.findByDestinatarioIdAndAcademiaIdAndLidaFalse(10L, 1L)).thenReturn(List.of(naoLida, outra));
        when(fx.notificacaoRepository.save(any(Notificacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<NotificacaoResponse> todas = fx.service.listarDoUsuarioAtual(false);
        List<NotificacaoResponse> filtradas = fx.service.listarDoUsuarioAtual(true);
        NotificacaoResponse lida = fx.service.marcarComoLida(1L);
        fx.service.marcarTodasComoLidas();

        assertEquals(2, todas.size());
        assertEquals(1, filtradas.size());
        assertTrue(lida.getLida());
        assertTrue(outra.getLida());
    }

    @Test
    void alunoNaoAcessaNotificacaoDeOutroUsuario() {
        Fixture fx = fixture();
        when(fx.permissaoService.usuarioAtual()).thenReturn(fx.usuario);
        when(fx.permissaoService.academiaAtualId()).thenReturn(1L);
        when(fx.notificacaoRepository.findByIdAndDestinatarioIdAndAcademiaId(99L, 10L, 1L)).thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class, () -> fx.service.marcarComoLida(99L));
    }

    @Test
    void relatorioRespeitaAcademiaEFiltrosSemAlterarDados() {
        Fixture fx = fixture();
        Notificacao notificacao = notificacaoAluno(fx, 1L, true);
        when(fx.permissaoService.academiaAtualId()).thenReturn(1L);
        when(fx.notificacaoRepository.buscarRelatorio(eq(1L), any(), any(), eq(TipoNotificacao.CONVITE_GRADUACAO_RECEBIDO),
                eq(StatusNotificacao.ENVIADA), eq(20L), eq(100L))).thenReturn(List.of(notificacao));

        var relatorio = fx.service.relatorio(LocalDateTime.now(CLOCK).minusDays(1), LocalDateTime.now(CLOCK),
                TipoNotificacao.CONVITE_GRADUACAO_RECEBIDO, StatusNotificacao.ENVIADA, 20L, 100L);

        assertEquals(1, relatorio.size());
        assertEquals(20L, relatorio.get(0).getAlunoId());
        verify(fx.permissaoService).exigirAdminOuProfessor();
        verify(fx.notificacaoRepository, never()).delete(any());
    }

    @Test
    void geraAvisoPrazoELembreteEventoPorEndpointManual() {
        Fixture fx = fixture();
        fx.evento.setFimInscricoes(LocalDateTime.now(CLOCK).plusHours(24));
        fx.evento.setDataEvento(LocalDate.now(CLOCK).plusDays(1));
        fx.evento.setHoraInicio(LocalTime.of(9, 0));
        when(fx.permissaoService.academiaAtualId()).thenReturn(1L);
        when(fx.eventoRepository.findByAcademiaIdAndStatusOrderByDataEventoAsc(1L, StatusEventoGraduacao.PUBLICADO)).thenReturn(List.of(fx.evento));
        when(fx.conviteRepository.findByAcademiaIdAndEventoIdOrderByEnviadoEmDesc(1L, 100L)).thenReturn(List.of(convite(fx)));
        when(fx.inscricaoRepository.findByAcademiaIdAndEventoIdOrderByInscritoEmDesc(1L, 100L)).thenReturn(List.of());
        when(fx.notificacaoRepository.save(any(Notificacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertEquals(1, fx.service.gerarAvisosPrazoInscricaoProximo());
        assertEquals(1, fx.service.gerarLembretesEventoGraduacao());
    }

    private Fixture fixture() {
        NotificacaoRepository notificacaoRepository = mock(NotificacaoRepository.class);
        FinanceiroRepository financeiroRepository = mock(FinanceiroRepository.class);
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        ConviteGraduacaoRepository conviteRepository = mock(ConviteGraduacaoRepository.class);
        InscricaoGraduacaoRepository inscricaoRepository = mock(InscricaoGraduacaoRepository.class);
        EventoGraduacaoRepository eventoRepository = mock(EventoGraduacaoRepository.class);
        PermissaoService permissaoService = mock(PermissaoService.class);
        NotificacaoService service = new NotificacaoService(notificacaoRepository, financeiroRepository, usuarioRepository,
                conviteRepository, inscricaoRepository, eventoRepository, permissaoService, CLOCK, 48, 24, false);

        Academia academia = new Academia();
        academia.setId(1L);
        academia.setNome("Elite");
        Graduacao atual = graduacao(1L, "Branco", academia);
        Graduacao destino = graduacao(2L, "Azul", academia);
        Aluno aluno = new Aluno();
        aluno.setId(20L);
        aluno.setNome("Aluno");
        aluno.setAcademia(academia);
        aluno.setGraduacaoEntidade(atual);
        Usuario usuario = new Usuario();
        usuario.setId(10L);
        usuario.setNome("Aluno");
        usuario.setPerfil(PerfilUsuario.ALUNO);
        usuario.setAcademia(academia);
        usuario.setAluno(aluno);
        EventoGraduacao evento = new EventoGraduacao();
        evento.setId(100L);
        evento.setAcademia(academia);
        evento.setTitulo("Graduação Elite");
        evento.setDataEvento(LocalDate.of(2026, 8, 1));
        evento.setHoraInicio(LocalTime.of(9, 0));
        evento.setHoraFim(LocalTime.of(12, 0));
        evento.setLocal("Tatame principal");
        evento.setFimInscricoes(LocalDateTime.of(2026, 7, 30, 18, 0));
        evento.setStatus(StatusEventoGraduacao.PUBLICADO);

        when(usuarioRepository.findByAlunoIdAndAcademiaId(20L, 1L)).thenReturn(Optional.of(usuario));
        return new Fixture(service, notificacaoRepository, financeiroRepository, usuarioRepository, conviteRepository,
                inscricaoRepository, eventoRepository, permissaoService, academia, atual, destino, aluno, usuario, evento);
    }

    private Graduacao graduacao(Long id, String nome, Academia academia) {
        Graduacao graduacao = new Graduacao();
        graduacao.setId(id);
        graduacao.setNome(nome);
        graduacao.setAcademia(academia);
        return graduacao;
    }

    private ConviteGraduacao convite(Fixture fx) {
        ConviteGraduacao convite = new ConviteGraduacao();
        convite.setId(50L);
        convite.setAcademia(fx.academia);
        convite.setAluno(fx.aluno);
        convite.setEvento(fx.evento);
        convite.setGraduacaoAtual(fx.atual);
        convite.setGraduacaoDestino(fx.destino);
        convite.setStatus(StatusConviteGraduacao.PENDENTE);
        convite.setEnviadoEm(LocalDateTime.now(CLOCK));
        convite.setEnviadoPor(fx.usuario);
        return convite;
    }

    private InscricaoGraduacao inscricao(Fixture fx, BigDecimal valor, StatusInscricaoGraduacao status) {
        InscricaoGraduacao inscricao = new InscricaoGraduacao();
        inscricao.setId(70L);
        inscricao.setAcademia(fx.academia);
        inscricao.setAluno(fx.aluno);
        inscricao.setEvento(fx.evento);
        inscricao.setGraduacaoDestino(fx.destino);
        inscricao.setValor(valor);
        inscricao.setStatus(status);
        inscricao.setInscritoEm(LocalDateTime.now(CLOCK));
        return inscricao;
    }

    private Pagamento pagamento(InscricaoGraduacao inscricao) {
        Pagamento pagamento = new Pagamento();
        pagamento.setId(80L);
        pagamento.setAcademia(inscricao.getAcademia());
        pagamento.setInscricaoGraduacao(inscricao);
        pagamento.setTipoCobranca(TipoCobranca.GRADUACAO);
        pagamento.setStatus(StatusPagamento.PENDENTE);
        pagamento.setValor(inscricao.getValor());
        return pagamento;
    }

    private AvaliacaoGraduacao avaliacao(Fixture fx) {
        AvaliacaoGraduacao avaliacao = new AvaliacaoGraduacao();
        avaliacao.setId(90L);
        avaliacao.setAcademia(fx.academia);
        avaliacao.setAluno(fx.aluno);
        avaliacao.setEvento(fx.evento);
        avaliacao.setGraduacaoAtual(fx.atual);
        avaliacao.setGraduacaoDestino(fx.destino);
        return avaliacao;
    }

    private Notificacao notificacaoAluno(Fixture fx, Long id, boolean lida) {
        Notificacao notificacao = new Notificacao(TipoNotificacao.CONVITE_GRADUACAO_RECEBIDO, "Mensagem");
        notificacao.setId(id);
        notificacao.setAcademia(fx.academia);
        notificacao.setDestinatario(fx.usuario);
        notificacao.setAluno(fx.aluno);
        notificacao.setTitulo("Título");
        notificacao.setLida(lida);
        notificacao.setCriadaEm(LocalDateTime.now(CLOCK));
        notificacao.setReferenciaTipo("EVENTO_GRADUACAO");
        notificacao.setReferenciaId(100L);
        notificacao.setStatus(StatusNotificacao.ENVIADA);
        return notificacao;
    }

    private Notificacao withId(Notificacao notificacao, Long id) {
        notificacao.setId(id);
        return notificacao;
    }

    private record Fixture(NotificacaoService service,
                           NotificacaoRepository notificacaoRepository,
                           FinanceiroRepository financeiroRepository,
                           UsuarioRepository usuarioRepository,
                           ConviteGraduacaoRepository conviteRepository,
                           InscricaoGraduacaoRepository inscricaoRepository,
                           EventoGraduacaoRepository eventoRepository,
                           PermissaoService permissaoService,
                           Academia academia,
                           Graduacao atual,
                           Graduacao destino,
                           Aluno aluno,
                           Usuario usuario,
                           EventoGraduacao evento) {
    }
}
