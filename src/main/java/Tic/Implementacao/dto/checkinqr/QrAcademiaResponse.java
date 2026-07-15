package Tic.Implementacao.dto.checkinqr;

public class QrAcademiaResponse {
    private Long academiaId;
    private String codigoPublico;
    private String checkInUrl;

    public QrAcademiaResponse(Long academiaId, String codigoPublico, String checkInUrl) {
        this.academiaId = academiaId;
        this.codigoPublico = codigoPublico;
        this.checkInUrl = checkInUrl;
    }

    public Long getAcademiaId() { return academiaId; }
    public String getCodigoPublico() { return codigoPublico; }
    public String getCheckInUrl() { return checkInUrl; }
}
