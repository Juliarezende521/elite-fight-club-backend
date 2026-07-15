package Tic.Implementacao.dto.graduacao;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class ReordenarGraduacoesRequest {

    @Valid
    @NotEmpty(message = "Informe as graduações para reordenar")
    private List<ItemReordenacaoGraduacaoRequest> graduacoes;

    public List<ItemReordenacaoGraduacaoRequest> getGraduacoes() { return graduacoes; }
    public void setGraduacoes(List<ItemReordenacaoGraduacaoRequest> graduacoes) { this.graduacoes = graduacoes; }
}
