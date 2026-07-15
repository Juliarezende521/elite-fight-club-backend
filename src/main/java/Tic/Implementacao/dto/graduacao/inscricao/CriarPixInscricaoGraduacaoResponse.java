package Tic.Implementacao.dto.graduacao.inscricao;

import Tic.Implementacao.model.StatusInscricaoGraduacao;
import Tic.Implementacao.model.StatusPagamento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CriarPixInscricaoGraduacaoResponse {
    private Long inscricaoId;
    private Long eventoId;
    private String graduacaoDestino;
    private BigDecimal valor;
    private StatusInscricaoGraduacao statusInscricao;
    private Long pagamentoId;
    private StatusPagamento statusPagamento;
    private String idPagamentoExterno;
    private String pixCopiaCola;
    private String pixQrCodeUrl;
    private String qrCodeBase64;
    private LocalDateTime expiraEm;

    public CriarPixInscricaoGraduacaoResponse(Long inscricaoId, Long eventoId, String graduacaoDestino, BigDecimal valor,
                                             StatusInscricaoGraduacao statusInscricao, Long pagamentoId,
                                             StatusPagamento statusPagamento, String idPagamentoExterno,
                                             String pixCopiaCola, String pixQrCodeUrl, String qrCodeBase64,
                                             LocalDateTime expiraEm) {
        this.inscricaoId = inscricaoId;
        this.eventoId = eventoId;
        this.graduacaoDestino = graduacaoDestino;
        this.valor = valor;
        this.statusInscricao = statusInscricao;
        this.pagamentoId = pagamentoId;
        this.statusPagamento = statusPagamento;
        this.idPagamentoExterno = idPagamentoExterno;
        this.pixCopiaCola = pixCopiaCola;
        this.pixQrCodeUrl = pixQrCodeUrl;
        this.qrCodeBase64 = qrCodeBase64;
        this.expiraEm = expiraEm;
    }

    public Long getInscricaoId() { return inscricaoId; }
    public Long getEventoId() { return eventoId; }
    public String getGraduacaoDestino() { return graduacaoDestino; }
    public BigDecimal getValor() { return valor; }
    public StatusInscricaoGraduacao getStatusInscricao() { return statusInscricao; }
    public Long getPagamentoId() { return pagamentoId; }
    public StatusPagamento getStatusPagamento() { return statusPagamento; }
    public String getIdPagamentoExterno() { return idPagamentoExterno; }
    public String getPixCopiaCola() { return pixCopiaCola; }
    public String getPixQrCodeUrl() { return pixQrCodeUrl; }
    public String getQrCodeBase64() { return qrCodeBase64; }
    public LocalDateTime getExpiraEm() { return expiraEm; }
}
