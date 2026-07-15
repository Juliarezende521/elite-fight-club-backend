package Tic.Implementacao.dto.relatorio.financeiro;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class AlunoFinanceiroMensalResponse {
    private Long alunoId;
    private String alunoNome;
    private String email;
    private String tipoPlano;
    private BigDecimal valorMensalidade;
    private Integer diaVencimento;
    private Long faturaId;
    private String competencia;
    private LocalDate vencimento;
    private BigDecimal valorFatura;
    private BigDecimal valorPago;
    private BigDecimal saldo;
    private String statusFatura;
    private Boolean vencida;
    private Boolean possuiDebitoAnterior;
    private String formaPagamentoPrincipal;
    private LocalDateTime ultimoPagamentoEm;

    public AlunoFinanceiroMensalResponse(Long alunoId, String alunoNome, String email, String tipoPlano,
                                         BigDecimal valorMensalidade, Integer diaVencimento, Long faturaId,
                                         String competencia, LocalDate vencimento, BigDecimal valorFatura,
                                         BigDecimal valorPago, BigDecimal saldo, String statusFatura,
                                         Boolean vencida, Boolean possuiDebitoAnterior,
                                         String formaPagamentoPrincipal, LocalDateTime ultimoPagamentoEm) {
        this.alunoId = alunoId;
        this.alunoNome = alunoNome;
        this.email = email;
        this.tipoPlano = tipoPlano;
        this.valorMensalidade = valorMensalidade;
        this.diaVencimento = diaVencimento;
        this.faturaId = faturaId;
        this.competencia = competencia;
        this.vencimento = vencimento;
        this.valorFatura = valorFatura;
        this.valorPago = valorPago;
        this.saldo = saldo;
        this.statusFatura = statusFatura;
        this.vencida = vencida;
        this.possuiDebitoAnterior = possuiDebitoAnterior;
        this.formaPagamentoPrincipal = formaPagamentoPrincipal;
        this.ultimoPagamentoEm = ultimoPagamentoEm;
    }

    public Long getAlunoId() { return alunoId; }
    public String getAlunoNome() { return alunoNome; }
    public String getEmail() { return email; }
    public String getTipoPlano() { return tipoPlano; }
    public BigDecimal getValorMensalidade() { return valorMensalidade; }
    public Integer getDiaVencimento() { return diaVencimento; }
    public Long getFaturaId() { return faturaId; }
    public String getCompetencia() { return competencia; }
    public LocalDate getVencimento() { return vencimento; }
    public BigDecimal getValorFatura() { return valorFatura; }
    public BigDecimal getValorPago() { return valorPago; }
    public BigDecimal getSaldo() { return saldo; }
    public String getStatusFatura() { return statusFatura; }
    public Boolean getVencida() { return vencida; }
    public Boolean getPossuiDebitoAnterior() { return possuiDebitoAnterior; }
    public String getFormaPagamentoPrincipal() { return formaPagamentoPrincipal; }
    public LocalDateTime getUltimoPagamentoEm() { return ultimoPagamentoEm; }
}
