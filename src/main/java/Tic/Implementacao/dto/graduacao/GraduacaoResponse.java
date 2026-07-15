package Tic.Implementacao.dto.graduacao;

import Tic.Implementacao.model.Graduacao;

import java.math.BigDecimal;

public class GraduacaoResponse {

    private Long id;
    private String nome;
    private String corHex;
    private Integer ordem;
    private Integer checkinsNecessarios;
    private Integer tempoMinimoMeses;
    private BigDecimal valorPadraoGraduacao;
    private String descricao;
    private Boolean graduacaoBase;
    private Boolean ativa;
    private Long academiaId;

    public GraduacaoResponse(Long id, String nome, String corHex, Integer ordem, Integer checkinsNecessarios,
                             Integer tempoMinimoMeses, BigDecimal valorPadraoGraduacao, String descricao,
                             Boolean graduacaoBase, Boolean ativa, Long academiaId) {
        this.id = id;
        this.nome = nome;
        this.corHex = corHex;
        this.ordem = ordem;
        this.checkinsNecessarios = checkinsNecessarios;
        this.tempoMinimoMeses = tempoMinimoMeses;
        this.valorPadraoGraduacao = valorPadraoGraduacao;
        this.descricao = descricao;
        this.graduacaoBase = graduacaoBase;
        this.ativa = ativa;
        this.academiaId = academiaId;
    }

    public static GraduacaoResponse from(Graduacao graduacao) {
        Long academiaId = graduacao.getAcademia() == null ? null : graduacao.getAcademia().getId();
        return new GraduacaoResponse(graduacao.getId(), graduacao.getNome(), graduacao.getCorHex(), graduacao.getOrdem(),
                graduacao.getCheckinsNecessarios(), graduacao.getTempoMinimoMeses(), graduacao.getValorPadraoGraduacao(),
                graduacao.getDescricao(), graduacao.getGraduacaoBase(), graduacao.getAtiva(), academiaId);
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getCorHex() { return corHex; }
    public Integer getOrdem() { return ordem; }
    public Integer getCheckinsNecessarios() { return checkinsNecessarios; }
    public Integer getTempoMinimoMeses() { return tempoMinimoMeses; }
    public BigDecimal getValorPadraoGraduacao() { return valorPadraoGraduacao; }
    public String getDescricao() { return descricao; }
    public Boolean getGraduacaoBase() { return graduacaoBase; }
    public Boolean getAtiva() { return ativa; }
    public Long getAcademiaId() { return academiaId; }
}
