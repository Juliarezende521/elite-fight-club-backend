package Tic.Implementacao.dto.dashboard.aluno;

import Tic.Implementacao.model.StatusMetaPeso;

import java.math.BigDecimal;

public class ProgressoMetaPesoDashboardResponse {
    private Long metaId;
    private BigDecimal pesoAtual;
    private BigDecimal pesoAlvo;
    private BigDecimal quantidadeRestante;
    private BigDecimal percentualProgresso;
    private String direcao;
    private Boolean metaAtingida;
    private StatusMetaPeso status;

    public ProgressoMetaPesoDashboardResponse(Long metaId, BigDecimal pesoAtual, BigDecimal pesoAlvo,
                                              BigDecimal quantidadeRestante, BigDecimal percentualProgresso,
                                              String direcao, Boolean metaAtingida, StatusMetaPeso status) {
        this.metaId = metaId;
        this.pesoAtual = pesoAtual;
        this.pesoAlvo = pesoAlvo;
        this.quantidadeRestante = quantidadeRestante;
        this.percentualProgresso = percentualProgresso;
        this.direcao = direcao;
        this.metaAtingida = metaAtingida;
        this.status = status;
    }

    public Long getMetaId() { return metaId; }
    public BigDecimal getPesoAtual() { return pesoAtual; }
    public BigDecimal getPesoAlvo() { return pesoAlvo; }
    public BigDecimal getQuantidadeRestante() { return quantidadeRestante; }
    public BigDecimal getPercentualProgresso() { return percentualProgresso; }
    public String getDirecao() { return direcao; }
    public Boolean getMetaAtingida() { return metaAtingida; }
    public StatusMetaPeso getStatus() { return status; }
}
