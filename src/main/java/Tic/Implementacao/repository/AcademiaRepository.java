package Tic.Implementacao.repository;

import Tic.Implementacao.model.Academia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AcademiaRepository extends JpaRepository<Academia, Long> {
    Optional<Academia> findByNomeIgnoreCase(String nome);
    Optional<Academia> findByCodigoCheckInPublico(String codigoCheckInPublico);
    boolean existsByCodigoCheckInPublico(String codigoCheckInPublico);
}
