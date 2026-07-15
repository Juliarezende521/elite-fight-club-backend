package Tic.Implementacao.dto.evolucaofisica.meta;

import java.util.List;

public class HistoricoMetasPesoResponse {
    private List<MetaPesoResponse> metas;

    public HistoricoMetasPesoResponse(List<MetaPesoResponse> metas) {
        this.metas = metas;
    }

    public List<MetaPesoResponse> getMetas() {
        return metas;
    }
}
