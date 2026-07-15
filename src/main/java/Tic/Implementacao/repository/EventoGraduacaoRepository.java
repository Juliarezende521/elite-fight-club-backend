package Tic.Implementacao.repository;

import Tic.Implementacao.model.EventoGraduacao;
import Tic.Implementacao.model.StatusEventoGraduacao;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EventoGraduacaoRepository extends JpaRepository<EventoGraduacao, Long> {

    List<EventoGraduacao> findByAcademiaIdOrderByDataEventoDesc(Long academiaId);

    List<EventoGraduacao> findByAcademiaIdAndStatusOrderByDataEventoAsc(Long academiaId, StatusEventoGraduacao status);

    Optional<EventoGraduacao> findByIdAndAcademiaId(Long id, Long academiaId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from EventoGraduacao e where e.id = :id and e.academia.id = :academiaId")
    Optional<EventoGraduacao> findByIdAndAcademiaIdForUpdate(@Param("id") Long id, @Param("academiaId") Long academiaId);
}
