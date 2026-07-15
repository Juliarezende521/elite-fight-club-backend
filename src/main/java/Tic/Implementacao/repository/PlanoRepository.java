package Tic.Implementacao.repository;

import Tic.Implementacao.model.Plano;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlanoRepository extends JpaRepository<Plano, Long> {
    List<Plano> findAllByAcademiaId(Long academiaId);
    Optional<Plano> findByIdAndAcademiaId(Long id, Long academiaId);
}
