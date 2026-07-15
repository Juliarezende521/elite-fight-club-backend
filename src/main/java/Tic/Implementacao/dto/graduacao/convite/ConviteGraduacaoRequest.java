package Tic.Implementacao.dto.graduacao.convite;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ConviteGraduacaoRequest {

    @NotNull(message = "Aluno é obrigatório")
    private Long alunoId;

    @NotNull(message = "Graduação de destino é obrigatória")
    private Long graduacaoDestinoId;

    @Size(max = 500, message = "Justificativa deve ter no máximo 500 caracteres")
    private String justificativa;

    public Long getAlunoId() { return alunoId; }
    public void setAlunoId(Long alunoId) { this.alunoId = alunoId; }
    public Long getGraduacaoDestinoId() { return graduacaoDestinoId; }
    public void setGraduacaoDestinoId(Long graduacaoDestinoId) { this.graduacaoDestinoId = graduacaoDestinoId; }
    public String getJustificativa() { return justificativa; }
    public void setJustificativa(String justificativa) { this.justificativa = justificativa; }
}
