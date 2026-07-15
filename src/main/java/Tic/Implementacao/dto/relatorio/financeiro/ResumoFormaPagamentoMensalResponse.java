package Tic.Implementacao.dto.relatorio.financeiro;

import java.math.BigDecimal;

public class ResumoFormaPagamentoMensalResponse {
    private String metodoPagamento;
    private Integer quantidadePagamentos;
    private BigDecimal valorBruto;
    private BigDecimal taxas;
    private BigDecimal valorLiquido;

    public ResumoFormaPagamentoMensalResponse(String metodoPagamento, Integer quantidadePagamentos,
                                              BigDecimal valorBruto, BigDecimal taxas, BigDecimal valorLiquido) {
        this.metodoPagamento = metodoPagamento;
        this.quantidadePagamentos = quantidadePagamentos;
        this.valorBruto = valorBruto;
        this.taxas = taxas;
        this.valorLiquido = valorLiquido;
    }

    public String getMetodoPagamento() { return metodoPagamento; }
    public Integer getQuantidadePagamentos() { return quantidadePagamentos; }
    public BigDecimal getValorBruto() { return valorBruto; }
    public BigDecimal getTaxas() { return taxas; }
    public BigDecimal getValorLiquido() { return valorLiquido; }
}
