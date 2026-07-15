package Tic.Implementacao.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "reservas_aula",
        indexes = {
                @Index(name = "idx_reserva_aula", columnList = "academia_id,aula_turma_id"),
                @Index(name = "idx_reserva_aluno", columnList = "academia_id,aluno_id")
        },
        uniqueConstraints = @UniqueConstraint(name = "uk_reserva_ativa", columnNames = "chave_reserva_ativa")
)
public class ReservaAula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "academia_id", nullable = false)
    private Academia academia;

    @ManyToOne
    @JoinColumn(name = "aula_turma_id", nullable = false)
    private AulaTurma aulaTurma;

    @ManyToOne
    @JoinColumn(name = "aluno_id", nullable = false)
    private Aluno aluno;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusReserva status = StatusReserva.CONFIRMADA;

    @Column(nullable = false)
    private LocalDateTime reservadoEm;

    private LocalDateTime canceladoEm;

    @Column(name = "chave_reserva_ativa", unique = true, length = 80)
    private String chaveReservaAtiva;

    public ReservaAula() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Academia getAcademia() { return academia; }
    public void setAcademia(Academia academia) { this.academia = academia; }
    public AulaTurma getAulaTurma() { return aulaTurma; }
    public void setAulaTurma(AulaTurma aulaTurma) { this.aulaTurma = aulaTurma; }
    public Aluno getAluno() { return aluno; }
    public void setAluno(Aluno aluno) { this.aluno = aluno; }
    public StatusReserva getStatus() { return status; }
    public void setStatus(StatusReserva status) { this.status = status; }
    public LocalDateTime getReservadoEm() { return reservadoEm; }
    public void setReservadoEm(LocalDateTime reservadoEm) { this.reservadoEm = reservadoEm; }
    public LocalDateTime getCanceladoEm() { return canceladoEm; }
    public void setCanceladoEm(LocalDateTime canceladoEm) { this.canceladoEm = canceladoEm; }
    public String getChaveReservaAtiva() { return chaveReservaAtiva; }
    public void setChaveReservaAtiva(String chaveReservaAtiva) { this.chaveReservaAtiva = chaveReservaAtiva; }
}
