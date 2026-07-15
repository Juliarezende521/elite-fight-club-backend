package Tic.Implementacao.dto.checkinqr;

import Tic.Implementacao.model.TipoItemAgenda;

import java.time.LocalTime;

public class OpcaoCheckInResponse {
    private Long sessaoId;
    private TipoItemAgenda tipo;
    private Long aulaId;
    private String modalidade;
    private String alunoPersonal;
    private LocalTime horaInicio;
    private LocalTime horaFim;

    public OpcaoCheckInResponse(Long sessaoId, TipoItemAgenda tipo, Long aulaId, String modalidade,
                                String alunoPersonal, LocalTime horaInicio, LocalTime horaFim) {
        this.sessaoId = sessaoId;
        this.tipo = tipo;
        this.aulaId = aulaId;
        this.modalidade = modalidade;
        this.alunoPersonal = alunoPersonal;
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
    }

    public Long getSessaoId() { return sessaoId; }
    public TipoItemAgenda getTipo() { return tipo; }
    public Long getAulaId() { return aulaId; }
    public String getModalidade() { return modalidade; }
    public String getAlunoPersonal() { return alunoPersonal; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public LocalTime getHoraFim() { return horaFim; }
}
