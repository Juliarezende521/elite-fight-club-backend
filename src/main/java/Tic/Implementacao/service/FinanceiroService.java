package Tic.Implementacao.service;

import Tic.Implementacao.model.Financeiro;
import Tic.Implementacao.model.StatusPagamento;
import Tic.Implementacao.model.TipoModalidade;
import Tic.Implementacao.repository.FinanceiroRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class FinanceiroService {

    private final FinanceiroRepository repository;

    public FinanceiroService(FinanceiroRepository repository) {
        this.repository = repository;
    }

    public List<Financeiro> listarTodos(){
        List<Financeiro> financeiros = repository.findAll();
        financeiros.forEach(this::atualizarStatusPagamento);
        return financeiros;
    }

    public Optional<Financeiro> buscarPorId(Long id){
        Optional<Financeiro> financeiro = repository.findById(id);
        financeiro.ifPresent(this::atualizarStatusPagamento);
        return financeiro;
    }

    public List<Financeiro> listarPorAluno(Long alunoId) {
        List<Financeiro> financeiros = repository.findByAlunoId(alunoId);
        financeiros.forEach(this::atualizarStatusPagamento);
        return financeiros;
    }

    public List<Financeiro> listarPorModalidade(Long modalidadeId) {
        List<Financeiro> financeiros = repository.findByModalidadeId(modalidadeId);
        financeiros.forEach(this::atualizarStatusPagamento);
        return financeiros;
    }

    public List<Financeiro> listarPorTipoModalidade(String tipoModalidade) {
        List<Financeiro> financeiros = repository.findByModalidadeTipoModalidade(TipoModalidade.valueOf(tipoModalidade.toUpperCase()));
        financeiros.forEach(this::atualizarStatusPagamento);
        return financeiros;
    }

    public List<Financeiro> listarPagos() {
        return repository.findByDataPagamentoIsNotNull();
    }

    public List<Financeiro> listarVencidos() {
        List<Financeiro> financeiros = repository.findByDataPagamentoIsNullAndDataVencimentoBefore(LocalDate.now());
        financeiros.forEach(this::atualizarStatusPagamento);
        return financeiros;
    }

    public List<Financeiro> listarVencendoHoje() {
        List<Financeiro> financeiros = repository.findByDataPagamentoIsNullAndDataVencimento(LocalDate.now());
        financeiros.forEach(this::atualizarStatusPagamento);
        return financeiros;
    }

    @Transactional
    public boolean remove(Long id){
        if(repository.existsById(id)){
            repository.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional
    public Financeiro atualizarInformacoes(Long id, Financeiro alterado){
        Financeiro financeiro = repository.findById(id).orElse(null);
        if(financeiro == null) return null;

        financeiro.setDescricao(alterado.getDescricao());
        financeiro.setValor(alterado.getValor());
        financeiro.setDataVencimento(alterado.getDataVencimento());
        financeiro.setDataPagamento(alterado.getDataPagamento());
        financeiro.setAluno(alterado.getAluno());
        financeiro.setModalidade(alterado.getModalidade());
        atualizarStatusPagamento(financeiro);

        return repository.save(financeiro);
    }

    @Transactional
    public Financeiro salva(Financeiro financeiro){
        atualizarStatusPagamento(financeiro);
        return repository.save(financeiro);
    }

    @Transactional
    public Financeiro registrarPagamento(Long id) {
        Financeiro financeiro = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Financeiro não encontrado com ID: " + id));

        financeiro.registrarPagamento();
        return repository.save(financeiro);
    }

    public Double calcularTotalRecebido() {
        return repository.findByDataPagamentoIsNotNull()
                .stream()
                .mapToDouble(Financeiro::getValor)
                .sum();
    }

    public Double calcularTotalRecebidoPorTipo(String tipoModalidade) {
        return repository.findByModalidadeTipoModalidade(TipoModalidade.valueOf(tipoModalidade.toUpperCase()))
                .stream()
                .filter(Financeiro::estaPago)
                .mapToDouble(Financeiro::getValor)
                .sum();
    }

    public void atualizarStatusPagamento(Financeiro financeiro) {
        if (financeiro == null) return;

        if (financeiro.getDataPagamento() != null) {
            financeiro.setStatusPagamento(StatusPagamento.PAGO);
            return;
        }

        LocalDate hoje = LocalDate.now();
        LocalDate vencimento = financeiro.getDataVencimento();

        if (vencimento == null) {
            financeiro.setStatusPagamento(StatusPagamento.PENDENTE);
        } else if (vencimento.isBefore(hoje)) {
            financeiro.setStatusPagamento(StatusPagamento.PENDENTE);
        } else if (vencimento.isEqual(hoje)) {
            financeiro.setStatusPagamento(StatusPagamento.PENDENTE);
        } else {
            financeiro.setStatusPagamento(StatusPagamento.PENDENTE);
        }
    }
}
