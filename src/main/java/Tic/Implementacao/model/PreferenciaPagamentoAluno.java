package Tic.Implementacao.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "preferencias_pagamento_alunos",
        uniqueConstraints = @UniqueConstraint(name = "uk_preferencia_pagamento_aluno", columnNames = "aluno_id")
)
public class PreferenciaPagamentoAluno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "aluno_id", nullable = false)
    private Aluno aluno;

    @ManyToOne(optional = false)
    @JoinColumn(name = "academia_id", nullable = false)
    private Academia academia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private FormaPagamentoPreferida formaPreferida;

    @Column(nullable = false)
    private Boolean ativa = true;

    @Column(nullable = false)
    private Boolean pagamentoAutomaticoAutorizado = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusAutorizacaoPagamento statusAutorizacao = StatusAutorizacaoPagamento.NAO_SOLICITADA;

    @Column(length = 60)
    private String provedor;

    @Column(length = 120)
    private String identificadorExternoMetodo;

    @Column(length = 120)
    private String descricaoMascarada;

    @Column(nullable = false)
    private LocalDateTime criadaEm;

    @Column(nullable = false)
    private LocalDateTime atualizadaEm;

    @ManyToOne(optional = false)
    @JoinColumn(name = "criada_por_id", nullable = false)
    private Usuario criadaPor;

    @ManyToOne
    @JoinColumn(name = "atualizada_por_id")
    private Usuario atualizadaPor;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Aluno getAluno() { return aluno; }
    public void setAluno(Aluno aluno) { this.aluno = aluno; }
    public Academia getAcademia() { return academia; }
    public void setAcademia(Academia academia) { this.academia = academia; }
    public FormaPagamentoPreferida getFormaPreferida() { return formaPreferida; }
    public void setFormaPreferida(FormaPagamentoPreferida formaPreferida) { this.formaPreferida = formaPreferida; }
    public Boolean getAtiva() { return ativa; }
    public void setAtiva(Boolean ativa) { this.ativa = ativa; }
    public Boolean getPagamentoAutomaticoAutorizado() { return pagamentoAutomaticoAutorizado; }
    public void setPagamentoAutomaticoAutorizado(Boolean pagamentoAutomaticoAutorizado) { this.pagamentoAutomaticoAutorizado = pagamentoAutomaticoAutorizado; }
    public StatusAutorizacaoPagamento getStatusAutorizacao() { return statusAutorizacao; }
    public void setStatusAutorizacao(StatusAutorizacaoPagamento statusAutorizacao) { this.statusAutorizacao = statusAutorizacao; }
    public String getProvedor() { return provedor; }
    public void setProvedor(String provedor) { this.provedor = provedor; }
    public String getIdentificadorExternoMetodo() { return identificadorExternoMetodo; }
    public void setIdentificadorExternoMetodo(String identificadorExternoMetodo) { this.identificadorExternoMetodo = identificadorExternoMetodo; }
    public String getDescricaoMascarada() { return descricaoMascarada; }
    public void setDescricaoMascarada(String descricaoMascarada) { this.descricaoMascarada = descricaoMascarada; }
    public LocalDateTime getCriadaEm() { return criadaEm; }
    public void setCriadaEm(LocalDateTime criadaEm) { this.criadaEm = criadaEm; }
    public LocalDateTime getAtualizadaEm() { return atualizadaEm; }
    public void setAtualizadaEm(LocalDateTime atualizadaEm) { this.atualizadaEm = atualizadaEm; }
    public Usuario getCriadaPor() { return criadaPor; }
    public void setCriadaPor(Usuario criadaPor) { this.criadaPor = criadaPor; }
    public Usuario getAtualizadaPor() { return atualizadaPor; }
    public void setAtualizadaPor(Usuario atualizadaPor) { this.atualizadaPor = atualizadaPor; }
}
