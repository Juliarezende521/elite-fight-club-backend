package Tic.Implementacao.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "dashboard_preferencias")
public class DashboardPreferencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Usuário é obrigatório")
    @OneToOne
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @NotBlank(message = "Cards visíveis são obrigatórios")
    @Column(nullable = false, length = 1000)
    private String cardsVisiveis;

    @Column(length = 1000)
    private String ordemCards;

    @Column(length = 100)
    private String cardPrincipal;

    @Column(length = 50)
    private String tema = "CLARO";

    @Column(length = 3000)
    private String layoutJson;

    public DashboardPreferencia() {
    }

    public DashboardPreferencia(Usuario usuario, String cardsVisiveis, String ordemCards,
                                String cardPrincipal, String tema, String layoutJson) {
        this.usuario = usuario;
        this.cardsVisiveis = cardsVisiveis;
        this.ordemCards = ordemCards;
        this.cardPrincipal = cardPrincipal;
        this.tema = tema;
        this.layoutJson = layoutJson;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getCardsVisiveis() {
        return cardsVisiveis;
    }

    public void setCardsVisiveis(String cardsVisiveis) {
        this.cardsVisiveis = cardsVisiveis;
    }

    public String getOrdemCards() {
        return ordemCards;
    }

    public void setOrdemCards(String ordemCards) {
        this.ordemCards = ordemCards;
    }

    public String getCardPrincipal() {
        return cardPrincipal;
    }

    public void setCardPrincipal(String cardPrincipal) {
        this.cardPrincipal = cardPrincipal;
    }

    public String getTema() {
        return tema;
    }

    public void setTema(String tema) {
        this.tema = tema;
    }

    public String getLayoutJson() {
        return layoutJson;
    }

    public void setLayoutJson(String layoutJson) {
        this.layoutJson = layoutJson;
    }
}
