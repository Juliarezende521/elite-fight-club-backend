package Tic.Implementacao.service;

import Tic.Implementacao.model.Campeonato;
import Tic.Implementacao.repository.CampeonatoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CampeonatoService {

    private CampeonatoRepository repo;

    public CampeonatoService(CampeonatoRepository repo) {
        this.repo = repo;
    }

    public List<Campeonato> listar() {
        return repo.findAll();
    }

    public Optional<Campeonato> exibirInformacoes(Long id) {
        return repo.findById(id);
    }

    @Transactional
    public boolean remove(Long id) {
        if (repo.existsById(id)) {
            repo.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional
    public Campeonato atualizarInformacoes(Long id, Campeonato alterado) {
        if (repo.existsById(id)) {
            alterado.setId(id);
            return repo.save(alterado);
        }
        return null;
    }

    @Transactional
    public Campeonato salva(Campeonato campeonato) {
        return repo.save(campeonato);
    }
}