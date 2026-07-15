package Tic.Implementacao.dto.checkinqr;

import Tic.Implementacao.model.StatusSessaoCheckIn;
import Tic.Implementacao.model.TipoItemAgenda;

import java.time.LocalDateTime;

public class SessaoCheckInResponse {
    private Long sessaoId;
    private Long academiaId;
    private TipoItemAgenda tipo;
    private Long aulaId;
    private StatusSessaoCheckIn status;
    private LocalDateTime abertaEm;
    private LocalDateTime expiraEm;
    private LocalDateTime encerradaEm;

    public SessaoCheckInResponse(Long sessaoId, Long academiaId, TipoItemAgenda tipo, Long aulaId,
                                 StatusSessaoCheckIn status, LocalDateTime abertaEm,
                                 LocalDateTime expiraEm, LocalDateTime encerradaEm) {
        this.sessaoId = sessaoId;
        this.academiaId = academiaId;
        this.tipo = tipo;
        this.aulaId = aulaId;
        this.status = status;
        this.abertaEm = abertaEm;
        this.expiraEm = expiraEm;
        this.encerradaEm = encerradaEm;
    }

    public Long getSessaoId() { return sessaoId; }
    public Long getAcademiaId() { return academiaId; }
    public TipoItemAgenda getTipo() { return tipo; }
    public Long getAulaId() { return aulaId; }
    public StatusSessaoCheckIn getStatus() { return status; }
    public LocalDateTime getAbertaEm() { return abertaEm; }
    public LocalDateTime getExpiraEm() { return expiraEm; }
    public LocalDateTime getEncerradaEm() { return encerradaEm; }
}
