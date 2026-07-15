package Tic.Implementacao.dto.graduacao.relatorio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class RelatorioGraduacoesPeriodoResponse {

    private LocalDate inicio;
    private LocalDate fim;
    private Integer totalEventos;
    private Integer eventosConcluidos;
    private Integer totalInscricoes;
    private Integer totalPagos;
    private Integer totalAprovados;
    private Integer totalPromovidos;
    private BigDecimal receitaBruta;
    private BigDecimal taxas;
    private BigDecimal receitaLiquida;
    private List<ResumoEventoGraduacaoRelatorioResponse> eventos;

    public RelatorioGraduacoesPeriodoResponse(LocalDate inicio, LocalDate fim, Integer totalEventos,
                                              Integer eventosConcluidos, Integer totalInscricoes,
                                              Integer totalPagos, Integer totalAprovados,
                                              Integer totalPromovidos, BigDecimal receitaBruta,
                                              BigDecimal taxas, BigDecimal receitaLiquida,
                                              List<ResumoEventoGraduacaoRelatorioResponse> eventos) {
        this.inicio = inicio;
        this.fim = fim;
        this.totalEventos = totalEventos;
        this.eventosConcluidos = eventosConcluidos;
        this.totalInscricoes = totalInscricoes;
        this.totalPagos = totalPagos;
        this.totalAprovados = totalAprovados;
        this.totalPromovidos = totalPromovidos;
        this.receitaBruta = receitaBruta;
        this.taxas = taxas;
        this.receitaLiquida = receitaLiquida;
        this.eventos = eventos;
    }

    public LocalDate getInicio() { return inicio; }
    public LocalDate getFim() { return fim; }
    public Integer getTotalEventos() { return totalEventos; }
    public Integer getEventosConcluidos() { return eventosConcluidos; }
    public Integer getTotalInscricoes() { return totalInscricoes; }
    public Integer getTotalPagos() { return totalPagos; }
    public Integer getTotalAprovados() { return totalAprovados; }
    public Integer getTotalPromovidos() { return totalPromovidos; }
    public BigDecimal getReceitaBruta() { return receitaBruta; }
    public BigDecimal getTaxas() { return taxas; }
    public BigDecimal getReceitaLiquida() { return receitaLiquida; }
    public List<ResumoEventoGraduacaoRelatorioResponse> getEventos() { return eventos; }
}
