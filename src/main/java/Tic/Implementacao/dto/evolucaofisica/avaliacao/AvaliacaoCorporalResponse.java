package Tic.Implementacao.dto.evolucaofisica.avaliacao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class AvaliacaoCorporalResponse {
    private Long id;
    private Long alunoId;
    private String alunoNome;
    private LocalDate dataAvaliacao;
    private BigDecimal alturaMetros;
    private BigDecimal pesoRegistrado;
    private String observacao;
    private Boolean ativa;
    private LocalDateTime criadaEm;
    private LocalDateTime atualizadaEm;
    private MedidasCorporaisRequest medidas;
    private BioimpedanciaRequest bioimpedancia;

    public AvaliacaoCorporalResponse(Long id, Long alunoId, String alunoNome, LocalDate dataAvaliacao,
                                     BigDecimal alturaMetros, BigDecimal pesoRegistrado, String observacao,
                                     Boolean ativa, LocalDateTime criadaEm, LocalDateTime atualizadaEm,
                                     MedidasCorporaisRequest medidas, BioimpedanciaRequest bioimpedancia) {
        this.id = id;
        this.alunoId = alunoId;
        this.alunoNome = alunoNome;
        this.dataAvaliacao = dataAvaliacao;
        this.alturaMetros = alturaMetros;
        this.pesoRegistrado = pesoRegistrado;
        this.observacao = observacao;
        this.ativa = ativa;
        this.criadaEm = criadaEm;
        this.atualizadaEm = atualizadaEm;
        this.medidas = medidas;
        this.bioimpedancia = bioimpedancia;
    }

    public Long getId() { return id; }
    public Long getAlunoId() { return alunoId; }
    public String getAlunoNome() { return alunoNome; }
    public LocalDate getDataAvaliacao() { return dataAvaliacao; }
    public BigDecimal getAlturaMetros() { return alturaMetros; }
    public BigDecimal getPesoRegistrado() { return pesoRegistrado; }
    public String getObservacao() { return observacao; }
    public Boolean getAtiva() { return ativa; }
    public LocalDateTime getCriadaEm() { return criadaEm; }
    public LocalDateTime getAtualizadaEm() { return atualizadaEm; }
    public MedidasCorporaisRequest getMedidas() { return medidas; }
    public BioimpedanciaRequest getBioimpedancia() { return bioimpedancia; }
}
