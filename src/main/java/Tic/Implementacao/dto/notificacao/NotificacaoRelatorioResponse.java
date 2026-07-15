package Tic.Implementacao.dto.notificacao;

import Tic.Implementacao.model.Notificacao;
import Tic.Implementacao.model.StatusNotificacao;
import Tic.Implementacao.model.TipoNotificacao;

import java.time.LocalDateTime;

public class NotificacaoRelatorioResponse {

    private Long id;
    private Long destinatarioId;
    private String destinatarioNome;
    private Long alunoId;
    private TipoNotificacao tipo;
    private LocalDateTime criadaEm;
    private Boolean lida;
    private String referenciaTipo;
    private Long referenciaId;
    private StatusNotificacao status;
    private String motivoFalha;

    public NotificacaoRelatorioResponse(Long id, Long destinatarioId, String destinatarioNome, Long alunoId,
                                        TipoNotificacao tipo, LocalDateTime criadaEm, Boolean lida,
                                        String referenciaTipo, Long referenciaId, StatusNotificacao status,
                                        String motivoFalha) {
        this.id = id;
        this.destinatarioId = destinatarioId;
        this.destinatarioNome = destinatarioNome;
        this.alunoId = alunoId;
        this.tipo = tipo;
        this.criadaEm = criadaEm;
        this.lida = lida;
        this.referenciaTipo = referenciaTipo;
        this.referenciaId = referenciaId;
        this.status = status;
        this.motivoFalha = motivoFalha;
    }

    public static NotificacaoRelatorioResponse from(Notificacao notificacao) {
        Long alunoId = notificacao.getDestinatario() == null || notificacao.getDestinatario().getAluno() == null
                ? null
                : notificacao.getDestinatario().getAluno().getId();
        return new NotificacaoRelatorioResponse(
                notificacao.getId(),
                notificacao.getDestinatario() == null ? null : notificacao.getDestinatario().getId(),
                notificacao.getDestinatario() == null ? null : notificacao.getDestinatario().getNome(),
                alunoId,
                notificacao.getTipoNotificacao(),
                notificacao.getCriadaEm(),
                notificacao.getLida(),
                notificacao.getReferenciaTipo(),
                notificacao.getReferenciaId(),
                notificacao.getStatus(),
                notificacao.getMotivoFalha()
        );
    }

    public Long getId() { return id; }
    public Long getDestinatarioId() { return destinatarioId; }
    public String getDestinatarioNome() { return destinatarioNome; }
    public Long getAlunoId() { return alunoId; }
    public TipoNotificacao getTipo() { return tipo; }
    public LocalDateTime getCriadaEm() { return criadaEm; }
    public Boolean getLida() { return lida; }
    public String getReferenciaTipo() { return referenciaTipo; }
    public Long getReferenciaId() { return referenciaId; }
    public StatusNotificacao getStatus() { return status; }
    public String getMotivoFalha() { return motivoFalha; }
}
