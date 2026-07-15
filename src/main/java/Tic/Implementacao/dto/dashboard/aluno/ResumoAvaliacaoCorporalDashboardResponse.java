package Tic.Implementacao.dto.dashboard.aluno;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ResumoAvaliacaoCorporalDashboardResponse {
    private Long avaliacaoId;
    private LocalDate dataAvaliacao;
    private BigDecimal percentualGordura;
    private BigDecimal massaMuscularKg;
    private BigDecimal cintura;
    private Boolean possuiBioimpedancia;
    private Boolean possuiMedidas;

    public ResumoAvaliacaoCorporalDashboardResponse(Long avaliacaoId, LocalDate dataAvaliacao,
                                                    BigDecimal percentualGordura, BigDecimal massaMuscularKg,
                                                    BigDecimal cintura, Boolean possuiBioimpedancia,
                                                    Boolean possuiMedidas) {
        this.avaliacaoId = avaliacaoId;
        this.dataAvaliacao = dataAvaliacao;
        this.percentualGordura = percentualGordura;
        this.massaMuscularKg = massaMuscularKg;
        this.cintura = cintura;
        this.possuiBioimpedancia = possuiBioimpedancia;
        this.possuiMedidas = possuiMedidas;
    }

    public Long getAvaliacaoId() { return avaliacaoId; }
    public LocalDate getDataAvaliacao() { return dataAvaliacao; }
    public BigDecimal getPercentualGordura() { return percentualGordura; }
    public BigDecimal getMassaMuscularKg() { return massaMuscularKg; }
    public BigDecimal getCintura() { return cintura; }
    public Boolean getPossuiBioimpedancia() { return possuiBioimpedancia; }
    public Boolean getPossuiMedidas() { return possuiMedidas; }
}
