package Tic.Implementacao.dto.graduacao;

public class AlunoElegivelGraduacaoResponse {

    private Long alunoId;
    private String alunoNome;
    private String graduacaoAtual;
    private String proximaGraduacao;
    private Integer checkInsAtuais;
    private Integer checkInsNecessarios;
    private Integer checkInsFaltantes;
    private Integer tempoMinimoMeses;
    private Boolean requisitoCheckInsAtingido;
    private Boolean requisitoTempoAtingido;
    private Boolean elegivel;

    public AlunoElegivelGraduacaoResponse(Long alunoId, String alunoNome, String graduacaoAtual, String proximaGraduacao,
                                          Integer checkInsAtuais, Integer checkInsNecessarios, Integer checkInsFaltantes,
                                          Integer tempoMinimoMeses, Boolean requisitoCheckInsAtingido,
                                          Boolean requisitoTempoAtingido, Boolean elegivel) {
        this.alunoId = alunoId;
        this.alunoNome = alunoNome;
        this.graduacaoAtual = graduacaoAtual;
        this.proximaGraduacao = proximaGraduacao;
        this.checkInsAtuais = checkInsAtuais;
        this.checkInsNecessarios = checkInsNecessarios;
        this.checkInsFaltantes = checkInsFaltantes;
        this.tempoMinimoMeses = tempoMinimoMeses;
        this.requisitoCheckInsAtingido = requisitoCheckInsAtingido;
        this.requisitoTempoAtingido = requisitoTempoAtingido;
        this.elegivel = elegivel;
    }

    public Long getAlunoId() { return alunoId; }
    public String getAlunoNome() { return alunoNome; }
    public String getGraduacaoAtual() { return graduacaoAtual; }
    public String getProximaGraduacao() { return proximaGraduacao; }
    public Integer getCheckInsAtuais() { return checkInsAtuais; }
    public Integer getCheckInsNecessarios() { return checkInsNecessarios; }
    public Integer getCheckInsFaltantes() { return checkInsFaltantes; }
    public Integer getTempoMinimoMeses() { return tempoMinimoMeses; }
    public Boolean getRequisitoCheckInsAtingido() { return requisitoCheckInsAtingido; }
    public Boolean getRequisitoTempoAtingido() { return requisitoTempoAtingido; }
    public Boolean getElegivel() { return elegivel; }
}
