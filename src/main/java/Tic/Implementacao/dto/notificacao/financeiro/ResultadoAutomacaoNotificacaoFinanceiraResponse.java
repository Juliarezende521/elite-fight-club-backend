package Tic.Implementacao.dto.notificacao.financeiro;

import java.time.LocalDate;
import java.util.Map;

public class ResultadoAutomacaoNotificacaoFinanceiraResponse {
    private LocalDate dataReferencia;
    private Integer faturasAnalisadas;
    private Integer notificacoesCriadas;
    private Integer notificacoesIgnoradas;
    private Integer falhas;
    private Map<String, Integer> motivosIgnorados;

    public ResultadoAutomacaoNotificacaoFinanceiraResponse(LocalDate dataReferencia, Integer faturasAnalisadas,
                                                           Integer notificacoesCriadas, Integer notificacoesIgnoradas,
                                                           Integer falhas, Map<String, Integer> motivosIgnorados) {
        this.dataReferencia = dataReferencia;
        this.faturasAnalisadas = faturasAnalisadas;
        this.notificacoesCriadas = notificacoesCriadas;
        this.notificacoesIgnoradas = notificacoesIgnoradas;
        this.falhas = falhas;
        this.motivosIgnorados = motivosIgnorados;
    }

    public LocalDate getDataReferencia() { return dataReferencia; }
    public Integer getFaturasAnalisadas() { return faturasAnalisadas; }
    public Integer getNotificacoesCriadas() { return notificacoesCriadas; }
    public Integer getNotificacoesIgnoradas() { return notificacoesIgnoradas; }
    public Integer getFalhas() { return falhas; }
    public Map<String, Integer> getMotivosIgnorados() { return motivosIgnorados; }
}
