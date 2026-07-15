package Tic.Implementacao.dto.presenca;

import Tic.Implementacao.model.OrigemCheckIn;

import java.time.LocalDateTime;

public class CheckInResponse {

    private Long id;
    private Long alunoId;
    private String alunoNome;
    private Long aulaTurmaId;
    private Long aulaPersonalId;
    private LocalDateTime realizadoEm;
    private OrigemCheckIn origem;
    private String confirmadoPor;

    public CheckInResponse(Long id, Long alunoId, String alunoNome, Long aulaTurmaId, Long aulaPersonalId,
                           LocalDateTime realizadoEm, OrigemCheckIn origem, String confirmadoPor) {
        this.id = id;
        this.alunoId = alunoId;
        this.alunoNome = alunoNome;
        this.aulaTurmaId = aulaTurmaId;
        this.aulaPersonalId = aulaPersonalId;
        this.realizadoEm = realizadoEm;
        this.origem = origem;
        this.confirmadoPor = confirmadoPor;
    }

    public Long getId() { return id; }
    public Long getAlunoId() { return alunoId; }
    public String getAlunoNome() { return alunoNome; }
    public Long getAulaTurmaId() { return aulaTurmaId; }
    public Long getAulaPersonalId() { return aulaPersonalId; }
    public LocalDateTime getRealizadoEm() { return realizadoEm; }
    public OrigemCheckIn getOrigem() { return origem; }
    public String getConfirmadoPor() { return confirmadoPor; }
}
