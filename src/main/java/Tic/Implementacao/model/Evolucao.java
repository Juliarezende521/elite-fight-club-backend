package Tic.Implementacao.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "evolucoes")
public class Evolucao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private float pesoAtual;

    @Column(nullable = false)
    private float bioimpedancia;

    @Column(nullable = false)
    private LocalDate dataAvaliacao;

    @Column(length = 500)
    private String observacao;

    @ManyToOne
    @JoinColumn(name = "aluno_id")
    private Aluno aluno;

    @ManyToOne
    @JoinColumn(name = "graduacao_id")
    private Graduacao graduacao;

    public Evolucao() {
    }

    public Evolucao(float pesoAtual, float bioimpedancia, LocalDate dataAvaliacao,
                    String observacao, Aluno aluno) {
        this.pesoAtual = pesoAtual;
        this.bioimpedancia = bioimpedancia;
        this.dataAvaliacao = dataAvaliacao;
        this.observacao = observacao;
        this.aluno = aluno;
    }

    public Long getId() {
        return id;
    }

    public float getPesoAtual() {
        return pesoAtual;
    }

    public void setPesoAtual(float pesoAtual) {
        this.pesoAtual = pesoAtual;
    }

    public float getBioimpedancia() {
        return bioimpedancia;
    }

    public void setBioimpedancia(float bioimpedancia) {
        this.bioimpedancia = bioimpedancia;
    }

    public LocalDate getDataAvaliacao() {
        return dataAvaliacao;
    }

    public void setDataAvaliacao(LocalDate dataAvaliacao) {
        this.dataAvaliacao = dataAvaliacao;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public Aluno getAluno() {
        return aluno;
    }

    public void setAluno(Aluno aluno) {
        this.aluno = aluno;
    }

    public Graduacao getGraduacao() {
        return graduacao;
    }

    public void setGraduacao(Graduacao graduacao) {
        this.graduacao = graduacao;
    }
}
