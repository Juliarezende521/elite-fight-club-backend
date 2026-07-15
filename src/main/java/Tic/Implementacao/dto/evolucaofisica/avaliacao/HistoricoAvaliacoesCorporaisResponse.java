package Tic.Implementacao.dto.evolucaofisica.avaliacao;

import java.util.List;

public class HistoricoAvaliacoesCorporaisResponse {
    private List<AvaliacaoCorporalResponse> avaliacoes;

    public HistoricoAvaliacoesCorporaisResponse(List<AvaliacaoCorporalResponse> avaliacoes) {
        this.avaliacoes = avaliacoes;
    }

    public List<AvaliacaoCorporalResponse> getAvaliacoes() {
        return avaliacoes;
    }
}
