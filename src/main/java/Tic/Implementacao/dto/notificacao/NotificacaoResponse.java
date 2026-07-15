package Tic.Implementacao.dto.notificacao;

import Tic.Implementacao.model.Notificacao;
import Tic.Implementacao.model.TipoNotificacao;

import java.time.LocalDateTime;

public class NotificacaoResponse {

    private Long id;
    private TipoNotificacao tipo;
    private String titulo;
    private String mensagem;
    private Boolean lida;
    private LocalDateTime criadaEm;
    private String referenciaTipo;
    private Long referenciaId;

    public NotificacaoResponse(Long id, TipoNotificacao tipo, String titulo, String mensagem, Boolean lida,
                               LocalDateTime criadaEm, String referenciaTipo, Long referenciaId) {
        this.id = id;
        this.tipo = tipo;
        this.titulo = titulo;
        this.mensagem = mensagem;
        this.lida = lida;
        this.criadaEm = criadaEm;
        this.referenciaTipo = referenciaTipo;
        this.referenciaId = referenciaId;
    }

    public static NotificacaoResponse from(Notificacao notificacao) {
        return new NotificacaoResponse(
                notificacao.getId(),
                notificacao.getTipoNotificacao(),
                notificacao.getTitulo(),
                notificacao.getMensagem(),
                notificacao.getLida(),
                notificacao.getCriadaEm(),
                notificacao.getReferenciaTipo(),
                notificacao.getReferenciaId()
        );
    }

    public Long getId() { return id; }
    public TipoNotificacao getTipo() { return tipo; }
    public String getTitulo() { return titulo; }
    public String getMensagem() { return mensagem; }
    public Boolean getLida() { return lida; }
    public LocalDateTime getCriadaEm() { return criadaEm; }
    public String getReferenciaTipo() { return referenciaTipo; }
    public Long getReferenciaId() { return referenciaId; }
}
