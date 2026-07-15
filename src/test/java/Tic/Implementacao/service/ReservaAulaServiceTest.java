package Tic.Implementacao.service;

import Tic.Implementacao.dto.reserva.OcupacaoAulaResponse;
import Tic.Implementacao.model.*;
import Tic.Implementacao.repository.AulaTurmaRepository;
import Tic.Implementacao.repository.ReservaAulaRepository;
import Tic.Implementacao.security.PermissaoService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ReservaAulaServiceTest {

    private static final LocalDate DATA_AULA = LocalDate.of(2026, 7, 21);

    @Test
    void deveReservarVagaValida() {
        Fixture fx = fixture(DATA_AULA.atTime(18, 0));

        ReservaAula reserva = fx.service.reservarMinhaVaga(10L);

        assertEquals(StatusReserva.CONFIRMADA, reserva.getStatus());
        assertEquals(fx.aluno, reserva.getAluno());
        assertEquals("10:20", reserva.getChaveReservaAtiva());
        verify(fx.aulaTurmaRepository).findByIdAndAcademiaIdForUpdate(10L, 1L);
        verify(fx.reservaRepository).save(any(ReservaAula.class));
    }

    @Test
    void naoDeveReservarAntesDoDiaDaAula() {
        Fixture fx = fixture(DATA_AULA.minusDays(1).atTime(18, 0));

        assertThrows(RuntimeException.class, () -> fx.service.reservarMinhaVaga(10L));
        verify(fx.reservaRepository, never()).save(any());
    }

    @Test
    void naoDeveReservarAposInicioDaAula() {
        Fixture fx = fixture(DATA_AULA.atTime(19, 0));

        assertThrows(RuntimeException.class, () -> fx.service.reservarMinhaVaga(10L));
        verify(fx.reservaRepository, never()).save(any());
    }

    @Test
    void naoDevePermitirAlunoPersonalReservarTurma() {
        Fixture fx = fixture(DATA_AULA.atTime(18, 0));
        fx.aluno.setTipoAluno(TipoAluno.PERSONAL);
        fx.aluno.getPlano().setTipo(TipoPlano.PERSONAL);

        assertThrows(RuntimeException.class, () -> fx.service.reservarMinhaVaga(10L));
    }

    @Test
    void naoDeveReservarComPlanoInativo() {
        Fixture fx = fixture(DATA_AULA.atTime(18, 0));
        fx.aluno.getPlano().setAtivo(false);

        assertThrows(RuntimeException.class, () -> fx.service.reservarMinhaVaga(10L));
    }

    @Test
    void naoDeveUltrapassarLimiteSemanal() {
        Fixture fx = fixture(DATA_AULA.atTime(18, 0));
        when(fx.reservaRepository.countReservasConfirmadasNaSemana(1L, 20L, StatusReserva.CONFIRMADA,
                LocalDate.of(2026, 7, 20), LocalDate.of(2026, 7, 26))).thenReturn(3L);

        assertThrows(RuntimeException.class, () -> fx.service.reservarMinhaVaga(10L));
    }

    @Test
    void naoDeveReservarDuplicado() {
        Fixture fx = fixture(DATA_AULA.atTime(18, 0));
        when(fx.reservaRepository.findByAcademiaIdAndAulaTurmaIdAndAlunoIdAndStatus(1L, 10L, 20L, StatusReserva.CONFIRMADA))
                .thenReturn(Optional.of(reserva(fx.aula, fx.aluno)));

        assertThrows(RuntimeException.class, () -> fx.service.reservarMinhaVaga(10L));
    }

    @Test
    void naoDeveReservarAulaLotada() {
        Fixture fx = fixture(DATA_AULA.atTime(18, 0));
        when(fx.reservaRepository.countByAcademiaIdAndAulaTurmaIdAndStatus(1L, 10L, StatusReserva.CONFIRMADA))
                .thenReturn(15L);

        assertThrows(RuntimeException.class, () -> fx.service.reservarMinhaVaga(10L));
    }

    @Test
    void deveProtegerUltimaVagaComLockNaAula() {
        Fixture fx = fixture(DATA_AULA.atTime(18, 0));
        fx.aula.setCapacidadeMaxima(1);

        fx.service.reservarMinhaVaga(10L);

        verify(fx.aulaTurmaRepository).findByIdAndAcademiaIdForUpdate(10L, 1L);
    }

    @Test
    void naoDeveReservarAulaCancelada() {
        Fixture fx = fixture(DATA_AULA.atTime(18, 0));
        fx.aula.setStatus(StatusAula.CANCELADA);

        assertThrows(RuntimeException.class, () -> fx.service.reservarMinhaVaga(10L));
    }

    @Test
    void naoDeveReservarAlunoDeOutraAcademia() {
        Fixture fx = fixture(DATA_AULA.atTime(18, 0));
        Academia outra = new Academia();
        outra.setId(2L);
        fx.aluno.setAcademia(outra);

        assertThrows(RuntimeException.class, () -> fx.service.reservarMinhaVaga(10L));
    }

    @Test
    void deveCancelarReservaAntesDoInicio() {
        Fixture fx = fixture(DATA_AULA.atTime(18, 0));
        ReservaAula reserva = reserva(fx.aula, fx.aluno);
        when(fx.reservaRepository.findByAcademiaIdAndAulaTurmaIdAndAlunoIdAndStatus(1L, 10L, 20L, StatusReserva.CONFIRMADA))
                .thenReturn(Optional.of(reserva));

        ReservaAula cancelada = fx.service.cancelarMinhaReserva(10L);

        assertEquals(StatusReserva.CANCELADA, cancelada.getStatus());
        assertNull(cancelada.getChaveReservaAtiva());
        assertNotNull(cancelada.getCanceladoEm());
    }

    @Test
    void devePermitirNovaReservaDepoisDeCancelamento() {
        Fixture fx = fixture(DATA_AULA.atTime(18, 0));

        ReservaAula reserva = fx.service.reservarMinhaVaga(10L);

        assertEquals(StatusReserva.CONFIRMADA, reserva.getStatus());
        assertEquals("10:20", reserva.getChaveReservaAtiva());
    }

    @Test
    void deveCalcularOcupacaoCorretamente() {
        Fixture fx = fixture(DATA_AULA.atTime(18, 0));
        when(fx.reservaRepository.countByAcademiaIdAndAulaTurmaIdAndStatus(1L, 10L, StatusReserva.CONFIRMADA))
                .thenReturn(5L);

        OcupacaoAulaResponse ocupacao = fx.service.consultarOcupacao(10L);

        assertEquals(5L, ocupacao.getConfirmados());
        assertEquals(15, ocupacao.getCapacidade());
        assertEquals(10L, ocupacao.getVagasDisponiveis());
        assertFalse(ocupacao.isLotada());
    }

    @Test
    void reservaNaoAlteraCheckinsDoAluno() {
        Fixture fx = fixture(DATA_AULA.atTime(18, 0));
        fx.aluno.setQuantidadeCheckins(4);

        fx.service.reservarMinhaVaga(10L);

        assertEquals(4, fx.aluno.getQuantidadeCheckins());
    }

    private Fixture fixture(LocalDateTime agora) {
        ReservaAulaRepository reservaRepository = mock(ReservaAulaRepository.class);
        AulaTurmaRepository aulaTurmaRepository = mock(AulaTurmaRepository.class);
        PermissaoService permissaoService = mock(PermissaoService.class);
        Clock clock = Clock.fixed(agora.atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());

        Academia academia = academia();
        Aluno aluno = aluno(academia);
        AulaTurma aula = aulaTurma(academia);
        Usuario usuario = usuario(aluno, academia);

        when(permissaoService.usuarioAtual()).thenReturn(usuario);
        when(permissaoService.academiaAtualId()).thenReturn(1L);
        when(aulaTurmaRepository.findByIdAndAcademiaIdForUpdate(10L, 1L)).thenReturn(Optional.of(aula));
        when(aulaTurmaRepository.findByIdAndAcademiaId(10L, 1L)).thenReturn(Optional.of(aula));
        when(reservaRepository.countByAcademiaIdAndAulaTurmaIdAndStatus(1L, 10L, StatusReserva.CONFIRMADA)).thenReturn(0L);
        when(reservaRepository.countReservasConfirmadasNaSemana(eq(1L), eq(20L), eq(StatusReserva.CONFIRMADA), any(), any()))
                .thenReturn(0L);
        when(reservaRepository.findByAcademiaIdAndAulaTurmaIdAndAlunoIdAndStatus(1L, 10L, 20L, StatusReserva.CONFIRMADA))
                .thenReturn(Optional.empty());
        when(reservaRepository.findAllByAcademiaIdAndAulaTurmaIdOrderByReservadoEmAsc(1L, 10L))
                .thenReturn(List.of());
        when(reservaRepository.save(any(ReservaAula.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReservaAulaService service = new ReservaAulaService(reservaRepository, aulaTurmaRepository, permissaoService, clock);
        return new Fixture(reservaRepository, aulaTurmaRepository, permissaoService, service, aluno, aula);
    }

    private ReservaAula reserva(AulaTurma aula, Aluno aluno) {
        ReservaAula reserva = new ReservaAula();
        reserva.setAcademia(aula.getAcademia());
        reserva.setAulaTurma(aula);
        reserva.setAluno(aluno);
        reserva.setStatus(StatusReserva.CONFIRMADA);
        reserva.setReservadoEm(LocalDateTime.of(2026, 7, 21, 18, 0));
        reserva.setChaveReservaAtiva("10:20");
        return reserva;
    }

    private Academia academia() {
        Academia academia = new Academia();
        academia.setId(1L);
        return academia;
    }

    private Aluno aluno(Academia academia) {
        Aluno aluno = new Aluno();
        aluno.setId(20L);
        aluno.setNome("Aluno Turma");
        aluno.setTipoAluno(TipoAluno.TURMA);
        aluno.setAcademia(academia);
        aluno.setQuantidadeCheckins(0);
        aluno.setPlano(plano(academia));
        return aluno;
    }

    private Plano plano(Academia academia) {
        Plano plano = new Plano();
        plano.setId(30L);
        plano.setAcademia(academia);
        plano.setTipo(TipoPlano.TURMA);
        plano.setQuantidadeAulasSemana(3);
        plano.setValor(BigDecimal.valueOf(180));
        plano.setAtivo(true);
        return plano;
    }

    private AulaTurma aulaTurma(Academia academia) {
        Professor professor = new Professor();
        professor.setId(40L);
        professor.setAcademia(academia);

        AulaTurma aula = new AulaTurma();
        aula.setId(10L);
        aula.setAcademia(academia);
        aula.setProfessor(professor);
        aula.setData(DATA_AULA);
        aula.setHoraInicio(LocalTime.of(19, 0));
        aula.setHoraFim(LocalTime.of(20, 0));
        aula.setCapacidadeMaxima(15);
        aula.setStatus(StatusAula.AGENDADA);
        return aula;
    }

    private Usuario usuario(Aluno aluno, Academia academia) {
        Usuario usuario = new Usuario();
        usuario.setId(50L);
        usuario.setAluno(aluno);
        usuario.setAcademia(academia);
        usuario.setPerfil(PerfilUsuario.ALUNO);
        return usuario;
    }

    private record Fixture(ReservaAulaRepository reservaRepository,
                           AulaTurmaRepository aulaTurmaRepository,
                           PermissaoService permissaoService,
                           ReservaAulaService service,
                           Aluno aluno,
                           AulaTurma aula) {
    }
}
