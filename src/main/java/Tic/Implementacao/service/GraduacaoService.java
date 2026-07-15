package Tic.Implementacao.service;

import Tic.Implementacao.dto.graduacao.GraduacaoRequest;
import Tic.Implementacao.dto.graduacao.GraduacaoResponse;
import Tic.Implementacao.dto.graduacao.ItemReordenacaoGraduacaoRequest;
import Tic.Implementacao.dto.graduacao.ReordenarGraduacoesRequest;
import Tic.Implementacao.model.Academia;
import Tic.Implementacao.model.Graduacao;
import Tic.Implementacao.repository.AlunoRepository;
import Tic.Implementacao.repository.GraduacaoRepository;
import Tic.Implementacao.security.PermissaoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class GraduacaoService {

    private final GraduacaoRepository repository;
    private final AlunoRepository alunoRepository;
    private final PermissaoService permissaoService;

    public GraduacaoService(GraduacaoRepository repository, AlunoRepository alunoRepository, PermissaoService permissaoService) {
        this.repository = repository;
        this.alunoRepository = alunoRepository;
        this.permissaoService = permissaoService;
    }

    @Transactional(readOnly = true)
    public List<GraduacaoResponse> listarTodas() {
        return repository.findByAcademiaIdOrderByOrdemAsc(permissaoService.academiaAtualId())
                .stream()
                .map(GraduacaoResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<GraduacaoResponse> listarAtivas() {
        return repository.findByAcademiaIdAndAtivaTrueOrderByOrdemAsc(permissaoService.academiaAtualId())
                .stream()
                .map(GraduacaoResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public GraduacaoResponse buscarPorId(Long id) {
        return repository.findByIdAndAcademiaId(id, permissaoService.academiaAtualId())
                .map(GraduacaoResponse::from)
                .orElseThrow(() -> new RuntimeException("Graduação não encontrada com ID: " + id));
    }

    @Transactional
    public GraduacaoResponse salvar(GraduacaoRequest request) {
        permissaoService.exigirAdminOuProfessor();
        Academia academia = permissaoService.academiaAtual();
        Graduacao graduacao = preencher(new Graduacao(), request);
        graduacao.setAcademia(academia);
        validarDuplicidadesAoCriar(academia.getId(), graduacao);
        return GraduacaoResponse.from(repository.save(graduacao));
    }

    @Transactional
    public GraduacaoResponse atualizar(Long id, GraduacaoRequest dadosNovos) {
        permissaoService.exigirAdminOuProfessor();
        Long academiaId = permissaoService.academiaAtualId();
        Graduacao graduacao = repository.findByIdAndAcademiaId(id, academiaId)
                .orElseThrow(() -> new RuntimeException("Graduação não encontrada com ID: " + id));
        preencher(graduacao, dadosNovos);
        validarDuplicidadesAoAtualizar(academiaId, graduacao);
        return GraduacaoResponse.from(repository.save(graduacao));
    }

    @Transactional
    public GraduacaoResponse alterarStatus(Long id, boolean ativa) {
        permissaoService.exigirAdminOuProfessor();
        Graduacao graduacao = repository.findByIdAndAcademiaId(id, permissaoService.academiaAtualId())
                .orElseThrow(() -> new RuntimeException("Graduação não encontrada com ID: " + id));

        if (Boolean.TRUE.equals(graduacao.getGraduacaoBase()) && !ativa) {
            throw new RuntimeException("Graduação base não pode ser desativada");
        }

        graduacao.setAtiva(ativa);
        return GraduacaoResponse.from(repository.save(graduacao));
    }

    @Transactional
    public boolean remover(Long id) {
        permissaoService.exigirAdminOuProfessor();
        Graduacao graduacao = repository.findByIdAndAcademiaId(id, permissaoService.academiaAtualId())
                .orElseThrow(() -> new RuntimeException("Graduação não encontrada com ID: " + id));

        if (Boolean.TRUE.equals(graduacao.getGraduacaoBase())) {
            throw new RuntimeException("Graduação base não pode ser excluída");
        }

        if (alunoRepository.existsByAcademiaIdAndGraduacaoId(permissaoService.academiaAtualId(), id)) {
            throw new RuntimeException("Graduação vinculada a aluno não pode ser excluída");
        }

        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional
    public List<GraduacaoResponse> reordenar(ReordenarGraduacoesRequest request) {
        permissaoService.exigirAdminOuProfessor();
        Long academiaId = permissaoService.academiaAtualId();
        List<ItemReordenacaoGraduacaoRequest> itens = request.getGraduacoes();

        Set<Long> ids = itens.stream().map(ItemReordenacaoGraduacaoRequest::getId).collect(Collectors.toSet());
        if (ids.size() != itens.size()) {
            throw new RuntimeException("Não é permitido repetir graduação na reordenação");
        }
        Set<Integer> ordens = itens.stream().map(ItemReordenacaoGraduacaoRequest::getOrdem).collect(Collectors.toSet());
        if (ordens.size() != itens.size()) {
            throw new RuntimeException("Não é permitido repetir ordem na reordenação");
        }

        List<Graduacao> graduacoes = repository.findByIdInAndAcademiaId(ids, academiaId);
        if (graduacoes.size() != ids.size()) {
            throw new RuntimeException("Todas as graduações devem existir na academia atual");
        }

        Map<Long, ItemReordenacaoGraduacaoRequest> porId = itens.stream()
                .collect(Collectors.toMap(ItemReordenacaoGraduacaoRequest::getId, Function.identity()));

        for (Graduacao graduacao : graduacoes) {
            repository.atualizarOrdem(graduacao.getId(), academiaId, -Math.toIntExact(graduacao.getId()));
        }
        for (Graduacao graduacao : graduacoes) {
            repository.atualizarOrdem(graduacao.getId(), academiaId, porId.get(graduacao.getId()).getOrdem());
        }

        return repository.findByAcademiaIdOrderByOrdemAsc(academiaId)
                .stream()
                .map(GraduacaoResponse::from)
                .toList();
    }

    public Graduacao buscarGraduacaoBase() {
        return repository.findByGraduacaoBaseTrue()
                .orElseThrow(() -> new RuntimeException("Graduação base não cadastrada"));
    }

    public Graduacao buscarGraduacaoBase(Long academiaId) {
        return repository.findByAcademiaIdAndGraduacaoBaseTrue(academiaId)
                .orElseThrow(() -> new RuntimeException("Graduação base não cadastrada para a academia"));
    }

    @Transactional(readOnly = true)
    public Optional<Graduacao> buscarProximaGraduacao(Long academiaId, Graduacao graduacaoAtual) {
        if (graduacaoAtual == null || graduacaoAtual.getOrdem() == null) {
            return Optional.empty();
        }
        return repository.findFirstByAcademiaIdAndAtivaTrueAndOrdemGreaterThanOrderByOrdemAsc(
                academiaId, graduacaoAtual.getOrdem());
    }

    private Graduacao preencher(Graduacao graduacao, GraduacaoRequest request) {
        graduacao.setNome(request.getNome());
        graduacao.setCorHex(request.getCorHex());
        graduacao.setOrdem(request.getOrdem());
        graduacao.setCheckinsNecessarios(request.getCheckinsNecessarios() == null ? 0 : request.getCheckinsNecessarios());
        graduacao.setTempoMinimoMeses(request.getTempoMinimoMeses() == null ? 0 : request.getTempoMinimoMeses());
        graduacao.setValorPadraoGraduacao(request.getValorPadraoGraduacao());
        graduacao.setDescricao(request.getDescricao());
        graduacao.setGraduacaoBase(Boolean.TRUE.equals(request.getGraduacaoBase()));
        graduacao.setAtiva(request.getAtiva() == null || request.getAtiva());
        return graduacao;
    }

    private void validarDuplicidadesAoCriar(Long academiaId, Graduacao graduacao) {
        if (repository.existsByAcademiaIdAndNomeIgnoreCase(academiaId, graduacao.getNome())) {
            throw new RuntimeException("Já existe uma graduação com esse nome na academia");
        }
        if (repository.existsByAcademiaIdAndOrdem(academiaId, graduacao.getOrdem())) {
            throw new RuntimeException("Já existe uma graduação com essa ordem na academia");
        }
        if (Boolean.TRUE.equals(graduacao.getGraduacaoBase())
                && Boolean.TRUE.equals(graduacao.getAtiva())
                && repository.existsByAcademiaIdAndGraduacaoBaseTrueAndAtivaTrue(academiaId)) {
            throw new RuntimeException("Já existe uma graduação base ativa cadastrada");
        }
    }

    private void validarDuplicidadesAoAtualizar(Long academiaId, Graduacao graduacao) {
        if (repository.existsByAcademiaIdAndNomeIgnoreCaseAndIdNot(academiaId, graduacao.getNome(), graduacao.getId())) {
            throw new RuntimeException("Já existe uma graduação com esse nome na academia");
        }
        if (repository.existsByAcademiaIdAndOrdemAndIdNot(academiaId, graduacao.getOrdem(), graduacao.getId())) {
            throw new RuntimeException("Já existe uma graduação com essa ordem na academia");
        }
        if (Boolean.TRUE.equals(graduacao.getGraduacaoBase())
                && Boolean.TRUE.equals(graduacao.getAtiva())
                && repository.existsByAcademiaIdAndGraduacaoBaseTrueAndAtivaTrueAndIdNot(academiaId, graduacao.getId())) {
            throw new RuntimeException("Já existe uma graduação base ativa cadastrada");
        }
    }
}
