package Tic.Implementacao.dto.auth;

import Tic.Implementacao.model.Academia;
import Tic.Implementacao.model.StatusAcademia;

public class AcademiaSessaoResponse {

    private Long id;
    private String nome;
    private StatusAcademia status;

    public AcademiaSessaoResponse() {
    }

    public AcademiaSessaoResponse(Long id, String nome, StatusAcademia status) {
        this.id = id;
        this.nome = nome;
        this.status = status;
    }

    public static AcademiaSessaoResponse from(Academia academia) {
        if (academia == null) {
            return null;
        }
        return new AcademiaSessaoResponse(academia.getId(), academia.getNome(), academia.getStatus());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public StatusAcademia getStatus() {
        return status;
    }

    public void setStatus(StatusAcademia status) {
        this.status = status;
    }
}
