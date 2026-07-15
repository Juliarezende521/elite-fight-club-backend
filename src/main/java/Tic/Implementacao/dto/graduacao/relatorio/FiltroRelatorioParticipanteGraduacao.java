package Tic.Implementacao.dto.graduacao.relatorio;

import Tic.Implementacao.model.ResultadoAvaliacaoGraduacao;
import Tic.Implementacao.model.StatusInscricaoGraduacao;
import Tic.Implementacao.model.StatusPagamento;
import Tic.Implementacao.model.StatusPresencaGraduacao;

public class FiltroRelatorioParticipanteGraduacao {

    private Long graduacaoDestinoId;
    private StatusInscricaoGraduacao statusInscricao;
    private StatusPagamento statusPagamento;
    private StatusPresencaGraduacao statusPresenca;
    private ResultadoAvaliacaoGraduacao resultadoAvaliacao;
    private Boolean promovido;

    public FiltroRelatorioParticipanteGraduacao(Long graduacaoDestinoId,
                                                StatusInscricaoGraduacao statusInscricao,
                                                StatusPagamento statusPagamento,
                                                StatusPresencaGraduacao statusPresenca,
                                                ResultadoAvaliacaoGraduacao resultadoAvaliacao,
                                                Boolean promovido) {
        this.graduacaoDestinoId = graduacaoDestinoId;
        this.statusInscricao = statusInscricao;
        this.statusPagamento = statusPagamento;
        this.statusPresenca = statusPresenca;
        this.resultadoAvaliacao = resultadoAvaliacao;
        this.promovido = promovido;
    }

    public Long getGraduacaoDestinoId() { return graduacaoDestinoId; }
    public StatusInscricaoGraduacao getStatusInscricao() { return statusInscricao; }
    public StatusPagamento getStatusPagamento() { return statusPagamento; }
    public StatusPresencaGraduacao getStatusPresenca() { return statusPresenca; }
    public ResultadoAvaliacaoGraduacao getResultadoAvaliacao() { return resultadoAvaliacao; }
    public Boolean getPromovido() { return promovido; }
}
