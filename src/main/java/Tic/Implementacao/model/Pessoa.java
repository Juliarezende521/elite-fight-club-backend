package Tic.Implementacao.model;

import jakarta.persistence.MappedSuperclass;


@MappedSuperclass
public abstract class Pessoa {
    protected String cpf;
    protected String nome;


    public Pessoa (){}

    public Pessoa (String cpf, String nome){
        this.cpf = cpf;
        this.nome = nome;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        if(cpf == null || cpf.isBlank())
            throw new IllegalArgumentException("Cpf não pode ser nulo ou vazio");
        this.cpf = cpf;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        if(nome == null || nome.isBlank())
            throw new IllegalArgumentException("Nome não pode ser nulo ou vazio");
        this.nome = nome;
    }


    public abstract void cadastrar();
    public abstract void atualizarDados();
    public abstract String exibirIdentificacao();



}
