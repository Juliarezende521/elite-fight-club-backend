package Tic.Implementacao.dto.agenda.professor;

import java.time.LocalDateTime;

public class AlunoReservaResponse {

    private Long alunoId;
    private String nome;
    private LocalDateTime reservadoEm;

    public AlunoReservaResponse(Long alunoId, String nome, LocalDateTime reservadoEm) {
        this.alunoId = alunoId;
        this.nome = nome;
        this.reservadoEm = reservadoEm;
    }

    public Long getAlunoId() { return alunoId; }
    public String getNome() { return nome; }
    public LocalDateTime getReservadoEm() { return reservadoEm; }
}
