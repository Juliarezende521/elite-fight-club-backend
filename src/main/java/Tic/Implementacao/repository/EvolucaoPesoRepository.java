package Tic.Implementacao.repository;

import Tic.Implementacao.model.EvolucaoPeso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EvolucaoPesoRepository extends JpaRepository<EvolucaoPeso, Long> {

    List<EvolucaoPeso> findByAcademiaIdAndAlunoIdAndAtivoTrueOrderByDataRegistroDescCriadoEmDesc(Long academiaId, Long alunoId);

    List<EvolucaoPeso> findByAcademiaIdAndAlunoIdAndAtivoTrueAndDataRegistroBetweenOrderByDataRegistroDescCriadoEmDesc(
            Long academiaId, Long alunoId, LocalDate inicio, LocalDate fim);

    Optional<EvolucaoPeso> findFirstByAcademiaIdAndAlunoIdAndAtivoTrueOrderByDataRegistroAscCriadoEmAsc(Long academiaId, Long alunoId);

    Optional<EvolucaoPeso> findFirstByAcademiaIdAndAlunoIdAndAtivoTrueOrderByDataRegistroDescCriadoEmDesc(Long academiaId, Long alunoId);

    Optional<EvolucaoPeso> findByIdAndAcademiaIdAndAlunoIdAndAtivoTrue(Long id, Long academiaId, Long alunoId);

    boolean existsByAcademiaIdAndAlunoIdAndDataRegistroAndAtivoTrue(Long academiaId, Long alunoId, LocalDate dataRegistro);

    boolean existsByAcademiaIdAndAlunoIdAndDataRegistroAndAtivoTrueAndIdNot(Long academiaId, Long alunoId, LocalDate dataRegistro, Long id);

    long countByAcademiaIdAndAlunoIdAndAtivoTrue(Long academiaId, Long alunoId);

    @Query("select min(e.peso) from EvolucaoPeso e where e.academia.id = :academiaId and e.aluno.id = :alunoId and e.ativo = true")
    BigDecimal menorPeso(@Param("academiaId") Long academiaId, @Param("alunoId") Long alunoId);

    @Query("select max(e.peso) from EvolucaoPeso e where e.academia.id = :academiaId and e.aluno.id = :alunoId and e.ativo = true")
    BigDecimal maiorPeso(@Param("academiaId") Long academiaId, @Param("alunoId") Long alunoId);
}
