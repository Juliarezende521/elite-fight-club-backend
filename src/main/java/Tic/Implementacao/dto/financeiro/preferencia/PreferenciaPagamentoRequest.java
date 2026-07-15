package Tic.Implementacao.dto.financeiro.preferencia;

import Tic.Implementacao.model.FormaPagamentoPreferida;
import jakarta.validation.constraints.NotNull;

public class PreferenciaPagamentoRequest {
    @NotNull(message = "Forma de pagamento preferida é obrigatória")
    private FormaPagamentoPreferida formaPreferida;

    public FormaPagamentoPreferida getFormaPreferida() { return formaPreferida; }
    public void setFormaPreferida(FormaPagamentoPreferida formaPreferida) { this.formaPreferida = formaPreferida; }
}
