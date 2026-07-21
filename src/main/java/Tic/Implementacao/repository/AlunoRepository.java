package Tic.Implementacao.repository;


import Tic.Implementacao.model.Aluno;
import Tic.Implementacao.model.TipoModalidade;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AlunoRepository extends JpaRepository<Aluno, Long> {

    List<Aluno> findDistinctByModalidadesIdAndAcademiaId(Long modalidadeId, Long academiaId);

    List<Aluno> findDistinctByModalidadesTipoModalidadeAndAcademiaId(TipoModalidade tipoModalidade, Long academiaId);

    boolean existsByEmail(String email);

    boolean existsByFirebaseUid(String firebaseUid);

    List<Aluno> findByAtivoTrue();

    List<Aluno> findByAtivoTrueAndAcademiaId(Long academiaId);

    List<Aluno> findAllByAcademiaId(Long academiaId);

    Optional<Aluno> findByIdAndAcademiaId(Long id, Long academiaId);

    Optional<Aluno> findByEmailAndAcademiaId(String email, Long academiaId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Aluno a where a.id = :id and a.academia.id = :academiaId")
    Optional<Aluno> findByIdAndAcademiaIdForUpdate(@Param("id") Long id, @Param("academiaId") Long academiaId);

    boolean existsByGraduacaoId(Long graduacaoId);

    boolean existsByAcademiaIdAndGraduacaoId(Long academiaId, Long graduacaoId);

    boolean existsByEmailAndAcademiaId(String email, Long academiaId);
}
