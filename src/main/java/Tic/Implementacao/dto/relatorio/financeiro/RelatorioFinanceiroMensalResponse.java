package Tic.Implementacao.dto.relatorio.financeiro;

import java.time.LocalDate;
import java.util.List;

public class RelatorioFinanceiroMensalResponse {
    private Integer ano;
    private Integer mes;
    private String competencia;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private ResumoFinanceiroMensalResponse resumo;
    private List<ResumoFormaPagamentoMensalResponse> porFormaPagamento;
    private List<ResumoTipoPlanoMensalResponse> porTipoPlano;
    private List<AlunoFinanceiroMensalResponse> alunos;

    public RelatorioFinanceiroMensalResponse(Integer ano, Integer mes, String competencia, LocalDate dataInicio,
                                             LocalDate dataFim, ResumoFinanceiroMensalResponse resumo,
                                             List<ResumoFormaPagamentoMensalResponse> porFormaPagamento,
                                             List<ResumoTipoPlanoMensalResponse> porTipoPlano,
                                             List<AlunoFinanceiroMensalResponse> alunos) {
        this.ano = ano;
        this.mes = mes;
        this.competencia = competencia;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.resumo = resumo;
        this.porFormaPagamento = porFormaPagamento;
        this.porTipoPlano = porTipoPlano;
        this.alunos = alunos;
    }

    public Integer getAno() { return ano; }
    public Integer getMes() { return mes; }
    public String getCompetencia() { return competencia; }
    public LocalDate getDataInicio() { return dataInicio; }
    public LocalDate getDataFim() { return dataFim; }
    public ResumoFinanceiroMensalResponse getResumo() { return resumo; }
    public List<ResumoFormaPagamentoMensalResponse> getPorFormaPagamento() { return porFormaPagamento; }
    public List<ResumoTipoPlanoMensalResponse> getPorTipoPlano() { return porTipoPlano; }
    public List<AlunoFinanceiroMensalResponse> getAlunos() { return alunos; }
}
