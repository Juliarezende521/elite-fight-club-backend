package Tic.Implementacao.dto.graduacao.evento;

import Tic.Implementacao.model.StatusConviteGraduacao;
import Tic.Implementacao.model.StatusInscricaoGraduacao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class EventoGraduacaoAlunoResponse {
    private Long eventoId;
    private String titulo;
    private String descricao;
    private LocalDate data;
    private LocalTime horaInicio;
    private LocalTime horaFim;
    private String local;
    private LocalDateTime fimInscricoes;
    private String graduacaoAtual;
    private String proximaGraduacao;
    private Integer checkInsAtuais;
    private Integer checkInsNecessarios;
    private Boolean elegivel;
    private BigDecimal valor;
    private StatusConviteGraduacao statusConvite;
    private StatusInscricaoGraduacao statusInscricao;

    public EventoGraduacaoAlunoResponse(Long eventoId, String titulo, String descricao, LocalDate data,
                                        LocalTime horaInicio, LocalTime horaFim, String local,
                                        LocalDateTime fimInscricoes, String graduacaoAtual, String proximaGraduacao,
                                        Integer checkInsAtuais, Integer checkInsNecessarios, Boolean elegivel,
                                        BigDecimal valor, StatusConviteGraduacao statusConvite,
                                        StatusInscricaoGraduacao statusInscricao) {
        this.eventoId = eventoId;
        this.titulo = titulo;
        this.descricao = descricao;
        this.data = data;
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
        this.local = local;
        this.fimInscricoes = fimInscricoes;
        this.graduacaoAtual = graduacaoAtual;
        this.proximaGraduacao = proximaGraduacao;
        this.checkInsAtuais = checkInsAtuais;
        this.checkInsNecessarios = checkInsNecessarios;
        this.elegivel = elegivel;
        this.valor = valor;
        this.statusConvite = statusConvite;
        this.statusInscricao = statusInscricao;
    }

    public Long getEventoId() { return eventoId; }
    public String getTitulo() { return titulo; }
    public String getDescricao() { return descricao; }
    public LocalDate getData() { return data; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public LocalTime getHoraFim() { return horaFim; }
    public String getLocal() { return local; }
    public LocalDateTime getFimInscricoes() { return fimInscricoes; }
    public String getGraduacaoAtual() { return graduacaoAtual; }
    public String getProximaGraduacao() { return proximaGraduacao; }
    public Integer getCheckInsAtuais() { return checkInsAtuais; }
    public Integer getCheckInsNecessarios() { return checkInsNecessarios; }
    public Boolean getElegivel() { return elegivel; }
    public BigDecimal getValor() { return valor; }
    public StatusConviteGraduacao getStatusConvite() { return statusConvite; }
    public StatusInscricaoGraduacao getStatusInscricao() { return statusInscricao; }
}
