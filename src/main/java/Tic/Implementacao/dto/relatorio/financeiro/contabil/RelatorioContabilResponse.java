package Tic.Implementacao.dto.relatorio.financeiro.contabil;

import java.time.LocalDate;
import java.util.List;

public class RelatorioContabilResponse {
    private LocalDate inicio;
    private LocalDate fim;
    private String academiaNome;
    private String academiaDocumento;
    private ResumoRelatorioContabilResponse resumo;
    private List<ResumoNaturezaReceitaResponse> porNatureza;
    private List<ResumoMetodoPagamentoContabilResponse> porMetodoPagamento;
    private List<LancamentoContabilResponse> lancamentos;
    private String aviso;

    public RelatorioContabilResponse(LocalDate inicio, LocalDate fim, String academiaNome, String academiaDocumento,
                                     ResumoRelatorioContabilResponse resumo,
                                     List<ResumoNaturezaReceitaResponse> porNatureza,
                                     List<ResumoMetodoPagamentoContabilResponse> porMetodoPagamento,
                                     List<LancamentoContabilResponse> lancamentos, String aviso) {
        this.inicio = inicio;
        this.fim = fim;
        this.academiaNome = academiaNome;
        this.academiaDocumento = academiaDocumento;
        this.resumo = resumo;
        this.porNatureza = porNatureza;
        this.porMetodoPagamento = porMetodoPagamento;
        this.lancamentos = lancamentos;
        this.aviso = aviso;
    }

    public LocalDate getInicio() { return inicio; }
    public LocalDate getFim() { return fim; }
    public String getAcademiaNome() { return academiaNome; }
    public String getAcademiaDocumento() { return academiaDocumento; }
    public ResumoRelatorioContabilResponse getResumo() { return resumo; }
    public List<ResumoNaturezaReceitaResponse> getPorNatureza() { return porNatureza; }
    public List<ResumoMetodoPagamentoContabilResponse> getPorMetodoPagamento() { return porMetodoPagamento; }
    public List<LancamentoContabilResponse> getLancamentos() { return lancamentos; }
    public String getAviso() { return aviso; }
}
