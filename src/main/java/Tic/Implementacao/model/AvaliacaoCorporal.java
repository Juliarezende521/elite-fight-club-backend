package Tic.Implementacao.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "avaliacoes_corporais",
        indexes = {
                @Index(name = "idx_avaliacoes_corporais_academia_aluno_data", columnList = "academia_id, aluno_id, data_avaliacao"),
                @Index(name = "idx_avaliacoes_corporais_aluno_ativa", columnList = "aluno_id, ativa")
        }
)
public class AvaliacaoCorporal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "academia_id", nullable = false)
    private Academia academia;

    @ManyToOne(optional = false)
    @JoinColumn(name = "aluno_id", nullable = false)
    private Aluno aluno;

    @Column(nullable = false)
    private LocalDate dataAvaliacao;

    @Column(precision = 5, scale = 2)
    private BigDecimal alturaMetros;

    @Column(precision = 6, scale = 2)
    private BigDecimal pesoRegistrado;

    @Column(length = 2000)
    private String observacao;

    @Column(nullable = false)
    private LocalDateTime criadaEm;

    private LocalDateTime atualizadaEm;

    @ManyToOne(optional = false)
    @JoinColumn(name = "criada_por_id", nullable = false)
    private Usuario criadaPor;

    @ManyToOne
    @JoinColumn(name = "atualizada_por_id")
    private Usuario atualizadaPor;

    @Column(nullable = false)
    private Boolean ativa = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Academia getAcademia() { return academia; }
    public void setAcademia(Academia academia) { this.academia = academia; }
    public Aluno getAluno() { return aluno; }
    public void setAluno(Aluno aluno) { this.aluno = aluno; }
    public LocalDate getDataAvaliacao() { return dataAvaliacao; }
    public void setDataAvaliacao(LocalDate dataAvaliacao) { this.dataAvaliacao = dataAvaliacao; }
    public BigDecimal getAlturaMetros() { return alturaMetros; }
    public void setAlturaMetros(BigDecimal alturaMetros) { this.alturaMetros = alturaMetros; }
    public BigDecimal getPesoRegistrado() { return pesoRegistrado; }
    public void setPesoRegistrado(BigDecimal pesoRegistrado) { this.pesoRegistrado = pesoRegistrado; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
    public LocalDateTime getCriadaEm() { return criadaEm; }
    public void setCriadaEm(LocalDateTime criadaEm) { this.criadaEm = criadaEm; }
    public LocalDateTime getAtualizadaEm() { return atualizadaEm; }
    public void setAtualizadaEm(LocalDateTime atualizadaEm) { this.atualizadaEm = atualizadaEm; }
    public Usuario getCriadaPor() { return criadaPor; }
    public void setCriadaPor(Usuario criadaPor) { this.criadaPor = criadaPor; }
    public Usuario getAtualizadaPor() { return atualizadaPor; }
    public void setAtualizadaPor(Usuario atualizadaPor) { this.atualizadaPor = atualizadaPor; }
    public Boolean getAtiva() { return ativa; }
    public void setAtiva(Boolean ativa) { this.ativa = ativa; }
}
