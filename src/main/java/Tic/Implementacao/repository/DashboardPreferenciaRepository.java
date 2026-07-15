package Tic.Implementacao.repository;

import Tic.Implementacao.model.DashboardPreferencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DashboardPreferenciaRepository extends JpaRepository<DashboardPreferencia, Long> {

    Optional<DashboardPreferencia> findByUsuarioId(Long usuarioId);

    boolean existsByUsuarioId(Long usuarioId);
}
