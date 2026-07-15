package Tic.Implementacao.dto.graduacao;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class GraduacaoRequest {

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
    private String nome;

    @NotBlank(message = "Cor é obrigatória")
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Cor deve estar no formato #RRGGBB")
    private String corHex;

    @NotNull(message = "Ordem é obrigatória")
    @Min(value = 1, message = "Ordem deve ser positiva")
    private Integer ordem;

    @NotNull(message = "Check-ins necessários é obrigatório")
    @Min(value = 0, message = "Check-ins necessários não pode ser negativo")
    private Integer checkinsNecessarios;

    @NotNull(message = "Tempo mínimo em meses é obrigatório")
    @Min(value = 0, message = "Tempo mínimo em meses não pode ser negativo")
    private Integer tempoMinimoMeses;

    @NotNull(message = "Valor padrão da graduação é obrigatório")
    @DecimalMin(value = "0.00", message = "Valor padrão da graduação não pode ser negativo")
    private BigDecimal valorPadraoGraduacao;

    @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
    private String descricao;

    private Boolean graduacaoBase = false;
    private Boolean ativa = true;

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCorHex() { return corHex; }
    public void setCorHex(String corHex) { this.corHex = corHex; }
    public Integer getOrdem() { return ordem; }
    public void setOrdem(Integer ordem) { this.ordem = ordem; }
    public Integer getCheckinsNecessarios() { return checkinsNecessarios; }
    public void setCheckinsNecessarios(Integer checkinsNecessarios) { this.checkinsNecessarios = checkinsNecessarios; }
    public Integer getTempoMinimoMeses() { return tempoMinimoMeses; }
    public void setTempoMinimoMeses(Integer tempoMinimoMeses) { this.tempoMinimoMeses = tempoMinimoMeses; }
    public BigDecimal getValorPadraoGraduacao() { return valorPadraoGraduacao; }
    public void setValorPadraoGraduacao(BigDecimal valorPadraoGraduacao) { this.valorPadraoGraduacao = valorPadraoGraduacao; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public Boolean getGraduacaoBase() { return graduacaoBase; }
    public void setGraduacaoBase(Boolean graduacaoBase) { this.graduacaoBase = graduacaoBase; }
    public Boolean getAtiva() { return ativa; }
    public void setAtiva(Boolean ativa) { this.ativa = ativa; }
}
