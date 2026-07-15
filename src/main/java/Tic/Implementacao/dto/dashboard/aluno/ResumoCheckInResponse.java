package Tic.Implementacao.dto.dashboard.aluno;

import java.time.LocalDateTime;

public class ResumoCheckInResponse {
    private Integer total;
    private Integer noMesAtual;
    private Integer naSemanaAtual;
    private LocalDateTime ultimoCheckInEm;

    public ResumoCheckInResponse(Integer total, Integer noMesAtual, Integer naSemanaAtual, LocalDateTime ultimoCheckInEm) {
        this.total = total;
        this.noMesAtual = noMesAtual;
        this.naSemanaAtual = naSemanaAtual;
        this.ultimoCheckInEm = ultimoCheckInEm;
    }

    public Integer getTotal() { return total; }
    public Integer getNoMesAtual() { return noMesAtual; }
    public Integer getNaSemanaAtual() { return naSemanaAtual; }
    public LocalDateTime getUltimoCheckInEm() { return ultimoCheckInEm; }
}
