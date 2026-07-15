package Tic.Implementacao.dto.dashboard.aluno;

import java.time.LocalDate;
import java.time.LocalTime;

public class AulaAlunoResponse {
    private Long aulaId;
    private String tipo;
    private LocalDate data;
    private LocalTime horaInicio;
    private LocalTime horaFim;
    private String status;
    private String modalidade;
    private String professorNome;
    private Boolean reservada;
    private Boolean checkInRealizado;
    private String statusPresenca;

    public AulaAlunoResponse(Long aulaId, String tipo, LocalDate data, LocalTime horaInicio, LocalTime horaFim,
                             String status, String modalidade, String professorNome, Boolean reservada,
                             Boolean checkInRealizado, String statusPresenca) {
        this.aulaId = aulaId;
        this.tipo = tipo;
        this.data = data;
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
        this.status = status;
        this.modalidade = modalidade;
        this.professorNome = professorNome;
        this.reservada = reservada;
        this.checkInRealizado = checkInRealizado;
        this.statusPresenca = statusPresenca;
    }

    public Long getAulaId() { return aulaId; }
    public String getTipo() { return tipo; }
    public LocalDate getData() { return data; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public LocalTime getHoraFim() { return horaFim; }
    public String getStatus() { return status; }
    public String getModalidade() { return modalidade; }
    public String getProfessorNome() { return professorNome; }
    public Boolean getReservada() { return reservada; }
    public Boolean getCheckInRealizado() { return checkInRealizado; }
    public String getStatusPresenca() { return statusPresenca; }
}
