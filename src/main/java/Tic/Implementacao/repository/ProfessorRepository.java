package Tic.Implementacao.repository;

import Tic.Implementacao.model.Professor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface ProfessorRepository extends JpaRepository<Professor, Long> {
    Optional<Professor> findByIdAndAcademiaId(Long id, Long academiaId);
    List<Professor> findAllByAcademiaId(Long academiaId);
}
