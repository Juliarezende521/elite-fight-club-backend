package Tic.Implementacao.service;

import Tic.Implementacao.dto.presenca.*;
import Tic.Implementacao.model.*;
import Tic.Implementacao.repository.*;
import Tic.Implementacao.security.PermissaoService;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.time.*;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PresencaAulaServiceTest {

    private static final LocalDate DATA = LocalDate.of(2026, 7, 14);

    @Test
    void deveRegistrarPresencaValidaEmTurma() {
        Fixture fx = fixture(PerfilUsuario.PROFESSOR, DATA.atTime(19, 5));

        PresencaAlunoResponse response = fx.service.registrarPresencaTurma(10L, 20L, request(StatusPresenca.PRESENTE));

        assertEquals(StatusPresenca.PRESENTE, response.getStatusPresenca());
        assertTrue(response.isCheckInRegistrado());
        assertEquals(1, fx.aluno.getQuantidadeCheckins());
        verify(fx.checkInRepository).save(any(CheckIn.class));
    }

    @Test
    void deveRegistrarPresencaValidaEmPersonal() {
        Fixture fx = fixture(PerfilUsuario.PROFESSOR, DATA.atTime(9, 5));

        PresencaAlunoResponse response = fx.service.registrarPresencaPersonal(11L, request(StatusPresenca.PRESENTE));

        assertEquals(StatusPresenca.PRESENTE, response.getStatusPresenca());
        assertEquals(1, fx.aluno.getQuantidadeCheckins());
    }

    @Test
    void bloqueiaAlunoSemReservaEmTurma() {
        Fixture fx = fixture(PerfilUsuario.PROFESSOR, DATA.atTime(19, 5));
        when(fx.reservaRepository.findByAcademiaIdAndAulaTurmaIdAndAlunoIdAndStatus(1L, 10L, 20L, StatusReserva.CONFIRMADA))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> fx.service.registrarPresencaTurma(10L, 20L, request(StatusPresenca.PRESENTE)));
    }

    @Test
    void reservaCanceladaNaoPermitePresenca() {
        bloqueiaAlunoSemReservaEmTurma();
    }

    @Test
    void bloqueiaAulaCanceladaERemarcada() {
        Fixture fx = fixture(PerfilUsuario.PROFESSOR, DATA.atTime(19, 5));
        fx.aulaTurma.setStatus(StatusAula.CANCELADA);
        assertThrows(RuntimeException.class, () -> fx.service.registrarPresencaTurma(10L, 20L, request(StatusPresenca.PRESENTE)));

        fx.aulaTurma.setStatus(StatusAula.REMARCADA);
        assertThrows(RuntimeException.class, () -> fx.service.registrarPresencaTurma(10L, 20L, request(StatusPresenca.PRESENTE)));
    }

    @Test
    void bloqueiaPresencaAntesDoInicio() {
        Fixture fx = fixture(PerfilUsuario.PROFESSOR, DATA.atTime(18, 30));

        assertThrows(RuntimeException.class, () -> fx.service.registrarPresencaTurma(10L, 20L, request(StatusPresenca.PRESENTE)));
    }

    @Test
    void bloqueiaProfessorDeOutraAcademiaOuOutraAula() {
        Fixture fx = fixture(PerfilUsuario.PROFESSOR, DATA.atTime(19, 5));
        Professor outro = professor(99L, fx.academia);
        fx.aulaTurma.setProfessor(outro);

        assertThrows(AccessDeniedException.class, () -> fx.service.registrarPresencaTurma(10L, 20L, request(StatusPresenca.PRESENTE)));
    }

    @Test
    void adminPodeRegistrarPresenca() {
        Fixture fx = fixture(PerfilUsuario.ADMIN, DATA.atTime(19, 5));

        PresencaAlunoResponse response = fx.service.registrarPresencaTurma(10L, 20L, request(StatusPresenca.PRESENTE));

        assertEquals(StatusPresenca.PRESENTE, response.getStatusPresenca());
    }

    @Test
    void presenteRepetidoNaoDuplicaCheckinNemIncrementa() {
        Fixture fx = fixture(PerfilUsuario.PROFESSOR, DATA.atTime(19, 5));
        CheckIn existente = checkIn(fx.aluno, fx.aulaTurma, fx.usuario);
        when(fx.checkInRepository.findByAcademiaIdAndChaveCheckInAtivoAndAtivoTrue(1L, "TURMA:10:20"))
                .thenReturn(Optional.of(existente));

        fx.service.registrarPresencaTurma(10L, 20L, request(StatusPresenca.PRESENTE));

        assertEquals(0, fx.aluno.getQuantidadeCheckins());
        verify(fx.checkInRepository, never()).save(any(CheckIn.class));
    }

    @Test
    void mudarPresenteParaAusenteInvalidaCheckinEDecrementa() {
        Fixture fx = fixture(PerfilUsuario.PROFESSOR, DATA.atTime(19, 5));
        fx.aluno.setQuantidadeCheckins(1);
        PresencaAula presenca = presenca(fx.aulaTurma, fx.aluno, StatusPresenca.PRESENTE, fx.usuario);
        CheckIn existente = checkIn(fx.aluno, fx.aulaTurma, fx.usuario);
        when(fx.presencaRepository.findByAcademiaIdAndChavePresenca(1L, "TURMA:10:20")).thenReturn(Optional.of(presenca));
        when(fx.checkInRepository.findByAcademiaIdAndChaveCheckInAtivoAndAtivoTrue(1L, "TURMA:10:20"))
                .thenReturn(Optional.of(existente));

        fx.service.registrarPresencaTurma(10L, 20L, request(StatusPresenca.AUSENTE));

        assertFalse(existente.getAtivo());
        assertNull(existente.getChaveCheckInAtivo());
        assertEquals(0, fx.aluno.getQuantidadeCheckins());
    }

    @Test
    void contadorNuncaFicaNegativo() {
        Fixture fx = fixture(PerfilUsuario.PROFESSOR, DATA.atTime(19, 5));
        fx.aluno.setQuantidadeCheckins(0);
        when(fx.checkInRepository.findByAcademiaIdAndChaveCheckInAtivoAndAtivoTrue(1L, "TURMA:10:20"))
                .thenReturn(Optional.of(checkIn(fx.aluno, fx.aulaTurma, fx.usuario)));

        fx.service.registrarPresencaTurma(10L, 20L, request(StatusPresenca.AUSENTE));

        assertEquals(0, fx.aluno.getQuantidadeCheckins());
    }

    @Test
    void ausenteNaoCriaCheckin() {
        Fixture fx = fixture(PerfilUsuario.PROFESSOR, DATA.atTime(19, 5));

        PresencaAlunoResponse response = fx.service.registrarPresencaTurma(10L, 20L, request(StatusPresenca.AUSENTE));

        assertFalse(response.isCheckInRegistrado());
        verify(fx.checkInRepository, never()).save(any(CheckIn.class));
    }

    @Test
    void reservaNaoAlteraCheckin() {
        Fixture fx = fixture(PerfilUsuario.PROFESSOR, DATA.atTime(19, 5));

        assertEquals(0, fx.aluno.getQuantidadeCheckins());
        verify(fx.checkInRepository, never()).save(any(CheckIn.class));
    }

    @Test
    void listaPresencasDaTurma() {
        Fixture fx = fixture(PerfilUsuario.PROFESSOR, DATA.atTime(19, 5));

        ListaPresencasAulaResponse response = fx.service.listarPresencasTurma(10L);

        assertEquals(1, response.getConfirmados());
        assertEquals(1, response.getPendentes());
        assertEquals(20L, response.getAlunos().getFirst().getAlunoId());
    }

    @Test
    void consultaPresencaPersonalPendente() {
        Fixture fx = fixture(PerfilUsuario.PROFESSOR, DATA.atTime(9, 5));

        PresencaAlunoResponse response = fx.service.consultarPresencaPersonal(11L);

        assertEquals(StatusPresenca.PENDENTE, response.getStatusPresenca());
        assertFalse(response.isCheckInRegistrado());
    }

    @Test
    void historicoDoAluno() {
        Fixture fx = fixture(PerfilUsuario.ADMIN, DATA.atTime(19, 5));
        when(fx.checkInRepository.findByAcademiaIdAndAlunoIdAndAtivoTrueAndDataHoraBetweenOrderByDataHoraDesc(
                eq(1L), eq(20L), any(), any())).thenReturn(List.of(checkIn(fx.aluno, fx.aulaTurma, fx.usuario)));

        HistoricoCheckInAlunoResponse response = fx.service.listarHistoricoAluno(20L, DATA.withDayOfMonth(1), DATA.withDayOfMonth(31));

        assertEquals(1, response.getTotal());
    }

    @Test
    void isolamentoPorAcademia() {
        Fixture fx = fixture(PerfilUsuario.ADMIN, DATA.atTime(19, 5));
        when(fx.aulaTurmaRepository.buscarDetalhePorId(10L, 1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> fx.service.registrarPresencaTurma(10L, 20L, request(StatusPresenca.PRESENTE)));
    }

    @Test
    void consultaNaoAlteraDados() {
        Fixture fx = fixture(PerfilUsuario.PROFESSOR, DATA.atTime(19, 5));

        fx.service.listarPresencasTurma(10L);

        verify(fx.presencaRepository, never()).save(any());
        verify(fx.checkInRepository, never()).save(any());
        verify(fx.alunoRepository, never()).save(any());
    }

    private Fixture fixture(PerfilUsuario perfil, LocalDateTime agora) {
        PresencaAulaRepository presencaRepository = mock(PresencaAulaRepository.class);
        CheckInRepository checkInRepository = mock(CheckInRepository.class);
        AulaTurmaRepository aulaTurmaRepository = mock(AulaTurmaRepository.class);
        AulaPersonalRepository aulaPersonalRepository = mock(AulaPersonalRepository.class);
        ReservaAulaRepository reservaRepository = mock(ReservaAulaRepository.class);
        AlunoRepository alunoRepository = mock(AlunoRepository.class);
        PermissaoService permissaoService = mock(PermissaoService.class);
        Clock clock = Clock.fixed(agora.atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());

        Academia academia = academia();
        Professor professor = professor(30L, academia);
        Aluno aluno = aluno(20L, academia);
        AulaTurma aulaTurma = aulaTurma(academia, professor);
        AulaPersonal aulaPersonal = aulaPersonal(academia, professor, aluno);
        Usuario usuario = usuario(perfil, academia, professor);
        ReservaAula reserva = reserva(aulaTurma, aluno);

        when(permissaoService.usuarioAtual()).thenReturn(usuario);
        when(permissaoService.academiaAtualId()).thenReturn(1L);
        when(aulaTurmaRepository.buscarDetalhePorId(10L, 1L)).thenReturn(Optional.of(aulaTurma));
        when(aulaPersonalRepository.buscarDetalhePorId(11L, 1L)).thenReturn(Optional.of(aulaPersonal));
        when(reservaRepository.findByAcademiaIdAndAulaTurmaIdAndAlunoIdAndStatus(1L, 10L, 20L, StatusReserva.CONFIRMADA))
                .thenReturn(Optional.of(reserva));
        when(reservaRepository.buscarConfirmadasComAluno(1L, 10L, StatusReserva.CONFIRMADA)).thenReturn(List.of(reserva));
        when(alunoRepository.findByIdAndAcademiaIdForUpdate(20L, 1L)).thenReturn(Optional.of(aluno));
        when(alunoRepository.findByIdAndAcademiaId(20L, 1L)).thenReturn(Optional.of(aluno));
        when(presencaRepository.save(any(PresencaAula.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(checkInRepository.save(any(CheckIn.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(checkInRepository.findByAcademiaIdAndChaveCheckInAtivoAndAtivoTrue(anyLong(), anyString())).thenReturn(Optional.empty());

        PresencaAulaService service = new PresencaAulaService(presencaRepository, checkInRepository, aulaTurmaRepository,
                aulaPersonalRepository, reservaRepository, alunoRepository, permissaoService, clock);
        return new Fixture(presencaRepository, checkInRepository, aulaTurmaRepository, aulaPersonalRepository,
                reservaRepository, alunoRepository, permissaoService, service, academia, professor, aluno,
                aulaTurma, aulaPersonal, usuario);
    }

    private RegistrarPresencaRequest request(StatusPresenca status) {
        RegistrarPresencaRequest request = new RegistrarPresencaRequest();
        request.setStatus(status);
        request.setObservacao("teste");
        return request;
    }

    private Academia academia() {
        Academia academia = new Academia();
        academia.setId(1L);
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
        Aluno aluno = new Aluno();
        aluno.setId(id);
        aluno.setNome("Aluno");
        aluno.setAcademia(academia);
        aluno.setQuantidadeCheckins(0);
        return aluno;
    }

    private AulaTurma aulaTurma(Academia academia, Professor professor) {
        AulaTurma aula = new AulaTurma();
        aula.setId(10L);
        aula.setAcademia(academia);
        aula.setProfessor(professor);
        aula.setData(DATA);
        aula.setHoraInicio(LocalTime.of(19, 0));
        aula.setHoraFim(LocalTime.of(20, 0));
        aula.setStatus(StatusAula.AGENDADA);
        aula.setCapacidadeMaxima(10);
        return aula;
    }

    private AulaPersonal aulaPersonal(Academia academia, Professor professor, Aluno aluno) {
        AgendaPersonal agenda = new AgendaPersonal();
        agenda.setId(50L);
        AulaPersonal aula = new AulaPersonal();
        aula.setId(11L);
        aula.setAcademia(academia);
        aula.setProfessor(professor);
        aula.setAluno(aluno);
        aula.setAgendaPersonal(agenda);
        aula.setData(DATA);
        aula.setHoraInicio(LocalTime.of(9, 0));
        aula.setHoraFim(LocalTime.of(10, 0));
        aula.setStatus(StatusAula.AGENDADA);
        return aula;
    }

    private Usuario usuario(PerfilUsuario perfil, Academia academia, Professor professor) {
        Usuario usuario = new Usuario();
        usuario.setId(70L);
        usuario.setNome("Usuario");
        usuario.setPerfil(perfil);
        usuario.setAcademia(academia);
        if (perfil == PerfilUsuario.PROFESSOR) {
            usuario.setProfessor(professor);
        }
        return usuario;
    }

    private ReservaAula reserva(AulaTurma aula, Aluno aluno) {
        ReservaAula reserva = new ReservaAula();
        reserva.setId(80L);
        reserva.setAcademia(aula.getAcademia());
        reserva.setAulaTurma(aula);
        reserva.setAluno(aluno);
        reserva.setStatus(StatusReserva.CONFIRMADA);
        reserva.setReservadoEm(DATA.atTime(18, 0));
        return reserva;
    }

    private PresencaAula presenca(AulaTurma aula, Aluno aluno, StatusPresenca status, Usuario usuario) {
        PresencaAula presenca = new PresencaAula();
        presenca.setId(90L);
        presenca.setAcademia(aula.getAcademia());
        presenca.setAulaTurma(aula);
        presenca.setAluno(aluno);
        presenca.setStatus(status);
        presenca.setRegistradoPor(usuario);
        presenca.setRegistradoEm(DATA.atTime(19, 5));
        presenca.setChavePresenca("TURMA:10:20");
        return presenca;
    }

    private CheckIn checkIn(Aluno aluno, AulaTurma aula, Usuario usuario) {
        CheckIn checkIn = new CheckIn();
        checkIn.setId(100L);
        checkIn.setAcademia(aluno.getAcademia());
        checkIn.setAluno(aluno);
        checkIn.setAulaTurma(aula);
        checkIn.setDataHora(DATA.atTime(19, 5));
        checkIn.setOrigem(OrigemCheckIn.PROFESSOR);
        checkIn.setConfirmadoPor(usuario);
        checkIn.setAtivo(true);
        checkIn.setChaveCheckInAtivo("TURMA:10:20");
        return checkIn;
    }

    private record Fixture(PresencaAulaRepository presencaRepository,
                           CheckInRepository checkInRepository,
                           AulaTurmaRepository aulaTurmaRepository,
                           AulaPersonalRepository aulaPersonalRepository,
                           ReservaAulaRepository reservaRepository,
                           AlunoRepository alunoRepository,
                           PermissaoService permissaoService,
                           PresencaAulaService service,
                           Academia academia,
                           Professor professor,
                           Aluno aluno,
                           AulaTurma aulaTurma,
                           AulaPersonal aulaPersonal,
                           Usuario usuario) {
    }
}
