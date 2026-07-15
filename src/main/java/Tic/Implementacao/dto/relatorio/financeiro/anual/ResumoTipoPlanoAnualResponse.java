package Tic.Implementacao.dto.relatorio.financeiro.anual;

import java.math.BigDecimal;

public class ResumoTipoPlanoAnualResponse {
    private String tipoPlano;
    private Integer quantidadeAlunos;
    private Integer quantidadeFaturas;
    private BigDecimal totalPrevisto;
    private BigDecimal totalRecebido;
    private BigDecimal totalPendente;
    private BigDecimal totalVencido;
    private BigDecimal ticketMedio;
    private BigDecimal taxaRecebimento;
    private Integer alunosInadimplentes;

    public ResumoTipoPlanoAnualResponse(String tipoPlano, Integer quantidadeAlunos, Integer quantidadeFaturas,
                                        BigDecimal totalPrevisto, BigDecimal totalRecebido,
                                        BigDecimal totalPendente, BigDecimal totalVencido,
                                        BigDecimal ticketMedio, BigDecimal taxaRecebimento,
                                        Integer alunosInadimplentes) {
        this.tipoPlano = tipoPlano;
        this.quantidadeAlunos = quantidadeAlunos;
        this.quantidadeFaturas = quantidadeFaturas;
        this.totalPrevisto = totalPrevisto;
        this.totalRecebido = totalRecebido;
        this.totalPendente = totalPendente;
        this.totalVencido = totalVencido;
        this.ticketMedio = ticketMedio;
        this.taxaRecebimento = taxaRecebimento;
        this.alunosInadimplentes = alunosInadimplentes;
    }

    public String getTipoPlano() { return tipoPlano; }
    public Integer getQuantidadeAlunos() { return quantidadeAlunos; }
    public Integer getQuantidadeFaturas() { return quantidadeFaturas; }
    public BigDecimal getTotalPrevisto() { return totalPrevisto; }
    public BigDecimal getTotalRecebido() { return totalRecebido; }
    public BigDecimal getTotalPendente() { return totalPendente; }
    public BigDecimal getTotalVencido() { return totalVencido; }
    public BigDecimal getTicketMedio() { return ticketMedio; }
    public BigDecimal getTaxaRecebimento() { return taxaRecebimento; }
    public Integer getAlunosInadimplentes() { return alunosInadimplentes; }
}
