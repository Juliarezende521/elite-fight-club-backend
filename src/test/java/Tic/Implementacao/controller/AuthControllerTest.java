package Tic.Implementacao.controller;

import Tic.Implementacao.dto.auth.AuthenticatedUserResponse;
import Tic.Implementacao.model.Academia;
import Tic.Implementacao.model.Aluno;
import Tic.Implementacao.model.PerfilUsuario;
import Tic.Implementacao.model.Professor;
import Tic.Implementacao.model.StatusAcademia;
import Tic.Implementacao.model.Usuario;
import Tic.Implementacao.repository.UsuarioRepository;
import Tic.Implementacao.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthControllerTest {

    @Test
    void deveRetornarUsuarioAutenticadoComVinculos() {
        AuthController controller = new AuthController(mock(UsuarioService.class), mock(UsuarioRepository.class));
        Usuario usuario = usuarioAutenticado();

        ResponseEntity<AuthenticatedUserResponse> response = controller.usuarioLogado(
                new UsernamePasswordAuthenticationToken(usuario, null),
                mock(HttpServletRequest.class)
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(10L, response.getBody().getId());
        assertEquals("Rafael Moreira", response.getBody().getNome());
        assertEquals("rafael@elitefc.com", response.getBody().getEmail());
        assertEquals(PerfilUsuario.ALUNO, response.getBody().getPerfil());
        assertTrue(response.getBody().getAtivo());
        assertEquals(1L, response.getBody().getAcademia().getId());
        assertEquals("Elite Fight Club", response.getBody().getAcademia().getNome());
        assertEquals(StatusAcademia.ATIVA, response.getBody().getAcademia().getStatus());
        assertEquals(20L, response.getBody().getAlunoId());
        assertEquals(30L, response.getBody().getProfessorId());
    }

    @Test
    void deveRetornarNaoAutorizadoSemPrincipalUsuario() {
        AuthController controller = new AuthController(mock(UsuarioService.class), mock(UsuarioRepository.class));
        HttpServletRequest request = mock(HttpServletRequest.class);

        ResponseEntity<AuthenticatedUserResponse> response = controller.usuarioLogado(null, request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void deveRetornarUsuarioInativoQuandoPrincipalValido() {
        AuthController controller = new AuthController(mock(UsuarioService.class), mock(UsuarioRepository.class));
        Usuario usuario = usuarioAutenticado();
        usuario.setAtivo(false);

        ResponseEntity<AuthenticatedUserResponse> response = controller.usuarioLogado(
                new UsernamePasswordAuthenticationToken(usuario, null),
                mock(HttpServletRequest.class)
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().getAtivo());
    }

    private Usuario usuarioAutenticado() {
        Academia academia = new Academia();
        academia.setId(1L);
        academia.setNome("Elite Fight Club");
        academia.setStatus(StatusAcademia.ATIVA);

        Aluno aluno = new Aluno();
        aluno.setId(20L);

        Professor professor = new Professor();
        professor.setId(30L);

        Usuario usuario = new Usuario();
        usuario.setId(10L);
        usuario.setNome("Rafael Moreira");
        usuario.setEmail("rafael@elitefc.com");
        usuario.setPerfil(PerfilUsuario.ALUNO);
        usuario.setAtivo(true);
        usuario.setAcademia(academia);
        usuario.setAluno(aluno);
        usuario.setProfessor(professor);
        return usuario;
    }
}
