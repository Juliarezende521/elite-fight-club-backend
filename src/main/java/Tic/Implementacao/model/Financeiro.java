package Tic.Implementacao.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Entity
@Table(name = "financeiros")
public class Financeiro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Descrição é obrigatória")
    @Column(nullable = false, length = 200)
    private String descricao;

    @NotNull(message = "Valor é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor precisa ser maior que zero")
    @Column(nullable = false)
    private Double valor;

    @NotNull(message = "Data de vencimento é obrigatória")
    @Column(nullable = false)
    private LocalDate dataVencimento;

    private LocalDate dataPagamento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusPagamento statusPagamento = StatusPagamento.PENDENTE;

    @ManyToOne
    @JoinColumn(name = "aluno_id")
    private Aluno aluno;

    @ManyToOne
    @JoinColumn(name = "modalidade_id")
    private Modalidade modalidade;

    public Financeiro() {
    }

    public Financeiro(String descricao, Double valor, LocalDate dataVencimento, LocalDate dataPagamento,
                      StatusPagamento statusPagamento, Aluno aluno, Modalidade modalidade) {
        this.descricao = descricao;
        this.valor = valor;
        this.dataVencimento = dataVencimento;
        this.dataPagamento = dataPagamento;
        this.statusPagamento = statusPagamento;
        this.aluno = aluno;
        this.modalidade = modalidade;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        if (descricao == null || descricao.isBlank()) throw new IllegalArgumentException("Descrição inválida");
        this.descricao = descricao;
    }

    public Double getValor() {
        return valor;
    }

    public void setValor(Double valor) {
        if (valor == null || valor < 0) throw new IllegalArgumentException("Valor inválido");
        this.valor = valor;
    }

    public LocalDate getDataVencimento() {
        return dataVencimento;
    }

    public void setDataVencimento(LocalDate dataVencimento) {
        this.dataVencimento = dataVencimento;
    }

    public LocalDate getDataPagamento() {
        return dataPagamento;
    }

    public void setDataPagamento(LocalDate dataPagamento) {
        this.dataPagamento = dataPagamento;
    }

    public StatusPagamento getStatusPagamento() {
        return statusPagamento;
    }

    public void setStatusPagamento(StatusPagamento statusPagamento) {
        if (statusPagamento == null) throw new IllegalArgumentException("Status do pagamento inválido");
        this.statusPagamento = statusPagamento;
    }

    public Aluno getAluno() {
        return aluno;
    }

    public void setAluno(Aluno aluno) {
        this.aluno = aluno;
    }

    public Modalidade getModalidade() {
        return modalidade;
    }

    public void setModalidade(Modalidade modalidade) {
        this.modalidade = modalidade;
    }

    public void registrarPagamento() {
        this.dataPagamento = LocalDate.now();
        this.statusPagamento = StatusPagamento.PAGO;
    }

    public boolean estaPago() {
        return dataPagamento != null || StatusPagamento.PAGO.equals(statusPagamento);
    }
}
