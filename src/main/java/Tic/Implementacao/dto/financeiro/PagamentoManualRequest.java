package Tic.Implementacao.dto.financeiro;

import Tic.Implementacao.model.MetodoPagamento;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PagamentoManualRequest {

    @NotNull(message = "Forma de pagamento é obrigatória")
    private MetodoPagamento metodo;

    @NotNull(message = "Valor é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor precisa ser maior que zero")
    private BigDecimal valor;

    @NotNull(message = "Data do pagamento é obrigatória")
    private LocalDateTime dataPagamento;

    private String observacao;

    public MetodoPagamento getMetodo() { return metodo; }
    public void setMetodo(MetodoPagamento metodo) { this.metodo = metodo; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
    public LocalDateTime getDataPagamento() { return dataPagamento; }
    public void setDataPagamento(LocalDateTime dataPagamento) { this.dataPagamento = dataPagamento; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
}
