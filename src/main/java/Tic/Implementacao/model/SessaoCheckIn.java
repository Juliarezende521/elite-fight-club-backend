package Tic.Implementacao.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "sessoes_check_in",
        indexes = {
                @Index(name = "idx_sessao_academia_status", columnList = "academia_id,status,expira_em"),
                @Index(name = "idx_sessao_aula_turma", columnList = "academia_id,aula_turma_id"),
                @Index(name = "idx_sessao_aula_personal", columnList = "academia_id,aula_personal_id")
        }
)
public class SessaoCheckIn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "academia_id", nullable = false)
    private Academia academia;

    @ManyToOne
    @JoinColumn(name = "aula_turma_id")
    private AulaTurma aulaTurma;

    @ManyToOne
    @JoinColumn(name = "aula_personal_id")
    private AulaPersonal aulaPersonal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusSessaoCheckIn status = StatusSessaoCheckIn.ABERTA;

    @Column(nullable = false)
    private LocalDateTime abertaEm;

    @Column(nullable = false)
    private LocalDateTime expiraEm;

    private LocalDateTime encerradaEm;

    @ManyToOne(optional = false)
    @JoinColumn(name = "aberta_por_id", nullable = false)
    private Usuario abertaPor;

    @Column(name = "chave_sessao_ativa", unique = true, length = 80)
    private String chaveSessaoAtiva;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Academia getAcademia() { return academia; }
    public void setAcademia(Academia academia) { this.academia = academia; }
    public AulaTurma getAulaTurma() { return aulaTurma; }
    public void setAulaTurma(AulaTurma aulaTurma) { this.aulaTurma = aulaTurma; }
    public AulaPersonal getAulaPersonal() { return aulaPersonal; }
    public void setAulaPersonal(AulaPersonal aulaPersonal) { this.aulaPersonal = aulaPersonal; }
    public StatusSessaoCheckIn getStatus() { return status; }
    public void setStatus(StatusSessaoCheckIn status) { this.status = status; }
    public LocalDateTime getAbertaEm() { return abertaEm; }
    public void setAbertaEm(LocalDateTime abertaEm) { this.abertaEm = abertaEm; }
    public LocalDateTime getExpiraEm() { return expiraEm; }
    public void setExpiraEm(LocalDateTime expiraEm) { this.expiraEm = expiraEm; }
    public LocalDateTime getEncerradaEm() { return encerradaEm; }
    public void setEncerradaEm(LocalDateTime encerradaEm) { this.encerradaEm = encerradaEm; }
    public Usuario getAbertaPor() { return abertaPor; }
    public void setAbertaPor(Usuario abertaPor) { this.abertaPor = abertaPor; }
    public String getChaveSessaoAtiva() { return chaveSessaoAtiva; }
    public void setChaveSessaoAtiva(String chaveSessaoAtiva) { this.chaveSessaoAtiva = chaveSessaoAtiva; }
}
