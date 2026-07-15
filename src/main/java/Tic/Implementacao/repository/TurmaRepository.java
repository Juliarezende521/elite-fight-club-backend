package Tic.Implementacao.repository;

import Tic.Implementacao.model.Turma;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface TurmaRepository extends JpaRepository<Turma, Long> {
    Optional<Turma> findByIdAndAcademiaId(Long id, Long academiaId);
    List<Turma> findAllByAcademiaId(Long academiaId);
}
