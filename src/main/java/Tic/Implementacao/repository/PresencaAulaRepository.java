package Tic.Implementacao.repository;

import Tic.Implementacao.model.PresencaAula;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PresencaAulaRepository extends JpaRepository<PresencaAula, Long> {

    Optional<PresencaAula> findByAcademiaIdAndChavePresenca(Long academiaId, String chavePresenca);

    @EntityGraph(attributePaths = {"aluno", "registradoPor"})
    List<PresencaAula> findAllByAcademiaIdAndAulaTurmaId(Long academiaId, Long aulaTurmaId);

    @EntityGraph(attributePaths = {"aluno", "registradoPor"})
    Optional<PresencaAula> findByAcademiaIdAndAulaPersonalId(Long academiaId, Long aulaPersonalId);

    List<PresencaAula> findByAcademiaIdAndAlunoIdAndChavePresencaIn(Long academiaId, Long alunoId, List<String> chaves);
}
