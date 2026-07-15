package Tic.Implementacao.dto.graduacao.evento;

import Tic.Implementacao.model.EventoGraduacaoPrajied;

import java.math.BigDecimal;

public class EventoGraduacaoPrajiedResponse {
    private Long id;
    private Long graduacaoDestinoId;
    private String graduacaoDestinoNome;
    private BigDecimal valor;
    private Integer limiteVagas;
    private Boolean ativo;

    public static EventoGraduacaoPrajiedResponse from(EventoGraduacaoPrajied item) {
        EventoGraduacaoPrajiedResponse response = new EventoGraduacaoPrajiedResponse();
        response.id = item.getId();
        response.graduacaoDestinoId = item.getGraduacaoDestino().getId();
        response.graduacaoDestinoNome = item.getGraduacaoDestino().getNome();
        response.valor = item.getValor();
        response.limiteVagas = item.getLimiteVagas();
        response.ativo = item.getAtivo();
        return response;
    }

    public Long getId() { return id; }
    public Long getGraduacaoDestinoId() { return graduacaoDestinoId; }
    public String getGraduacaoDestinoNome() { return graduacaoDestinoNome; }
    public BigDecimal getValor() { return valor; }
    public Integer getLimiteVagas() { return limiteVagas; }
    public Boolean getAtivo() { return ativo; }
}
