package Tic.Implementacao.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

@Entity
@Table(name = "graduacoes")
public class Graduacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome da graduação é obrigatório")
    @Column(nullable = false, length = 100)
    private String nome;

    @NotBlank(message = "Cor da graduação é obrigatória")
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Cor deve estar no formato #RRGGBB")
    @Column(nullable = false, length = 7)
    private String corHex;

    @Min(value = 1, message = "Ordem deve ser positiva")
    @Column(nullable = false)
    private Integer ordem = 1;

    @Min(value = 0, message = "Check-ins necessários não pode ser negativo")
    @Column(nullable = false)
    private Integer checkinsNecessarios = 0;

    @Min(value = 0, message = "Tempo mínimo em meses não pode ser negativo")
    @Column(nullable = false)
    private Integer tempoMinimoMeses = 0;

    @DecimalMin(value = "0.00", message = "Valor padrão da graduação não pode ser negativo")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valorPadraoGraduacao = BigDecimal.ZERO;

    @Column(length = 500)
    private String descricao;

    @Column(nullable = false)
    private Boolean graduacaoBase = false;

    @Column(nullable = false)
    private Boolean ativa = true;

    @ManyToOne
    @JoinColumn(name = "academia_id")
    private Academia academia;

    public Graduacao() {
    }

    public Graduacao(String nome, String cor, int nivel, int checkinsNecessarios, TipoGraduacao tipoGraduacao) {
        this.nome = nome;
        this.corHex = cor;
        this.ordem = nivel;
        this.checkinsNecessarios = checkinsNecessarios;
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
        if (nome == null || nome.isBlank()) throw new IllegalArgumentException("Nome da graduação inválido");
        this.nome = nome;
    }

    public String getCor() {
        return corHex;
    }

    public void setCor(String cor) {
        if (cor == null || !cor.matches("^#[0-9A-Fa-f]{6}$")) throw new IllegalArgumentException("Cor da graduação inválida");
        this.corHex = cor;
    }

    public int getNivel() {
        return ordem;
    }

    public void setNivel(int nivel) {
        if (nivel <= 0) throw new IllegalArgumentException("Nível da graduação inválido");
        this.ordem = nivel;
    }

    public int getCheckinsNecessarios() {
        return checkinsNecessarios;
    }

    public void setCheckinsNecessarios(int checkinsNecessarios) {
        if (checkinsNecessarios < 0) throw new IllegalArgumentException("Quantidade de check-ins inválida");
        this.checkinsNecessarios = checkinsNecessarios;
    }

    public String getCorHex() {
        return corHex;
    }

    public void setCorHex(String corHex) {
        setCor(corHex);
    }

    public Integer getOrdem() {
        return ordem;
    }

    public void setOrdem(Integer ordem) {
        if (ordem == null || ordem <= 0) throw new IllegalArgumentException("Ordem da graduação inválida");
        this.ordem = ordem;
    }

    public Integer getTempoMinimoMeses() {
        return tempoMinimoMeses;
    }

    public void setTempoMinimoMeses(Integer tempoMinimoMeses) {
        if (tempoMinimoMeses == null || tempoMinimoMeses < 0) {
            throw new IllegalArgumentException("Tempo mínimo da graduação inválido");
        }
        this.tempoMinimoMeses = tempoMinimoMeses;
    }

    public BigDecimal getValorPadraoGraduacao() {
        return valorPadraoGraduacao;
    }

    public void setValorPadraoGraduacao(BigDecimal valorPadraoGraduacao) {
        if (valorPadraoGraduacao == null || valorPadraoGraduacao.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Valor padrão da graduação inválido");
        }
        this.valorPadraoGraduacao = valorPadraoGraduacao;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Boolean getGraduacaoBase() {
        return graduacaoBase;
    }

    public void setGraduacaoBase(Boolean graduacaoBase) {
        this.graduacaoBase = graduacaoBase;
    }

    public Boolean getAtiva() {
        return ativa;
    }

    public void setAtiva(Boolean ativa) {
        this.ativa = ativa;
    }

    public Academia getAcademia() {
        return academia;
    }

    public void setAcademia(Academia academia) {
        this.academia = academia;
    }
}
