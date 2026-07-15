package Tic.Implementacao.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagamentos", indexes = {
        @Index(name = "idx_pagamento_externo", columnList = "idPagamentoExterno")
})
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "fatura_id")
    private Fatura fatura;

    @ManyToOne
    @JoinColumn(name = "inscricao_graduacao_id")
    private InscricaoGraduacao inscricaoGraduacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoCobranca tipoCobranca = TipoCobranca.MENSALIDADE;

    @ManyToOne
    @JoinColumn(name = "academia_id", nullable = false)
    private Academia academia;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MetodoPagamento metodo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusPagamento status;

    private String provedor;
    private String idPagamentoExterno;

    @Column(length = 5000)
    private String pixCopiaCola;

    private String pixQrCodeUrl;
    @Column(length = 10000)
    private String pixQrCodeBase64;
    private LocalDateTime dataExpiracao;
    private String webhookStatus;
    private String ultimoEventoExterno;
    private LocalDateTime ultimoEventoEm;
    private String observacao;
    private LocalDateTime pagoEm;
    private LocalDateTime criadoEm;
    private LocalDateTime conciliadoEm;

    @ManyToOne
    @JoinColumn(name = "confirmado_por_usuario_id")
    private Usuario confirmadoPor;

    public Pagamento() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Fatura getFatura() { return fatura; }
    public void setFatura(Fatura fatura) { this.fatura = fatura; }
    public InscricaoGraduacao getInscricaoGraduacao() { return inscricaoGraduacao; }
    public void setInscricaoGraduacao(InscricaoGraduacao inscricaoGraduacao) { this.inscricaoGraduacao = inscricaoGraduacao; }
    public TipoCobranca getTipoCobranca() { return tipoCobranca; }
    public void setTipoCobranca(TipoCobranca tipoCobranca) { this.tipoCobranca = tipoCobranca; }
    public Academia getAcademia() { return academia; }
    public void setAcademia(Academia academia) { this.academia = academia; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
    public MetodoPagamento getMetodo() { return metodo; }
    public void setMetodo(MetodoPagamento metodo) { this.metodo = metodo; }
    public StatusPagamento getStatus() { return status; }
    public void setStatus(StatusPagamento status) { this.status = status; }
    public String getProvedor() { return provedor; }
    public void setProvedor(String provedor) { this.provedor = provedor; }
    public String getIdPagamentoExterno() { return idPagamentoExterno; }
    public void setIdPagamentoExterno(String idPagamentoExterno) { this.idPagamentoExterno = idPagamentoExterno; }
    public String getPixCopiaCola() { return pixCopiaCola; }
    public void setPixCopiaCola(String pixCopiaCola) { this.pixCopiaCola = pixCopiaCola; }
    public String getPixQrCodeUrl() { return pixQrCodeUrl; }
    public void setPixQrCodeUrl(String pixQrCodeUrl) { this.pixQrCodeUrl = pixQrCodeUrl; }
    public String getPixQrCodeBase64() { return pixQrCodeBase64; }
    public void setPixQrCodeBase64(String pixQrCodeBase64) { this.pixQrCodeBase64 = pixQrCodeBase64; }
    public LocalDateTime getDataExpiracao() { return dataExpiracao; }
    public void setDataExpiracao(LocalDateTime dataExpiracao) { this.dataExpiracao = dataExpiracao; }
    public String getWebhookStatus() { return webhookStatus; }
    public void setWebhookStatus(String webhookStatus) { this.webhookStatus = webhookStatus; }
    public String getUltimoEventoExterno() { return ultimoEventoExterno; }
    public void setUltimoEventoExterno(String ultimoEventoExterno) { this.ultimoEventoExterno = ultimoEventoExterno; }
    public LocalDateTime getUltimoEventoEm() { return ultimoEventoEm; }
    public void setUltimoEventoEm(LocalDateTime ultimoEventoEm) { this.ultimoEventoEm = ultimoEventoEm; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
    public LocalDateTime getPagoEm() { return pagoEm; }
    public void setPagoEm(LocalDateTime pagoEm) { this.pagoEm = pagoEm; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
    public LocalDateTime getConciliadoEm() { return conciliadoEm; }
    public void setConciliadoEm(LocalDateTime conciliadoEm) { this.conciliadoEm = conciliadoEm; }
    public Usuario getConfirmadoPor() { return confirmadoPor; }
    public void setConfirmadoPor(Usuario confirmadoPor) { this.confirmadoPor = confirmadoPor; }
}
