package Tic.Implementacao.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity
@Table(name = "horarios_turma")
public class HorarioTurma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "academia_id", nullable = false)
    private Academia academia;

    @NotNull(message = "Professor é obrigatório")
    @ManyToOne
    @JoinColumn(name = "professor_id", nullable = false)
    private Professor professor;

    @NotNull(message = "Modalidade é obrigatória")
    @ManyToOne
    @JoinColumn(name = "modalidade_id", nullable = false)
    private Modalidade modalidade;

    @NotNull(message = "Dia da semana é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DayOfWeek diaSemana;

    @NotNull(message = "Hora inicial é obrigatória")
    @Column(nullable = false)
    private LocalTime horaInicial;

    @NotNull(message = "Hora final é obrigatória")
    @Column(nullable = false)
    private LocalTime horaFinal;

    @Min(value = 1, message = "Capacidade máxima deve ser maior que zero")
    @Column(nullable = false)
    private Integer capacidadeMaxima;

    @Column(nullable = false)
    private Boolean ativo = true;

    public HorarioTurma() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Academia getAcademia() { return academia; }
    public void setAcademia(Academia academia) { this.academia = academia; }
    public Professor getProfessor() { return professor; }
    public void setProfessor(Professor professor) { this.professor = professor; }
    public Modalidade getModalidade() { return modalidade; }
    public void setModalidade(Modalidade modalidade) { this.modalidade = modalidade; }
    public DayOfWeek getDiaSemana() { return diaSemana; }
    public void setDiaSemana(DayOfWeek diaSemana) { this.diaSemana = diaSemana; }
    public LocalTime getHoraInicial() { return horaInicial; }
    public void setHoraInicial(LocalTime horaInicial) { this.horaInicial = horaInicial; }
    public LocalTime getHoraFinal() { return horaFinal; }
    public void setHoraFinal(LocalTime horaFinal) { this.horaFinal = horaFinal; }
    public Integer getCapacidadeMaxima() { return capacidadeMaxima; }
    public void setCapacidadeMaxima(Integer capacidadeMaxima) { this.capacidadeMaxima = capacidadeMaxima; }
    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
}
