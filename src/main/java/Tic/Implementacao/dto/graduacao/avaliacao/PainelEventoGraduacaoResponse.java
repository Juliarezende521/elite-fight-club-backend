package Tic.Implementacao.dto.graduacao.avaliacao;

import Tic.Implementacao.model.StatusEventoGraduacao;

import java.time.LocalDate;
import java.util.List;

public class PainelEventoGraduacaoResponse {
    private Long eventoId;
    private String titulo;
    private LocalDate dataEvento;
    private StatusEventoGraduacao status;
    private Integer totalInscritos;
    private Integer totalPagos;
    private Integer presentes;
    private Integer ausentes;
    private Integer avaliacoesPendentes;
    private Integer aprovados;
    private Integer reprovados;
    private Integer promovidos;
    private Integer aprovadosAguardandoPromocao;
    private List<ParticipanteGraduacaoResponse> participantes;

    public PainelEventoGraduacaoResponse(Long eventoId, String titulo, LocalDate dataEvento, StatusEventoGraduacao status,
                                          Integer totalInscritos, Integer totalPagos, Integer presentes,
                                          Integer ausentes, Integer avaliacoesPendentes, Integer aprovados,
                                          Integer reprovados, Integer promovidos, Integer aprovadosAguardandoPromocao,
                                          List<ParticipanteGraduacaoResponse> participantes) {
        this.eventoId = eventoId;
        this.titulo = titulo;
        this.dataEvento = dataEvento;
        this.status = status;
        this.totalInscritos = totalInscritos;
        this.totalPagos = totalPagos;
        this.presentes = presentes;
        this.ausentes = ausentes;
        this.avaliacoesPendentes = avaliacoesPendentes;
        this.aprovados = aprovados;
        this.reprovados = reprovados;
        this.promovidos = promovidos;
        this.aprovadosAguardandoPromocao = aprovadosAguardandoPromocao;
        this.participantes = participantes;
    }

    public Long getEventoId() { return eventoId; }
    public String getTitulo() { return titulo; }
    public LocalDate getDataEvento() { return dataEvento; }
    public StatusEventoGraduacao getStatus() { return status; }
    public Integer getTotalInscritos() { return totalInscritos; }
    public Integer getTotalPagos() { return totalPagos; }
    public Integer getPresentes() { return presentes; }
    public Integer getAusentes() { return ausentes; }
    public Integer getAvaliacoesPendentes() { return avaliacoesPendentes; }
    public Integer getAprovados() { return aprovados; }
    public Integer getReprovados() { return reprovados; }
    public Integer getPromovidos() { return promovidos; }
    public Integer getAprovadosAguardandoPromocao() { return aprovadosAguardandoPromocao; }
    public List<ParticipanteGraduacaoResponse> getParticipantes() { return participantes; }
}
