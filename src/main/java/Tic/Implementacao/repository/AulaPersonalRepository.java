package Tic.Implementacao.repository;

import Tic.Implementacao.model.AulaPersonal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AulaPersonalRepository extends JpaRepository<AulaPersonal, Long> {
    Optional<AulaPersonal> findByAcademiaIdAndAgendaPersonalIdAndData(Long academiaId, Long agendaPersonalId, LocalDate data);
    Optional<AulaPersonal> findByIdAndAcademiaId(Long id, Long academiaId);
    @EntityGraph(attributePaths = {"academia", "agendaPersonal", "aluno", "professor", "modalidade", "aulaOriginal"})
    @Query("select a from AulaPersonal a where a.id = :id and a.academia.id = :academiaId")
    Optional<AulaPersonal> buscarDetalhePorId(@Param("id") Long id, @Param("academiaId") Long academiaId);
    @EntityGraph(attributePaths = {"academia", "agendaPersonal", "aluno", "professor", "modalidade", "aulaOriginal"})
    List<AulaPersonal> findByAcademiaIdAndProfessorIdAndData(Long academiaId, Long professorId, LocalDate data);
    @EntityGraph(attributePaths = {"academia", "agendaPersonal", "aluno", "professor", "modalidade", "aulaOriginal"})
    List<AulaPersonal> findByAcademiaIdAndProfessorIdAndDataBetween(Long academiaId, Long professorId, LocalDate inicio, LocalDate fim);

    @EntityGraph(attributePaths = {"academia", "agendaPersonal", "aluno", "professor", "modalidade", "aulaOriginal"})
    List<AulaPersonal> findByAcademiaIdAndAlunoIdAndDataBetween(Long academiaId, Long alunoId, LocalDate inicio, LocalDate fim);
}
