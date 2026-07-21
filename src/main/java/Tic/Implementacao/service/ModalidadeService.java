package Tic.Implementacao.service;

import Tic.Implementacao.model.Modalidade;
import Tic.Implementacao.model.TipoModalidade;
import Tic.Implementacao.repository.ModalidadeRepository;
import Tic.Implementacao.security.PermissaoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ModalidadeService {

    private final ModalidadeRepository repository;
    private final PermissaoService permissaoService;

    public ModalidadeService(ModalidadeRepository repository, PermissaoService permissaoService) {
        this.repository = repository;
        this.permissaoService = permissaoService;
    }

    public List<Modalidade> listarTodas() {
        return repository.findByAcademiaId(permissaoService.academiaAtualId());
    }

    public List<Modalidade> listarAtivas() {
        return repository.findByAcademiaIdAndAtivaTrue(permissaoService.academiaAtualId());
    }

    public List<Modalidade> listarPorTipo(String tipoModalidade) {
        return repository.findByAcademiaIdAndTipoModalidade(
                permissaoService.academiaAtualId(),
                TipoModalidade.valueOf(tipoModalidade.toUpperCase()));
    }

    public Optional<Modalidade> buscarPorId(Long id) {
        return repository.findById(id)
                .filter(modalidade -> modalidade.getAcademia() != null
                        && modalidade.getAcademia().getId().equals(permissaoService.academiaAtualId()));
    }

    @Transactional
    public Modalidade salvar(Modalidade modalidade) {
        permissaoService.exigirAdminOuProfessor();
        modalidade.setAcademia(permissaoService.academiaAtual());
        return repository.save(modalidade);
    }

    @Transactional
    public Modalidade atualizar(Long id, Modalidade dadosNovos) {
        permissaoService.exigirAdminOuProfessor();
        Modalidade modalidade = buscarPorId(id)
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
        permissaoService.exigirAdminOuProfessor();
        if (buscarPorId(id).isPresent()) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }
}
