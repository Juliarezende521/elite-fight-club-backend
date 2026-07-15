package Tic.Implementacao.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "historico_graduacoes")
public class HistoricoGraduacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "academia_id", nullable = false)
    private Academia academia;

    @ManyToOne(optional = false)
    @JoinColumn(name = "aluno_id", nullable = false)
    private Aluno aluno;

    @ManyToOne
    @JoinColumn(name = "graduacao_anterior_id")
    private Graduacao graduacaoAnterior;

    @ManyToOne(optional = false)
    @JoinColumn(name = "nova_graduacao_id", nullable = false)
    private Graduacao novaGraduacao;

    @Column(nullable = false)
    private LocalDateTime alteradoEm;

    @ManyToOne(optional = false)
    @JoinColumn(name = "alterado_por_id", nullable = false)
    private Usuario alteradoPor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private MotivoAlteracaoGraduacao motivo;

    @Column(length = 500)
    private String observacao;

    @Column(nullable = false)
    private Boolean promocaoPorAvaliacao = false;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Academia getAcademia() { return academia; }
    public void setAcademia(Academia academia) { this.academia = academia; }
    public Aluno getAluno() { return aluno; }
    public void setAluno(Aluno aluno) { this.aluno = aluno; }
    public Graduacao getGraduacaoAnterior() { return graduacaoAnterior; }
    public void setGraduacaoAnterior(Graduacao graduacaoAnterior) { this.graduacaoAnterior = graduacaoAnterior; }
    public Graduacao getNovaGraduacao() { return novaGraduacao; }
    public void setNovaGraduacao(Graduacao novaGraduacao) { this.novaGraduacao = novaGraduacao; }
    public LocalDateTime getAlteradoEm() { return alteradoEm; }
    public void setAlteradoEm(LocalDateTime alteradoEm) { this.alteradoEm = alteradoEm; }
    public Usuario getAlteradoPor() { return alteradoPor; }
    public void setAlteradoPor(Usuario alteradoPor) { this.alteradoPor = alteradoPor; }
    public MotivoAlteracaoGraduacao getMotivo() { return motivo; }
    public void setMotivo(MotivoAlteracaoGraduacao motivo) { this.motivo = motivo; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
    public Boolean getPromocaoPorAvaliacao() { return promocaoPorAvaliacao; }
    public void setPromocaoPorAvaliacao(Boolean promocaoPorAvaliacao) { this.promocaoPorAvaliacao = promocaoPorAvaliacao; }
}
