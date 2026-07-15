package Tic.Implementacao.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "inscricoes_graduacao",
        uniqueConstraints = @UniqueConstraint(name = "uk_inscricao_graduacao_ativa", columnNames = "chave_inscricao_ativa")
)
public class InscricaoGraduacao {

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
    @JoinColumn(name = "graduacao_destino_id", nullable = false)
    private Graduacao graduacaoDestino;

    @ManyToOne
    @JoinColumn(name = "convite_id")
    private ConviteGraduacao convite;

    @ManyToOne
    @JoinColumn(name = "pagamento_id")
    private Pagamento pagamento;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private StatusInscricaoGraduacao status = StatusInscricaoGraduacao.PENDENTE_PAGAMENTO;

    @Column(nullable = false)
    private LocalDateTime inscritoEm;

    private LocalDateTime canceladoEm;

    private LocalDateTime pagoEm;

    @Column(length = 500)
    private String observacao;

    @Column(name = "chave_inscricao_ativa", unique = true, length = 80)
    private String chaveInscricaoAtiva;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Academia getAcademia() { return academia; }
    public void setAcademia(Academia academia) { this.academia = academia; }
    public EventoGraduacao getEvento() { return evento; }
    public void setEvento(EventoGraduacao evento) { this.evento = evento; }
    public Aluno getAluno() { return aluno; }
    public void setAluno(Aluno aluno) { this.aluno = aluno; }
    public Graduacao getGraduacaoDestino() { return graduacaoDestino; }
    public void setGraduacaoDestino(Graduacao graduacaoDestino) { this.graduacaoDestino = graduacaoDestino; }
    public ConviteGraduacao getConvite() { return convite; }
    public void setConvite(ConviteGraduacao convite) { this.convite = convite; }
    public Pagamento getPagamento() { return pagamento; }
    public void setPagamento(Pagamento pagamento) { this.pagamento = pagamento; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
    public StatusInscricaoGraduacao getStatus() { return status; }
    public void setStatus(StatusInscricaoGraduacao status) { this.status = status; }
    public LocalDateTime getInscritoEm() { return inscritoEm; }
    public void setInscritoEm(LocalDateTime inscritoEm) { this.inscritoEm = inscritoEm; }
    public LocalDateTime getCanceladoEm() { return canceladoEm; }
    public void setCanceladoEm(LocalDateTime canceladoEm) { this.canceladoEm = canceladoEm; }
    public LocalDateTime getPagoEm() { return pagoEm; }
    public void setPagoEm(LocalDateTime pagoEm) { this.pagoEm = pagoEm; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
    public String getChaveInscricaoAtiva() { return chaveInscricaoAtiva; }
    public void setChaveInscricaoAtiva(String chaveInscricaoAtiva) { this.chaveInscricaoAtiva = chaveInscricaoAtiva; }
}
