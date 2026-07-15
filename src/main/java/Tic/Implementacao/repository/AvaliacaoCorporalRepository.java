package Tic.Implementacao.repository;

import Tic.Implementacao.model.AvaliacaoCorporal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AvaliacaoCorporalRepository extends JpaRepository<AvaliacaoCorporal, Long> {

    List<AvaliacaoCorporal> findByAcademiaIdAndAlunoIdAndAtivaTrueOrderByDataAvaliacaoDescCriadaEmDesc(Long academiaId, Long alunoId);

    Optional<AvaliacaoCorporal> findFirstByAcademiaIdAndAlunoIdAndAtivaTrueOrderByDataAvaliacaoDescCriadaEmDesc(Long academiaId, Long alunoId);

    Optional<AvaliacaoCorporal> findByIdAndAcademiaIdAndAlunoIdAndAtivaTrue(Long id, Long academiaId, Long alunoId);

    boolean existsByAcademiaIdAndAlunoIdAndDataAvaliacaoAndAtivaTrue(Long academiaId, Long alunoId, LocalDate dataAvaliacao);

    boolean existsByAcademiaIdAndAlunoIdAndDataAvaliacaoAndAtivaTrueAndIdNot(Long academiaId, Long alunoId, LocalDate dataAvaliacao, Long id);

    List<AvaliacaoCorporal> findByAcademiaIdAndAlunoIdAndAtivaTrueAndDataAvaliacaoBetweenOrderByDataAvaliacaoDescCriadaEmDesc(
            Long academiaId, Long alunoId, LocalDate inicio, LocalDate fim);
}
