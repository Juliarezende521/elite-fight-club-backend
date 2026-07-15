package Tic.Implementacao.dto.presenca;

import Tic.Implementacao.model.StatusPresenca;
import jakarta.validation.constraints.NotNull;

public class RegistrarPresencaRequest {

    @NotNull(message = "Status da presença é obrigatório")
    private StatusPresenca status;

    private String observacao;

    public StatusPresenca getStatus() { return status; }
    public void setStatus(StatusPresenca status) { this.status = status; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
}
