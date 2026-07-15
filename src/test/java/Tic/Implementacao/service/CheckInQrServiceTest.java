package Tic.Implementacao.service;

import Tic.Implementacao.dto.checkinqr.*;
import Tic.Implementacao.dto.presenca.PresencaAlunoResponse;
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

class CheckInQrServiceTest {

    private static final LocalDate DATA = LocalDate.of(2026, 7, 14);

    @Test
    void geraCodigoPublicoDaAcademiaQuandoNaoExiste() {
        Fixture fx = fixture(PerfilUsuario.ADMIN, DATA.atTime(18, 50));
        fx.academia.setCodigoCheckInPublico(null);
        when(fx.academiaRepository.existsByCodigoCheckInPublico(anyString())).thenReturn(false);

        QrAcademiaResponse response = fx.service.consultarQrAcademia(1L);

        assertNotNull(response.getCodigoPublico());
        assertTrue(response.getCheckInUrl().contains(response.getCodigoPublico()));
        verify(fx.academiaRepository).save(fx.academia);
    }

    @Test
    void regeneracaoInvalidaCodigoAnterior() {
        Fixture fx = fixture(PerfilUsuario.ADMIN, DATA.atTime(18, 50));
        String antigo = fx.academia.getCodigoCheckInPublico();

        QrAcademiaResponse response = fx.service.regenerarQrAcademia(1L);

        assertNotEquals(antigo, response.getCodigoPublico());
        verify(fx.academiaRepository).save(fx.academia);
    }

    @Test
    void professorAbreSessaoPropria() {
        Fixture fx = fixture(PerfilUsuario.PROFESSOR, DATA.atTime(18, 50));

        SessaoCheckInResponse response = fx.service.abrirSessaoTurma(10L);

        assertEquals(TipoItemAgenda.TURMA, response.getTipo());
        assertEquals(StatusSessaoCheckIn.ABERTA, response.getStatus());
        verify(fx.sessaoRepository).save(any(SessaoCheckIn.class));
    }

    @Test
    void professorNaoAbreSessaoDeOutroProfessor() {
        Fixture fx = fixture(PerfilUsuario.PROFESSOR, DATA.atTime(18, 50));
        fx.aulaTurma.setProfessor(professor(99L, fx.academia));

        assertThrows(AccessDeniedException.class, () -> fx.service.abrirSessaoTurma(10L));
    }

    @Test
    void adminDaMesmaAcademiaAbreSessao() {
        Fixture fx = fixture(PerfilUsuario.ADMIN, DATA.atTime(18, 50));

        SessaoCheckInResponse response = fx.service.abrirSessaoTurma(10L);

        assertEquals(StatusSessaoCheckIn.ABERTA, response.getStatus());
    }

    @Test
    void adminDeOutraAcademiaNaoConsultaQr() {
        Fixture fx = fixture(PerfilUsuario.ADMIN, DATA.atTime(18, 50));
        when(fx.permissaoService.academiaAtualId()).thenReturn(2L);

        assertThrows(AccessDeniedException.class, () -> fx.service.consultarQrAcademia(1L));
    }

    @Test
    void aulaCanceladaOuRemarcadaNaoAbreSessao() {
        Fixture fx = fixture(PerfilUsuario.ADMIN, DATA.atTime(18, 50));
        fx.aulaTurma.setStatus(StatusAula.CANCELADA);
        assertThrows(RuntimeException.class, () -> fx.service.abrirSessaoTurma(10L));

        fx.aulaTurma.setStatus(StatusAula.REMARCADA);
        assertThrows(RuntimeException.class, () -> fx.service.abrirSessaoTurma(10L));
    }

    @Test
    void bloqueiaSessaoDuplicada() {
        Fixture fx = fixture(PerfilUsuario.ADMIN, DATA.atTime(18, 50));
        when(fx.sessaoRepository.findByAcademiaIdAndChaveSessaoAtivaAndStatus(1L, "TURMA:10", StatusSessaoCheckIn.ABERTA))
                .thenReturn(Optional.of(sessaoTurma(fx)));

        assertThrows(RuntimeException.class, () -> fx.service.abrirSessaoTurma(10L));
    }

    @Test
    void bloqueiaSessaoAntesDaJanela() {
        Fixture fx = fixture(PerfilUsuario.ADMIN, DATA.atTime(18, 40));

        assertThrows(RuntimeException.class, () -> fx.service.abrirSessaoTurma(10L));
    }

