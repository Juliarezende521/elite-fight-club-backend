package Tic.Implementacao.dto.graduacao.evento;

public class PublicarEventoGraduacaoRequest {
    private Boolean criarConvitesAutomaticos = false;

    public Boolean getCriarConvitesAutomaticos() { return criarConvitesAutomaticos; }
    public void setCriarConvitesAutomaticos(Boolean criarConvitesAutomaticos) { this.criarConvitesAutomaticos = criarConvitesAutomaticos; }
}
