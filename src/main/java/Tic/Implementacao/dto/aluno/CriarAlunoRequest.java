package Tic.Implementacao.dto.aluno;

import Tic.Implementacao.model.TipoAluno;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class CriarAlunoRequest {

    @NotBlank(message = "Nome é obrigatório")
    private String nome;

    @Email(message = "E-mail inválido")
    @NotBlank(message = "E-mail é obrigatório")
    private String email;

    @NotBlank(message = "Senha inicial é obrigatória")
    @Size(min = 8, message = "Senha inicial deve ter pelo menos 8 caracteres")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).+$", message = "Senha deve conter letra maiúscula, minúscula e número")
    private String senhaInicial;

    @NotNull(message = "Tipo de aluno é obrigatório")
    private TipoAluno tipoAluno;

    @NotNull(message = "Valor da mensalidade é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor da mensalidade precisa ser maior que zero")
    private BigDecimal valorMensalidade;

    @NotNull(message = "Dia de vencimento é obrigatório")
    @Min(value = 1, message = "Dia de vencimento deve ser entre 1 e 28")
    @Max(value = 28, message = "Dia de vencimento deve ser entre 1 e 28")
    private Integer diaVencimento;

    private Long planoId;

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSenhaInicial() { return senhaInicial; }
    public void setSenhaInicial(String senhaInicial) { this.senhaInicial = senhaInicial; }
    public TipoAluno getTipoAluno() { return tipoAluno; }
    public void setTipoAluno(TipoAluno tipoAluno) { this.tipoAluno = tipoAluno; }
    public BigDecimal getValorMensalidade() { return valorMensalidade; }
    public void setValorMensalidade(BigDecimal valorMensalidade) { this.valorMensalidade = valorMensalidade; }
    public Integer getDiaVencimento() { return diaVencimento; }
    public void setDiaVencimento(Integer diaVencimento) { this.diaVencimento = diaVencimento; }
    public Long getPlanoId() { return planoId; }
    public void setPlanoId(Long planoId) { this.planoId = planoId; }
}
