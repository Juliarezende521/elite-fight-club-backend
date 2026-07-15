package Tic.Implementacao.repository;

import Tic.Implementacao.model.HorarioTurma;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

public interface HorarioTurmaRepository extends JpaRepository<HorarioTurma, Long> {
    Optional<HorarioTurma> findByIdAndAcademiaId(Long id, Long academiaId);
    List<HorarioTurma> findByAcademiaIdAndProfessorIdAndDiaSemanaAndAtivoTrue(Long academiaId, Long professorId, DayOfWeek diaSemana);
    List<HorarioTurma> findByAcademiaIdAndDiaSemanaAndAtivoTrue(Long academiaId, DayOfWeek diaSemana);
}
