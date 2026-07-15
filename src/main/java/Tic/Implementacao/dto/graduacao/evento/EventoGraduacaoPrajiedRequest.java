package Tic.Implementacao.dto.graduacao.evento;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class EventoGraduacaoPrajiedRequest {

    @NotNull(message = "Graduação de destino é obrigatória")
    private Long graduacaoDestinoId;

    @DecimalMin(value = "0.00", message = "Valor não pode ser negativo")
    private BigDecimal valor;

    @Min(value = 1, message = "Limite de vagas deve ser maior que zero")
    private Integer limiteVagas;

    private Boolean ativo = true;

    public Long getGraduacaoDestinoId() { return graduacaoDestinoId; }
    public void setGraduacaoDestinoId(Long graduacaoDestinoId) { this.graduacaoDestinoId = graduacaoDestinoId; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
    public Integer getLimiteVagas() { return limiteVagas; }
    public void setLimiteVagas(Integer limiteVagas) { this.limiteVagas = limiteVagas; }
    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
}
