package Tic.Implementacao.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "eventos_graduacao")
public class EventoGraduacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "academia_id", nullable = false)
    private Academia academia;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(length = 2000)
    private String descricao;

    @Column(nullable = false)
    private LocalDate dataEvento;

    @Column(nullable = false)
    private LocalTime horaInicio;

    @Column(nullable = false)
    private LocalTime horaFim;

    @Column(name = "local_evento", nullable = false, length = 200)
    private String local;

    @Column(nullable = false)
    private LocalDateTime inicioInscricoes;

    @Column(nullable = false)
    private LocalDateTime fimInscricoes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private StatusEventoGraduacao status = StatusEventoGraduacao.RASCUNHO;

    private Integer limiteParticipantes;

    @Column(nullable = false)
    private LocalDateTime criadoEm;

    @Column(nullable = false)
    private LocalDateTime atualizadoEm;

    @ManyToOne(optional = false)
    @JoinColumn(name = "criado_por_id", nullable = false)
    private Usuario criadoPor;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Academia getAcademia() { return academia; }
    public void setAcademia(Academia academia) { this.academia = academia; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public LocalDate getDataEvento() { return dataEvento; }
    public void setDataEvento(LocalDate dataEvento) { this.dataEvento = dataEvento; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }
    public LocalTime getHoraFim() { return horaFim; }
    public void setHoraFim(LocalTime horaFim) { this.horaFim = horaFim; }
    public String getLocal() { return local; }
    public void setLocal(String local) { this.local = local; }
    public LocalDateTime getInicioInscricoes() { return inicioInscricoes; }
    public void setInicioInscricoes(LocalDateTime inicioInscricoes) { this.inicioInscricoes = inicioInscricoes; }
    public LocalDateTime getFimInscricoes() { return fimInscricoes; }
    public void setFimInscricoes(LocalDateTime fimInscricoes) { this.fimInscricoes = fimInscricoes; }
    public StatusEventoGraduacao getStatus() { return status; }
    public void setStatus(StatusEventoGraduacao status) { this.status = status; }
    public Integer getLimiteParticipantes() { return limiteParticipantes; }
    public void setLimiteParticipantes(Integer limiteParticipantes) { this.limiteParticipantes = limiteParticipantes; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }
    public void setAtualizadoEm(LocalDateTime atualizadoEm) { this.atualizadoEm = atualizadoEm; }
    public Usuario getCriadoPor() { return criadoPor; }
    public void setCriadoPor(Usuario criadoPor) { this.criadoPor = criadoPor; }
}
