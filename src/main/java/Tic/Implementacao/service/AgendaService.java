package Tic.Implementacao.service;

import Tic.Implementacao.dto.agenda.AgendaPersonalRequest;
import Tic.Implementacao.dto.agenda.HorarioTurmaRequest;
import Tic.Implementacao.dto.agenda.ItemAgendaProfessorResponse;
import Tic.Implementacao.model.*;
import Tic.Implementacao.repository.*;
import Tic.Implementacao.security.PermissaoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class AgendaService {

    private final HorarioTurmaRepository horarioTurmaRepository;
    private final AgendaPersonalRepository agendaPersonalRepository;
    private final ProfessorRepository professorRepository;
    private final ModalidadeRepository modalidadeRepository;
    private final AlunoRepository alunoRepository;
    private final PermissaoService permissaoService;

    public AgendaService(HorarioTurmaRepository horarioTurmaRepository,
                         AgendaPersonalRepository agendaPersonalRepository,
                         ProfessorRepository professorRepository,
                         ModalidadeRepository modalidadeRepository,
                         AlunoRepository alunoRepository,
                         PermissaoService permissaoService) {
        this.horarioTurmaRepository = horarioTurmaRepository;
        this.agendaPersonalRepository = agendaPersonalRepository;
        this.professorRepository = professorRepository;
        this.modalidadeRepository = modalidadeRepository;
        this.alunoRepository = alunoRepository;
        this.permissaoService = permissaoService;
    }

    @Transactional
    public HorarioTurma cadastrarHorarioTurma(HorarioTurmaRequest request) {
        permissaoService.exigirAdminOuProfessor();
        validarIntervalo(request.getHoraInicial(), request.getHoraFinal());
        Long academiaId = permissaoService.academiaAtualId();
        Professor professor = buscarProfessor(request.getProfessorId(), academiaId);
        Modalidade modalidade = modalidadeRepository.findByIdAndAcademiaId(request.getModalidadeId(), academiaId)
                .orElseThrow(() -> new RuntimeException("Modalidade não encontrada com ID: " + request.getModalidadeId()));

        validarConflito(professor.getId(), request.getDiaSemana(), request.getHoraInicial(), request.getHoraFinal(), null, null);

        HorarioTurma horario = new HorarioTurma();
        horario.setAcademia(permissaoService.academiaAtual());
        horario.setProfessor(professor);
        horario.setModalidade(modalidade);
        horario.setDiaSemana(request.getDiaSemana());
        horario.setHoraInicial(request.getHoraInicial());
        horario.setHoraFinal(request.getHoraFinal());
        horario.setCapacidadeMaxima(request.getCapacidadeMaxima());
        horario.setAtivo(true);
        return horarioTurmaRepository.save(horario);
    }

    @Transactional
    public AgendaPersonal cadastrarAgendaPersonal(AgendaPersonalRequest request) {
        permissaoService.exigirAdminOuProfessor();
        validarIntervalo(request.getHoraInicial(), request.getHoraFinal());
        Long academiaId = permissaoService.academiaAtualId();
        Professor professor = buscarProfessor(request.getProfessorId(), academiaId);
        Aluno aluno = alunoRepository.findByIdAndAcademiaId(request.getAlunoId(), academiaId)
                .orElseThrow(() -> new RuntimeException("Aluno não encontrado com ID: " + request.getAlunoId()));

        validarConflito(professor.getId(), request.getDiaSemana(), request.getHoraInicial(), request.getHoraFinal(), null, null);

        AgendaPersonal agenda = new AgendaPersonal();
        agenda.setAcademia(permissaoService.academiaAtual());
        agenda.setAluno(aluno);
        agenda.setProfessor(professor);
        agenda.setDiaSemana(request.getDiaSemana());
        agenda.setHoraInicial(request.getHoraInicial());
        agenda.setHoraFinal(request.getHoraFinal());
        agenda.setDataInicio(request.getDataInicio());
        agenda.setAtivo(true);
        return agendaPersonalRepository.save(agenda);
    }

    public List<ItemAgendaProfessorResponse> listarAgendaProfessorPorData(Long professorId, LocalDate data) {
        Long academiaId = permissaoService.academiaAtualId();
        buscarProfessor(professorId, academiaId);
        DayOfWeek diaSemana = data.getDayOfWeek();
        List<ItemAgendaProfessorResponse> agenda = new ArrayList<>();

        horarioTurmaRepository.findByAcademiaIdAndProfessorIdAndDiaSemanaAndAtivoTrue(academiaId, professorId, diaSemana)
                .forEach(horario -> agenda.add(new ItemAgendaProfessorResponse(
                        "TURMA",
                        horario.getId(),
                        null,
                        horario.getModalidade().getNome(),
                        horario.getHoraInicial(),
                        horario.getHoraFinal(),
                        horario.getCapacidadeMaxima()
                )));

        agendaPersonalRepository.findByAcademiaIdAndProfessorIdAndDiaSemanaAndAtivoTrueAndDataInicioLessThanEqual(academiaId, professorId, diaSemana, data)
                .forEach(personal -> agenda.add(new ItemAgendaProfessorResponse(
                        "PERSONAL",
                        personal.getId(),
                        personal.getAluno().getNome(),
                        null,
                        personal.getHoraInicial(),
                        personal.getHoraFinal(),
                        null
                )));

        agenda.sort(Comparator.comparing(ItemAgendaProfessorResponse::getHoraInicial));
        return agenda;
    }

    @Transactional
    public HorarioTurma atualizarHorarioTurma(Long id, HorarioTurmaRequest request) {
        permissaoService.exigirAdminOuProfessor();
        validarIntervalo(request.getHoraInicial(), request.getHoraFinal());
        Long academiaId = permissaoService.academiaAtualId();
        HorarioTurma horario = horarioTurmaRepository.findByIdAndAcademiaId(id, academiaId)
                .orElseThrow(() -> new RuntimeException("Horário de turma não encontrado com ID: " + id));
        Professor professor = buscarProfessor(request.getProfessorId(), academiaId);
        Modalidade modalidade = modalidadeRepository.findByIdAndAcademiaId(request.getModalidadeId(), academiaId)
                .orElseThrow(() -> new RuntimeException("Modalidade não encontrada com ID: " + request.getModalidadeId()));

        validarConflito(professor.getId(), request.getDiaSemana(), request.getHoraInicial(), request.getHoraFinal(), id, null);

        horario.setProfessor(professor);
        horario.setModalidade(modalidade);
        horario.setDiaSemana(request.getDiaSemana());
        horario.setHoraInicial(request.getHoraInicial());
        horario.setHoraFinal(request.getHoraFinal());
        horario.setCapacidadeMaxima(request.getCapacidadeMaxima());
        return horarioTurmaRepository.save(horario);
    }

    @Transactional
    public AgendaPersonal atualizarAgendaPersonal(Long id, AgendaPersonalRequest request) {
        permissaoService.exigirAdminOuProfessor();
        validarIntervalo(request.getHoraInicial(), request.getHoraFinal());
        Long academiaId = permissaoService.academiaAtualId();
        AgendaPersonal agenda = agendaPersonalRepository.findByIdAndAcademiaId(id, academiaId)
                .orElseThrow(() -> new RuntimeException("Agenda personal não encontrada com ID: " + id));
        Professor professor = buscarProfessor(request.getProfessorId(), academiaId);
        Aluno aluno = alunoRepository.findByIdAndAcademiaId(request.getAlunoId(), academiaId)
                .orElseThrow(() -> new RuntimeException("Aluno não encontrado com ID: " + request.getAlunoId()));

        validarConflito(professor.getId(), request.getDiaSemana(), request.getHoraInicial(), request.getHoraFinal(), null, id);

        agenda.setProfessor(professor);
        agenda.setAluno(aluno);
        agenda.setDiaSemana(request.getDiaSemana());
        agenda.setHoraInicial(request.getHoraInicial());
        agenda.setHoraFinal(request.getHoraFinal());
        agenda.setDataInicio(request.getDataInicio());
        return agendaPersonalRepository.save(agenda);
    }

    @Transactional
    public HorarioTurma alterarStatusHorarioTurma(Long id, boolean ativo) {
        permissaoService.exigirAdminOuProfessor();
        HorarioTurma horario = horarioTurmaRepository.findByIdAndAcademiaId(id, permissaoService.academiaAtualId())
                .orElseThrow(() -> new RuntimeException("Horário de turma não encontrado com ID: " + id));
        horario.setAtivo(ativo);
        return horarioTurmaRepository.save(horario);
    }

    @Transactional
    public AgendaPersonal alterarStatusAgendaPersonal(Long id, boolean ativo) {
        permissaoService.exigirAdminOuProfessor();
        AgendaPersonal agenda = agendaPersonalRepository.findByIdAndAcademiaId(id, permissaoService.academiaAtualId())
                .orElseThrow(() -> new RuntimeException("Agenda personal não encontrada com ID: " + id));
        agenda.setAtivo(ativo);
        return agendaPersonalRepository.save(agenda);
    }

    private Professor buscarProfessor(Long professorId, Long academiaId) {
        return professorRepository.findByIdAndAcademiaId(professorId, academiaId)
                .orElseThrow(() -> new RuntimeException("Professor não encontrado com ID: " + professorId));
    }

    private void validarConflito(Long professorId, DayOfWeek diaSemana, LocalTime inicio, LocalTime fim,
                                 Long horarioTurmaIgnoradoId, Long agendaPersonalIgnoradaId) {
        Long academiaId = permissaoService.academiaAtualId();

        boolean conflitoTurma = horarioTurmaRepository
                .findByAcademiaIdAndProfessorIdAndDiaSemanaAndAtivoTrue(academiaId, professorId, diaSemana)
                .stream()
                .filter(horario -> horarioTurmaIgnoradoId == null || !horarioTurmaIgnoradoId.equals(horario.getId()))
                .anyMatch(horario -> sobrepoe(inicio, fim, horario.getHoraInicial(), horario.getHoraFinal()));

        if (conflitoTurma) {
            throw new RuntimeException("Professor já possui turma nesse horário");
        }

        boolean conflitoPersonal = agendaPersonalRepository
                .findByAcademiaIdAndProfessorIdAndDiaSemanaAndAtivoTrue(academiaId, professorId, diaSemana)
                .stream()
                .filter(agenda -> agendaPersonalIgnoradaId == null || !agendaPersonalIgnoradaId.equals(agenda.getId()))
                .anyMatch(agenda -> sobrepoe(inicio, fim, agenda.getHoraInicial(), agenda.getHoraFinal()));

        if (conflitoPersonal) {
            throw new RuntimeException("Professor já possui personal nesse horário");
        }
    }

    private boolean sobrepoe(LocalTime inicioNovo, LocalTime fimNovo, LocalTime inicioExistente, LocalTime fimExistente) {
        return inicioExistente.isBefore(fimNovo) && fimExistente.isAfter(inicioNovo);
    }

    private void validarIntervalo(LocalTime inicio, LocalTime fim) {
        if (inicio == null || fim == null) {
            return;
        }
        if (!inicio.isBefore(fim)) {
            throw new RuntimeException("Hora inicial deve ser menor que hora final");
        }
    }
}
