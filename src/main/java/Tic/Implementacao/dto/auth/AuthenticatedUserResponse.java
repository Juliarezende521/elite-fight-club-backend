package Tic.Implementacao.dto.auth;

import Tic.Implementacao.model.PerfilUsuario;
import Tic.Implementacao.model.Usuario;

public class AuthenticatedUserResponse {

    private Long id;
    private String nome;
    private String email;
    private PerfilUsuario perfil;
    private Boolean ativo;
    private AcademiaSessaoResponse academia;
    private Long alunoId;
    private Long professorId;

    public AuthenticatedUserResponse() {
    }

    public static AuthenticatedUserResponse from(Usuario usuario) {
        AuthenticatedUserResponse response = new AuthenticatedUserResponse();
        response.setId(usuario.getId());
        response.setNome(usuario.getNome());
        response.setEmail(usuario.getEmail());
        response.setPerfil(usuario.getPerfil());
        response.setAtivo(usuario.getAtivo());
        response.setAcademia(AcademiaSessaoResponse.from(usuario.getAcademia()));
        response.setAlunoId(usuario.getAluno() != null ? usuario.getAluno().getId() : null);
        response.setProfessorId(usuario.getProfessor() != null ? usuario.getProfessor().getId() : null);
        return response;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public PerfilUsuario getPerfil() {
        return perfil;
    }

    public void setPerfil(PerfilUsuario perfil) {
        this.perfil = perfil;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public AcademiaSessaoResponse getAcademia() {
        return academia;
    }

    public void setAcademia(AcademiaSessaoResponse academia) {
        this.academia = academia;
    }

    public Long getAlunoId() {
        return alunoId;
    }

    public void setAlunoId(Long alunoId) {
        this.alunoId = alunoId;
    }

    public Long getProfessorId() {
        return professorId;
    }

    public void setProfessorId(Long professorId) {
        this.professorId = professorId;
    }
}
