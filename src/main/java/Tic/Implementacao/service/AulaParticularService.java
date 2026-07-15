package Tic.Implementacao.service;

import Tic.Implementacao.model.AulaParticular;
import Tic.Implementacao.repository.AulaParticularRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AulaParticularService {

    private final AulaParticularRepository repository;

    public AulaParticularService(AulaParticularRepository repository) {
        this.repository = repository;
    }

    public List<AulaParticular> listarTodas() {
        return repository.findAll();
    }

    public Optional<AulaParticular> buscarPorId(Long id) {
        return repository.findById(id);
    }

    @Transactional
    public AulaParticular salvar(AulaParticular aulaParticular) {
        return repository.save(aulaParticular);
    }

    @Transactional
    public AulaParticular atualizar(Long id, AulaParticular dadosNovos) {

        AulaParticular aula = repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Aula Particular não encontrada com ID: " + id));

        aula.setData(dadosNovos.getData());
        aula.setHorario(dadosNovos.getHorario());
        aula.setAluno(dadosNovos.getAluno());
        aula.setProfessor(dadosNovos.getProfessor());

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