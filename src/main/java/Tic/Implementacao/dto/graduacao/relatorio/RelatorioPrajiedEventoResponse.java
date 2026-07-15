package Tic.Implementacao.dto.graduacao.relatorio;

import java.math.BigDecimal;

public class RelatorioPrajiedEventoResponse {

    private Long graduacaoId;
    private String graduacaoNome;
    private String corHex;
    private BigDecimal valorUnitario;
    private Integer convidados;
    private Integer inscritos;
    private Integer pagos;
    private Integer presentes;
    private Integer aprovados;
    private Integer reprovados;
    private Integer promovidos;
    private BigDecimal receitaBruta;
    private BigDecimal receitaLiquida;

    public RelatorioPrajiedEventoResponse(Long graduacaoId, String graduacaoNome, String corHex,
                                          BigDecimal valorUnitario, Integer convidados, Integer inscritos,
                                          Integer pagos, Integer presentes, Integer aprovados,
                                          Integer reprovados, Integer promovidos, BigDecimal receitaBruta,
                                          BigDecimal receitaLiquida) {
        this.graduacaoId = graduacaoId;
        this.graduacaoNome = graduacaoNome;
        this.corHex = corHex;
        this.valorUnitario = valorUnitario;
        this.convidados = convidados;
        this.inscritos = inscritos;
        this.pagos = pagos;
        this.presentes = presentes;
        this.aprovados = aprovados;
        this.reprovados = reprovados;
        this.promovidos = promovidos;
        this.receitaBruta = receitaBruta;
        this.receitaLiquida = receitaLiquida;
    }

    public Long getGraduacaoId() { return graduacaoId; }
    public String getGraduacaoNome() { return graduacaoNome; }
    public String getCorHex() { return corHex; }
    public BigDecimal getValorUnitario() { return valorUnitario; }
    public Integer getConvidados() { return convidados; }
    public Integer getInscritos() { return inscritos; }
    public Integer getPagos() { return pagos; }
    public Integer getPresentes() { return presentes; }
    public Integer getAprovados() { return aprovados; }
    public Integer getReprovados() { return reprovados; }
    public Integer getPromovidos() { return promovidos; }
    public BigDecimal getReceitaBruta() { return receitaBruta; }
    public BigDecimal getReceitaLiquida() { return receitaLiquida; }
}
