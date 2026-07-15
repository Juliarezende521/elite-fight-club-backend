package Tic.Implementacao.dto.financeiro;

import Tic.Implementacao.model.StatusFatura;
import Tic.Implementacao.model.StatusInscricaoGraduacao;
import Tic.Implementacao.model.StatusPagamento;
import Tic.Implementacao.model.TipoCobranca;

import java.time.LocalDateTime;

public class ConciliacaoPagamentoResponse {
    private Long pagamentoId;
    private Long faturaId;
    private Long inscricaoGraduacaoId;
    private TipoCobranca tipoCobranca;
    private StatusPagamento statusPagamentoAnterior;
    private StatusPagamento statusPagamentoAtual;
    private StatusFatura statusFaturaAnterior;
    private StatusFatura statusFaturaAtual;
    private StatusInscricaoGraduacao statusInscricaoAnterior;
    private StatusInscricaoGraduacao statusInscricaoAtual;
    private LocalDateTime conciliadoEm;

    public ConciliacaoPagamentoResponse() {
    }

    public ConciliacaoPagamentoResponse(Long pagamentoId, Long faturaId,
                                        StatusPagamento statusPagamentoAnterior, StatusPagamento statusPagamentoAtual,
                                        StatusFatura statusFaturaAnterior, StatusFatura statusFaturaAtual,
                                        LocalDateTime conciliadoEm) {
        this.pagamentoId = pagamentoId;
        this.faturaId = faturaId;
        this.statusPagamentoAnterior = statusPagamentoAnterior;
        this.statusPagamentoAtual = statusPagamentoAtual;
        this.statusFaturaAnterior = statusFaturaAnterior;
        this.statusFaturaAtual = statusFaturaAtual;
        this.conciliadoEm = conciliadoEm;
    }

    public ConciliacaoPagamentoResponse(Long pagamentoId, Long faturaId, Long inscricaoGraduacaoId, TipoCobranca tipoCobranca,
                                        StatusPagamento statusPagamentoAnterior, StatusPagamento statusPagamentoAtual,
                                        StatusFatura statusFaturaAnterior, StatusFatura statusFaturaAtual,
                                        StatusInscricaoGraduacao statusInscricaoAnterior,
                                        StatusInscricaoGraduacao statusInscricaoAtual,
                                        LocalDateTime conciliadoEm) {
        this.pagamentoId = pagamentoId;
        this.faturaId = faturaId;
        this.inscricaoGraduacaoId = inscricaoGraduacaoId;
        this.tipoCobranca = tipoCobranca;
        this.statusPagamentoAnterior = statusPagamentoAnterior;
        this.statusPagamentoAtual = statusPagamentoAtual;
        this.statusFaturaAnterior = statusFaturaAnterior;
        this.statusFaturaAtual = statusFaturaAtual;
        this.statusInscricaoAnterior = statusInscricaoAnterior;
        this.statusInscricaoAtual = statusInscricaoAtual;
        this.conciliadoEm = conciliadoEm;
    }

    public Long getPagamentoId() { return pagamentoId; }
    public void setPagamentoId(Long pagamentoId) { this.pagamentoId = pagamentoId; }
    public Long getFaturaId() { return faturaId; }
    public void setFaturaId(Long faturaId) { this.faturaId = faturaId; }
    public Long getInscricaoGraduacaoId() { return inscricaoGraduacaoId; }
    public void setInscricaoGraduacaoId(Long inscricaoGraduacaoId) { this.inscricaoGraduacaoId = inscricaoGraduacaoId; }
    public TipoCobranca getTipoCobranca() { return tipoCobranca; }
    public void setTipoCobranca(TipoCobranca tipoCobranca) { this.tipoCobranca = tipoCobranca; }
    public StatusPagamento getStatusPagamentoAnterior() { return statusPagamentoAnterior; }
    public void setStatusPagamentoAnterior(StatusPagamento statusPagamentoAnterior) { this.statusPagamentoAnterior = statusPagamentoAnterior; }
    public StatusPagamento getStatusPagamentoAtual() { return statusPagamentoAtual; }
    public void setStatusPagamentoAtual(StatusPagamento statusPagamentoAtual) { this.statusPagamentoAtual = statusPagamentoAtual; }
    public StatusFatura getStatusFaturaAnterior() { return statusFaturaAnterior; }
    public void setStatusFaturaAnterior(StatusFatura statusFaturaAnterior) { this.statusFaturaAnterior = statusFaturaAnterior; }
    public StatusFatura getStatusFaturaAtual() { return statusFaturaAtual; }
    public void setStatusFaturaAtual(StatusFatura statusFaturaAtual) { this.statusFaturaAtual = statusFaturaAtual; }
    public StatusInscricaoGraduacao getStatusInscricaoAnterior() { return statusInscricaoAnterior; }
    public void setStatusInscricaoAnterior(StatusInscricaoGraduacao statusInscricaoAnterior) { this.statusInscricaoAnterior = statusInscricaoAnterior; }
    public StatusInscricaoGraduacao getStatusInscricaoAtual() { return statusInscricaoAtual; }
    public void setStatusInscricaoAtual(StatusInscricaoGraduacao statusInscricaoAtual) { this.statusInscricaoAtual = statusInscricaoAtual; }
    public LocalDateTime getConciliadoEm() { return conciliadoEm; }
    public void setConciliadoEm(LocalDateTime conciliadoEm) { this.conciliadoEm = conciliadoEm; }
}
