package Tic.Implementacao.service;

import Tic.Implementacao.model.*;
import Tic.Implementacao.dto.aluno.AtualizarAlunoRequest;
import Tic.Implementacao.dto.aluno.CriarAlunoRequest;
import Tic.Implementacao.exception.NegocioException;
import Tic.Implementacao.repository.AlunoRepository;
import Tic.Implementacao.repository.ModalidadeRepository;
import Tic.Implementacao.repository.PlanoRepository;
import Tic.Implementacao.repository.UsuarioRepository;
import Tic.Implementacao.security.PermissaoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class AlunoService {

    private final AlunoRepository repo;
    private final UsuarioRepository usuarioRepository;
    private final PlanoRepository planoRepository;
    private final ModalidadeRepository modalidadeRepository;
    private final FirebaseAuthService firebaseAuthService;
    private final GraduacaoService graduacaoService;
    private final GraduacaoAlunoService graduacaoAlunoService;
    private final FaturaService faturaService;
    private final PermissaoService permissaoService;
    private final TransactionTemplate transactionTemplate;

    public AlunoService(AlunoRepository repo, UsuarioRepository usuarioRepository, PlanoRepository planoRepository,
                        ModalidadeRepository modalidadeRepository, FirebaseAuthService firebaseAuthService,
                        GraduacaoService graduacaoService, GraduacaoAlunoService graduacaoAlunoService,
                        FaturaService faturaService, PermissaoService permissaoService,
                        PlatformTransactionManager transactionManager){
        this.repo = repo;
        this.usuarioRepository = usuarioRepository;
        this.planoRepository = planoRepository;
        this.modalidadeRepository = modalidadeRepository;
        this.firebaseAuthService = firebaseAuthService;
        this.graduacaoService = graduacaoService;
        this.graduacaoAlunoService = graduacaoAlunoService;
        this.faturaService = faturaService;
        this.permissaoService = permissaoService;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    /*
    @Override
    public void cadastrar() {}
    @Override
    public void atualizarDados() {}
    @Override
    public String exibirIdentificacao() { return ""; }
    */

    public List<Aluno> listar(){
        return repo.findAllByAcademiaId(permissaoService.academiaAtualId());
    }

    public Optional<Aluno> exibirIdentificacao(Long id){
        return repo.findByIdAndAcademiaId(id, permissaoService.academiaAtualId());
    }

    public List<Aluno> listarPorModalidade(Long modalidadeId) {
        return repo.findDistinctByModalidadesIdAndAcademiaId(modalidadeId, permissaoService.academiaAtualId());
    }

    public List<Aluno> listarPorTipoModalidade(String tipoModalidade) {
        return repo.findDistinctByModalidadesTipoModalidadeAndAcademiaId(
                TipoModalidade.valueOf(tipoModalidade.toUpperCase()), permissaoService.academiaAtualId());
    }

    @Transactional
    public boolean remove(Long id){
        permissaoService.exigirAdmin();
        Long academiaId = permissaoService.academiaAtualId();
        Optional<Aluno> alunoOptional = repo.findByIdAndAcademiaId(id, academiaId);
        if(alunoOptional.isPresent()){
            Aluno aluno = alunoOptional.get();
            aluno.setAtivo(false);
            repo.save(aluno);
            usuarioRepository.findByAlunoIdAndAcademiaId(aluno.getId(), academiaId)
                    .ifPresent(usuario -> {
                        usuario.setAtivo(false);
                        usuarioRepository.save(usuario);
                    });
            return true;
        }
        return false;
    }

    @Transactional
    public Aluno atualizarDados(Long id, Aluno alterado){
        throw new NegocioException("Endpoint legado de atualização de aluno desativado. Use PUT /api/alunos/{id}.");
    }

    @Transactional
    public Aluno salva(Aluno aluno){
        throw new NegocioException("Endpoint legado de cadastro de aluno desativado. Use POST /api/alunos.");
    }

    public Aluno criarAluno(CriarAlunoRequest request) {
        permissaoService.exigirAdminOuProfessor();
        Academia academia = permissaoService.academiaAtual();

        if (repo.existsByEmailAndAcademiaId(request.getEmail(), academia.getId())
                || usuarioRepository.existsByEmailAndAcademiaId(request.getEmail(), academia.getId())) {
            throw new RuntimeException("E-mail já cadastrado");
        }

        String firebaseUid = firebaseAuthService.criarUsuario(request.getNome(), request.getEmail(), request.getSenhaInicial());

        try {
            return transactionTemplate.execute(status -> salvarAlunoComUsuarioEFatura(request, firebaseUid, academia));
        } catch (RuntimeException e) {
            firebaseAuthService.excluirUsuario(firebaseUid);
            throw e;
        }
    }

    protected Aluno salvarAlunoComUsuarioEFatura(CriarAlunoRequest request, String firebaseUid, Academia academia) {
        Graduacao graduacaoBase = graduacaoService.buscarGraduacaoBase(academia.getId());

        Aluno aluno = new Aluno();
        aluno.setNome(request.getNome());
        aluno.setEmail(request.getEmail());
        aluno.setFirebaseUid(firebaseUid);
        aluno.setTipoAluno(request.getTipoAluno());
        aluno.setValorMensalidade(request.getValorMensalidade());
        aluno.setDiaVencimento(request.getDiaVencimento());
        aluno.setQuantidadeCheckins(0);
        aluno.setAtivo(true);
        aluno.setGraduacaoEntidade(graduacaoBase);
        aluno.setGraduacao(graduacaoBase.getNome());
        aluno.setAcademia(academia);
        if (request.getPlanoId() != null) {
            Plano plano = planoRepository.findByIdAndAcademiaId(request.getPlanoId(), academia.getId())
                    .orElseThrow(() -> new RuntimeException("Plano não encontrado com ID: " + request.getPlanoId()));
            aluno.setPlano(plano);
        }
        aluno.setModalidades(buscarModalidadesContratadas(request.getModalidadeIds(), academia, aluno.getPlano()));

        aluno = repo.save(aluno);

        Usuario usuario = new Usuario();
        usuario.setNome(request.getNome());
        usuario.setEmail(request.getEmail());
        usuario.setSenha(null);
        usuario.setFirebaseUid(firebaseUid);
        usuario.setPerfil(PerfilUsuario.ALUNO);
        usuario.setAtivo(true);
        usuario.setAluno(aluno);
        usuario.setAcademia(academia);
        usuarioRepository.save(usuario);

        graduacaoAlunoService.registrarHistoricoInicial(aluno, permissaoService.usuarioAtual());

        faturaService.gerarFaturaMensal(aluno.getId(), YearMonth.now());

        return aluno;
    }

    @Transactional
    public Aluno atualizarModalidades(Long alunoId, List<Long> modalidadeIds) {
        permissaoService.exigirAdminOuProfessor();
        Academia academia = permissaoService.academiaAtual();
        Aluno aluno = repo.findByIdAndAcademiaId(alunoId, academia.getId())
                .orElseThrow(() -> new RuntimeException("Aluno não encontrado com ID: " + alunoId));

        aluno.setModalidades(buscarModalidadesContratadas(modalidadeIds, academia, aluno.getPlano()));
        return repo.save(aluno);
    }

    @Transactional
    public Aluno atualizarCadastro(Long alunoId, AtualizarAlunoRequest request) {
        permissaoService.exigirAdminOuProfessor();
        Academia academia = permissaoService.academiaAtual();
        Aluno aluno = repo.findByIdAndAcademiaId(alunoId, academia.getId())
                .orElseThrow(() -> new RuntimeException("Aluno não encontrado com ID: " + alunoId));

        String emailNormalizado = request.getEmail().trim();
        if (!emailNormalizado.equalsIgnoreCase(aluno.getEmail())) {
            repo.findByEmailAndAcademiaId(emailNormalizado, academia.getId())
                    .filter(outro -> !outro.getId().equals(aluno.getId()))
                    .ifPresent(outro -> {
                        throw new RuntimeException("E-mail já cadastrado");
                    });
            usuarioRepository.findByEmailAndAcademiaId(emailNormalizado, academia.getId())
                    .filter(usuario -> usuario.getAluno() == null || !usuario.getAluno().getId().equals(aluno.getId()))
                    .ifPresent(usuario -> {
                        throw new RuntimeException("E-mail já cadastrado");
                    });
        }

        Plano plano = null;
        if (request.getPlanoId() != null) {
            plano = planoRepository.findByIdAndAcademiaId(request.getPlanoId(), academia.getId())
                    .orElseThrow(() -> new RuntimeException("Plano não encontrado com ID: " + request.getPlanoId()));
            if (!Boolean.TRUE.equals(plano.getAtivo())) {
                throw new RuntimeException("Plano inativo não pode ser vinculado ao aluno");
            }
        }

        List<Modalidade> modalidades = buscarModalidadesContratadas(request.getModalidadeIds(), academia, plano);

        String nomeNormalizado = request.getNome().trim();
        boolean deveAtualizarFirebase = aluno.getFirebaseUid() != null
                && !aluno.getFirebaseUid().isBlank()
                && (!nomeNormalizado.equals(aluno.getNome())
                || !emailNormalizado.equalsIgnoreCase(aluno.getEmail()));

        if (deveAtualizarFirebase) {
            firebaseAuthService.atualizarUsuario(aluno.getFirebaseUid(), nomeNormalizado, emailNormalizado);
        }

        aluno.setNome(nomeNormalizado);
        aluno.setEmail(emailNormalizado);
        aluno.setTipoAluno(request.getTipoAluno());
        aluno.setValorMensalidade(request.getValorMensalidade());
        aluno.setDiaVencimento(request.getDiaVencimento());
        aluno.setPlano(plano);
        aluno.setAtivo(request.getAtivo());
        aluno.setModalidades(modalidades);

        usuarioRepository.findByAlunoIdAndAcademiaId(aluno.getId(), academia.getId())
                .ifPresent(usuario -> {
                    usuario.setNome(aluno.getNome());
                    usuario.setEmail(aluno.getEmail());
                    usuario.setAtivo(aluno.getAtivo());
                    usuarioRepository.save(usuario);
                });

        return repo.save(aluno);
    }

    private List<Modalidade> buscarModalidadesContratadas(List<Long> modalidadeIds, Academia academia, Plano plano) {
        if (plano != null && plano.getTipo() == TipoPlano.TURMA
                && (modalidadeIds == null || modalidadeIds.isEmpty())) {
            throw new RuntimeException("Informe ao menos uma modalidade para plano de turma");
        }
        if (modalidadeIds == null || modalidadeIds.isEmpty()) {
            return List.of();
        }

        Set<Long> idsUnicos = new LinkedHashSet<>(modalidadeIds);
        List<Modalidade> modalidades = modalidadeRepository.findByIdInAndAcademiaId(new ArrayList<>(idsUnicos), academia.getId());
        if (modalidades.size() != idsUnicos.size()) {
            throw new RuntimeException("Uma ou mais modalidades não pertencem à academia atual");
        }
        modalidades.forEach(modalidade -> {
            if (!modalidade.isAtiva()) {
                throw new RuntimeException("Modalidade inativa não pode ser vinculada ao aluno");
            }
        });
        return modalidades;
    }
}
