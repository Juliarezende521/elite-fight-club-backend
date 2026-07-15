package Tic.Implementacao.dto.relatorio.financeiro.anual;

import java.time.LocalDate;
import java.util.List;

public class RelatorioFinanceiroAnualResponse {
    private Integer ano;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private ResumoFinanceiroAnualResponse resumo;
    private List<ResumoFinanceiroMesResponse> meses;
    private List<ResumoFormaPagamentoAnualResponse> porFormaPagamento;
    private List<ResumoTipoPlanoAnualResponse> porTipoPlano;
    private List<AlunoInadimplenciaAnualResponse> maioresInadimplentes;
    private Integer melhorMesReceita;
    private Integer piorMesReceita;
    private Integer mesMaiorInadimplencia;

    public RelatorioFinanceiroAnualResponse(Integer ano, LocalDate dataInicio, LocalDate dataFim,
                                            ResumoFinanceiroAnualResponse resumo,
                                            List<ResumoFinanceiroMesResponse> meses,
                                            List<ResumoFormaPagamentoAnualResponse> porFormaPagamento,
                                            List<ResumoTipoPlanoAnualResponse> porTipoPlano,
                                            List<AlunoInadimplenciaAnualResponse> maioresInadimplentes,
                                            Integer melhorMesReceita, Integer piorMesReceita,
                                            Integer mesMaiorInadimplencia) {
        this.ano = ano;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.resumo = resumo;
        this.meses = meses;
        this.porFormaPagamento = porFormaPagamento;
        this.porTipoPlano = porTipoPlano;
        this.maioresInadimplentes = maioresInadimplentes;
        this.melhorMesReceita = melhorMesReceita;
        this.piorMesReceita = piorMesReceita;
        this.mesMaiorInadimplencia = mesMaiorInadimplencia;
    }

    public Integer getAno() { return ano; }
    public LocalDate getDataInicio() { return dataInicio; }
    public LocalDate getDataFim() { return dataFim; }
    public ResumoFinanceiroAnualResponse getResumo() { return resumo; }
    public List<ResumoFinanceiroMesResponse> getMeses() { return meses; }
    public List<ResumoFormaPagamentoAnualResponse> getPorFormaPagamento() { return porFormaPagamento; }
    public List<ResumoTipoPlanoAnualResponse> getPorTipoPlano() { return porTipoPlano; }
    public List<AlunoInadimplenciaAnualResponse> getMaioresInadimplentes() { return maioresInadimplentes; }
    public Integer getMelhorMesReceita() { return melhorMesReceita; }
    public Integer getPiorMesReceita() { return piorMesReceita; }
    public Integer getMesMaiorInadimplencia() { return mesMaiorInadimplencia; }
}
