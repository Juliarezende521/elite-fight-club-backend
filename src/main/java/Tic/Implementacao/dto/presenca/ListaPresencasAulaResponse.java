package Tic.Implementacao.dto.presenca;

import java.util.List;

public class ListaPresencasAulaResponse {

    private Long aulaId;
    private Integer confirmados;
    private Integer presentes;
    private Integer ausentes;
    private Integer pendentes;
    private List<PresencaAlunoResponse> alunos;

    public ListaPresencasAulaResponse(Long aulaId, Integer confirmados, Integer presentes,
                                      Integer ausentes, Integer pendentes, List<PresencaAlunoResponse> alunos) {
        this.aulaId = aulaId;
        this.confirmados = confirmados;
        this.presentes = presentes;
        this.ausentes = ausentes;
        this.pendentes = pendentes;
        this.alunos = alunos;
    }

    public Long getAulaId() { return aulaId; }
    public Integer getConfirmados() { return confirmados; }
    public Integer getPresentes() { return presentes; }
    public Integer getAusentes() { return ausentes; }
    public Integer getPendentes() { return pendentes; }
    public List<PresencaAlunoResponse> getAlunos() { return alunos; }
}
