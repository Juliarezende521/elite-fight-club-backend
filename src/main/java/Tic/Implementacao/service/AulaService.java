package Tic.Implementacao.service;

import Tic.Implementacao.model.Aula;
import Tic.Implementacao.repository.AulaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AulaService {

    private final AulaRepository repository;

    public AulaService(AulaRepository repository) {
        this.repository = repository;
    }

    public List<Aula> listarTodas() {
        return repository.findAll();
    }

    public Optional<Aula> buscarPorId(Long id) {
        return repository.findById(id);
    }

    @Transactional
    public Aula salvar(Aula aula) {
        return repository.save(aula);
    }

    @Transactional
    public Aula atualizar(Long id, Aula dadosNovos) {

        Aula aula = repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Aula não encontrada com ID: " + id));

        aula.setData(dadosNovos.getData());
        aula.setHorarioInicio(dadosNovos.getHorarioInicio());
        aula.setDuracaoMinutos(dadosNovos.getDuracaoMinutos());
        aula.setPresente(dadosNovos.isPresente());
        aula.setObservacao(dadosNovos.getObservacao());
        aula.setProfessor(dadosNovos.getProfessor());
        aula.setModalidade(dadosNovos.getModalidade());

        return repository.save(aula);
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