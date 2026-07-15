package Tic.Implementacao.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "presencas_aula",
        indexes = {
                @Index(name = "idx_presenca_turma", columnList = "academia_id,aula_turma_id"),
                @Index(name = "idx_presenca_personal", columnList = "academia_id,aula_personal_id"),
                @Index(name = "idx_presenca_aluno", columnList = "academia_id,aluno_id")
        },
        uniqueConstraints = @UniqueConstraint(name = "uk_presenca_aula_aluno", columnNames = "chave_presenca")
)
public class PresencaAula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "academia_id", nullable = false)
    private Academia academia;

    @ManyToOne(optional = false)
    @JoinColumn(name = "aluno_id", nullable = false)
    private Aluno aluno;

    @ManyToOne
    @JoinColumn(name = "aula_turma_id")
    private AulaTurma aulaTurma;

    @ManyToOne
    @JoinColumn(name = "aula_personal_id")
    private AulaPersonal aulaPersonal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusPresenca status = StatusPresenca.PENDENTE;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_anterior", length = 30)
    private StatusPresenca statusAnterior;

    @Column(nullable = false)
    private LocalDateTime registradoEm;

    @ManyToOne
    @JoinColumn(name = "registrado_por_id")
    private Usuario registradoPor;

    @Column(length = 500)
    private String observacao;

    @Column(name = "chave_presenca", nullable = false, unique = true, length = 80)
    private String chavePresenca;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Academia getAcademia() { return academia; }
    public void setAcademia(Academia academia) { this.academia = academia; }
    public Aluno getAluno() { return aluno; }
    public void setAluno(Aluno aluno) { this.aluno = aluno; }
    public AulaTurma getAulaTurma() { return aulaTurma; }
    public void setAulaTurma(AulaTurma aulaTurma) { this.aulaTurma = aulaTurma; }
    public AulaPersonal getAulaPersonal() { return aulaPersonal; }
    public void setAulaPersonal(AulaPersonal aulaPersonal) { this.aulaPersonal = aulaPersonal; }
    public StatusPresenca getStatus() { return status; }
    public void setStatus(StatusPresenca status) { this.status = status; }
    public StatusPresenca getStatusAnterior() { return statusAnterior; }
    public void setStatusAnterior(StatusPresenca statusAnterior) { this.statusAnterior = statusAnterior; }
    public LocalDateTime getRegistradoEm() { return registradoEm; }
    public void setRegistradoEm(LocalDateTime registradoEm) { this.registradoEm = registradoEm; }
    public Usuario getRegistradoPor() { return registradoPor; }
    public void setRegistradoPor(Usuario registradoPor) { this.registradoPor = registradoPor; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
    public String getChavePresenca() { return chavePresenca; }
    public void setChavePresenca(String chavePresenca) { this.chavePresenca = chavePresenca; }
}
