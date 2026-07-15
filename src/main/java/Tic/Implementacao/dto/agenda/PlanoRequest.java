package Tic.Implementacao.dto.agenda;

import Tic.Implementacao.model.TipoPlano;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class PlanoRequest {

    @NotNull(message = "Tipo do plano é obrigatório")
    private TipoPlano tipo;

    @NotNull(message = "Quantidade de aulas por semana é obrigatória")
    @Min(value = 1, message = "Quantidade de aulas por semana deve ser maior que zero")
    private Integer quantidadeAulasSemana;

    @NotNull(message = "Valor é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
    private BigDecimal valor;

    private Boolean ativo = true;

    public TipoPlano getTipo() { return tipo; }
    public void setTipo(TipoPlano tipo) { this.tipo = tipo; }
    public Integer getQuantidadeAulasSemana() { return quantidadeAulasSemana; }
    public void setQuantidadeAulasSemana(Integer quantidadeAulasSemana) { this.quantidadeAulasSemana = quantidadeAulasSemana; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
}
