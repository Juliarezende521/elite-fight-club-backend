package Tic.Implementacao.dto.graduacao.evento;

import Tic.Implementacao.model.EventoGraduacao;
import Tic.Implementacao.model.StatusEventoGraduacao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class EventoGraduacaoResponse {
    private Long id;
    private String titulo;
    private String descricao;
    private LocalDate dataEvento;
    private LocalTime horaInicio;
    private LocalTime horaFim;
    private String local;
    private LocalDateTime inicioInscricoes;
    private LocalDateTime fimInscricoes;
    private StatusEventoGraduacao status;
    private Integer limiteParticipantes;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

    public static EventoGraduacaoResponse from(EventoGraduacao evento) {
        EventoGraduacaoResponse response = new EventoGraduacaoResponse();
        response.id = evento.getId();
        response.titulo = evento.getTitulo();
        response.descricao = evento.getDescricao();
        response.dataEvento = evento.getDataEvento();
        response.horaInicio = evento.getHoraInicio();
        response.horaFim = evento.getHoraFim();
        response.local = evento.getLocal();
        response.inicioInscricoes = evento.getInicioInscricoes();
        response.fimInscricoes = evento.getFimInscricoes();
        response.status = evento.getStatus();
        response.limiteParticipantes = evento.getLimiteParticipantes();
        response.criadoEm = evento.getCriadoEm();
        response.atualizadoEm = evento.getAtualizadoEm();
        return response;
    }

    public Long getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getDescricao() { return descricao; }
    public LocalDate getDataEvento() { return dataEvento; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public LocalTime getHoraFim() { return horaFim; }
    public String getLocal() { return local; }
    public LocalDateTime getInicioInscricoes() { return inicioInscricoes; }
    public LocalDateTime getFimInscricoes() { return fimInscricoes; }
    public StatusEventoGraduacao getStatus() { return status; }
    public Integer getLimiteParticipantes() { return limiteParticipantes; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }
}
