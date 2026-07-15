package Tic.Implementacao.dto.evolucaofisica;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class EvolucaoPesoResponse {
    private Long id;
    private Long alunoId;
    private String alunoNome;
    private BigDecimal peso;
    private LocalDate dataRegistro;
    private String observacao;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

    public EvolucaoPesoResponse(Long id, Long alunoId, String alunoNome, BigDecimal peso, LocalDate dataRegistro,
                                String observacao, LocalDateTime criadoEm, LocalDateTime atualizadoEm) {
        this.id = id;
        this.alunoId = alunoId;
        this.alunoNome = alunoNome;
        this.peso = peso;
        this.dataRegistro = dataRegistro;
        this.observacao = observacao;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    public Long getId() { return id; }
    public Long getAlunoId() { return alunoId; }
    public String getAlunoNome() { return alunoNome; }
    public BigDecimal getPeso() { return peso; }
    public LocalDate getDataRegistro() { return dataRegistro; }
    public String getObservacao() { return observacao; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }
}
