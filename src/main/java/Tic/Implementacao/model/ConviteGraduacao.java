package Tic.Implementacao.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "convites_graduacao",
        uniqueConstraints = @UniqueConstraint(name = "uk_convite_graduacao", columnNames = {"evento_id", "aluno_id", "graduacao_destino_id"})
)
public class ConviteGraduacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "academia_id", nullable = false)
    private Academia academia;

    @ManyToOne(optional = false)
    @JoinColumn(name = "evento_id", nullable = false)
    private EventoGraduacao evento;

    @ManyToOne(optional = false)
    @JoinColumn(name = "aluno_id", nullable = false)
    private Aluno aluno;

    @ManyToOne(optional = false)
    @JoinColumn(name = "graduacao_atual_id", nullable = false)
    private Graduacao graduacaoAtual;

    @ManyToOne(optional = false)
    @JoinColumn(name = "graduacao_destino_id", nullable = false)
    private Graduacao graduacaoDestino;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private StatusConviteGraduacao status = StatusConviteGraduacao.PENDENTE;

    @Column(nullable = false)
    private LocalDateTime enviadoEm;

    private LocalDateTime visualizadoEm;
    private LocalDateTime respondidoEm;

    @ManyToOne(optional = false)
    @JoinColumn(name = "enviado_por_id", nullable = false)
    private Usuario enviadoPor;

    @Column(length = 500)
    private String justificativa;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Academia getAcademia() { return academia; }
    public void setAcademia(Academia academia) { this.academia = academia; }
    public EventoGraduacao getEvento() { return evento; }
    public void setEvento(EventoGraduacao evento) { this.evento = evento; }
    public Aluno getAluno() { return aluno; }
    public void setAluno(Aluno aluno) { this.aluno = aluno; }
    public Graduacao getGraduacaoAtual() { return graduacaoAtual; }
    public void setGraduacaoAtual(Graduacao graduacaoAtual) { this.graduacaoAtual = graduacaoAtual; }
    public Graduacao getGraduacaoDestino() { return graduacaoDestino; }
    public void setGraduacaoDestino(Graduacao graduacaoDestino) { this.graduacaoDestino = graduacaoDestino; }
    public StatusConviteGraduacao getStatus() { return status; }
    public void setStatus(StatusConviteGraduacao status) { this.status = status; }
    public LocalDateTime getEnviadoEm() { return enviadoEm; }
    public void setEnviadoEm(LocalDateTime enviadoEm) { this.enviadoEm = enviadoEm; }
    public LocalDateTime getVisualizadoEm() { return visualizadoEm; }
    public void setVisualizadoEm(LocalDateTime visualizadoEm) { this.visualizadoEm = visualizadoEm; }
    public LocalDateTime getRespondidoEm() { return respondidoEm; }
    public void setRespondidoEm(LocalDateTime respondidoEm) { this.respondidoEm = respondidoEm; }
    public Usuario getEnviadoPor() { return enviadoPor; }
    public void setEnviadoPor(Usuario enviadoPor) { this.enviadoPor = enviadoPor; }
    public String getJustificativa() { return justificativa; }
    public void setJustificativa(String justificativa) { this.justificativa = justificativa; }
}
