package Tic.Implementacao.dto.graduacao.certificado;

import java.util.List;

public class DashboardProfessorGraduacaoResponse {

    private Long totalCertificadosEmitidos;
    private Long certificadosCancelados;
    private Integer alunosAguardandoCertificado;
    private List<CertificadoGraduacaoResponse> ultimasPromocoes;

    public DashboardProfessorGraduacaoResponse(Long totalCertificadosEmitidos, Long certificadosCancelados,
                                               Integer alunosAguardandoCertificado,
                                               List<CertificadoGraduacaoResponse> ultimasPromocoes) {
        this.totalCertificadosEmitidos = totalCertificadosEmitidos;
        this.certificadosCancelados = certificadosCancelados;
        this.alunosAguardandoCertificado = alunosAguardandoCertificado;
        this.ultimasPromocoes = ultimasPromocoes;
    }

    public Long getTotalCertificadosEmitidos() { return totalCertificadosEmitidos; }
    public Long getCertificadosCancelados() { return certificadosCancelados; }
    public Integer getAlunosAguardandoCertificado() { return alunosAguardandoCertificado; }
    public List<CertificadoGraduacaoResponse> getUltimasPromocoes() { return ultimasPromocoes; }
}
