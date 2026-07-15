package Tic.Implementacao.dto.graduacao.avaliacao;

import Tic.Implementacao.model.ResultadoAvaliacaoGraduacao;
import Tic.Implementacao.model.StatusInscricaoGraduacao;
import Tic.Implementacao.model.StatusPagamento;
import Tic.Implementacao.model.StatusPresencaGraduacao;

import java.time.LocalDateTime;

public class ParticipanteGraduacaoResponse {
    private Long inscricaoId;
    private Long alunoId;
    private String alunoNome;
    private String graduacaoAtual;
    private String graduacaoDestino;
    private StatusPagamento statusPagamento;
    private StatusInscricaoGraduacao statusInscricao;
    private StatusPresencaGraduacao statusPresenca;
    private ResultadoAvaliacaoGraduacao resultado;
    private Boolean promocaoConfirmada;
    private LocalDateTime promovidoEm;

    public ParticipanteGraduacaoResponse(Long inscricaoId, Long alunoId, String alunoNome, String graduacaoAtual,
                                          String graduacaoDestino, StatusPagamento statusPagamento,
                                          StatusInscricaoGraduacao statusInscricao,
                                          StatusPresencaGraduacao statusPresenca,
                                          ResultadoAvaliacaoGraduacao resultado,
                                          Boolean promocaoConfirmada, LocalDateTime promovidoEm) {
        this.inscricaoId = inscricaoId;
        this.alunoId = alunoId;
        this.alunoNome = alunoNome;
        this.graduacaoAtual = graduacaoAtual;
        this.graduacaoDestino = graduacaoDestino;
        this.statusPagamento = statusPagamento;
        this.statusInscricao = statusInscricao;
        this.statusPresenca = statusPresenca;
        this.resultado = resultado;
        this.promocaoConfirmada = promocaoConfirmada;
        this.promovidoEm = promovidoEm;
    }

    public Long getInscricaoId() { return inscricaoId; }
    public Long getAlunoId() { return alunoId; }
    public String getAlunoNome() { return alunoNome; }
    public String getGraduacaoAtual() { return graduacaoAtual; }
    public String getGraduacaoDestino() { return graduacaoDestino; }
    public StatusPagamento getStatusPagamento() { return statusPagamento; }
    public StatusInscricaoGraduacao getStatusInscricao() { return statusInscricao; }
    public StatusPresencaGraduacao getStatusPresenca() { return statusPresenca; }
    public ResultadoAvaliacaoGraduacao getResultado() { return resultado; }
    public Boolean getPromocaoConfirmada() { return promocaoConfirmada; }
    public LocalDateTime getPromovidoEm() { return promovidoEm; }
}
