package Tic.Implementacao.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "bioimpedancias")
public class Bioimpedancia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "avaliacao_id", nullable = false, unique = true)
    private AvaliacaoCorporal avaliacao;

    @Column(precision = 5, scale = 2) private BigDecimal percentualGordura;
    @Column(precision = 6, scale = 2) private BigDecimal massaGordaKg;
    @Column(precision = 6, scale = 2) private BigDecimal massaMagraKg;
    @Column(precision = 6, scale = 2) private BigDecimal massaMuscularKg;
    @Column(precision = 5, scale = 2) private BigDecimal aguaCorporalPercentual;
    @Column(precision = 6, scale = 2) private BigDecimal gorduraVisceral;
    @Column(precision = 6, scale = 2) private BigDecimal massaOsseaKg;
    @Column(precision = 8, scale = 2) private BigDecimal metabolismoBasalKcal;
    @Column(precision = 6, scale = 2) private BigDecimal idadeMetabolica;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public AvaliacaoCorporal getAvaliacao() { return avaliacao; }
    public void setAvaliacao(AvaliacaoCorporal avaliacao) { this.avaliacao = avaliacao; }
    public BigDecimal getPercentualGordura() { return percentualGordura; }
    public void setPercentualGordura(BigDecimal percentualGordura) { this.percentualGordura = percentualGordura; }
    public BigDecimal getMassaGordaKg() { return massaGordaKg; }
    public void setMassaGordaKg(BigDecimal massaGordaKg) { this.massaGordaKg = massaGordaKg; }
    public BigDecimal getMassaMagraKg() { return massaMagraKg; }
    public void setMassaMagraKg(BigDecimal massaMagraKg) { this.massaMagraKg = massaMagraKg; }
    public BigDecimal getMassaMuscularKg() { return massaMuscularKg; }
    public void setMassaMuscularKg(BigDecimal massaMuscularKg) { this.massaMuscularKg = massaMuscularKg; }
    public BigDecimal getAguaCorporalPercentual() { return aguaCorporalPercentual; }
    public void setAguaCorporalPercentual(BigDecimal aguaCorporalPercentual) { this.aguaCorporalPercentual = aguaCorporalPercentual; }
    public BigDecimal getGorduraVisceral() { return gorduraVisceral; }
    public void setGorduraVisceral(BigDecimal gorduraVisceral) { this.gorduraVisceral = gorduraVisceral; }
    public BigDecimal getMassaOsseaKg() { return massaOsseaKg; }
    public void setMassaOsseaKg(BigDecimal massaOsseaKg) { this.massaOsseaKg = massaOsseaKg; }
    public BigDecimal getMetabolismoBasalKcal() { return metabolismoBasalKcal; }
    public void setMetabolismoBasalKcal(BigDecimal metabolismoBasalKcal) { this.metabolismoBasalKcal = metabolismoBasalKcal; }
    public BigDecimal getIdadeMetabolica() { return idadeMetabolica; }
    public void setIdadeMetabolica(BigDecimal idadeMetabolica) { this.idadeMetabolica = idadeMetabolica; }
}
