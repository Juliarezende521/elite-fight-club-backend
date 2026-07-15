package Tic.Implementacao.dto.graduacao.avaliacao;

import jakarta.validation.constraints.Size;

public class PromoverAlunoEventoRequest {

    @Size(max = 1000, message = "Observação deve ter no máximo 1000 caracteres")
    private String observacao;

    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
}
