package Tic.Implementacao.dto.graduacao.certificado;

import jakarta.validation.constraints.NotBlank;

public class CancelarCertificadoRequest {

    @NotBlank(message = "Motivo do cancelamento é obrigatório")
    private String motivo;

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}
