package Tic.Implementacao.dto.checkin;

import Tic.Implementacao.model.OrigemCheckIn;
import jakarta.validation.constraints.NotNull;

public class RegistrarCheckInRequest {

    @NotNull(message = "Aluno é obrigatório")
    private Long alunoId;

    private Long turmaId;
    private Long professorId;
    private OrigemCheckIn origem;

    public Long getAlunoId() { return alunoId; }
    public void setAlunoId(Long alunoId) { this.alunoId = alunoId; }
    public Long getTurmaId() { return turmaId; }
    public void setTurmaId(Long turmaId) { this.turmaId = turmaId; }
    public Long getProfessorId() { return professorId; }
    public void setProfessorId(Long professorId) { this.professorId = professorId; }
    public OrigemCheckIn getOrigem() { return origem; }
    public void setOrigem(OrigemCheckIn origem) { this.origem = origem; }
}
