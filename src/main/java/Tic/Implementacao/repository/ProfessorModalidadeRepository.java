package Tic.Implementacao.repository;

import Tic.Implementacao.model.ProfessorModalidade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProfessorModalidadeRepository extends JpaRepository<ProfessorModalidade, Long> {

    List<ProfessorModalidade> findByAcademiaIdAndProfessorId(Long academiaId, Long professorId);

    List<ProfessorModalidade> findByAcademiaIdAndProfessorIdAndAtivoTrue(Long academiaId, Long professorId);

    List<ProfessorModalidade> findByAcademiaIdAndModalidadeIdAndAtivoTrue(Long academiaId, Long modalidadeId);

    Optional<ProfessorModalidade> findByAcademiaIdAndProfessorIdAndModalidadeId(
            Long academiaId, Long professorId, Long modalidadeId);

    boolean existsByAcademiaIdAndProfessorIdAndModalidadeIdAndAtivoTrueAndPermiteTurmaTrue(
            Long academiaId, Long professorId, Long modalidadeId);

    boolean existsByAcademiaIdAndProfessorIdAndModalidadeIdAndAtivoTrueAndPermitePersonalTrue(
            Long academiaId, Long professorId, Long modalidadeId);
}
