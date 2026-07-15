package Tic.Implementacao.dto.evolucaofisica.avaliacao;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ResumoAvaliacaoCorporalResponse {
    private Long avaliacaoId;
    private LocalDate dataAvaliacao;
    private BigDecimal alturaMetros;
    private BigDecimal pesoRegistrado;
    private BigDecimal percentualGordura;
    private BigDecimal massaMagraKg;
    private BigDecimal massaMuscularKg;
    private BigDecimal cintura;
    private BigDecimal quadril;
    private Boolean possuiMedidas;
    private Boolean possuiBioimpedancia;

    public ResumoAvaliacaoCorporalResponse(Long avaliacaoId, LocalDate dataAvaliacao, BigDecimal alturaMetros,
                                           BigDecimal pesoRegistrado, BigDecimal percentualGordura,
                                           BigDecimal massaMagraKg, BigDecimal massaMuscularKg,
                                           BigDecimal cintura, BigDecimal quadril,
                                           Boolean possuiMedidas, Boolean possuiBioimpedancia) {
        this.avaliacaoId = avaliacaoId;
        this.dataAvaliacao = dataAvaliacao;
        this.alturaMetros = alturaMetros;
        this.pesoRegistrado = pesoRegistrado;
        this.percentualGordura = percentualGordura;
        this.massaMagraKg = massaMagraKg;
        this.massaMuscularKg = massaMuscularKg;
        this.cintura = cintura;
        this.quadril = quadril;
        this.possuiMedidas = possuiMedidas;
        this.possuiBioimpedancia = possuiBioimpedancia;
    }

    public Long getAvaliacaoId() { return avaliacaoId; }
    public LocalDate getDataAvaliacao() { return dataAvaliacao; }
    public BigDecimal getAlturaMetros() { return alturaMetros; }
    public BigDecimal getPesoRegistrado() { return pesoRegistrado; }
    public BigDecimal getPercentualGordura() { return percentualGordura; }
    public BigDecimal getMassaMagraKg() { return massaMagraKg; }
    public BigDecimal getMassaMuscularKg() { return massaMuscularKg; }
    public BigDecimal getCintura() { return cintura; }
    public BigDecimal getQuadril() { return quadril; }
    public Boolean getPossuiMedidas() { return possuiMedidas; }
    public Boolean getPossuiBioimpedancia() { return possuiBioimpedancia; }
}
