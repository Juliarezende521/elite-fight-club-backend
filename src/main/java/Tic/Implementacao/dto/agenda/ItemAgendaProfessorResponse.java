package Tic.Implementacao.dto.agenda;

import java.time.LocalTime;

public class ItemAgendaProfessorResponse {
    private String tipo;
    private Long id;
    private String aluno;
    private String modalidade;
    private LocalTime horaInicial;
    private LocalTime horaFinal;
    private Integer capacidadeMaxima;

    public ItemAgendaProfessorResponse(String tipo, Long id, String aluno, String modalidade,
                                       LocalTime horaInicial, LocalTime horaFinal, Integer capacidadeMaxima) {
        this.tipo = tipo;
        this.id = id;
        this.aluno = aluno;
        this.modalidade = modalidade;
        this.horaInicial = horaInicial;
        this.horaFinal = horaFinal;
        this.capacidadeMaxima = capacidadeMaxima;
    }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getAluno() { return aluno; }
    public void setAluno(String aluno) { this.aluno = aluno; }
    public String getModalidade() { return modalidade; }
    public void setModalidade(String modalidade) { this.modalidade = modalidade; }
    public LocalTime getHoraInicial() { return horaInicial; }
    public void setHoraInicial(LocalTime horaInicial) { this.horaInicial = horaInicial; }
    public LocalTime getHoraFinal() { return horaFinal; }
    public void setHoraFinal(LocalTime horaFinal) { this.horaFinal = horaFinal; }
    public Integer getCapacidadeMaxima() { return capacidadeMaxima; }
    public void setCapacidadeMaxima(Integer capacidadeMaxima) { this.capacidadeMaxima = capacidadeMaxima; }
}
