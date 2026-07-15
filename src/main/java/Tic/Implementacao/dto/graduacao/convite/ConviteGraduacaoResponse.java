package Tic.Implementacao.dto.graduacao.convite;

import Tic.Implementacao.model.ConviteGraduacao;
import Tic.Implementacao.model.StatusConviteGraduacao;

import java.time.LocalDateTime;

public class ConviteGraduacaoResponse {
    private Long id;
    private Long eventoId;
    private String eventoTitulo;
    private Long alunoId;
    private String alunoNome;
    private Long graduacaoDestinoId;
    private String graduacaoDestinoNome;
    private StatusConviteGraduacao status;
    private LocalDateTime enviadoEm;
    private LocalDateTime visualizadoEm;
    private LocalDateTime respondidoEm;
    private String justificativa;

    public static ConviteGraduacaoResponse from(ConviteGraduacao convite) {
        ConviteGraduacaoResponse response = new ConviteGraduacaoResponse();
        response.id = convite.getId();
        response.eventoId = convite.getEvento().getId();
        response.eventoTitulo = convite.getEvento().getTitulo();
        response.alunoId = convite.getAluno().getId();
        response.alunoNome = convite.getAluno().getNome();
        response.graduacaoDestinoId = convite.getGraduacaoDestino().getId();
        response.graduacaoDestinoNome = convite.getGraduacaoDestino().getNome();
        response.status = convite.getStatus();
        response.enviadoEm = convite.getEnviadoEm();
        response.visualizadoEm = convite.getVisualizadoEm();
        response.respondidoEm = convite.getRespondidoEm();
        response.justificativa = convite.getJustificativa();
        return response;
    }

    public Long getId() { return id; }
    public Long getEventoId() { return eventoId; }
    public String getEventoTitulo() { return eventoTitulo; }
    public Long getAlunoId() { return alunoId; }
    public String getAlunoNome() { return alunoNome; }
    public Long getGraduacaoDestinoId() { return graduacaoDestinoId; }
    public String getGraduacaoDestinoNome() { return graduacaoDestinoNome; }
    public StatusConviteGraduacao getStatus() { return status; }
    public LocalDateTime getEnviadoEm() { return enviadoEm; }
    public LocalDateTime getVisualizadoEm() { return visualizadoEm; }
    public LocalDateTime getRespondidoEm() { return respondidoEm; }
    public String getJustificativa() { return justificativa; }
}
