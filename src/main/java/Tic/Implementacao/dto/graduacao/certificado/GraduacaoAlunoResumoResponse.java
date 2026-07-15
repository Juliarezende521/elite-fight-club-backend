package Tic.Implementacao.dto.graduacao.certificado;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class GraduacaoAlunoResumoResponse {

    private String graduacaoAtual;
    private String proximaGraduacao;
    private Integer checkInsAtuais;
    private Integer checkInsRestantes;
    private Integer diasNaGraduacaoAtual;
    private LocalDate dataEntradaGraduacaoAtual;
    private LocalDateTime dataUltimaPromocao;
    private CertificadoGraduacaoResponse ultimoCertificado;
    private List<HistoricoGraduacaoCompletoResponse> historico;
    private List<CertificadoGraduacaoResponse> certificados;

    public GraduacaoAlunoResumoResponse(String graduacaoAtual, String proximaGraduacao, Integer checkInsAtuais,
                                        Integer checkInsRestantes, Integer diasNaGraduacaoAtual,
                                        LocalDate dataEntradaGraduacaoAtual, LocalDateTime dataUltimaPromocao,
                                        CertificadoGraduacaoResponse ultimoCertificado,
                                        List<HistoricoGraduacaoCompletoResponse> historico,
                                        List<CertificadoGraduacaoResponse> certificados) {
        this.graduacaoAtual = graduacaoAtual;
        this.proximaGraduacao = proximaGraduacao;
        this.checkInsAtuais = checkInsAtuais;
        this.checkInsRestantes = checkInsRestantes;
        this.diasNaGraduacaoAtual = diasNaGraduacaoAtual;
        this.dataEntradaGraduacaoAtual = dataEntradaGraduacaoAtual;
        this.dataUltimaPromocao = dataUltimaPromocao;
        this.ultimoCertificado = ultimoCertificado;
        this.historico = historico;
        this.certificados = certificados;
    }

    public String getGraduacaoAtual() { return graduacaoAtual; }
    public String getProximaGraduacao() { return proximaGraduacao; }
    public Integer getCheckInsAtuais() { return checkInsAtuais; }
    public Integer getCheckInsRestantes() { return checkInsRestantes; }
    public Integer getDiasNaGraduacaoAtual() { return diasNaGraduacaoAtual; }
    public LocalDate getDataEntradaGraduacaoAtual() { return dataEntradaGraduacaoAtual; }
    public LocalDateTime getDataUltimaPromocao() { return dataUltimaPromocao; }
    public CertificadoGraduacaoResponse getUltimoCertificado() { return ultimoCertificado; }
    public List<HistoricoGraduacaoCompletoResponse> getHistorico() { return historico; }
    public List<CertificadoGraduacaoResponse> getCertificados() { return certificados; }
}
