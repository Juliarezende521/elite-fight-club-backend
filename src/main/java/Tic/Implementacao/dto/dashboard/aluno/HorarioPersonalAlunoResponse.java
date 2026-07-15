package Tic.Implementacao.dto.dashboard.aluno;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

public class HorarioPersonalAlunoResponse {
    private Long agendaPersonalId;
    private DayOfWeek diaSemana;
    private LocalTime horaInicio;
    private LocalTime horaFim;
    private String professorNome;
    private LocalDate dataInicio;
    private Boolean ativo;

    public HorarioPersonalAlunoResponse(Long agendaPersonalId, DayOfWeek diaSemana, LocalTime horaInicio,
                                        LocalTime horaFim, String professorNome, LocalDate dataInicio, Boolean ativo) {
        this.agendaPersonalId = agendaPersonalId;
        this.diaSemana = diaSemana;
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
        this.professorNome = professorNome;
        this.dataInicio = dataInicio;
        this.ativo = ativo;
    }

    public Long getAgendaPersonalId() { return agendaPersonalId; }
    public DayOfWeek getDiaSemana() { return diaSemana; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public LocalTime getHoraFim() { return horaFim; }
    public String getProfessorNome() { return professorNome; }
    public LocalDate getDataInicio() { return dataInicio; }
    public Boolean getAtivo() { return ativo; }
}
