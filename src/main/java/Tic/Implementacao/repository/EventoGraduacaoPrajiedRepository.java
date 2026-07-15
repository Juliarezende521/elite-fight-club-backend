package Tic.Implementacao.repository;

import Tic.Implementacao.model.EventoGraduacaoPrajied;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EventoGraduacaoPrajiedRepository extends JpaRepository<EventoGraduacaoPrajied, Long> {

    List<EventoGraduacaoPrajied> findByEventoIdAndAtivoTrueOrderByGraduacaoDestinoOrdemAsc(Long eventoId);

    @Query("""
            select p from EventoGraduacaoPrajied p
            join fetch p.graduacaoDestino
            where p.evento.id in :eventoIds and p.ativo = true
            order by p.evento.id asc, p.graduacaoDestino.ordem asc
            """)
    List<EventoGraduacaoPrajied> findAtivosByEventoIds(@Param("eventoIds") List<Long> eventoIds);

    Optional<EventoGraduacaoPrajied> findByIdAndEventoIdAndEventoAcademiaId(Long id, Long eventoId, Long academiaId);

    Optional<EventoGraduacaoPrajied> findByEventoIdAndGraduacaoDestinoIdAndAtivoTrue(Long eventoId, Long graduacaoDestinoId);

    boolean existsByEventoIdAndGraduacaoDestinoId(Long eventoId, Long graduacaoDestinoId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from EventoGraduacaoPrajied p where p.evento.id = :eventoId and p.graduacaoDestino.id = :graduacaoId and p.ativo = true")
    Optional<EventoGraduacaoPrajied> findDisponivelForUpdate(@Param("eventoId") Long eventoId, @Param("graduacaoId") Long graduacaoId);
}
