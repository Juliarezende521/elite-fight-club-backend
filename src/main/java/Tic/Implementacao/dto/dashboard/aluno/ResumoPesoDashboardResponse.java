package Tic.Implementacao.dto.dashboard.aluno;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ResumoPesoDashboardResponse {
    private BigDecimal pesoAtual;
    private LocalDate dataPesoAtual;
    private BigDecimal diferencaPeso;
    private BigDecimal menorPeso;
    private BigDecimal maiorPeso;
    private Long totalRegistros;

    public ResumoPesoDashboardResponse(BigDecimal pesoAtual, LocalDate dataPesoAtual, BigDecimal diferencaPeso,
                                       BigDecimal menorPeso, BigDecimal maiorPeso, Long totalRegistros) {
        this.pesoAtual = pesoAtual;
        this.dataPesoAtual = dataPesoAtual;
        this.diferencaPeso = diferencaPeso;
        this.menorPeso = menorPeso;
        this.maiorPeso = maiorPeso;
        this.totalRegistros = totalRegistros;
    }

    public BigDecimal getPesoAtual() { return pesoAtual; }
    public LocalDate getDataPesoAtual() { return dataPesoAtual; }
    public BigDecimal getDiferencaPeso() { return diferencaPeso; }
    public BigDecimal getMenorPeso() { return menorPeso; }
    public BigDecimal getMaiorPeso() { return maiorPeso; }
    public Long getTotalRegistros() { return totalRegistros; }
}
