package Tic.Implementacao.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;



@Entity
@Table(
        name = "notificacoes",
        uniqueConstraints = @UniqueConstraint(name = "uk_notificacao_chave_idempotencia", columnNames = "chave_idempotencia")
)
public class Notificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "academia_id")
    private Academia academia;

    @ManyToOne
    @JoinColumn(name = "destinatario_id")
    private Usuario destinatario;

    @NotNull(message = "Tipo de notificação é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_notificacao", nullable = false, length = 80)
    private TipoNotificacao tipoNotificacao = TipoNotificacao.AVISO_GERAL;

    @Column(length = 180)
    private String titulo;

    @NotBlank(message = "Mensagem é obrigatória")
    @Column(nullable = false, length = 2000)
    private String mensagem;

    @NotNull(message = "Data de envio é obrigatória")
    @Column(name = "data_envio", nullable = false)
    private LocalDateTime dataEnvio;

    @Column(nullable = false)
    private boolean enviada = false;

    @Column(nullable = false)
    private Boolean lida = false;

    @Column(name = "criada_em")
    private LocalDateTime criadaEm;

    @Column(name = "lida_em")
    private LocalDateTime lidaEm;

    @Column(name = "referencia_tipo", length = 80)
    private String referenciaTipo;

    @Column(name = "referencia_id")
    private Long referenciaId;

    @Column(name = "chave_idempotencia", length = 220)
    private String chaveIdempotencia;

    @Column(name = "agendada_para")
    private LocalDateTime agendadaPara;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private StatusNotificacao status = StatusNotificacao.ENVIADA;

    @Column(name = "motivo_falha", length = 1000)
    private String motivoFalha;

    @ManyToOne
    @JoinColumn(name = "aluno_id")
    private Aluno aluno;

    @ManyToOne
    @JoinColumn(name = "financeiro_id")
    private Financeiro financeiro;

    public Notificacao(){

    }

    public Notificacao(TipoNotificacao tipoNotificacao, String mensagem){
        this.tipoNotificacao = tipoNotificacao;
        this.mensagem = mensagem;
        this.dataEnvio = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Academia getAcademia() {
        return academia;
    }

    public void setAcademia(Academia academia) {
        this.academia = academia;
    }

    public Usuario getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(Usuario destinatario) {
        this.destinatario = destinatario;
    }

    public TipoNotificacao getTipoNotificacao() {
        return tipoNotificacao;
    }

    public void setTipoNotificacao(TipoNotificacao tipoNotificacao) {
        this.tipoNotificacao = tipoNotificacao;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public LocalDateTime getDataEnvio() {
        return dataEnvio;
    }

    public void setDataEnvio(LocalDateTime dataEnvio) {
        this.dataEnvio = dataEnvio;
    }

    public boolean isEnviada() {
        return enviada;
    }

    public void setEnviada(boolean enviada) {
        this.enviada = enviada;
    }

    public Boolean getLida() {
        return lida;
    }

    public void setLida(Boolean lida) {
        this.lida = lida;
    }

    public LocalDateTime getCriadaEm() {
        return criadaEm;
    }

    public void setCriadaEm(LocalDateTime criadaEm) {
        this.criadaEm = criadaEm;
    }

    public LocalDateTime getLidaEm() {
        return lidaEm;
    }

    public void setLidaEm(LocalDateTime lidaEm) {
        this.lidaEm = lidaEm;
    }

    public String getReferenciaTipo() {
        return referenciaTipo;
    }

    public void setReferenciaTipo(String referenciaTipo) {
        this.referenciaTipo = referenciaTipo;
    }

    public Long getReferenciaId() {
        return referenciaId;
    }

    public void setReferenciaId(Long referenciaId) {
        this.referenciaId = referenciaId;
    }

    public String getChaveIdempotencia() {
        return chaveIdempotencia;
    }

    public void setChaveIdempotencia(String chaveIdempotencia) {
        this.chaveIdempotencia = chaveIdempotencia;
    }

    public LocalDateTime getAgendadaPara() {
        return agendadaPara;
    }

    public void setAgendadaPara(LocalDateTime agendadaPara) {
        this.agendadaPara = agendadaPara;
    }

    public StatusNotificacao getStatus() {
        return status;
    }

    public void setStatus(StatusNotificacao status) {
        this.status = status;
    }

    public String getMotivoFalha() {
        return motivoFalha;
    }

    public void setMotivoFalha(String motivoFalha) {
        this.motivoFalha = motivoFalha;
    }

    public Aluno getAluno() {
        return aluno;
    }

    public void setAluno(Aluno aluno) {
        this.aluno = aluno;
    }

    public Financeiro getFinanceiro() {
        return financeiro;
    }

    public void setFinanceiro(Financeiro financeiro) {
        this.financeiro = financeiro;
    }

    public void enviar(){
        this.enviada = true;
        this.dataEnvio = LocalDateTime.now();
        this.status = StatusNotificacao.ENVIADA;
    }
}
