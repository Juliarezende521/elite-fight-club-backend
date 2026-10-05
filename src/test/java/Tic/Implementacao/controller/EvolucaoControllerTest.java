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

class EvolucaoControllerTest {

    @Test
    void getListaLegadaNaoRetornaEvolucoes() throws Exception {
        PermissaoService permissaoService = mock(PermissaoService.class);
        MockMvc mvc = mvc(permissaoService);

        mvc.perform(get("/Evolucao"))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.erro").value("LEGACY_ENDPOINT_DISABLED"))
                .andExpect(jsonPath("$.mensagem", not(containsString("SQL"))))
                .andExpect(jsonPath("$.mensagem", not(containsString("EvolucaoRepository"))))
                .andExpect(jsonPath("$.campos").isEmpty());

        verify(permissaoService).exigirAdmin();
    }

    @Test
    void getPorIdLegadoNaoRetornaEntidade() throws Exception {
        PermissaoService permissaoService = mock(PermissaoService.class);
        MockMvc mvc = mvc(permissaoService);

        mvc.perform(get("/Evolucao/1"))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.erro").value("LEGACY_ENDPOINT_DISABLED"))
                .andExpect(jsonPath("$.campos").isEmpty());

        verify(permissaoService).exigirAdmin();
    }

    @Test
    void getPorAlunoLegadoNaoRetornaDadosFisicos() throws Exception {
        PermissaoService permissaoService = mock(PermissaoService.class);
        MockMvc mvc = mvc(permissaoService);

        mvc.perform(get("/Evolucao/aluno/10"))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.erro").value("LEGACY_ENDPOINT_DISABLED"))
                .andExpect(jsonPath("$.mensagem", not(containsString("peso"))))
                .andExpect(jsonPath("$.campos").isEmpty());

        verify(permissaoService).exigirAdmin();
    }

    @Test
    void getLinhaDoTempoLegadaNaoRetornaDadosFisicos() throws Exception {
        PermissaoService permissaoService = mock(PermissaoService.class);
        MockMvc mvc = mvc(permissaoService);

        mvc.perform(get("/Evolucao/aluno/10/linha-do-tempo"))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.erro").value("LEGACY_ENDPOINT_DISABLED"))
                .andExpect(jsonPath("$.campos").isEmpty());

        verify(permissaoService).exigirAdmin();
    }

    @Test
    void postLegadoNaoCriaEvolucao() throws Exception {
        PermissaoService permissaoService = mock(PermissaoService.class);
        MockMvc mvc = mvc(permissaoService);

        mvc.perform(post("/Evolucao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "pesoAtual": 78.4,
                                  "bioimpedancia": 20.0,
                                  "dataAvaliacao": "2026-07-21",
                                  "observacao": "nao deve persistir"
                                }
                                """))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.erro").value("LEGACY_ENDPOINT_DISABLED"))
                .andExpect(jsonPath("$.campos").isEmpty());

        verify(permissaoService).exigirAdmin();
    }

    @Test
    void patchLegadoNaoAlteraEvolucao() throws Exception {
        PermissaoService permissaoService = mock(PermissaoService.class);
        MockMvc mvc = mvc(permissaoService);

        mvc.perform(patch("/Evolucao/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"observacao\":\"alteracao indevida\"}"))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.erro").value("LEGACY_ENDPOINT_DISABLED"));

        verify(permissaoService).exigirAdmin();
    }

    @Test
    void deleteLegadoNaoRemoveEvolucao() throws Exception {
        PermissaoService permissaoService = mock(PermissaoService.class);
        MockMvc mvc = mvc(permissaoService);

        mvc.perform(delete("/Evolucao/1"))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.erro").value("LEGACY_ENDPOINT_DISABLED"));

        verify(permissaoService).exigirAdmin();
    }

    @Test
    void perfilSemAdminContinuaBloqueadoNoLegado() {
        PermissaoService permissaoService = mock(PermissaoService.class);
        doThrow(new AccessDeniedException("Acesso permitido apenas para ADMIN"))
                .when(permissaoService).exigirAdmin();
        EvolucaoController controller = new EvolucaoController(permissaoService);

        org.junit.jupiter.api.Assertions.assertThrows(
                AccessDeniedException.class,
                controller::endpointLegadoDesativado
        );
    }

    private MockMvc mvc(PermissaoService permissaoService) {
        return MockMvcBuilders.standaloneSetup(new EvolucaoController(permissaoService)).build();
    }
}
