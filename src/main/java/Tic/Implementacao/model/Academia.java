package Tic.Implementacao.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

@Entity
@Table(name = "academias")
public class Academia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome da academia é obrigatório")
    @Column(nullable = false, length = 150)
    private String nome;

    @Column(length = 20)
    private String cpfOuCnpj;

    @Email(message = "E-mail da academia inválido")
    @Column(length = 150)
    private String email;

    @Column(length = 30)
    private String telefone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusAcademia status = StatusAcademia.ATIVA;

    @Column(nullable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "codigo_check_in_publico", unique = true, length = 80)
    private String codigoCheckInPublico;

    public Academia() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCpfOuCnpj() { return cpfOuCnpj; }
    public void setCpfOuCnpj(String cpfOuCnpj) { this.cpfOuCnpj = cpfOuCnpj; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public StatusAcademia getStatus() { return status; }
    public void setStatus(StatusAcademia status) { this.status = status; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }
    public String getCodigoCheckInPublico() { return codigoCheckInPublico; }
    public void setCodigoCheckInPublico(String codigoCheckInPublico) { this.codigoCheckInPublico = codigoCheckInPublico; }
}
