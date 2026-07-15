package Tic.Implementacao.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "turmas")
public class Turma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @ManyToMany
    @JoinTable(
            name = "turma_alunos",
            joinColumns = @JoinColumn(name = "turma_id"),
            inverseJoinColumns = @JoinColumn(name = "aluno_id")
    )
    private List<Aluno> alunosInscritos;

    @ManyToOne
    @JoinColumn(name = "academia_id")
    private Academia academia;

    public Turma() {
    }

    public Turma(String nome, List<Aluno> alunosInscritos) {
        this.nome = nome;
        this.alunosInscritos = alunosInscritos;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public List<Aluno> getAlunosInscritos() {
        return alunosInscritos;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setNome(String nome) {
        if(nome == null || nome.isBlank()){
            throw new IllegalArgumentException("Nome inválido");
        }
        this.nome = nome;
    }

    public void setAlunosInscritos(List<Aluno> alunosInscritos) {
        this.alunosInscritos = alunosInscritos;
    }

    public Academia getAcademia() {
        return academia;
    }

    public void setAcademia(Academia academia) {
        this.academia = academia;
    }

    public void criarTurma(){}

    public void inscreverAluno(Aluno aluno){
        alunosInscritos.add(aluno);
    }
}
