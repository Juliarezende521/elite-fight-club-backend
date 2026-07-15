package Tic.Implementacao.dto.evolucaofisica.avaliacao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public class ComparativoAvaliacoesResponse {
    private Long avaliacaoInicialId;
    private Long avaliacaoFinalId;
    private LocalDate dataInicial;
    private LocalDate dataFinal;
    private Map<String, BigDecimal> valoresIniciais;
    private Map<String, BigDecimal> valoresFinais;
    private Map<String, BigDecimal> diferencas;

    public ComparativoAvaliacoesResponse(Long avaliacaoInicialId, Long avaliacaoFinalId, LocalDate dataInicial,
                                         LocalDate dataFinal, Map<String, BigDecimal> valoresIniciais,
                                         Map<String, BigDecimal> valoresFinais, Map<String, BigDecimal> diferencas) {
        this.avaliacaoInicialId = avaliacaoInicialId;
        this.avaliacaoFinalId = avaliacaoFinalId;
        this.dataInicial = dataInicial;
        this.dataFinal = dataFinal;
        this.valoresIniciais = valoresIniciais;
        this.valoresFinais = valoresFinais;
        this.diferencas = diferencas;
    }

    public Long getAvaliacaoInicialId() { return avaliacaoInicialId; }
    public Long getAvaliacaoFinalId() { return avaliacaoFinalId; }
    public LocalDate getDataInicial() { return dataInicial; }
    public LocalDate getDataFinal() { return dataFinal; }
    public Map<String, BigDecimal> getValoresIniciais() { return valoresIniciais; }
    public Map<String, BigDecimal> getValoresFinais() { return valoresFinais; }
    public Map<String, BigDecimal> getDiferencas() { return diferencas; }
}
