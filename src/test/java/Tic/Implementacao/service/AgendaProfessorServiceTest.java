package Tic.Implementacao.service;

import Tic.Implementacao.dto.agenda.professor.*;
import Tic.Implementacao.model.*;
import Tic.Implementacao.repository.AulaPersonalRepository;
import Tic.Implementacao.repository.AulaTurmaRepository;
import Tic.Implementacao.repository.ProfessorRepository;
import Tic.Implementacao.repository.ReservaAulaRepository;
import Tic.Implementacao.security.PermissaoService;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AgendaProfessorServiceTest {

    private static final LocalDate TERCA = LocalDate.of(2026, 7, 14);

    @Test
    void agendaDiariaSomenteComTurma() {
        Fixture fx = fixtureAdmin();
        AulaTurma turma = aulaTurma(1L, TERCA, LocalTime.of(18, 0), StatusAula.AGENDADA, fx.academia, fx.professor);
        when(fx.aulaTurmaRepository.findByAcademiaIdAndProfessorIdAndData(1L, 10L, TERCA)).thenReturn(List.of(turma));
        when(fx.reservaAulaRepository.contarConfirmadasPorAulas(eq(1L), anyCollection(), eq(StatusReserva.CONFIRMADA)))
                .thenReturn(List.of(ocupacao(1L, 3L)));

        ResumoAgendaProfessorResponse response = fx.service.buscarAgendaDiaria(10L, TERCA);

        assertEquals(1, response.getTotalTurmas());
        assertEquals(0, response.getTotalPersonais());
        assertEquals(TipoItemAgenda.TURMA, response.getAgenda().getFirst().getTipo());
        assertEquals(3, response.getAgenda().getFirst().getConfirmados());
    }

    @Test
    void agendaDiariaSomenteComPersonal() {
        Fixture fx = fixtureAdmin();
        AulaPersonal personal = aulaPersonal(2L, TERCA, LocalTime.of(8, 0), StatusAula.AGENDADA, fx.academia, fx.professor, fx.aluno);
        when(fx.aulaPersonalRepository.findByAcademiaIdAndProfessorIdAndData(1L, 10L, TERCA)).thenReturn(List.of(personal));

        ResumoAgendaProfessorResponse response = fx.service.buscarAgendaDiaria(10L, TERCA);

        assertEquals(0, response.getTotalTurmas());
        assertEquals(1, response.getTotalPersonais());
        assertEquals(TipoItemAgenda.PERSONAL, response.getAgenda().getFirst().getTipo());
        assertEquals(fx.aluno.getNome(), response.getAgenda().getFirst().getAlunoNome());
    }

    @Test
    void agendaComTurmaEPersonalOrdenadaPorHorario() {
        Fixture fx = fixtureAdmin();
        AulaTurma turma = aulaTurma(1L, TERCA, LocalTime.of(18, 0), StatusAula.AGENDADA, fx.academia, fx.professor);
        AulaPersonal personal = aulaPersonal(2L, TERCA, LocalTime.of(8, 0), StatusAula.AGENDADA, fx.academia, fx.professor, fx.aluno);
        when(fx.aulaTurmaRepository.findByAcademiaIdAndProfessorIdAndData(1L, 10L, TERCA)).thenReturn(List.of(turma));
        when(fx.aulaPersonalRepository.findByAcademiaIdAndProfessorIdAndData(1L, 10L, TERCA)).thenReturn(List.of(personal));

        ResumoAgendaProfessorResponse response = fx.service.buscarAgendaDiaria(10L, TERCA);

        assertEquals(2, response.getTotalAulas());
        assertEquals(TipoItemAgenda.PERSONAL, response.getAgenda().get(0).getTipo());
        assertEquals(TipoItemAgenda.TURMA, response.getAgenda().get(1).getTipo());
    }

    @Test
    void calculaOcupacaoSemContarCanceladas() {
        Fixture fx = fixtureAdmin();
        AulaTurma turma = aulaTurma(1L, TERCA, LocalTime.of(18, 0), StatusAula.AGENDADA, fx.academia, fx.professor);
        when(fx.aulaTurmaRepository.findByAcademiaIdAndProfessorIdAndData(1L, 10L, TERCA)).thenReturn(List.of(turma));
        when(fx.reservaAulaRepository.contarConfirmadasPorAulas(eq(1L), anyCollection(), eq(StatusReserva.CONFIRMADA)))
                .thenReturn(List.of(ocupacao(1L, 12L)));

        ItemAgendaProfessorResponse item = fx.service.buscarAgendaDiaria(10L, TERCA).getAgenda().getFirst();

        assertEquals(12, item.getConfirmados());
        assertEquals(3, item.getVagasDisponiveis());
        verify(fx.reservaAulaRepository).contarConfirmadasPorAulas(eq(1L), anyCollection(), eq(StatusReserva.CONFIRMADA));
    }

    @Test
    void identificaTurmaLotadaEResumoDiario() {
        Fixture fx = fixtureAdmin();
        AulaTurma turma = aulaTurma(1L, TERCA, LocalTime.of(18, 0), StatusAula.AGENDADA, fx.academia, fx.professor);
        when(fx.aulaTurmaRepository.findByAcademiaIdAndProfessorIdAndData(1L, 10L, TERCA)).thenReturn(List.of(turma));
        when(fx.reservaAulaRepository.contarConfirmadasPorAulas(eq(1L), anyCollection(), eq(StatusReserva.CONFIRMADA)))
                .thenReturn(List.of(ocupacao(1L, 15L)));

        ResumoAgendaProfessorResponse response = fx.service.buscarAgendaDiaria(10L, TERCA);

        assertEquals(15, response.getTotalConfirmados());
        assertEquals(0, response.getTotalVagasDisponiveis());
        assertEquals(1, response.getTurmasLotadas());
        assertTrue(response.getAgenda().getFirst().getLotada());
    }

    @Test
    void resumoSemanalNormalizaParaSegundaEIncluiSeteDias() {
        Fixture fx = fixtureAdmin();
        AulaTurma turma = aulaTurma(1L, TERCA, LocalTime.of(18, 0), StatusAula.AGENDADA, fx.academia, fx.professor);
        AulaPersonal personal = aulaPersonal(2L, TERCA.plusDays(1), LocalTime.of(9, 0), StatusAula.AGENDADA, fx.academia, fx.professor, fx.aluno);
        when(fx.aulaTurmaRepository.findByAcademiaIdAndProfessorIdAndDataBetween(1L, 10L, LocalDate.of(2026, 7, 13), LocalDate.of(2026, 7, 19)))
                .thenReturn(List.of(turma));
        when(fx.aulaPersonalRepository.findByAcademiaIdAndProfessorIdAndDataBetween(1L, 10L, LocalDate.of(2026, 7, 13), LocalDate.of(2026, 7, 19)))
                .thenReturn(List.of(personal));
        when(fx.reservaAulaRepository.contarConfirmadasPorAulas(eq(1L), anyCollection(), eq(StatusReserva.CONFIRMADA)))
                .thenReturn(List.of(ocupacao(1L, 4L)));

        AgendaSemanalProfessorResponse response = fx.service.buscarAgendaSemanal(10L, TERCA);

        assertEquals(LocalDate.of(2026, 7, 13), response.getInicio());
        assertEquals(LocalDate.of(2026, 7, 19), response.getFim());
        assertEquals(7, response.getDias().size());
        assertEquals(1, response.getTotalTurmas());
        assertEquals(1, response.getTotalPersonais());
        assertEquals(4, response.getTotalConfirmados());
    }

    @Test
    void exibeAulaCanceladaERemarcada() {
        Fixture fx = fixtureAdmin();
        AulaTurma cancelada = aulaTurma(1L, TERCA, LocalTime.of(18, 0), StatusAula.CANCELADA, fx.academia, fx.professor);
        AulaTurma originalRemarcada = aulaTurma(2L, TERCA, LocalTime.of(19, 0), StatusAula.REMARCADA, fx.academia, fx.professor);
        AulaTurma nova = aulaTurma(3L, TERCA, LocalTime.of(20, 0), StatusAula.AGENDADA, fx.academia, fx.professor);
        nova.setAulaOriginal(originalRemarcada);
        when(fx.aulaTurmaRepository.findByAcademiaIdAndProfessorIdAndData(1L, 10L, TERCA)).thenReturn(List.of(cancelada, originalRemarcada, nova));

        List<ItemAgendaProfessorResponse> itens = fx.service.buscarAgendaDiaria(10L, TERCA).getAgenda();

        assertEquals(StatusAula.CANCELADA, itens.get(0).getStatus());
        assertEquals(StatusAula.REMARCADA, itens.get(1).getStatus());
        assertEquals(2L, itens.get(2).getAulaOriginalId());
    }

    @Test
    void professorSemAulasRetornaResumoVazio() {
        Fixture fx = fixtureAdmin();

        ResumoAgendaProfessorResponse response = fx.service.buscarAgendaDiaria(10L, TERCA);

        assertEquals(0, response.getTotalAulas());
        assertTrue(response.getAgenda().isEmpty());
    }

    @Test
    void bloqueiaProfessorDeOutraAcademia() {
        Fixture fx = fixtureAdmin();
        when(fx.professorRepository.findByIdAndAcademiaId(99L, 1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> fx.service.buscarAgendaDiaria(99L, TERCA));
    }

    @Test
    void bloqueiaProfessorTentandoAcessarOutroProfessor() {
        Fixture fx = fixtureProfessor();
        when(fx.professorRepository.findByIdAndAcademiaId(11L, 1L)).thenReturn(Optional.of(professor(11L, fx.academia)));

        assertThrows(AccessDeniedException.class, () -> fx.service.buscarAgendaDiaria(11L, TERCA));
    }

    @Test
    void administradorConsultaProfessorPermitido() {
        Fixture fx = fixtureAdmin();

        ResumoAgendaProfessorResponse response = fx.service.buscarAgendaDiaria(10L, TERCA);

        assertEquals(10L, response.getProfessorId());
    }

    @Test
    void detalheDaTurmaTrazAlunosConfirmados() {
        Fixture fx = fixtureAdmin();
        AulaTurma turma = aulaTurma(1L, TERCA, LocalTime.of(18, 0), StatusAula.AGENDADA, fx.academia, fx.professor);
        when(fx.aulaTurmaRepository.buscarDetalhePorId(1L, 1L)).thenReturn(Optional.of(turma));
        when(fx.reservaAulaRepository.buscarConfirmadasComAluno(1L, 1L, StatusReserva.CONFIRMADA))
                .thenReturn(List.of(reserva(turma, fx.aluno)));

        DetalheAulaTurmaProfessorResponse response = fx.service.buscarDetalheTurma(1L);

        assertEquals(1, response.getConfirmados());
        assertEquals(14, response.getVagasDisponiveis());
        assertEquals(fx.aluno.getNome(), response.getAlunosConfirmados().getFirst().getNome());
    }

    @Test
    void detalheDoPersonal() {
        Fixture fx = fixtureAdmin();
        AulaPersonal personal = aulaPersonal(2L, TERCA, LocalTime.of(8, 0), StatusAula.AGENDADA, fx.academia, fx.professor, fx.aluno);
        when(fx.aulaPersonalRepository.buscarDetalhePorId(2L, 1L)).thenReturn(Optional.of(personal));

        DetalheAulaPersonalProfessorResponse response = fx.service.buscarDetalhePersonal(2L);

        assertEquals(2L, response.getAulaId());
        assertEquals(fx.aluno.getId(), response.getAlunoId());
        assertEquals(100L, response.getAgendaPersonalId());
    }

    @Test
    void consultasNaoAlteramDados() {
        Fixture fx = fixtureAdmin();

        fx.service.buscarAgendaDiaria(10L, TERCA);

        verify(fx.aulaTurmaRepository, never()).save(any());
        verify(fx.aulaPersonalRepository, never()).save(any());
        verify(fx.reservaAulaRepository, never()).save(any());
    }

    private Fixture fixtureAdmin() {
        return fixture(PerfilUsuario.ADMIN);
    }

    private Fixture fixtureProfessor() {
        return fixture(PerfilUsuario.PROFESSOR);
    }

    private Fixture fixture(PerfilUsuario perfil) {
        AulaTurmaRepository aulaTurmaRepository = mock(AulaTurmaRepository.class);
        AulaPersonalRepository aulaPersonalRepository = mock(AulaPersonalRepository.class);
        ReservaAulaRepository reservaAulaRepository = mock(ReservaAulaRepository.class);
        ProfessorRepository professorRepository = mock(ProfessorRepository.class);
        PermissaoService permissaoService = mock(PermissaoService.class);

        Academia academia = academia();
        Professor professor = professor(10L, academia);
        Aluno aluno = aluno(20L, academia);
        Usuario usuario = usuario(perfil, academia, professor);

        when(permissaoService.usuarioAtual()).thenReturn(usuario);
        when(permissaoService.academiaAtualId()).thenReturn(1L);
        when(professorRepository.findByIdAndAcademiaId(10L, 1L)).thenReturn(Optional.of(professor));
        when(reservaAulaRepository.contarConfirmadasPorAulas(anyLong(), anyCollection(), eq(StatusReserva.CONFIRMADA)))
                .thenReturn(List.of());

        AgendaProfessorService service = new AgendaProfessorService(aulaTurmaRepository, aulaPersonalRepository,
                reservaAulaRepository, professorRepository, permissaoService);
        return new Fixture(aulaTurmaRepository, aulaPersonalRepository, reservaAulaRepository, professorRepository,
                permissaoService, service, academia, professor, aluno);
    }

    private ReservaAulaRepository.OcupacaoAulaProjection ocupacao(Long aulaId, Long confirmados) {
        return new ReservaAulaRepository.OcupacaoAulaProjection() {
            @Override
            public Long getAulaId() {
                return aulaId;
            }

            @Override
            public Long getConfirmados() {
                return confirmados;
            }
        };
    }

    private AulaTurma aulaTurma(Long id, LocalDate data, LocalTime inicio, StatusAula status, Academia academia, Professor professor) {
        Modalidade modalidade = new Modalidade();
        modalidade.setId(30L);
        modalidade.setNome("Muay Thai");

        AulaTurma aula = new AulaTurma();
        aula.setId(id);
        aula.setAcademia(academia);
        aula.setProfessor(professor);
        aula.setModalidade(modalidade);
        aula.setData(data);
        aula.setHoraInicio(inicio);
        aula.setHoraFim(inicio.plusHours(1));
        aula.setCapacidadeMaxima(15);
        aula.setStatus(status);
        return aula;
    }

    private AulaPersonal aulaPersonal(Long id, LocalDate data, LocalTime inicio, StatusAula status, Academia academia,
                                      Professor professor, Aluno aluno) {
        AgendaPersonal agenda = new AgendaPersonal();
        agenda.setId(100L);
        agenda.setAcademia(academia);
        agenda.setProfessor(professor);
        agenda.setAluno(aluno);

        AulaPersonal aula = new AulaPersonal();
        aula.setId(id);
        aula.setAcademia(academia);
        aula.setAgendaPersonal(agenda);
        aula.setAluno(aluno);
        aula.setProfessor(professor);
        aula.setData(data);
        aula.setHoraInicio(inicio);
        aula.setHoraFim(inicio.plusHours(1));
        aula.setStatus(status);
        return aula;
    }

    private ReservaAula reserva(AulaTurma aula, Aluno aluno) {
        ReservaAula reserva = new ReservaAula();
        reserva.setAulaTurma(aula);
        reserva.setAluno(aluno);
        reserva.setStatus(StatusReserva.CONFIRMADA);
        reserva.setReservadoEm(LocalDateTime.of(2026, 7, 14, 17, 0));
        return reserva;
    }

    private Academia academia() {
        Academia academia = new Academia();
        academia.setId(1L);
        academia.setNome("Elite");
        return academia;
    }

    private Professor professor(Long id, Academia academia) {
        Professor professor = new Professor();
        professor.setId(id);
        professor.setNome("Professor Teste");
        professor.setAcademia(academia);
        return professor;
    }

    private Aluno aluno(Long id, Academia academia) {
        Aluno aluno = new Aluno();
        aluno.setId(id);
        aluno.setNome("Aluno Teste");
        aluno.setAcademia(academia);
        return aluno;
    }

    private Usuario usuario(PerfilUsuario perfil, Academia academia, Professor professor) {
        Usuario usuario = new Usuario();
        usuario.setId(50L);
        usuario.setPerfil(perfil);
        usuario.setAcademia(academia);
        if (perfil == PerfilUsuario.PROFESSOR) {
            usuario.setProfessor(professor);
        }
        return usuario;
    }

    private record Fixture(AulaTurmaRepository aulaTurmaRepository,
                           AulaPersonalRepository aulaPersonalRepository,
                           ReservaAulaRepository reservaAulaRepository,
                           ProfessorRepository professorRepository,
                           PermissaoService permissaoService,
                           AgendaProfessorService service,
                           Academia academia,
                           Professor professor,
                           Aluno aluno) {
    }
}
