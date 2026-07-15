package Tic.Implementacao.dto.relatorio.financeiro;

import java.math.BigDecimal;

public class ResumoFinanceiroMensalResponse {
    private BigDecimal totalPrevisto;
    private BigDecimal totalRecebido;
    private BigDecimal totalPendente;
    private BigDecimal totalVencido;
    private BigDecimal totalParcialmentePago;
    private BigDecimal totalCancelado;
    private BigDecimal totalEstornado;
    private BigDecimal totalTaxas;
    private BigDecimal receitaLiquida;
    private Integer quantidadeFaturas;
    private Integer faturasPagas;
    private Integer faturasPendentes;
    private Integer faturasVencidas;
    private Integer faturasParcialmentePagas;
    private Integer faturasCanceladas;
    private Integer alunosAdimplentes;
    private Integer alunosInadimplentes;
    private Integer alunosSemFatura;

    public ResumoFinanceiroMensalResponse(BigDecimal totalPrevisto, BigDecimal totalRecebido, BigDecimal totalPendente,
                                          BigDecimal totalVencido, BigDecimal totalParcialmentePago,
                                          BigDecimal totalCancelado, BigDecimal totalEstornado, BigDecimal totalTaxas,
                                          BigDecimal receitaLiquida, Integer quantidadeFaturas, Integer faturasPagas,
                                          Integer faturasPendentes, Integer faturasVencidas,
                                          Integer faturasParcialmentePagas, Integer faturasCanceladas,
                                          Integer alunosAdimplentes, Integer alunosInadimplentes,
                                          Integer alunosSemFatura) {
        this.totalPrevisto = totalPrevisto;
        this.totalRecebido = totalRecebido;
        this.totalPendente = totalPendente;
        this.totalVencido = totalVencido;
        this.totalParcialmentePago = totalParcialmentePago;
        this.totalCancelado = totalCancelado;
        this.totalEstornado = totalEstornado;
        this.totalTaxas = totalTaxas;
        this.receitaLiquida = receitaLiquida;
        this.quantidadeFaturas = quantidadeFaturas;
        this.faturasPagas = faturasPagas;
        this.faturasPendentes = faturasPendentes;
        this.faturasVencidas = faturasVencidas;
        this.faturasParcialmentePagas = faturasParcialmentePagas;
        this.faturasCanceladas = faturasCanceladas;
        this.alunosAdimplentes = alunosAdimplentes;
        this.alunosInadimplentes = alunosInadimplentes;
        this.alunosSemFatura = alunosSemFatura;
    }

    public BigDecimal getTotalPrevisto() { return totalPrevisto; }
    public BigDecimal getTotalRecebido() { return totalRecebido; }
    public BigDecimal getTotalPendente() { return totalPendente; }
    public BigDecimal getTotalVencido() { return totalVencido; }
    public BigDecimal getTotalParcialmentePago() { return totalParcialmentePago; }
    public BigDecimal getTotalCancelado() { return totalCancelado; }
    public BigDecimal getTotalEstornado() { return totalEstornado; }
    public BigDecimal getTotalTaxas() { return totalTaxas; }
    public BigDecimal getReceitaLiquida() { return receitaLiquida; }
    public Integer getQuantidadeFaturas() { return quantidadeFaturas; }
    public Integer getFaturasPagas() { return faturasPagas; }
    public Integer getFaturasPendentes() { return faturasPendentes; }
    public Integer getFaturasVencidas() { return faturasVencidas; }
    public Integer getFaturasParcialmentePagas() { return faturasParcialmentePagas; }
    public Integer getFaturasCanceladas() { return faturasCanceladas; }
    public Integer getAlunosAdimplentes() { return alunosAdimplentes; }
    public Integer getAlunosInadimplentes() { return alunosInadimplentes; }
    public Integer getAlunosSemFatura() { return alunosSemFatura; }
}
