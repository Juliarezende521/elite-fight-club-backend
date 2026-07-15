package Tic.Implementacao.dto.relatorio.financeiro.contabil;

import java.math.BigDecimal;

public class ResumoRelatorioContabilResponse {
    private BigDecimal totalRecebidoBruto;
    private BigDecimal totalTaxas;
    private BigDecimal totalEstornado;
    private BigDecimal totalLiquido;
    private BigDecimal totalMensalidades;
    private BigDecimal totalGraduacoes;
    private BigDecimal totalOutrasReceitas;
    private Integer quantidadePagamentos;
    private Integer quantidadeAlunosPagadores;
    private Integer quantidadeEstornos;

    public ResumoRelatorioContabilResponse(BigDecimal totalRecebidoBruto, BigDecimal totalTaxas,
                                           BigDecimal totalEstornado, BigDecimal totalLiquido,
                                           BigDecimal totalMensalidades, BigDecimal totalGraduacoes,
                                           BigDecimal totalOutrasReceitas, Integer quantidadePagamentos,
                                           Integer quantidadeAlunosPagadores, Integer quantidadeEstornos) {
        this.totalRecebidoBruto = totalRecebidoBruto;
        this.totalTaxas = totalTaxas;
        this.totalEstornado = totalEstornado;
        this.totalLiquido = totalLiquido;
        this.totalMensalidades = totalMensalidades;
        this.totalGraduacoes = totalGraduacoes;
        this.totalOutrasReceitas = totalOutrasReceitas;
        this.quantidadePagamentos = quantidadePagamentos;
        this.quantidadeAlunosPagadores = quantidadeAlunosPagadores;
        this.quantidadeEstornos = quantidadeEstornos;
    }

    public BigDecimal getTotalRecebidoBruto() { return totalRecebidoBruto; }
    public BigDecimal getTotalTaxas() { return totalTaxas; }
    public BigDecimal getTotalEstornado() { return totalEstornado; }
    public BigDecimal getTotalLiquido() { return totalLiquido; }
    public BigDecimal getTotalMensalidades() { return totalMensalidades; }
    public BigDecimal getTotalGraduacoes() { return totalGraduacoes; }
    public BigDecimal getTotalOutrasReceitas() { return totalOutrasReceitas; }
    public Integer getQuantidadePagamentos() { return quantidadePagamentos; }
    public Integer getQuantidadeAlunosPagadores() { return quantidadeAlunosPagadores; }
    public Integer getQuantidadeEstornos() { return quantidadeEstornos; }
}
