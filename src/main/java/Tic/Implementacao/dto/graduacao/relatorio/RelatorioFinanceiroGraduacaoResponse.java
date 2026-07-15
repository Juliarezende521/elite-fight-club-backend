package Tic.Implementacao.dto.graduacao.relatorio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class RelatorioFinanceiroGraduacaoResponse {

    private LocalDate inicio;
    private LocalDate fim;
    private BigDecimal totalBruto;
    private BigDecimal totalTaxas;
    private BigDecimal totalLiquido;
    private BigDecimal totalEstornado;
    private BigDecimal totalPendente;
    private List<LancamentoFinanceiroGraduacaoResponse> lancamentos;

    public RelatorioFinanceiroGraduacaoResponse(LocalDate inicio, LocalDate fim, BigDecimal totalBruto,
                                                BigDecimal totalTaxas, BigDecimal totalLiquido,
                                                BigDecimal totalEstornado, BigDecimal totalPendente,
                                                List<LancamentoFinanceiroGraduacaoResponse> lancamentos) {
        this.inicio = inicio;
        this.fim = fim;
        this.totalBruto = totalBruto;
        this.totalTaxas = totalTaxas;
        this.totalLiquido = totalLiquido;
        this.totalEstornado = totalEstornado;
        this.totalPendente = totalPendente;
        this.lancamentos = lancamentos;
    }

    public LocalDate getInicio() { return inicio; }
    public LocalDate getFim() { return fim; }
    public BigDecimal getTotalBruto() { return totalBruto; }
    public BigDecimal getTotalTaxas() { return totalTaxas; }
    public BigDecimal getTotalLiquido() { return totalLiquido; }
    public BigDecimal getTotalEstornado() { return totalEstornado; }
    public BigDecimal getTotalPendente() { return totalPendente; }
    public List<LancamentoFinanceiroGraduacaoResponse> getLancamentos() { return lancamentos; }
}
