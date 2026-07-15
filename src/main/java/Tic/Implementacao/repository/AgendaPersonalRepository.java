package Tic.Implementacao.repository;

import Tic.Implementacao.model.AgendaPersonal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AgendaPersonalRepository extends JpaRepository<AgendaPersonal, Long> {
    Optional<AgendaPersonal> findByIdAndAcademiaId(Long id, Long academiaId);
    List<AgendaPersonal> findByAcademiaIdAndProfessorIdAndDiaSemanaAndAtivoTrue(Long academiaId, Long professorId, DayOfWeek diaSemana);
    List<AgendaPersonal> findByAcademiaIdAndProfessorIdAndDiaSemanaAndAtivoTrueAndDataInicioLessThanEqual(Long academiaId, Long professorId, DayOfWeek diaSemana, LocalDate data);
    List<AgendaPersonal> findByAcademiaIdAndDiaSemanaAndAtivoTrueAndDataInicioLessThanEqual(Long academiaId, DayOfWeek diaSemana, LocalDate data);
    List<AgendaPersonal> findByAcademiaIdAndAlunoIdAndAtivoTrue(Long academiaId, Long alunoId);
}
