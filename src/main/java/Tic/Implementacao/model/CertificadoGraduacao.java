package Tic.Implementacao.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "certificados_graduacao",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_certificado_graduacao_numero", columnNames = "numero_certificado"),
                @UniqueConstraint(name = "uk_certificado_graduacao_codigo", columnNames = "codigo_validacao"),
                @UniqueConstraint(name = "uk_certificado_graduacao_avaliacao", columnNames = "avaliacao_id")
        }
)
public class CertificadoGraduacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "academia_id", nullable = false)
    private Academia academia;

    @ManyToOne(optional = false)
    @JoinColumn(name = "aluno_id", nullable = false)
    private Aluno aluno;

    @ManyToOne(optional = false)
    @JoinColumn(name = "evento_id", nullable = false)
    private EventoGraduacao evento;

    @ManyToOne
    @JoinColumn(name = "graduacao_anterior_id")
    private Graduacao graduacaoAnterior;

    @ManyToOne(optional = false)
    @JoinColumn(name = "graduacao_nova_id", nullable = false)
    private Graduacao graduacaoNova;

    @OneToOne(optional = false)
    @JoinColumn(name = "avaliacao_id", nullable = false)
    private AvaliacaoGraduacao avaliacao;

    @ManyToOne(optional = false)
    @JoinColumn(name = "historico_id", nullable = false)
    private HistoricoGraduacao historico;

    @Column(name = "numero_certificado", nullable = false, length = 30)
    private String numeroCertificado;

    @Column(name = "codigo_validacao", nullable = false, length = 30)
    private String codigoValidacao;

    @Column(nullable = false)
    private LocalDateTime dataEmissao;

    @ManyToOne(optional = false)
    @JoinColumn(name = "emitido_por_id", nullable = false)
    private Usuario emitidoPor;

    @Column(length = 1000)
    private String observacoes;

    @Column(nullable = false)
    private Boolean ativo = true;

    @Column(length = 1000)
    private String motivoCancelamento;

    private LocalDateTime canceladoEm;

    @ManyToOne
    @JoinColumn(name = "cancelado_por_id")
    private Usuario canceladoPor;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Academia getAcademia() { return academia; }
    public void setAcademia(Academia academia) { this.academia = academia; }
    public Aluno getAluno() { return aluno; }
    public void setAluno(Aluno aluno) { this.aluno = aluno; }
    public EventoGraduacao getEvento() { return evento; }
    public void setEvento(EventoGraduacao evento) { this.evento = evento; }
    public Graduacao getGraduacaoAnterior() { return graduacaoAnterior; }
    public void setGraduacaoAnterior(Graduacao graduacaoAnterior) { this.graduacaoAnterior = graduacaoAnterior; }
    public Graduacao getGraduacaoNova() { return graduacaoNova; }
    public void setGraduacaoNova(Graduacao graduacaoNova) { this.graduacaoNova = graduacaoNova; }
    public AvaliacaoGraduacao getAvaliacao() { return avaliacao; }
    public void setAvaliacao(AvaliacaoGraduacao avaliacao) { this.avaliacao = avaliacao; }
    public HistoricoGraduacao getHistorico() { return historico; }
    public void setHistorico(HistoricoGraduacao historico) { this.historico = historico; }
    public String getNumeroCertificado() { return numeroCertificado; }
    public void setNumeroCertificado(String numeroCertificado) { this.numeroCertificado = numeroCertificado; }
    public String getCodigoValidacao() { return codigoValidacao; }
    public void setCodigoValidacao(String codigoValidacao) { this.codigoValidacao = codigoValidacao; }
    public LocalDateTime getDataEmissao() { return dataEmissao; }
    public void setDataEmissao(LocalDateTime dataEmissao) { this.dataEmissao = dataEmissao; }
    public Usuario getEmitidoPor() { return emitidoPor; }
    public void setEmitidoPor(Usuario emitidoPor) { this.emitidoPor = emitidoPor; }
    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
    public String getMotivoCancelamento() { return motivoCancelamento; }
    public void setMotivoCancelamento(String motivoCancelamento) { this.motivoCancelamento = motivoCancelamento; }
    public LocalDateTime getCanceladoEm() { return canceladoEm; }
    public void setCanceladoEm(LocalDateTime canceladoEm) { this.canceladoEm = canceladoEm; }
    public Usuario getCanceladoPor() { return canceladoPor; }
    public void setCanceladoPor(Usuario canceladoPor) { this.canceladoPor = canceladoPor; }
}
