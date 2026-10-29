package Tic.Implementacao.service;

import Tic.Implementacao.dto.professor.ProfessorRequest;
import Tic.Implementacao.dto.professor.ProfessorResponse;
import Tic.Implementacao.model.Professor;
import Tic.Implementacao.model.Usuario;
import Tic.Implementacao.repository.ProfessorRepository;
import Tic.Implementacao.security.PermissaoService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
public class ProfessorService {
    private final ProfessorRepository repo;
    private final PermissaoService permissoes;
    public ProfessorService(ProfessorRepository repo, PermissaoService permissoes) {
        this.repo = repo;
        this.permissoes = permissoes;
    }
    private void autorizar() {
        Usuario usuario;
        try {
            usuario = permissoes.usuarioAtual();
        } catch (RuntimeException ex) {
            throw new AuthenticationCredentialsNotFoundException("Autenticacao obrigatoria");
        }
        if (!Boolean.TRUE.equals(usuario.getAtivo())) throw new AccessDeniedException("Usuario inativo");
        permissoes.exigirAdmin();
    }
    @Transactional(readOnly = true)
    public List<ProfessorResponse> listar() {
        autorizar();
        return repo.findAllByAcademiaId(permissoes.academiaAtualId()).stream().map(ProfessorResponse::from).toList();
    }
    @Transactional(readOnly = true)
    public ProfessorResponse exibirIdentificacao(Long id) {
        autorizar();
        return ProfessorResponse.from(buscar(id));
    }
    @Transactional
    public void remove(Long id) {
        autorizar();
        repo.delete(buscar(id));
    }
    @Transactional
    public ProfessorResponse atualizarDados(Long id, ProfessorRequest request) {
        autorizar();
        Professor professor = buscar(id);
        preencher(professor, request);
        return ProfessorResponse.from(repo.save(professor));
    }
    @Transactional
    public ProfessorResponse salva(ProfessorRequest request) {
        autorizar();
        Professor professor = new Professor();
        professor.setAcademia(permissoes.academiaAtual());
        preencher(professor, request);
        return ProfessorResponse.from(repo.save(professor));
    }
    private Professor buscar(Long id) {
        return repo.findByIdAndAcademiaId(id, permissoes.academiaAtualId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Professor nao encontrado"));
    }
    private void preencher(Professor professor, ProfessorRequest request) {
        professor.setNome(request.nome());
        professor.setCpf(request.cpf());
        professor.setEspecialidade(request.especialidade());
        professor.setContato(request.contato());
    }
}
