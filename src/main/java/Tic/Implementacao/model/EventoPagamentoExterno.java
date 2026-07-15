package Tic.Implementacao.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "eventos_pagamento_externo",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_evento_pagamento_externo",
                columnNames = {"provedor", "evento_externo_id", "tipo_evento"}
        )
)
public class EventoPagamentoExterno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 40)
    private String provedor;

    @Column(name = "evento_externo_id", nullable = false, length = 120)
    private String eventoExternoId;

    @Column(name = "tipo_evento", nullable = false, length = 80)
    private String tipoEvento;

    @Column(name = "pagamento_externo_id", length = 120)
    private String pagamentoExternoId;

    @ManyToOne
    @JoinColumn(name = "pagamento_id")
    private Pagamento pagamento;

    @Column(nullable = false)
    private LocalDateTime processadoEm;

    public EventoPagamentoExterno() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getProvedor() { return provedor; }
    public void setProvedor(String provedor) { this.provedor = provedor; }
    public String getEventoExternoId() { return eventoExternoId; }
    public void setEventoExternoId(String eventoExternoId) { this.eventoExternoId = eventoExternoId; }
    public String getTipoEvento() { return tipoEvento; }
    public void setTipoEvento(String tipoEvento) { this.tipoEvento = tipoEvento; }
    public String getPagamentoExternoId() { return pagamentoExternoId; }
    public void setPagamentoExternoId(String pagamentoExternoId) { this.pagamentoExternoId = pagamentoExternoId; }
    public Pagamento getPagamento() { return pagamento; }
    public void setPagamento(Pagamento pagamento) { this.pagamento = pagamento; }
    public LocalDateTime getProcessadoEm() { return processadoEm; }
    public void setProcessadoEm(LocalDateTime processadoEm) { this.processadoEm = processadoEm; }
}
