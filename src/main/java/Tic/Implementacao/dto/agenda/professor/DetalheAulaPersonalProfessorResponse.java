package Tic.Implementacao.dto.agenda.professor;

import Tic.Implementacao.model.StatusAula;

import java.time.LocalDate;
import java.time.LocalTime;

public class DetalheAulaPersonalProfessorResponse {

    private Long aulaId;
    private Long agendaPersonalId;
    private LocalDate data;
    private LocalTime horaInicio;
    private LocalTime horaFim;
    private StatusAula status;
    private Long professorId;
    private String professorNome;
    private Long alunoId;
    private String alunoNome;
    private Long aulaOriginalId;

    public DetalheAulaPersonalProfessorResponse(Long aulaId, Long agendaPersonalId, LocalDate data,
                                                LocalTime horaInicio, LocalTime horaFim, StatusAula status,
                                                Long professorId, String professorNome, Long alunoId,
                                                String alunoNome, Long aulaOriginalId) {
        this.aulaId = aulaId;
        this.agendaPersonalId = agendaPersonalId;
        this.data = data;
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
        this.status = status;
        this.professorId = professorId;
        this.professorNome = professorNome;
        this.alunoId = alunoId;
        this.alunoNome = alunoNome;
        this.aulaOriginalId = aulaOriginalId;
    }

    public Long getAulaId() { return aulaId; }
    public Long getAgendaPersonalId() { return agendaPersonalId; }
    public LocalDate getData() { return data; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public LocalTime getHoraFim() { return horaFim; }
    public StatusAula getStatus() { return status; }
    public Long getProfessorId() { return professorId; }
    public String getProfessorNome() { return professorNome; }
    public Long getAlunoId() { return alunoId; }
    public String getAlunoNome() { return alunoNome; }
    public Long getAulaOriginalId() { return aulaOriginalId; }
}
