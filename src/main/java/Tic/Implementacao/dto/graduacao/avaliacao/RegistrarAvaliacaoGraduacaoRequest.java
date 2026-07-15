package Tic.Implementacao.dto.graduacao.avaliacao;

import Tic.Implementacao.model.ResultadoAvaliacaoGraduacao;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class RegistrarAvaliacaoGraduacaoRequest {

    @NotNull(message = "Resultado é obrigatório")
    private ResultadoAvaliacaoGraduacao resultado;

    @DecimalMin(value = "0.00", message = "Nota técnica deve ser maior ou igual a 0")
    @DecimalMax(value = "10.00", message = "Nota técnica deve ser menor ou igual a 10")
    private BigDecimal notaTecnica;

    @Size(max = 2000, message = "Observação técnica deve ter no máximo 2000 caracteres")
    private String observacaoTecnica;

    public ResultadoAvaliacaoGraduacao getResultado() { return resultado; }
    public void setResultado(ResultadoAvaliacaoGraduacao resultado) { this.resultado = resultado; }
    public BigDecimal getNotaTecnica() { return notaTecnica; }
    public void setNotaTecnica(BigDecimal notaTecnica) { this.notaTecnica = notaTecnica; }
    public String getObservacaoTecnica() { return observacaoTecnica; }
    public void setObservacaoTecnica(String observacaoTecnica) { this.observacaoTecnica = observacaoTecnica; }
}
