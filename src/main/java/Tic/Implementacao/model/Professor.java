package Tic.Implementacao.model;

import jakarta.persistence.*;

@Entity
@Table(name = "professores")
public class Professor extends Pessoa{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 500)
    private String especialidade;
    @Column(length = 500)
    private String contato;

    @ManyToOne
    @JoinColumn(name = "academia_id")
    private Academia academia;

    public Professor(String cpf, String nome, Long id, String especialidade, String contato) {
        super(cpf, nome);
        this.id = id;
        this.especialidade = especialidade;
        this.contato = contato;
    }

    public Professor() {
    }

    @Override
    public void cadastrar() {

    }

    @Override
    public void atualizarDados() {

    }

    @Override
    public String exibirIdentificacao() {
        return "";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEspecialidade() {
        return especialidade;
    }

    public void setEspecialidade(String especialidade) {
        this.especialidade = especialidade;
    }

    public String getContato() {
        return contato;
    }

    public void setContato(String contato) {
        this.contato = contato;
    }

    public Academia getAcademia() {
        return academia;
    }

    public void setAcademia(Academia academia) {
        this.academia = academia;
    }
}
