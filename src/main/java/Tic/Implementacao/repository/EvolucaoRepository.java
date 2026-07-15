package Tic.Implementacao.repository;

import Tic.Implementacao.model.Evolucao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvolucaoRepository extends JpaRepository<Evolucao, Long> {

    List<Evolucao> findByAlunoId(Long alunoId);

    List<Evolucao> findByAlunoIdOrderByDataAvaliacaoAsc(Long alunoId);
}
