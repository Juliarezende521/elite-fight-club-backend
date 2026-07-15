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

    @Query("""
            select e from EventoGraduacao e
            where e.academia.id = :academiaId
              and e.dataEvento between :inicio and :fim
              and (:eventoId is null or e.id = :eventoId)
              and (:status is null or e.status = :status)
            order by e.dataEvento asc
            """)
    List<EventoGraduacao> buscarParaRelatorio(@Param("academiaId") Long academiaId,
                                              @Param("inicio") java.time.LocalDate inicio,
                                              @Param("fim") java.time.LocalDate fim,
                                              @Param("eventoId") Long eventoId,
                                              @Param("status") StatusEventoGraduacao status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from EventoGraduacao e where e.id = :id and e.academia.id = :academiaId")
    Optional<EventoGraduacao> findByIdAndAcademiaIdForUpdate(@Param("id") Long id, @Param("academiaId") Long academiaId);
}
