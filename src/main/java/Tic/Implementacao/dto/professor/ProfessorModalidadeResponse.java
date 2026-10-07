package Tic.Implementacao.dto.professor;

public class ProfessorModalidadeResponse {

    private Long id;
    private Long modalidadeId;
    private String modalidadeNome;
    private Boolean permiteTurma;
    private Boolean permitePersonal;
    private Boolean ativo;

    public ProfessorModalidadeResponse(Long id, Long modalidadeId, String modalidadeNome,
                                       Boolean permiteTurma, Boolean permitePersonal, Boolean ativo) {
        this.id = id;
        this.modalidadeId = modalidadeId;
        this.modalidadeNome = modalidadeNome;
        this.permiteTurma = permiteTurma;
        this.permitePersonal = permitePersonal;
        this.ativo = ativo;
    }

    public Long getId() { return id; }
    public Long getModalidadeId() { return modalidadeId; }
    public String getModalidadeNome() { return modalidadeNome; }
    public Boolean getPermiteTurma() { return permiteTurma; }
    public Boolean getPermitePersonal() { return permitePersonal; }
    public Boolean getAtivo() { return ativo; }
}
