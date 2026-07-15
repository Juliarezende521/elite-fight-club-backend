package Tic.Implementacao.repository;

import Tic.Implementacao.model.Fatura;
import Tic.Implementacao.model.StatusFatura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FaturaRepository extends JpaRepository<Fatura, Long> {

    Optional<Fatura> findByAcademiaIdAndAlunoIdAndMesReferencia(Long academiaId, Long alunoId, LocalDate mesReferencia);

    List<Fatura> findByAcademiaId(Long academiaId);

    List<Fatura> findByAcademiaIdAndAlunoId(Long academiaId, Long alunoId);

    Optional<Fatura> findByIdAndAcademiaId(Long id, Long academiaId);

    List<Fatura> findByStatus(StatusFatura status);

    List<Fatura> findByAcademiaIdAndStatusAndDataVencimentoBefore(Long academiaId, StatusFatura status, LocalDate data);

    List<Fatura> findByAcademiaIdAndAlunoIdAndStatusInOrderByDataVencimentoAsc(Long academiaId, Long alunoId, List<StatusFatura> status);

    Optional<Fatura> findFirstByAcademiaIdAndAlunoIdAndMesReferenciaOrderByDataVencimentoDesc(Long academiaId, Long alunoId, LocalDate mesReferencia);

    @Query("""
            select f from Fatura f
            join fetch f.aluno a
            left join fetch a.plano p
            where f.academia.id = :academiaId
              and f.mesReferencia = :mesReferencia
            """)
    List<Fatura> findMensaisComAlunoPlano(@Param("academiaId") Long academiaId,
                                          @Param("mesReferencia") LocalDate mesReferencia);

    @Query("""
            select f from Fatura f
            join fetch f.aluno a
            left join fetch a.plano p
            where f.academia.id = :academiaId
              and f.mesReferencia between :inicio and :fim
            """)
    List<Fatura> findMensaisDoPeriodoComAlunoPlano(@Param("academiaId") Long academiaId,
                                                   @Param("inicio") LocalDate inicio,
                                                   @Param("fim") LocalDate fim);

    @Query("""
            select f from Fatura f
            join fetch f.aluno a
            left join fetch a.plano p
            where f.academia.id = :academiaId
              and f.dataVencimento < :dataReferencia
              and f.status <> Tic.Implementacao.model.StatusFatura.CANCELADA
            """)
    List<Fatura> findPossiveisVencidasAntesDe(@Param("academiaId") Long academiaId,
                                              @Param("dataReferencia") LocalDate dataReferencia);
}
