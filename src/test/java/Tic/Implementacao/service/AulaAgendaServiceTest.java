package Tic.Implementacao.service;

import Tic.Implementacao.dto.aula.RemarcarAulaRequest;
import Tic.Implementacao.model.*;
import Tic.Implementacao.repository.*;
import Tic.Implementacao.security.PermissaoService;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AulaAgendaServiceTest {

    @Test
    void deveGerarAulaDeTurma() {
        Fixture fx = fixture();
        HorarioTurma horario = horarioTurma();
        when(fx.horarioTurmaRepository.findByIdAndAcademiaId(1L, 1L)).thenReturn(Optional.of(horario));
        when(fx.aulaTurmaRepository.findByAcademiaIdAndHorarioTurmaIdAndData(1L, 1L, LocalDate.of(2026, 7, 20)))
                .thenReturn(Optional.empty());

        AulaTurma aula = fx.service.gerarAulaTurma(1L, LocalDate.of(2026, 7, 20));

        assertEquals(horario.getProfessor(), aula.getProfessor());
        assertEquals(horario.getModalidade(), aula.getModalidade());
        assertEquals(horario.getCapacidadeMaxima(), aula.getCapacidadeMaxima());
        assertEquals(StatusAula.AGENDADA, aula.getStatus());
        verify(fx.aulaTurmaRepository).save(any(AulaTurma.class));
    }

    @Test
    void deveGerarAulaPersonal() {
        Fixture fx = fixture();
        AgendaPersonal agenda = agendaPersonal();
        when(fx.agendaPersonalRepository.findByIdAndAcademiaId(2L, 1L)).thenReturn(Optional.of(agenda));
        when(fx.aulaPersonalRepository.findByAcademiaIdAndAgendaPersonalIdAndData(1L, 2L, LocalDate.of(2026, 7, 20)))
                .thenReturn(Optional.empty());

        AulaPersonal aula = fx.service.gerarAulaPersonal(2L, LocalDate.of(2026, 7, 20));

        assertEquals(agenda.getAluno(), aula.getAluno());
        assertEquals(agenda.getProfessor(), aula.getProfessor());
        assertEquals(StatusAula.AGENDADA, aula.getStatus());
        verify(fx.aulaPersonalRepository).save(any(AulaPersonal.class));
    }

    @Test
    void naoDeveGerarEmDiaDaSemanaIncorreto() {
        Fixture fx = fixture();
        when(fx.horarioTurmaRepository.findByIdAndAcademiaId(1L, 1L)).thenReturn(Optional.of(horarioTurma()));

        assertThrows(RuntimeException.class, () -> fx.service.gerarAulaTurma(1L, LocalDate.of(2026, 7, 21)));
        verify(fx.aulaTurmaRepository, never()).save(any());
    }

    @Test
    void deveRetornarAulaExistenteSemDuplicar() {
        Fixture fx = fixture();
        HorarioTurma horario = horarioTurma();
        AulaTurma existente = aulaTurma(horario, LocalDate.of(2026, 7, 20));
        existente.setId(99L);

        when(fx.horarioTurmaRepository.findByIdAndAcademiaId(1L, 1L)).thenReturn(Optional.of(horario));
        when(fx.aulaTurmaRepository.findByAcademiaIdAndHorarioTurmaIdAndData(1L, 1L, LocalDate.of(2026, 7, 20)))
                .thenReturn(Optional.of(existente));

        AulaTurma aula = fx.service.gerarAulaTurma(1L, LocalDate.of(2026, 7, 20));

        assertEquals(99L, aula.getId());
        verify(fx.aulaTurmaRepository, never()).save(any());
    }

    @Test
    void deveGerarAulasDoPeriodo() {
        Fixture fx = fixture();
        HorarioTurma horario = horarioTurma();
        AgendaPersonal agenda = agendaPersonal();

        when(fx.horarioTurmaRepository.findByAcademiaIdAndDiaSemanaAndAtivoTrue(eq(1L), any()))
                .thenAnswer(invocation -> invocation.getArgument(1) == DayOfWeek.MONDAY ? List.of(horario) : List.of());
        when(fx.agendaPersonalRepository.findByAcademiaIdAndDiaSemanaAndAtivoTrueAndDataInicioLessThanEqual(eq(1L), any(), any()))
                .thenAnswer(invocation -> invocation.getArgument(1) == DayOfWeek.MONDAY ? List.of(agenda) : List.of());
        when(fx.aulaTurmaRepository.findByAcademiaIdAndHorarioTurmaIdAndData(eq(1L), eq(1L), any()))
                .thenReturn(Optional.empty());
        when(fx.aulaPersonalRepository.findByAcademiaIdAndAgendaPersonalIdAndData(eq(1L), eq(2L), any()))
                .thenReturn(Optional.empty());

        fx.service.gerarAulasDoPeriodo(LocalDate.of(2026, 7, 20), LocalDate.of(2026, 7, 26));

        verify(fx.aulaTurmaRepository, times(1)).save(any(AulaTurma.class));
        verify(fx.aulaPersonalRepository, times(1)).save(any(AulaPersonal.class));
    }

    @Test
    void cancelarAulaNaoDesativaHorarioRecorrente() {
        Fixture fx = fixture();
        HorarioTurma horario = horarioTurma();
        AulaTurma aula = aulaTurma(horario, LocalDate.of(2026, 7, 20));
        when(fx.aulaTurmaRepository.findByIdAndAcademiaId(5L, 1L)).thenReturn(Optional.of(aula));

        AulaTurma cancelada = fx.service.cancelarAulaTurma(5L, "Professor ausente");

        assertEquals(StatusAula.CANCELADA, cancelada.getStatus());
        assertTrue(horario.getAtivo());
    }

    @Test
    void deveRemarcarPreservandoAulaOriginal() {
        Fixture fx = fixture();
        HorarioTurma horario = horarioTurma();
        AulaTurma original = aulaTurma(horario, LocalDate.of(2026, 7, 20));
        original.setId(5L);
        when(fx.aulaTurmaRepository.findByIdAndAcademiaId(5L, 1L)).thenReturn(Optional.of(original));
        when(fx.aulaTurmaRepository.findByAcademiaIdAndHorarioTurmaIdAndData(1L, 1L, LocalDate.of(2026, 7, 27)))
                .thenReturn(Optional.empty());

        RemarcarAulaRequest request = new RemarcarAulaRequest();
        request.setNovaData(LocalDate.of(2026, 7, 27));
        request.setNovaHoraInicio(LocalTime.of(9, 0));
        request.setNovaHoraFim(LocalTime.of(10, 0));
        request.setObservacao("Remarcada");

        AulaTurma nova = fx.service.remarcarAulaTurma(5L, request);

        assertEquals(StatusAula.REMARCADA, original.getStatus());
        assertEquals(original, nova.getAulaOriginal());
        assertEquals(LocalDate.of(2026, 7, 27), nova.getData());
    }

    @Test
    void naoDeveAlterarOuExcluirAulaRealizada() {
        Fixture fx = fixture();
        AulaTurma realizada = aulaTurma(horarioTurma(), LocalDate.of(2026, 7, 20));
        realizada.setStatus(StatusAula.REALIZADA);
        when(fx.aulaTurmaRepository.findByIdAndAcademiaId(5L, 1L)).thenReturn(Optional.of(realizada));

        assertThrows(RuntimeException.class, () -> fx.service.cancelarAulaTurma(5L, "cancelar"));
        assertThrows(RuntimeException.class, () -> fx.service.removerAulaTurma(5L));
    }

    @Test
    void naoDeveAlterarAulaDeOutraAcademia() {
        Fixture fx = fixture();
        when(fx.aulaTurmaRepository.findByIdAndAcademiaId(5L, 1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> fx.service.cancelarAulaTurma(5L, "outra academia"));
        verify(fx.aulaTurmaRepository, never()).save(any());
    }

    private Fixture fixture() {
        AulaTurmaRepository aulaTurmaRepository = mock(AulaTurmaRepository.class);
        AulaPersonalRepository aulaPersonalRepository = mock(AulaPersonalRepository.class);
        HorarioTurmaRepository horarioTurmaRepository = mock(HorarioTurmaRepository.class);
        AgendaPersonalRepository agendaPersonalRepository = mock(AgendaPersonalRepository.class);
        ProfessorRepository professorRepository = mock(ProfessorRepository.class);
        PermissaoService permissaoService = mock(PermissaoService.class);

        when(permissaoService.academiaAtualId()).thenReturn(1L);
        when(aulaTurmaRepository.findByAcademiaIdAndHorarioTurmaIdAndData(anyLong(), anyLong(), any()))
                .thenReturn(Optional.empty());
        when(aulaPersonalRepository.findByAcademiaIdAndAgendaPersonalIdAndData(anyLong(), anyLong(), any()))
                .thenReturn(Optional.empty());
        when(aulaTurmaRepository.save(any(AulaTurma.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(aulaPersonalRepository.save(any(AulaPersonal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AulaAgendaService service = new AulaAgendaService(aulaTurmaRepository, aulaPersonalRepository,
                horarioTurmaRepository, agendaPersonalRepository, professorRepository, permissaoService);
        return new Fixture(aulaTurmaRepository, aulaPersonalRepository, horarioTurmaRepository,
                agendaPersonalRepository, professorRepository, permissaoService, service);
    }

    private HorarioTurma horarioTurma() {
        HorarioTurma horario = new HorarioTurma();
        horario.setId(1L);
        horario.setAcademia(academia());
        horario.setProfessor(professor());
        horario.setModalidade(modalidade());
        horario.setDiaSemana(DayOfWeek.MONDAY);
        horario.setHoraInicial(LocalTime.of(8, 0));
        horario.setHoraFinal(LocalTime.of(9, 0));
        horario.setCapacidadeMaxima(12);
        horario.setAtivo(true);
        return horario;
    }

    private AgendaPersonal agendaPersonal() {
        AgendaPersonal agenda = new AgendaPersonal();
        agenda.setId(2L);
        agenda.setAcademia(academia());
        agenda.setAluno(aluno());
        agenda.setProfessor(professor());
        agenda.setDiaSemana(DayOfWeek.MONDAY);
        agenda.setHoraInicial(LocalTime.of(10, 0));
        agenda.setHoraFinal(LocalTime.of(11, 0));
        agenda.setDataInicio(LocalDate.of(2026, 7, 1));
        agenda.setAtivo(true);
        return agenda;
    }

    private AulaTurma aulaTurma(HorarioTurma horario, LocalDate data) {
        AulaTurma aula = new AulaTurma();
        aula.setId(5L);
        aula.setAcademia(horario.getAcademia());
        aula.setHorarioTurma(horario);
        aula.setProfessor(horario.getProfessor());
        aula.setModalidade(horario.getModalidade());
        aula.setData(data);
        aula.setHoraInicio(horario.getHoraInicial());
        aula.setHoraFim(horario.getHoraFinal());
        aula.setCapacidadeMaxima(horario.getCapacidadeMaxima());
        aula.setStatus(StatusAula.AGENDADA);
        aula.setCriadoEm(LocalDateTime.now());
        aula.setAtualizadoEm(LocalDateTime.now());
        return aula;
    }

    private Academia academia() {
        Academia academia = new Academia();
        academia.setId(1L);
        return academia;
    }

    private Professor professor() {
        Professor professor = new Professor();
        professor.setId(10L);
        professor.setNome("Professor Teste");
        professor.setAcademia(academia());
        return professor;
    }

    private Modalidade modalidade() {
        Modalidade modalidade = new Modalidade();
        modalidade.setId(20L);
        modalidade.setNome("Muay Thai");
        return modalidade;
    }

    private Aluno aluno() {
        Aluno aluno = new Aluno();
        aluno.setId(30L);
        aluno.setNome("Aluno Teste");
        aluno.setAcademia(academia());
        return aluno;
    }

    private record Fixture(AulaTurmaRepository aulaTurmaRepository,
                           AulaPersonalRepository aulaPersonalRepository,
                           HorarioTurmaRepository horarioTurmaRepository,
                           AgendaPersonalRepository agendaPersonalRepository,
                           ProfessorRepository professorRepository,
                           PermissaoService permissaoService,
                           AulaAgendaService service) {
    }
}
