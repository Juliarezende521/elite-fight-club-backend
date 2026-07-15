package Tic.Implementacao.service;

import Tic.Implementacao.model.Evolucao;
import Tic.Implementacao.model.Financeiro;
import Tic.Implementacao.model.Relatorio;
import Tic.Implementacao.model.TipoModalidade;
import Tic.Implementacao.repository.EvolucaoRepository;
import Tic.Implementacao.repository.FinanceiroRepository;
import Tic.Implementacao.repository.RelatorioRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class RelatorioService {

    private final RelatorioRepository repository;
    private final FinanceiroRepository financeiroRepository;
    private final EvolucaoRepository evolucaoRepository;

    public RelatorioService(RelatorioRepository repository, FinanceiroRepository financeiroRepository,
                            EvolucaoRepository evolucaoRepository) {
        this.repository = repository;
        this.financeiroRepository = financeiroRepository;
        this.evolucaoRepository = evolucaoRepository;
    }

    public List<Relatorio> listarTodos() {
        return repository.findAll();
    }

    public List<Relatorio> listarPorTipo(String tipoRelatorio) {
        return repository.findByTipoRelatorioIgnoreCase(tipoRelatorio);
    }

    public Optional<Relatorio> buscarPorId(Long id) {
        return repository.findById(id);
    }

    public Relatorio salvar(Relatorio relatorio) {

        if (relatorio.getDataGeracao() == null) {
            relatorio.setDataGeracao(LocalDate.now());
        }

        return repository.save(relatorio);
    }

    public Relatorio gerarRelatorioFinanceiro(String tipoModalidade) {
        List<Financeiro> financeiros;
        String tipo = "FINANCEIRO_CONSOLIDADO";

        if (tipoModalidade == null || tipoModalidade.isBlank() || "CONSOLIDADO".equalsIgnoreCase(tipoModalidade)) {
            financeiros = financeiroRepository.findAll();
        } else {
            financeiros = financeiroRepository.findByModalidadeTipoModalidade(TipoModalidade.valueOf(tipoModalidade.toUpperCase()));
            tipo = "FINANCEIRO_" + tipoModalidade.toUpperCase();
        }

        long totalPagos = financeiros.stream().filter(Financeiro::estaPago).count();
        long totalVencidos = financeiros.stream()
                .filter(financeiro -> financeiro.getDataPagamento() == null)
                .filter(financeiro -> financeiro.getDataVencimento() != null)
                .filter(financeiro -> financeiro.getDataVencimento().isBefore(LocalDate.now()))
                .count();
        double valorRecebido = financeiros.stream()
                .filter(Financeiro::estaPago)
                .mapToDouble(Financeiro::getValor)
                .sum();
        double valorPendente = financeiros.stream()
                .filter(financeiro -> !financeiro.estaPago())
                .mapToDouble(Financeiro::getValor)
                .sum();

        String conteudo = "Total de mensalidades: " + financeiros.size()
                + " | Pagas: " + totalPagos
                + " | Vencidas: " + totalVencidos
                + " | Valor recebido: " + valorRecebido
                + " | Valor pendente: " + valorPendente;

        Relatorio relatorio = new Relatorio(tipo, "ATUAL", null, null, LocalDate.now(), conteudo);
        return repository.save(relatorio);
    }

    public Relatorio gerarRelatorioEvolucaoAluno(Long alunoId) {
        List<Evolucao> evolucoes = evolucaoRepository.findByAlunoIdOrderByDataAvaliacaoAsc(alunoId);

        StringBuilder conteudo = new StringBuilder();
        for (Evolucao evolucao : evolucoes) {
            String graduacao = evolucao.getGraduacao() != null ? evolucao.getGraduacao().getNome() : "Sem graduação vinculada";
            conteudo.append(evolucao.getDataAvaliacao())
                    .append(" | Peso: ")
                    .append(evolucao.getPesoAtual())
                    .append(" | Graduação: ")
                    .append(graduacao)
                    .append(" | Observação: ")
                    .append(evolucao.getObservacao() == null ? "" : evolucao.getObservacao())
                    .append("\n");
        }

        Relatorio relatorio = new Relatorio(
                "EVOLUCAO_ALUNO",
                "LINHA_DO_TEMPO",
                null,
                null,
                LocalDate.now(),
                conteudo.toString()
        );

        return repository.save(relatorio);
    }

    public Relatorio atualizar(Long id, Relatorio dadosNovos) {

        Relatorio relatorio = repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Relatório não encontrado com ID: " + id));

        relatorio.setTipoRelatorio(dadosNovos.getTipoRelatorio());
        relatorio.setPeriodo(dadosNovos.getPeriodo());
        relatorio.setDataInicio(dadosNovos.getDataInicio());
        relatorio.setDataFim(dadosNovos.getDataFim());
        relatorio.setDataGeracao(dadosNovos.getDataGeracao());
        relatorio.setConteudo(dadosNovos.getConteudo());

        if (relatorio.getDataGeracao() == null) {
            relatorio.setDataGeracao(LocalDate.now());
        }

        return repository.save(relatorio);
    }

    public boolean remover(Long id) {

        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }

        return false;
    }
}
