package Tic.Implementacao.dto.aluno;

import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

public class AtualizarModalidadesAlunoRequest {

    @NotNull(message = "Modalidades são obrigatórias")
    private List<Long> modalidadeIds = new ArrayList<>();

    public List<Long> getModalidadeIds() {
        return modalidadeIds;
    }

    public void setModalidadeIds(List<Long> modalidadeIds) {
        this.modalidadeIds = modalidadeIds;
    }
}

