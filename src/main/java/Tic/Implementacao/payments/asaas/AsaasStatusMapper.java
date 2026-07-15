package Tic.Implementacao.payments.asaas;

import Tic.Implementacao.model.StatusPagamento;

import java.util.Set;

public final class AsaasStatusMapper {

    private static final Set<String> STATUS_PAGO = Set.of("RECEIVED", "RECEIVED_IN_CASH");
    private static final Set<String> STATUS_ANALISE = Set.of("CONFIRMED");
    private static final Set<String> STATUS_CANCELADO = Set.of("DELETED", "CANCELLED");
    private static final Set<String> STATUS_ESTORNADO = Set.of("REFUNDED", "REFUND_REQUESTED", "REFUND_IN_PROGRESS", "CHARGEBACK_REQUESTED", "CHARGEBACK_DISPUTE", "AWAITING_CHARGEBACK_REVERSAL");

    private AsaasStatusMapper() {
    }

    public static StatusPagamento paraStatusInterno(String statusAsaas) {
        if (statusAsaas == null || statusAsaas.isBlank()) {
            return StatusPagamento.PENDENTE;
        }
        if (STATUS_PAGO.contains(statusAsaas)) {
            return StatusPagamento.PAGO;
        }
        if (STATUS_ANALISE.contains(statusAsaas)) {
            return StatusPagamento.PROCESSANDO;
        }
        if ("OVERDUE".equals(statusAsaas)) {
            return StatusPagamento.FALHOU;
        }
        if (STATUS_CANCELADO.contains(statusAsaas)) {
            return StatusPagamento.CANCELADO;
        }
        if (STATUS_ESTORNADO.contains(statusAsaas)) {
            return StatusPagamento.ESTORNADO;
        }
        return StatusPagamento.PENDENTE;
    }

    public static boolean eventoConfirmaPagamento(String evento) {
        return "PAYMENT_RECEIVED".equals(evento);
    }

    public static boolean eventoPagamentoEmAnalise(String evento) {
        return "PAYMENT_CONFIRMED".equals(evento);
    }

    public static boolean eventoVencimento(String evento) {
        return "PAYMENT_OVERDUE".equals(evento);
    }

    public static boolean eventoCancelamento(String evento) {
        return "PAYMENT_DELETED".equals(evento);
    }

    public static boolean eventoEstorno(String evento) {
        return "PAYMENT_REFUNDED".equals(evento)
                || "PAYMENT_REFUND_REQUESTED".equals(evento)
                || "PAYMENT_CHARGEBACK_REQUESTED".equals(evento);
    }
}
