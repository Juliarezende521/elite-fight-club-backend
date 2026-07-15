package Tic.Implementacao.dto.graduacao.evento;

import Tic.Implementacao.model.StatusEventoGraduacao;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class EventoGraduacaoRequest {

    @NotBlank(message = "Título é obrigatório")
    @Size(max = 150, message = "Título deve ter no máximo 150 caracteres")
    private String titulo;

    @Size(max = 2000, message = "Descrição deve ter no máximo 2000 caracteres")
    private String descricao;

    @NotNull(message = "Data do evento é obrigatória")
    private LocalDate dataEvento;

    @NotNull(message = "Hora de início é obrigatória")
    private LocalTime horaInicio;

    @NotNull(message = "Hora de fim é obrigatória")
    private LocalTime horaFim;

    @NotBlank(message = "Local é obrigatório")
    @Size(max = 200, message = "Local deve ter no máximo 200 caracteres")
    private String local;

    @NotNull(message = "Início das inscrições é obrigatório")
    private LocalDateTime inicioInscricoes;

    @NotNull(message = "Fim das inscrições é obrigatório")
    private LocalDateTime fimInscricoes;

    private StatusEventoGraduacao status = StatusEventoGraduacao.RASCUNHO;

    @Min(value = 1, message = "Limite de participantes deve ser maior que zero")
    private Integer limiteParticipantes;

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public LocalDate getDataEvento() { return dataEvento; }
    public void setDataEvento(LocalDate dataEvento) { this.dataEvento = dataEvento; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }
    public LocalTime getHoraFim() { return horaFim; }
    public void setHoraFim(LocalTime horaFim) { this.horaFim = horaFim; }
    public String getLocal() { return local; }
    public void setLocal(String local) { this.local = local; }
    public LocalDateTime getInicioInscricoes() { return inicioInscricoes; }
    public void setInicioInscricoes(LocalDateTime inicioInscricoes) { this.inicioInscricoes = inicioInscricoes; }
    public LocalDateTime getFimInscricoes() { return fimInscricoes; }
    public void setFimInscricoes(LocalDateTime fimInscricoes) { this.fimInscricoes = fimInscricoes; }
    public StatusEventoGraduacao getStatus() { return status; }
    public void setStatus(StatusEventoGraduacao status) { this.status = status; }
    public Integer getLimiteParticipantes() { return limiteParticipantes; }
    public void setLimiteParticipantes(Integer limiteParticipantes) { this.limiteParticipantes = limiteParticipantes; }
}
