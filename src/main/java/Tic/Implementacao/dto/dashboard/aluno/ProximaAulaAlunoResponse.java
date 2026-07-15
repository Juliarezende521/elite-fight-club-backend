package Tic.Implementacao.dto.dashboard.aluno;

import java.time.LocalDate;
import java.time.LocalTime;

public class ProximaAulaAlunoResponse {
    private Long aulaId;
    private String tipo;
    private LocalDate data;
    private LocalTime horaInicio;
    private LocalTime horaFim;
    private String modalidade;
    private String professorNome;
    private Boolean reservada;
    private Boolean sessaoCheckInAberta;

    public ProximaAulaAlunoResponse(Long aulaId, String tipo, LocalDate data, LocalTime horaInicio,
                                    LocalTime horaFim, String modalidade, String professorNome,
                                    Boolean reservada, Boolean sessaoCheckInAberta) {
        this.aulaId = aulaId;
        this.tipo = tipo;
        this.data = data;
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
        this.modalidade = modalidade;
        this.professorNome = professorNome;
        this.reservada = reservada;
        this.sessaoCheckInAberta = sessaoCheckInAberta;
    }

    public Long getAulaId() { return aulaId; }
    public String getTipo() { return tipo; }
    public LocalDate getData() { return data; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public LocalTime getHoraFim() { return horaFim; }
    public String getModalidade() { return modalidade; }
    public String getProfessorNome() { return professorNome; }
    public Boolean getReservada() { return reservada; }
    public Boolean getSessaoCheckInAberta() { return sessaoCheckInAberta; }
}
