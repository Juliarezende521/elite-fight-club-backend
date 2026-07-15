package Tic.Implementacao.repository;

import Tic.Implementacao.model.AvaliacaoGraduacao;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AvaliacaoGraduacaoRepository extends JpaRepository<AvaliacaoGraduacao, Long> {

    Optional<AvaliacaoGraduacao> findByAcademiaIdAndInscricaoId(Long academiaId, Long inscricaoId);

    Optional<AvaliacaoGraduacao> findByAcademiaIdAndIdAndAlunoId(Long academiaId, Long id, Long alunoId);

    List<AvaliacaoGraduacao> findByAcademiaIdAndAlunoIdOrderByEventoDataEventoDesc(Long academiaId, Long alunoId);

    List<AvaliacaoGraduacao> findByAcademiaIdAndEventoId(Long academiaId, Long eventoId);

    List<AvaliacaoGraduacao> findByAcademiaIdAndEventoIdIn(Long academiaId, java.util.Collection<Long> eventoIds);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from AvaliacaoGraduacao a where a.academia.id = :academiaId and a.inscricao.id = :inscricaoId")
    Optional<AvaliacaoGraduacao> findByAcademiaIdAndInscricaoIdForUpdate(@Param("academiaId") Long academiaId,
                                                                          @Param("inscricaoId") Long inscricaoId);
}
