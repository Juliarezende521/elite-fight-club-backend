package Tic.Implementacao.dto.evolucaofisica.avaliacao;

import java.math.BigDecimal;

public class BioimpedanciaRequest {
    private BigDecimal percentualGordura;
    private BigDecimal massaGordaKg;
    private BigDecimal massaMagraKg;
    private BigDecimal massaMuscularKg;
    private BigDecimal aguaCorporalPercentual;
    private BigDecimal gorduraVisceral;
    private BigDecimal massaOsseaKg;
    private BigDecimal metabolismoBasalKcal;
    private BigDecimal idadeMetabolica;

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
