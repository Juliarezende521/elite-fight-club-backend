package Tic.Implementacao.dto.evolucaofisica.meta;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

public class EncerrarMetaPesoRequest {

    @Size(max = 1000, message = "Motivo deve ter no máximo 1000 caracteres")
    private String motivo;

    @Valid
    private CriarMetaPesoRequest novaMeta;

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public CriarMetaPesoRequest getNovaMeta() { return novaMeta; }
    public void setNovaMeta(CriarMetaPesoRequest novaMeta) { this.novaMeta = novaMeta; }
}
