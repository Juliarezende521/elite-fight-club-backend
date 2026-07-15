package Tic.Implementacao.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(
        name = "eventos_graduacao_prajieds",
        uniqueConstraints = @UniqueConstraint(name = "uk_evento_graduacao_prajied", columnNames = {"evento_id", "graduacao_destino_id"})
)
public class EventoGraduacaoPrajied {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "evento_id", nullable = false)
    private EventoGraduacao evento;

    @ManyToOne(optional = false)
    @JoinColumn(name = "graduacao_destino_id", nullable = false)
    private Graduacao graduacaoDestino;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor = BigDecimal.ZERO;

    private Integer limiteVagas;

    @Column(nullable = false)
    private Boolean ativo = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public EventoGraduacao getEvento() { return evento; }
    public void setEvento(EventoGraduacao evento) { this.evento = evento; }
    public Graduacao getGraduacaoDestino() { return graduacaoDestino; }
    public void setGraduacaoDestino(Graduacao graduacaoDestino) { this.graduacaoDestino = graduacaoDestino; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
    public Integer getLimiteVagas() { return limiteVagas; }
    public void setLimiteVagas(Integer limiteVagas) { this.limiteVagas = limiteVagas; }
    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
}
