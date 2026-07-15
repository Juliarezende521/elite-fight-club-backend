package Tic.Implementacao.dto.financeiro;

import Tic.Implementacao.model.StatusPagamento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CobrancaPixResponse {
    private Long pagamentoId;
    private Long faturaId;
    private StatusPagamento status;
    private BigDecimal valor;
    private String idPagamentoExterno;
    private String pixCopiaCola;
    private String pixQrCodeUrl;
    private String qrCodeBase64;
    private LocalDateTime dataExpiracao;

    public CobrancaPixResponse() {
    }

    public CobrancaPixResponse(Long pagamentoId, String idPagamentoExterno, String pixCopiaCola, String pixQrCodeUrl) {
        this.pagamentoId = pagamentoId;
        this.idPagamentoExterno = idPagamentoExterno;
        this.pixCopiaCola = pixCopiaCola;
        this.pixQrCodeUrl = pixQrCodeUrl;
    }

    public CobrancaPixResponse(Long pagamentoId, Long faturaId, StatusPagamento status, BigDecimal valor,
                               String idPagamentoExterno, String pixCopiaCola, String pixQrCodeUrl,
                               String qrCodeBase64, LocalDateTime dataExpiracao) {
        this.pagamentoId = pagamentoId;
        this.faturaId = faturaId;
        this.status = status;
        this.valor = valor;
        this.idPagamentoExterno = idPagamentoExterno;
        this.pixCopiaCola = pixCopiaCola;
        this.pixQrCodeUrl = pixQrCodeUrl;
        this.qrCodeBase64 = qrCodeBase64;
        this.dataExpiracao = dataExpiracao;
    }

    public Long getPagamentoId() { return pagamentoId; }
    public void setPagamentoId(Long pagamentoId) { this.pagamentoId = pagamentoId; }
    public Long getFaturaId() { return faturaId; }
    public void setFaturaId(Long faturaId) { this.faturaId = faturaId; }
    public StatusPagamento getStatus() { return status; }
    public void setStatus(StatusPagamento status) { this.status = status; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
    public String getIdPagamentoExterno() { return idPagamentoExterno; }
    public void setIdPagamentoExterno(String idPagamentoExterno) { this.idPagamentoExterno = idPagamentoExterno; }
    public String getPixCopiaCola() { return pixCopiaCola; }
    public void setPixCopiaCola(String pixCopiaCola) { this.pixCopiaCola = pixCopiaCola; }
    public String getPixQrCodeUrl() { return pixQrCodeUrl; }
    public void setPixQrCodeUrl(String pixQrCodeUrl) { this.pixQrCodeUrl = pixQrCodeUrl; }
    public String getQrCodeBase64() { return qrCodeBase64; }
    public void setQrCodeBase64(String qrCodeBase64) { this.qrCodeBase64 = qrCodeBase64; }
    public LocalDateTime getDataExpiracao() { return dataExpiracao; }
    public void setDataExpiracao(LocalDateTime dataExpiracao) { this.dataExpiracao = dataExpiracao; }
}
