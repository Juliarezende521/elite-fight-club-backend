package Tic.Implementacao.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Entity
@Table(name = "modalidades")
public class Modalidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome da modalidade é obrigatório")
    @Column(nullable = false, length = 100)
    private String nome;

    @Column(length = 255)
    private String descricao;

    @Column(nullable = false)
    private boolean ativa = true;

    @NotNull(message = "Tipo da modalidade é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoModalidade tipoModalidade = TipoModalidade.TURMA;

    @NotNull(message = "Valor da mensalidade é obrigatório")
    @DecimalMin(value = "0.00", message = "Valor da mensalidade não pode ser negativo")
    @Column(nullable = false)
    private Double valorMensalidade = 0.0;

    @OneToMany(mappedBy = "modalidade")
    @JsonIgnore
    private List<Horario> horarios;

    public Modalidade() {}

    public Modalidade(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
        this.ativa = true;
    }

    public Modalidade(String nome, String descricao, TipoModalidade tipoModalidade, Double valorMensalidade) {
        this.nome = nome;
        this.descricao = descricao;
        this.ativa = true;
        this.tipoModalidade = tipoModalidade;
        this.valorMensalidade = valorMensalidade;
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
        if (nome == null || nome.isBlank())
            throw new IllegalArgumentException("Nome não pode ser nulo ou vazio");
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public boolean isAtiva() {
        return ativa;
    }

    public void setAtiva(boolean ativa) {
        this.ativa = ativa;
    }

    public TipoModalidade getTipoModalidade() {
        return tipoModalidade;
    }

    public void setTipoModalidade(TipoModalidade tipoModalidade) {
        if (tipoModalidade == null) throw new IllegalArgumentException("Tipo de modalidade inválido");
        this.tipoModalidade = tipoModalidade;
    }

    public Double getValorMensalidade() {
        return valorMensalidade;
    }

    public void setValorMensalidade(Double valorMensalidade) {
        if (valorMensalidade == null || valorMensalidade < 0)
            throw new IllegalArgumentException("Valor da mensalidade inválido");
        this.valorMensalidade = valorMensalidade;
    }

    public List<Horario> getHorarios() {
        return horarios;
    }

    public void setHorarios(List<Horario> horarios) {
        this.horarios = horarios;
    }
}
