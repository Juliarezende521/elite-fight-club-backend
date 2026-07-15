package Tic.Implementacao.dto.evolucaofisica.avaliacao;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CriarAvaliacaoCorporalRequest {
    @NotNull(message = "Data da avaliação é obrigatória")
    @PastOrPresent(message = "Data da avaliação não pode ser futura")
    private LocalDate dataAvaliacao;
    private BigDecimal alturaMetros;
    private BigDecimal pesoRegistrado;
    @Size(max = 2000, message = "Observação deve ter no máximo 2000 caracteres")
    private String observacao;
    @Valid
    private MedidasCorporaisRequest medidas;
    @Valid
    private BioimpedanciaRequest bioimpedancia;

    public LocalDate getDataAvaliacao() { return dataAvaliacao; }
    public void setDataAvaliacao(LocalDate dataAvaliacao) { this.dataAvaliacao = dataAvaliacao; }
    public BigDecimal getAlturaMetros() { return alturaMetros; }
    public void setAlturaMetros(BigDecimal alturaMetros) { this.alturaMetros = alturaMetros; }
    public BigDecimal getPesoRegistrado() { return pesoRegistrado; }
    public void setPesoRegistrado(BigDecimal pesoRegistrado) { this.pesoRegistrado = pesoRegistrado; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
    public MedidasCorporaisRequest getMedidas() { return medidas; }
    public void setMedidas(MedidasCorporaisRequest medidas) { this.medidas = medidas; }
    public BioimpedanciaRequest getBioimpedancia() { return bioimpedancia; }
    public void setBioimpedancia(BioimpedanciaRequest bioimpedancia) { this.bioimpedancia = bioimpedancia; }
}
