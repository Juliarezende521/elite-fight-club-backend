package Tic.Implementacao.controller;

import Tic.Implementacao.exception.GlobalExceptionHandler;
import Tic.Implementacao.model.*;
import Tic.Implementacao.repository.ProfessorRepository;
import Tic.Implementacao.security.PermissaoService;
import Tic.Implementacao.service.ProfessorService;
import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.List;
import java.util.Optional;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProfessorSecurityTest {
    private ProfessorRepository repo;
    private MockMvc mvc;
    private Academia academia;
    private Professor professor;
    private static final String BODY = """
            {"nome":"Professor Teste","cpf":"00000000000","contato":"Contato sintetico"}
            """;

    @BeforeEach
    void preparar() {
        repo = mock(ProfessorRepository.class);
        academia = new Academia();
        academia.setId(1L);
        professor = new Professor();
        professor.setId(7L);
        professor.setNome("Professor Teste");
        professor.setCpf("00000000000");
        professor.setAcademia(academia);
        when(repo.findByIdAndAcademiaId(7L, 1L)).thenReturn(Optional.of(professor));
        when(repo.findAllByAcademiaId(1L)).thenReturn(List.of(professor));
        when(repo.save(any())).thenAnswer(invocation -> {
            Professor salvo = invocation.getArgument(0);
            if (salvo.getId() == null) salvo.setId(7L);
            return salvo;
        });
        mvc = MockMvcBuilders.standaloneSetup(new ProfessorController(
                new ProfessorService(repo, new PermissaoService())))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
        entrar(PerfilUsuario.ADMIN, academia);
    }

    private void entrar(PerfilUsuario perfil, Academia tenant) {
        Usuario usuario = new Usuario();
        usuario.setPerfil(perfil);
        usuario.setAcademia(tenant);
        usuario.setAtivo(true);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(usuario, null, List.of()));
    }

    @AfterEach
    void limpar() { SecurityContextHolder.clearContext(); }

    @Test
    void adminPodeCadastrarSemMassAssignment() throws Exception {
        mvc.perform(post("/professores").contentType(MediaType.APPLICATION_JSON)
                .content(BODY.replace("}", ",\"id\":99,\"academia\":{\"id\":2}}")))
                .andExpect(status().isCreated()).andExpect(header().string("Location", "/professores/7"))
                .andExpect(jsonPath("$.id").value(7)).andExpect(jsonPath("$.cpf").doesNotExist())
                .andExpect(jsonPath("$.academia").doesNotExist());
        verify(repo).save(argThat(p -> p.getAcademia().getId().equals(1L) && p.getId().equals(7L)));
    }

    @Test
    void adminPodeConsultarEditarExcluir() throws Exception {
        mvc.perform(get("/professores")).andExpect(status().isOk()).andExpect(jsonPath("$[0].nome").value("Professor Teste"));
        mvc.perform(get("/professores/7")).andExpect(status().isOk());
        mvc.perform(patch("/professores/7").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk());
        mvc.perform(delete("/professores/7")).andExpect(status().isNoContent());
        verify(repo).delete(professor);
    }

    private void bloqueado(PerfilUsuario perfil) throws Exception {
        entrar(perfil, academia);
        mvc.perform(get("/professores")).andExpect(status().isForbidden());
        mvc.perform(get("/professores/7")).andExpect(status().isForbidden());
        mvc.perform(post("/professores").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isForbidden());
        mvc.perform(patch("/professores/7").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/professores/7")).andExpect(status().isForbidden());
        verifyNoInteractions(repo);
    }

    @Test void alunoBloqueado() throws Exception { bloqueado(PerfilUsuario.ALUNO); }
    @Test void professorBloqueado() throws Exception { bloqueado(PerfilUsuario.PROFESSOR); }

    @Test
    void outraAcademiaNaoConsultaNemAltera() throws Exception {
        Academia outra = new Academia();
        outra.setId(2L);
        entrar(PerfilUsuario.ADMIN, outra);
        when(repo.findByIdAndAcademiaId(7L, 2L)).thenReturn(Optional.empty());
        mvc.perform(get("/professores/7")).andExpect(status().isNotFound());
        mvc.perform(patch("/professores/7").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/professores/7")).andExpect(status().isNotFound());
        verify(repo, never()).save(any());
        verify(repo, never()).delete(any());
    }

    @Test
    void naoAutenticado() throws Exception {
        SecurityContextHolder.clearContext();
        mvc.perform(get("/professores")).andExpect(status().isUnauthorized());
        verifyNoInteractions(repo);
    }

    @Test
    void entradaInvalida() throws Exception {
        mvc.perform(post("/professores").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"\",\"cpf\":\"abc\"}")).andExpect(status().isBadRequest());
        verifyNoInteractions(repo);
    }

    @Test
    void inexistenteRetorna404() throws Exception {
        mvc.perform(get("/professores/999")).andExpect(status().isNotFound());
        mvc.perform(patch("/professores/999").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/professores/999")).andExpect(status().isNotFound());
        verify(repo, never()).save(any());
        verify(repo, never()).delete(any());
    }
}
