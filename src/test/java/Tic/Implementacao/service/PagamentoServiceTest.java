package Tic.Implementacao.service;

import Tic.Implementacao.dto.financeiro.CobrancaPixResponse;
import Tic.Implementacao.dto.financeiro.ConciliacaoPagamentoResponse;
import Tic.Implementacao.dto.financeiro.PagamentoManualRequest;
import Tic.Implementacao.model.*;
import Tic.Implementacao.payments.ProvedorPagamento;
import Tic.Implementacao.payments.asaas.AsaasProperties;
import Tic.Implementacao.repository.AlunoRepository;
import Tic.Implementacao.repository.EventoPagamentoExternoRepository;
import Tic.Implementacao.repository.InscricaoGraduacaoRepository;
import Tic.Implementacao.repository.PagamentoRepository;
import Tic.Implementacao.security.PermissaoService;
import Tic.Implementacao.security.PermissaoSistema;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PagamentoServiceTest {

    @Test
    void deveCriarPixValidoDeInscricaoGraduacaoComValorDaInscricao() {
        Fixture fx = fixture();
        InscricaoGraduacao inscricao = inscricaoGraduacao(BigDecimal.valueOf(120));
        Usuario usuario = usuarioAluno(inscricao.getAluno());
        CobrancaPixResponse cobranca = new CobrancaPixResponse(null, null, StatusPagamento.PENDENTE, BigDecimal.valueOf(999),
                "pay_grad_1", "pix-copia", "qr-url", "base64", LocalDateTime.now().plusDays(1));

        when(fx.permissaoService.usuarioAtual()).thenReturn(usuario);
        when(fx.permissaoService.academiaAtualId()).thenReturn(1L);
        when(fx.inscricaoGraduacaoRepository.findByIdAndAcademiaIdAndAlunoIdForUpdate(55L, 1L, 10L)).thenReturn(Optional.of(inscricao));
        when(fx.provedorPagamento.criarCobrancaPixGraduacao(inscricao)).thenReturn(cobranca);
        when(fx.pagamentoRepository.save(any(Pagamento.class))).thenAnswer(invocation -> {
            Pagamento pagamento = invocation.getArgument(0);
            pagamento.setId(88L);
            return pagamento;
        });

        var response = fx.service.criarPixInscricaoGraduacao(55L);

        assertEquals(55L, response.getInscricaoId());
        assertEquals(BigDecimal.valueOf(120), response.getValor());
        assertEquals(StatusInscricaoGraduacao.PAGAMENTO_PROCESSANDO, inscricao.getStatus());
        verify(fx.provedorPagamento).criarCobrancaPixGraduacao(inscricao);
        verify(fx.faturaService, never()).buscarPorId(anyLong());
    }

    @Test
    void bloqueiaPixDeInscricaoValorZeroOutroAlunoInscricaoCanceladaEEventoCancelado() {
        Fixture fx = fixture();
        InscricaoGraduacao zero = inscricaoGraduacao(BigDecimal.ZERO);
        Usuario usuario = usuarioAluno(zero.getAluno());
        when(fx.permissaoService.usuarioAtual()).thenReturn(usuario);
        when(fx.permissaoService.academiaAtualId()).thenReturn(1L);
        when(fx.inscricaoGraduacaoRepository.findByIdAndAcademiaIdAndAlunoIdForUpdate(55L, 1L, 10L)).thenReturn(Optional.of(zero));
        assertThrows(RuntimeException.class, () -> fx.service.criarPixInscricaoGraduacao(55L));

        when(fx.inscricaoGraduacaoRepository.findByIdAndAcademiaIdAndAlunoIdForUpdate(55L, 1L, 10L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> fx.service.criarPixInscricaoGraduacao(55L));

        InscricaoGraduacao cancelada = inscricaoGraduacao(BigDecimal.valueOf(120));
        cancelada.setStatus(StatusInscricaoGraduacao.CANCELADA);
        when(fx.inscricaoGraduacaoRepository.findByIdAndAcademiaIdAndAlunoIdForUpdate(55L, 1L, 10L)).thenReturn(Optional.of(cancelada));
        assertThrows(RuntimeException.class, () -> fx.service.criarPixInscricaoGraduacao(55L));

        InscricaoGraduacao eventoCancelado = inscricaoGraduacao(BigDecimal.valueOf(120));
        eventoCancelado.getEvento().setStatus(StatusEventoGraduacao.CANCELADO);
        when(fx.inscricaoGraduacaoRepository.findByIdAndAcademiaIdAndAlunoIdForUpdate(55L, 1L, 10L)).thenReturn(Optional.of(eventoCancelado));
        assertThrows(RuntimeException.class, () -> fx.service.criarPixInscricaoGraduacao(55L));
    }

    @Test
    void reutilizaClienteAsaasEPixPendenteValidoDaInscricao() {
        Fixture fx = fixture();
        InscricaoGraduacao inscricao = inscricaoGraduacao(BigDecimal.valueOf(120));
        Usuario usuario = usuarioAluno(inscricao.getAluno());
        Pagamento pagamento = pagamentoGraduacao(inscricao, "pay_grad_1", LocalDateTime.now().plusDays(1));

        when(fx.permissaoService.usuarioAtual()).thenReturn(usuario);
        when(fx.permissaoService.academiaAtualId()).thenReturn(1L);
        when(fx.inscricaoGraduacaoRepository.findByIdAndAcademiaIdAndAlunoIdForUpdate(55L, 1L, 10L)).thenReturn(Optional.of(inscricao));
        when(fx.pagamentoRepository.findFirstByAcademiaIdAndInscricaoGraduacaoIdAndProvedorAndMetodoAndStatusInAndIdPagamentoExternoIsNotNullOrderByCriadoEmDesc(
                eq(1L), eq(55L), eq("ASAAS"), eq(MetodoPagamento.PIX), any())).thenReturn(Optional.of(pagamento));
        when(fx.provedorPagamento.nome()).thenReturn("ASAAS");

        var response = fx.service.criarPixInscricaoGraduacao(55L);

        assertEquals(88L, response.getPagamentoId());
        verify(fx.provedorPagamento, never()).criarCobrancaPixGraduacao(any());
        verify(fx.alunoRepository, never()).save(any());
    }

    @Test
    void webhookGraduacaoAtualizaInscricaoSemAlterarGraduacao() {
        Fixture fx = fixture();
        InscricaoGraduacao inscricao = inscricaoGraduacao(BigDecimal.valueOf(120));
        Pagamento pagamento = pagamentoGraduacao(inscricao, "pay_grad_1", LocalDateTime.now().plusDays(1));
        Graduacao atual = inscricao.getAluno().getGraduacaoEntidade();
        String payload = "{\"id\":\"evt1\",\"event\":\"PAYMENT_RECEIVED\",\"payment\":{\"id\":\"pay_grad_1\",\"status\":\"RECEIVED\",\"value\":120.00}}";

        when(fx.pagamentoRepository.findByIdPagamentoExterno("pay_grad_1")).thenReturn(Optional.of(pagamento));

        fx.service.processarWebhookAsaas("token-teste", payload);

        assertEquals(StatusPagamento.PAGO, pagamento.getStatus());
        assertEquals(StatusInscricaoGraduacao.PAGA, inscricao.getStatus());
        assertEquals(atual, inscricao.getAluno().getGraduacaoEntidade());
        verify(fx.eventoRepository).save(any(EventoPagamentoExterno.class));
    }

    @Test
    void webhookGraduacaoProcessandoVencidoCanceladoEEstornado() {
        Fixture fx = fixture();
        InscricaoGraduacao inscricao = inscricaoGraduacao(BigDecimal.valueOf(120));
        Pagamento pagamento = pagamentoGraduacao(inscricao, "pay_grad_1", LocalDateTime.now().plusDays(1));
        when(fx.pagamentoRepository.findByIdPagamentoExterno("pay_grad_1")).thenReturn(Optional.of(pagamento));

        fx.service.processarWebhookAsaas("token-teste", "{\"id\":\"evt2\",\"event\":\"PAYMENT_CONFIRMED\",\"payment\":{\"id\":\"pay_grad_1\",\"status\":\"CONFIRMED\",\"value\":120.00}}");
        assertEquals(StatusInscricaoGraduacao.PAGAMENTO_PROCESSANDO, inscricao.getStatus());

        fx.service.processarWebhookAsaas("token-teste", "{\"id\":\"evt3\",\"event\":\"PAYMENT_OVERDUE\",\"payment\":{\"id\":\"pay_grad_1\",\"status\":\"OVERDUE\",\"value\":120.00}}");
        assertEquals(StatusInscricaoGraduacao.PENDENTE_PAGAMENTO, inscricao.getStatus());

        fx.service.processarWebhookAsaas("token-teste", "{\"id\":\"evt4\",\"event\":\"PAYMENT_DELETED\",\"payment\":{\"id\":\"pay_grad_1\",\"status\":\"DELETED\",\"value\":120.00}}");
        assertEquals(StatusInscricaoGraduacao.PENDENTE_PAGAMENTO, inscricao.getStatus());

        fx.service.processarWebhookAsaas("token-teste", "{\"id\":\"evt5\",\"event\":\"PAYMENT_REFUNDED\",\"payment\":{\"id\":\"pay_grad_1\",\"status\":\"REFUNDED\",\"value\":120.00}}");
        assertEquals(StatusInscricaoGraduacao.PENDENTE_PAGAMENTO, inscricao.getStatus());
    }

    @Test
    void conciliacaoGraduacaoAtualizaInscricaoEMensalidadeContinuaFuncionando() {
        Fixture fx = fixture();
        Usuario admin = new Usuario();
        admin.setPerfil(PerfilUsuario.ADMIN);
        InscricaoGraduacao inscricao = inscricaoGraduacao(BigDecimal.valueOf(120));
        Pagamento pagamento = pagamentoGraduacao(inscricao, "pay_grad_1", LocalDateTime.now().plusDays(1));
        when(fx.permissaoService.academiaAtualId()).thenReturn(1L);
        when(fx.pagamentoRepository.findByIdAndAcademiaId(88L, 1L)).thenReturn(Optional.of(pagamento));
        when(fx.provedorPagamento.consultarPagamento("pay_grad_1")).thenReturn(StatusPagamento.PAGO);

        ConciliacaoPagamentoResponse response = fx.service.conciliar(88L);

        assertEquals(TipoCobranca.GRADUACAO, response.getTipoCobranca());
        assertEquals(StatusInscricaoGraduacao.PAGA, response.getStatusInscricaoAtual());

        Fatura fatura = fatura();
        Pagamento mensalidade = pagamentoPix(fatura, "pay_mensalidade", LocalDateTime.now().plusDays(1));
        when(fx.pagamentoRepository.findByIdAndAcademiaId(3L, 1L)).thenReturn(Optional.of(mensalidade));
        when(fx.provedorPagamento.consultarPagamento("pay_mensalidade")).thenReturn(StatusPagamento.PAGO);
        when(fx.pagamentoRepository.somarPagamentosConfirmadosPorFaturaIgnorandoPagamento(1L, 3L)).thenReturn(BigDecimal.ZERO);

        assertEquals(StatusFatura.PAGA, afterConciliarMensalidade(fx, fatura));
    }


    @Test
    void deveRegistrarPagamentoManualEConfirmarFatura() {
        Fixture fx = fixture();
        Usuario usuario = new Usuario();
        usuario.setPerfil(PerfilUsuario.ADMIN);
        Fatura fatura = fatura();

        when(fx.permissaoService.usuarioAtual()).thenReturn(usuario);
        when(fx.faturaService.buscarPorId(1L)).thenReturn(fatura);
        when(fx.pagamentoRepository.somarPagamentosConfirmadosPorFatura(1L)).thenReturn(BigDecimal.ZERO);
        when(fx.pagamentoRepository.save(any(Pagamento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Pagamento pagamento = fx.service.registrarPagamentoManual(1L, request(BigDecimal.valueOf(180)));

        assertEquals(StatusPagamento.PAGO, pagamento.getStatus());
        assertEquals(StatusFatura.PAGA, fatura.getStatus());
        assertEquals(usuario, pagamento.getConfirmadoPor());
        verify(fx.permissaoService).exigirPermissao(PermissaoSistema.PAGAMENTO_CONFIRMAR);
    }

    @Test
    void deveRegistrarPagamentoParcialEManterSaldoPendente() {
        Fixture fx = fixture();
        Fatura fatura = fatura();

        when(fx.faturaService.buscarPorId(1L)).thenReturn(fatura);
        when(fx.permissaoService.usuarioAtual()).thenReturn(new Usuario());
        when(fx.pagamentoRepository.somarPagamentosConfirmadosPorFatura(1L)).thenReturn(BigDecimal.ZERO);
        when(fx.pagamentoRepository.save(any(Pagamento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Pagamento pagamento = fx.service.registrarPagamentoManual(1L, request(BigDecimal.valueOf(80)));

        assertEquals(BigDecimal.valueOf(80), pagamento.getValor());
        assertEquals(StatusFatura.PARCIALMENTE_PAGA, fatura.getStatus());
    }

    @Test
    void naoDevePermitirPagamentoMaiorQueSaldo() {
        Fixture fx = fixture();
        Fatura fatura = fatura();

        when(fx.faturaService.buscarPorId(1L)).thenReturn(fatura);
        when(fx.permissaoService.usuarioAtual()).thenReturn(new Usuario());
        when(fx.pagamentoRepository.somarPagamentosConfirmadosPorFatura(1L)).thenReturn(BigDecimal.valueOf(100));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> fx.service.registrarPagamentoManual(1L, request(BigDecimal.valueOf(90))));

        assertTrue(ex.getMessage().contains("maior que o saldo"));
        verify(fx.pagamentoRepository, never()).save(any());
    }

    @Test
    void naoDevePermitirAlunoConfirmarPagamentoManual() {
        Fixture fx = fixture();
        doThrow(new AccessDeniedException("Usuário sem permissão: PAGAMENTO_CONFIRMAR"))
                .when(fx.permissaoService).exigirPermissao(PermissaoSistema.PAGAMENTO_CONFIRMAR);

        assertThrows(AccessDeniedException.class, () -> fx.service.registrarPagamentoManual(1L, request(BigDecimal.valueOf(180))));
        verify(fx.faturaService, never()).buscarPorId(any());
    }

    @Test
    void naoDevePermitirProfessorComumConfirmarPagamentoManual() {
        Fixture fx = fixture();
        doThrow(new AccessDeniedException("Usuário sem permissão: PAGAMENTO_CONFIRMAR"))
                .when(fx.permissaoService).exigirPermissao(PermissaoSistema.PAGAMENTO_CONFIRMAR);

        assertThrows(AccessDeniedException.class, () -> fx.service.registrarPagamentoManual(1L, request(BigDecimal.valueOf(180))));
        verify(fx.faturaService, never()).buscarPorId(any());
    }

    @Test
    void naoDevePagarFaturaDeOutraAcademia() {
        Fixture fx = fixture();
        when(fx.faturaService.buscarPorId(99L)).thenThrow(new RuntimeException("Fatura não encontrada com ID: 99"));

        assertThrows(RuntimeException.class, () -> fx.service.registrarPagamentoManual(99L, request(BigDecimal.valueOf(180))));
        verify(fx.pagamentoRepository, never()).save(any());
    }

    @Test
    void deveReutilizarClienteAsaasJaPersistido() {
        Fixture fx = fixture();
        Fatura fatura = fatura();
        fatura.getAluno().setAsaasCustomerId("cus_existente");
        when(fx.provedorPagamento.nome()).thenReturn("ASAAS");
        when(fx.faturaService.buscarPorId(1L)).thenReturn(fatura);
        when(fx.pagamentoRepository.somarPagamentosConfirmadosPorFatura(1L)).thenReturn(BigDecimal.ZERO);
        when(fx.pagamentoRepository.findFirstByAcademiaIdAndFaturaIdAndProvedorAndMetodoAndStatusAndIdPagamentoExternoIsNotNullOrderByCriadoEmDesc(anyLong(), anyLong(), anyString(), any(), any()))
                .thenReturn(Optional.empty());
        when(fx.pagamentoRepository.save(any(Pagamento.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(fx.provedorPagamento.criarCobrancaPix(fatura)).thenReturn(cobranca("pay_1", LocalDateTime.now().plusHours(2)));

        fx.service.criarCobrancaPix(1L);

        verify(fx.provedorPagamento, never()).criarCliente(any());
        verify(fx.alunoRepository, never()).save(any());
    }

    @Test
    void deveCriarClienteSomenteQuandoNecessario() {
        Fixture fx = fixture();
        Fatura fatura = fatura();
        fatura.getAluno().setAsaasCustomerId(null);
        when(fx.provedorPagamento.nome()).thenReturn("ASAAS");
        when(fx.provedorPagamento.criarCliente(fatura.getAluno())).thenReturn("cus_novo");
        when(fx.faturaService.buscarPorId(1L)).thenReturn(fatura);
        when(fx.pagamentoRepository.somarPagamentosConfirmadosPorFatura(1L)).thenReturn(BigDecimal.ZERO);
        when(fx.pagamentoRepository.findFirstByAcademiaIdAndFaturaIdAndProvedorAndMetodoAndStatusAndIdPagamentoExternoIsNotNullOrderByCriadoEmDesc(anyLong(), anyLong(), anyString(), any(), any()))
                .thenReturn(Optional.empty());
        when(fx.pagamentoRepository.save(any(Pagamento.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(fx.provedorPagamento.criarCobrancaPix(fatura)).thenReturn(cobranca("pay_1", LocalDateTime.now().plusHours(2)));

        fx.service.criarCobrancaPix(1L);

        assertEquals("cus_novo", fatura.getAluno().getAsaasCustomerId());
        verify(fx.alunoRepository).save(fatura.getAluno());
    }

    @Test
    void deveReutilizarPixPendenteValido() {
        Fixture fx = fixture();
        Fatura fatura = fatura();
        Pagamento existente = pagamentoPix(fatura, "pay_existente", LocalDateTime.now().plusHours(1));
        existente.setId(15L);

        when(fx.provedorPagamento.nome()).thenReturn("ASAAS");
        when(fx.faturaService.buscarPorId(1L)).thenReturn(fatura);
        when(fx.pagamentoRepository.somarPagamentosConfirmadosPorFatura(1L)).thenReturn(BigDecimal.ZERO);
        when(fx.pagamentoRepository.findFirstByAcademiaIdAndFaturaIdAndProvedorAndMetodoAndStatusAndIdPagamentoExternoIsNotNullOrderByCriadoEmDesc(1L, 1L, "ASAAS", MetodoPagamento.PIX, StatusPagamento.PENDENTE))
                .thenReturn(Optional.of(existente));

        CobrancaPixResponse response = fx.service.criarCobrancaPix(1L);

        assertEquals(15L, response.getPagamentoId());
        assertEquals("pay_existente", response.getIdPagamentoExterno());
        verify(fx.provedorPagamento, never()).criarCobrancaPix(any());
        verify(fx.pagamentoRepository, never()).save(any());
    }

    @Test
    void deveCriarNovaCobrancaQuandoPixExpirou() {
        Fixture fx = fixture();
        Fatura fatura = fatura();
        Pagamento expirado = pagamentoPix(fatura, "pay_expirado", LocalDateTime.now().minusMinutes(1));

        when(fx.provedorPagamento.nome()).thenReturn("ASAAS");
        when(fx.faturaService.buscarPorId(1L)).thenReturn(fatura);
        when(fx.pagamentoRepository.somarPagamentosConfirmadosPorFatura(1L)).thenReturn(BigDecimal.ZERO);
        when(fx.pagamentoRepository.findFirstByAcademiaIdAndFaturaIdAndProvedorAndMetodoAndStatusAndIdPagamentoExternoIsNotNullOrderByCriadoEmDesc(1L, 1L, "ASAAS", MetodoPagamento.PIX, StatusPagamento.PENDENTE))
                .thenReturn(Optional.of(expirado));
        when(fx.pagamentoRepository.save(any(Pagamento.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(fx.provedorPagamento.criarCobrancaPix(fatura)).thenReturn(cobranca("pay_novo", LocalDateTime.now().plusHours(1)));

        CobrancaPixResponse response = fx.service.criarCobrancaPix(1L);

        assertEquals("pay_novo", response.getIdPagamentoExterno());
        verify(fx.provedorPagamento).criarCobrancaPix(fatura);
    }

    @Test
    void duasSolicitacoesSimultaneasNaoDevemCriarDoisPix() throws Exception {
        Fixture fx = fixture();
        Fatura fatura = fatura();
        AtomicReference<Pagamento> salvo = new AtomicReference<>();
        AtomicInteger chamadasProvider = new AtomicInteger();

        when(fx.provedorPagamento.nome()).thenReturn("ASAAS");
        when(fx.faturaService.buscarPorId(1L)).thenReturn(fatura);
        when(fx.pagamentoRepository.somarPagamentosConfirmadosPorFatura(1L)).thenReturn(BigDecimal.ZERO);
        when(fx.pagamentoRepository.findFirstByAcademiaIdAndFaturaIdAndProvedorAndMetodoAndStatusAndIdPagamentoExternoIsNotNullOrderByCriadoEmDesc(anyLong(), anyLong(), anyString(), any(), any()))
                .thenAnswer(invocation -> Optional.ofNullable(salvo.get()).filter(p -> p.getIdPagamentoExterno() != null));
        when(fx.pagamentoRepository.save(any(Pagamento.class))).thenAnswer(invocation -> {
            Pagamento pagamento = invocation.getArgument(0);
            if (pagamento.getId() == null) {
                pagamento.setId(77L);
            }
            salvo.set(pagamento);
            return pagamento;
        });
        when(fx.provedorPagamento.criarCobrancaPix(fatura)).thenAnswer(invocation -> {
            chamadasProvider.incrementAndGet();
            Thread.sleep(100);
            return cobranca("pay_unico", LocalDateTime.now().plusHours(1));
        });

        CountDownLatch start = new CountDownLatch(1);
        var executor = Executors.newFixedThreadPool(2);
        var first = executor.submit(() -> {
            start.await();
            return fx.service.criarCobrancaPix(1L);
        });
        var second = executor.submit(() -> {
            start.await();
            return fx.service.criarCobrancaPix(1L);
        });
        start.countDown();

        CobrancaPixResponse r1 = first.get(2, TimeUnit.SECONDS);
        CobrancaPixResponse r2 = second.get(2, TimeUnit.SECONDS);
        executor.shutdownNow();

        assertEquals("pay_unico", r1.getIdPagamentoExterno());
        assertEquals("pay_unico", r2.getIdPagamentoExterno());
        assertEquals(1, chamadasProvider.get());
    }

    @Test
    void webhookDeveRejeitarTokenAusenteOuInvalido() {
        Fixture fx = fixture();

        assertThrows(AccessDeniedException.class, () -> fx.service.processarWebhookAsaas(null, "{}"));
        assertThrows(AccessDeniedException.class, () -> fx.service.processarWebhookAsaas("errado", "{}"));
    }

    @Test
    void deveIgnorarWebhookAsaasDuplicado() {
        Fixture fx = fixture();
        when(fx.eventoRepository.existsByProvedorAndEventoExternoIdAndTipoEvento("ASAAS", "evt_1", "PAYMENT_RECEIVED"))
                .thenReturn(true);

        fx.service.processarWebhookAsaas("token-teste", "{\"id\":\"evt_1\",\"event\":\"PAYMENT_RECEIVED\",\"payment\":{\"id\":\"pay_1\",\"status\":\"RECEIVED\"}}");

        verify(fx.pagamentoRepository, never()).findByIdPagamentoExterno(any());
        verify(fx.pagamentoRepository, never()).save(any());
    }

    @Test
    void webhookEventoDesconhecidoNaoAlteraFinanceiro() {
        Fixture fx = fixture();
        Pagamento pagamento = pagamentoPix(fatura(), "pay_1", LocalDateTime.now().plusHours(1));
        pagamento.setStatus(StatusPagamento.PENDENTE);
        when(fx.pagamentoRepository.findByIdPagamentoExterno("pay_1")).thenReturn(Optional.of(pagamento));
        when(fx.pagamentoRepository.save(any(Pagamento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        fx.service.processarWebhookAsaas("token-teste", "{\"id\":\"evt_x\",\"event\":\"PAYMENT_UNKNOWN\",\"payment\":{\"id\":\"pay_1\",\"status\":\"UNKNOWN\"}}");

        assertEquals(StatusPagamento.PENDENTE, pagamento.getStatus());
        assertEquals(StatusFatura.PENDENTE, pagamento.getFatura().getStatus());
        verify(fx.eventoRepository).save(any(EventoPagamentoExterno.class));
    }

    @Test
    void webhookDeveConfirmarPagamento() {
        Fixture fx = fixture();
        Pagamento pagamento = pagamentoPix(fatura(), "pay_1", LocalDateTime.now().plusHours(1));
        pagamento.setId(9L);
        when(fx.pagamentoRepository.findByIdPagamentoExterno("pay_1")).thenReturn(Optional.of(pagamento));
        when(fx.pagamentoRepository.somarPagamentosConfirmadosPorFaturaIgnorandoPagamento(1L, 9L)).thenReturn(BigDecimal.ZERO);

        fx.service.processarWebhookAsaas("token-teste", "{\"id\":\"evt_1\",\"event\":\"PAYMENT_RECEIVED\",\"payment\":{\"id\":\"pay_1\",\"status\":\"RECEIVED\",\"value\":180.00}}");

        assertEquals(StatusPagamento.PAGO, pagamento.getStatus());
        assertEquals(StatusFatura.PAGA, pagamento.getFatura().getStatus());
    }

    @Test
    void webhookDeveTratarVencimentoCancelamentoEEstorno() {
        Fixture fx = fixture();
        Pagamento pagamento = pagamentoPix(fatura(), "pay_1", LocalDateTime.now().plusHours(1));
        when(fx.pagamentoRepository.findByIdPagamentoExterno("pay_1")).thenReturn(Optional.of(pagamento));

        fx.service.processarWebhookAsaas("token-teste", "{\"id\":\"evt_1\",\"event\":\"PAYMENT_OVERDUE\",\"payment\":{\"id\":\"pay_1\",\"status\":\"OVERDUE\"}}");
        assertEquals(StatusFatura.VENCIDA, pagamento.getFatura().getStatus());

        fx.service.processarWebhookAsaas("token-teste", "{\"id\":\"evt_2\",\"event\":\"PAYMENT_DELETED\",\"payment\":{\"id\":\"pay_1\",\"status\":\"DELETED\"}}");
        assertEquals(StatusFatura.CANCELADA, pagamento.getFatura().getStatus());

        fx.service.processarWebhookAsaas("token-teste", "{\"id\":\"evt_3\",\"event\":\"PAYMENT_REFUNDED\",\"payment\":{\"id\":\"pay_1\",\"status\":\"REFUNDED\"}}");
        assertEquals(StatusFatura.PENDENTE, pagamento.getFatura().getStatus());
    }

    @Test
    void conciliarDeveAtualizarStatus() {
        Fixture fx = fixture();
        Pagamento pagamento = pagamentoPix(fatura(), "pay_1", LocalDateTime.now().plusHours(1));
        pagamento.setId(3L);
        when(fx.permissaoService.academiaAtualId()).thenReturn(1L);
        when(fx.pagamentoRepository.findByIdAndAcademiaId(3L, 1L)).thenReturn(Optional.of(pagamento));
        when(fx.provedorPagamento.consultarPagamento("pay_1")).thenReturn(StatusPagamento.PAGO);
        when(fx.pagamentoRepository.somarPagamentosConfirmadosPorFaturaIgnorandoPagamento(1L, 3L)).thenReturn(BigDecimal.ZERO);

        ConciliacaoPagamentoResponse response = fx.service.conciliar(3L);

        assertEquals(StatusPagamento.PAGO, response.getStatusPagamentoAtual());
        assertEquals(StatusFatura.PAGA, response.getStatusFaturaAtual());
        assertNotNull(response.getConciliadoEm());
        verify(fx.permissaoService).exigirAdmin();
    }

    @Test
    void naoDeveConciliarPagamentoDeOutraAcademia() {
        Fixture fx = fixture();
        when(fx.permissaoService.academiaAtualId()).thenReturn(1L);
        when(fx.pagamentoRepository.findByIdAndAcademiaId(3L, 1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> fx.service.conciliar(3L));
        verify(fx.provedorPagamento, never()).consultarPagamento(anyString());
    }

    @Test
    void falhaTemporariaDaApiExternaNaoDeveAlterarConciliacao() {
        Fixture fx = fixture();
        Pagamento pagamento = pagamentoPix(fatura(), "pay_1", LocalDateTime.now().plusHours(1));
        when(fx.permissaoService.academiaAtualId()).thenReturn(1L);
        when(fx.pagamentoRepository.findByIdAndAcademiaId(3L, 1L)).thenReturn(Optional.of(pagamento));
        when(fx.provedorPagamento.consultarPagamento("pay_1")).thenThrow(new RuntimeException("Erro de comunicação com Asaas Sandbox"));

        assertThrows(RuntimeException.class, () -> fx.service.conciliar(3L));
        assertNull(pagamento.getConciliadoEm());
    }

    private PagamentoManualRequest request(BigDecimal valor) {
        PagamentoManualRequest request = new PagamentoManualRequest();
        request.setMetodo(MetodoPagamento.PIX_MANUAL);
        request.setValor(valor);
        request.setDataPagamento(LocalDateTime.now());
        request.setObservacao("Pagamento confirmado na recepção");
        return request;
    }

    private CobrancaPixResponse cobranca(String externo, LocalDateTime expiracao) {
        return new CobrancaPixResponse(null, 1L, StatusPagamento.PENDENTE, BigDecimal.valueOf(180),
                externo, "pix-copia-cola", null, "base64", expiracao);
    }

    private Pagamento pagamentoPix(Fatura fatura, String externo, LocalDateTime expiracao) {
        Pagamento pagamento = new Pagamento();
        pagamento.setId(3L);
        pagamento.setFatura(fatura);
        pagamento.setTipoCobranca(TipoCobranca.MENSALIDADE);
        pagamento.setAcademia(fatura.getAcademia());
        pagamento.setValor(fatura.getValor());
        pagamento.setMetodo(MetodoPagamento.PIX);
        pagamento.setStatus(StatusPagamento.PENDENTE);
        pagamento.setProvedor("ASAAS");
        pagamento.setIdPagamentoExterno(externo);
        pagamento.setPixCopiaCola("pix-copia-cola");
        pagamento.setPixQrCodeBase64("base64");
        pagamento.setDataExpiracao(expiracao);
        pagamento.setCriadoEm(LocalDateTime.now());
        return pagamento;
    }

    private StatusFatura afterConciliarMensalidade(Fixture fx, Fatura fatura) {
        fx.service.conciliar(3L);
        return fatura.getStatus();
    }

    private InscricaoGraduacao inscricaoGraduacao(BigDecimal valor) {
        Academia academia = new Academia();
        academia.setId(1L);
        Graduacao atual = new Graduacao();
        atual.setId(1L);
        atual.setNome("Branco");
        atual.setCorHex("#FFFFFF");
        atual.setOrdem(1);
        atual.setAcademia(academia);
        Graduacao destino = new Graduacao();
        destino.setId(2L);
        destino.setNome("Azul");
        destino.setCorHex("#0000FF");
        destino.setOrdem(2);
        destino.setAcademia(academia);
        Aluno aluno = new Aluno();
        aluno.setId(10L);
        aluno.setNome("Aluno Teste");
        aluno.setEmail("aluno@email.com");
        aluno.setAcademia(academia);
        aluno.setAsaasCustomerId("cus_123");
        aluno.setGraduacaoEntidade(atual);
        Usuario usuario = new Usuario();
        usuario.setId(5L);
        usuario.setNome("Professor");
        usuario.setEmail("prof@email.com");
        usuario.setPerfil(PerfilUsuario.PROFESSOR);
        usuario.setAcademia(academia);
        EventoGraduacao evento = new EventoGraduacao();
        evento.setId(12L);
        evento.setAcademia(academia);
        evento.setTitulo("Graduação Elite");
        evento.setStatus(StatusEventoGraduacao.PUBLICADO);
        evento.setFimInscricoes(LocalDateTime.now().plusDays(5));
        evento.setCriadoPor(usuario);
        evento.setCriadoEm(LocalDateTime.now());
        evento.setAtualizadoEm(LocalDateTime.now());
        InscricaoGraduacao inscricao = new InscricaoGraduacao();
        inscricao.setId(55L);
        inscricao.setAcademia(academia);
        inscricao.setEvento(evento);
        inscricao.setAluno(aluno);
        inscricao.setGraduacaoDestino(destino);
        inscricao.setValor(valor);
        inscricao.setStatus(valor.compareTo(BigDecimal.ZERO) == 0 ? StatusInscricaoGraduacao.CONFIRMADA : StatusInscricaoGraduacao.PENDENTE_PAGAMENTO);
        inscricao.setInscritoEm(LocalDateTime.now());
        return inscricao;
    }

    private Usuario usuarioAluno(Aluno aluno) {
        Usuario usuario = new Usuario();
        usuario.setId(44L);
        usuario.setNome(aluno.getNome());
        usuario.setEmail(aluno.getEmail());
        usuario.setPerfil(PerfilUsuario.ALUNO);
        usuario.setAcademia(aluno.getAcademia());
        usuario.setAluno(aluno);
        return usuario;
    }

    private Pagamento pagamentoGraduacao(InscricaoGraduacao inscricao, String externo, LocalDateTime expiracao) {
        Pagamento pagamento = new Pagamento();
        pagamento.setId(88L);
        pagamento.setTipoCobranca(TipoCobranca.GRADUACAO);
        pagamento.setInscricaoGraduacao(inscricao);
        pagamento.setAcademia(inscricao.getAcademia());
        pagamento.setValor(inscricao.getValor());
        pagamento.setMetodo(MetodoPagamento.PIX);
        pagamento.setStatus(StatusPagamento.PENDENTE);
        pagamento.setProvedor("ASAAS");
        pagamento.setIdPagamentoExterno(externo);
        pagamento.setPixCopiaCola("pix-copia-cola");
        pagamento.setPixQrCodeBase64("base64");
        pagamento.setDataExpiracao(expiracao);
        pagamento.setCriadoEm(LocalDateTime.now());
        inscricao.setPagamento(pagamento);
        return pagamento;
    }

    private Fatura fatura() {
        Academia academia = new Academia();
        academia.setId(1L);
        Aluno aluno = new Aluno();
        aluno.setId(10L);
        aluno.setNome("Aluno Teste");
        aluno.setEmail("aluno@email.com");
        aluno.setAcademia(academia);
        aluno.setAsaasCustomerId("cus_123");

        Fatura fatura = new Fatura();
        fatura.setId(1L);
        fatura.setAluno(aluno);
        fatura.setAcademia(academia);
        fatura.setValor(BigDecimal.valueOf(180));
        fatura.setStatus(StatusFatura.PENDENTE);
        return fatura;
    }

    private Fixture fixture() {
        PagamentoRepository pagamentoRepository = mock(PagamentoRepository.class);
        EventoPagamentoExternoRepository eventoRepository = mock(EventoPagamentoExternoRepository.class);
        AlunoRepository alunoRepository = mock(AlunoRepository.class);
        InscricaoGraduacaoRepository inscricaoGraduacaoRepository = mock(InscricaoGraduacaoRepository.class);
        FaturaService faturaService = mock(FaturaService.class);
        ProvedorPagamento provedorPagamento = mock(ProvedorPagamento.class);
        PermissaoService permissaoService = mock(PermissaoService.class);
        NotificacaoService notificacaoService = mock(NotificacaoService.class);
        NotificacaoFinanceiraAutomacaoService notificacaoFinanceiraAutomacaoService = mock(NotificacaoFinanceiraAutomacaoService.class);
        PagamentoService service = new PagamentoService(pagamentoRepository, eventoRepository, alunoRepository, inscricaoGraduacaoRepository, faturaService,
                provedorPagamento, permissaoService, notificacaoService, notificacaoFinanceiraAutomacaoService, new ObjectMapper(), asaasProperties());
        return new Fixture(pagamentoRepository, eventoRepository, alunoRepository, inscricaoGraduacaoRepository, faturaService, provedorPagamento,
                permissaoService, notificacaoService, notificacaoFinanceiraAutomacaoService, service);
    }

    private AsaasProperties asaasProperties() {
        return new AsaasProperties("https://api-sandbox.asaas.com/v3", "api-key", "token-teste");
    }

    private record Fixture(PagamentoRepository pagamentoRepository,
                           EventoPagamentoExternoRepository eventoRepository,
                           AlunoRepository alunoRepository,
                           InscricaoGraduacaoRepository inscricaoGraduacaoRepository,
                           FaturaService faturaService,
                           ProvedorPagamento provedorPagamento,
                           PermissaoService permissaoService,
                           NotificacaoService notificacaoService,
                           NotificacaoFinanceiraAutomacaoService notificacaoFinanceiraAutomacaoService,
                           PagamentoService service) {
    }
}
