package Tic.Implementacao.dto.relatorio.financeiro.anual;

import java.math.BigDecimal;
import java.time.LocalDate;

public class AlunoInadimplenciaAnualResponse {
    private Long alunoId;
    private String alunoNome;
    private String email;
    private String tipoPlano;
    private Integer quantidadeFaturasVencidas;
    private BigDecimal totalVencido;
    private LocalDate vencimentoMaisAntigo;
    private Integer maiorQuantidadeDiasAtraso;

    public AlunoInadimplenciaAnualResponse(Long alunoId, String alunoNome, String email, String tipoPlano,
                                           Integer quantidadeFaturasVencidas, BigDecimal totalVencido,
                                           LocalDate vencimentoMaisAntigo, Integer maiorQuantidadeDiasAtraso) {
        this.alunoId = alunoId;
        this.alunoNome = alunoNome;
        this.email = email;
        this.tipoPlano = tipoPlano;
        this.quantidadeFaturasVencidas = quantidadeFaturasVencidas;
        this.totalVencido = totalVencido;
        this.vencimentoMaisAntigo = vencimentoMaisAntigo;
        this.maiorQuantidadeDiasAtraso = maiorQuantidadeDiasAtraso;
    }

    public Long getAlunoId() { return alunoId; }
    public String getAlunoNome() { return alunoNome; }
    public String getEmail() { return email; }
    public String getTipoPlano() { return tipoPlano; }
    public Integer getQuantidadeFaturasVencidas() { return quantidadeFaturasVencidas; }
    public BigDecimal getTotalVencido() { return totalVencido; }
    public LocalDate getVencimentoMaisAntigo() { return vencimentoMaisAntigo; }
    public Integer getMaiorQuantidadeDiasAtraso() { return maiorQuantidadeDiasAtraso; }
}
