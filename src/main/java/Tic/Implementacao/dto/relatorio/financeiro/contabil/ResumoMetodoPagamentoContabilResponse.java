package Tic.Implementacao.dto.relatorio.financeiro.contabil;

import java.math.BigDecimal;

public class ResumoMetodoPagamentoContabilResponse {
    private String metodoPagamento;
    private Integer quantidadePagamentos;
    private BigDecimal totalBruto;
    private BigDecimal totalTaxas;
    private BigDecimal totalLiquido;

    public ResumoMetodoPagamentoContabilResponse(String metodoPagamento, Integer quantidadePagamentos,
                                                 BigDecimal totalBruto, BigDecimal totalTaxas,
                                                 BigDecimal totalLiquido) {
        this.metodoPagamento = metodoPagamento;
        this.quantidadePagamentos = quantidadePagamentos;
        this.totalBruto = totalBruto;
        this.totalTaxas = totalTaxas;
        this.totalLiquido = totalLiquido;
    }

    public String getMetodoPagamento() { return metodoPagamento; }
    public Integer getQuantidadePagamentos() { return quantidadePagamentos; }
    public BigDecimal getTotalBruto() { return totalBruto; }
    public BigDecimal getTotalTaxas() { return totalTaxas; }
    public BigDecimal getTotalLiquido() { return totalLiquido; }
}
