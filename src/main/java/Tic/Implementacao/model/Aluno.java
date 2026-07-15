package Tic.Implementacao.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "alunos")
public class Aluno extends Pessoa {
    @Id  //indica chave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private int idade;

    @Column(nullable = false)
    private float peso;

    @Column(nullable = false)
    private float altura;

    @DecimalMin(value = "0.01", message = "Valor da mensalidade precisa ser maior que zero")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valorMensalidade = BigDecimal.ZERO;

    @Column(length = 500)
    private String graduacaoDescricao;

    @Column(unique = true, length = 150)
    private String firebaseUid;

    @Column(name = "asaas_customer_id", length = 80)
    private String asaasCustomerId;

    @Email(message = "E-mail inválido")
    @Column(unique = true, length = 150)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoAluno tipoAluno = TipoAluno.TURMA;

    @Min(value = 1, message = "Dia de vencimento deve ser entre 1 e 28")
    @Max(value = 28, message = "Dia de vencimento deve ser entre 1 e 28")
    @Column(nullable = false)
    private Integer diaVencimento = 1;

    @Column(nullable = false)
    private Integer quantidadeCheckins = 0;

    @Column(nullable = false)
    private Boolean ativo = true;

    @ManyToOne
    @JoinColumn(name = "academia_id")
    private Academia academia;

    @ManyToMany
    @JoinTable(
            name = "alunos_modalidades",
            joinColumns = @JoinColumn(name = "aluno_id"),
            inverseJoinColumns = @JoinColumn(name = "modalidade_id")
    )
    private List<Modalidade> modalidades = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "graduacao_id")
    private Graduacao graduacao;

    @ManyToOne
    @JoinColumn(name = "plano_id")
    private Plano plano;

    public Aluno() {
    }

    public Aluno(String cpf, String nome, int idade, float peso, float altura, float valorMensalidade, String graduacao) {
        super(cpf, nome);
        this.idade = idade;
        this.peso = peso;
        this.altura = altura;
        this.valorMensalidade = BigDecimal.valueOf(valorMensalidade);
        this.graduacaoDescricao = graduacao;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getIdade() {
        return idade;
    }

    public void setIdade(int idade) {
        if (idade <= 0) throw new IllegalArgumentException("Idade inválida");
        this.idade = idade;
    }

    public float getPeso() {
        return peso;
    }

    public void setPeso(float peso) {
        if (peso <= 0) throw new IllegalArgumentException("Peso inválido");
        this.peso = peso;
    }

    public float getAltura() {
        return altura;
    }

    public void setAltura(float altura) {
        if (altura <= 0) throw new IllegalArgumentException("Altura inválida");
        this.altura = altura;
    }

    public BigDecimal getValorMensalidade() {
        return valorMensalidade;
    }

    public void setValorMensalidade(BigDecimal valorMensalidade) {
        if (valorMensalidade == null || valorMensalidade.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Mensalidade inválida");
        this.valorMensalidade = valorMensalidade;
    }

    public String getGraduacao() {
        return graduacaoDescricao;
    }

    public void setGraduacao(String graduacao) {
        this.graduacaoDescricao = graduacao;
    }

    public String getFirebaseUid() {
        return firebaseUid;
    }

    public void setFirebaseUid(String firebaseUid) {
        this.firebaseUid = firebaseUid;
    }

    public String getAsaasCustomerId() {
        return asaasCustomerId;
    }

    public void setAsaasCustomerId(String asaasCustomerId) {
        this.asaasCustomerId = asaasCustomerId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public TipoAluno getTipoAluno() {
        return tipoAluno;
    }

    public void setTipoAluno(TipoAluno tipoAluno) {
        if (tipoAluno == null) throw new IllegalArgumentException("Tipo de aluno inválido");
        this.tipoAluno = tipoAluno;
    }

    public Integer getDiaVencimento() {
        return diaVencimento;
    }

    public void setDiaVencimento(Integer diaVencimento) {
        if (diaVencimento == null || diaVencimento < 1 || diaVencimento > 28)
            throw new IllegalArgumentException("Dia de vencimento inválido");
        this.diaVencimento = diaVencimento;
    }

    public Integer getQuantidadeCheckins() {
        return quantidadeCheckins;
    }

    public void setQuantidadeCheckins(Integer quantidadeCheckins) {
        this.quantidadeCheckins = quantidadeCheckins;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public Academia getAcademia() {
        return academia;
    }

    public void setAcademia(Academia academia) {
        this.academia = academia;
    }

    public List<Modalidade> getModalidades() {
        return modalidades;
    }

    public void setModalidades(List<Modalidade> modalidades) {
        this.modalidades = modalidades;
    }

    public Graduacao getGraduacaoAtual() {
        return graduacao;
    }

    public void setGraduacaoAtual(Graduacao graduacaoAtual) {
        this.graduacao = graduacaoAtual;
    }

    public Graduacao getGraduacaoEntidade() {
        return graduacao;
    }

    public void setGraduacaoEntidade(Graduacao graduacao) {
        this.graduacao = graduacao;
    }

    public Plano getPlano() {
        return plano;
    }

    public void setPlano(Plano plano) {
        this.plano = plano;
    }



    @Override
    public void cadastrar() {}
    @Override
    public void atualizarDados() {}
    @Override
    public String exibirIdentificacao() { return ""; }





}
