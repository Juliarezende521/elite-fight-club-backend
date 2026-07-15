package Tic.Implementacao.dto.graduacao.relatorio;

import java.math.BigDecimal;

public class IndicadoresEventoGraduacaoResponse {

    private BigDecimal taxaAceitacaoConvite;
    private BigDecimal taxaInscricao;
    private BigDecimal taxaPagamento;
    private BigDecimal taxaComparecimento;
    private BigDecimal taxaAprovacao;
    private BigDecimal taxaPromocao;

    public IndicadoresEventoGraduacaoResponse(BigDecimal taxaAceitacaoConvite, BigDecimal taxaInscricao,
                                              BigDecimal taxaPagamento, BigDecimal taxaComparecimento,
                                              BigDecimal taxaAprovacao, BigDecimal taxaPromocao) {
        this.taxaAceitacaoConvite = taxaAceitacaoConvite;
        this.taxaInscricao = taxaInscricao;
        this.taxaPagamento = taxaPagamento;
        this.taxaComparecimento = taxaComparecimento;
        this.taxaAprovacao = taxaAprovacao;
        this.taxaPromocao = taxaPromocao;
    }

    public BigDecimal getTaxaAceitacaoConvite() { return taxaAceitacaoConvite; }
    public BigDecimal getTaxaInscricao() { return taxaInscricao; }
    public BigDecimal getTaxaPagamento() { return taxaPagamento; }
    public BigDecimal getTaxaComparecimento() { return taxaComparecimento; }
    public BigDecimal getTaxaAprovacao() { return taxaAprovacao; }
    public BigDecimal getTaxaPromocao() { return taxaPromocao; }
}
