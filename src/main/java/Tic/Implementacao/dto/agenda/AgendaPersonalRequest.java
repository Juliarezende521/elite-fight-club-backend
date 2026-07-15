package Tic.Implementacao.dto.agenda;

import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

public class AgendaPersonalRequest {

    @NotNull(message = "Aluno é obrigatório")
    private Long alunoId;

    @NotNull(message = "Professor é obrigatório")
    private Long professorId;

    @NotNull(message = "Dia da semana é obrigatório")
    private DayOfWeek diaSemana;

    @NotNull(message = "Hora inicial é obrigatória")
    private LocalTime horaInicial;

    @NotNull(message = "Hora final é obrigatória")
    private LocalTime horaFinal;

    @NotNull(message = "Data de início é obrigatória")
    private LocalDate dataInicio;

    public Long getAlunoId() { return alunoId; }
    public void setAlunoId(Long alunoId) { this.alunoId = alunoId; }
    public Long getProfessorId() { return professorId; }
    public void setProfessorId(Long professorId) { this.professorId = professorId; }
    public DayOfWeek getDiaSemana() { return diaSemana; }
    public void setDiaSemana(DayOfWeek diaSemana) { this.diaSemana = diaSemana; }
    public LocalTime getHoraInicial() { return horaInicial; }
    public void setHoraInicial(LocalTime horaInicial) { this.horaInicial = horaInicial; }
    public LocalTime getHoraFinal() { return horaFinal; }
    public void setHoraFinal(LocalTime horaFinal) { this.horaFinal = horaFinal; }
    public LocalDate getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDate dataInicio) { this.dataInicio = dataInicio; }
}
