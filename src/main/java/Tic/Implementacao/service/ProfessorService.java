package Tic.Implementacao.service;

import Tic.Implementacao.model.Professor;
import Tic.Implementacao.repository.ProfessorRepository;
import Tic.Implementacao.security.PermissaoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProfessorService {

    private final ProfessorRepository repo;
    private final PermissaoService permissaoService;

    public ProfessorService(ProfessorRepository repo, PermissaoService permissaoService) {
        this.repo = repo;
        this.permissaoService = permissaoService;
    }

    public List<Professor> listar(){
        return repo.findAllByAcademiaId(permissaoService.academiaAtualId());
    }

    public Optional<Professor> exibirIdentificacao(Long id){
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
    public Professor atualizarDados(Long id, Professor alterado){
        if (repo.findByIdAndAcademiaId(id, permissaoService.academiaAtualId()).isPresent()){
            alterado.setAcademia(permissaoService.academiaAtual());
            alterado.setId(id);
            return repo.save(alterado);
        }
        return null;
    }

    @Transactional
    public Professor salva(Professor professor){
        professor.setAcademia(permissaoService.academiaAtual());
        return repo.save(professor);
    }
}
