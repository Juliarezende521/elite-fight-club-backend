package Tic.Implementacao.dto.graduacao.certificado;

import Tic.Implementacao.model.CertificadoGraduacao;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class CertificadoGraduacaoResponse {

    private Long id;
    private Long alunoId;
    private String alunoNome;
    private Long eventoId;
    private String eventoTitulo;
    private String graduacaoAnterior;
    private String graduacaoNova;
    private String numeroCertificado;
    private String codigoValidacao;
    private LocalDateTime dataEmissao;
    private String emitidoPorNome;
    private Boolean ativo;
    private String motivoCancelamento;
    private LocalDateTime canceladoEm;

    public static CertificadoGraduacaoResponse from(CertificadoGraduacao certificado) {
        CertificadoGraduacaoResponse response = new CertificadoGraduacaoResponse();
        response.id = certificado.getId();
        response.alunoId = certificado.getAluno().getId();
        response.alunoNome = certificado.getAluno().getNome();
        response.eventoId = certificado.getEvento().getId();
        response.eventoTitulo = certificado.getEvento().getTitulo();
        response.graduacaoAnterior = certificado.getGraduacaoAnterior() == null ? null : certificado.getGraduacaoAnterior().getNome();
        response.graduacaoNova = certificado.getGraduacaoNova().getNome();
        response.numeroCertificado = certificado.getNumeroCertificado();
        response.codigoValidacao = certificado.getCodigoValidacao();
        response.dataEmissao = certificado.getDataEmissao();
        response.emitidoPorNome = certificado.getEmitidoPor().getNome();
        response.ativo = certificado.getAtivo();
        response.motivoCancelamento = certificado.getMotivoCancelamento();
        response.canceladoEm = certificado.getCanceladoEm();
        return response;
    }

    public Long getId() { return id; }
    public Long getAlunoId() { return alunoId; }
    public String getAlunoNome() { return alunoNome; }
    public Long getEventoId() { return eventoId; }
    public String getEventoTitulo() { return eventoTitulo; }
    public String getGraduacaoAnterior() { return graduacaoAnterior; }
    public String getGraduacaoNova() { return graduacaoNova; }
    public String getNumeroCertificado() { return numeroCertificado; }
    public String getCodigoValidacao() { return codigoValidacao; }
    public LocalDateTime getDataEmissao() { return dataEmissao; }
    public String getEmitidoPorNome() { return emitidoPorNome; }
    public Boolean getAtivo() { return ativo; }
    public String getMotivoCancelamento() { return motivoCancelamento; }
    public LocalDateTime getCanceladoEm() { return canceladoEm; }
}
