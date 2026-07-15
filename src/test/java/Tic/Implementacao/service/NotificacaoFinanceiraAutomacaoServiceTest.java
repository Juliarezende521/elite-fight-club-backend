package Tic.Implementacao.service;

import Tic.Implementacao.dto.notificacao.financeiro.NotificacaoFinanceiraRelatorioResponse;
import Tic.Implementacao.dto.notificacao.financeiro.ResultadoAutomacaoNotificacaoFinanceiraResponse;
import Tic.Implementacao.dto.financeiro.preferencia.ContextoPagamentoAluno;
import Tic.Implementacao.model.*;
import Tic.Implementacao.repository.*;
import Tic.Implementacao.security.PermissaoService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class NotificacaoFinanceiraAutomacaoServiceTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 7, 15);
    private static final Clock CLOCK = Clock.fixed(HOJE.atTime(9, 0).atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());

    @Test
    void criaLembreteParaFaturaVencendoHojeSemPagamentoIniciado() {
        Fixture fx = fixture();
        Fatura fatura = fatura(fx, 101L, StatusFatura.PENDENTE, HOJE, "200");
        preparar(fx, List.of(fatura), List.of(), List.of(fx.usuarioAluno));
        when(fx.notificacaoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ResultadoAutomacaoNotificacaoFinanceiraResponse response = fx.service.gerarLembretesDeVencimento(HOJE);

        assertEquals(1, response.getFaturasAnalisadas());
        assertEquals(1, response.getNotificacoesCriadas());
        verify(fx.notificacaoRepository).save(argThat(n ->
                n.getTipoNotificacao() == TipoNotificacao.VENCIMENTO_MENSALIDADE_HOJE
                        && n.getChaveIdempotencia().equals("VENCIMENTO_MENSALIDADE_HOJE:101:2026-07-15")
                        && n.getMensagem().contains("vence hoje")));
    }

    @Test
    void ignoraFaturaPagaSaldoZeradoCanceladaEAlunoInativo() {
        Fixture fx = fixture();
        Fatura paga = fatura(fx, 101L, StatusFatura.PAGA, HOJE, "200");
        Fatura cancelada = fatura(fx, 102L, StatusFatura.CANCELADA, HOJE, "200");
        Fatura inativa = fatura(fx, 103L, StatusFatura.PENDENTE, HOJE, "200");
        inativa.getAluno().setAtivo(false);
        preparar(fx, List.of(paga, cancelada, inativa), List.of(), List.of(fx.usuarioAluno));

        ResultadoAutomacaoNotificacaoFinanceiraResponse response = fx.service.gerarLembretesDeVencimento(HOJE);

        assertEquals(3, response.getFaturasAnalisadas());
        assertEquals(0, response.getNotificacoesCriadas());
        assertEquals(3, response.getNotificacoesIgnoradas());
        assertEquals(1, response.getMotivosIgnorados().get("FATURA_PAGA"));
        assertEquals(1, response.getMotivosIgnorados().get("FATURA_CANCELADA"));
        assertEquals(1, response.getMotivosIgnorados().get("ALUNO_INATIVO"));
    }

    @Test
    void pixValidoNaoCriaNovaCobrancaMasEnviaLembreteInformativo() {
        Fixture fx = fixture();
        Fatura fatura = fatura(fx, 101L, StatusFatura.PENDENTE, HOJE, "200");
        Pagamento pix = pagamento(fx, fatura, 1L, StatusPagamento.PENDENTE, "200");
        pix.setMetodo(MetodoPagamento.PIX);
        pix.setIdPagamentoExterno("pay_1");
        pix.setDataExpiracao(LocalDateTime.now(CLOCK).plusHours(2));
        preparar(fx, List.of(fatura), List.of(pix), List.of(fx.usuarioAluno));
        when(fx.preferenciaPagamentoAlunoService.montarContextoPagamento(eq(fatura), anyList(), nullable(PreferenciaPagamentoAluno.class)))
                .thenReturn(new ContextoPagamentoAluno(FormaPagamentoPreferida.PIX, StatusAutorizacaoPagamento.NAO_SOLICITADA,
                        false, false, true, false));
        when(fx.notificacaoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        fx.service.gerarLembretesDeVencimento(HOJE);

        verify(fx.notificacaoRepository).save(argThat(n -> n.getMensagem().contains("Existe um Pix disponível")));
        verify(fx.pagamentoRepository, never()).save(any());
        verify(fx.faturaRepository, never()).save(any());
    }

    @Test
    void pagamentoProcessandoCriaNotificacaoInformativaENaoAvisoComum() {
        Fixture fx = fixture();
        Fatura fatura = fatura(fx, 101L, StatusFatura.PENDENTE, HOJE, "200");
        Pagamento processando = pagamento(fx, fatura, 1L, StatusPagamento.PROCESSANDO, "200");
        preparar(fx, List.of(fatura), List.of(processando), List.of(fx.usuarioAluno));
        when(fx.preferenciaPagamentoAlunoService.montarContextoPagamento(eq(fatura), anyList(), nullable(PreferenciaPagamentoAluno.class)))
                .thenReturn(new ContextoPagamentoAluno(FormaPagamentoPreferida.PIX_AUTOMATICO, StatusAutorizacaoPagamento.ATIVA,
                        true, true, false, false));
        when(fx.notificacaoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        fx.service.gerarLembretesDeVencimento(HOJE);

        verify(fx.notificacaoRepository).save(argThat(n -> n.getTipoNotificacao() == TipoNotificacao.PAGAMENTO_MENSALIDADE_PROCESSANDO
                && n.getMensagem().contains("pagamento automático")));
    }

    @Test
    void pixAutomaticoPendenteInformaAutorizacaoNaoConcluida() {
        Fixture fx = fixture();
        Fatura fatura = fatura(fx, 101L, StatusFatura.PENDENTE, HOJE, "200");
        preparar(fx, List.of(fatura), List.of(), List.of(fx.usuarioAluno));
        when(fx.preferenciaPagamentoAlunoService.montarContextoPagamento(eq(fatura), anyList(), nullable(PreferenciaPagamentoAluno.class)))
                .thenReturn(new ContextoPagamentoAluno(FormaPagamentoPreferida.PIX_AUTOMATICO, StatusAutorizacaoPagamento.PENDENTE,
                        false, false, false, false));
        when(fx.notificacaoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        fx.service.gerarLembretesDeVencimento(HOJE);

        verify(fx.notificacaoRepository).save(argThat(n -> n.getMensagem().contains("autorização do Pix Automático ainda não foi concluída")));
    }

    @Test
    void cartaoNaoConfiguradoInformaConclusaoPendente() {
        Fixture fx = fixture();
        Fatura fatura = fatura(fx, 101L, StatusFatura.PENDENTE, HOJE, "200");
        preparar(fx, List.of(fatura), List.of(), List.of(fx.usuarioAluno));
        when(fx.preferenciaPagamentoAlunoService.montarContextoPagamento(eq(fatura), anyList(), nullable(PreferenciaPagamentoAluno.class)))
                .thenReturn(new ContextoPagamentoAluno(FormaPagamentoPreferida.CARTAO, StatusAutorizacaoPagamento.NAO_SOLICITADA,
                        false, false, false, false));
        when(fx.notificacaoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        fx.service.gerarLembretesDeVencimento(HOJE);

        verify(fx.notificacaoRepository).save(argThat(n -> n.getMensagem().contains("forma cartão ainda precisa ser concluída")));
    }

    @Test
    void boletoValidoInformaCobrancaDisponivelSemCriarPagamento() {
        Fixture fx = fixture();
        Fatura fatura = fatura(fx, 101L, StatusFatura.PENDENTE, HOJE, "200");
        Pagamento boleto = pagamento(fx, fatura, 1L, StatusPagamento.PENDENTE, "200");
        boleto.setMetodo(MetodoPagamento.BOLETO);
        boleto.setIdPagamentoExterno("bol_1");
        boleto.setDataExpiracao(LocalDateTime.now(CLOCK).plusDays(1));
        preparar(fx, List.of(fatura), List.of(boleto), List.of(fx.usuarioAluno));
        when(fx.preferenciaPagamentoAlunoService.montarContextoPagamento(eq(fatura), anyList(), nullable(PreferenciaPagamentoAluno.class)))
                .thenReturn(new ContextoPagamentoAluno(FormaPagamentoPreferida.BOLETO, StatusAutorizacaoPagamento.NAO_SOLICITADA,
                        false, false, false, true));
        when(fx.notificacaoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        fx.service.gerarLembretesDeVencimento(HOJE);

        verify(fx.notificacaoRepository).save(argThat(n -> n.getMensagem().contains("boleto disponível")));
        verify(fx.pagamentoRepository, never()).save(any());
    }

    @Test
    void dinheiroAguardaConfirmacaoManual() {
        Fixture fx = fixture();
        Fatura fatura = fatura(fx, 101L, StatusFatura.PENDENTE, HOJE, "200");
        preparar(fx, List.of(fatura), List.of(), List.of(fx.usuarioAluno));
        when(fx.preferenciaPagamentoAlunoService.montarContextoPagamento(eq(fatura), anyList(), nullable(PreferenciaPagamentoAluno.class)))
                .thenReturn(new ContextoPagamentoAluno(FormaPagamentoPreferida.DINHEIRO, StatusAutorizacaoPagamento.NAO_SOLICITADA,
                        false, false, false, false));
        when(fx.notificacaoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        fx.service.gerarLembretesDeVencimento(HOJE);

        verify(fx.notificacaoRepository).save(argThat(n -> n.getMensagem().contains("confirmação manual")));
    }

    @Test
    void pagamentoParcialEAtrasoDeDoisDiasInformamSaldoPendente() {
        Fixture fx = fixture();
        Fatura fatura = fatura(fx, 101L, StatusFatura.PARCIALMENTE_PAGA, HOJE.minusDays(2), "200");
        Pagamento parcial = pagamento(fx, fatura, 1L, StatusPagamento.PAGO, "150");
        preparar(fx, List.of(fatura), List.of(parcial), List.of(fx.usuarioAluno));
        when(fx.notificacaoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ResultadoAutomacaoNotificacaoFinanceiraResponse response = fx.service.gerarAvisosDeAtraso(HOJE);

        assertEquals(1, response.getNotificacoesCriadas());
        verify(fx.notificacaoRepository).save(argThat(n ->
                n.getTipoNotificacao() == TipoNotificacao.MENSALIDADE_ATRASADA
                        && n.getMensagem().contains("R$ 50,00")));
    }

    @Test
    void naoDuplicaNotificacaoERegistraAlunoSemUsuarioComoFalha() {
        Fixture fx = fixture();
        Fatura duplicada = fatura(fx, 101L, StatusFatura.PENDENTE, HOJE, "200");
        Fatura semUsuario = fatura(fx, 102L, StatusFatura.PENDENTE, HOJE, "200");
        Notificacao existente = new Notificacao(TipoNotificacao.VENCIMENTO_MENSALIDADE_HOJE, "existente");
        preparar(fx, List.of(duplicada, semUsuario), List.of(), List.of(fx.usuarioAluno));
        when(fx.notificacaoRepository.findByChaveIdempotencia("VENCIMENTO_MENSALIDADE_HOJE:101:2026-07-15")).thenReturn(Optional.of(existente));

        ResultadoAutomacaoNotificacaoFinanceiraResponse response = fx.service.gerarLembretesDeVencimento(HOJE);

        assertEquals(1, response.getNotificacoesIgnoradas());
        assertEquals(1, response.getFalhas());
        assertEquals(1, response.getMotivosIgnorados().get("NOTIFICACAO_DUPLICADA"));
        assertEquals(1, response.getMotivosIgnorados().get("USUARIO_NAO_VINCULADO"));
    }

    @Test
    void notificaPagamentoConfirmadoEstornadoCanceladoEWebhookRepetidoNaoDuplica() {
        Fixture fx = fixture();
        Fatura fatura = fatura(fx, 101L, StatusFatura.PENDENTE, HOJE, "200");
        Pagamento pago = pagamento(fx, fatura, 1L, StatusPagamento.PAGO, "200");
        when(fx.usuarioRepository.findByAlunoIdAndAcademiaId(10L, 1L)).thenReturn(Optional.of(fx.usuarioAluno));
        when(fx.notificacaoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        fx.service.notificarPagamentoMensalidade(pago, StatusPagamento.PAGO);
        when(fx.notificacaoRepository.findByChaveIdempotencia("PAGAMENTO_MENSALIDADE_CONFIRMADO:101:1")).thenReturn(Optional.of(new Notificacao()));
        fx.service.notificarPagamentoMensalidade(pago, StatusPagamento.PAGO);
        Pagamento estornado = pagamento(fx, fatura, 2L, StatusPagamento.ESTORNADO, "200");
        fx.service.notificarPagamentoMensalidade(estornado, StatusPagamento.ESTORNADO);
        Pagamento cancelado = pagamento(fx, fatura, 3L, StatusPagamento.CANCELADO, "200");
        fx.service.notificarPagamentoMensalidade(cancelado, StatusPagamento.CANCELADO);

        verify(fx.notificacaoRepository, times(3)).save(any());
    }

    @Test
    void relatorioOperacionalFiltraNotificacoesFinanceiras() {
        Fixture fx = fixture();
        Fatura fatura = fatura(fx, 101L, StatusFatura.PENDENTE, HOJE, "200");
        Notificacao notificacao = new Notificacao(TipoNotificacao.VENCIMENTO_MENSALIDADE_HOJE, "mensagem");
        notificacao.setId(5L);
        notificacao.setAluno(fx.aluno);
        notificacao.setReferenciaTipo("FATURA");
        notificacao.setReferenciaId(101L);
        notificacao.setStatus(StatusNotificacao.ENVIADA);
        notificacao.setCriadaEm(LocalDateTime.now(CLOCK));
        when(fx.permissaoService.academiaAtualId()).thenReturn(1L);
        when(fx.notificacaoRepository.buscarRelatorioFinanceiro(eq(1L), any(), any(),
                nullable(TipoNotificacao.class), nullable(StatusNotificacao.class),
                nullable(Long.class), nullable(Long.class), eq(false)))
                .thenReturn(List.of(notificacao));
        when(fx.faturaRepository.findComAlunoByAcademiaIdAndIdIn(1L, List.of(101L))).thenReturn(List.of(fatura));

        List<NotificacaoFinanceiraRelatorioResponse> response = fx.service.relatorio(LocalDateTime.now(CLOCK).minusDays(1),
                LocalDateTime.now(CLOCK), null, null, null, null, false);

        assertEquals(1, response.size());
        assertEquals(101L, response.get(0).getFaturaId());
    }

    @Test
    void schedulerDesabilitadoNaoExecuta() {
        NotificacaoFinanceiraAutomacaoService service = mock(NotificacaoFinanceiraAutomacaoService.class);
        NotificacaoFinanceiraScheduler scheduler = new NotificacaoFinanceiraScheduler(service, CLOCK, false, "America/Sao_Paulo");

        scheduler.executarDiariamente();

        verify(service, never()).gerarNotificacoesFinanceiras(any());
    }

    private void preparar(Fixture fx, List<Fatura> faturas, List<Pagamento> pagamentos, List<Usuario> usuarios) {
        when(fx.permissaoService.academiaAtualId()).thenReturn(1L);
        LocalDate vencimento = faturas.get(0).getDataVencimento();
        when(fx.faturaRepository.findComAlunoByAcademiaIdAndDataVencimento(1L, vencimento)).thenReturn(faturas);
        when(fx.pagamentoRepository.findMensalidadesComFaturaPorFaturas(eq(1L), anyList())).thenReturn(pagamentos);
        when(fx.usuarioRepository.findByAcademiaIdAndAlunoIdIn(eq(1L), anyList())).thenReturn(usuarios);
        when(fx.preferenciaPagamentoRepository.findByAcademiaIdAndAlunoIdInAndAtivaTrue(eq(1L), anyList())).thenReturn(List.of());
        for (Fatura fatura : faturas) {
            when(fx.preferenciaPagamentoAlunoService.montarContextoPagamento(eq(fatura), anyList(), nullable(PreferenciaPagamentoAluno.class)))
                    .thenReturn(new ContextoPagamentoAluno(null, null, false, false, false, false));
        }
    }

    private Fixture fixture() {
        FaturaRepository faturaRepository = mock(FaturaRepository.class);
        PagamentoRepository pagamentoRepository = mock(PagamentoRepository.class);
        NotificacaoRepository notificacaoRepository = mock(NotificacaoRepository.class);
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        PreferenciaPagamentoAlunoRepository preferenciaPagamentoRepository = mock(PreferenciaPagamentoAlunoRepository.class);
        PermissaoService permissaoService = mock(PermissaoService.class);
        PreferenciaPagamentoAlunoService preferenciaPagamentoAlunoService = mock(PreferenciaPagamentoAlunoService.class);
        Academia academia = new Academia();
        academia.setId(1L);
        academia.setNome("Elite");
        Aluno aluno = new Aluno();
        aluno.setId(10L);
        aluno.setNome("Aluno");
        aluno.setEmail("aluno@email.com");
        aluno.setAcademia(academia);
        aluno.setAtivo(true);
        aluno.setDiaVencimento(15);
        aluno.setValorMensalidade(new BigDecimal("200"));
        Usuario usuario = new Usuario();
        usuario.setId(20L);
        usuario.setNome("Aluno");
        usuario.setEmail("aluno@email.com");
        usuario.setPerfil(PerfilUsuario.ALUNO);
        usuario.setAtivo(true);
        usuario.setAcademia(academia);
        usuario.setAluno(aluno);
        NotificacaoFinanceiraAutomacaoService service = new NotificacaoFinanceiraAutomacaoService(
                faturaRepository, pagamentoRepository, notificacaoRepository, usuarioRepository,
                preferenciaPagamentoRepository, preferenciaPagamentoAlunoService, permissaoService, CLOCK, 2);
        return new Fixture(service, faturaRepository, pagamentoRepository, notificacaoRepository, usuarioRepository,
                preferenciaPagamentoRepository, preferenciaPagamentoAlunoService, permissaoService, academia, aluno, usuario);
    }

    private Fatura fatura(Fixture fx, Long id, StatusFatura status, LocalDate vencimento, String valor) {
        Fatura fatura = new Fatura();
        fatura.setId(id);
        fatura.setAcademia(fx.academia);
        fatura.setAluno(id.equals(102L) ? alunoSemUsuario(fx, id + 10) : fx.aluno);
        fatura.setStatus(status);
        fatura.setMesReferencia(vencimento.withDayOfMonth(1));
        fatura.setDataVencimento(vencimento);
        fatura.setValor(new BigDecimal(valor));
        return fatura;
    }

    private Aluno alunoSemUsuario(Fixture fx, Long id) {
        Aluno aluno = new Aluno();
        aluno.setId(id);
        aluno.setNome("Sem Usuario");
        aluno.setEmail("sem@email.com");
        aluno.setAcademia(fx.academia);
        aluno.setAtivo(true);
        aluno.setDiaVencimento(15);
        aluno.setValorMensalidade(new BigDecimal("200"));
        return aluno;
    }

    private Pagamento pagamento(Fixture fx, Fatura fatura, Long id, StatusPagamento status, String valor) {
        Pagamento pagamento = new Pagamento();
        pagamento.setId(id);
        pagamento.setAcademia(fx.academia);
        pagamento.setFatura(fatura);
        pagamento.setTipoCobranca(TipoCobranca.MENSALIDADE);
        pagamento.setMetodo(MetodoPagamento.PIX);
        pagamento.setStatus(status);
        pagamento.setValor(new BigDecimal(valor));
        pagamento.setCriadoEm(LocalDateTime.now(CLOCK));
        pagamento.setPagoEm(status == StatusPagamento.PAGO ? LocalDateTime.now(CLOCK) : null);
        return pagamento;
    }

    private record Fixture(NotificacaoFinanceiraAutomacaoService service,
                           FaturaRepository faturaRepository,
                           PagamentoRepository pagamentoRepository,
                           NotificacaoRepository notificacaoRepository,
                           UsuarioRepository usuarioRepository,
                           PreferenciaPagamentoAlunoRepository preferenciaPagamentoRepository,
                           PreferenciaPagamentoAlunoService preferenciaPagamentoAlunoService,
                           PermissaoService permissaoService,
                           Academia academia,
                           Aluno aluno,
                           Usuario usuarioAluno) {
    }
}
