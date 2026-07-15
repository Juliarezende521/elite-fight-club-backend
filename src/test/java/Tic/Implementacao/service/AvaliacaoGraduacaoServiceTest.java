package Tic.Implementacao.service;

import Tic.Implementacao.dto.graduacao.avaliacao.*;
import Tic.Implementacao.model.*;
import Tic.Implementacao.repository.*;
import Tic.Implementacao.security.PermissaoService;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AvaliacaoGraduacaoServiceTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 7, 14);

    @Test
    void registraPresencaValida() {
        Fixture fx = fixture(PerfilUsuario.PROFESSOR);
        when(fx.avaliacaoRepository.findByAcademiaIdAndInscricaoIdForUpdate(1L, 70L)).thenReturn(Optional.empty());

        AvaliacaoGraduacaoResponse response = fx.service.registrarPresenca(100L, 70L, presenca(StatusPresencaGraduacao.PRESENTE, "Compareceu"));

        assertEquals(StatusPresencaGraduacao.PRESENTE, response.getStatusPresenca());
        assertEquals(StatusInscricaoGraduacao.EM_AVALIACAO, fx.inscricao.getStatus());
        verify(fx.avaliacaoRepository, times(2)).save(any(AvaliacaoGraduacao.class));
    }

    @Test
    void registraAusenciaSemPromover() {
        Fixture fx = fixture(PerfilUsuario.PROFESSOR);
        when(fx.avaliacaoRepository.findByAcademiaIdAndInscricaoIdForUpdate(1L, 70L)).thenReturn(Optional.empty());

        AvaliacaoGraduacaoResponse response = fx.service.registrarPresenca(100L, 70L, presenca(StatusPresencaGraduacao.AUSENTE, "Não veio"));

        assertEquals(StatusPresencaGraduacao.AUSENTE, response.getStatusPresenca());
        assertEquals(ResultadoAvaliacaoGraduacao.NAO_AVALIADO, response.getResultado());
        assertEquals(StatusInscricaoGraduacao.AUSENTE, fx.inscricao.getStatus());
        assertEquals(fx.atual, fx.aluno.getGraduacaoEntidade());
    }

    @Test
    void presencaRepetidaReusaAvaliacao() {
        Fixture fx = fixture(PerfilUsuario.PROFESSOR);
        AvaliacaoGraduacao avaliacao = avaliacao(fx);
        avaliacao.setStatusPresenca(StatusPresencaGraduacao.PRESENTE);
        when(fx.avaliacaoRepository.findByAcademiaIdAndInscricaoIdForUpdate(1L, 70L)).thenReturn(Optional.of(avaliacao));

        AvaliacaoGraduacaoResponse response = fx.service.registrarPresenca(100L, 70L, presenca(StatusPresencaGraduacao.PRESENTE, null));

        assertEquals(avaliacao.getId(), response.getId());
        verify(fx.avaliacaoRepository, never()).save(argThat(a -> a.getId() == null));
    }

    @Test
    void bloqueiaInscricaoCancelada() {
        Fixture fx = fixture(PerfilUsuario.PROFESSOR);
        fx.inscricao.setStatus(StatusInscricaoGraduacao.CANCELADA);

        assertThrows(RuntimeException.class, () -> fx.service.registrarPresenca(100L, 70L, presenca(StatusPresencaGraduacao.PRESENTE, "x")));
    }

    @Test
    void bloqueiaProfessorComPagamentoPendenteEAdminComJustificativaPodeAvaliar() {
        Fixture professor = fixture(PerfilUsuario.PROFESSOR);
        professor.inscricao.setStatus(StatusInscricaoGraduacao.PENDENTE_PAGAMENTO);
        assertThrows(RuntimeException.class, () -> professor.service.registrarPresenca(100L, 70L, presenca(StatusPresencaGraduacao.PRESENTE, "")));

        Fixture admin = fixture(PerfilUsuario.ADMIN);
        admin.inscricao.setStatus(StatusInscricaoGraduacao.PENDENTE_PAGAMENTO);
        when(admin.avaliacaoRepository.findByAcademiaIdAndInscricaoIdForUpdate(1L, 70L)).thenReturn(Optional.empty());
        AvaliacaoGraduacaoResponse response = admin.service.registrarPresenca(100L, 70L,
                presenca(StatusPresencaGraduacao.PRESENTE, "Autorização administrativa"));
        assertEquals(StatusPresencaGraduacao.PRESENTE, response.getStatusPresenca());
    }

    @Test
    void inscricaoGratuitaConfirmadaPodeAvaliar() {
        Fixture fx = fixture(PerfilUsuario.PROFESSOR);
        fx.inscricao.setValor(BigDecimal.ZERO);
        fx.inscricao.setStatus(StatusInscricaoGraduacao.CONFIRMADA);
        when(fx.avaliacaoRepository.findByAcademiaIdAndInscricaoIdForUpdate(1L, 70L)).thenReturn(Optional.empty());

        assertEquals(StatusPresencaGraduacao.PRESENTE,
                fx.service.registrarPresenca(100L, 70L, presenca(StatusPresencaGraduacao.PRESENTE, null)).getStatusPresenca());
    }

    @Test
    void avaliacaoAprovadaNaoPromoveAutomaticamente() {
        Fixture fx = fixture(PerfilUsuario.PROFESSOR);
        AvaliacaoGraduacao avaliacao = avaliacao(fx);
        avaliacao.setStatusPresenca(StatusPresencaGraduacao.PRESENTE);
        when(fx.avaliacaoRepository.findByAcademiaIdAndInscricaoIdForUpdate(1L, 70L)).thenReturn(Optional.of(avaliacao));

        AvaliacaoGraduacaoResponse response = fx.service.registrarAvaliacao(100L, 70L, avaliacaoRequest(ResultadoAvaliacaoGraduacao.APROVADO, "Aprovado", BigDecimal.valueOf(8.5)));

        assertEquals(ResultadoAvaliacaoGraduacao.APROVADO, response.getResultado());
        assertFalse(response.getPromocaoConfirmada());
        assertEquals(fx.atual, fx.aluno.getGraduacaoEntidade());
        verify(fx.historicoRepository, never()).save(any());
    }

    @Test
    void reprovadoSemObservacaoEAvaliacaoSemPresencaEBloqueiosNota() {
        Fixture fx = fixture(PerfilUsuario.PROFESSOR);
        AvaliacaoGraduacao avaliacao = avaliacao(fx);
        when(fx.avaliacaoRepository.findByAcademiaIdAndInscricaoIdForUpdate(1L, 70L)).thenReturn(Optional.of(avaliacao));

        assertThrows(RuntimeException.class, () -> fx.service.registrarAvaliacao(100L, 70L,
                avaliacaoRequest(ResultadoAvaliacaoGraduacao.REPROVADO, "", BigDecimal.valueOf(7))));
        assertThrows(RuntimeException.class, () -> fx.service.registrarAvaliacao(100L, 70L,
                avaliacaoRequest(ResultadoAvaliacaoGraduacao.APROVADO, "ok", BigDecimal.valueOf(11))));

        avaliacao.setStatusPresenca(StatusPresencaGraduacao.AUSENTE);
        assertThrows(RuntimeException.class, () -> fx.service.registrarAvaliacao(100L, 70L,
                avaliacaoRequest(ResultadoAvaliacaoGraduacao.APROVADO, "ok", BigDecimal.valueOf(7))));
    }

    @Test
    void avaliacaoReprovadaNaoPodePromover() {
        Fixture fx = fixture(PerfilUsuario.PROFESSOR);
        AvaliacaoGraduacao avaliacao = avaliacao(fx);
        avaliacao.setStatusPresenca(StatusPresencaGraduacao.PRESENTE);
        avaliacao.setResultado(ResultadoAvaliacaoGraduacao.REPROVADO);
        when(fx.avaliacaoRepository.findByAcademiaIdAndInscricaoIdForUpdate(1L, 70L)).thenReturn(Optional.of(avaliacao));

        assertThrows(RuntimeException.class, () -> fx.service.promoverAluno(100L, 70L, promover("Não deve")));
    }

    @Test
    void promocaoAprovadaCriaHistoricoUmaVezERepetidaEhIdempotente() {
        Fixture fx = fixture(PerfilUsuario.PROFESSOR);
        AvaliacaoGraduacao avaliacao = avaliacao(fx);
        avaliacao.setStatusPresenca(StatusPresencaGraduacao.PRESENTE);
        avaliacao.setResultado(ResultadoAvaliacaoGraduacao.APROVADO);
        when(fx.avaliacaoRepository.findByAcademiaIdAndInscricaoIdForUpdate(1L, 70L)).thenReturn(Optional.of(avaliacao));
        when(fx.alunoRepository.findByIdAndAcademiaIdForUpdate(20L, 1L)).thenReturn(Optional.of(fx.aluno));
        when(fx.historicoRepository.save(any(HistoricoGraduacao.class))).thenAnswer(invocation -> {
            HistoricoGraduacao h = invocation.getArgument(0);
            h.setId(500L);
            return h;
        });

        AvaliacaoGraduacaoResponse response = fx.service.promoverAluno(100L, 70L, promover("Promoção confirmada"));
        assertTrue(response.getPromocaoConfirmada());
        assertEquals("Azul", fx.aluno.getGraduacaoEntidade().getNome());
        verify(fx.historicoRepository, times(1)).save(any(HistoricoGraduacao.class));

        fx.service.promoverAluno(100L, 70L, promover("Repetida"));
        verify(fx.historicoRepository, times(1)).save(any(HistoricoGraduacao.class));
    }

    @Test
    void bloqueiaPromocaoSeGraduacaoMudouOuDestinoInativo() {
        Fixture fx = fixture(PerfilUsuario.PROFESSOR);
        AvaliacaoGraduacao avaliacao = avaliacao(fx);
        avaliacao.setStatusPresenca(StatusPresencaGraduacao.PRESENTE);
        avaliacao.setResultado(ResultadoAvaliacaoGraduacao.APROVADO);
        fx.aluno.setGraduacaoEntidade(fx.destino);
        when(fx.avaliacaoRepository.findByAcademiaIdAndInscricaoIdForUpdate(1L, 70L)).thenReturn(Optional.of(avaliacao));
        when(fx.alunoRepository.findByIdAndAcademiaIdForUpdate(20L, 1L)).thenReturn(Optional.of(fx.aluno));
        assertThrows(RuntimeException.class, () -> fx.service.promoverAluno(100L, 70L, promover("Conflito")));

        Fixture inativa = fixture(PerfilUsuario.PROFESSOR);
        AvaliacaoGraduacao avaliacaoInativa = avaliacao(inativa);
        avaliacaoInativa.setStatusPresenca(StatusPresencaGraduacao.PRESENTE);
        avaliacaoInativa.setResultado(ResultadoAvaliacaoGraduacao.APROVADO);
        inativa.destino.setAtiva(false);
        when(inativa.avaliacaoRepository.findByAcademiaIdAndInscricaoIdForUpdate(1L, 70L)).thenReturn(Optional.of(avaliacaoInativa));
        when(inativa.alunoRepository.findByIdAndAcademiaIdForUpdate(20L, 1L)).thenReturn(Optional.of(inativa.aluno));
        assertThrows(RuntimeException.class, () -> inativa.service.promoverAluno(100L, 70L, promover("Destino inativo")));
    }

    @Test
    void alunoNaoPodeAvaliar() {
        Fixture fx = fixture(PerfilUsuario.ALUNO);
        doThrow(new AccessDeniedException("Acesso permitido apenas para ADMIN ou PROFESSOR"))
                .when(fx.permissaoService).exigirAdminOuProfessor();

        assertThrows(AccessDeniedException.class,
                () -> fx.service.registrarPresenca(100L, 70L, presenca(StatusPresencaGraduacao.PRESENTE, "x")));
    }

    @Test
    void painelCalculaTotais() {
        Fixture fx = fixture(PerfilUsuario.PROFESSOR);
        AvaliacaoGraduacao avaliacao = avaliacao(fx);
        avaliacao.setStatusPresenca(StatusPresencaGraduacao.PRESENTE);
        avaliacao.setResultado(ResultadoAvaliacaoGraduacao.APROVADO);
        when(fx.avaliacaoRepository.findByAcademiaIdAndEventoId(1L, 100L)).thenReturn(List.of(avaliacao));

        PainelEventoGraduacaoResponse response = fx.service.painel(100L);

        assertEquals(1, response.getTotalInscritos());
        assertEquals(1, response.getPresentes());
        assertEquals(1, response.getAprovados());
        assertEquals(1, response.getAprovadosAguardandoPromocao());
    }

    @Test
    void alunoConsultaSomentePropriasAvaliacoes() {
        Fixture fx = fixture(PerfilUsuario.ALUNO);
        AvaliacaoGraduacao avaliacao = avaliacao(fx);
        when(fx.avaliacaoRepository.findByAcademiaIdAndAlunoIdOrderByEventoDataEventoDesc(1L, 20L)).thenReturn(List.of(avaliacao));
        when(fx.avaliacaoRepository.findByAcademiaIdAndIdAndAlunoId(1L, 300L, 20L)).thenReturn(Optional.of(avaliacao));

        assertEquals(1, fx.service.listarAvaliacoesAlunoAtual().size());
        assertEquals(300L, fx.service.buscarAvaliacaoAlunoAtual(300L).getId());
    }

    @Test
    void conclusaoBloqueiaAprovadoPendenteEMarcaSemPresencaComoAusente() {
        Fixture fx = fixture(PerfilUsuario.PROFESSOR);
        fx.evento.setDataEvento(HOJE.minusDays(1));
        AvaliacaoGraduacao avaliacao = avaliacao(fx);
        avaliacao.setStatusPresenca(StatusPresencaGraduacao.PRESENTE);
        avaliacao.setResultado(ResultadoAvaliacaoGraduacao.APROVADO);
        when(fx.avaliacaoRepository.findByAcademiaIdAndInscricaoIdForUpdate(1L, 70L)).thenReturn(Optional.of(avaliacao));
        assertThrows(RuntimeException.class, () -> fx.service.concluirEvento(100L, concluir(false)));

        Fixture resolvido = fixture(PerfilUsuario.PROFESSOR);
        resolvido.evento.setDataEvento(HOJE.minusDays(1));
        when(resolvido.avaliacaoRepository.findByAcademiaIdAndInscricaoIdForUpdate(1L, 70L)).thenReturn(Optional.empty());
        PainelEventoGraduacaoResponse response = resolvido.service.concluirEvento(100L, concluir(true));
        assertEquals(StatusEventoGraduacao.CONCLUIDO, response.getStatus());
        assertEquals(StatusInscricaoGraduacao.AUSENTE, resolvido.inscricao.getStatus());
    }

    private Fixture fixture(PerfilUsuario perfil) {
        AvaliacaoGraduacaoRepository avaliacaoRepository = mock(AvaliacaoGraduacaoRepository.class);
        EventoGraduacaoRepository eventoRepository = mock(EventoGraduacaoRepository.class);
        InscricaoGraduacaoRepository inscricaoRepository = mock(InscricaoGraduacaoRepository.class);
        EventoGraduacaoPrajiedRepository prajiedRepository = mock(EventoGraduacaoPrajiedRepository.class);
        AlunoRepository alunoRepository = mock(AlunoRepository.class);
        HistoricoGraduacaoRepository historicoRepository = mock(HistoricoGraduacaoRepository.class);
        NotificacaoService notificacaoService = mock(NotificacaoService.class);
        PermissaoService permissaoService = mock(PermissaoService.class);
        Clock clock = Clock.fixed(HOJE.atTime(12, 0).atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());

        Academia academia = academia();
        Graduacao atual = graduacao(1L, "Branco", 1, academia);
        Graduacao destino = graduacao(2L, "Azul", 2, academia);
        Aluno aluno = aluno(academia, atual);
        Usuario usuario = usuario(perfil, academia, aluno);
        EventoGraduacao evento = evento(academia, usuario);
        EventoGraduacaoPrajied item = item(evento, destino);
        InscricaoGraduacao inscricao = inscricao(evento, aluno, destino);

        when(permissaoService.academiaAtualId()).thenReturn(1L);
        when(permissaoService.usuarioAtual()).thenReturn(usuario);
        when(eventoRepository.findByIdAndAcademiaIdForUpdate(100L, 1L)).thenReturn(Optional.of(evento));
        when(eventoRepository.findByIdAndAcademiaId(100L, 1L)).thenReturn(Optional.of(evento));
        when(inscricaoRepository.findByIdAndAcademiaIdAndEventoIdForUpdate(70L, 1L, 100L)).thenReturn(Optional.of(inscricao));
        when(inscricaoRepository.findByAcademiaIdAndEventoIdOrderByInscritoEmDesc(1L, 100L)).thenReturn(List.of(inscricao));
        when(prajiedRepository.findByEventoIdAndGraduacaoDestinoIdAndAtivoTrue(100L, 2L)).thenReturn(Optional.of(item));
        when(avaliacaoRepository.save(any(AvaliacaoGraduacao.class))).thenAnswer(invocation -> {
            AvaliacaoGraduacao avaliacao = invocation.getArgument(0);
            if (avaliacao.getId() == null) {
                avaliacao.setId(300L);
            }
            return avaliacao;
        });
        when(inscricaoRepository.save(any(InscricaoGraduacao.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(eventoRepository.save(any(EventoGraduacao.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(alunoRepository.save(any(Aluno.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AvaliacaoGraduacaoService service = new AvaliacaoGraduacaoService(avaliacaoRepository, eventoRepository,
                inscricaoRepository, prajiedRepository, alunoRepository, historicoRepository, notificacaoService,
                permissaoService, clock);
        return new Fixture(service, avaliacaoRepository, eventoRepository, inscricaoRepository, prajiedRepository,
                alunoRepository, historicoRepository, notificacaoService, permissaoService, academia, atual, destino, aluno, usuario, evento, item, inscricao);
    }

    private RegistrarPresencaGraduacaoRequest presenca(StatusPresencaGraduacao status, String observacao) {
        RegistrarPresencaGraduacaoRequest request = new RegistrarPresencaGraduacaoRequest();
        request.setStatus(status);
        request.setObservacao(observacao);
        return request;
    }

    private RegistrarAvaliacaoGraduacaoRequest avaliacaoRequest(ResultadoAvaliacaoGraduacao resultado, String observacao, BigDecimal nota) {
        RegistrarAvaliacaoGraduacaoRequest request = new RegistrarAvaliacaoGraduacaoRequest();
        request.setResultado(resultado);
        request.setObservacaoTecnica(observacao);
        request.setNotaTecnica(nota);
        return request;
    }

    private PromoverAlunoEventoRequest promover(String observacao) {
        PromoverAlunoEventoRequest request = new PromoverAlunoEventoRequest();
        request.setObservacao(observacao);
        return request;
    }

    private ConcluirEventoGraduacaoRequest concluir(boolean confirmarPendencias) {
        ConcluirEventoGraduacaoRequest request = new ConcluirEventoGraduacaoRequest();
        request.setConfirmarPendencias(confirmarPendencias);
        return request;
    }

    private AvaliacaoGraduacao avaliacao(Fixture fx) {
        AvaliacaoGraduacao avaliacao = new AvaliacaoGraduacao();
        avaliacao.setId(300L);
        avaliacao.setAcademia(fx.academia);
        avaliacao.setInscricao(fx.inscricao);
        avaliacao.setEvento(fx.evento);
        avaliacao.setAluno(fx.aluno);
        avaliacao.setGraduacaoAtual(fx.atual);
        avaliacao.setGraduacaoDestino(fx.destino);
        avaliacao.setStatusPresenca(StatusPresencaGraduacao.PENDENTE);
        avaliacao.setResultado(ResultadoAvaliacaoGraduacao.PENDENTE);
        avaliacao.setPromocaoConfirmada(false);
        avaliacao.setHistoricoGeradoFlag(false);
        avaliacao.setCriadoEm(HOJE.atTime(12, 0));
        avaliacao.setAtualizadoEm(HOJE.atTime(12, 0));
        return avaliacao;
    }

    private Academia academia() {
        Academia academia = new Academia();
        academia.setId(1L);
        academia.setNome("Elite");
        return academia;
    }

    private Graduacao graduacao(Long id, String nome, int ordem, Academia academia) {
        Graduacao graduacao = new Graduacao();
        graduacao.setId(id);
        graduacao.setNome(nome);
        graduacao.setCorHex("#FFFFFF");
        graduacao.setOrdem(ordem);
        graduacao.setCheckinsNecessarios(10);
        graduacao.setTempoMinimoMeses(0);
        graduacao.setValorPadraoGraduacao(BigDecimal.ZERO);
        graduacao.setAtiva(true);
        graduacao.setAcademia(academia);
        return graduacao;
    }

    private Aluno aluno(Academia academia, Graduacao graduacao) {
        Aluno aluno = new Aluno();
        aluno.setId(20L);
        aluno.setNome("Aluno");
        aluno.setAtivo(true);
        aluno.setAcademia(academia);
        aluno.setGraduacaoEntidade(graduacao);
        aluno.setGraduacao(graduacao.getNome());
        return aluno;
    }

    private Usuario usuario(PerfilUsuario perfil, Academia academia, Aluno aluno) {
        Usuario usuario = new Usuario();
        usuario.setId(30L);
        usuario.setNome("Usuário");
        usuario.setEmail("usuario@email.com");
        usuario.setPerfil(perfil);
        usuario.setAcademia(academia);
        if (perfil == PerfilUsuario.ALUNO) {
            usuario.setAluno(aluno);
        }
        return usuario;
    }

    private EventoGraduacao evento(Academia academia, Usuario usuario) {
        EventoGraduacao evento = new EventoGraduacao();
        evento.setId(100L);
        evento.setAcademia(academia);
        evento.setTitulo("Graduação Elite");
        evento.setDataEvento(HOJE);
        evento.setHoraInicio(LocalTime.of(9, 0));
        evento.setHoraFim(LocalTime.of(12, 0));
        evento.setLocal("Elite");
        evento.setInicioInscricoes(HOJE.minusDays(10).atStartOfDay());
        evento.setFimInscricoes(HOJE.atTime(9, 0));
        evento.setStatus(StatusEventoGraduacao.PUBLICADO);
        evento.setCriadoPor(usuario);
        evento.setCriadoEm(HOJE.minusDays(10).atStartOfDay());
        evento.setAtualizadoEm(HOJE.minusDays(10).atStartOfDay());
        return evento;
    }

    private EventoGraduacaoPrajied item(EventoGraduacao evento, Graduacao destino) {
        EventoGraduacaoPrajied item = new EventoGraduacaoPrajied();
        item.setId(40L);
        item.setEvento(evento);
        item.setGraduacaoDestino(destino);
        item.setValor(BigDecimal.valueOf(120));
        item.setAtivo(true);
        return item;
    }

    private InscricaoGraduacao inscricao(EventoGraduacao evento, Aluno aluno, Graduacao destino) {
        InscricaoGraduacao inscricao = new InscricaoGraduacao();
        inscricao.setId(70L);
        inscricao.setAcademia(evento.getAcademia());
        inscricao.setEvento(evento);
        inscricao.setAluno(aluno);
        inscricao.setGraduacaoDestino(destino);
        inscricao.setValor(BigDecimal.valueOf(120));
        inscricao.setStatus(StatusInscricaoGraduacao.PAGA);
        inscricao.setInscritoEm(HOJE.minusDays(1).atTime(12, 0));
        return inscricao;
    }

    private record Fixture(AvaliacaoGraduacaoService service,
                           AvaliacaoGraduacaoRepository avaliacaoRepository,
                           EventoGraduacaoRepository eventoRepository,
                           InscricaoGraduacaoRepository inscricaoRepository,
                           EventoGraduacaoPrajiedRepository prajiedRepository,
                           AlunoRepository alunoRepository,
                           HistoricoGraduacaoRepository historicoRepository,
                           NotificacaoService notificacaoService,
                           PermissaoService permissaoService,
                           Academia academia,
                           Graduacao atual,
                           Graduacao destino,
                           Aluno aluno,
                           Usuario usuario,
                           EventoGraduacao evento,
                           EventoGraduacaoPrajied item,
                           InscricaoGraduacao inscricao) {
    }
}
