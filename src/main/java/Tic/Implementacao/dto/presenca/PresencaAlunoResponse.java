package Tic.Implementacao.dto.presenca;

import Tic.Implementacao.model.StatusPresenca;

import java.time.LocalDateTime;

public class PresencaAlunoResponse {

    private Long alunoId;
    private String alunoNome;
    private Long reservaId;
    private StatusPresenca statusPresenca;
    private boolean checkInRegistrado;
    private Long checkInId;
    private LocalDateTime registradoEm;
    private String registradoPor;

    public PresencaAlunoResponse(Long alunoId, String alunoNome, Long reservaId, StatusPresenca statusPresenca,
                                 boolean checkInRegistrado, Long checkInId, LocalDateTime registradoEm,
                                 String registradoPor) {
        this.alunoId = alunoId;
        this.alunoNome = alunoNome;
        this.reservaId = reservaId;
        this.statusPresenca = statusPresenca;
        this.checkInRegistrado = checkInRegistrado;
        this.checkInId = checkInId;
        this.registradoEm = registradoEm;
        this.registradoPor = registradoPor;
    }

    public Long getAlunoId() { return alunoId; }
    public String getAlunoNome() { return alunoNome; }
    public Long getReservaId() { return reservaId; }
    public StatusPresenca getStatusPresenca() { return statusPresenca; }
    public boolean isCheckInRegistrado() { return checkInRegistrado; }
    public Long getCheckInId() { return checkInId; }
    public LocalDateTime getRegistradoEm() { return registradoEm; }
    public String getRegistradoPor() { return registradoPor; }
}
