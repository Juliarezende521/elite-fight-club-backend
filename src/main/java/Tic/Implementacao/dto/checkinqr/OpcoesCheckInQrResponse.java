package Tic.Implementacao.dto.checkinqr;

import java.util.List;

public class OpcoesCheckInQrResponse {
    private String academiaNome;
    private List<OpcaoCheckInResponse> opcoes;

    public OpcoesCheckInQrResponse(String academiaNome, List<OpcaoCheckInResponse> opcoes) {
        this.academiaNome = academiaNome;
        this.opcoes = opcoes;
    }

    public String getAcademiaNome() { return academiaNome; }
    public List<OpcaoCheckInResponse> getOpcoes() { return opcoes; }
}
