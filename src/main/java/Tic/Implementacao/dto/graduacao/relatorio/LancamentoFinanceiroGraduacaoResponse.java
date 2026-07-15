package Tic.Implementacao.dto.graduacao.relatorio;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class LancamentoFinanceiroGraduacaoResponse {

    private Long pagamentoId;
    private Long inscricaoId;
    private Long eventoId;
    private String eventoTitulo;
    private Long alunoId;
    private String alunoNome;
    private String graduacaoDestino;
    private BigDecimal valorBruto;
    private BigDecimal taxa;
    private BigDecimal valorLiquido;
    private String statusPagamento;
    private String metodoPagamento;
    private String provedor;
    private String idPagamentoExterno;
    private LocalDateTime pagoEm;

    public LancamentoFinanceiroGraduacaoResponse(Long pagamentoId, Long inscricaoId, Long eventoId,
                                                 String eventoTitulo, Long alunoId, String alunoNome,
                                                 String graduacaoDestino, BigDecimal valorBruto,
                                                 BigDecimal taxa, BigDecimal valorLiquido,
                                                 String statusPagamento, String metodoPagamento,
                                                 String provedor, String idPagamentoExterno,
                                                 LocalDateTime pagoEm) {
        this.pagamentoId = pagamentoId;
        this.inscricaoId = inscricaoId;
        this.eventoId = eventoId;
        this.eventoTitulo = eventoTitulo;
        this.alunoId = alunoId;
        this.alunoNome = alunoNome;
        this.graduacaoDestino = graduacaoDestino;
        this.valorBruto = valorBruto;
        this.taxa = taxa;
        this.valorLiquido = valorLiquido;
        this.statusPagamento = statusPagamento;
        this.metodoPagamento = metodoPagamento;
        this.provedor = provedor;
        this.idPagamentoExterno = idPagamentoExterno;
        this.pagoEm = pagoEm;
    }

    public Long getPagamentoId() { return pagamentoId; }
    public Long getInscricaoId() { return inscricaoId; }
    public Long getEventoId() { return eventoId; }
    public String getEventoTitulo() { return eventoTitulo; }
    public Long getAlunoId() { return alunoId; }
    public String getAlunoNome() { return alunoNome; }
    public String getGraduacaoDestino() { return graduacaoDestino; }
    public BigDecimal getValorBruto() { return valorBruto; }
    public BigDecimal getTaxa() { return taxa; }
    public BigDecimal getValorLiquido() { return valorLiquido; }
    public String getStatusPagamento() { return statusPagamento; }
    public String getMetodoPagamento() { return metodoPagamento; }
    public String getProvedor() { return provedor; }
    public String getIdPagamentoExterno() { return idPagamentoExterno; }
    public LocalDateTime getPagoEm() { return pagoEm; }
}
