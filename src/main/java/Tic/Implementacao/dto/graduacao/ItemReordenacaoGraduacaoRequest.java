package Tic.Implementacao.dto.graduacao;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class ItemReordenacaoGraduacaoRequest {

    @NotNull(message = "ID da graduação é obrigatório")
    private Long id;

    @NotNull(message = "Ordem é obrigatória")
    @Min(value = 1, message = "Ordem deve ser positiva")
    private Integer ordem;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getOrdem() { return ordem; }
    public void setOrdem(Integer ordem) { this.ordem = ordem; }
}
