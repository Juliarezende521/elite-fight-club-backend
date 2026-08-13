package Tic.Implementacao.controller;

import Tic.Implementacao.exception.ApiErrorResponse;
import Tic.Implementacao.security.PermissaoService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FinanceiroControllerTest {

    @Test
    void adminRecebeEndpointLegadoDesativadoSemDadosFinanceiros() {
        PermissaoService permissaoService = mock(PermissaoService.class);
        FinanceiroController controller = new FinanceiroController(permissaoService);

        ResponseEntity<ApiErrorResponse> response = controller.endpointLegadoDesativado();

        assertEquals(HttpStatus.GONE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("LEGACY_ENDPOINT_DISABLED", response.getBody().getErro());
        assertTrue(response.getBody().getCampos().isEmpty());
        verify(permissaoService).exigirAdmin();
    }

    @Test
    void professorEAlunoSaoBloqueadosNoFinanceiroLegado() {
        PermissaoService permissaoService = mock(PermissaoService.class);
        doThrow(new AccessDeniedException("Acesso permitido apenas para ADMIN"))
                .when(permissaoService).exigirAdmin();
        FinanceiroController controller = new FinanceiroController(permissaoService);

        assertThrows(AccessDeniedException.class, controller::endpointLegadoDesativado);
    }
}
