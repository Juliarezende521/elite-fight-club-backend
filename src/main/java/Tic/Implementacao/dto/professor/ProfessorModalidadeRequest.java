package Tic.Implementacao.dto.professor;

import jakarta.validation.constraints.NotNull;

public class ProfessorModalidadeRequest {

    @NotNull(message = "Modalidade é obrigatória")
    private Long modalidadeId;

    @NotNull(message = "Informe se permite turma")
    private Boolean permiteTurma;

    @NotNull(message = "Informe se permite personal")
    private Boolean permitePersonal;

    public Long getModalidadeId() { return modalidadeId; }
    public void setModalidadeId(Long modalidadeId) { this.modalidadeId = modalidadeId; }
    public Boolean getPermiteTurma() { return permiteTurma; }
    public void setPermiteTurma(Boolean permiteTurma) { this.permiteTurma = permiteTurma; }
    public Boolean getPermitePersonal() { return permitePersonal; }
    public void setPermitePersonal(Boolean permitePersonal) { this.permitePersonal = permitePersonal; }
}
