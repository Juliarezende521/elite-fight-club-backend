package Tic.Implementacao.service;

import Tic.Implementacao.dto.graduacao.ElegibilidadeGraduacaoResponse;
import Tic.Implementacao.dto.graduacao.convite.ConviteGraduacaoRequest;
import Tic.Implementacao.dto.graduacao.convite.ConviteGraduacaoResponse;
import Tic.Implementacao.dto.graduacao.evento.*;
import Tic.Implementacao.dto.graduacao.inscricao.InscricaoGraduacaoResponse;
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

class EventoGraduacaoServiceTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 7, 14);

    @Test
    void criaEventoValido() {
        Fixture fx = fixture(PerfilUsuario.PROFESSOR);
        when(fx.eventoRepository.save(any(EventoGraduacao.class))).thenAnswer(invocation -> {
            EventoGraduacao evento = invocation.getArgument(0);
            evento.setId(100L);
            return evento;
        });

        EventoGraduacaoResponse response = fx.service.criarEvento(eventoRequest());

        assertEquals("Graduação Elite", response.getTitulo());
        assertEquals(StatusEventoGraduacao.RASCUNHO, response.getStatus());
    }

    @Test
    void bloqueiaDataPassadaHorarioInvalidoEPrazoInvalido() {
        Fixture fx = fixture(PerfilUsuario.PROFESSOR);
        EventoGraduacaoRequest passado = eventoRequest();
        passado.setDataEvento(HOJE);
        assertThrows(RuntimeException.class, () -> fx.service.criarEvento(passado));

        EventoGraduacaoRequest horario = eventoRequest();
        horario.setHoraFim(horario.getHoraInicio());
        assertThrows(RuntimeException.class, () -> fx.service.criarEvento(horario));

        EventoGraduacaoRequest prazo = eventoRequest();
        prazo.setFimInscricoes(LocalDateTime.of(2026, 12, 12, 10, 0));
        assertThrows(RuntimeException.class, () -> fx.service.criarEvento(prazo));
    }

    @Test
    void publicacaoSemPrajiedBloqueada() {
        Fixture fx = fixture(PerfilUsuario.PROFESSOR);
        when(fx.prajiedRepository.findByEventoIdAndAtivoTrueOrderByGraduacaoDestinoOrdemAsc(100L)).thenReturn(List.of());

        assertThrows(RuntimeException.class, () -> fx.service.publicarEvento(100L, publicar(true)));
    }

    @Test
    void publicacaoValidaCriaConviteAutomaticoParaElegivelENaoParaNaoElegivel() {
        Fixture fx = fixture(PerfilUsuario.PROFESSOR);
        Aluno naoElegivel = aluno(21L, fx.academia, fx.atual);
        when(fx.prajiedRepository.findByEventoIdAndAtivoTrueOrderByGraduacaoDestinoOrdemAsc(100L)).thenReturn(List.of(fx.item));
        when(fx.alunoRepository.findByAtivoTrueAndAcademiaId(1L)).thenReturn(List.of(fx.aluno, naoElegivel));
        when(fx.graduacaoAlunoService.calcularElegibilidade(fx.aluno)).thenReturn(elegibilidade(fx.aluno, fx.atual, fx.destino, true));
        when(fx.graduacaoAlunoService.calcularElegibilidade(naoElegivel)).thenReturn(elegibilidade(naoElegivel, fx.atual, fx.destino, false));
        when(fx.conviteRepository.save(any(ConviteGraduacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EventoGraduacaoResponse response = fx.service.publicarEvento(100L, publicar(true));

        assertEquals(StatusEventoGraduacao.PUBLICADO, response.getStatus());
        verify(fx.conviteRepository, times(1)).save(any(ConviteGraduacao.class));
    }

    @Test
    void conviteManualElegivelECasoNaoElegivelSemJustificativa() {
        Fixture fx = fixture(PerfilUsuario.PROFESSOR);
        when(fx.prajiedRepository.findByEventoIdAndGraduacaoDestinoIdAndAtivoTrue(100L, 2L)).thenReturn(Optional.of(fx.item));
        when(fx.graduacaoAlunoService.calcularElegibilidade(fx.aluno)).thenReturn(elegibilidade(fx.aluno, fx.atual, fx.destino, true));
        when(fx.conviteRepository.save(any(ConviteGraduacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ConviteGraduacaoResponse response = fx.service.criarConviteManual(100L, conviteRequest(null));
        assertEquals("Azul", response.getGraduacaoDestinoNome());

        Fixture naoElegivel = fixture(PerfilUsuario.PROFESSOR);
        when(naoElegivel.prajiedRepository.findByEventoIdAndGraduacaoDestinoIdAndAtivoTrue(100L, 2L)).thenReturn(Optional.of(naoElegivel.item));
        when(naoElegivel.graduacaoAlunoService.calcularElegibilidade(naoElegivel.aluno))
                .thenReturn(elegibilidade(naoElegivel.aluno, naoElegivel.atual, naoElegivel.destino, false));
        assertThrows(RuntimeException.class, () -> naoElegivel.service.criarConviteManual(100L, conviteRequest(null)));
    }

    @Test
    void conviteDuplicadoBloqueadoEAlunoOutraAcademiaBloqueado() {
        Fixture fx = fixture(PerfilUsuario.PROFESSOR);
        when(fx.prajiedRepository.findByEventoIdAndGraduacaoDestinoIdAndAtivoTrue(100L, 2L)).thenReturn(Optional.of(fx.item));
        when(fx.graduacaoAlunoService.calcularElegibilidade(fx.aluno)).thenReturn(elegibilidade(fx.aluno, fx.atual, fx.destino, true));
        when(fx.conviteRepository.existsByAcademiaIdAndEventoIdAndAlunoIdAndGraduacaoDestinoId(1L, 100L, 20L, 2L)).thenReturn(true);

        assertThrows(RuntimeException.class, () -> fx.service.criarConviteManual(100L, conviteRequest("Manual")));

        when(fx.alunoRepository.findByIdAndAcademiaId(99L, 1L)).thenReturn(Optional.empty());
        ConviteGraduacaoRequest request = conviteRequest("Manual");
        request.setAlunoId(99L);
        assertThrows(RuntimeException.class, () -> fx.service.criarConviteManual(100L, request));
    }

    @Test
    void alunoAceitaERecusaConvite() {
        Fixture fx = fixture(PerfilUsuario.ALUNO);
        when(fx.conviteRepository.findByIdAndAcademiaIdAndAlunoId(50L, 1L, 20L)).thenReturn(Optional.of(fx.convite));
        when(fx.conviteRepository.save(any(ConviteGraduacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertEquals(StatusConviteGraduacao.ACEITO, fx.service.aceitarConvite(50L).getStatus());

        fx.convite.setStatus(StatusConviteGraduacao.PENDENTE);
        assertEquals(StatusConviteGraduacao.RECUSADO, fx.service.recusarConvite(50L).getStatus());
    }

    @Test
    void inscricaoValidaValorPositivoFicaPendenteEValorZeroConfirma() {
        Fixture fx = fixture(PerfilUsuario.ALUNO);
        prepararInscricao(fx);
        fx.item.setValor(BigDecimal.valueOf(120));
        when(fx.inscricaoRepository.save(any(InscricaoGraduacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InscricaoGraduacaoResponse pendente = fx.service.inscreverAlunoAtual(100L);
        assertEquals(StatusInscricaoGraduacao.PENDENTE_PAGAMENTO, pendente.getStatus());
        assertEquals(BigDecimal.valueOf(120), pendente.getValor());

        Fixture zero = fixture(PerfilUsuario.ALUNO);
        prepararInscricao(zero);
        zero.item.setValor(BigDecimal.ZERO);
        when(zero.inscricaoRepository.save(any(InscricaoGraduacao.class))).thenAnswer(invocation -> invocation.getArgument(0));
        assertEquals(StatusInscricaoGraduacao.CONFIRMADA, zero.service.inscreverAlunoAtual(100L).getStatus());
    }

    @Test
    void bloqueiaInscricaoDuplicadaEventoNaoPublicadoEInscricoesEncerradas() {
        Fixture fx = fixture(PerfilUsuario.ALUNO);
        prepararInscricao(fx);
        when(fx.inscricaoRepository.findFirstByAcademiaIdAndEventoIdAndAlunoIdAndStatusInOrderByInscritoEmDesc(eq(1L), eq(100L), eq(20L), any()))
                .thenReturn(Optional.of(fx.inscricao));
        assertThrows(RuntimeException.class, () -> fx.service.inscreverAlunoAtual(100L));

        Fixture rascunho = fixture(PerfilUsuario.ALUNO);
        prepararInscricao(rascunho);
        rascunho.evento.setStatus(StatusEventoGraduacao.RASCUNHO);
        assertThrows(RuntimeException.class, () -> rascunho.service.inscreverAlunoAtual(100L));

        Fixture encerrado = fixture(PerfilUsuario.ALUNO);
        prepararInscricao(encerrado);
        encerrado.evento.setFimInscricoes(LocalDateTime.of(2026, 7, 13, 23, 59));
        assertThrows(RuntimeException.class, () -> encerrado.service.inscreverAlunoAtual(100L));
    }

    @Test
    void bloqueiaLimiteGeralELimitePorPrajied() {
        Fixture fx = fixture(PerfilUsuario.ALUNO);
        prepararInscricao(fx);
        fx.evento.setLimiteParticipantes(1);
        when(fx.inscricaoRepository.countByAcademiaIdAndEventoIdAndStatusIn(eq(1L), eq(100L), any())).thenReturn(1L);
        assertThrows(RuntimeException.class, () -> fx.service.inscreverAlunoAtual(100L));

        Fixture porPrajied = fixture(PerfilUsuario.ALUNO);
        prepararInscricao(porPrajied);
        porPrajied.item.setLimiteVagas(1);
        when(porPrajied.inscricaoRepository.countByAcademiaIdAndEventoIdAndGraduacaoDestinoIdAndStatusIn(eq(1L), eq(100L), eq(2L), any()))
                .thenReturn(1L);
        assertThrows(RuntimeException.class, () -> porPrajied.service.inscreverAlunoAtual(100L));
    }

    @Test
    void usaLockParaProtegerUltimaVaga() {
        Fixture fx = fixture(PerfilUsuario.ALUNO);
        prepararInscricao(fx);
        when(fx.inscricaoRepository.save(any(InscricaoGraduacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        fx.service.inscreverAlunoAtual(100L);

        verify(fx.eventoRepository).findByIdAndAcademiaIdForUpdate(100L, 1L);
        verify(fx.prajiedRepository).findDisponivelForUpdate(100L, 2L);
    }

    @Test
    void cancelamentoEEventoCancelado() {
        Fixture fx = fixture(PerfilUsuario.ALUNO);
        fx.inscricao.setStatus(StatusInscricaoGraduacao.PENDENTE_PAGAMENTO);
        when(fx.inscricaoRepository.findByIdAndAcademiaIdAndAlunoId(70L, 1L, 20L)).thenReturn(Optional.of(fx.inscricao));
        when(fx.inscricaoRepository.save(any(InscricaoGraduacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertEquals(StatusInscricaoGraduacao.CANCELADA, fx.service.cancelarInscricaoAlunoAtual(70L).getStatus());

        Fixture admin = fixture(PerfilUsuario.PROFESSOR);
        when(admin.conviteRepository.findByAcademiaIdAndEventoIdAndStatusIn(eq(1L), eq(100L), any())).thenReturn(List.of(admin.convite));
        when(admin.eventoRepository.save(any(EventoGraduacao.class))).thenAnswer(invocation -> invocation.getArgument(0));
        assertEquals(StatusEventoGraduacao.CANCELADO, admin.service.cancelarEvento(100L).getStatus());
        assertEquals(StatusConviteGraduacao.CANCELADO, admin.convite.getStatus());
    }

    @Test
    void segurancaAlunoENaoPromoveGraduacao() {
        Fixture fx = fixture(PerfilUsuario.ALUNO);
        prepararInscricao(fx);
        when(fx.inscricaoRepository.save(any(InscricaoGraduacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        fx.service.inscreverAlunoAtual(100L);

        assertEquals(fx.atual, fx.aluno.getGraduacaoEntidade());
        verify(fx.alunoRepository, never()).save(any());

        Fixture professor = fixture(PerfilUsuario.PROFESSOR);
        assertThrows(AccessDeniedException.class, () -> professor.service.listarEventosDoAlunoAtual());
    }

    private void prepararInscricao(Fixture fx) {
        when(fx.eventoRepository.findByIdAndAcademiaIdForUpdate(100L, 1L)).thenReturn(Optional.of(fx.evento));
        when(fx.prajiedRepository.findDisponivelForUpdate(100L, 2L)).thenReturn(Optional.of(fx.item));
        when(fx.graduacaoAlunoService.calcularElegibilidade(fx.aluno)).thenReturn(elegibilidade(fx.aluno, fx.atual, fx.destino, true));
        when(fx.inscricaoRepository.findFirstByAcademiaIdAndEventoIdAndAlunoIdAndStatusInOrderByInscritoEmDesc(eq(1L), eq(100L), eq(20L), any()))
                .thenReturn(Optional.empty());
        when(fx.conviteRepository.findFirstByAcademiaIdAndEventoIdAndAlunoIdAndStatusInOrderByEnviadoEmDesc(eq(1L), eq(100L), eq(20L), any()))
                .thenReturn(Optional.empty());
    }

    private Fixture fixture(PerfilUsuario perfil) {
        EventoGraduacaoRepository eventoRepository = mock(EventoGraduacaoRepository.class);
        EventoGraduacaoPrajiedRepository prajiedRepository = mock(EventoGraduacaoPrajiedRepository.class);
        ConviteGraduacaoRepository conviteRepository = mock(ConviteGraduacaoRepository.class);
        InscricaoGraduacaoRepository inscricaoRepository = mock(InscricaoGraduacaoRepository.class);
        AlunoRepository alunoRepository = mock(AlunoRepository.class);
        GraduacaoRepository graduacaoRepository = mock(GraduacaoRepository.class);
        GraduacaoAlunoService graduacaoAlunoService = mock(GraduacaoAlunoService.class);
        PermissaoService permissaoService = mock(PermissaoService.class);
        Clock clock = Clock.fixed(HOJE.atTime(12, 0).atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());

        Academia academia = academia();
        Graduacao atual = graduacao(1L, "Branco", 1, academia, BigDecimal.ZERO);
        Graduacao destino = graduacao(2L, "Azul", 2, academia, BigDecimal.valueOf(120));
        Aluno aluno = aluno(20L, academia, atual);
        Usuario usuario = usuario(perfil, academia, aluno);
        EventoGraduacao evento = evento(usuario);
        EventoGraduacaoPrajied item = item(evento, destino);
        ConviteGraduacao convite = convite(evento, aluno, atual, destino, usuario);
        InscricaoGraduacao inscricao = inscricao(evento, aluno, destino);

        when(permissaoService.academiaAtual()).thenReturn(academia);
        when(permissaoService.academiaAtualId()).thenReturn(1L);
        when(permissaoService.usuarioAtual()).thenReturn(usuario);
        when(eventoRepository.findByIdAndAcademiaId(100L, 1L)).thenReturn(Optional.of(evento));
        when(alunoRepository.findByIdAndAcademiaId(20L, 1L)).thenReturn(Optional.of(aluno));
        when(graduacaoRepository.findByIdAndAcademiaId(2L, 1L)).thenReturn(Optional.of(destino));
        when(graduacaoRepository.findByIdAndAcademiaId(1L, 1L)).thenReturn(Optional.of(atual));

        EventoGraduacaoService service = new EventoGraduacaoService(eventoRepository, prajiedRepository, conviteRepository,
                inscricaoRepository, alunoRepository, graduacaoRepository, graduacaoAlunoService, permissaoService, clock);
        return new Fixture(service, eventoRepository, prajiedRepository, conviteRepository, inscricaoRepository,
                alunoRepository, graduacaoRepository, graduacaoAlunoService, permissaoService, academia, atual,
                destino, aluno, usuario, evento, item, convite, inscricao);
    }

    private EventoGraduacaoRequest eventoRequest() {
        EventoGraduacaoRequest request = new EventoGraduacaoRequest();
        request.setTitulo("Graduação Elite");
        request.setDescricao("Evento técnico");
        request.setDataEvento(LocalDate.of(2026, 12, 12));
        request.setHoraInicio(LocalTime.of(9, 0));
        request.setHoraFim(LocalTime.of(12, 0));
        request.setLocal("Elite Fight Club");
        request.setInicioInscricoes(LocalDateTime.of(2026, 7, 15, 8, 0));
        request.setFimInscricoes(LocalDateTime.of(2026, 12, 12, 9, 0));
        request.setLimiteParticipantes(20);
        return request;
    }

    private PublicarEventoGraduacaoRequest publicar(boolean automatico) {
        PublicarEventoGraduacaoRequest request = new PublicarEventoGraduacaoRequest();
        request.setCriarConvitesAutomaticos(automatico);
        return request;
    }

    private ConviteGraduacaoRequest conviteRequest(String justificativa) {
        ConviteGraduacaoRequest request = new ConviteGraduacaoRequest();
        request.setAlunoId(20L);
        request.setGraduacaoDestinoId(2L);
        request.setJustificativa(justificativa);
        return request;
    }

    private ElegibilidadeGraduacaoResponse elegibilidade(Aluno aluno, Graduacao atual, Graduacao proxima, boolean elegivel) {
        return new ElegibilidadeGraduacaoResponse(aluno.getId(), atual.getId(), atual.getNome(), proxima.getId(),
                proxima.getNome(), elegivel ? 10 : 2, 10, elegivel ? 0 : 8, 0, HOJE.minusMonths(3), HOJE.minusMonths(3),
                elegivel, true, elegivel, elegivel ? "Elegível" : "Pendente");
    }

    private Academia academia() {
        Academia academia = new Academia();
        academia.setId(1L);
        academia.setNome("Elite");
        return academia;
    }

    private Graduacao graduacao(Long id, String nome, int ordem, Academia academia, BigDecimal valor) {
        Graduacao graduacao = new Graduacao();
        graduacao.setId(id);
        graduacao.setNome(nome);
        graduacao.setCorHex("#FFFFFF");
        graduacao.setOrdem(ordem);
        graduacao.setCheckinsNecessarios(10);
        graduacao.setTempoMinimoMeses(0);
        graduacao.setValorPadraoGraduacao(valor);
        graduacao.setAtiva(true);
        graduacao.setAcademia(academia);
        return graduacao;
    }

    private Aluno aluno(Long id, Academia academia, Graduacao graduacao) {
        Aluno aluno = new Aluno();
        aluno.setId(id);
        aluno.setNome("Aluno");
        aluno.setAtivo(true);
        aluno.setAcademia(academia);
        aluno.setGraduacaoEntidade(graduacao);
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

    private EventoGraduacao evento(Usuario usuario) {
        EventoGraduacao evento = new EventoGraduacao();
        evento.setId(100L);
        evento.setAcademia(usuario.getAcademia());
        evento.setTitulo("Graduação Elite");
        evento.setDescricao("Evento técnico");
        evento.setDataEvento(LocalDate.of(2026, 12, 12));
        evento.setHoraInicio(LocalTime.of(9, 0));
        evento.setHoraFim(LocalTime.of(12, 0));
        evento.setLocal("Elite Fight Club");
        evento.setInicioInscricoes(LocalDateTime.of(2026, 7, 1, 8, 0));
        evento.setFimInscricoes(LocalDateTime.of(2026, 12, 12, 9, 0));
        evento.setStatus(StatusEventoGraduacao.PUBLICADO);
        evento.setCriadoPor(usuario);
        evento.setCriadoEm(LocalDateTime.of(2026, 7, 1, 8, 0));
        evento.setAtualizadoEm(LocalDateTime.of(2026, 7, 1, 8, 0));
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

    private ConviteGraduacao convite(EventoGraduacao evento, Aluno aluno, Graduacao atual, Graduacao destino, Usuario usuario) {
        ConviteGraduacao convite = new ConviteGraduacao();
        convite.setId(50L);
        convite.setAcademia(evento.getAcademia());
        convite.setEvento(evento);
        convite.setAluno(aluno);
        convite.setGraduacaoAtual(atual);
        convite.setGraduacaoDestino(destino);
        convite.setStatus(StatusConviteGraduacao.PENDENTE);
        convite.setEnviadoEm(LocalDateTime.of(2026, 7, 10, 8, 0));
        convite.setEnviadoPor(usuario);
        return convite;
    }

    private InscricaoGraduacao inscricao(EventoGraduacao evento, Aluno aluno, Graduacao destino) {
        InscricaoGraduacao inscricao = new InscricaoGraduacao();
        inscricao.setId(70L);
        inscricao.setAcademia(evento.getAcademia());
        inscricao.setEvento(evento);
        inscricao.setAluno(aluno);
        inscricao.setGraduacaoDestino(destino);
        inscricao.setStatus(StatusInscricaoGraduacao.CONFIRMADA);
        inscricao.setValor(BigDecimal.valueOf(120));
        inscricao.setInscritoEm(LocalDateTime.of(2026, 7, 14, 12, 0));
        inscricao.setChaveInscricaoAtiva("100:20");
        return inscricao;
    }

    private record Fixture(EventoGraduacaoService service,
                           EventoGraduacaoRepository eventoRepository,
                           EventoGraduacaoPrajiedRepository prajiedRepository,
                           ConviteGraduacaoRepository conviteRepository,
                           InscricaoGraduacaoRepository inscricaoRepository,
                           AlunoRepository alunoRepository,
                           GraduacaoRepository graduacaoRepository,
                           GraduacaoAlunoService graduacaoAlunoService,
                           PermissaoService permissaoService,
                           Academia academia,
                           Graduacao atual,
                           Graduacao destino,
                           Aluno aluno,
                           Usuario usuario,
                           EventoGraduacao evento,
                           EventoGraduacaoPrajied item,
                           ConviteGraduacao convite,
                           InscricaoGraduacao inscricao) {
    }
}
