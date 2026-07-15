package Tic.Implementacao.repository;

import Tic.Implementacao.model.ReservaAula;
import Tic.Implementacao.model.StatusReserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReservaAulaRepository extends JpaRepository<ReservaAula, Long> {

    interface OcupacaoAulaProjection {
        Long getAulaId();
        Long getConfirmados();
    }

    long countByAcademiaIdAndAulaTurmaIdAndStatus(Long academiaId, Long aulaTurmaId, StatusReserva status);

    Optional<ReservaAula> findByAcademiaIdAndAulaTurmaIdAndAlunoIdAndStatus(
            Long academiaId, Long aulaTurmaId, Long alunoId, StatusReserva status);

    List<ReservaAula> findAllByAcademiaIdAndAulaTurmaIdOrderByReservadoEmAsc(Long academiaId, Long aulaTurmaId);

    @Query("""
            select r.aulaTurma.id as aulaId, count(r) as confirmados
            from ReservaAula r
            where r.academia.id = :academiaId
              and r.status = :status
              and r.aulaTurma.id in :aulaIds
            group by r.aulaTurma.id
            """)
    List<OcupacaoAulaProjection> contarConfirmadasPorAulas(@Param("academiaId") Long academiaId,
                                                           @Param("aulaIds") Collection<Long> aulaIds,
                                                           @Param("status") StatusReserva status);

    @Query("""
            select r
            from ReservaAula r
            join fetch r.aluno a
            where r.academia.id = :academiaId
              and r.aulaTurma.id = :aulaId
              and r.status = :status
            order by r.reservadoEm asc
            """)
    List<ReservaAula> buscarConfirmadasComAluno(@Param("academiaId") Long academiaId,
                                                @Param("aulaId") Long aulaId,
                                                @Param("status") StatusReserva status);

    boolean existsByAcademiaIdAndAulaTurmaIdAndAlunoIdAndStatus(
            Long academiaId, Long aulaTurmaId, Long alunoId, StatusReserva status);

    @Query("""
            select count(r)
            from ReservaAula r
            where r.academia.id = :academiaId
              and r.aluno.id = :alunoId
              and r.status = :status
              and r.aulaTurma.data between :inicio and :fim
            """)
    long countReservasConfirmadasNaSemana(@Param("academiaId") Long academiaId,
                                          @Param("alunoId") Long alunoId,
                                          @Param("status") StatusReserva status,
                                          @Param("inicio") LocalDate inicio,
                                          @Param("fim") LocalDate fim);

    @Query("""
            select r
            from ReservaAula r
            where r.academia.id = :academiaId
              and r.aluno.id = :alunoId
              and r.aulaTurma.data between :inicio and :fim
            order by r.aulaTurma.data asc, r.aulaTurma.horaInicio asc
            """)
    List<ReservaAula> listarReservasAlunoNoPeriodo(@Param("academiaId") Long academiaId,
                                                   @Param("alunoId") Long alunoId,
                                                   @Param("inicio") LocalDate inicio,
                                                   @Param("fim") LocalDate fim);

    @Query("""
            select r
            from ReservaAula r
            join fetch r.aulaTurma a
            join fetch a.professor p
            join fetch a.modalidade m
            where r.academia.id = :academiaId
              and r.aluno.id = :alunoId
              and r.status = :status
              and a.data between :inicio and :fim
            order by a.data asc, a.horaInicio asc
            """)
    List<ReservaAula> buscarReservasConfirmadasComAulaNoPeriodo(@Param("academiaId") Long academiaId,
                                                                @Param("alunoId") Long alunoId,
                                                                @Param("status") StatusReserva status,
                                                                @Param("inicio") LocalDate inicio,
                                                                @Param("fim") LocalDate fim);
}
