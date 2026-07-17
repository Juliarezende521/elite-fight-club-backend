package Tic.Implementacao.controller;

import Tic.Implementacao.dto.auth.AuthenticatedUserResponse;
import Tic.Implementacao.model.Academia;
import Tic.Implementacao.model.Aluno;
import Tic.Implementacao.model.PerfilUsuario;
import Tic.Implementacao.model.Professor;
import Tic.Implementacao.model.StatusAcademia;
import Tic.Implementacao.model.Usuario;
import Tic.Implementacao.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class AuthControllerTest {

    @Test
    void deveRetornarUsuarioAutenticadoComVinculos() {
        AuthController controller = new AuthController(mock(UsuarioService.class));
        Usuario usuario = usuarioAutenticado();

        ResponseEntity<AuthenticatedUserResponse> response = controller.usuarioLogado(
                new UsernamePasswordAuthenticationToken(usuario, null)
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
        AuthController controller = new AuthController(mock(UsuarioService.class));

        ResponseEntity<AuthenticatedUserResponse> response = controller.usuarioLogado(null);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
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
