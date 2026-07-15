package Tic.Implementacao.service;

import Tic.Implementacao.model.Evolucao;
import Tic.Implementacao.repository.EvolucaoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EvolucaoService {

    private final EvolucaoRepository repository;

    public EvolucaoService(EvolucaoRepository repository) {
        this.repository = repository;
    }

    public List<Evolucao> listarTodas() {
        return repository.findAll();
    }

    public List<Evolucao> listarPorAluno(Long alunoId) {
        return repository.findByAlunoId(alunoId);
    }

    public List<Evolucao> listarLinhaDoTempoPorAluno(Long alunoId) {
        return repository.findByAlunoIdOrderByDataAvaliacaoAsc(alunoId);
    }

    public Optional<Evolucao> buscarPorId(Long id) {
        return repository.findById(id);
    }

    public Evolucao salvar(Evolucao evolucao) {
        return repository.save(evolucao);
    }

    public Evolucao atualizar(Long id, Evolucao dadosNovos) {

        Evolucao evolucao = repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Evolução não encontrada com ID: " + id));

        evolucao.setPesoAtual(dadosNovos.getPesoAtual());
        evolucao.setBioimpedancia(dadosNovos.getBioimpedancia());
        evolucao.setDataAvaliacao(dadosNovos.getDataAvaliacao());
        evolucao.setObservacao(dadosNovos.getObservacao());
        evolucao.setAluno(dadosNovos.getAluno());
        evolucao.setGraduacao(dadosNovos.getGraduacao());

        return repository.save(evolucao);
    }

    public boolean remover(Long id) {

        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }

        return false;
    }
}
