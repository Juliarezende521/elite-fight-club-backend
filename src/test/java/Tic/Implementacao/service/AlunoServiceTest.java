package Tic.Implementacao.service;

import Tic.Implementacao.dto.aluno.CriarAlunoRequest;
import Tic.Implementacao.model.*;
import Tic.Implementacao.repository.AlunoRepository;
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

        AlunoService service = new AlunoService(alunoRepository, usuarioRepository, mock(PlanoRepository.class), firebaseAuthService,
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

        AlunoService service = new AlunoService(alunoRepository, usuarioRepository, mock(PlanoRepository.class), firebaseAuthService,
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

        AlunoService service = new AlunoService(alunoRepository, usuarioRepository, mock(PlanoRepository.class), firebaseAuthService,
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

        AlunoService service = new AlunoService(alunoRepository, mock(UsuarioRepository.class), mock(PlanoRepository.class), mock(FirebaseAuthService.class),
                mock(GraduacaoService.class), mock(GraduacaoAlunoService.class), mock(FaturaService.class), permissaoService, transactionManager());

        assertTrue(service.exibirIdentificacao(99L).isEmpty());
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

    private Academia academia() {
        Academia academia = new Academia();
        academia.setId(1L);
        academia.setNome("Elite Fight Club");
        return academia;
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
