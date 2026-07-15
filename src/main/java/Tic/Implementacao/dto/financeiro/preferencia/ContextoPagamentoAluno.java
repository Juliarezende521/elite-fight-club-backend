package Tic.Implementacao.dto.financeiro.preferencia;

import Tic.Implementacao.model.FormaPagamentoPreferida;
import Tic.Implementacao.model.StatusAutorizacaoPagamento;

public class ContextoPagamentoAluno {
    private FormaPagamentoPreferida formaPreferida;
    private StatusAutorizacaoPagamento statusAutorizacao;
    private Boolean pagamentoAutomaticoAtivo;
    private Boolean possuiPagamentoProcessando;
    private Boolean possuiPixValido;
    private Boolean possuiBoletoValido;

    public ContextoPagamentoAluno(FormaPagamentoPreferida formaPreferida, StatusAutorizacaoPagamento statusAutorizacao,
                                  Boolean pagamentoAutomaticoAtivo, Boolean possuiPagamentoProcessando,
                                  Boolean possuiPixValido) {
        this(formaPreferida, statusAutorizacao, pagamentoAutomaticoAtivo, possuiPagamentoProcessando, possuiPixValido, false);
    }

    public ContextoPagamentoAluno(FormaPagamentoPreferida formaPreferida, StatusAutorizacaoPagamento statusAutorizacao,
                                  Boolean pagamentoAutomaticoAtivo, Boolean possuiPagamentoProcessando,
                                  Boolean possuiPixValido, Boolean possuiBoletoValido) {
        this.formaPreferida = formaPreferida;
        this.statusAutorizacao = statusAutorizacao;
        this.pagamentoAutomaticoAtivo = pagamentoAutomaticoAtivo;
        this.possuiPagamentoProcessando = possuiPagamentoProcessando;
        this.possuiPixValido = possuiPixValido;
        this.possuiBoletoValido = possuiBoletoValido;
    }

    public FormaPagamentoPreferida getFormaPreferida() { return formaPreferida; }
    public StatusAutorizacaoPagamento getStatusAutorizacao() { return statusAutorizacao; }
    public Boolean getPagamentoAutomaticoAtivo() { return pagamentoAutomaticoAtivo; }
    public Boolean getPossuiPagamentoProcessando() { return possuiPagamentoProcessando; }
    public Boolean getPossuiPixValido() { return possuiPixValido; }
    public Boolean getPossuiBoletoValido() { return possuiBoletoValido; }
}
