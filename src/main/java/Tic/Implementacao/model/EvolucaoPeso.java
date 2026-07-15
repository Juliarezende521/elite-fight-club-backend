package Tic.Implementacao.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "evolucao_peso",
        indexes = {
                @Index(name = "idx_evolucao_peso_academia_aluno_data", columnList = "academia_id, aluno_id, data_registro"),
                @Index(name = "idx_evolucao_peso_aluno_ativo", columnList = "aluno_id, ativo")
        }
)
public class EvolucaoPeso {

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
    private BigDecimal peso;

    @Column(nullable = false)
    private LocalDate dataRegistro;

    @Column(length = 1000)
    private String observacao;

    @Column(nullable = false)
    private LocalDateTime criadoEm;

    private LocalDateTime atualizadoEm;

    @ManyToOne(optional = false)
    @JoinColumn(name = "criado_por_id", nullable = false)
    private Usuario criadoPor;

    @ManyToOne
    @JoinColumn(name = "atualizado_por_id")
    private Usuario atualizadoPor;

    @Column(nullable = false)
    private Boolean ativo = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Academia getAcademia() { return academia; }
    public void setAcademia(Academia academia) { this.academia = academia; }
    public Aluno getAluno() { return aluno; }
    public void setAluno(Aluno aluno) { this.aluno = aluno; }
    public BigDecimal getPeso() { return peso; }
    public void setPeso(BigDecimal peso) { this.peso = peso; }
    public LocalDate getDataRegistro() { return dataRegistro; }
    public void setDataRegistro(LocalDate dataRegistro) { this.dataRegistro = dataRegistro; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }
    public void setAtualizadoEm(LocalDateTime atualizadoEm) { this.atualizadoEm = atualizadoEm; }
    public Usuario getCriadoPor() { return criadoPor; }
    public void setCriadoPor(Usuario criadoPor) { this.criadoPor = criadoPor; }
    public Usuario getAtualizadoPor() { return atualizadoPor; }
    public void setAtualizadoPor(Usuario atualizadoPor) { this.atualizadoPor = atualizadoPor; }
    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
}
