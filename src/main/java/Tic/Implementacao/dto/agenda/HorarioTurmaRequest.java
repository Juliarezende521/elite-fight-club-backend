package Tic.Implementacao.dto.agenda;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;

public class HorarioTurmaRequest {

    @NotNull(message = "Professor é obrigatório")
    private Long professorId;

    @NotNull(message = "Modalidade é obrigatória")
    private Long modalidadeId;

    @NotNull(message = "Dia da semana é obrigatório")
    private DayOfWeek diaSemana;

    @NotNull(message = "Hora inicial é obrigatória")
    private LocalTime horaInicial;

    @NotNull(message = "Hora final é obrigatória")
    private LocalTime horaFinal;

    @NotNull(message = "Capacidade máxima é obrigatória")
    @Min(value = 1, message = "Capacidade máxima deve ser maior que zero")
    private Integer capacidadeMaxima;

    public Long getProfessorId() { return professorId; }
    public void setProfessorId(Long professorId) { this.professorId = professorId; }
    public Long getModalidadeId() { return modalidadeId; }
    public void setModalidadeId(Long modalidadeId) { this.modalidadeId = modalidadeId; }
    public DayOfWeek getDiaSemana() { return diaSemana; }
    public void setDiaSemana(DayOfWeek diaSemana) { this.diaSemana = diaSemana; }
    public LocalTime getHoraInicial() { return horaInicial; }
    public void setHoraInicial(LocalTime horaInicial) { this.horaInicial = horaInicial; }
    public LocalTime getHoraFinal() { return horaFinal; }
    public void setHoraFinal(LocalTime horaFinal) { this.horaFinal = horaFinal; }
    public Integer getCapacidadeMaxima() { return capacidadeMaxima; }
    public void setCapacidadeMaxima(Integer capacidadeMaxima) { this.capacidadeMaxima = capacidadeMaxima; }
}
