package Tic.Implementacao.dto.graduacao;

import jakarta.validation.constraints.Size;

public class PromoverGraduacaoAlunoRequest {

    @Size(max = 500, message = "Observação deve ter no máximo 500 caracteres")
    private String observacao;

    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
}
