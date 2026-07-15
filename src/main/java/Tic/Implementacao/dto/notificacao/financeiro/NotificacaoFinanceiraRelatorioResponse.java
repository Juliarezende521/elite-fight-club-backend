package Tic.Implementacao.dto.notificacao.financeiro;

import Tic.Implementacao.model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class NotificacaoFinanceiraRelatorioResponse {
    private Long notificacaoId;
    private Long alunoId;
    private String alunoNome;
    private Long faturaId;
    private String competencia;
    private LocalDate vencimento;
    private TipoNotificacao tipo;
    private StatusNotificacao status;
    private LocalDateTime criadaEm;
    private LocalDateTime lidaEm;
    private String motivoFalha;
    private String referenciaTipo;
    private Long referenciaId;

    public NotificacaoFinanceiraRelatorioResponse(Long notificacaoId, Long alunoId, String alunoNome,
                                                  Long faturaId, String competencia, LocalDate vencimento,
                                                  TipoNotificacao tipo, StatusNotificacao status,
                                                  LocalDateTime criadaEm, LocalDateTime lidaEm,
                                                  String motivoFalha, String referenciaTipo, Long referenciaId) {
        this.notificacaoId = notificacaoId;
        this.alunoId = alunoId;
        this.alunoNome = alunoNome;
        this.faturaId = faturaId;
        this.competencia = competencia;
        this.vencimento = vencimento;
        this.tipo = tipo;
        this.status = status;
        this.criadaEm = criadaEm;
        this.lidaEm = lidaEm;
        this.motivoFalha = motivoFalha;
        this.referenciaTipo = referenciaTipo;
        this.referenciaId = referenciaId;
    }

    public static NotificacaoFinanceiraRelatorioResponse from(Notificacao notificacao, Fatura fatura) {
        Aluno aluno = fatura == null ? notificacao.getAluno() : fatura.getAluno();
        return new NotificacaoFinanceiraRelatorioResponse(
                notificacao.getId(),
                aluno == null ? null : aluno.getId(),
                aluno == null ? null : aluno.getNome(),
                fatura == null ? null : fatura.getId(),
                fatura == null ? null : fatura.getMesReferencia().toString(),
                fatura == null ? null : fatura.getDataVencimento(),
                notificacao.getTipoNotificacao(),
                notificacao.getStatus(),
                notificacao.getCriadaEm(),
                notificacao.getLidaEm(),
                notificacao.getMotivoFalha(),
                notificacao.getReferenciaTipo(),
                notificacao.getReferenciaId());
    }

    public Long getNotificacaoId() { return notificacaoId; }
    public Long getAlunoId() { return alunoId; }
    public String getAlunoNome() { return alunoNome; }
    public Long getFaturaId() { return faturaId; }
    public String getCompetencia() { return competencia; }
    public LocalDate getVencimento() { return vencimento; }
    public TipoNotificacao getTipo() { return tipo; }
    public StatusNotificacao getStatus() { return status; }
    public LocalDateTime getCriadaEm() { return criadaEm; }
    public LocalDateTime getLidaEm() { return lidaEm; }
    public String getMotivoFalha() { return motivoFalha; }
    public String getReferenciaTipo() { return referenciaTipo; }
    public Long getReferenciaId() { return referenciaId; }
}
