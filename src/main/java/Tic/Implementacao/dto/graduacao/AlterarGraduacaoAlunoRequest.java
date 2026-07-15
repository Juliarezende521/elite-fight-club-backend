package Tic.Implementacao.dto.graduacao;

import Tic.Implementacao.model.MotivoAlteracaoGraduacao;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AlterarGraduacaoAlunoRequest {

    @NotNull(message = "Nova graduação é obrigatória")
    private Long novaGraduacaoId;

    @NotNull(message = "Motivo é obrigatório")
    private MotivoAlteracaoGraduacao motivo;

    @Size(max = 500, message = "Observação deve ter no máximo 500 caracteres")
    private String observacao;

    public Long getNovaGraduacaoId() { return novaGraduacaoId; }
    public void setNovaGraduacaoId(Long novaGraduacaoId) { this.novaGraduacaoId = novaGraduacaoId; }
    public MotivoAlteracaoGraduacao getMotivo() { return motivo; }
    public void setMotivo(MotivoAlteracaoGraduacao motivo) { this.motivo = motivo; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
}
