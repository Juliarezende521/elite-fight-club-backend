package Tic.Implementacao.dto.dashboard.aluno;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ResumoFinanceiroAlunoResponse {
    private Boolean emDia;
    private Long faturaAtualId;
    private String mesReferencia;
    private LocalDate vencimento;
    private BigDecimal valorTotal;
    private BigDecimal valorPago;
    private BigDecimal saldo;
    private String status;
    private Boolean possuiPixPendente;
    private Long pagamentoPixId;
    private LocalDateTime pixExpiraEm;

    public ResumoFinanceiroAlunoResponse(Boolean emDia, Long faturaAtualId, String mesReferencia,
                                         LocalDate vencimento, BigDecimal valorTotal, BigDecimal valorPago,
                                         BigDecimal saldo, String status, Boolean possuiPixPendente,
                                         Long pagamentoPixId, LocalDateTime pixExpiraEm) {
        this.emDia = emDia;
        this.faturaAtualId = faturaAtualId;
        this.mesReferencia = mesReferencia;
        this.vencimento = vencimento;
        this.valorTotal = valorTotal;
        this.valorPago = valorPago;
        this.saldo = saldo;
        this.status = status;
        this.possuiPixPendente = possuiPixPendente;
        this.pagamentoPixId = pagamentoPixId;
        this.pixExpiraEm = pixExpiraEm;
    }

    public Boolean getEmDia() { return emDia; }
    public Long getFaturaAtualId() { return faturaAtualId; }
    public String getMesReferencia() { return mesReferencia; }
    public LocalDate getVencimento() { return vencimento; }
    public BigDecimal getValorTotal() { return valorTotal; }
    public BigDecimal getValorPago() { return valorPago; }
    public BigDecimal getSaldo() { return saldo; }
    public String getStatus() { return status; }
    public Boolean getPossuiPixPendente() { return possuiPixPendente; }
    public Long getPagamentoPixId() { return pagamentoPixId; }
    public LocalDateTime getPixExpiraEm() { return pixExpiraEm; }
}
