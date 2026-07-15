package Tic.Implementacao.repository;

import Tic.Implementacao.model.Fatura;
import Tic.Implementacao.model.StatusFatura;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
