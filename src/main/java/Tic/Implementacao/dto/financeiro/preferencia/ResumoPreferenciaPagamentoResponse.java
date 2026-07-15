package Tic.Implementacao.dto.financeiro.preferencia;

public class ResumoPreferenciaPagamentoResponse {
    private String formaPreferida;
    private String statusAutorizacao;
    private Boolean pagamentoAutomaticoAtivo;
    private String descricaoMascarada;

    public ResumoPreferenciaPagamentoResponse(String formaPreferida, String statusAutorizacao,
                                              Boolean pagamentoAutomaticoAtivo, String descricaoMascarada) {
        this.formaPreferida = formaPreferida;
        this.statusAutorizacao = statusAutorizacao;
        this.pagamentoAutomaticoAtivo = pagamentoAutomaticoAtivo;
        this.descricaoMascarada = descricaoMascarada;
    }

    public String getFormaPreferida() { return formaPreferida; }
    public String getStatusAutorizacao() { return statusAutorizacao; }
    public Boolean getPagamentoAutomaticoAtivo() { return pagamentoAutomaticoAtivo; }
    public String getDescricaoMascarada() { return descricaoMascarada; }
}
