package Tic.Implementacao.dto.graduacao.avaliacao;

import Tic.Implementacao.model.StatusPresencaGraduacao;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class RegistrarPresencaGraduacaoRequest {

    @NotNull(message = "Status de presença é obrigatório")
    private StatusPresencaGraduacao status;

    @Size(max = 500, message = "Observação deve ter no máximo 500 caracteres")
    private String observacao;

    public StatusPresencaGraduacao getStatus() { return status; }
    public void setStatus(StatusPresencaGraduacao status) { this.status = status; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
}
