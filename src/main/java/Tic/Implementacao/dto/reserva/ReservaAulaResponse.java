package Tic.Implementacao.dto.reserva;

import Tic.Implementacao.model.StatusReserva;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class ReservaAulaResponse {

    private Long id;
    private Long aulaId;
    private Long alunoId;
    private String alunoNome;
    private LocalDate data;
    private LocalTime horaInicio;
    private LocalTime horaFim;
    private StatusReserva status;
    private LocalDateTime reservadoEm;
    private LocalDateTime canceladoEm;

    public ReservaAulaResponse(Long id, Long aulaId, Long alunoId, String alunoNome, LocalDate data,
                               LocalTime horaInicio, LocalTime horaFim, StatusReserva status,
                               LocalDateTime reservadoEm, LocalDateTime canceladoEm) {
        this.id = id;
        this.aulaId = aulaId;
        this.alunoId = alunoId;
        this.alunoNome = alunoNome;
        this.data = data;
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
        this.status = status;
        this.reservadoEm = reservadoEm;
        this.canceladoEm = canceladoEm;
    }

    public Long getId() { return id; }
    public Long getAulaId() { return aulaId; }
    public Long getAlunoId() { return alunoId; }
    public String getAlunoNome() { return alunoNome; }
    public LocalDate getData() { return data; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public LocalTime getHoraFim() { return horaFim; }
    public StatusReserva getStatus() { return status; }
    public LocalDateTime getReservadoEm() { return reservadoEm; }
    public LocalDateTime getCanceladoEm() { return canceladoEm; }
}
