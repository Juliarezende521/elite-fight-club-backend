package Tic.Implementacao.dto.aula;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public class RemarcarAulaRequest {

    @NotNull(message = "Nova data é obrigatória")
    private LocalDate novaData;

    private LocalTime novaHoraInicio;
    private LocalTime novaHoraFim;
    private String observacao;

    public LocalDate getNovaData() { return novaData; }
    public void setNovaData(LocalDate novaData) { this.novaData = novaData; }
    public LocalTime getNovaHoraInicio() { return novaHoraInicio; }
    public void setNovaHoraInicio(LocalTime novaHoraInicio) { this.novaHoraInicio = novaHoraInicio; }
    public LocalTime getNovaHoraFim() { return novaHoraFim; }
    public void setNovaHoraFim(LocalTime novaHoraFim) { this.novaHoraFim = novaHoraFim; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
}
