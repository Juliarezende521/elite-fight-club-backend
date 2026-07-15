package Tic.Implementacao.service;

import Tic.Implementacao.dto.graduacao.GraduacaoRequest;
import Tic.Implementacao.dto.graduacao.GraduacaoResponse;
import Tic.Implementacao.dto.graduacao.ItemReordenacaoGraduacaoRequest;
import Tic.Implementacao.dto.graduacao.ReordenarGraduacoesRequest;
import Tic.Implementacao.model.Academia;
import Tic.Implementacao.model.Graduacao;
import Tic.Implementacao.repository.AlunoRepository;
import Tic.Implementacao.repository.GraduacaoRepository;
import Tic.Implementacao.security.PermissaoService;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GraduacaoServiceTest {

    @Test
    void cadastraGraduacaoValida() {
        Fixture fx = fixture();
        when(fx.repository.save(any(Graduacao.class))).thenAnswer(invocation -> {
            Graduacao graduacao = invocation.getArgument(0);
            graduacao.setId(10L);
            return graduacao;
        });

        GraduacaoResponse response = fx.service.salvar(request("Amarelo", 2));

        assertEquals("Amarelo", response.getNome());
        assertEquals(BigDecimal.valueOf(120), response.getValorPadraoGraduacao());
        verify(fx.repository).save(any(Graduacao.class));
    }

    @Test
    void bloqueiaCorInvalida() {
        Fixture fx = fixture();
        GraduacaoRequest request = request("Amarelo", 2);
        request.setCorHex("bg-yellow-500");

        assertThrows(IllegalArgumentException.class, () -> fx.service.salvar(request));
        verify(fx.repository, never()).save(any());
    }

    @Test
    void bloqueiaNomeDuplicado() {
        Fixture fx = fixture();
        when(fx.repository.existsByAcademiaIdAndNomeIgnoreCase(1L, "Amarelo")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> fx.service.salvar(request("Amarelo", 2)));

        assertTrue(ex.getMessage().contains("nome"));
        verify(fx.repository, never()).save(any());
    }

    @Test
    void bloqueiaOrdemDuplicada() {
        Fixture fx = fixture();
        when(fx.repository.existsByAcademiaIdAndOrdem(1L, 2)).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> fx.service.salvar(request("Amarelo", 2)));

        assertTrue(ex.getMessage().contains("ordem"));
        verify(fx.repository, never()).save(any());
    }

    @Test
    void bloqueiaValorNegativoTempoNegativoECheckinsNegativos() {
        Fixture fx = fixture();

        GraduacaoRequest valorNegativo = request("Amarelo", 2);
        valorNegativo.setValorPadraoGraduacao(BigDecimal.valueOf(-1));
        assertThrows(IllegalArgumentException.class, () -> fx.service.salvar(valorNegativo));

        GraduacaoRequest tempoNegativo = request("Laranja", 3);
        tempoNegativo.setTempoMinimoMeses(-1);
        assertThrows(IllegalArgumentException.class, () -> fx.service.salvar(tempoNegativo));

        GraduacaoRequest checkinsNegativos = request("Verde", 4);
        checkinsNegativos.setCheckinsNecessarios(-1);
        assertThrows(IllegalArgumentException.class, () -> fx.service.salvar(checkinsNegativos));
    }

    @Test
    void permiteGraduacaoBaseComMetaZero() {
        Fixture fx = fixture();
        when(fx.repository.save(any(Graduacao.class))).thenAnswer(invocation -> invocation.getArgument(0));
        GraduacaoRequest request = request("Branco", 1);
        request.setGraduacaoBase(true);
        request.setCheckinsNecessarios(0);
        request.setValorPadraoGraduacao(BigDecimal.ZERO);

        GraduacaoResponse response = fx.service.salvar(request);

        assertTrue(response.getGraduacaoBase());
        assertEquals(0, response.getCheckinsNecessarios());
    }

    @Test
    void bloqueiaSegundaGraduacaoBaseAtiva() {
        Fixture fx = fixture();
        when(fx.repository.existsByAcademiaIdAndGraduacaoBaseTrueAndAtivaTrue(1L)).thenReturn(true);
        GraduacaoRequest request = request("Outra Base", 2);
        request.setGraduacaoBase(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> fx.service.salvar(request));

        assertTrue(ex.getMessage().contains("base ativa"));
        verify(fx.repository, never()).save(any());
    }

    @Test
    void bloqueiaExclusaoDaBase() {
        Fixture fx = fixture();
        when(fx.repository.findByIdAndAcademiaId(10L, 1L)).thenReturn(Optional.of(graduacao(10L, "Branco", 1, true)));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> fx.service.remover(10L));

        assertTrue(ex.getMessage().contains("base"));
        verify(fx.repository, never()).deleteById(anyLong());
    }

    @Test
    void bloqueiaExclusaoComAlunoVinculado() {
        Fixture fx = fixture();
        when(fx.repository.findByIdAndAcademiaId(10L, 1L)).thenReturn(Optional.of(graduacao(10L, "Azul", 2, false)));
        when(fx.alunoRepository.existsByAcademiaIdAndGraduacaoId(1L, 10L)).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> fx.service.remover(10L));

        assertTrue(ex.getMessage().contains("aluno"));
        verify(fx.repository, never()).deleteById(anyLong());
    }

    @Test
    void desativaGraduacaoNaoBase() {
        Fixture fx = fixture();
        when(fx.repository.findByIdAndAcademiaId(10L, 1L)).thenReturn(Optional.of(graduacao(10L, "Azul", 2, false)));
        when(fx.repository.save(any(Graduacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GraduacaoResponse response = fx.service.alterarStatus(10L, false);

        assertFalse(response.getAtiva());
    }

    @Test
    void reordenaGraduacoesValidas() {
        Fixture fx = fixture();
        Graduacao branco = graduacao(1L, "Branco", 1, true);
        Graduacao azul = graduacao(2L, "Azul", 2, false);
        when(fx.repository.findByIdInAndAcademiaId(anyCollection(), eq(1L))).thenReturn(List.of(branco, azul));
        when(fx.repository.findByAcademiaIdOrderByOrdemAsc(1L)).thenReturn(List.of(azul, branco));

        List<GraduacaoResponse> response = fx.service.reordenar(reordenar(item(1L, 2), item(2L, 1)));

        assertEquals(2, response.size());
        verify(fx.repository, times(4)).atualizarOrdem(anyLong(), eq(1L), anyInt());
    }

    @Test
    void bloqueiaReordenacaoComDuplicidadeDeOrdem() {
        Fixture fx = fixture();

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> fx.service.reordenar(reordenar(item(1L, 1), item(2L, 1))));

        assertTrue(ex.getMessage().contains("ordem"));
        verify(fx.repository, never()).atualizarOrdem(anyLong(), anyLong(), anyInt());
    }

    @Test
    void buscaProximaGraduacaoPelaOrdemAtivaDaMesmaAcademia() {
        Fixture fx = fixture();
        Graduacao atual = graduacao(1L, "Branco", 1, true);
        Graduacao proxima = graduacao(2L, "Azul", 2, false);
        when(fx.repository.findFirstByAcademiaIdAndAtivaTrueAndOrdemGreaterThanOrderByOrdemAsc(1L, 1))
                .thenReturn(Optional.of(proxima));

        Optional<Graduacao> resultado = fx.service.buscarProximaGraduacao(1L, atual);

        assertTrue(resultado.isPresent());
        assertEquals("Azul", resultado.get().getNome());
    }

    @Test
    void graduacaoMaisAltaNaoTemProxima() {
        Fixture fx = fixture();
        Graduacao atual = graduacao(3L, "Preto", 10, false);
        when(fx.repository.findFirstByAcademiaIdAndAtivaTrueAndOrdemGreaterThanOrderByOrdemAsc(1L, 10))
                .thenReturn(Optional.empty());

        assertTrue(fx.service.buscarProximaGraduacao(1L, atual).isEmpty());
    }

    @Test
    void respeitaIsolamentoPorAcademia() {
        Fixture fx = fixture();
        when(fx.repository.findByIdAndAcademiaId(10L, 1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> fx.service.buscarPorId(10L));

        assertTrue(ex.getMessage().contains("não encontrada"));
    }

    @Test
    void alunoNaoPodeEditarGraduacao() {
        Fixture fx = fixture();
        doThrow(new AccessDeniedException("Acesso permitido apenas para ADMIN ou PROFESSOR"))
                .when(fx.permissaoService).exigirAdminOuProfessor();

        assertThrows(AccessDeniedException.class, () -> fx.service.salvar(request("Amarelo", 2)));
        verify(fx.repository, never()).save(any());
    }

    private Fixture fixture() {
        GraduacaoRepository repository = mock(GraduacaoRepository.class);
        AlunoRepository alunoRepository = mock(AlunoRepository.class);
        PermissaoService permissaoService = mock(PermissaoService.class);
        Academia academia = new Academia();
        academia.setId(1L);
        academia.setNome("Elite");
        when(permissaoService.academiaAtual()).thenReturn(academia);
        when(permissaoService.academiaAtualId()).thenReturn(1L);
        GraduacaoService service = new GraduacaoService(repository, alunoRepository, permissaoService);
        return new Fixture(repository, alunoRepository, permissaoService, service);
    }

    private GraduacaoRequest request(String nome, Integer ordem) {
        GraduacaoRequest request = new GraduacaoRequest();
        request.setNome(nome);
        request.setCorHex("#FFD700");
        request.setOrdem(ordem);
        request.setCheckinsNecessarios(30);
        request.setTempoMinimoMeses(3);
        request.setValorPadraoGraduacao(BigDecimal.valueOf(120));
        request.setDescricao("Descrição da graduação");
        request.setGraduacaoBase(false);
        request.setAtiva(true);
        return request;
    }

    private Graduacao graduacao(Long id, String nome, Integer ordem, boolean base) {
        Academia academia = new Academia();
        academia.setId(1L);
        Graduacao graduacao = new Graduacao();
        graduacao.setId(id);
        graduacao.setNome(nome);
        graduacao.setCorHex("#FFFFFF");
        graduacao.setOrdem(ordem);
        graduacao.setCheckinsNecessarios(base ? 0 : 30);
        graduacao.setTempoMinimoMeses(0);
        graduacao.setValorPadraoGraduacao(BigDecimal.ZERO);
        graduacao.setGraduacaoBase(base);
        graduacao.setAtiva(true);
        graduacao.setAcademia(academia);
        return graduacao;
    }

    private ItemReordenacaoGraduacaoRequest item(Long id, Integer ordem) {
        ItemReordenacaoGraduacaoRequest item = new ItemReordenacaoGraduacaoRequest();
        item.setId(id);
        item.setOrdem(ordem);
        return item;
    }

    private ReordenarGraduacoesRequest reordenar(ItemReordenacaoGraduacaoRequest... itens) {
        ReordenarGraduacoesRequest request = new ReordenarGraduacoesRequest();
        request.setGraduacoes(List.of(itens));
        return request;
    }

    private record Fixture(GraduacaoRepository repository,
                           AlunoRepository alunoRepository,
                           PermissaoService permissaoService,
                           GraduacaoService service) {
    }
}
