package Tic.Implementacao.dto.presenca;

import java.util.List;

public class HistoricoCheckInAlunoResponse {

    private Long alunoId;
    private Integer total;
    private List<CheckInResponse> checkIns;

    public HistoricoCheckInAlunoResponse(Long alunoId, Integer total, List<CheckInResponse> checkIns) {
        this.alunoId = alunoId;
        this.total = total;
        this.checkIns = checkIns;
    }

    public Long getAlunoId() { return alunoId; }
    public Integer getTotal() { return total; }
    public List<CheckInResponse> getCheckIns() { return checkIns; }
}
