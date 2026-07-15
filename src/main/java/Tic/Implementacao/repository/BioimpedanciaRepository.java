package Tic.Implementacao.repository;

import Tic.Implementacao.model.Bioimpedancia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BioimpedanciaRepository extends JpaRepository<Bioimpedancia, Long> {
    Optional<Bioimpedancia> findByAvaliacaoId(Long avaliacaoId);
    List<Bioimpedancia> findByAvaliacaoIdIn(List<Long> avaliacaoIds);
}
