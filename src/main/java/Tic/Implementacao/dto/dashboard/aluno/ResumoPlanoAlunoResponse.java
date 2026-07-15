package Tic.Implementacao.dto.dashboard.aluno;

import Tic.Implementacao.model.TipoPlano;

import java.math.BigDecimal;

public class ResumoPlanoAlunoResponse {
    private Long planoId;
    private String nome;
    private TipoPlano tipo;
    private Integer aulasPorSemana;
    private BigDecimal valor;
    private Boolean ativo;
    private Integer reservasConfirmadasNaSemana;
    private Integer aulasRestantesNaSemana;

    public ResumoPlanoAlunoResponse(Long planoId, String nome, TipoPlano tipo, Integer aulasPorSemana,
                                    BigDecimal valor, Boolean ativo, Integer reservasConfirmadasNaSemana,
                                    Integer aulasRestantesNaSemana) {
        this.planoId = planoId;
        this.nome = nome;
        this.tipo = tipo;
        this.aulasPorSemana = aulasPorSemana;
        this.valor = valor;
        this.ativo = ativo;
        this.reservasConfirmadasNaSemana = reservasConfirmadasNaSemana;
        this.aulasRestantesNaSemana = aulasRestantesNaSemana;
    }

    public Long getPlanoId() { return planoId; }
    public String getNome() { return nome; }
    public TipoPlano getTipo() { return tipo; }
    public Integer getAulasPorSemana() { return aulasPorSemana; }
    public BigDecimal getValor() { return valor; }
    public Boolean getAtivo() { return ativo; }
    public Integer getReservasConfirmadasNaSemana() { return reservasConfirmadasNaSemana; }
    public Integer getAulasRestantesNaSemana() { return aulasRestantesNaSemana; }
}
