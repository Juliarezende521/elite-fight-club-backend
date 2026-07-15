package Tic.Implementacao.dto.relatorio.financeiro;

import java.math.BigDecimal;

public class ResumoTipoPlanoMensalResponse {
    private String tipoPlano;
    private Integer quantidadeAlunos;
    private BigDecimal totalPrevisto;
    private BigDecimal totalRecebido;
    private BigDecimal totalPendente;
    private Integer inadimplentes;

    public ResumoTipoPlanoMensalResponse(String tipoPlano, Integer quantidadeAlunos, BigDecimal totalPrevisto,
                                         BigDecimal totalRecebido, BigDecimal totalPendente, Integer inadimplentes) {
        this.tipoPlano = tipoPlano;
        this.quantidadeAlunos = quantidadeAlunos;
        this.totalPrevisto = totalPrevisto;
        this.totalRecebido = totalRecebido;
        this.totalPendente = totalPendente;
        this.inadimplentes = inadimplentes;
    }

    public String getTipoPlano() { return tipoPlano; }
    public Integer getQuantidadeAlunos() { return quantidadeAlunos; }
    public BigDecimal getTotalPrevisto() { return totalPrevisto; }
    public BigDecimal getTotalRecebido() { return totalRecebido; }
    public BigDecimal getTotalPendente() { return totalPendente; }
    public Integer getInadimplentes() { return inadimplentes; }
}
