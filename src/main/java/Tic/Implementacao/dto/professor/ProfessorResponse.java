package Tic.Implementacao.dto.professor;

import Tic.Implementacao.model.Professor;

public record ProfessorResponse(Long id, String nome, String especialidade, String contato) {
    public static ProfessorResponse from(Professor professor) {
        return new ProfessorResponse(professor.getId(), professor.getNome(),
                professor.getEspecialidade(), professor.getContato());
    }
}
