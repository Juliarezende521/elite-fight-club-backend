package Tic.Implementacao.service;

import Tic.Implementacao.model.Usuario;
import Tic.Implementacao.repository.AcademiaRepository;
import Tic.Implementacao.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;
    private final AcademiaRepository academiaRepository;

    public UsuarioService(UsuarioRepository repository, AcademiaRepository academiaRepository) {
        this.repository = repository;
        this.academiaRepository = academiaRepository;
    }

    public List<Usuario> listarTodos() {
        return repository.findAll();
    }

    public Optional<Usuario> buscarPorId(Long id) {
        return repository.findById(id);
    }

    public Optional<Usuario> buscarPorEmail(String email) {
        return repository.findByEmail(email);
    }

    @Transactional
    public Usuario salvar(Usuario usuario) {
        if (repository.existsByEmail(usuario.getEmail())) {
            throw new RuntimeException("Já existe usuário cadastrado com este e-mail");
        }
        if (usuario.getAcademia() == null) {
            usuario.setAcademia(academiaRepository.findByNomeIgnoreCase("Elite Fight Club")
                    .orElseThrow(() -> new RuntimeException("Academia padrão não encontrada")));
        }
        usuario.setSenha(null);
        return repository.save(usuario);
    }

    @Transactional
    public Usuario atualizar(Long id, Usuario dadosNovos) {
        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com ID: " + id));

        usuario.setNome(dadosNovos.getNome());
        usuario.setEmail(dadosNovos.getEmail());
        usuario.setPerfil(dadosNovos.getPerfil());
        usuario.setAluno(dadosNovos.getAluno());
        usuario.setProfessor(dadosNovos.getProfessor());
        usuario.setSenha(null);

        return repository.save(usuario);
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
