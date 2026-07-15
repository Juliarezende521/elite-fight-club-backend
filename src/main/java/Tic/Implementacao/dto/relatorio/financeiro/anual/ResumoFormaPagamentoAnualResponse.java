package Tic.Implementacao.dto.relatorio.financeiro.anual;

import java.math.BigDecimal;

public class ResumoFormaPagamentoAnualResponse {
    private String metodoPagamento;
    private Integer quantidadePagamentos;
    private BigDecimal valorBruto;
    private BigDecimal taxas;
    private BigDecimal valorLiquido;
    private BigDecimal percentualDoRecebido;

    public ResumoFormaPagamentoAnualResponse(String metodoPagamento, Integer quantidadePagamentos,
                                             BigDecimal valorBruto, BigDecimal taxas, BigDecimal valorLiquido,
                                             BigDecimal percentualDoRecebido) {
        this.metodoPagamento = metodoPagamento;
        this.quantidadePagamentos = quantidadePagamentos;
        this.valorBruto = valorBruto;
        this.taxas = taxas;
        this.valorLiquido = valorLiquido;
        this.percentualDoRecebido = percentualDoRecebido;
    }

    public String getMetodoPagamento() { return metodoPagamento; }
    public Integer getQuantidadePagamentos() { return quantidadePagamentos; }
    public BigDecimal getValorBruto() { return valorBruto; }
    public BigDecimal getTaxas() { return taxas; }
    public BigDecimal getValorLiquido() { return valorLiquido; }
    public BigDecimal getPercentualDoRecebido() { return percentualDoRecebido; }
}
