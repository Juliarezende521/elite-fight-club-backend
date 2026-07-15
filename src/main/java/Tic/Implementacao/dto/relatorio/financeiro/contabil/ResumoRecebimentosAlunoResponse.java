package Tic.Implementacao.dto.relatorio.financeiro.contabil;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ResumoRecebimentosAlunoResponse {
    private Long alunoId;
    private String alunoNome;
    private String alunoDocumento;
    private BigDecimal totalMensalidades;
    private BigDecimal totalGraduacoes;
    private BigDecimal totalPago;
    private Integer quantidadePagamentos;
    private LocalDateTime primeiroPagamento;
    private LocalDateTime ultimoPagamento;

    public ResumoRecebimentosAlunoResponse(Long alunoId, String alunoNome, String alunoDocumento,
                                           BigDecimal totalMensalidades, BigDecimal totalGraduacoes,
                                           BigDecimal totalPago, Integer quantidadePagamentos,
                                           LocalDateTime primeiroPagamento, LocalDateTime ultimoPagamento) {
        this.alunoId = alunoId;
        this.alunoNome = alunoNome;
        this.alunoDocumento = alunoDocumento;
        this.totalMensalidades = totalMensalidades;
        this.totalGraduacoes = totalGraduacoes;
        this.totalPago = totalPago;
        this.quantidadePagamentos = quantidadePagamentos;
        this.primeiroPagamento = primeiroPagamento;
        this.ultimoPagamento = ultimoPagamento;
    }

    public Long getAlunoId() { return alunoId; }
    public String getAlunoNome() { return alunoNome; }
    public String getAlunoDocumento() { return alunoDocumento; }
    public BigDecimal getTotalMensalidades() { return totalMensalidades; }
    public BigDecimal getTotalGraduacoes() { return totalGraduacoes; }
    public BigDecimal getTotalPago() { return totalPago; }
    public Integer getQuantidadePagamentos() { return quantidadePagamentos; }
    public LocalDateTime getPrimeiroPagamento() { return primeiroPagamento; }
    public LocalDateTime getUltimoPagamento() { return ultimoPagamento; }
}
