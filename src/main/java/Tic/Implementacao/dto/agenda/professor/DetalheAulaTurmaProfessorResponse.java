package Tic.Implementacao.dto.agenda.professor;

import Tic.Implementacao.model.StatusAula;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class DetalheAulaTurmaProfessorResponse {

    private Long aulaId;
    private LocalDate data;
    private LocalTime horaInicio;
    private LocalTime horaFim;
    private StatusAula status;
    private Long professorId;
    private String professorNome;
    private String modalidadeNome;
    private Integer capacidadeMaxima;
    private Integer confirmados;
    private Integer vagasDisponiveis;
    private Boolean lotada;
    private Long aulaOriginalId;
    private List<AlunoReservaResponse> alunosConfirmados;

    public DetalheAulaTurmaProfessorResponse(Long aulaId, LocalDate data, LocalTime horaInicio, LocalTime horaFim,
                                             StatusAula status, Long professorId, String professorNome,
                                             String modalidadeNome, Integer capacidadeMaxima, Integer confirmados,
                                             Integer vagasDisponiveis, Boolean lotada, Long aulaOriginalId,
                                             List<AlunoReservaResponse> alunosConfirmados) {
        this.aulaId = aulaId;
        this.data = data;
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
        this.status = status;
        this.professorId = professorId;
        this.professorNome = professorNome;
        this.modalidadeNome = modalidadeNome;
        this.capacidadeMaxima = capacidadeMaxima;
        this.confirmados = confirmados;
        this.vagasDisponiveis = vagasDisponiveis;
        this.lotada = lotada;
        this.aulaOriginalId = aulaOriginalId;
        this.alunosConfirmados = alunosConfirmados;
    }

    public Long getAulaId() { return aulaId; }
    public LocalDate getData() { return data; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public LocalTime getHoraFim() { return horaFim; }
    public StatusAula getStatus() { return status; }
    public Long getProfessorId() { return professorId; }
    public String getProfessorNome() { return professorNome; }
    public String getModalidadeNome() { return modalidadeNome; }
    public Integer getCapacidadeMaxima() { return capacidadeMaxima; }
    public Integer getConfirmados() { return confirmados; }
    public Integer getVagasDisponiveis() { return vagasDisponiveis; }
    public Boolean getLotada() { return lotada; }
    public Long getAulaOriginalId() { return aulaOriginalId; }
    public List<AlunoReservaResponse> getAlunosConfirmados() { return alunosConfirmados; }
}
