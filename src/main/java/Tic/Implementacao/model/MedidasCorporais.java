package Tic.Implementacao.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "medidas_corporais")
public class MedidasCorporais {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "avaliacao_id", nullable = false, unique = true)
    private AvaliacaoCorporal avaliacao;

    @Column(precision = 6, scale = 2) private BigDecimal pescoco;
    @Column(precision = 6, scale = 2) private BigDecimal ombros;
    @Column(precision = 6, scale = 2) private BigDecimal torax;
    @Column(precision = 6, scale = 2) private BigDecimal cintura;
    @Column(precision = 6, scale = 2) private BigDecimal abdomen;
    @Column(precision = 6, scale = 2) private BigDecimal quadril;
    @Column(precision = 6, scale = 2) private BigDecimal bracoDireito;
    @Column(precision = 6, scale = 2) private BigDecimal bracoEsquerdo;
    @Column(precision = 6, scale = 2) private BigDecimal antebracoDireito;
    @Column(precision = 6, scale = 2) private BigDecimal antebracoEsquerdo;
    @Column(precision = 6, scale = 2) private BigDecimal coxaDireita;
    @Column(precision = 6, scale = 2) private BigDecimal coxaEsquerda;
    @Column(precision = 6, scale = 2) private BigDecimal panturrilhaDireita;
    @Column(precision = 6, scale = 2) private BigDecimal panturrilhaEsquerda;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public AvaliacaoCorporal getAvaliacao() { return avaliacao; }
    public void setAvaliacao(AvaliacaoCorporal avaliacao) { this.avaliacao = avaliacao; }
    public BigDecimal getPescoco() { return pescoco; }
    public void setPescoco(BigDecimal pescoco) { this.pescoco = pescoco; }
    public BigDecimal getOmbros() { return ombros; }
    public void setOmbros(BigDecimal ombros) { this.ombros = ombros; }
    public BigDecimal getTorax() { return torax; }
    public void setTorax(BigDecimal torax) { this.torax = torax; }
    public BigDecimal getCintura() { return cintura; }
    public void setCintura(BigDecimal cintura) { this.cintura = cintura; }
    public BigDecimal getAbdomen() { return abdomen; }
    public void setAbdomen(BigDecimal abdomen) { this.abdomen = abdomen; }
    public BigDecimal getQuadril() { return quadril; }
    public void setQuadril(BigDecimal quadril) { this.quadril = quadril; }
    public BigDecimal getBracoDireito() { return bracoDireito; }
    public void setBracoDireito(BigDecimal bracoDireito) { this.bracoDireito = bracoDireito; }
    public BigDecimal getBracoEsquerdo() { return bracoEsquerdo; }
    public void setBracoEsquerdo(BigDecimal bracoEsquerdo) { this.bracoEsquerdo = bracoEsquerdo; }
    public BigDecimal getAntebracoDireito() { return antebracoDireito; }
    public void setAntebracoDireito(BigDecimal antebracoDireito) { this.antebracoDireito = antebracoDireito; }
    public BigDecimal getAntebracoEsquerdo() { return antebracoEsquerdo; }
    public void setAntebracoEsquerdo(BigDecimal antebracoEsquerdo) { this.antebracoEsquerdo = antebracoEsquerdo; }
    public BigDecimal getCoxaDireita() { return coxaDireita; }
    public void setCoxaDireita(BigDecimal coxaDireita) { this.coxaDireita = coxaDireita; }
    public BigDecimal getCoxaEsquerda() { return coxaEsquerda; }
    public void setCoxaEsquerda(BigDecimal coxaEsquerda) { this.coxaEsquerda = coxaEsquerda; }
    public BigDecimal getPanturrilhaDireita() { return panturrilhaDireita; }
    public void setPanturrilhaDireita(BigDecimal panturrilhaDireita) { this.panturrilhaDireita = panturrilhaDireita; }
    public BigDecimal getPanturrilhaEsquerda() { return panturrilhaEsquerda; }
    public void setPanturrilhaEsquerda(BigDecimal panturrilhaEsquerda) { this.panturrilhaEsquerda = panturrilhaEsquerda; }
}
