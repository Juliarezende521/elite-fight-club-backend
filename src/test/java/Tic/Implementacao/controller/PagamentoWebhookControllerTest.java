package Tic.Implementacao.controller;

import Tic.Implementacao.service.PagamentoService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PagamentoWebhookControllerTest {

    @Test
    void alunoNaoConsegueUsarWebhookMockPorqueEndpointNaoExisteNoRuntime() throws Exception {
        assertMockInexistente();
    }

    @Test
    void professorNaoConsegueUsarWebhookMockPorqueEndpointNaoExisteNoRuntime() throws Exception {
        assertMockInexistente();
    }

    @Test
    void usuarioNaoAutenticadoNaoConsegueUsarWebhookMockPorqueEndpointNaoExisteNoRuntime() throws Exception {
        assertMockInexistente();
    }

    @Test
    void adminNaoPossuiWebhookMockEmRuntime() throws Exception {
        assertMockInexistente();
    }

    private void assertMockInexistente() throws Exception {
        PagamentoService service = mock(PagamentoService.class);
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new PagamentoWebhookController(service)).build();

        mvc.perform(post("/api/webhooks/pagamentos/mock")
                        .param("idPagamentoExterno", "pay_123")
                        .param("status", "PAGO"))
                .andExpect(status().isNotFound());

        verify(service, never()).processarWebhookAsaas(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void webhookAsaasContinuaDisponivel() throws Exception {
        PagamentoService service = mock(PagamentoService.class);
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new PagamentoWebhookController(service)).build();

        mvc.perform(post("/api/webhooks/pagamentos/asaas")
                        .header("asaas-access-token", "token-teste")
                        .content("{}"))
                .andExpect(status().isOk());

        verify(service).processarWebhookAsaas("token-teste", "{}");
    }
}
