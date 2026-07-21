package Tic.Implementacao.dto.aluno;

import Tic.Implementacao.model.TipoAluno;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class AtualizarAlunoRequest {

    @NotBlank(message = "Nome é obrigatório")
    private String nome;

    @Email(message = "E-mail inválido")
    @NotBlank(message = "E-mail é obrigatório")
    private String email;

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

    @NotNull(message = "Status do aluno é obrigatório")
    private Boolean ativo;

    private List<Long> modalidadeIds = new ArrayList<>();

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public TipoAluno getTipoAluno() { return tipoAluno; }
    public void setTipoAluno(TipoAluno tipoAluno) { this.tipoAluno = tipoAluno; }
    public BigDecimal getValorMensalidade() { return valorMensalidade; }
    public void setValorMensalidade(BigDecimal valorMensalidade) { this.valorMensalidade = valorMensalidade; }
    public Integer getDiaVencimento() { return diaVencimento; }
    public void setDiaVencimento(Integer diaVencimento) { this.diaVencimento = diaVencimento; }
    public Long getPlanoId() { return planoId; }
    public void setPlanoId(Long planoId) { this.planoId = planoId; }
    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
    public List<Long> getModalidadeIds() { return modalidadeIds; }
    public void setModalidadeIds(List<Long> modalidadeIds) { this.modalidadeIds = modalidadeIds; }
}
