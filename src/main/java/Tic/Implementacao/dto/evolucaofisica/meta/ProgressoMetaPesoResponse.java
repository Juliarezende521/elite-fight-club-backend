package Tic.Implementacao.dto.evolucaofisica.meta;

import Tic.Implementacao.model.StatusMetaPeso;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ProgressoMetaPesoResponse {
    private Long metaId;
    private Long alunoId;
    private BigDecimal pesoInicial;
    private BigDecimal pesoAtual;
    private BigDecimal pesoAlvo;
    private String direcao;
    private BigDecimal variacaoDesdeInicio;
    private BigDecimal quantidadeRestante;
    private BigDecimal percentualProgresso;
    private Boolean metaAtingida;
    private StatusMetaPeso status;
    private LocalDate dataInicio;
    private LocalDate dataLimite;
    private Integer diasRestantes;

    public ProgressoMetaPesoResponse(Long metaId, Long alunoId, BigDecimal pesoInicial, BigDecimal pesoAtual,
                                     BigDecimal pesoAlvo, String direcao, BigDecimal variacaoDesdeInicio,
                                     BigDecimal quantidadeRestante, BigDecimal percentualProgresso,
                                     Boolean metaAtingida, StatusMetaPeso status, LocalDate dataInicio,
                                     LocalDate dataLimite, Integer diasRestantes) {
        this.metaId = metaId;
        this.alunoId = alunoId;
        this.pesoInicial = pesoInicial;
        this.pesoAtual = pesoAtual;
        this.pesoAlvo = pesoAlvo;
        this.direcao = direcao;
        this.variacaoDesdeInicio = variacaoDesdeInicio;
        this.quantidadeRestante = quantidadeRestante;
        this.percentualProgresso = percentualProgresso;
        this.metaAtingida = metaAtingida;
        this.status = status;
        this.dataInicio = dataInicio;
        this.dataLimite = dataLimite;
        this.diasRestantes = diasRestantes;
    }

    public Long getMetaId() { return metaId; }
    public Long getAlunoId() { return alunoId; }
    public BigDecimal getPesoInicial() { return pesoInicial; }
    public BigDecimal getPesoAtual() { return pesoAtual; }
    public BigDecimal getPesoAlvo() { return pesoAlvo; }
    public String getDirecao() { return direcao; }
    public BigDecimal getVariacaoDesdeInicio() { return variacaoDesdeInicio; }
    public BigDecimal getQuantidadeRestante() { return quantidadeRestante; }
    public BigDecimal getPercentualProgresso() { return percentualProgresso; }
    public Boolean getMetaAtingida() { return metaAtingida; }
    public StatusMetaPeso getStatus() { return status; }
    public LocalDate getDataInicio() { return dataInicio; }
    public LocalDate getDataLimite() { return dataLimite; }
    public Integer getDiasRestantes() { return diasRestantes; }
}
