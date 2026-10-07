package Tic.Implementacao.dto.professor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public class CriarProfessorRequest {

    @NotBlank(message = "Nome é obrigatório")
    private String nome;

    @NotBlank(message = "CPF é obrigatório")
    private String cpf;

    @NotBlank(message = "E-mail é obrigatório")
    @Email(message = "E-mail inválido")
    private String email;

    @NotBlank(message = "Senha inicial é obrigatória")
    @Size(min = 6, message = "Senha inicial deve ter no mínimo 6 caracteres")
    private String senhaInicial;

    private String contato;

    @NotEmpty(message = "Informe ao menos uma modalidade ministrada")
    @Valid
    private List<ProfessorModalidadeRequest> modalidades;

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSenhaInicial() { return senhaInicial; }
    public void setSenhaInicial(String senhaInicial) { this.senhaInicial = senhaInicial; }
    public String getContato() { return contato; }
    public void setContato(String contato) { this.contato = contato; }
    public List<ProfessorModalidadeRequest> getModalidades() { return modalidades; }
    public void setModalidades(List<ProfessorModalidadeRequest> modalidades) { this.modalidades = modalidades; }
}
