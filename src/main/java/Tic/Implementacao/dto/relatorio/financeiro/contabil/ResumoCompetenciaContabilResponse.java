package Tic.Implementacao.dto.relatorio.financeiro.contabil;

import java.math.BigDecimal;

public class ResumoCompetenciaContabilResponse {
    private Integer mes;
    private String competencia;
    private BigDecimal totalMensalidades;
    private BigDecimal totalGraduacoes;
    private BigDecimal totalBruto;
    private BigDecimal totalEstornado;
    private BigDecimal totalLiquido;
    private Integer quantidadePagamentos;

    public ResumoCompetenciaContabilResponse(Integer mes, String competencia, BigDecimal totalMensalidades,
                                             BigDecimal totalGraduacoes, BigDecimal totalBruto,
                                             BigDecimal totalEstornado, BigDecimal totalLiquido,
                                             Integer quantidadePagamentos) {
        this.mes = mes;
        this.competencia = competencia;
        this.totalMensalidades = totalMensalidades;
        this.totalGraduacoes = totalGraduacoes;
        this.totalBruto = totalBruto;
        this.totalEstornado = totalEstornado;
        this.totalLiquido = totalLiquido;
        this.quantidadePagamentos = quantidadePagamentos;
    }

    public Integer getMes() { return mes; }
    public String getCompetencia() { return competencia; }
    public BigDecimal getTotalMensalidades() { return totalMensalidades; }
    public BigDecimal getTotalGraduacoes() { return totalGraduacoes; }
    public BigDecimal getTotalBruto() { return totalBruto; }
    public BigDecimal getTotalEstornado() { return totalEstornado; }
    public BigDecimal getTotalLiquido() { return totalLiquido; }
    public Integer getQuantidadePagamentos() { return quantidadePagamentos; }
}
