package Tic.Implementacao.repository;

import Tic.Implementacao.model.CheckIn;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CheckInRepository extends JpaRepository<CheckIn, Long> {

    List<CheckIn> findByAlunoIdOrderByDataHoraDesc(Long alunoId);

    List<CheckIn> findByAcademiaIdAndAlunoIdOrderByDataHoraDesc(Long academiaId, Long alunoId);

    long countByAlunoId(Long alunoId);

    boolean existsByAcademiaIdAndAlunoIdAndTurmaIdAndDataHoraBetween(Long academiaId, Long alunoId, Long turmaId, LocalDateTime inicio, LocalDateTime fim);

    Optional<CheckIn> findByAcademiaIdAndChaveCheckInAtivoAndAtivoTrue(Long academiaId, String chaveCheckInAtivo);

    List<CheckIn> findByAcademiaIdAndAlunoIdAndAtivoTrueAndDataHoraBetweenOrderByDataHoraDesc(
            Long academiaId, Long alunoId, LocalDateTime inicio, LocalDateTime fim);

    long countByAcademiaIdAndAlunoIdAndAtivoTrue(Long academiaId, Long alunoId);

    long countByAcademiaIdAndAlunoIdAndAtivoTrueAndDataHoraBetween(Long academiaId, Long alunoId, LocalDateTime inicio, LocalDateTime fim);

    Optional<CheckIn> findFirstByAcademiaIdAndAlunoIdAndAtivoTrueOrderByDataHoraDesc(Long academiaId, Long alunoId);

    List<CheckIn> findByAcademiaIdAndAlunoIdAndAtivoTrueAndChaveCheckInAtivoIn(Long academiaId, Long alunoId, List<String> chaves);
}
