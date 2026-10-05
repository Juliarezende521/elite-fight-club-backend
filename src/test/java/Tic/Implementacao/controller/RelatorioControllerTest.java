package Tic.Implementacao.controller;

import Tic.Implementacao.security.PermissaoService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RelatorioControllerTest {

    @Test
    void getLegadoNaoRetornaRelatorios() throws Exception {
        PermissaoService permissaoService = mock(PermissaoService.class);
        MockMvc mvc = mvc(permissaoService);

        mvc.perform(get("/Relatorio"))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.erro").value("LEGACY_ENDPOINT_DISABLED"))
                .andExpect(jsonPath("$.mensagem", not(containsString("SQL"))))
                .andExpect(jsonPath("$.mensagem", not(containsString("FinanceiroRepository"))))
                .andExpect(jsonPath("$.campos").isEmpty());

        verify(permissaoService).exigirAdmin();
    }

    @Test
    void postLegadoNaoCriaRelatorio() throws Exception {
        PermissaoService permissaoService = mock(PermissaoService.class);
        MockMvc mvc = mvc(permissaoService);

        mvc.perform(post("/Relatorio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tipoRelatorio": "FINANCEIRO_CONSOLIDADO",
                                  "periodo": "ATUAL",
                                  "conteudo": "conteudo arbitrario"
                                }
                                """))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.erro").value("LEGACY_ENDPOINT_DISABLED"))
                .andExpect(jsonPath("$.campos").isEmpty());

        verify(permissaoService).exigirAdmin();
    }

    @Test
    void postFinanceiroLegadoNaoGeraConsultaFinanceira() throws Exception {
        PermissaoService permissaoService = mock(PermissaoService.class);
        MockMvc mvc = mvc(permissaoService);

        mvc.perform(post("/Relatorio/financeiro"))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.erro").value("LEGACY_ENDPOINT_DISABLED"))
                .andExpect(jsonPath("$.mensagem", not(containsString("financeiro_id"))));

        verify(permissaoService).exigirAdmin();
    }

    @Test
    void postEvolucaoLegadoNaoGeraConsultaFisica() throws Exception {
        PermissaoService permissaoService = mock(PermissaoService.class);
        MockMvc mvc = mvc(permissaoService);

        mvc.perform(post("/Relatorio/evolucao/aluno/10"))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.erro").value("LEGACY_ENDPOINT_DISABLED"))
                .andExpect(jsonPath("$.mensagem", not(containsString("EvolucaoRepository"))));

        verify(permissaoService).exigirAdmin();
    }

    @Test
    void patchLegadoNaoAlteraRelatorio() throws Exception {
        PermissaoService permissaoService = mock(PermissaoService.class);
        MockMvc mvc = mvc(permissaoService);

        mvc.perform(patch("/Relatorio/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"conteudo\":\"alterado\"}"))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.erro").value("LEGACY_ENDPOINT_DISABLED"));

        verify(permissaoService).exigirAdmin();
    }

    @Test
    void deleteLegadoNaoRemoveRelatorio() throws Exception {
        PermissaoService permissaoService = mock(PermissaoService.class);
        MockMvc mvc = mvc(permissaoService);

        mvc.perform(delete("/Relatorio/1"))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.erro").value("LEGACY_ENDPOINT_DISABLED"));

        verify(permissaoService).exigirAdmin();
    }

    @Test
    void perfilSemAdminContinuaBloqueadoNoRelatorioLegado() {
        PermissaoService permissaoService = mock(PermissaoService.class);
        doThrow(new AccessDeniedException("Acesso permitido apenas para ADMIN"))
                .when(permissaoService).exigirAdmin();
        RelatorioController controller = new RelatorioController(permissaoService);

        org.junit.jupiter.api.Assertions.assertThrows(
                AccessDeniedException.class,
                controller::endpointLegadoDesativado
        );
    }

    private MockMvc mvc(PermissaoService permissaoService) {
        return MockMvcBuilders.standaloneSetup(new RelatorioController(permissaoService)).build();
    }
}
