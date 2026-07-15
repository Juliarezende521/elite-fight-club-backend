package Tic.Implementacao.service;

import Tic.Implementacao.model.DashboardPreferencia;
import Tic.Implementacao.model.Usuario;
import Tic.Implementacao.repository.DashboardPreferenciaRepository;
import Tic.Implementacao.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class DashboardPreferenciaService {

    private final DashboardPreferenciaRepository repository;
    private final UsuarioRepository usuarioRepository;

    public DashboardPreferenciaService(DashboardPreferenciaRepository repository, UsuarioRepository usuarioRepository) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<DashboardPreferencia> listarTodas() {
        return repository.findAll();
    }

    public Optional<DashboardPreferencia> buscarPorId(Long id) {
        return repository.findById(id);
    }

    public Optional<DashboardPreferencia> buscarPorUsuario(Long usuarioId) {
        return repository.findByUsuarioId(usuarioId);
    }

    @Transactional
    public DashboardPreferencia salvar(DashboardPreferencia preferencia) {
        Long usuarioId = preferencia.getUsuario() != null ? preferencia.getUsuario().getId() : null;
        if (usuarioId != null && repository.existsByUsuarioId(usuarioId)) {
            throw new RuntimeException("Usuário já possui preferências de dashboard");
        }
        return repository.save(preferencia);
    }

    @Transactional
    public DashboardPreferencia atualizar(Long id, DashboardPreferencia dadosNovos) {
        DashboardPreferencia preferencia = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Preferência não encontrada com ID: " + id));

        preferencia.setUsuario(dadosNovos.getUsuario());
        preferencia.setCardsVisiveis(dadosNovos.getCardsVisiveis());
        preferencia.setOrdemCards(dadosNovos.getOrdemCards());
        preferencia.setCardPrincipal(dadosNovos.getCardPrincipal());
        preferencia.setTema(dadosNovos.getTema());
        preferencia.setLayoutJson(dadosNovos.getLayoutJson());

        return repository.save(preferencia);
    }

    @Transactional
    public DashboardPreferencia salvarOuAtualizarPorUsuario(Long usuarioId, DashboardPreferencia dadosNovos) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com ID: " + usuarioId));
        Optional<DashboardPreferencia> preferenciaExistente = repository.findByUsuarioId(usuarioId);

        if (preferenciaExistente.isEmpty()) {
            dadosNovos.setUsuario(usuario);
            return repository.save(dadosNovos);
        }

        DashboardPreferencia preferencia = preferenciaExistente.get();
        preferencia.setCardsVisiveis(dadosNovos.getCardsVisiveis());
        preferencia.setOrdemCards(dadosNovos.getOrdemCards());
        preferencia.setCardPrincipal(dadosNovos.getCardPrincipal());
        preferencia.setTema(dadosNovos.getTema());
        preferencia.setLayoutJson(dadosNovos.getLayoutJson());

        return repository.save(preferencia);
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
