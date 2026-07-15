package Tic.Implementacao.dto.checkinqr;

import jakarta.validation.constraints.NotNull;

public class ConfirmarCheckInQrRequest {
    @NotNull(message = "Sessão é obrigatória")
    private Long sessaoId;

    public Long getSessaoId() { return sessaoId; }
    public void setSessaoId(Long sessaoId) { this.sessaoId = sessaoId; }
}
