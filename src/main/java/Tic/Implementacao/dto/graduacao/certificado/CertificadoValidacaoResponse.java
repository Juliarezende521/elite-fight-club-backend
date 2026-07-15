package Tic.Implementacao.dto.graduacao.certificado;

import java.time.LocalDateTime;

public class CertificadoValidacaoResponse {

    private Boolean valido;
    private String status;
    private String alunoNome;
    private String academia;
    private String evento;
    private String graduacaoConquistada;
    private LocalDateTime dataEmissao;

    public CertificadoValidacaoResponse(Boolean valido, String status, String alunoNome, String academia,
                                        String evento, String graduacaoConquistada, LocalDateTime dataEmissao) {
        this.valido = valido;
        this.status = status;
        this.alunoNome = alunoNome;
        this.academia = academia;
        this.evento = evento;
        this.graduacaoConquistada = graduacaoConquistada;
        this.dataEmissao = dataEmissao;
    }

    public Boolean getValido() { return valido; }
    public String getStatus() { return status; }
    public String getAlunoNome() { return alunoNome; }
    public String getAcademia() { return academia; }
    public String getEvento() { return evento; }
    public String getGraduacaoConquistada() { return graduacaoConquistada; }
    public LocalDateTime getDataEmissao() { return dataEmissao; }
}
