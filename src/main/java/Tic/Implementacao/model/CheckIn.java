package Tic.Implementacao.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "checkins",
        uniqueConstraints = @UniqueConstraint(name = "uk_checkin_ativo", columnNames = "chave_checkin_ativo")
)
public class CheckIn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    @ManyToOne
    @JoinColumn(name = "aluno_id")
    private Aluno aluno;

    @ManyToOne
    @JoinColumn(name = "turma_id")
    private Turma turma;

    @ManyToOne
    @JoinColumn(name = "professor_id")
    private Professor professor;

    @ManyToOne
    @JoinColumn(name = "aula_turma_id")
    private AulaTurma aulaTurma;

    @ManyToOne
    @JoinColumn(name = "aula_personal_id")
    private AulaPersonal aulaPersonal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrigemCheckIn origem = OrigemCheckIn.PROFESSOR;

    @ManyToOne
    @JoinColumn(name = "academia_id")
    private Academia academia;

    @ManyToOne
    @JoinColumn(name = "confirmado_por_id")
    private Usuario confirmadoPor;

    @Column(nullable = false)
    private Boolean ativo = true;

    private LocalDateTime invalidadoEm;

    @Column(length = 500)
    private String observacao;

    @Column(name = "chave_checkin_ativo", unique = true, length = 80)
    private String chaveCheckInAtivo;

    public CheckIn() {
    }

    public CheckIn(LocalDateTime dataHora, Aluno aluno, Turma turma, Professor professor, OrigemCheckIn origem) {
        this.dataHora = dataHora;
        this.aluno = aluno;
        this.turma = turma;
        this.professor = professor;
        this.origem = origem;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public Turma getTurma() {
        return turma;
    }

    public void setTurma(Turma turma) {
        this.turma = turma;
    }

    public Professor getProfessor() {
        return professor;
    }

    public void setProfessor(Professor professor) {
        this.professor = professor;
    }

    public OrigemCheckIn getOrigem() {
        return origem;
    }

    public void setOrigem(OrigemCheckIn origem) {
        this.origem = origem;
    }

    public Aluno getAluno() {
        return aluno;
    }

    public void setAluno(Aluno aluno) {
        this.aluno = aluno;
    }

    public Academia getAcademia() {
        return academia;
    }

    public void setAcademia(Academia academia) {
        this.academia = academia;
    }

    public AulaTurma getAulaTurma() {
        return aulaTurma;
    }

    public void setAulaTurma(AulaTurma aulaTurma) {
        this.aulaTurma = aulaTurma;
    }

    public AulaPersonal getAulaPersonal() {
        return aulaPersonal;
    }

    public void setAulaPersonal(AulaPersonal aulaPersonal) {
        this.aulaPersonal = aulaPersonal;
    }

    public Usuario getConfirmadoPor() {
        return confirmadoPor;
    }

    public void setConfirmadoPor(Usuario confirmadoPor) {
        this.confirmadoPor = confirmadoPor;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public LocalDateTime getInvalidadoEm() {
        return invalidadoEm;
    }

    public void setInvalidadoEm(LocalDateTime invalidadoEm) {
        this.invalidadoEm = invalidadoEm;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public String getChaveCheckInAtivo() {
        return chaveCheckInAtivo;
    }

    public void setChaveCheckInAtivo(String chaveCheckInAtivo) {
        this.chaveCheckInAtivo = chaveCheckInAtivo;
    }
}
