package Tic.Implementacao.dto.reserva;

public class OcupacaoAulaResponse {

    private Long aulaId;
    private long confirmados;
    private Integer capacidade;
    private long vagasDisponiveis;
    private boolean lotada;

    public OcupacaoAulaResponse(Long aulaId, long confirmados, Integer capacidade) {
        this.aulaId = aulaId;
        this.confirmados = confirmados;
        this.capacidade = capacidade;
        this.vagasDisponiveis = Math.max(0, capacidade - confirmados);
        this.lotada = confirmados >= capacidade;
    }

    public Long getAulaId() { return aulaId; }
    public long getConfirmados() { return confirmados; }
    public Integer getCapacidade() { return capacidade; }
    public long getVagasDisponiveis() { return vagasDisponiveis; }
    public boolean isLotada() { return lotada; }
}
