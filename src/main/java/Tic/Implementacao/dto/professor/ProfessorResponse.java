package Tic.Implementacao.dto.professor;

import java.util.List;

public class ProfessorResponse {

    private Long id;
    private String nome;
    private String cpf;
    private String email;
    private String contato;
    private Boolean ativo;
    private Long academiaId;
    private String academiaNome;
    private List<ProfessorModalidadeResponse> modalidades;

    public ProfessorResponse(Long id, String nome, String cpf, String email, String contato, Boolean ativo,
                             Long academiaId, String academiaNome, List<ProfessorModalidadeResponse> modalidades) {
        this.id = id;
        this.nome = nome;
        this.cpf = cpf;
        this.email = email;
        this.contato = contato;
        this.ativo = ativo;
        this.academiaId = academiaId;
        this.academiaNome = academiaNome;
        this.modalidades = modalidades;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getCpf() { return cpf; }
    public String getEmail() { return email; }
    public String getContato() { return contato; }
    public Boolean getAtivo() { return ativo; }
    public Long getAcademiaId() { return academiaId; }
    public String getAcademiaNome() { return academiaNome; }
    public List<ProfessorModalidadeResponse> getModalidades() { return modalidades; }
}
