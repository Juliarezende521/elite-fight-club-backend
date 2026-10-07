package Tic.Implementacao.service;

import Tic.Implementacao.dto.professor.*;
import Tic.Implementacao.model.*;
import Tic.Implementacao.repository.ModalidadeRepository;
import Tic.Implementacao.repository.ProfessorModalidadeRepository;
import Tic.Implementacao.repository.ProfessorRepository;
import Tic.Implementacao.repository.UsuarioRepository;
import Tic.Implementacao.security.PermissaoService;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ProfessorServiceTest {

    @Test
    void deveCriarProfessorComFirebaseUsuarioEModalidades() {
        Fixture fx = fixture();
        CriarProfessorRequest request = criarRequest();

        when(fx.firebaseAuthService.criarUsuario(anyString(), anyString(), anyString())).thenReturn("firebase-prof");
        when(fx.professorRepository.save(any(Professor.class))).thenAnswer(invocation -> {
            Professor professor = invocation.getArgument(0);
            professor.setId(10L);
            return professor;
        });
        when(fx.professorModalidadeRepository.findByAcademiaIdAndProfessorId(1L, 10L)).thenReturn(List.of());

        ProfessorResponse response = fx.service.criar(request);

        assertEquals(10L, response.getId());
        verify(fx.usuarioRepository).save(argThat(usuario ->
                usuario.getPerfil() == PerfilUsuario.PROFESSOR
                        && "firebase-prof".equals(usuario.getFirebaseUid())
                        && usuario.getSenha() == null
                        && usuario.getProfessor() != null));
        verify(fx.professorModalidadeRepository).save(argThat(vinculo ->
                vinculo.getProfessor().getId().equals(10L)
                        && vinculo.getModalidade().getId().equals(20L)
                        && Boolean.TRUE.equals(vinculo.getPermiteTurma())
                        && Boolean.TRUE.equals(vinculo.getPermitePersonal())));
        verify(fx.firebaseAuthService, never()).excluirUsuario(anyString());
    }

    @Test
    void deveExcluirFirebaseQuandoBancoFalharAoCriarProfessor() {
        Fixture fx = fixture();
        when(fx.firebaseAuthService.criarUsuario(anyString(), anyString(), anyString())).thenReturn("firebase-prof");
        when(fx.professorRepository.save(any(Professor.class))).thenThrow(new RuntimeException("Falha no banco"));

        assertThrows(RuntimeException.class, () -> fx.service.criar(criarRequest()));

        verify(fx.firebaseAuthService).excluirUsuario("firebase-prof");
    }

    @Test
    void deveBloquearEmailDuplicadoAntesDoFirebase() {
        Fixture fx = fixture();
        Usuario existente = new Usuario();
        existente.setEmail("professor@email.com");
        when(fx.usuarioRepository.findByEmail("professor@email.com")).thenReturn(Optional.of(existente));

        assertThrows(RuntimeException.class, () -> fx.service.criar(criarRequest()));

        verify(fx.firebaseAuthService, never()).criarUsuario(anyString(), anyString(), anyString());
    }

    @Test
    void deveAtualizarCadastroUsuarioFirebaseEModalidades() {
        Fixture fx = fixture();
        Professor professor = professor();
        Usuario usuario = usuarioProfessor(professor);
        AtualizarProfessorRequest request = atualizarRequest();

        when(fx.professorRepository.findByIdAndAcademiaId(10L, 1L)).thenReturn(Optional.of(professor));
        when(fx.usuarioRepository.findByProfessorIdAndAcademiaId(10L, 1L)).thenReturn(Optional.of(usuario));
        when(fx.professorRepository.save(any(Professor.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(fx.professorModalidadeRepository.findByAcademiaIdAndProfessorId(1L, 10L)).thenReturn(List.of());

        ProfessorResponse response = fx.service.atualizar(10L, request);

        assertEquals("Professor Atualizado", response.getNome());
        assertFalse(response.getAtivo());
        verify(fx.firebaseAuthService).atualizarUsuario("firebase-prof", "Professor Atualizado", "novo@email.com");
        verify(fx.usuarioRepository).save(argThat(salvo ->
                "novo@email.com".equals(salvo.getEmail()) && Boolean.FALSE.equals(salvo.getAtivo())));
        verify(fx.professorModalidadeRepository).save(any(ProfessorModalidade.class));
    }

    @Test
    void deveDesativarProfessorEUsuarioSemExcluirRegistro() {
        Fixture fx = fixture();
        Professor professor = professor();
        Usuario usuario = usuarioProfessor(professor);
        AtualizarStatusProfessorRequest request = new AtualizarStatusProfessorRequest();
        request.setAtivo(false);

        when(fx.professorRepository.findByIdAndAcademiaId(10L, 1L)).thenReturn(Optional.of(professor));
        when(fx.usuarioRepository.findByProfessorIdAndAcademiaId(10L, 1L)).thenReturn(Optional.of(usuario));
        when(fx.professorRepository.save(any(Professor.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(fx.professorModalidadeRepository.findByAcademiaIdAndProfessorId(1L, 10L)).thenReturn(List.of());

        ProfessorResponse response = fx.service.alterarStatus(10L, request);

        assertFalse(response.getAtivo());
        verify(fx.professorRepository, never()).delete(any());
        verify(fx.usuarioRepository).save(argThat(salvo -> Boolean.FALSE.equals(salvo.getAtivo())));
    }

    @Test
    void deveBloquearModalidadeDeOutraAcademia() {
        Fixture fx = fixture();
        when(fx.modalidadeRepository.findByIdInAndAcademiaId(List.of(99L), 1L)).thenReturn(List.of());
        CriarProfessorRequest request = criarRequest();
        request.getModalidades().get(0).setModalidadeId(99L);

        assertThrows(RuntimeException.class, () -> fx.service.criar(request));

        verify(fx.firebaseAuthService, never()).criarUsuario(anyString(), anyString(), anyString());
    }

    private Fixture fixture() {
        ProfessorRepository professorRepository = mock(ProfessorRepository.class);
        ProfessorModalidadeRepository professorModalidadeRepository = mock(ProfessorModalidadeRepository.class);
        ModalidadeRepository modalidadeRepository = mock(ModalidadeRepository.class);
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        FirebaseAuthService firebaseAuthService = mock(FirebaseAuthService.class);
        PermissaoService permissaoService = mock(PermissaoService.class);
        Academia academia = academia();
        Modalidade modalidade = modalidade(academia, 20L, true);

        when(permissaoService.academiaAtual()).thenReturn(academia);
        when(permissaoService.academiaAtualId()).thenReturn(1L);
        when(modalidadeRepository.findByIdInAndAcademiaId(List.of(20L), 1L)).thenReturn(List.of(modalidade));

        ProfessorService service = new ProfessorService(professorRepository, professorModalidadeRepository,
                modalidadeRepository, usuarioRepository, firebaseAuthService, permissaoService, transactionManager());
        return new Fixture(professorRepository, professorModalidadeRepository, modalidadeRepository,
                usuarioRepository, firebaseAuthService, permissaoService, service);
    }

    private CriarProfessorRequest criarRequest() {
        CriarProfessorRequest request = new CriarProfessorRequest();
        request.setNome("Professor Teste");
        request.setCpf("12345678900");
        request.setEmail("professor@email.com");
        request.setSenhaInicial("123456");
        request.setContato("11999999999");
        request.setModalidades(List.of(professorModalidadeRequest(20L, true, true)));
        return request;
    }

    private AtualizarProfessorRequest atualizarRequest() {
        AtualizarProfessorRequest request = new AtualizarProfessorRequest();
        request.setNome("Professor Atualizado");
        request.setCpf("12345678900");
        request.setEmail("novo@email.com");
        request.setContato("11888888888");
        request.setAtivo(false);
        request.setModalidades(List.of(professorModalidadeRequest(20L, true, false)));
        return request;
    }

    private ProfessorModalidadeRequest professorModalidadeRequest(Long modalidadeId, boolean turma, boolean personal) {
        ProfessorModalidadeRequest request = new ProfessorModalidadeRequest();
        request.setModalidadeId(modalidadeId);
        request.setPermiteTurma(turma);
        request.setPermitePersonal(personal);
        return request;
    }

    private Academia academia() {
        Academia academia = new Academia();
        academia.setId(1L);
        academia.setNome("Elite Fight Club");
        return academia;
    }

    private Professor professor() {
        Professor professor = new Professor();
        professor.setId(10L);
        professor.setNome("Professor Teste");
        professor.setCpf("12345678900");
        professor.setAcademia(academia());
        professor.setAtivo(true);
        return professor;
    }

    private Usuario usuarioProfessor(Professor professor) {
        Usuario usuario = new Usuario();
        usuario.setId(50L);
        usuario.setNome(professor.getNome());
        usuario.setEmail("professor@email.com");
        usuario.setFirebaseUid("firebase-prof");
        usuario.setPerfil(PerfilUsuario.PROFESSOR);
        usuario.setAtivo(true);
        usuario.setProfessor(professor);
        usuario.setAcademia(professor.getAcademia());
        return usuario;
    }

    private Modalidade modalidade(Academia academia, Long id, boolean ativa) {
        Modalidade modalidade = new Modalidade();
        modalidade.setId(id);
        modalidade.setNome("Muay Thai");
        modalidade.setAcademia(academia);
        modalidade.setAtiva(ativa);
        return modalidade;
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

    private record Fixture(ProfessorRepository professorRepository,
                           ProfessorModalidadeRepository professorModalidadeRepository,
                           ModalidadeRepository modalidadeRepository,
                           UsuarioRepository usuarioRepository,
                           FirebaseAuthService firebaseAuthService,
                           PermissaoService permissaoService,
                           ProfessorService service) {
    }
}
