package Tic.Implementacao.repository;

import Tic.Implementacao.model.MedidasCorporais;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MedidasCorporaisRepository extends JpaRepository<MedidasCorporais, Long> {
    Optional<MedidasCorporais> findByAvaliacaoId(Long avaliacaoId);
    List<MedidasCorporais> findByAvaliacaoIdIn(List<Long> avaliacaoIds);
}
