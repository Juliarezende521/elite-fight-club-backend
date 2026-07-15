package Tic.Implementacao.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(
        name = "aulas_turma",
        uniqueConstraints = @UniqueConstraint(name = "uk_aula_turma_horario_data", columnNames = {"horario_turma_id", "data"})
)
public class AulaTurma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "academia_id", nullable = false)
    private Academia academia;

    @ManyToOne
    @JoinColumn(name = "horario_turma_id", nullable = false)
    private HorarioTurma horarioTurma;

    @ManyToOne
    @JoinColumn(name = "professor_id", nullable = false)
    private Professor professor;

    @ManyToOne
    @JoinColumn(name = "modalidade_id", nullable = false)
    private Modalidade modalidade;

    @Column(nullable = false)
    private LocalDate data;

    @Column(nullable = false)
    private LocalTime horaInicio;

    @Column(nullable = false)
    private LocalTime horaFim;

    @Column(nullable = false)
    private Integer capacidadeMaxima;

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
    private AulaTurma aulaOriginal;

    public AulaTurma() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Academia getAcademia() { return academia; }
    public void setAcademia(Academia academia) { this.academia = academia; }
    public HorarioTurma getHorarioTurma() { return horarioTurma; }
    public void setHorarioTurma(HorarioTurma horarioTurma) { this.horarioTurma = horarioTurma; }
    public Professor getProfessor() { return professor; }
    public void setProfessor(Professor professor) { this.professor = professor; }
    public Modalidade getModalidade() { return modalidade; }
    public void setModalidade(Modalidade modalidade) { this.modalidade = modalidade; }
    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }
    public LocalTime getHoraFim() { return horaFim; }
    public void setHoraFim(LocalTime horaFim) { this.horaFim = horaFim; }
    public Integer getCapacidadeMaxima() { return capacidadeMaxima; }
    public void setCapacidadeMaxima(Integer capacidadeMaxima) { this.capacidadeMaxima = capacidadeMaxima; }
    public StatusAula getStatus() { return status; }
    public void setStatus(StatusAula status) { this.status = status; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }
    public void setAtualizadoEm(LocalDateTime atualizadoEm) { this.atualizadoEm = atualizadoEm; }
    public AulaTurma getAulaOriginal() { return aulaOriginal; }
    public void setAulaOriginal(AulaTurma aulaOriginal) { this.aulaOriginal = aulaOriginal; }
}
