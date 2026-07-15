package Tic.Implementacao.dto.evolucaofisica;

import java.util.List;

public class HistoricoPesoResponse {
    private List<EvolucaoPesoResponse> registros;

    public HistoricoPesoResponse(List<EvolucaoPesoResponse> registros) {
        this.registros = registros;
    }

    public List<EvolucaoPesoResponse> getRegistros() {
        return registros;
    }
}
