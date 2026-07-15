package Tic.Implementacao.dto.graduacao;

import java.time.LocalDate;

public class ElegibilidadeGraduacaoResponse {

    private Long alunoId;
    private Long graduacaoAtualId;
    private String graduacaoAtualNome;
    private Long proximaGraduacaoId;
    private String proximaGraduacaoNome;
    private Integer checkInsAtuais;
    private Integer checkInsNecessarios;
    private Integer checkInsFaltantes;
    private Integer tempoMinimoMeses;
    private LocalDate dataEntradaGraduacaoAtual;
    private LocalDate dataMinimaElegibilidade;
    private Boolean requisitoCheckInsAtingido;
    private Boolean requisitoTempoAtingido;
    private Boolean elegivelParaAvaliacao;
    private String motivo;

    public ElegibilidadeGraduacaoResponse(Long alunoId, Long graduacaoAtualId, String graduacaoAtualNome,
                                          Long proximaGraduacaoId, String proximaGraduacaoNome,
                                          Integer checkInsAtuais, Integer checkInsNecessarios,
                                          Integer checkInsFaltantes, Integer tempoMinimoMeses,
                                          LocalDate dataEntradaGraduacaoAtual, LocalDate dataMinimaElegibilidade,
                                          Boolean requisitoCheckInsAtingido, Boolean requisitoTempoAtingido,
                                          Boolean elegivelParaAvaliacao, String motivo) {
        this.alunoId = alunoId;
        this.graduacaoAtualId = graduacaoAtualId;
        this.graduacaoAtualNome = graduacaoAtualNome;
        this.proximaGraduacaoId = proximaGraduacaoId;
        this.proximaGraduacaoNome = proximaGraduacaoNome;
        this.checkInsAtuais = checkInsAtuais;
        this.checkInsNecessarios = checkInsNecessarios;
        this.checkInsFaltantes = checkInsFaltantes;
        this.tempoMinimoMeses = tempoMinimoMeses;
        this.dataEntradaGraduacaoAtual = dataEntradaGraduacaoAtual;
        this.dataMinimaElegibilidade = dataMinimaElegibilidade;
        this.requisitoCheckInsAtingido = requisitoCheckInsAtingido;
        this.requisitoTempoAtingido = requisitoTempoAtingido;
        this.elegivelParaAvaliacao = elegivelParaAvaliacao;
        this.motivo = motivo;
    }

    public Long getAlunoId() { return alunoId; }
    public Long getGraduacaoAtualId() { return graduacaoAtualId; }
    public String getGraduacaoAtualNome() { return graduacaoAtualNome; }
    public Long getProximaGraduacaoId() { return proximaGraduacaoId; }
    public String getProximaGraduacaoNome() { return proximaGraduacaoNome; }
    public Integer getCheckInsAtuais() { return checkInsAtuais; }
    public Integer getCheckInsNecessarios() { return checkInsNecessarios; }
    public Integer getCheckInsFaltantes() { return checkInsFaltantes; }
    public Integer getTempoMinimoMeses() { return tempoMinimoMeses; }
    public LocalDate getDataEntradaGraduacaoAtual() { return dataEntradaGraduacaoAtual; }
    public LocalDate getDataMinimaElegibilidade() { return dataMinimaElegibilidade; }
    public Boolean getRequisitoCheckInsAtingido() { return requisitoCheckInsAtingido; }
    public Boolean getRequisitoTempoAtingido() { return requisitoTempoAtingido; }
    public Boolean getElegivelParaAvaliacao() { return elegivelParaAvaliacao; }
    public String getMotivo() { return motivo; }
}
