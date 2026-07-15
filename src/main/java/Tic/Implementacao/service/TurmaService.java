package Tic.Implementacao.service;

import Tic.Implementacao.model.Turma;
import Tic.Implementacao.repository.TurmaRepository;
import Tic.Implementacao.security.PermissaoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TurmaService {

    private final TurmaRepository repo;
    private final PermissaoService permissaoService;

    public TurmaService(TurmaRepository repo, PermissaoService permissaoService){
        this.repo = repo;
        this.permissaoService = permissaoService;
    }

    public List<Turma> listar(){
        return repo.findAllByAcademiaId(permissaoService.academiaAtualId());
    }

    public Optional<Turma> buscarPorId(Long id){
        return repo.findByIdAndAcademiaId(id, permissaoService.academiaAtualId());
    }

    @Transactional
    public boolean remove(Long id){
        if(repo.findByIdAndAcademiaId(id, permissaoService.academiaAtualId()).isPresent()){
            repo.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional
    public Turma atualizarDados(Long id, Turma alterado){
        if(repo.findByIdAndAcademiaId(id, permissaoService.academiaAtualId()).isPresent()){
            alterado.setAcademia(permissaoService.academiaAtual());
            alterado.setId(id);
            return repo.save(alterado);
        }
        return null;
    }

    @Transactional
    public Turma salva(Turma turma){
        turma.setAcademia(permissaoService.academiaAtual());
        return repo.save(turma);
    }
}
