package Tic.Implementacao.service;

import Tic.Implementacao.dto.aluno.CriarAlunoRequest;
import Tic.Implementacao.dto.aluno.AtualizarAlunoRequest;
import Tic.Implementacao.model.*;
import Tic.Implementacao.repository.AlunoRepository;
import Tic.Implementacao.repository.ModalidadeRepository;
import Tic.Implementacao.repository.PlanoRepository;
import Tic.Implementacao.repository.UsuarioRepository;
import Tic.Implementacao.security.PermissaoService;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AlunoServiceTest {

    @Test
    void deveCriarAlunoComFirebaseUidEPerfilAluno() {
        AlunoRepository alunoRepository = mock(AlunoRepository.class);
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        FirebaseAuthService firebaseAuthService = mock(FirebaseAuthService.class);
        GraduacaoService graduacaoService = mock(GraduacaoService.class);
        GraduacaoAlunoService graduacaoAlunoService = mock(GraduacaoAlunoService.class);
        FaturaService faturaService = mock(FaturaService.class);
        PermissaoService permissaoService = mock(PermissaoService.class);

        Academia academia = academia();
        Usuario responsavel = usuario(academia);
        Graduacao graduacao = graduacao(academia);
        CriarAlunoRequest request = request();

        when(permissaoService.academiaAtual()).thenReturn(academia);
        when(permissaoService.usuarioAtual()).thenReturn(responsavel);
        when(firebaseAuthService.criarUsuario(anyString(), anyString(), anyString())).thenReturn("firebase-uid");
        when(graduacaoService.buscarGraduacaoBase(1L)).thenReturn(graduacao);
        when(alunoRepository.save(any(Aluno.class))).thenAnswer(invocation -> {
            Aluno aluno = invocation.getArgument(0);
            aluno.setId(10L);
            return aluno;
        });

        AlunoService service = new AlunoService(alunoRepository, usuarioRepository, mock(PlanoRepository.class),
                mock(ModalidadeRepository.class), firebaseAuthService,
                graduacaoService, graduacaoAlunoService, faturaService, permissaoService, transactionManager());

        Aluno aluno = service.criarAluno(request);

        assertEquals("firebase-uid", aluno.getFirebaseUid());
        assertEquals(0, aluno.getQuantidadeCheckins());
        assertEquals(academia, aluno.getAcademia());
        verify(usuarioRepository).save(argThat(usuario ->
                usuario.getPerfil() == PerfilUsuario.ALUNO
                        && "firebase-uid".equals(usuario.getFirebaseUid())
                        && usuario.getSenha() == null));
        verify(faturaService).gerarFaturaMensal(10L, YearMonth.now());
        verify(graduacaoAlunoService).registrarHistoricoInicial(aluno, responsavel);
        verify(firebaseAuthService, never()).excluirUsuario(anyString());
    }

    @Test
    void deveBloquearEmailDuplicadoAntesDoFirebase() {
        AlunoRepository alunoRepository = mock(AlunoRepository.class);
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        FirebaseAuthService firebaseAuthService = mock(FirebaseAuthService.class);
        PermissaoService permissaoService = mock(PermissaoService.class);
        Academia academia = academia();

        when(permissaoService.academiaAtual()).thenReturn(academia);
        when(alunoRepository.existsByEmailAndAcademiaId("aluno@email.com", 1L)).thenReturn(true);

        AlunoService service = new AlunoService(alunoRepository, usuarioRepository, mock(PlanoRepository.class),
                mock(ModalidadeRepository.class), firebaseAuthService,
                mock(GraduacaoService.class), mock(GraduacaoAlunoService.class), mock(FaturaService.class), permissaoService, transactionManager());

        assertThrows(RuntimeException.class, () -> service.criarAluno(request()));
        verify(firebaseAuthService, never()).criarUsuario(anyString(), anyString(), anyString());
    }

    @Test
    void deveExcluirFirebaseQuandoBancoFalhar() {
        AlunoRepository alunoRepository = mock(AlunoRepository.class);
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        FirebaseAuthService firebaseAuthService = mock(FirebaseAuthService.class);
        GraduacaoService graduacaoService = mock(GraduacaoService.class);
        PermissaoService permissaoService = mock(PermissaoService.class);
        Academia academia = academia();

        when(permissaoService.academiaAtual()).thenReturn(academia);
        when(firebaseAuthService.criarUsuario(anyString(), anyString(), anyString())).thenReturn("firebase-uid");
        when(graduacaoService.buscarGraduacaoBase(1L)).thenReturn(graduacao(academia));
        when(alunoRepository.save(any(Aluno.class))).thenThrow(new RuntimeException("Falha no banco"));

        AlunoService service = new AlunoService(alunoRepository, usuarioRepository, mock(PlanoRepository.class),
                mock(ModalidadeRepository.class), firebaseAuthService,
                graduacaoService, mock(GraduacaoAlunoService.class), mock(FaturaService.class), permissaoService, transactionManager());

        assertThrows(RuntimeException.class, () -> service.criarAluno(request()));
        verify(firebaseAuthService).excluirUsuario("firebase-uid");
    }

    @Test
    void naoDeveAcessarAlunoDeOutraAcademia() {
        AlunoRepository alunoRepository = mock(AlunoRepository.class);
        PermissaoService permissaoService = mock(PermissaoService.class);

        when(permissaoService.academiaAtualId()).thenReturn(1L);
        when(alunoRepository.findByIdAndAcademiaId(99L, 1L)).thenReturn(java.util.Optional.empty());

        AlunoService service = new AlunoService(alunoRepository, mock(UsuarioRepository.class), mock(PlanoRepository.class),
                mock(ModalidadeRepository.class), mock(FirebaseAuthService.class),
                mock(GraduacaoService.class), mock(GraduacaoAlunoService.class), mock(FaturaService.class), permissaoService, transactionManager());

        assertTrue(service.exibirIdentificacao(99L).isEmpty());
    }

    @Test
    void deveCriarAlunoComModalidadeContratada() {
        AlunoRepository alunoRepository = mock(AlunoRepository.class);
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        PlanoRepository planoRepository = mock(PlanoRepository.class);
        ModalidadeRepository modalidadeRepository = mock(ModalidadeRepository.class);
        FirebaseAuthService firebaseAuthService = mock(FirebaseAuthService.class);
        GraduacaoService graduacaoService = mock(GraduacaoService.class);
        GraduacaoAlunoService graduacaoAlunoService = mock(GraduacaoAlunoService.class);
        FaturaService faturaService = mock(FaturaService.class);
        PermissaoService permissaoService = mock(PermissaoService.class);

        Academia academia = academia();
        Plano plano = plano(academia);
        Modalidade modalidade = modalidade(academia, 7L, true);
        CriarAlunoRequest request = request();
        request.setPlanoId(30L);
        request.setModalidadeIds(List.of(7L));

        when(permissaoService.academiaAtual()).thenReturn(academia);
        when(permissaoService.usuarioAtual()).thenReturn(usuario(academia));
        when(firebaseAuthService.criarUsuario(anyString(), anyString(), anyString())).thenReturn("firebase-uid");
        when(graduacaoService.buscarGraduacaoBase(1L)).thenReturn(graduacao(academia));
        when(planoRepository.findByIdAndAcademiaId(30L, 1L)).thenReturn(java.util.Optional.of(plano));
        when(modalidadeRepository.findByIdInAndAcademiaId(List.of(7L), 1L)).thenReturn(List.of(modalidade));
        when(alunoRepository.save(any(Aluno.class))).thenAnswer(invocation -> {
            Aluno aluno = invocation.getArgument(0);
            aluno.setId(10L);
            return aluno;
        });

        AlunoService service = new AlunoService(alunoRepository, usuarioRepository, planoRepository,
                modalidadeRepository, firebaseAuthService, graduacaoService, graduacaoAlunoService,
                faturaService, permissaoService, transactionManager());

        Aluno aluno = service.criarAluno(request);

        assertEquals(List.of(modalidade), aluno.getModalidades());
    }

    @Test
    void deveBloquearPlanoTurmaSemModalidade() {
        AlunoRepository alunoRepository = mock(AlunoRepository.class);
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        PlanoRepository planoRepository = mock(PlanoRepository.class);
        ModalidadeRepository modalidadeRepository = mock(ModalidadeRepository.class);
        FirebaseAuthService firebaseAuthService = mock(FirebaseAuthService.class);
        GraduacaoService graduacaoService = mock(GraduacaoService.class);
        PermissaoService permissaoService = mock(PermissaoService.class);
        Academia academia = academia();
        CriarAlunoRequest request = request();
        request.setPlanoId(30L);

        when(permissaoService.academiaAtual()).thenReturn(academia);
        when(firebaseAuthService.criarUsuario(anyString(), anyString(), anyString())).thenReturn("firebase-uid");
        when(graduacaoService.buscarGraduacaoBase(1L)).thenReturn(graduacao(academia));
        when(planoRepository.findByIdAndAcademiaId(30L, 1L)).thenReturn(java.util.Optional.of(plano(academia)));

        AlunoService service = new AlunoService(alunoRepository, usuarioRepository, planoRepository,
                modalidadeRepository, firebaseAuthService, graduacaoService, mock(GraduacaoAlunoService.class),
                mock(FaturaService.class), permissaoService, transactionManager());

        assertThrows(RuntimeException.class, () -> service.criarAluno(request));
        verify(firebaseAuthService).excluirUsuario("firebase-uid");
    }

    @Test
    void deveBloquearModalidadeInativaNoCadastro() {
        AlunoRepository alunoRepository = mock(AlunoRepository.class);
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        PlanoRepository planoRepository = mock(PlanoRepository.class);
        ModalidadeRepository modalidadeRepository = mock(ModalidadeRepository.class);
        FirebaseAuthService firebaseAuthService = mock(FirebaseAuthService.class);
        GraduacaoService graduacaoService = mock(GraduacaoService.class);
        PermissaoService permissaoService = mock(PermissaoService.class);
        Academia academia = academia();
        CriarAlunoRequest request = request();
        request.setPlanoId(30L);
        request.setModalidadeIds(List.of(8L));

        when(permissaoService.academiaAtual()).thenReturn(academia);
        when(firebaseAuthService.criarUsuario(anyString(), anyString(), anyString())).thenReturn("firebase-uid");
        when(graduacaoService.buscarGraduacaoBase(1L)).thenReturn(graduacao(academia));
        when(planoRepository.findByIdAndAcademiaId(30L, 1L)).thenReturn(java.util.Optional.of(plano(academia)));
        when(modalidadeRepository.findByIdInAndAcademiaId(List.of(8L), 1L))
                .thenReturn(List.of(modalidade(academia, 8L, false)));

        AlunoService service = new AlunoService(alunoRepository, usuarioRepository, planoRepository,
                modalidadeRepository, firebaseAuthService, graduacaoService, mock(GraduacaoAlunoService.class),
                mock(FaturaService.class), permissaoService, transactionManager());

        assertThrows(RuntimeException.class, () -> service.criarAluno(request));
        verify(firebaseAuthService).excluirUsuario("firebase-uid");
    }

    @Test
    void deveAtualizarCadastroDoAlunoComModalidades() {
        AlunoRepository alunoRepository = mock(AlunoRepository.class);
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        PlanoRepository planoRepository = mock(PlanoRepository.class);
        ModalidadeRepository modalidadeRepository = mock(ModalidadeRepository.class);
        FirebaseAuthService firebaseAuthService = mock(FirebaseAuthService.class);
        PermissaoService permissaoService = mock(PermissaoService.class);

        Academia academia = academia();
        Aluno aluno = aluno(academia);
        Usuario usuario = usuario(academia);
        usuario.setAluno(aluno);
        Plano plano = plano(academia);
        Modalidade modalidade = modalidade(academia, 7L, true);
        AtualizarAlunoRequest request = atualizarRequest();
        request.setPlanoId(30L);
        request.setModalidadeIds(List.of(7L));

        when(permissaoService.academiaAtual()).thenReturn(academia);
        when(alunoRepository.findByIdAndAcademiaId(10L, 1L)).thenReturn(java.util.Optional.of(aluno));
        when(planoRepository.findByIdAndAcademiaId(30L, 1L)).thenReturn(java.util.Optional.of(plano));
        when(modalidadeRepository.findByIdInAndAcademiaId(List.of(7L), 1L)).thenReturn(List.of(modalidade));
        when(usuarioRepository.findByAlunoIdAndAcademiaId(10L, 1L)).thenReturn(java.util.Optional.of(usuario));
        when(alunoRepository.save(any(Aluno.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AlunoService service = new AlunoService(alunoRepository, usuarioRepository, planoRepository,
                modalidadeRepository, firebaseAuthService, mock(GraduacaoService.class),
                mock(GraduacaoAlunoService.class), mock(FaturaService.class), permissaoService, transactionManager());

        Aluno atualizado = service.atualizarCadastro(10L, request);

        assertEquals("Aluno Editado", atualizado.getNome());
        assertEquals("editado@email.com", atualizado.getEmail());
        assertEquals(BigDecimal.valueOf(250), atualizado.getValorMensalidade());
        assertEquals(15, atualizado.getDiaVencimento());
        assertEquals(plano, atualizado.getPlano());
        assertEquals(List.of(modalidade), atualizado.getModalidades());
        verify(firebaseAuthService).atualizarUsuario("firebase-uid", "Aluno Editado", "editado@email.com");
        verify(usuarioRepository).save(argThat(u -> "Aluno Editado".equals(u.getNome())
                && "editado@email.com".equals(u.getEmail())
                && Boolean.TRUE.equals(u.getAtivo())));
    }

    @Test
    void deveBloquearEmailDuplicadoAoAtualizarCadastro() {
        AlunoRepository alunoRepository = mock(AlunoRepository.class);
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        PermissaoService permissaoService = mock(PermissaoService.class);
        Academia academia = academia();
        Aluno aluno = aluno(academia);
        Aluno outro = aluno(academia);
        outro.setId(99L);
        AtualizarAlunoRequest request = atualizarRequest();

        when(permissaoService.academiaAtual()).thenReturn(academia);
        when(alunoRepository.findByIdAndAcademiaId(10L, 1L)).thenReturn(java.util.Optional.of(aluno));
        when(alunoRepository.findByEmailAndAcademiaId("editado@email.com", 1L)).thenReturn(java.util.Optional.of(outro));

        AlunoService service = new AlunoService(alunoRepository, usuarioRepository, mock(PlanoRepository.class),
                mock(ModalidadeRepository.class), mock(FirebaseAuthService.class),
                mock(GraduacaoService.class), mock(GraduacaoAlunoService.class),
                mock(FaturaService.class), permissaoService, transactionManager());

        assertThrows(RuntimeException.class, () -> service.atualizarCadastro(10L, request));
    }

    @Test
    void deveBloquearPlanoTurmaSemModalidadeAoAtualizarCadastro() {
        AlunoRepository alunoRepository = mock(AlunoRepository.class);
        PlanoRepository planoRepository = mock(PlanoRepository.class);
        PermissaoService permissaoService = mock(PermissaoService.class);
        Academia academia = academia();
        Aluno aluno = aluno(academia);
        AtualizarAlunoRequest request = atualizarRequest();
        request.setPlanoId(30L);
        request.setModalidadeIds(List.of());

        when(permissaoService.academiaAtual()).thenReturn(academia);
        when(alunoRepository.findByIdAndAcademiaId(10L, 1L)).thenReturn(java.util.Optional.of(aluno));
        when(planoRepository.findByIdAndAcademiaId(30L, 1L)).thenReturn(java.util.Optional.of(plano(academia)));

        AlunoService service = new AlunoService(alunoRepository, mock(UsuarioRepository.class), planoRepository,
                mock(ModalidadeRepository.class), mock(FirebaseAuthService.class),
                mock(GraduacaoService.class), mock(GraduacaoAlunoService.class),
                mock(FaturaService.class), permissaoService, transactionManager());

        assertThrows(RuntimeException.class, () -> service.atualizarCadastro(10L, request));
    }

    private CriarAlunoRequest request() {
        CriarAlunoRequest request = new CriarAlunoRequest();
        request.setNome("Aluno Teste");
        request.setEmail("aluno@email.com");
        request.setSenhaInicial("Senha123");
        request.setTipoAluno(TipoAluno.TURMA);
        request.setValorMensalidade(BigDecimal.valueOf(180));
        request.setDiaVencimento(10);
        return request;
    }

    private AtualizarAlunoRequest atualizarRequest() {
        AtualizarAlunoRequest request = new AtualizarAlunoRequest();
        request.setNome("Aluno Editado");
        request.setEmail("editado@email.com");
        request.setTipoAluno(TipoAluno.TURMA);
        request.setValorMensalidade(BigDecimal.valueOf(250));
        request.setDiaVencimento(15);
        request.setAtivo(true);
        request.setModalidadeIds(List.of());
        return request;
    }

    private Aluno aluno(Academia academia) {
        Aluno aluno = new Aluno();
        aluno.setId(10L);
        aluno.setNome("Aluno Teste");
        aluno.setEmail("aluno@email.com");
        aluno.setFirebaseUid("firebase-uid");
        aluno.setTipoAluno(TipoAluno.TURMA);
        aluno.setValorMensalidade(BigDecimal.valueOf(180));
        aluno.setDiaVencimento(10);
        aluno.setQuantidadeCheckins(0);
        aluno.setAtivo(true);
        aluno.setAcademia(academia);
        return aluno;
    }

    private Academia academia() {
        Academia academia = new Academia();
        academia.setId(1L);
        academia.setNome("Elite Fight Club");
        return academia;
    }

    private Plano plano(Academia academia) {
        Plano plano = new Plano();
        plano.setId(30L);
        plano.setAcademia(academia);
        plano.setTipo(TipoPlano.TURMA);
        plano.setQuantidadeAulasSemana(3);
        plano.setValor(BigDecimal.valueOf(180));
        plano.setAtivo(true);
        return plano;
    }

    private Modalidade modalidade(Academia academia, Long id, boolean ativa) {
        Modalidade modalidade = new Modalidade();
        modalidade.setId(id);
        modalidade.setNome("Jiu-Jitsu");
        modalidade.setAcademia(academia);
        modalidade.setAtiva(ativa);
        return modalidade;
    }

    private Graduacao graduacao(Academia academia) {
        Graduacao graduacao = new Graduacao();
        graduacao.setId(1L);
        graduacao.setNome("Branco");
        graduacao.setAcademia(academia);
        return graduacao;
    }

    private Usuario usuario(Academia academia) {
        Usuario usuario = new Usuario();
        usuario.setId(99L);
        usuario.setNome("Professor");
        usuario.setEmail("prof@email.com");
        usuario.setPerfil(PerfilUsuario.PROFESSOR);
        usuario.setAcademia(academia);
        return usuario;
    }

    private PlatformTransactionManager transactionManager() {
        return new AbstractPlatformTransactionManager() {
            @Override
            protected Object doGetTransaction() {
                return new Object();
            }

            @Override
            protected void doBegin(Object transaction, TransactionDefinition definition) {
            }

            @Override
            protected void doCommit(DefaultTransactionStatus status) {
            }

            @Override
            protected void doRollback(DefaultTransactionStatus status) {
            }
        };
    }
}
