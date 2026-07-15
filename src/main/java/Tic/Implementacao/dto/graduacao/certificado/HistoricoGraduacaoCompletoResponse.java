package Tic.Implementacao.dto.graduacao.certificado;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class HistoricoGraduacaoCompletoResponse {

    private Long historicoId;
    private String graduacaoAnterior;
    private String graduacaoNova;
    private Long eventoId;
    private String eventoTitulo;
    private String professorResponsavel;
    private LocalDateTime data;
    private BigDecimal nota;
    private String observacoes;
    private Boolean certificadoEmitido;
    private Long certificadoId;
    private String numeroCertificado;

    public HistoricoGraduacaoCompletoResponse(Long historicoId, String graduacaoAnterior, String graduacaoNova,
                                              Long eventoId, String eventoTitulo, String professorResponsavel,
                                              LocalDateTime data, BigDecimal nota, String observacoes,
                                              Boolean certificadoEmitido, Long certificadoId, String numeroCertificado) {
        this.historicoId = historicoId;
        this.graduacaoAnterior = graduacaoAnterior;
        this.graduacaoNova = graduacaoNova;
        this.eventoId = eventoId;
        this.eventoTitulo = eventoTitulo;
        this.professorResponsavel = professorResponsavel;
        this.data = data;
        this.nota = nota;
        this.observacoes = observacoes;
        this.certificadoEmitido = certificadoEmitido;
        this.certificadoId = certificadoId;
        this.numeroCertificado = numeroCertificado;
    }

    public Long getHistoricoId() { return historicoId; }
    public String getGraduacaoAnterior() { return graduacaoAnterior; }
    public String getGraduacaoNova() { return graduacaoNova; }
    public Long getEventoId() { return eventoId; }
    public String getEventoTitulo() { return eventoTitulo; }
    public String getProfessorResponsavel() { return professorResponsavel; }
    public LocalDateTime getData() { return data; }
    public BigDecimal getNota() { return nota; }
    public String getObservacoes() { return observacoes; }
    public Boolean getCertificadoEmitido() { return certificadoEmitido; }
    public Long getCertificadoId() { return certificadoId; }
    public String getNumeroCertificado() { return numeroCertificado; }
}
