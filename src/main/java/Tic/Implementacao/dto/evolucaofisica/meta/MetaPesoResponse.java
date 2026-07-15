package Tic.Implementacao.dto.evolucaofisica.meta;

import Tic.Implementacao.model.StatusMetaPeso;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class MetaPesoResponse {
    private Long id;
    private Long alunoId;
    private String alunoNome;
    private BigDecimal pesoInicial;
    private BigDecimal pesoAlvo;
    private Boolean pesoInicialManual;
    private String direcao;
    private LocalDate dataInicio;
    private LocalDate dataLimite;
    private StatusMetaPeso status;
    private String observacao;
    private LocalDateTime criadaEm;
    private LocalDateTime atualizadaEm;
    private LocalDateTime encerradaEm;

    public MetaPesoResponse(Long id, Long alunoId, String alunoNome, BigDecimal pesoInicial, BigDecimal pesoAlvo,
                            Boolean pesoInicialManual, String direcao, LocalDate dataInicio, LocalDate dataLimite,
                            StatusMetaPeso status, String observacao, LocalDateTime criadaEm,
                            LocalDateTime atualizadaEm, LocalDateTime encerradaEm) {
        this.id = id;
        this.alunoId = alunoId;
        this.alunoNome = alunoNome;
        this.pesoInicial = pesoInicial;
        this.pesoAlvo = pesoAlvo;
        this.pesoInicialManual = pesoInicialManual;
        this.direcao = direcao;
        this.dataInicio = dataInicio;
        this.dataLimite = dataLimite;
        this.status = status;
        this.observacao = observacao;
        this.criadaEm = criadaEm;
        this.atualizadaEm = atualizadaEm;
        this.encerradaEm = encerradaEm;
    }

    public Long getId() { return id; }
    public Long getAlunoId() { return alunoId; }
    public String getAlunoNome() { return alunoNome; }
    public BigDecimal getPesoInicial() { return pesoInicial; }
    public BigDecimal getPesoAlvo() { return pesoAlvo; }
    public Boolean getPesoInicialManual() { return pesoInicialManual; }
    public String getDirecao() { return direcao; }
    public LocalDate getDataInicio() { return dataInicio; }
    public LocalDate getDataLimite() { return dataLimite; }
    public StatusMetaPeso getStatus() { return status; }
    public String getObservacao() { return observacao; }
    public LocalDateTime getCriadaEm() { return criadaEm; }
    public LocalDateTime getAtualizadaEm() { return atualizadaEm; }
    public LocalDateTime getEncerradaEm() { return encerradaEm; }
}
