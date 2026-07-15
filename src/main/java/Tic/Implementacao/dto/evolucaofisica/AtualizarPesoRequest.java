package Tic.Implementacao.dto.evolucaofisica;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.math.BigDecimal;
import java.time.LocalDate;

public class AtualizarPesoRequest {

    @NotNull(message = "Peso é obrigatório")
    @DecimalMin(value = "0.01", message = "Peso deve ser maior que zero")
    @DecimalMax(value = "500.00", message = "Peso deve ser menor ou igual a 500")
    private BigDecimal peso;

    @NotNull(message = "Data do registro é obrigatória")
    @PastOrPresent(message = "Data do registro não pode ser futura")
    private LocalDate dataRegistro;

    private String observacao;

    public BigDecimal getPeso() {
        return peso;
    }

    public void setPeso(BigDecimal peso) {
        this.peso = peso;
    }

    public LocalDate getDataRegistro() {
        return dataRegistro;
    }

    public void setDataRegistro(LocalDate dataRegistro) {
        this.dataRegistro = dataRegistro;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }
}
