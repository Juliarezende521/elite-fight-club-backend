package Tic.Implementacao.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(
        name = "aulas_personal",
        uniqueConstraints = @UniqueConstraint(name = "uk_aula_personal_agenda_data", columnNames = {"agenda_personal_id", "data"})
)
public class AulaPersonal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "academia_id", nullable = false)
    private Academia academia;

    @ManyToOne
    @JoinColumn(name = "agenda_personal_id", nullable = false)
    private AgendaPersonal agendaPersonal;

    @ManyToOne
    @JoinColumn(name = "aluno_id", nullable = false)
    private Aluno aluno;

    @ManyToOne
    @JoinColumn(name = "professor_id", nullable = false)
    private Professor professor;

    @Column(nullable = false)
    private LocalDate data;

    @Column(nullable = false)
    private LocalTime horaInicio;

    @Column(nullable = false)
    private LocalTime horaFim;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusAula status = StatusAula.AGENDADA;

    @Column(length = 500)
    private String observacao;

    @Column(nullable = false)
    private LocalDateTime criadoEm;

    @Column(nullable = false)
    private LocalDateTime atualizadoEm;

    @ManyToOne
    @JoinColumn(name = "aula_original_id")
    private AulaPersonal aulaOriginal;

    public AulaPersonal() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Academia getAcademia() { return academia; }
    public void setAcademia(Academia academia) { this.academia = academia; }
    public AgendaPersonal getAgendaPersonal() { return agendaPersonal; }
    public void setAgendaPersonal(AgendaPersonal agendaPersonal) { this.agendaPersonal = agendaPersonal; }
    public Aluno getAluno() { return aluno; }
    public void setAluno(Aluno aluno) { this.aluno = aluno; }
    public Professor getProfessor() { return professor; }
    public void setProfessor(Professor professor) { this.professor = professor; }
    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }
    public LocalTime getHoraFim() { return horaFim; }
    public void setHoraFim(LocalTime horaFim) { this.horaFim = horaFim; }
    public StatusAula getStatus() { return status; }
    public void setStatus(StatusAula status) { this.status = status; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }
    public void setAtualizadoEm(LocalDateTime atualizadoEm) { this.atualizadoEm = atualizadoEm; }
    public AulaPersonal getAulaOriginal() { return aulaOriginal; }
    public void setAulaOriginal(AulaPersonal aulaOriginal) { this.aulaOriginal = aulaOriginal; }
}
