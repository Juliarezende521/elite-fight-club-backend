package Tic.Implementacao.repository;

import Tic.Implementacao.model.AulaTurma;
import org.springframework.data.jpa.repository.EntityGraph;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AulaTurmaRepository extends JpaRepository<AulaTurma, Long> {
    Optional<AulaTurma> findByAcademiaIdAndHorarioTurmaIdAndData(Long academiaId, Long horarioTurmaId, LocalDate data);
    Optional<AulaTurma> findByIdAndAcademiaId(Long id, Long academiaId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from AulaTurma a where a.id = :id and a.academia.id = :academiaId")
    Optional<AulaTurma> findByIdAndAcademiaIdForUpdate(@Param("id") Long id, @Param("academiaId") Long academiaId);
    @EntityGraph(attributePaths = {"academia", "professor", "modalidade", "aulaOriginal"})
    @Query("select a from AulaTurma a where a.id = :id and a.academia.id = :academiaId")
    Optional<AulaTurma> buscarDetalhePorId(@Param("id") Long id, @Param("academiaId") Long academiaId);
    @EntityGraph(attributePaths = {"academia", "professor", "modalidade", "aulaOriginal"})
    List<AulaTurma> findByAcademiaIdAndProfessorIdAndData(Long academiaId, Long professorId, LocalDate data);
    @EntityGraph(attributePaths = {"academia", "professor", "modalidade", "aulaOriginal"})
    List<AulaTurma> findByAcademiaIdAndProfessorIdAndDataBetween(Long academiaId, Long professorId, LocalDate inicio, LocalDate fim);
}
