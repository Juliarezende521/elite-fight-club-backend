package Tic.Implementacao.dto.dashboard.aluno;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ResumoPrajiedResponse {
    private Long prajiedId;
    private String nome;
    private String corHex;
    private Integer ordem;
    private Integer checkInsAtuais;
    private Integer checkInsNecessariosProximaGraduacao;
    private Integer checkInsFaltantes;
    private Long proximoPrajiedId;
    private String proximoPrajiedNome;
    private BigDecimal percentualProgresso;
    private Boolean elegivelParaAvaliacao;
    private Integer tempoMinimoMeses;
    private LocalDate dataEntradaGraduacaoAtual;
    private LocalDate dataMinimaElegibilidade;
    private Boolean requisitoCheckInsAtingido;
    private Boolean requisitoTempoAtingido;
    private String motivo;

    public ResumoPrajiedResponse(Long prajiedId, String nome, String corHex, Integer ordem,
                                 Integer checkInsAtuais, Integer checkInsNecessariosProximaGraduacao,
                                 Integer checkInsFaltantes, Long proximoPrajiedId, String proximoPrajiedNome,
                                 BigDecimal percentualProgresso, Boolean elegivelParaAvaliacao) {
        this(prajiedId, nome, corHex, ordem, checkInsAtuais, checkInsNecessariosProximaGraduacao, checkInsFaltantes,
                proximoPrajiedId, proximoPrajiedNome, percentualProgresso, elegivelParaAvaliacao, 0, null, null,
                false, false, null);
    }

    public ResumoPrajiedResponse(Long prajiedId, String nome, String corHex, Integer ordem,
                                 Integer checkInsAtuais, Integer checkInsNecessariosProximaGraduacao,
                                 Integer checkInsFaltantes, Long proximoPrajiedId, String proximoPrajiedNome,
                                 BigDecimal percentualProgresso, Boolean elegivelParaAvaliacao,
                                 Integer tempoMinimoMeses, LocalDate dataEntradaGraduacaoAtual,
                                 LocalDate dataMinimaElegibilidade, Boolean requisitoCheckInsAtingido,
                                 Boolean requisitoTempoAtingido, String motivo) {
        this.prajiedId = prajiedId;
        this.nome = nome;
        this.corHex = corHex;
        this.ordem = ordem;
        this.checkInsAtuais = checkInsAtuais;
        this.checkInsNecessariosProximaGraduacao = checkInsNecessariosProximaGraduacao;
        this.checkInsFaltantes = checkInsFaltantes;
        this.proximoPrajiedId = proximoPrajiedId;
        this.proximoPrajiedNome = proximoPrajiedNome;
        this.percentualProgresso = percentualProgresso;
        this.elegivelParaAvaliacao = elegivelParaAvaliacao;
        this.tempoMinimoMeses = tempoMinimoMeses;
        this.dataEntradaGraduacaoAtual = dataEntradaGraduacaoAtual;
        this.dataMinimaElegibilidade = dataMinimaElegibilidade;
        this.requisitoCheckInsAtingido = requisitoCheckInsAtingido;
        this.requisitoTempoAtingido = requisitoTempoAtingido;
        this.motivo = motivo;
    }

    public Long getPrajiedId() { return prajiedId; }
    public String getNome() { return nome; }
    public String getCorHex() { return corHex; }
    public Integer getOrdem() { return ordem; }
    public Integer getCheckInsAtuais() { return checkInsAtuais; }
    public Integer getCheckInsNecessariosProximaGraduacao() { return checkInsNecessariosProximaGraduacao; }
    public Integer getCheckInsFaltantes() { return checkInsFaltantes; }
    public Long getProximoPrajiedId() { return proximoPrajiedId; }
    public String getProximoPrajiedNome() { return proximoPrajiedNome; }
    public BigDecimal getPercentualProgresso() { return percentualProgresso; }
    public Boolean getElegivelParaAvaliacao() { return elegivelParaAvaliacao; }
    public Integer getTempoMinimoMeses() { return tempoMinimoMeses; }
    public LocalDate getDataEntradaGraduacaoAtual() { return dataEntradaGraduacaoAtual; }
    public LocalDate getDataMinimaElegibilidade() { return dataMinimaElegibilidade; }
    public Boolean getRequisitoCheckInsAtingido() { return requisitoCheckInsAtingido; }
    public Boolean getRequisitoTempoAtingido() { return requisitoTempoAtingido; }
    public String getMotivo() { return motivo; }
}
