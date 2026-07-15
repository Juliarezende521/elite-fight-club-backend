package Tic.Implementacao.exception;

import java.util.Map;

public class ApiErrorResponse {
    private int status;
    private String erro;
    private String mensagem;
    private Map<String, String> campos;

    public ApiErrorResponse(int status, String erro, String mensagem, Map<String, String> campos) {
        this.status = status;
        this.erro = erro;
        this.mensagem = mensagem;
        this.campos = campos;
    }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    public String getErro() { return erro; }
    public void setErro(String erro) { this.erro = erro; }
    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }
    public Map<String, String> getCampos() { return campos; }
    public void setCampos(Map<String, String> campos) { this.campos = campos; }
}
