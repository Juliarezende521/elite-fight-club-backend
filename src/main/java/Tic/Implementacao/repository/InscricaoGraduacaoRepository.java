package Tic.Implementacao.repository;

import Tic.Implementacao.model.InscricaoGraduacao;
import Tic.Implementacao.model.StatusInscricaoGraduacao;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface InscricaoGraduacaoRepository extends JpaRepository<InscricaoGraduacao, Long> {

    List<InscricaoGraduacao> findByAcademiaIdAndEventoIdOrderByInscritoEmDesc(Long academiaId, Long eventoId);

    List<InscricaoGraduacao> findByAcademiaIdAndAlunoIdOrderByInscritoEmDesc(Long academiaId, Long alunoId);

    Optional<InscricaoGraduacao> findByIdAndAcademiaIdAndAlunoId(Long id, Long academiaId, Long alunoId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from InscricaoGraduacao i where i.id = :id and i.academia.id = :academiaId and i.aluno.id = :alunoId")
    Optional<InscricaoGraduacao> findByIdAndAcademiaIdAndAlunoIdForUpdate(@Param("id") Long id, @Param("academiaId") Long academiaId, @Param("alunoId") Long alunoId);

    Optional<InscricaoGraduacao> findFirstByAcademiaIdAndEventoIdAndAlunoIdAndStatusInOrderByInscritoEmDesc(
            Long academiaId, Long eventoId, Long alunoId, Collection<StatusInscricaoGraduacao> status);

    long countByAcademiaIdAndEventoIdAndStatusIn(Long academiaId, Long eventoId, Collection<StatusInscricaoGraduacao> status);

    long countByAcademiaIdAndEventoIdAndGraduacaoDestinoIdAndStatusIn(
            Long academiaId, Long eventoId, Long graduacaoDestinoId, Collection<StatusInscricaoGraduacao> status);
}
