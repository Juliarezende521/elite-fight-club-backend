package Tic.Implementacao.dto.evolucaofisica.meta;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CriarMetaPesoRequest {

    @NotNull(message = "Peso alvo é obrigatório")
    @DecimalMin(value = "0.01", message = "Peso alvo deve ser maior que zero")
    @DecimalMax(value = "500.00", message = "Peso alvo deve ser menor ou igual a 500")
    private BigDecimal pesoAlvo;

    @PastOrPresent(message = "Data de início não pode ser futura")
    private LocalDate dataInicio;

    private LocalDate dataLimite;

    @Size(max = 1000, message = "Observação deve ter no máximo 1000 caracteres")
    private String observacao;

    @DecimalMin(value = "0.01", message = "Peso inicial manual deve ser maior que zero")
    @DecimalMax(value = "500.00", message = "Peso inicial manual deve ser menor ou igual a 500")
    private BigDecimal pesoInicialManual;

    public BigDecimal getPesoAlvo() { return pesoAlvo; }
    public void setPesoAlvo(BigDecimal pesoAlvo) { this.pesoAlvo = pesoAlvo; }
    public LocalDate getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDate dataInicio) { this.dataInicio = dataInicio; }
    public LocalDate getDataLimite() { return dataLimite; }
    public void setDataLimite(LocalDate dataLimite) { this.dataLimite = dataLimite; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
    public BigDecimal getPesoInicialManual() { return pesoInicialManual; }
    public void setPesoInicialManual(BigDecimal pesoInicialManual) { this.pesoInicialManual = pesoInicialManual; }
}
