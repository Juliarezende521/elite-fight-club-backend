package Tic.Implementacao.dto.graduacao.inscricao;

import Tic.Implementacao.model.StatusPagamento;
import Tic.Implementacao.model.TipoCobranca;

public class ResumoPagamentoInscricaoResponse {
    private Long pagamentoId;
    private TipoCobranca tipoCobranca;
    private StatusPagamento statusPagamento;

    public ResumoPagamentoInscricaoResponse(Long pagamentoId, TipoCobranca tipoCobranca, StatusPagamento statusPagamento) {
        this.pagamentoId = pagamentoId;
        this.tipoCobranca = tipoCobranca;
        this.statusPagamento = statusPagamento;
    }

    public Long getPagamentoId() { return pagamentoId; }
    public TipoCobranca getTipoCobranca() { return tipoCobranca; }
    public StatusPagamento getStatusPagamento() { return statusPagamento; }
}
