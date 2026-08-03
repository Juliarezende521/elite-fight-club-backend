package Tic.Implementacao.dto.professor;

import jakarta.validation.constraints.NotNull;

public class AtualizarStatusProfessorRequest {

    @NotNull(message = "Status ativo é obrigatório")
    private Boolean ativo;

    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
}
