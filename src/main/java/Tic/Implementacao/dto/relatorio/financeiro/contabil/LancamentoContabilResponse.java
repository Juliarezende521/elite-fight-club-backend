package Tic.Implementacao.dto.relatorio.financeiro.contabil;

import Tic.Implementacao.model.TipoCobranca;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class LancamentoContabilResponse {
    private Long pagamentoId;
    private TipoCobranca naturezaReceita;
    private Long alunoId;
    private String alunoNome;
    private String alunoEmail;
    private String alunoDocumento;
    private Long faturaId;
    private String competencia;
    private LocalDate vencimento;
    private Long inscricaoGraduacaoId;
    private Long eventoGraduacaoId;
    private String eventoGraduacaoTitulo;
    private String graduacaoDestino;
    private BigDecimal valorBruto;
    private BigDecimal taxa;
    private BigDecimal valorEstornado;
    private BigDecimal valorLiquido;
    private String metodoPagamento;
    private String provedor;
    private String statusPagamento;
    private String idPagamentoExterno;
    private LocalDateTime criadoEm;
    private LocalDateTime pagoEm;
    private LocalDateTime estornadoEm;

    public LancamentoContabilResponse(Long pagamentoId, TipoCobranca naturezaReceita, Long alunoId, String alunoNome,
                                      String alunoEmail, String alunoDocumento, Long faturaId, String competencia,
                                      LocalDate vencimento, Long inscricaoGraduacaoId, Long eventoGraduacaoId,
                                      String eventoGraduacaoTitulo, String graduacaoDestino, BigDecimal valorBruto,
                                      BigDecimal taxa, BigDecimal valorEstornado, BigDecimal valorLiquido,
                                      String metodoPagamento, String provedor, String statusPagamento,
                                      String idPagamentoExterno, LocalDateTime criadoEm, LocalDateTime pagoEm,
                                      LocalDateTime estornadoEm) {
        this.pagamentoId = pagamentoId;
        this.naturezaReceita = naturezaReceita;
        this.alunoId = alunoId;
        this.alunoNome = alunoNome;
        this.alunoEmail = alunoEmail;
        this.alunoDocumento = alunoDocumento;
        this.faturaId = faturaId;
        this.competencia = competencia;
        this.vencimento = vencimento;
        this.inscricaoGraduacaoId = inscricaoGraduacaoId;
        this.eventoGraduacaoId = eventoGraduacaoId;
        this.eventoGraduacaoTitulo = eventoGraduacaoTitulo;
        this.graduacaoDestino = graduacaoDestino;
        this.valorBruto = valorBruto;
        this.taxa = taxa;
        this.valorEstornado = valorEstornado;
        this.valorLiquido = valorLiquido;
        this.metodoPagamento = metodoPagamento;
        this.provedor = provedor;
        this.statusPagamento = statusPagamento;
        this.idPagamentoExterno = idPagamentoExterno;
        this.criadoEm = criadoEm;
        this.pagoEm = pagoEm;
        this.estornadoEm = estornadoEm;
    }

    public Long getPagamentoId() { return pagamentoId; }
    public TipoCobranca getNaturezaReceita() { return naturezaReceita; }
    public Long getAlunoId() { return alunoId; }
    public String getAlunoNome() { return alunoNome; }
    public String getAlunoEmail() { return alunoEmail; }
    public String getAlunoDocumento() { return alunoDocumento; }
    public Long getFaturaId() { return faturaId; }
    public String getCompetencia() { return competencia; }
    public LocalDate getVencimento() { return vencimento; }
    public Long getInscricaoGraduacaoId() { return inscricaoGraduacaoId; }
    public Long getEventoGraduacaoId() { return eventoGraduacaoId; }
    public String getEventoGraduacaoTitulo() { return eventoGraduacaoTitulo; }
    public String getGraduacaoDestino() { return graduacaoDestino; }
    public BigDecimal getValorBruto() { return valorBruto; }
    public BigDecimal getTaxa() { return taxa; }
    public BigDecimal getValorEstornado() { return valorEstornado; }
    public BigDecimal getValorLiquido() { return valorLiquido; }
    public String getMetodoPagamento() { return metodoPagamento; }
    public String getProvedor() { return provedor; }
    public String getStatusPagamento() { return statusPagamento; }
    public String getIdPagamentoExterno() { return idPagamentoExterno; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public LocalDateTime getPagoEm() { return pagoEm; }
    public LocalDateTime getEstornadoEm() { return estornadoEm; }
}
