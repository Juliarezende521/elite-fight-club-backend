package Tic.Implementacao.dto.agenda.professor;

import java.time.LocalDate;
import java.util.List;

public class ResumoAgendaProfessorResponse {

    private Long professorId;
    private LocalDate data;
    private Integer totalTurmas;
    private Integer totalPersonais;
    private Integer totalAulas;
    private Integer totalConfirmados;
    private Integer totalVagasDisponiveis;
    private Integer turmasLotadas;
    private List<ItemAgendaProfessorResponse> agenda;

    public ResumoAgendaProfessorResponse(Long professorId, LocalDate data, Integer totalTurmas, Integer totalPersonais,
                                         Integer totalAulas, Integer totalConfirmados, Integer totalVagasDisponiveis,
                                         Integer turmasLotadas, List<ItemAgendaProfessorResponse> agenda) {
        this.professorId = professorId;
        this.data = data;
        this.totalTurmas = totalTurmas;
        this.totalPersonais = totalPersonais;
        this.totalAulas = totalAulas;
        this.totalConfirmados = totalConfirmados;
        this.totalVagasDisponiveis = totalVagasDisponiveis;
        this.turmasLotadas = turmasLotadas;
        this.agenda = agenda;
    }

    public Long getProfessorId() { return professorId; }
    public LocalDate getData() { return data; }
    public Integer getTotalTurmas() { return totalTurmas; }
    public Integer getTotalPersonais() { return totalPersonais; }
    public Integer getTotalAulas() { return totalAulas; }
    public Integer getTotalConfirmados() { return totalConfirmados; }
    public Integer getTotalVagasDisponiveis() { return totalVagasDisponiveis; }
    public Integer getTurmasLotadas() { return turmasLotadas; }
    public List<ItemAgendaProfessorResponse> getAgenda() { return agenda; }
}
