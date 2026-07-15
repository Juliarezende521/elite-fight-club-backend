package Tic.Implementacao.repository;

import Tic.Implementacao.model.HistoricoGraduacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HistoricoGraduacaoRepository extends JpaRepository<HistoricoGraduacao, Long> {

    List<HistoricoGraduacao> findByAcademiaIdAndAlunoIdOrderByAlteradoEmDesc(Long academiaId, Long alunoId);

    List<HistoricoGraduacao> findByAcademiaIdAndAlunoIdOrderByAlteradoEmAsc(Long academiaId, Long alunoId);

    Optional<HistoricoGraduacao> findFirstByAcademiaIdAndAlunoIdAndNovaGraduacaoIdOrderByAlteradoEmDesc(
            Long academiaId, Long alunoId, Long novaGraduacaoId);

    boolean existsByAcademiaIdAndAlunoId(Long academiaId, Long alunoId);
}
