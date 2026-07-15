package Tic.Implementacao.dto.graduacao;

import Tic.Implementacao.model.HistoricoGraduacao;

import java.time.LocalDateTime;

public class HistoricoGraduacaoResponse {

    private Long id;
    private Long alunoId;
    private String alunoNome;
    private Long graduacaoAnteriorId;
    private String graduacaoAnteriorNome;
    private Long novaGraduacaoId;
    private String novaGraduacaoNome;
    private LocalDateTime alteradoEm;
    private Long alteradoPorId;
    private String alteradoPorNome;
    private String motivo;
    private String observacao;
    private Boolean promocaoPorAvaliacao;

    public static HistoricoGraduacaoResponse from(HistoricoGraduacao historico) {
        HistoricoGraduacaoResponse response = new HistoricoGraduacaoResponse();
        response.id = historico.getId();
        response.alunoId = historico.getAluno().getId();
        response.alunoNome = historico.getAluno().getNome();
        response.graduacaoAnteriorId = historico.getGraduacaoAnterior() == null ? null : historico.getGraduacaoAnterior().getId();
        response.graduacaoAnteriorNome = historico.getGraduacaoAnterior() == null ? null : historico.getGraduacaoAnterior().getNome();
        response.novaGraduacaoId = historico.getNovaGraduacao().getId();
        response.novaGraduacaoNome = historico.getNovaGraduacao().getNome();
        response.alteradoEm = historico.getAlteradoEm();
        response.alteradoPorId = historico.getAlteradoPor().getId();
        response.alteradoPorNome = historico.getAlteradoPor().getNome();
        response.motivo = historico.getMotivo().name();
        response.observacao = historico.getObservacao();
        response.promocaoPorAvaliacao = historico.getPromocaoPorAvaliacao();
        return response;
    }

    public Long getId() { return id; }
    public Long getAlunoId() { return alunoId; }
    public String getAlunoNome() { return alunoNome; }
    public Long getGraduacaoAnteriorId() { return graduacaoAnteriorId; }
    public String getGraduacaoAnteriorNome() { return graduacaoAnteriorNome; }
    public Long getNovaGraduacaoId() { return novaGraduacaoId; }
    public String getNovaGraduacaoNome() { return novaGraduacaoNome; }
    public LocalDateTime getAlteradoEm() { return alteradoEm; }
    public Long getAlteradoPorId() { return alteradoPorId; }
    public String getAlteradoPorNome() { return alteradoPorNome; }
    public String getMotivo() { return motivo; }
    public String getObservacao() { return observacao; }
    public Boolean getPromocaoPorAvaliacao() { return promocaoPorAvaliacao; }
}
