package Tic.Implementacao.repository;

import Tic.Implementacao.model.MetaPeso;
import Tic.Implementacao.model.StatusMetaPeso;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MetaPesoRepository extends JpaRepository<MetaPeso, Long> {

    Optional<MetaPeso> findFirstByAcademiaIdAndAlunoIdAndStatusOrderByCriadaEmDesc(Long academiaId, Long alunoId, StatusMetaPeso status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from MetaPeso m where m.academia.id = :academiaId and m.aluno.id = :alunoId and m.status = :status order by m.criadaEm desc")
    List<MetaPeso> findAtivasForUpdate(@Param("academiaId") Long academiaId,
                                       @Param("alunoId") Long alunoId,
                                       @Param("status") StatusMetaPeso status);

    List<MetaPeso> findByAcademiaIdAndAlunoIdOrderByCriadaEmDesc(Long academiaId, Long alunoId);

    Optional<MetaPeso> findByIdAndAcademiaIdAndAlunoId(Long id, Long academiaId, Long alunoId);

    boolean existsByAcademiaIdAndAlunoIdAndStatus(Long academiaId, Long alunoId, StatusMetaPeso status);

    List<MetaPeso> findByAcademiaIdAndStatus(Long academiaId, StatusMetaPeso status);
}
