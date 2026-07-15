package Tic.Implementacao.repository;

import Tic.Implementacao.model.Campeonato;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampeonatoRepository extends JpaRepository<Campeonato, Long> {
}
