package Tic.Implementacao.dto.aula;

import Tic.Implementacao.model.StatusAula;

import java.time.LocalDate;
import java.time.LocalTime;

public class ItemAulaProfessorResponse {
    private String tipo;
    private Long id;
    private Long recorrenciaId;
    private String aluno;
    private String modalidade;
    private LocalDate data;
    private LocalTime horaInicio;
    private LocalTime horaFim;
    private StatusAula status;

    public ItemAulaProfessorResponse(String tipo, Long id, Long recorrenciaId, String aluno, String modalidade,
                                     LocalDate data, LocalTime horaInicio, LocalTime horaFim, StatusAula status) {
        this.tipo = tipo;
        this.id = id;
        this.recorrenciaId = recorrenciaId;
        this.aluno = aluno;
        this.modalidade = modalidade;
        this.data = data;
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
        this.status = status;
    }

    public String getTipo() { return tipo; }
    public Long getId() { return id; }
    public Long getRecorrenciaId() { return recorrenciaId; }
    public String getAluno() { return aluno; }
    public String getModalidade() { return modalidade; }
    public LocalDate getData() { return data; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public LocalTime getHoraFim() { return horaFim; }
    public StatusAula getStatus() { return status; }
}
