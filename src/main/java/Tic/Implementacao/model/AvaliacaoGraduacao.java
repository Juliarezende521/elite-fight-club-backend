package Tic.Implementacao.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "avaliacoes_graduacao",
        uniqueConstraints = @UniqueConstraint(name = "uk_avaliacao_graduacao_inscricao", columnNames = "inscricao_id")
)
public class AvaliacaoGraduacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "academia_id", nullable = false)
    private Academia academia;

    @OneToOne(optional = false)
    @JoinColumn(name = "inscricao_id", nullable = false)
    private InscricaoGraduacao inscricao;

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
    @Column(nullable = false, length = 30)
    private StatusPresencaGraduacao statusPresenca = StatusPresencaGraduacao.PENDENTE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ResultadoAvaliacaoGraduacao resultado = ResultadoAvaliacaoGraduacao.PENDENTE;

    @Column(length = 2000)
    private String observacaoTecnica;

    @Column(precision = 4, scale = 2)
    private BigDecimal notaTecnica;

    private LocalDateTime avaliadoEm;

    @ManyToOne
    @JoinColumn(name = "avaliado_por_id")
    private Usuario avaliadoPor;

    @Column(nullable = false)
    private Boolean promocaoConfirmada = false;

    private LocalDateTime promovidoEm;

    @ManyToOne
    @JoinColumn(name = "promovido_por_id")
    private Usuario promovidoPor;

    @ManyToOne
    @JoinColumn(name = "historico_gerado_id")
    private HistoricoGraduacao historicoGerado;

    @Column(nullable = false)
    private Boolean historicoGeradoFlag = false;

    @Column(nullable = false)
    private LocalDateTime criadoEm;

    @Column(nullable = false)
    private LocalDateTime atualizadoEm;

    private LocalDateTime presencaRegistradaEm;

    @ManyToOne
    @JoinColumn(name = "presenca_registrada_por_id")
    private Usuario presencaRegistradaPor;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private StatusPresencaGraduacao statusPresencaAnterior;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private StatusPresencaGraduacao statusPresencaAtual;

    @Column(length = 500)
    private String observacaoPresenca;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private ResultadoAvaliacaoGraduacao resultadoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private ResultadoAvaliacaoGraduacao resultadoAtual;

    @Column(length = 1000)
    private String observacaoPromocao;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Academia getAcademia() { return academia; }
    public void setAcademia(Academia academia) { this.academia = academia; }
    public InscricaoGraduacao getInscricao() { return inscricao; }
    public void setInscricao(InscricaoGraduacao inscricao) { this.inscricao = inscricao; }
    public EventoGraduacao getEvento() { return evento; }
    public void setEvento(EventoGraduacao evento) { this.evento = evento; }
    public Aluno getAluno() { return aluno; }
    public void setAluno(Aluno aluno) { this.aluno = aluno; }
    public Graduacao getGraduacaoAtual() { return graduacaoAtual; }
    public void setGraduacaoAtual(Graduacao graduacaoAtual) { this.graduacaoAtual = graduacaoAtual; }
    public Graduacao getGraduacaoDestino() { return graduacaoDestino; }
    public void setGraduacaoDestino(Graduacao graduacaoDestino) { this.graduacaoDestino = graduacaoDestino; }
    public StatusPresencaGraduacao getStatusPresenca() { return statusPresenca; }
    public void setStatusPresenca(StatusPresencaGraduacao statusPresenca) { this.statusPresenca = statusPresenca; }
    public ResultadoAvaliacaoGraduacao getResultado() { return resultado; }
    public void setResultado(ResultadoAvaliacaoGraduacao resultado) { this.resultado = resultado; }
    public String getObservacaoTecnica() { return observacaoTecnica; }
    public void setObservacaoTecnica(String observacaoTecnica) { this.observacaoTecnica = observacaoTecnica; }
    public BigDecimal getNotaTecnica() { return notaTecnica; }
    public void setNotaTecnica(BigDecimal notaTecnica) { this.notaTecnica = notaTecnica; }
    public LocalDateTime getAvaliadoEm() { return avaliadoEm; }
    public void setAvaliadoEm(LocalDateTime avaliadoEm) { this.avaliadoEm = avaliadoEm; }
    public Usuario getAvaliadoPor() { return avaliadoPor; }
    public void setAvaliadoPor(Usuario avaliadoPor) { this.avaliadoPor = avaliadoPor; }
    public Boolean getPromocaoConfirmada() { return promocaoConfirmada; }
    public void setPromocaoConfirmada(Boolean promocaoConfirmada) { this.promocaoConfirmada = promocaoConfirmada; }
    public LocalDateTime getPromovidoEm() { return promovidoEm; }
    public void setPromovidoEm(LocalDateTime promovidoEm) { this.promovidoEm = promovidoEm; }
    public Usuario getPromovidoPor() { return promovidoPor; }
    public void setPromovidoPor(Usuario promovidoPor) { this.promovidoPor = promovidoPor; }
    public HistoricoGraduacao getHistoricoGerado() { return historicoGerado; }
    public void setHistoricoGerado(HistoricoGraduacao historicoGerado) { this.historicoGerado = historicoGerado; }
    public Boolean getHistoricoGeradoFlag() { return historicoGeradoFlag; }
    public void setHistoricoGeradoFlag(Boolean historicoGeradoFlag) { this.historicoGeradoFlag = historicoGeradoFlag; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }
    public void setAtualizadoEm(LocalDateTime atualizadoEm) { this.atualizadoEm = atualizadoEm; }
    public LocalDateTime getPresencaRegistradaEm() { return presencaRegistradaEm; }
    public void setPresencaRegistradaEm(LocalDateTime presencaRegistradaEm) { this.presencaRegistradaEm = presencaRegistradaEm; }
    public Usuario getPresencaRegistradaPor() { return presencaRegistradaPor; }
    public void setPresencaRegistradaPor(Usuario presencaRegistradaPor) { this.presencaRegistradaPor = presencaRegistradaPor; }
    public StatusPresencaGraduacao getStatusPresencaAnterior() { return statusPresencaAnterior; }
    public void setStatusPresencaAnterior(StatusPresencaGraduacao statusPresencaAnterior) { this.statusPresencaAnterior = statusPresencaAnterior; }
    public StatusPresencaGraduacao getStatusPresencaAtual() { return statusPresencaAtual; }
    public void setStatusPresencaAtual(StatusPresencaGraduacao statusPresencaAtual) { this.statusPresencaAtual = statusPresencaAtual; }
    public String getObservacaoPresenca() { return observacaoPresenca; }
    public void setObservacaoPresenca(String observacaoPresenca) { this.observacaoPresenca = observacaoPresenca; }
    public ResultadoAvaliacaoGraduacao getResultadoAnterior() { return resultadoAnterior; }
    public void setResultadoAnterior(ResultadoAvaliacaoGraduacao resultadoAnterior) { this.resultadoAnterior = resultadoAnterior; }
    public ResultadoAvaliacaoGraduacao getResultadoAtual() { return resultadoAtual; }
    public void setResultadoAtual(ResultadoAvaliacaoGraduacao resultadoAtual) { this.resultadoAtual = resultadoAtual; }
    public String getObservacaoPromocao() { return observacaoPromocao; }
    public void setObservacaoPromocao(String observacaoPromocao) { this.observacaoPromocao = observacaoPromocao; }
}
