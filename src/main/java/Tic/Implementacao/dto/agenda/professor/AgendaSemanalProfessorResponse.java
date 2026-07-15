package Tic.Implementacao.dto.agenda.professor;

import java.time.LocalDate;
import java.util.List;

public class AgendaSemanalProfessorResponse {

    private Long professorId;
    private LocalDate inicio;
    private LocalDate fim;
    private Integer totalTurmas;
    private Integer totalPersonais;
    private Integer totalAulas;
    private Integer totalConfirmados;
    private List<ResumoAgendaProfessorResponse> dias;

    public AgendaSemanalProfessorResponse(Long professorId, LocalDate inicio, LocalDate fim, Integer totalTurmas,
                                          Integer totalPersonais, Integer totalAulas, Integer totalConfirmados,
                                          List<ResumoAgendaProfessorResponse> dias) {
        this.professorId = professorId;
        this.inicio = inicio;
        this.fim = fim;
        this.totalTurmas = totalTurmas;
        this.totalPersonais = totalPersonais;
        this.totalAulas = totalAulas;
        this.totalConfirmados = totalConfirmados;
        this.dias = dias;
    }

    public Long getProfessorId() { return professorId; }
    public LocalDate getInicio() { return inicio; }
    public LocalDate getFim() { return fim; }
    public Integer getTotalTurmas() { return totalTurmas; }
    public Integer getTotalPersonais() { return totalPersonais; }
    public Integer getTotalAulas() { return totalAulas; }
    public Integer getTotalConfirmados() { return totalConfirmados; }
    public List<ResumoAgendaProfessorResponse> getDias() { return dias; }
}
