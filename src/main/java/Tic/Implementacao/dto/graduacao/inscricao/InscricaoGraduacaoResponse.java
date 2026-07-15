package Tic.Implementacao.dto.graduacao.inscricao;

import Tic.Implementacao.model.InscricaoGraduacao;
import Tic.Implementacao.model.StatusInscricaoGraduacao;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class InscricaoGraduacaoResponse {
    private Long id;
    private Long eventoId;
    private String eventoTitulo;
    private Long alunoId;
    private String alunoNome;
    private Long graduacaoDestinoId;
    private String graduacaoDestinoNome;
    private BigDecimal valor;
    private StatusInscricaoGraduacao status;
    private LocalDateTime inscritoEm;
    private LocalDateTime canceladoEm;
    private String observacao;

    public static InscricaoGraduacaoResponse from(InscricaoGraduacao inscricao) {
        InscricaoGraduacaoResponse response = new InscricaoGraduacaoResponse();
        response.id = inscricao.getId();
        response.eventoId = inscricao.getEvento().getId();
        response.eventoTitulo = inscricao.getEvento().getTitulo();
        response.alunoId = inscricao.getAluno().getId();
        response.alunoNome = inscricao.getAluno().getNome();
        response.graduacaoDestinoId = inscricao.getGraduacaoDestino().getId();
        response.graduacaoDestinoNome = inscricao.getGraduacaoDestino().getNome();
        response.valor = inscricao.getValor();
        response.status = inscricao.getStatus();
        response.inscritoEm = inscricao.getInscritoEm();
        response.canceladoEm = inscricao.getCanceladoEm();
        response.observacao = inscricao.getObservacao();
        return response;
    }

    public Long getId() { return id; }
    public Long getEventoId() { return eventoId; }
    public String getEventoTitulo() { return eventoTitulo; }
    public Long getAlunoId() { return alunoId; }
    public String getAlunoNome() { return alunoNome; }
    public Long getGraduacaoDestinoId() { return graduacaoDestinoId; }
    public String getGraduacaoDestinoNome() { return graduacaoDestinoNome; }
    public BigDecimal getValor() { return valor; }
    public StatusInscricaoGraduacao getStatus() { return status; }
    public LocalDateTime getInscritoEm() { return inscritoEm; }
    public LocalDateTime getCanceladoEm() { return canceladoEm; }
    public String getObservacao() { return observacao; }
}
