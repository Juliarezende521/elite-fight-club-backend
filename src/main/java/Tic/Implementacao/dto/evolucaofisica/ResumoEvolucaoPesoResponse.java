package Tic.Implementacao.dto.evolucaofisica;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ResumoEvolucaoPesoResponse {
    private BigDecimal pesoInicial;
    private LocalDate dataPesoInicial;
    private BigDecimal pesoAtual;
    private LocalDate dataPesoAtual;
    private BigDecimal diferencaPeso;
    private BigDecimal menorPeso;
    private BigDecimal maiorPeso;
    private Long totalRegistros;

    public ResumoEvolucaoPesoResponse(BigDecimal pesoInicial, LocalDate dataPesoInicial, BigDecimal pesoAtual,
                                      LocalDate dataPesoAtual, BigDecimal diferencaPeso, BigDecimal menorPeso,
                                      BigDecimal maiorPeso, Long totalRegistros) {
        this.pesoInicial = pesoInicial;
        this.dataPesoInicial = dataPesoInicial;
        this.pesoAtual = pesoAtual;
        this.dataPesoAtual = dataPesoAtual;
        this.diferencaPeso = diferencaPeso;
        this.menorPeso = menorPeso;
        this.maiorPeso = maiorPeso;
        this.totalRegistros = totalRegistros;
    }

    public BigDecimal getPesoInicial() { return pesoInicial; }
    public LocalDate getDataPesoInicial() { return dataPesoInicial; }
    public BigDecimal getPesoAtual() { return pesoAtual; }
    public LocalDate getDataPesoAtual() { return dataPesoAtual; }
    public BigDecimal getDiferencaPeso() { return diferencaPeso; }
    public BigDecimal getMenorPeso() { return menorPeso; }
    public BigDecimal getMaiorPeso() { return maiorPeso; }
    public Long getTotalRegistros() { return totalRegistros; }
}
