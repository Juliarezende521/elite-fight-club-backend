package Tic.Implementacao.service;

import Tic.Implementacao.dto.agenda.PlanoRequest;
import Tic.Implementacao.model.Plano;
import Tic.Implementacao.repository.PlanoRepository;
import Tic.Implementacao.security.PermissaoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PlanoService {

    private final PlanoRepository repository;
    private final PermissaoService permissaoService;

    public PlanoService(PlanoRepository repository, PermissaoService permissaoService) {
        this.repository = repository;
        this.permissaoService = permissaoService;
    }

    public List<Plano> listarTodos() {
        return repository.findAllByAcademiaId(permissaoService.academiaAtualId());
    }

    @Transactional
    public Plano salvar(PlanoRequest request) {
        permissaoService.exigirAdminOuProfessor();
        Plano plano = new Plano();
        aplicarDados(plano, request);
        plano.setAcademia(permissaoService.academiaAtual());
        return repository.save(plano);
    }

    @Transactional
    public Plano atualizar(Long id, PlanoRequest request) {
        permissaoService.exigirAdminOuProfessor();
        Plano plano = repository.findByIdAndAcademiaId(id, permissaoService.academiaAtualId())
                .orElseThrow(() -> new RuntimeException("Plano não encontrado com ID: " + id));
        aplicarDados(plano, request);
        return repository.save(plano);
    }

    @Transactional
    public Plano alterarStatus(Long id, boolean ativo) {
        permissaoService.exigirAdminOuProfessor();
        Plano plano = repository.findByIdAndAcademiaId(id, permissaoService.academiaAtualId())
                .orElseThrow(() -> new RuntimeException("Plano não encontrado com ID: " + id));
        plano.setAtivo(ativo);
        return repository.save(plano);
    }

    private void aplicarDados(Plano plano, PlanoRequest request) {
        plano.setTipo(request.getTipo());
        plano.setQuantidadeAulasSemana(request.getQuantidadeAulasSemana());
        plano.setValor(request.getValor());
        plano.setAtivo(request.getAtivo() == null || request.getAtivo());
    }
}
