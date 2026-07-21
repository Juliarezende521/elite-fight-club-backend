package Tic.Implementacao.service;

import Tic.Implementacao.dto.agenda.AgendaPersonalRequest;
import Tic.Implementacao.dto.agenda.HorarioTurmaRequest;
import Tic.Implementacao.model.*;
import Tic.Implementacao.repository.*;
import Tic.Implementacao.security.PermissaoService;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AgendaServiceTest {

    @Test
    void naoDeveCadastrarTurmaQuandoProfessorJaTemTurmaNoMesmoHorario() {
        Fixture fx = fixture();
        HorarioTurma existente = horarioTurma(LocalTime.of(8, 0), LocalTime.of(9, 0));

        when(fx.horarioTurmaRepository.findByAcademiaIdAndProfessorIdAndDiaSemanaAndAtivoTrue(1L, 10L, DayOfWeek.MONDAY))
                .thenReturn(List.of(existente));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> fx.service.cadastrarHorarioTurma(horarioTurmaRequest(LocalTime.of(8, 30), LocalTime.of(9, 30))));

        assertTrue(ex.getMessage().contains("turma"));
        verify(fx.horarioTurmaRepository, never()).save(any());
    }

    @Test
    void naoDeveCadastrarPersonalQuandoProfessorJaTemTurmaNoMesmoHorario() {
        Fixture fx = fixture();
        HorarioTurma existente = horarioTurma(LocalTime.of(8, 0), LocalTime.of(9, 0));

        when(fx.horarioTurmaRepository.findByAcademiaIdAndProfessorIdAndDiaSemanaAndAtivoTrue(1L, 10L, DayOfWeek.MONDAY))
                .thenReturn(List.of(existente));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> fx.service.cadastrarAgendaPersonal(agendaPersonalRequest(LocalTime.of(8, 30), LocalTime.of(9, 30))));

        assertTrue(ex.getMessage().contains("turma"));
        verify(fx.agendaPersonalRepository, never()).save(any());
    }

    @Test
    void naoDeveCadastrarDoisPersonaisSimultaneos() {
        Fixture fx = fixture();
        AgendaPersonal existente = agendaPersonal(LocalTime.of(10, 0), LocalTime.of(11, 0));

        when(fx.agendaPersonalRepository.findByAcademiaIdAndProfessorIdAndDiaSemanaAndAtivoTrue(1L, 10L, DayOfWeek.MONDAY))
                .thenReturn(List.of(existente));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> fx.service.cadastrarAgendaPersonal(agendaPersonalRequest(LocalTime.of(10, 30), LocalTime.of(11, 30))));

        assertTrue(ex.getMessage().contains("personal"));
        verify(fx.agendaPersonalRepository, never()).save(any());
    }

    @Test
    void devePermitirHorariosEncostadosSemSobreposicao() {
        Fixture fx = fixture();
        HorarioTurma existente = horarioTurma(LocalTime.of(8, 0), LocalTime.of(9, 0));

        when(fx.horarioTurmaRepository.findByAcademiaIdAndProfessorIdAndDiaSemanaAndAtivoTrue(1L, 10L, DayOfWeek.MONDAY))
                .thenReturn(List.of(existente));
        when(fx.horarioTurmaRepository.save(any(HorarioTurma.class))).thenAnswer(invocation -> invocation.getArgument(0));

        HorarioTurma salvo = fx.service.cadastrarHorarioTurma(horarioTurmaRequest(LocalTime.of(9, 0), LocalTime.of(10, 0)));

        assertEquals(LocalTime.of(9, 0), salvo.getHoraInicial());
        verify(fx.horarioTurmaRepository).save(any(HorarioTurma.class));
    }

    @Test
    void deveListarAgendaDoProfessorPorData() {
        Fixture fx = fixture();
        HorarioTurma turma = horarioTurma(LocalTime.of(8, 0), LocalTime.of(9, 0));
        AgendaPersonal personal = agendaPersonal(LocalTime.of(10, 0), LocalTime.of(11, 0));

        when(fx.horarioTurmaRepository.findByAcademiaIdAndProfessorIdAndDiaSemanaAndAtivoTrue(1L, 10L, DayOfWeek.MONDAY))
                .thenReturn(List.of(turma));
        when(fx.agendaPersonalRepository.findByAcademiaIdAndProfessorIdAndDiaSemanaAndAtivoTrueAndDataInicioLessThanEqual(1L, 10L, DayOfWeek.MONDAY, LocalDate.of(2026, 7, 20)))
                .thenReturn(List.of(personal));

        var agenda = fx.service.listarAgendaProfessorPorData(10L, LocalDate.of(2026, 7, 20));

        assertEquals(2, agenda.size());
        assertEquals("TURMA", agenda.get(0).getTipo());
        assertEquals("PERSONAL", agenda.get(1).getTipo());
    }

    private Fixture fixture() {
        HorarioTurmaRepository horarioTurmaRepository = mock(HorarioTurmaRepository.class);
        AgendaPersonalRepository agendaPersonalRepository = mock(AgendaPersonalRepository.class);
        ProfessorRepository professorRepository = mock(ProfessorRepository.class);
        ModalidadeRepository modalidadeRepository = mock(ModalidadeRepository.class);
        AlunoRepository alunoRepository = mock(AlunoRepository.class);
        PermissaoService permissaoService = mock(PermissaoService.class);

        Academia academia = academia();
        Professor professor = professor(academia);
        Modalidade modalidade = modalidade();
        Aluno aluno = aluno(academia);

        when(permissaoService.academiaAtualId()).thenReturn(1L);
        when(permissaoService.academiaAtual()).thenReturn(academia);
        when(professorRepository.findByIdAndAcademiaId(10L, 1L)).thenReturn(Optional.of(professor));
        when(modalidadeRepository.findByIdAndAcademiaId(20L, 1L)).thenReturn(Optional.of(modalidade));
        when(alunoRepository.findByIdAndAcademiaId(30L, 1L)).thenReturn(Optional.of(aluno));

        AgendaService service = new AgendaService(horarioTurmaRepository, agendaPersonalRepository,
                professorRepository, modalidadeRepository, alunoRepository, permissaoService);
        return new Fixture(horarioTurmaRepository, agendaPersonalRepository, professorRepository,
                modalidadeRepository, alunoRepository, permissaoService, service);
    }

    private HorarioTurmaRequest horarioTurmaRequest(LocalTime inicio, LocalTime fim) {
        HorarioTurmaRequest request = new HorarioTurmaRequest();
        request.setProfessorId(10L);
        request.setModalidadeId(20L);
        request.setDiaSemana(DayOfWeek.MONDAY);
        request.setHoraInicial(inicio);
        request.setHoraFinal(fim);
        request.setCapacidadeMaxima(12);
        return request;
    }

    private AgendaPersonalRequest agendaPersonalRequest(LocalTime inicio, LocalTime fim) {
        AgendaPersonalRequest request = new AgendaPersonalRequest();
        request.setAlunoId(30L);
        request.setProfessorId(10L);
        request.setDiaSemana(DayOfWeek.MONDAY);
        request.setHoraInicial(inicio);
        request.setHoraFinal(fim);
        request.setDataInicio(LocalDate.of(2026, 7, 14));
        return request;
    }

    private HorarioTurma horarioTurma(LocalTime inicio, LocalTime fim) {
        HorarioTurma horario = new HorarioTurma();
        horario.setId(1L);
        horario.setProfessor(professor(academia()));
        horario.setModalidade(modalidade());
        horario.setDiaSemana(DayOfWeek.MONDAY);
        horario.setHoraInicial(inicio);
        horario.setHoraFinal(fim);
        horario.setCapacidadeMaxima(10);
        horario.setAtivo(true);
        return horario;
    }

    private AgendaPersonal agendaPersonal(LocalTime inicio, LocalTime fim) {
        AgendaPersonal agenda = new AgendaPersonal();
        agenda.setId(2L);
        agenda.setProfessor(professor(academia()));
        agenda.setAluno(aluno(academia()));
        agenda.setDiaSemana(DayOfWeek.MONDAY);
        agenda.setHoraInicial(inicio);
        agenda.setHoraFinal(fim);
        agenda.setDataInicio(LocalDate.of(2026, 7, 1));
        agenda.setAtivo(true);
        return agenda;
    }

    private Academia academia() {
        Academia academia = new Academia();
        academia.setId(1L);
        return academia;
    }

    private Professor professor(Academia academia) {
        Professor professor = new Professor();
        professor.setId(10L);
        professor.setNome("Professor Teste");
        professor.setAcademia(academia);
        return professor;
    }

    private Modalidade modalidade() {
        Modalidade modalidade = new Modalidade();
        modalidade.setId(20L);
        modalidade.setNome("Muay Thai");
        modalidade.setAcademia(academia());
        return modalidade;
    }

    private Aluno aluno(Academia academia) {
        Aluno aluno = new Aluno();
        aluno.setId(30L);
        aluno.setNome("Aluno Teste");
        aluno.setAcademia(academia);
        return aluno;
    }

    private record Fixture(HorarioTurmaRepository horarioTurmaRepository,
                           AgendaPersonalRepository agendaPersonalRepository,
                           ProfessorRepository professorRepository,
                           ModalidadeRepository modalidadeRepository,
                           AlunoRepository alunoRepository,
                           PermissaoService permissaoService,
                           AgendaService service) {
    }
}
