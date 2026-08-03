package Tic.Implementacao.dto.professor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class AtualizarProfessorRequest {

    @NotBlank(message = "Nome é obrigatório")
    private String nome;

    @NotBlank(message = "CPF é obrigatório")
    private String cpf;

    @NotBlank(message = "E-mail é obrigatório")
    @Email(message = "E-mail inválido")
    private String email;

    private String contato;

    @NotNull(message = "Status ativo é obrigatório")
    private Boolean ativo;

    @NotEmpty(message = "Informe ao menos uma modalidade ministrada")
    @Valid
    private List<ProfessorModalidadeRequest> modalidades;

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getContato() { return contato; }
    public void setContato(String contato) { this.contato = contato; }
    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
    public List<ProfessorModalidadeRequest> getModalidades() { return modalidades; }
    public void setModalidades(List<ProfessorModalidadeRequest> modalidades) { this.modalidades = modalidades; }
}
