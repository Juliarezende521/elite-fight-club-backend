package Tic.Implementacao.dto.relatorio.financeiro.contabil;

import Tic.Implementacao.model.TipoCobranca;

import java.math.BigDecimal;

public class ResumoNaturezaReceitaResponse {
    private TipoCobranca naturezaReceita;
    private Integer quantidadePagamentos;
    private BigDecimal totalBruto;
    private BigDecimal totalEstornado;
    private BigDecimal totalLiquido;

    public ResumoNaturezaReceitaResponse(TipoCobranca naturezaReceita, Integer quantidadePagamentos,
                                         BigDecimal totalBruto, BigDecimal totalEstornado,
                                         BigDecimal totalLiquido) {
        this.naturezaReceita = naturezaReceita;
        this.quantidadePagamentos = quantidadePagamentos;
        this.totalBruto = totalBruto;
        this.totalEstornado = totalEstornado;
        this.totalLiquido = totalLiquido;
    }

    public TipoCobranca getNaturezaReceita() { return naturezaReceita; }
    public Integer getQuantidadePagamentos() { return quantidadePagamentos; }
    public BigDecimal getTotalBruto() { return totalBruto; }
    public BigDecimal getTotalEstornado() { return totalEstornado; }
    public BigDecimal getTotalLiquido() { return totalLiquido; }
}
