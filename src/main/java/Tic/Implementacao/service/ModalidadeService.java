package Tic.Implementacao.service;

import Tic.Implementacao.model.Modalidade;
import Tic.Implementacao.model.TipoModalidade;
import Tic.Implementacao.repository.ModalidadeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ModalidadeService {

    private final ModalidadeRepository repository;

    public ModalidadeService(ModalidadeRepository repository) {
        this.repository = repository;
    }

    public List<Modalidade> listarTodas() {
        return repository.findAll();
    }

    public List<Modalidade> listarAtivas() {
        return repository.findByAtivaTrue();
    }

    public List<Modalidade> listarPorTipo(String tipoModalidade) {
        return repository.findByTipoModalidade(TipoModalidade.valueOf(tipoModalidade.toUpperCase()));
    }

    public Optional<Modalidade> buscarPorId(Long id) {
        return repository.findById(id);
    }

    @Transactional
    public Modalidade salvar(Modalidade modalidade) {
        return repository.save(modalidade);
    }

    @Transactional
    public Modalidade atualizar(Long id, Modalidade dadosNovos) {
        Modalidade modalidade = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Modalidade não encontrada com ID: " + id));

        modalidade.setNome(dadosNovos.getNome());
        modalidade.setDescricao(dadosNovos.getDescricao());
        modalidade.setAtiva(dadosNovos.isAtiva());
        modalidade.setTipoModalidade(dadosNovos.getTipoModalidade());
        modalidade.setValorMensalidade(dadosNovos.getValorMensalidade());

        return repository.save(modalidade);
    }

    @Transactional
    public boolean remover(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }
}
