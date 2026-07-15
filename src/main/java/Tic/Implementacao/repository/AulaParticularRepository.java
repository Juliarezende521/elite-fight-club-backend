package Tic.Implementacao.repository;

import Tic.Implementacao.model.AulaParticular;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AulaParticularRepository extends JpaRepository<AulaParticular, Long> {
}