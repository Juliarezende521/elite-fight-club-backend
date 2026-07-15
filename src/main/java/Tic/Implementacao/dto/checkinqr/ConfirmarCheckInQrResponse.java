package Tic.Implementacao.dto.checkinqr;

import Tic.Implementacao.model.TipoItemAgenda;

public class ConfirmarCheckInQrResponse {
    private boolean sucesso;
    private String mensagem;
    private Long checkInId;
    private Long aulaId;
    private TipoItemAgenda tipoAula;
    private String modalidade;
    private Integer quantidadeCheckins;

    public ConfirmarCheckInQrResponse(boolean sucesso, String mensagem, Long checkInId, Long aulaId,
                                      TipoItemAgenda tipoAula, String modalidade, Integer quantidadeCheckins) {
        this.sucesso = sucesso;
        this.mensagem = mensagem;
        this.checkInId = checkInId;
        this.aulaId = aulaId;
        this.tipoAula = tipoAula;
        this.modalidade = modalidade;
        this.quantidadeCheckins = quantidadeCheckins;
    }

    public boolean isSucesso() { return sucesso; }
    public String getMensagem() { return mensagem; }
    public Long getCheckInId() { return checkInId; }
    public Long getAulaId() { return aulaId; }
    public TipoItemAgenda getTipoAula() { return tipoAula; }
    public String getModalidade() { return modalidade; }
    public Integer getQuantidadeCheckins() { return quantidadeCheckins; }
}
