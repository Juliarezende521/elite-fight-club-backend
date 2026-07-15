package Tic.Implementacao.dto.relatorio.financeiro;

import java.math.BigDecimal;
import java.time.LocalDate;

public class AlunoInadimplenteResponse {
    private Long alunoId;
    private String alunoNome;
    private String email;
    private Integer quantidadeFaturasVencidas;
    private BigDecimal totalVencido;
    private LocalDate vencimentoMaisAntigo;
    private Integer diasEmAtraso;

    public AlunoInadimplenteResponse(Long alunoId, String alunoNome, String email,
                                     Integer quantidadeFaturasVencidas, BigDecimal totalVencido,
                                     LocalDate vencimentoMaisAntigo, Integer diasEmAtraso) {
        this.alunoId = alunoId;
        this.alunoNome = alunoNome;
        this.email = email;
        this.quantidadeFaturasVencidas = quantidadeFaturasVencidas;
        this.totalVencido = totalVencido;
        this.vencimentoMaisAntigo = vencimentoMaisAntigo;
        this.diasEmAtraso = diasEmAtraso;
    }

    public Long getAlunoId() { return alunoId; }
    public String getAlunoNome() { return alunoNome; }
    public String getEmail() { return email; }
    public Integer getQuantidadeFaturasVencidas() { return quantidadeFaturasVencidas; }
    public BigDecimal getTotalVencido() { return totalVencido; }
    public LocalDate getVencimentoMaisAntigo() { return vencimentoMaisAntigo; }
    public Integer getDiasEmAtraso() { return diasEmAtraso; }
}
