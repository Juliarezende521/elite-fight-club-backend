package Tic.Implementacao.model;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name="campeonatos")
public class Campeonato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 500)
    private String nome;
    @Column (length = 500)
    private String data;
    @Column(length = 500)
    private String locomocao;
    @Column(nullable = false)
    private Double valores;
    @Column(length = 500)
    private String descricao;

    private List<String> pesosDeLuta;


    @OneToMany
    private List<Aluno> alunosInscritos; // ATENÇÃO AO ADICIONAR OS RELACIONAMENTOS

    public Campeonato(String nome, String data, String locomocao, Double valores, String descricao, List<String> pesosDeLuta, List<Aluno> alunosInscritos) {
        this.id = id;
        this.nome = nome;
        this.data = data;
        this.locomocao = locomocao;
        this.valores = valores;
        this.descricao = descricao;
        this.pesosDeLuta = pesosDeLuta;
        this.alunosInscritos = alunosInscritos;
    }

    public Campeonato() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getLocomocao() {
        return locomocao;
    }

    public void setLocomocao(String locomocao) {
        this.locomocao = locomocao;
    }

    public Double getValores() {
        return valores;
    }

    public void setValores(Double valores) {
        this.valores = valores;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public List<String> getPesosDeLuta() {
        return pesosDeLuta;
    }

    public void setPesosDeLuta(List<String> pesosDeLuta) {
        this.pesosDeLuta = pesosDeLuta;
    }

    public List<Aluno> getAlunosInscritos() {
        return alunosInscritos;
    }

    public void setAlunosInscritos(List<Aluno> alunosInscritos) {
        this.alunosInscritos = alunosInscritos;
    }
}
