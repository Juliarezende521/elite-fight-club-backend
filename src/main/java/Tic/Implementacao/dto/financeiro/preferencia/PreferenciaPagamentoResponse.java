package Tic.Implementacao.dto.financeiro.preferencia;

import Tic.Implementacao.model.*;

import java.time.LocalDateTime;

public class PreferenciaPagamentoResponse {
    private Long id;
    private Long alunoId;
    private String alunoNome;
    private FormaPagamentoPreferida formaPreferida;
    private StatusAutorizacaoPagamento statusAutorizacao;
    private Boolean pagamentoAutomaticoAtivo;
    private String provedor;
    private String descricaoMascarada;
    private Boolean ativa;
    private LocalDateTime criadaEm;
    private LocalDateTime atualizadaEm;

    public PreferenciaPagamentoResponse(Long id, Long alunoId, String alunoNome, FormaPagamentoPreferida formaPreferida,
                                        StatusAutorizacaoPagamento statusAutorizacao, Boolean pagamentoAutomaticoAtivo,
                                        String provedor, String descricaoMascarada, Boolean ativa,
                                        LocalDateTime criadaEm, LocalDateTime atualizadaEm) {
        this.id = id;
        this.alunoId = alunoId;
        this.alunoNome = alunoNome;
        this.formaPreferida = formaPreferida;
        this.statusAutorizacao = statusAutorizacao;
        this.pagamentoAutomaticoAtivo = pagamentoAutomaticoAtivo;
        this.provedor = provedor;
        this.descricaoMascarada = descricaoMascarada;
        this.ativa = ativa;
        this.criadaEm = criadaEm;
        this.atualizadaEm = atualizadaEm;
    }

    public static PreferenciaPagamentoResponse from(PreferenciaPagamentoAluno preferencia) {
        return new PreferenciaPagamentoResponse(preferencia.getId(), preferencia.getAluno().getId(),
                preferencia.getAluno().getNome(), preferencia.getFormaPreferida(), preferencia.getStatusAutorizacao(),
                preferencia.getStatusAutorizacao() == StatusAutorizacaoPagamento.ATIVA
                        && Boolean.TRUE.equals(preferencia.getPagamentoAutomaticoAutorizado()),
                preferencia.getProvedor(), preferencia.getDescricaoMascarada(), preferencia.getAtiva(),
                preferencia.getCriadaEm(), preferencia.getAtualizadaEm());
    }

    public Long getId() { return id; }
    public Long getAlunoId() { return alunoId; }
    public String getAlunoNome() { return alunoNome; }
    public FormaPagamentoPreferida getFormaPreferida() { return formaPreferida; }
    public StatusAutorizacaoPagamento getStatusAutorizacao() { return statusAutorizacao; }
    public Boolean getPagamentoAutomaticoAtivo() { return pagamentoAutomaticoAtivo; }
    public String getProvedor() { return provedor; }
    public String getDescricaoMascarada() { return descricaoMascarada; }
    public Boolean getAtiva() { return ativa; }
    public LocalDateTime getCriadaEm() { return criadaEm; }
    public LocalDateTime getAtualizadaEm() { return atualizadaEm; }
}
