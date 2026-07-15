package Tic.Implementacao.dto.graduacao.relatorio;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RelatorioParticipanteGraduacaoResponse {

    private Long alunoId;
    private String alunoNome;
    private String email;
    private String graduacaoAtual;
    private String graduacaoDestino;
    private String statusConvite;
    private String statusInscricao;
    private String statusPagamento;
    private String statusPresenca;
    private String resultadoAvaliacao;
    private Boolean promovido;
    private BigDecimal valorInscricao;
    private BigDecimal taxaPagamento;
    private BigDecimal valorLiquido;
    private LocalDateTime inscritoEm;
    private LocalDateTime pagoEm;
    private LocalDateTime avaliadoEm;
    private LocalDateTime promovidoEm;

    public RelatorioParticipanteGraduacaoResponse(Long alunoId, String alunoNome, String email,
                                                  String graduacaoAtual, String graduacaoDestino,
                                                  String statusConvite, String statusInscricao,
                                                  String statusPagamento, String statusPresenca,
                                                  String resultadoAvaliacao, Boolean promovido,
                                                  BigDecimal valorInscricao, BigDecimal taxaPagamento,
                                                  BigDecimal valorLiquido, LocalDateTime inscritoEm,
                                                  LocalDateTime pagoEm, LocalDateTime avaliadoEm,
                                                  LocalDateTime promovidoEm) {
        this.alunoId = alunoId;
        this.alunoNome = alunoNome;
        this.email = email;
        this.graduacaoAtual = graduacaoAtual;
        this.graduacaoDestino = graduacaoDestino;
        this.statusConvite = statusConvite;
        this.statusInscricao = statusInscricao;
        this.statusPagamento = statusPagamento;
        this.statusPresenca = statusPresenca;
        this.resultadoAvaliacao = resultadoAvaliacao;
        this.promovido = promovido;
        this.valorInscricao = valorInscricao;
        this.taxaPagamento = taxaPagamento;
        this.valorLiquido = valorLiquido;
        this.inscritoEm = inscritoEm;
        this.pagoEm = pagoEm;
        this.avaliadoEm = avaliadoEm;
        this.promovidoEm = promovidoEm;
    }

    public Long getAlunoId() { return alunoId; }
    public String getAlunoNome() { return alunoNome; }
    public String getEmail() { return email; }
    public String getGraduacaoAtual() { return graduacaoAtual; }
    public String getGraduacaoDestino() { return graduacaoDestino; }
    public String getStatusConvite() { return statusConvite; }
    public String getStatusInscricao() { return statusInscricao; }
    public String getStatusPagamento() { return statusPagamento; }
    public String getStatusPresenca() { return statusPresenca; }
    public String getResultadoAvaliacao() { return resultadoAvaliacao; }
    public Boolean getPromovido() { return promovido; }
    public BigDecimal getValorInscricao() { return valorInscricao; }
    public BigDecimal getTaxaPagamento() { return taxaPagamento; }
    public BigDecimal getValorLiquido() { return valorLiquido; }
    public LocalDateTime getInscritoEm() { return inscritoEm; }
    public LocalDateTime getPagoEm() { return pagoEm; }
    public LocalDateTime getAvaliadoEm() { return avaliadoEm; }
    public LocalDateTime getPromovidoEm() { return promovidoEm; }
}
