package Tic.Implementacao.dto.graduacao.relatorio;

import Tic.Implementacao.model.StatusEventoGraduacao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class RelatorioEventoGraduacaoResponse {

    private Long eventoId;
    private String titulo;
    private LocalDate dataEvento;
    private String local;
    private StatusEventoGraduacao status;
    private Integer totalConvidados;
    private Integer convitesPendentes;
    private Integer convitesAceitos;
    private Integer convitesRecusados;
    private Integer totalInscritos;
    private Integer inscricoesPagas;
    private Integer inscricoesPendentes;
    private Integer inscricoesCanceladas;
    private Integer inscricoesExpiradas;
    private Integer presentes;
    private Integer ausentes;
    private Integer avaliacoesPendentes;
    private Integer aprovados;
    private Integer reprovados;
    private Integer promovidos;
    private Integer aprovadosNaoPromovidos;
    private BigDecimal receitaBruta;
    private BigDecimal taxasPagamento;
    private BigDecimal receitaLiquida;
    private BigDecimal valoresPendentes;
    private BigDecimal valoresEstornados;
    private IndicadoresEventoGraduacaoResponse indicadores;
    private List<RelatorioPrajiedEventoResponse> porPrajied;

    public RelatorioEventoGraduacaoResponse(Long eventoId, String titulo, LocalDate dataEvento, String local,
                                            StatusEventoGraduacao status, Integer totalConvidados,
                                            Integer convitesPendentes, Integer convitesAceitos,
                                            Integer convitesRecusados, Integer totalInscritos,
                                            Integer inscricoesPagas, Integer inscricoesPendentes,
                                            Integer inscricoesCanceladas, Integer inscricoesExpiradas,
                                            Integer presentes, Integer ausentes, Integer avaliacoesPendentes,
                                            Integer aprovados, Integer reprovados, Integer promovidos,
                                            Integer aprovadosNaoPromovidos, BigDecimal receitaBruta,
                                            BigDecimal taxasPagamento, BigDecimal receitaLiquida,
                                            BigDecimal valoresPendentes, BigDecimal valoresEstornados,
                                            IndicadoresEventoGraduacaoResponse indicadores,
                                            List<RelatorioPrajiedEventoResponse> porPrajied) {
        this.eventoId = eventoId;
        this.titulo = titulo;
        this.dataEvento = dataEvento;
        this.local = local;
        this.status = status;
        this.totalConvidados = totalConvidados;
        this.convitesPendentes = convitesPendentes;
        this.convitesAceitos = convitesAceitos;
        this.convitesRecusados = convitesRecusados;
        this.totalInscritos = totalInscritos;
        this.inscricoesPagas = inscricoesPagas;
        this.inscricoesPendentes = inscricoesPendentes;
        this.inscricoesCanceladas = inscricoesCanceladas;
        this.inscricoesExpiradas = inscricoesExpiradas;
        this.presentes = presentes;
        this.ausentes = ausentes;
        this.avaliacoesPendentes = avaliacoesPendentes;
        this.aprovados = aprovados;
        this.reprovados = reprovados;
        this.promovidos = promovidos;
        this.aprovadosNaoPromovidos = aprovadosNaoPromovidos;
        this.receitaBruta = receitaBruta;
        this.taxasPagamento = taxasPagamento;
        this.receitaLiquida = receitaLiquida;
        this.valoresPendentes = valoresPendentes;
        this.valoresEstornados = valoresEstornados;
        this.indicadores = indicadores;
        this.porPrajied = porPrajied;
    }

    public Long getEventoId() { return eventoId; }
    public String getTitulo() { return titulo; }
    public LocalDate getDataEvento() { return dataEvento; }
    public String getLocal() { return local; }
    public StatusEventoGraduacao getStatus() { return status; }
    public Integer getTotalConvidados() { return totalConvidados; }
    public Integer getConvitesPendentes() { return convitesPendentes; }
    public Integer getConvitesAceitos() { return convitesAceitos; }
    public Integer getConvitesRecusados() { return convitesRecusados; }
    public Integer getTotalInscritos() { return totalInscritos; }
    public Integer getInscricoesPagas() { return inscricoesPagas; }
    public Integer getInscricoesPendentes() { return inscricoesPendentes; }
    public Integer getInscricoesCanceladas() { return inscricoesCanceladas; }
    public Integer getInscricoesExpiradas() { return inscricoesExpiradas; }
    public Integer getPresentes() { return presentes; }
    public Integer getAusentes() { return ausentes; }
    public Integer getAvaliacoesPendentes() { return avaliacoesPendentes; }
    public Integer getAprovados() { return aprovados; }
    public Integer getReprovados() { return reprovados; }
    public Integer getPromovidos() { return promovidos; }
    public Integer getAprovadosNaoPromovidos() { return aprovadosNaoPromovidos; }
    public BigDecimal getReceitaBruta() { return receitaBruta; }
    public BigDecimal getTaxasPagamento() { return taxasPagamento; }
    public BigDecimal getReceitaLiquida() { return receitaLiquida; }
    public BigDecimal getValoresPendentes() { return valoresPendentes; }
    public BigDecimal getValoresEstornados() { return valoresEstornados; }
    public IndicadoresEventoGraduacaoResponse getIndicadores() { return indicadores; }
    public List<RelatorioPrajiedEventoResponse> getPorPrajied() { return porPrajied; }
}
