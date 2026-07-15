package Tic.Implementacao.service;

import Tic.Implementacao.model.*;
import Tic.Implementacao.dto.aluno.CriarAlunoRequest;
import Tic.Implementacao.repository.AlunoRepository;
import Tic.Implementacao.repository.PlanoRepository;
import Tic.Implementacao.repository.UsuarioRepository;
import Tic.Implementacao.security.PermissaoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Service
public class AlunoService {

    private final AlunoRepository repo;
    private final UsuarioRepository usuarioRepository;
    private final PlanoRepository planoRepository;
    private final FirebaseAuthService firebaseAuthService;
    private final GraduacaoService graduacaoService;
    private final GraduacaoAlunoService graduacaoAlunoService;
    private final FaturaService faturaService;
    private final PermissaoService permissaoService;
    private final TransactionTemplate transactionTemplate;

    public AlunoService(AlunoRepository repo, UsuarioRepository usuarioRepository, PlanoRepository planoRepository, FirebaseAuthService firebaseAuthService,
                        GraduacaoService graduacaoService, GraduacaoAlunoService graduacaoAlunoService,
                        FaturaService faturaService, PermissaoService permissaoService,
                        PlatformTransactionManager transactionManager){
        this.repo = repo;
        this.usuarioRepository = usuarioRepository;
        this.planoRepository = planoRepository;
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
        return repo.findDistinctByModalidadesId(modalidadeId);
    }

    public List<Aluno> listarPorTipoModalidade(String tipoModalidade) {
        return repo.findDistinctByModalidadesTipoModalidade(TipoModalidade.valueOf(tipoModalidade.toUpperCase()));
    }

    @Transactional
    public boolean remove(Long id){
        Long academiaId = permissaoService.academiaAtualId();
        if(repo.findByIdAndAcademiaId(id, academiaId).isPresent()){
            repo.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional
    public Aluno atualizarDados(Long id, Aluno alterado){
        Long academiaId = permissaoService.academiaAtualId();
        if(repo.findByIdAndAcademiaId(id, academiaId).isPresent()){
            Aluno atual = repo.findByIdAndAcademiaId(id, academiaId).orElseThrow();
            alterado.setFirebaseUid(atual.getFirebaseUid());
            alterado.setQuantidadeCheckins(atual.getQuantidadeCheckins());
            alterado.setAcademia(atual.getAcademia());
            alterado.setId(id);
            return repo.save(alterado);
        }
        return null;
    }

    @Transactional
    public Aluno salva(Aluno aluno){
        return repo.save(aluno);
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
}
