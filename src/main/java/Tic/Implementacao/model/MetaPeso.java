package Tic.Implementacao.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "metas_peso",
        indexes = {
                @Index(name = "idx_metas_peso_academia_aluno_status", columnList = "academia_id, aluno_id, status"),
                @Index(name = "idx_metas_peso_aluno_data", columnList = "aluno_id, data_inicio")
        }
)
public class MetaPeso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "academia_id", nullable = false)
    private Academia academia;

    @ManyToOne(optional = false)
    @JoinColumn(name = "aluno_id", nullable = false)
    private Aluno aluno;

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal pesoInicial;

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal pesoAlvo;

    @Column(nullable = false)
    private Boolean pesoInicialManual = false;

    @Column(nullable = false)
    private LocalDate dataInicio;

    private LocalDate dataLimite;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusMetaPeso status = StatusMetaPeso.ATIVA;

    @Column(length = 1000)
    private String observacao;

    @Column(nullable = false)
    private LocalDateTime criadaEm;

    private LocalDateTime atualizadaEm;
    private LocalDateTime encerradaEm;

    @ManyToOne(optional = false)
    @JoinColumn(name = "criada_por_id", nullable = false)
    private Usuario criadaPor;

    @ManyToOne
    @JoinColumn(name = "atualizada_por_id")
    private Usuario atualizadaPor;

    @ManyToOne
    @JoinColumn(name = "encerrada_por_id")
    private Usuario encerradaPor;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Academia getAcademia() { return academia; }
    public void setAcademia(Academia academia) { this.academia = academia; }
    public Aluno getAluno() { return aluno; }
    public void setAluno(Aluno aluno) { this.aluno = aluno; }
    public BigDecimal getPesoInicial() { return pesoInicial; }
    public void setPesoInicial(BigDecimal pesoInicial) { this.pesoInicial = pesoInicial; }
    public BigDecimal getPesoAlvo() { return pesoAlvo; }
    public void setPesoAlvo(BigDecimal pesoAlvo) { this.pesoAlvo = pesoAlvo; }
    public Boolean getPesoInicialManual() { return pesoInicialManual; }
    public void setPesoInicialManual(Boolean pesoInicialManual) { this.pesoInicialManual = pesoInicialManual; }
    public LocalDate getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDate dataInicio) { this.dataInicio = dataInicio; }
    public LocalDate getDataLimite() { return dataLimite; }
    public void setDataLimite(LocalDate dataLimite) { this.dataLimite = dataLimite; }
    public StatusMetaPeso getStatus() { return status; }
    public void setStatus(StatusMetaPeso status) { this.status = status; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
    public LocalDateTime getCriadaEm() { return criadaEm; }
    public void setCriadaEm(LocalDateTime criadaEm) { this.criadaEm = criadaEm; }
    public LocalDateTime getAtualizadaEm() { return atualizadaEm; }
    public void setAtualizadaEm(LocalDateTime atualizadaEm) { this.atualizadaEm = atualizadaEm; }
    public LocalDateTime getEncerradaEm() { return encerradaEm; }
    public void setEncerradaEm(LocalDateTime encerradaEm) { this.encerradaEm = encerradaEm; }
    public Usuario getCriadaPor() { return criadaPor; }
    public void setCriadaPor(Usuario criadaPor) { this.criadaPor = criadaPor; }
    public Usuario getAtualizadaPor() { return atualizadaPor; }
    public void setAtualizadaPor(Usuario atualizadaPor) { this.atualizadaPor = atualizadaPor; }
    public Usuario getEncerradaPor() { return encerradaPor; }
    public void setEncerradaPor(Usuario encerradaPor) { this.encerradaPor = encerradaPor; }
}
