package Tic.Implementacao.service;

import Tic.Implementacao.model.Aluno;
import Tic.Implementacao.model.CheckIn;
import Tic.Implementacao.model.Graduacao;
import Tic.Implementacao.model.OrigemCheckIn;
import Tic.Implementacao.model.Professor;
import Tic.Implementacao.model.Turma;
import Tic.Implementacao.dto.checkin.RegistrarCheckInRequest;
import Tic.Implementacao.repository.AlunoRepository;
import Tic.Implementacao.repository.CheckInRepository;
import Tic.Implementacao.repository.GraduacaoRepository;
import Tic.Implementacao.repository.ProfessorRepository;
import Tic.Implementacao.repository.TurmaRepository;
import Tic.Implementacao.security.PermissaoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CheckInService {

    private final CheckInRepository repository;
    private final AlunoRepository alunoRepository;
    private final GraduacaoRepository graduacaoRepository;
    private final TurmaRepository turmaRepository;
    private final ProfessorRepository professorRepository;
    private final PermissaoService permissaoService;

    public CheckInService(CheckInRepository repository, AlunoRepository alunoRepository,
                          GraduacaoRepository graduacaoRepository, TurmaRepository turmaRepository,
                          ProfessorRepository professorRepository, PermissaoService permissaoService) {
        this.repository = repository;
        this.alunoRepository = alunoRepository;
        this.graduacaoRepository = graduacaoRepository;
        this.turmaRepository = turmaRepository;
        this.professorRepository = professorRepository;
        this.permissaoService = permissaoService;
    }

    public List<CheckIn> listarTodos() {
        return repository.findAll();
    }

    public List<CheckIn> listarPorAluno(Long alunoId) {
        return repository.findByAcademiaIdAndAlunoIdOrderByDataHoraDesc(permissaoService.academiaAtualId(), alunoId);
    }

    public Optional<CheckIn> buscarPorId(Long id) {
        return repository.findById(id);
    }

    @Transactional
    public CheckIn salvar(CheckIn checkIn) {
        if (checkIn.getDataHora() == null) {
            checkIn.setDataHora(LocalDateTime.now());
        }
        return repository.save(checkIn);
    }

    @Transactional
    public CheckIn registrarCheckIn(RegistrarCheckInRequest request) {
        permissaoService.exigirAdminOuProfessor();

        Long academiaId = permissaoService.academiaAtualId();
        Aluno aluno = alunoRepository.findByIdAndAcademiaId(request.getAlunoId(), academiaId)
                .orElseThrow(() -> new RuntimeException("Aluno não encontrado com ID: " + request.getAlunoId()));

        Turma turma = request.getTurmaId() == null ? null : turmaRepository.findByIdAndAcademiaId(request.getTurmaId(), academiaId)
                .orElseThrow(() -> new RuntimeException("Turma não encontrada com ID: " + request.getTurmaId()));

        Professor professor = request.getProfessorId() == null ? null : professorRepository.findByIdAndAcademiaId(request.getProfessorId(), academiaId)
                .orElseThrow(() -> new RuntimeException("Professor não encontrado com ID: " + request.getProfessorId()));

        LocalDate hoje = LocalDate.now();
        if (turma != null && repository.existsByAcademiaIdAndAlunoIdAndTurmaIdAndDataHoraBetween(
                academiaId,
                aluno.getId(),
                turma.getId(),
                hoje.atStartOfDay(),
                hoje.plusDays(1).atStartOfDay())) {
            throw new RuntimeException("Check-in duplicado para aluno, turma e data");
        }

        CheckIn checkIn = new CheckIn(
                LocalDateTime.now(),
                aluno,
                turma,
                professor,
                request.getOrigem() == null ? OrigemCheckIn.PROFESSOR : request.getOrigem()
        );
        checkIn.setAcademia(aluno.getAcademia());

        aluno.setQuantidadeCheckins(aluno.getQuantidadeCheckins() + 1);
        alunoRepository.save(aluno);

        return repository.save(checkIn);
    }

    @Transactional
    public CheckIn atualizar(Long id, CheckIn dadosNovos) {
        CheckIn checkIn = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Check-in não encontrado com ID: " + id));

        checkIn.setDataHora(dadosNovos.getDataHora());
        checkIn.setAluno(dadosNovos.getAluno());
        checkIn.setTurma(dadosNovos.getTurma());
        checkIn.setProfessor(dadosNovos.getProfessor());
        checkIn.setOrigem(dadosNovos.getOrigem());

        return repository.save(checkIn);
    }

    @Transactional
    public boolean remover(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }

    public Map<String, Object> montarDashboardAluno(Long alunoId) {
        Aluno aluno = alunoRepository.findById(alunoId)
                .orElseThrow(() -> new RuntimeException("Aluno não encontrado com ID: " + alunoId));

        long totalCheckins = aluno.getQuantidadeCheckins();
        Graduacao graduacaoAtual = aluno.getGraduacaoEntidade();
        Graduacao proximaGraduacao = null;

        if (graduacaoAtual != null) {
            proximaGraduacao = graduacaoRepository
                    .findFirstByOrdemGreaterThanOrderByOrdemAsc(graduacaoAtual.getOrdem())
                    .orElse(null);
        }

        int checkinsNecessarios = proximaGraduacao != null ? proximaGraduacao.getCheckinsNecessarios() : 0;
        long checkinsRestantes = Math.max(checkinsNecessarios - totalCheckins, 0);

        Map<String, Object> dashboard = new LinkedHashMap<>();
        dashboard.put("aluno", aluno);
        dashboard.put("totalCheckins", totalCheckins);
        dashboard.put("graduacaoAtual", graduacaoAtual);
        dashboard.put("proximaGraduacao", proximaGraduacao);
        dashboard.put("checkinsNecessarios", checkinsNecessarios);
        dashboard.put("checkinsRestantes", checkinsRestantes);
        dashboard.put("aptoParaGraduar", proximaGraduacao != null && checkinsRestantes == 0);

        return dashboard;
    }
}
