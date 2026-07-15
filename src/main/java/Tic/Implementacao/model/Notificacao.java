package Tic.Implementacao.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;



@Entity
@Table(name = "notificacoes")
public class Notificacao    {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Tipo de notificação é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoNotificacao tipoNotificacao = TipoNotificacao.AVISO_GERAL;


    @NotBlank(message = "Mensagem é obrigatória")
    @Column(nullable = false)
    private String mensagem;

    @NotNull(message = "Data de envio é obrigatória")
    @Column(nullable = false)
    private LocalDateTime dataEnvio;

    @Column(nullable = false)
    private boolean enviada = false;

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


    public TipoNotificacao getTipoNotificacao() {
        return tipoNotificacao;
    }

    public void setTipoNotificacao(TipoNotificacao tipoNotificacao) {
        this.tipoNotificacao = tipoNotificacao;
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
    }
}
