package Tic.Implementacao.dto.relatorio.financeiro.anual;

import java.math.BigDecimal;

public class ResumoFinanceiroAnualResponse {
    private BigDecimal totalPrevisto;
    private BigDecimal totalRecebido;
    private BigDecimal totalPendente;
    private BigDecimal totalVencido;
    private BigDecimal totalParcialmentePago;
    private BigDecimal totalCancelado;
    private BigDecimal totalEstornado;
    private BigDecimal totalTaxas;
    private BigDecimal receitaLiquida;
    private BigDecimal mediaMensalPrevista;
    private BigDecimal mediaMensalRecebida;
    private BigDecimal ticketMedioRecebido;
    private BigDecimal taxaRecebimento;
    private BigDecimal taxaInadimplencia;
    private Integer quantidadeFaturas;
    private Integer quantidadePagamentosConfirmados;
    private Integer alunosAtivosNoAno;
    private Integer alunosComInadimplencia;

    public ResumoFinanceiroAnualResponse(BigDecimal totalPrevisto, BigDecimal totalRecebido,
                                         BigDecimal totalPendente, BigDecimal totalVencido,
                                         BigDecimal totalParcialmentePago, BigDecimal totalCancelado,
                                         BigDecimal totalEstornado, BigDecimal totalTaxas,
                                         BigDecimal receitaLiquida, BigDecimal mediaMensalPrevista,
                                         BigDecimal mediaMensalRecebida, BigDecimal ticketMedioRecebido,
                                         BigDecimal taxaRecebimento, BigDecimal taxaInadimplencia,
                                         Integer quantidadeFaturas, Integer quantidadePagamentosConfirmados,
                                         Integer alunosAtivosNoAno, Integer alunosComInadimplencia) {
        this.totalPrevisto = totalPrevisto;
        this.totalRecebido = totalRecebido;
        this.totalPendente = totalPendente;
        this.totalVencido = totalVencido;
        this.totalParcialmentePago = totalParcialmentePago;
        this.totalCancelado = totalCancelado;
        this.totalEstornado = totalEstornado;
        this.totalTaxas = totalTaxas;
        this.receitaLiquida = receitaLiquida;
        this.mediaMensalPrevista = mediaMensalPrevista;
        this.mediaMensalRecebida = mediaMensalRecebida;
        this.ticketMedioRecebido = ticketMedioRecebido;
        this.taxaRecebimento = taxaRecebimento;
        this.taxaInadimplencia = taxaInadimplencia;
        this.quantidadeFaturas = quantidadeFaturas;
        this.quantidadePagamentosConfirmados = quantidadePagamentosConfirmados;
        this.alunosAtivosNoAno = alunosAtivosNoAno;
        this.alunosComInadimplencia = alunosComInadimplencia;
    }

    public BigDecimal getTotalPrevisto() { return totalPrevisto; }
    public BigDecimal getTotalRecebido() { return totalRecebido; }
    public BigDecimal getTotalPendente() { return totalPendente; }
    public BigDecimal getTotalVencido() { return totalVencido; }
    public BigDecimal getTotalParcialmentePago() { return totalParcialmentePago; }
    public BigDecimal getTotalCancelado() { return totalCancelado; }
    public BigDecimal getTotalEstornado() { return totalEstornado; }
    public BigDecimal getTotalTaxas() { return totalTaxas; }
    public BigDecimal getReceitaLiquida() { return receitaLiquida; }
    public BigDecimal getMediaMensalPrevista() { return mediaMensalPrevista; }
    public BigDecimal getMediaMensalRecebida() { return mediaMensalRecebida; }
    public BigDecimal getTicketMedioRecebido() { return ticketMedioRecebido; }
    public BigDecimal getTaxaRecebimento() { return taxaRecebimento; }
    public BigDecimal getTaxaInadimplencia() { return taxaInadimplencia; }
    public Integer getQuantidadeFaturas() { return quantidadeFaturas; }
    public Integer getQuantidadePagamentosConfirmados() { return quantidadePagamentosConfirmados; }
    public Integer getAlunosAtivosNoAno() { return alunosAtivosNoAno; }
    public Integer getAlunosComInadimplencia() { return alunosComInadimplencia; }
}
