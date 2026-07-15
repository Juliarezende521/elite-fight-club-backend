package Tic.Implementacao.dto.graduacao.relatorio;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ResumoEventoGraduacaoRelatorioResponse {

    private Long eventoId;
    private String titulo;
    private LocalDate dataEvento;
    private String status;
    private Integer inscritos;
    private Integer pagos;
    private Integer presentes;
    private Integer aprovados;
    private Integer promovidos;
    private BigDecimal receitaBruta;
    private BigDecimal receitaLiquida;

    public ResumoEventoGraduacaoRelatorioResponse(Long eventoId, String titulo, LocalDate dataEvento, String status,
                                                  Integer inscritos, Integer pagos, Integer presentes,
                                                  Integer aprovados, Integer promovidos, BigDecimal receitaBruta,
                                                  BigDecimal receitaLiquida) {
        this.eventoId = eventoId;
        this.titulo = titulo;
        this.dataEvento = dataEvento;
        this.status = status;
        this.inscritos = inscritos;
        this.pagos = pagos;
        this.presentes = presentes;
        this.aprovados = aprovados;
        this.promovidos = promovidos;
        this.receitaBruta = receitaBruta;
        this.receitaLiquida = receitaLiquida;
    }

    public Long getEventoId() { return eventoId; }
    public String getTitulo() { return titulo; }
    public LocalDate getDataEvento() { return dataEvento; }
    public String getStatus() { return status; }
    public Integer getInscritos() { return inscritos; }
    public Integer getPagos() { return pagos; }
    public Integer getPresentes() { return presentes; }
    public Integer getAprovados() { return aprovados; }
    public Integer getPromovidos() { return promovidos; }
    public BigDecimal getReceitaBruta() { return receitaBruta; }
    public BigDecimal getReceitaLiquida() { return receitaLiquida; }
}
