package Tic.Implementacao.dto.relatorio.financeiro.anual;

import java.math.BigDecimal;

public class ResumoFinanceiroMesResponse {
    private Integer mes;
    private String nomeMes;
    private String competencia;
    private BigDecimal totalPrevisto;
    private BigDecimal totalRecebido;
    private BigDecimal totalPendente;
    private BigDecimal totalVencido;
    private BigDecimal totalEstornado;
    private BigDecimal receitaLiquida;
    private Integer faturasPagas;
    private Integer faturasPendentes;
    private Integer faturasVencidas;
    private Integer alunosInadimplentes;
    private BigDecimal taxaRecebimento;
    private BigDecimal taxaInadimplencia;
    private BigDecimal variacaoReceitaPercentual;
    private BigDecimal variacaoPrevistoPercentual;
    private BigDecimal variacaoInadimplenciaPercentual;
    private BigDecimal recebidoAcumulado;
    private BigDecimal previstoAcumulado;
    private BigDecimal vencidoAcumulado;

    public ResumoFinanceiroMesResponse(Integer mes, String nomeMes, String competencia, BigDecimal totalPrevisto,
                                       BigDecimal totalRecebido, BigDecimal totalPendente, BigDecimal totalVencido,
                                       BigDecimal totalEstornado, BigDecimal receitaLiquida, Integer faturasPagas,
                                       Integer faturasPendentes, Integer faturasVencidas,
                                       Integer alunosInadimplentes, BigDecimal taxaRecebimento,
                                       BigDecimal taxaInadimplencia, BigDecimal variacaoReceitaPercentual,
                                       BigDecimal variacaoPrevistoPercentual,
                                       BigDecimal variacaoInadimplenciaPercentual, BigDecimal recebidoAcumulado,
                                       BigDecimal previstoAcumulado, BigDecimal vencidoAcumulado) {
        this.mes = mes;
        this.nomeMes = nomeMes;
        this.competencia = competencia;
        this.totalPrevisto = totalPrevisto;
        this.totalRecebido = totalRecebido;
        this.totalPendente = totalPendente;
        this.totalVencido = totalVencido;
        this.totalEstornado = totalEstornado;
        this.receitaLiquida = receitaLiquida;
        this.faturasPagas = faturasPagas;
        this.faturasPendentes = faturasPendentes;
        this.faturasVencidas = faturasVencidas;
        this.alunosInadimplentes = alunosInadimplentes;
        this.taxaRecebimento = taxaRecebimento;
        this.taxaInadimplencia = taxaInadimplencia;
        this.variacaoReceitaPercentual = variacaoReceitaPercentual;
        this.variacaoPrevistoPercentual = variacaoPrevistoPercentual;
        this.variacaoInadimplenciaPercentual = variacaoInadimplenciaPercentual;
        this.recebidoAcumulado = recebidoAcumulado;
        this.previstoAcumulado = previstoAcumulado;
        this.vencidoAcumulado = vencidoAcumulado;
    }

    public Integer getMes() { return mes; }
    public String getNomeMes() { return nomeMes; }
    public String getCompetencia() { return competencia; }
    public BigDecimal getTotalPrevisto() { return totalPrevisto; }
    public BigDecimal getTotalRecebido() { return totalRecebido; }
    public BigDecimal getTotalPendente() { return totalPendente; }
    public BigDecimal getTotalVencido() { return totalVencido; }
    public BigDecimal getTotalEstornado() { return totalEstornado; }
    public BigDecimal getReceitaLiquida() { return receitaLiquida; }
    public Integer getFaturasPagas() { return faturasPagas; }
    public Integer getFaturasPendentes() { return faturasPendentes; }
    public Integer getFaturasVencidas() { return faturasVencidas; }
    public Integer getAlunosInadimplentes() { return alunosInadimplentes; }
    public BigDecimal getTaxaRecebimento() { return taxaRecebimento; }
    public BigDecimal getTaxaInadimplencia() { return taxaInadimplencia; }
    public BigDecimal getVariacaoReceitaPercentual() { return variacaoReceitaPercentual; }
    public BigDecimal getVariacaoPrevistoPercentual() { return variacaoPrevistoPercentual; }
    public BigDecimal getVariacaoInadimplenciaPercentual() { return variacaoInadimplenciaPercentual; }
    public BigDecimal getRecebidoAcumulado() { return recebidoAcumulado; }
    public BigDecimal getPrevistoAcumulado() { return previstoAcumulado; }
    public BigDecimal getVencidoAcumulado() { return vencidoAcumulado; }
}
