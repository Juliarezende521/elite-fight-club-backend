package Tic.Implementacao.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "relatorios")
public class Relatorio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String tipoRelatorio;

    @Column(nullable = false, length = 100)
    private String periodo;

    private LocalDate dataInicio;

    private LocalDate dataFim;

    @Column(nullable = false)
    private LocalDate dataGeracao;

    @Column(length = 1000)
    private String conteudo;

    public Relatorio() {
    }

    public Relatorio(String tipoRelatorio, String periodo, LocalDate dataInicio,
                     LocalDate dataFim, LocalDate dataGeracao, String conteudo) {
        this.tipoRelatorio = tipoRelatorio;
        this.periodo = periodo;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.dataGeracao = dataGeracao;
        this.conteudo = conteudo;
    }

    public Long getId() {
        return id;
    }

    public String getTipoRelatorio() {
        return tipoRelatorio;
    }

    public void setTipoRelatorio(String tipoRelatorio) {
        this.tipoRelatorio = tipoRelatorio;
    }

    public String getPeriodo() {
        return periodo;
    }

    public void setPeriodo(String periodo) {
        this.periodo = periodo;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDate getDataFim() {
        return dataFim;
    }

    public void setDataFim(LocalDate dataFim) {
        this.dataFim = dataFim;
    }

    public LocalDate getDataGeracao() {
        return dataGeracao;
    }

    public void setDataGeracao(LocalDate dataGeracao) {
        this.dataGeracao = dataGeracao;
    }

    public String getConteudo() {
        return conteudo;
    }

    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }
}
