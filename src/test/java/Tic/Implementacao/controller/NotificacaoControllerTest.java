package Tic.Implementacao.controller;

import Tic.Implementacao.dto.notificacao.NotificacaoRelatorioResponse;
import Tic.Implementacao.exception.ApiErrorResponse;
import Tic.Implementacao.security.PermissaoService;
import Tic.Implementacao.service.NotificacaoService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificacaoControllerTest {

    @Test
    void crudLegadoEstaDesativadoParaAdminSemExporNotificacoes() {
        PermissaoService permissaoService = mock(PermissaoService.class);
        NotificacaoController controller = new NotificacaoController(mock(NotificacaoService.class), permissaoService);

        ResponseEntity<ApiErrorResponse> response = controller.endpointLegadoDesativado();

        assertEquals(HttpStatus.GONE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("LEGACY_ENDPOINT_DISABLED", response.getBody().getErro());
        verify(permissaoService).exigirAdmin();
    }

    @Test
    void alunoEProfessorSaoBloqueadosNoCrudLegado() {
        PermissaoService permissaoService = mock(PermissaoService.class);
        doThrow(new AccessDeniedException("Acesso permitido apenas para ADMIN"))
                .when(permissaoService).exigirAdmin();
        NotificacaoController controller = new NotificacaoController(mock(NotificacaoService.class), permissaoService);

        assertThrows(AccessDeniedException.class, controller::endpointLegadoDesativado);
    }

    @Test
    void relatorioOperacionalContinuaDelegandoParaServiceOficial() {
        NotificacaoService service = mock(NotificacaoService.class);
        when(service.relatorio(null, null, null, null, null, null)).thenReturn(List.of());
        NotificacaoController controller = new NotificacaoController(service, mock(PermissaoService.class));

        ResponseEntity<List<NotificacaoRelatorioResponse>> response =
                controller.relatorio(null, null, null, null, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(service).relatorio(null, null, null, null, null, null);
    }
}
