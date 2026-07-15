package Tic.Implementacao.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Entity
@Table(name = "planos")
public class Plano {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Tipo do plano é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoPlano tipo;

    @Min(value = 1, message = "Quantidade de aulas por semana deve ser maior que zero")
    @Column(nullable = false)
    private Integer quantidadeAulasSemana;

    @NotNull(message = "Valor é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(nullable = false)
    private Boolean ativo = true;

    @ManyToOne
    @JoinColumn(name = "academia_id", nullable = false)
    private Academia academia;

    public Plano() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public TipoPlano getTipo() { return tipo; }
    public void setTipo(TipoPlano tipo) { this.tipo = tipo; }
    public Integer getQuantidadeAulasSemana() { return quantidadeAulasSemana; }
    public void setQuantidadeAulasSemana(Integer quantidadeAulasSemana) { this.quantidadeAulasSemana = quantidadeAulasSemana; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
    public Academia getAcademia() { return academia; }
    public void setAcademia(Academia academia) { this.academia = academia; }
}
