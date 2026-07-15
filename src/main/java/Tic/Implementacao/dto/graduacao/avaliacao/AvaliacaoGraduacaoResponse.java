package Tic.Implementacao.dto.graduacao.avaliacao;

import Tic.Implementacao.model.AvaliacaoGraduacao;
import Tic.Implementacao.model.ResultadoAvaliacaoGraduacao;
import Tic.Implementacao.model.StatusPresencaGraduacao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class AvaliacaoGraduacaoResponse {
    private Long id;
    private Long eventoId;
    private String eventoTitulo;
    private LocalDate dataEvento;
    private Long inscricaoId;
    private Long alunoId;
    private String alunoNome;
    private Long graduacaoAtualId;
    private String graduacaoAtualNome;
    private Long graduacaoDestinoId;
    private String graduacaoDestinoNome;
    private StatusPresencaGraduacao statusPresenca;
    private ResultadoAvaliacaoGraduacao resultado;
    private BigDecimal notaTecnica;
    private String observacaoTecnica;
    private LocalDateTime avaliadoEm;
    private Long avaliadoPorId;
    private String avaliadoPorNome;
    private Boolean promocaoConfirmada;
    private LocalDateTime promovidoEm;
    private Long promovidoPorId;
    private String promovidoPorNome;
    private Long historicoGeradoId;

    public static AvaliacaoGraduacaoResponse from(AvaliacaoGraduacao avaliacao) {
        AvaliacaoGraduacaoResponse response = new AvaliacaoGraduacaoResponse();
        response.id = avaliacao.getId();
        response.eventoId = avaliacao.getEvento().getId();
        response.eventoTitulo = avaliacao.getEvento().getTitulo();
        response.dataEvento = avaliacao.getEvento().getDataEvento();
        response.inscricaoId = avaliacao.getInscricao().getId();
        response.alunoId = avaliacao.getAluno().getId();
        response.alunoNome = avaliacao.getAluno().getNome();
        response.graduacaoAtualId = avaliacao.getGraduacaoAtual().getId();
        response.graduacaoAtualNome = avaliacao.getGraduacaoAtual().getNome();
        response.graduacaoDestinoId = avaliacao.getGraduacaoDestino().getId();
        response.graduacaoDestinoNome = avaliacao.getGraduacaoDestino().getNome();
        response.statusPresenca = avaliacao.getStatusPresenca();
        response.resultado = avaliacao.getResultado();
        response.notaTecnica = avaliacao.getNotaTecnica();
        response.observacaoTecnica = avaliacao.getObservacaoTecnica();
        response.avaliadoEm = avaliacao.getAvaliadoEm();
        response.avaliadoPorId = avaliacao.getAvaliadoPor() == null ? null : avaliacao.getAvaliadoPor().getId();
        response.avaliadoPorNome = avaliacao.getAvaliadoPor() == null ? null : avaliacao.getAvaliadoPor().getNome();
        response.promocaoConfirmada = avaliacao.getPromocaoConfirmada();
        response.promovidoEm = avaliacao.getPromovidoEm();
        response.promovidoPorId = avaliacao.getPromovidoPor() == null ? null : avaliacao.getPromovidoPor().getId();
        response.promovidoPorNome = avaliacao.getPromovidoPor() == null ? null : avaliacao.getPromovidoPor().getNome();
        response.historicoGeradoId = avaliacao.getHistoricoGerado() == null ? null : avaliacao.getHistoricoGerado().getId();
        return response;
    }

    public Long getId() { return id; }
    public Long getEventoId() { return eventoId; }
    public String getEventoTitulo() { return eventoTitulo; }
    public LocalDate getDataEvento() { return dataEvento; }
    public Long getInscricaoId() { return inscricaoId; }
    public Long getAlunoId() { return alunoId; }
    public String getAlunoNome() { return alunoNome; }
    public Long getGraduacaoAtualId() { return graduacaoAtualId; }
    public String getGraduacaoAtualNome() { return graduacaoAtualNome; }
    public Long getGraduacaoDestinoId() { return graduacaoDestinoId; }
    public String getGraduacaoDestinoNome() { return graduacaoDestinoNome; }
    public StatusPresencaGraduacao getStatusPresenca() { return statusPresenca; }
    public ResultadoAvaliacaoGraduacao getResultado() { return resultado; }
    public BigDecimal getNotaTecnica() { return notaTecnica; }
    public String getObservacaoTecnica() { return observacaoTecnica; }
    public LocalDateTime getAvaliadoEm() { return avaliadoEm; }
    public Long getAvaliadoPorId() { return avaliadoPorId; }
    public String getAvaliadoPorNome() { return avaliadoPorNome; }
    public Boolean getPromocaoConfirmada() { return promocaoConfirmada; }
    public LocalDateTime getPromovidoEm() { return promovidoEm; }
    public Long getPromovidoPorId() { return promovidoPorId; }
    public String getPromovidoPorNome() { return promovidoPorNome; }
    public Long getHistoricoGeradoId() { return historicoGeradoId; }
}
