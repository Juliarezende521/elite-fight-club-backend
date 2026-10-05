package Tic.Implementacao.service;

import Tic.Implementacao.model.Relatorio;
import Tic.Implementacao.repository.RelatorioRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class RelatorioService {

    private final RelatorioRepository repository;

    public RelatorioService(RelatorioRepository repository) {
        this.repository = repository;
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