    @Test
    void sessaoExpiradaOuEncerradaBloqueiaConfirmacao() {
        Fixture fx = fixture(PerfilUsuario.ALUNO, DATA.atTime(19, 20));
        SessaoCheckIn sessao = sessaoTurma(fx);
        sessao.setExpiraEm(DATA.atTime(19, 15));
        when(fx.sessaoRepository.findByIdAndAcademiaIdForUpdate(100L, 1L)).thenReturn(Optional.of(sessao));

        assertThrows(RuntimeException.class, () -> fx.service.confirmar("codigo", confirmar(100L)));

        sessao.setStatus(StatusSessaoCheckIn.ENCERRADA);
        assertThrows(RuntimeException.class, () -> fx.service.confirmar("codigo", confirmar(100L)));
    }

    @Test
    void encerrarSessaoManual() {
        Fixture fx = fixture(PerfilUsuario.PROFESSOR, DATA.atTime(19, 0));
        SessaoCheckIn sessao = sessaoTurma(fx);
        when(fx.sessaoRepository.findByIdAndAcademiaIdForUpdate(100L, 1L)).thenReturn(Optional.of(sessao));
        when(fx.sessaoRepository.save(any(SessaoCheckIn.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SessaoCheckInResponse response = fx.service.encerrarSessao(100L);

        assertEquals(StatusSessaoCheckIn.ENCERRADA, response.getStatus());
        assertNull(sessao.getChaveSessaoAtiva());
    }

    @Test
    void alunoSemAutenticacaoOuOutraAcademiaNaoListaOpcoes() {
        Fixture fx = fixture(PerfilUsuario.PROFESSOR, DATA.atTime(19, 0));
        assertThrows(AccessDeniedException.class, () -> fx.service.listarOpcoes("codigo"));

        Fixture alunoOutra = fixture(PerfilUsuario.ALUNO, DATA.atTime(19, 0));
        alunoOutra.aluno.setAcademia(academia(2L));
        assertThrows(RuntimeException.class, () -> alunoOutra.service.listarOpcoes("codigo"));
    }

    @Test
    void alunoInativoOuPlanoInativoNaoListaOpcoes() {
        Fixture fx = fixture(PerfilUsuario.ALUNO, DATA.atTime(19, 0));
        fx.aluno.setAtivo(false);
        assertThrows(RuntimeException.class, () -> fx.service.listarOpcoes("codigo"));

        fx.aluno.setAtivo(true);
        fx.aluno.getPlano().setAtivo(false);
        assertThrows(RuntimeException.class, () -> fx.service.listarOpcoes("codigo"));
    }

    @Test
    void alunoDeTurmaComReservaConfirmadaVeOpcao() {
        Fixture fx = fixture(PerfilUsuario.ALUNO, DATA.atTime(19, 0));
        when(fx.sessaoRepository.buscarAbertasValidas(1L, StatusSessaoCheckIn.ABERTA, DATA.atTime(19, 0)))
                .thenReturn(List.of(sessaoTurma(fx)));

        OpcoesCheckInQrResponse response = fx.service.listarOpcoes("codigo");

        assertEquals(1, response.getOpcoes().size());
        assertEquals(TipoItemAgenda.TURMA, response.getOpcoes().getFirst().getTipo());
    }

    @Test
    void alunoDeTurmaSemReservaOuReservaCanceladaNaoVeOpcao() {
        Fixture fx = fixture(PerfilUsuario.ALUNO, DATA.atTime(19, 0));
        when(fx.reservaRepository.existsByAcademiaIdAndAulaTurmaIdAndAlunoIdAndStatus(1L, 10L, 20L, StatusReserva.CONFIRMADA))
                .thenReturn(false);
        when(fx.sessaoRepository.buscarAbertasValidas(1L, StatusSessaoCheckIn.ABERTA, DATA.atTime(19, 0)))
                .thenReturn(List.of(sessaoTurma(fx)));

        assertTrue(fx.service.listarOpcoes("codigo").getOpcoes().isEmpty());
    }

    @Test
    void alunoPersonalCorretoVeOpcaoEOutroAlunoNao() {
        Fixture fx = fixture(PerfilUsuario.ALUNO, DATA.atTime(9, 0));
        when(fx.sessaoRepository.buscarAbertasValidas(1L, StatusSessaoCheckIn.ABERTA, DATA.atTime(9, 0)))
                .thenReturn(List.of(sessaoPersonal(fx)));

        assertEquals(1, fx.service.listarOpcoes("codigo").getOpcoes().size());

        fx.usuario.setAluno(aluno(21L, fx.academia));
        assertTrue(fx.service.listarOpcoes("codigo").getOpcoes().isEmpty());
    }

    @Test
    void multiplasOuNenhumaSessaoValida() {
        Fixture fx = fixture(PerfilUsuario.ALUNO, DATA.atTime(19, 0));
        SessaoCheckIn turma = sessaoTurma(fx);
        SessaoCheckIn personal = sessaoPersonal(fx);
        personal.getAulaPersonal().setHoraInicio(LocalTime.of(19, 0));
        personal.getAulaPersonal().setHoraFim(LocalTime.of(20, 0));
        personal.setExpiraEm(DATA.atTime(19, 15));
        when(fx.sessaoRepository.buscarAbertasValidas(1L, StatusSessaoCheckIn.ABERTA, DATA.atTime(19, 0)))
                .thenReturn(List.of(turma, personal));

        assertEquals(2, fx.service.listarOpcoes("codigo").getOpcoes().size());

        when(fx.sessaoRepository.buscarAbertasValidas(1L, StatusSessaoCheckIn.ABERTA, DATA.atTime(19, 0)))
                .thenReturn(List.of());
        assertTrue(fx.service.listarOpcoes("codigo").getOpcoes().isEmpty());
    }

    @Test
    void confirmaCheckinPorQrComOrigemQrCode() {
        Fixture fx = fixture(PerfilUsuario.ALUNO, DATA.atTime(19, 0));
        SessaoCheckIn sessao = sessaoTurma(fx);
        when(fx.sessaoRepository.findByIdAndAcademiaIdForUpdate(100L, 1L)).thenReturn(Optional.of(sessao));
        when(fx.alunoRepository.findByIdAndAcademiaIdForUpdate(20L, 1L)).thenReturn(Optional.of(fx.aluno));
        when(fx.presencaAulaService.registrarPresencaECheckIn(eq(fx.aluno), eq(fx.aulaTurma), isNull(),
                eq(OrigemCheckIn.QR_CODE), eq(fx.usuario), anyString(), eq(false)))
                .thenReturn(new PresencaAlunoResponse(20L, "Aluno", null, StatusPresenca.PRESENTE, true, 200L, DATA.atTime(19, 0), "Aluno"));
        when(fx.checkInRepository.findByAcademiaIdAndChaveCheckInAtivoAndAtivoTrue(1L, "TURMA:10:20"))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(checkIn(fx)));

        ConfirmarCheckInQrResponse response = fx.service.confirmar("codigo", confirmar(100L));

        assertEquals("Check-in realizado com sucesso", response.getMensagem());
        assertEquals(OrigemCheckIn.QR_CODE, OrigemCheckIn.QR_CODE);
        verify(fx.presencaAulaService).registrarPresencaECheckIn(any(), any(), isNull(), eq(OrigemCheckIn.QR_CODE), any(), anyString(), eq(false));
    }

    @Test
    void checkinRepetidoNaoIncrementaNovamente() {
        Fixture fx = fixture(PerfilUsuario.ALUNO, DATA.atTime(19, 0));
        when(fx.sessaoRepository.findByIdAndAcademiaIdForUpdate(100L, 1L)).thenReturn(Optional.of(sessaoTurma(fx)));
        when(fx.checkInRepository.findByAcademiaIdAndChaveCheckInAtivoAndAtivoTrue(1L, "TURMA:10:20"))
                .thenReturn(Optional.of(checkIn(fx)));

        ConfirmarCheckInQrResponse response = fx.service.confirmar("codigo", confirmar(100L));

        assertEquals("Check-in já realizado", response.getMensagem());
        verify(fx.presencaAulaService, never()).registrarPresencaECheckIn(any(), any(), any(), any(), any(), any(), anyBoolean());
    }

    @Test
    void consultaDeOpcoesNaoAlteraDados() {
        Fixture fx = fixture(PerfilUsuario.ALUNO, DATA.atTime(19, 0));

        fx.service.listarOpcoes("codigo");

        verify(fx.sessaoRepository, never()).save(any());
        verify(fx.presencaAulaService, never()).registrarPresencaECheckIn(any(), any(), any(), any(), any(), any(), anyBoolean());
    }

    private Fixture fixture(PerfilUsuario perfil, LocalDateTime agora) {
        AcademiaRepository academiaRepository = mock(AcademiaRepository.class);
        SessaoCheckInRepository sessaoRepository = mock(SessaoCheckInRepository.class);
        AulaTurmaRepository aulaTurmaRepository = mock(AulaTurmaRepository.class);
        AulaPersonalRepository aulaPersonalRepository = mock(AulaPersonalRepository.class);
        ReservaAulaRepository reservaRepository = mock(ReservaAulaRepository.class);
        AlunoRepository alunoRepository = mock(AlunoRepository.class);
        CheckInRepository checkInRepository = mock(CheckInRepository.class);
        PresencaAulaService presencaAulaService = mock(PresencaAulaService.class);
        PermissaoService permissaoService = mock(PermissaoService.class);
        Clock clock = Clock.fixed(agora.atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());

        Academia academia = academia(1L);
        Professor professor = professor(30L, academia);
        Aluno aluno = aluno(20L, academia);
        AulaTurma aulaTurma = aulaTurma(academia, professor);
        AulaPersonal aulaPersonal = aulaPersonal(academia, professor, aluno);
        Usuario usuario = usuario(perfil, academia, professor, aluno);

        when(permissaoService.usuarioAtual()).thenReturn(usuario);
        when(permissaoService.academiaAtualId()).thenReturn(1L);
        when(academiaRepository.findById(1L)).thenReturn(Optional.of(academia));
        when(academiaRepository.findByCodigoCheckInPublico("codigo")).thenReturn(Optional.of(academia));
        when(academiaRepository.existsByCodigoCheckInPublico(anyString())).thenReturn(false);
        when(academiaRepository.save(any(Academia.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(aulaTurmaRepository.buscarDetalhePorId(10L, 1L)).thenReturn(Optional.of(aulaTurma));
        when(aulaPersonalRepository.buscarDetalhePorId(11L, 1L)).thenReturn(Optional.of(aulaPersonal));
        when(sessaoRepository.save(any(SessaoCheckIn.class))).thenAnswer(invocation -> {
            SessaoCheckIn sessao = invocation.getArgument(0);
            if (sessao.getId() == null) {
                sessao.setId(100L);
            }
            return sessao;
        });
        when(reservaRepository.existsByAcademiaIdAndAulaTurmaIdAndAlunoIdAndStatus(1L, 10L, 20L, StatusReserva.CONFIRMADA))
                .thenReturn(true);
        when(checkInRepository.findByAcademiaIdAndChaveCheckInAtivoAndAtivoTrue(anyLong(), anyString())).thenReturn(Optional.empty());

        CheckInQrService service = new CheckInQrService(academiaRepository, sessaoRepository, aulaTurmaRepository,
                aulaPersonalRepository, reservaRepository, alunoRepository, checkInRepository, presencaAulaService,
                permissaoService, clock, 15, 15, "http://localhost:3000");
        return new Fixture(academiaRepository, sessaoRepository, aulaTurmaRepository, aulaPersonalRepository,
                reservaRepository, alunoRepository, checkInRepository, presencaAulaService, permissaoService,
                service, academia, professor, aluno, aulaTurma, aulaPersonal, usuario);
    }

    private Academia academia(Long id) {
        Academia academia = new Academia();
        academia.setId(id);
        academia.setNome("Elite");
        academia.setCodigoCheckInPublico("codigo");
        return academia;
    }

    private Professor professor(Long id, Academia academia) {
        Professor professor = new Professor();
        professor.setId(id);
        professor.setNome("Professor");
        professor.setAcademia(academia);
        return professor;
    }

    private Aluno aluno(Long id, Academia academia) {
        Plano plano = new Plano();
        plano.setId(1L);
        plano.setAcademia(academia);
        plano.setTipo(TipoPlano.TURMA);
        plano.setAtivo(true);
        plano.setQuantidadeAulasSemana(3);
        plano.setValor(BigDecimal.valueOf(180));

        Aluno aluno = new Aluno();
        aluno.setId(id);
        aluno.setNome("Aluno");
        aluno.setAcademia(academia);
        aluno.setAtivo(true);
        aluno.setPlano(plano);
        aluno.setQuantidadeCheckins(0);
        return aluno;
    }

    private AulaTurma aulaTurma(Academia academia, Professor professor) {
        Modalidade modalidade = new Modalidade();
        modalidade.setId(1L);
        modalidade.setNome("Muay Thai");

        AulaTurma aula = new AulaTurma();
        aula.setId(10L);
        aula.setAcademia(academia);
        aula.setProfessor(professor);
        aula.setModalidade(modalidade);
        aula.setData(DATA);
        aula.setHoraInicio(LocalTime.of(19, 0));
        aula.setHoraFim(LocalTime.of(20, 0));
        aula.setStatus(StatusAula.AGENDADA);
        aula.setCapacidadeMaxima(10);
        return aula;
    }

    private AulaPersonal aulaPersonal(Academia academia, Professor professor, Aluno aluno) {
        AulaPersonal aula = new AulaPersonal();
        aula.setId(11L);
        aula.setAcademia(academia);
        aula.setProfessor(professor);
        aula.setAluno(aluno);
        aula.setData(DATA);
        aula.setHoraInicio(LocalTime.of(9, 0));
        aula.setHoraFim(LocalTime.of(10, 0));
        aula.setStatus(StatusAula.AGENDADA);
        return aula;
    }

    private Usuario usuario(PerfilUsuario perfil, Academia academia, Professor professor, Aluno aluno) {
        Usuario usuario = new Usuario();
        usuario.setId(70L);
        usuario.setNome(perfil == PerfilUsuario.ALUNO ? "Aluno" : "Usuario");
        usuario.setPerfil(perfil);
        usuario.setAcademia(academia);
        if (perfil == PerfilUsuario.PROFESSOR) {
            usuario.setProfessor(professor);
        }
        if (perfil == PerfilUsuario.ALUNO) {
            usuario.setAluno(aluno);
        }
        return usuario;
    }

    private SessaoCheckIn sessaoTurma(Fixture fx) {
        SessaoCheckIn sessao = new SessaoCheckIn();
        sessao.setId(100L);
        sessao.setAcademia(fx.academia);
        sessao.setAulaTurma(fx.aulaTurma);
        sessao.setStatus(StatusSessaoCheckIn.ABERTA);
        sessao.setAbertaEm(DATA.atTime(18, 50));
        sessao.setExpiraEm(DATA.atTime(19, 15));
        sessao.setAbertaPor(fx.usuario);
        sessao.setChaveSessaoAtiva("TURMA:10");
        return sessao;
    }

    private SessaoCheckIn sessaoPersonal(Fixture fx) {
        SessaoCheckIn sessao = new SessaoCheckIn();
        sessao.setId(101L);
        sessao.setAcademia(fx.academia);
        sessao.setAulaPersonal(fx.aulaPersonal);
        sessao.setStatus(StatusSessaoCheckIn.ABERTA);
        sessao.setAbertaEm(DATA.atTime(8, 50));
        sessao.setExpiraEm(DATA.atTime(9, 15));
        sessao.setAbertaPor(fx.usuario);
        sessao.setChaveSessaoAtiva("PERSONAL:11");
        return sessao;
    }

    private ConfirmarCheckInQrRequest confirmar(Long sessaoId) {
        ConfirmarCheckInQrRequest request = new ConfirmarCheckInQrRequest();
        request.setSessaoId(sessaoId);
        return request;
    }

    private CheckIn checkIn(Fixture fx) {
        CheckIn checkIn = new CheckIn();
        checkIn.setId(200L);
        checkIn.setAcademia(fx.academia);
        checkIn.setAluno(fx.aluno);
        checkIn.setAulaTurma(fx.aulaTurma);
        checkIn.setOrigem(OrigemCheckIn.QR_CODE);
        checkIn.setAtivo(true);
        checkIn.setChaveCheckInAtivo("TURMA:10:20");
        return checkIn;
    }

    private record Fixture(AcademiaRepository academiaRepository,
                           SessaoCheckInRepository sessaoRepository,
                           AulaTurmaRepository aulaTurmaRepository,
                           AulaPersonalRepository aulaPersonalRepository,
                           ReservaAulaRepository reservaRepository,
                           AlunoRepository alunoRepository,
                           CheckInRepository checkInRepository,
                           PresencaAulaService presencaAulaService,
                           PermissaoService permissaoService,
                           CheckInQrService service,
                           Academia academia,
                           Professor professor,
                           Aluno aluno,
                           AulaTurma aulaTurma,
                           AulaPersonal aulaPersonal,
                           Usuario usuario) {
    }
}
