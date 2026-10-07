package Tic.Implementacao.dto.professor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class AtualizarModalidadesProfessorRequest {

    @NotEmpty(message = "Informe ao menos uma modalidade ministrada")
    @Valid
    private List<ProfessorModalidadeRequest> modalidades;

    public List<ProfessorModalidadeRequest> getModalidades() { return modalidades; }
    public void setModalidades(List<ProfessorModalidadeRequest> modalidades) { this.modalidades = modalidades; }
}
