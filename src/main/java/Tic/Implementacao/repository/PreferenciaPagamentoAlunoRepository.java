package Tic.Implementacao.repository;

import Tic.Implementacao.model.PreferenciaPagamentoAluno;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PreferenciaPagamentoAlunoRepository extends JpaRepository<PreferenciaPagamentoAluno, Long> {
    Optional<PreferenciaPagamentoAluno> findByAcademiaIdAndAlunoIdAndAtivaTrue(Long academiaId, Long alunoId);
    Optional<PreferenciaPagamentoAluno> findByAcademiaIdAndAlunoId(Long academiaId, Long alunoId);
    List<PreferenciaPagamentoAluno> findByAcademiaIdAndAlunoIdInAndAtivaTrue(Long academiaId, List<Long> alunoIds);
}
