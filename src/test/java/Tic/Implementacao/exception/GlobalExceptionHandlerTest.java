package Tic.Implementacao.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    @Test
    void runtimeInesperadoNaoExpoeDetalhesInternos() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ResponseEntity<ApiErrorResponse> response = handler.tratarRuntime(
                new RuntimeException("JDBC exception executing SQL [select token from usuarios]")
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("UNEXPECTED_ERROR", response.getBody().getErro());
        assertEquals("Não foi possível concluir a operação. Tente novamente.", response.getBody().getMensagem());
        assertFalse(response.getBody().getMensagem().contains("JDBC"));
        assertFalse(response.getBody().getMensagem().contains("token"));
    }

    @Test
    void excecaoDeNegocioMantemMensagemAmigavel() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ResponseEntity<ApiErrorResponse> response = handler.tratarNegocio(
                new NegocioException("Endpoint legado de cadastro de aluno desativado. Use POST /api/alunos.")
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("BUSINESS_ERROR", response.getBody().getErro());
        assertTrue(response.getBody().getMensagem().contains("Endpoint legado"));
    }
}
