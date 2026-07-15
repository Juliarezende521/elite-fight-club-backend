package Tic.Implementacao.dto.agenda.professor;

import Tic.Implementacao.model.StatusAula;
import Tic.Implementacao.model.TipoItemAgenda;

import java.time.LocalDate;
import java.time.LocalTime;

public class ItemAgendaProfessorResponse {

    private Long aulaId;
    private TipoItemAgenda tipo;
    private LocalDate data;
    private LocalTime horaInicio;
    private LocalTime horaFim;
    private StatusAula status;
    private Long professorId;
    private String professorNome;
    private Long academiaId;
    private Long modalidadeId;
    private String modalidadeNome;
    private Integer capacidadeMaxima;
    private Integer confirmados;
    private Integer vagasDisponiveis;
    private Boolean lotada;
    private Long alunoId;
    private String alunoNome;
    private Long aulaOriginalId;

    public Long getAulaId() { return aulaId; }
    public void setAulaId(Long aulaId) { this.aulaId = aulaId; }
    public TipoItemAgenda getTipo() { return tipo; }
    public void setTipo(TipoItemAgenda tipo) { this.tipo = tipo; }
    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }
    public LocalTime getHoraFim() { return horaFim; }
    public void setHoraFim(LocalTime horaFim) { this.horaFim = horaFim; }
    public StatusAula getStatus() { return status; }
    public void setStatus(StatusAula status) { this.status = status; }
    public Long getProfessorId() { return professorId; }
    public void setProfessorId(Long professorId) { this.professorId = professorId; }
    public String getProfessorNome() { return professorNome; }
    public void setProfessorNome(String professorNome) { this.professorNome = professorNome; }
    public Long getAcademiaId() { return academiaId; }
    public void setAcademiaId(Long academiaId) { this.academiaId = academiaId; }
    public Long getModalidadeId() { return modalidadeId; }
    public void setModalidadeId(Long modalidadeId) { this.modalidadeId = modalidadeId; }
    public String getModalidadeNome() { return modalidadeNome; }
    public void setModalidadeNome(String modalidadeNome) { this.modalidadeNome = modalidadeNome; }
    public Integer getCapacidadeMaxima() { return capacidadeMaxima; }
    public void setCapacidadeMaxima(Integer capacidadeMaxima) { this.capacidadeMaxima = capacidadeMaxima; }
    public Integer getConfirmados() { return confirmados; }
    public void setConfirmados(Integer confirmados) { this.confirmados = confirmados; }
    public Integer getVagasDisponiveis() { return vagasDisponiveis; }
    public void setVagasDisponiveis(Integer vagasDisponiveis) { this.vagasDisponiveis = vagasDisponiveis; }
    public Boolean getLotada() { return lotada; }
    public void setLotada(Boolean lotada) { this.lotada = lotada; }
    public Long getAlunoId() { return alunoId; }
    public void setAlunoId(Long alunoId) { this.alunoId = alunoId; }
    public String getAlunoNome() { return alunoNome; }
    public void setAlunoNome(String alunoNome) { this.alunoNome = alunoNome; }
    public Long getAulaOriginalId() { return aulaOriginalId; }
    public void setAulaOriginalId(Long aulaOriginalId) { this.aulaOriginalId = aulaOriginalId; }
}
