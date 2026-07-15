package Tic.Implementacao.payments.asaas.dto;

public class AsaasPixQrCodeResponse {
    private String encodedImage;
    private String payload;
    private String expirationDate;

    public String getEncodedImage() { return encodedImage; }
    public void setEncodedImage(String encodedImage) { this.encodedImage = encodedImage; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
    public String getExpirationDate() { return expirationDate; }
    public void setExpirationDate(String expirationDate) { this.expirationDate = expirationDate; }
}
