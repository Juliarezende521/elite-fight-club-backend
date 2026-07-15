package Tic.Implementacao.repository;

import Tic.Implementacao.model.Modalidade;
import Tic.Implementacao.model.TipoModalidade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModalidadeRepository extends JpaRepository<Modalidade, Long> {

    List<Modalidade> findByAtivaTrue();

    List<Modalidade> findByTipoModalidade(TipoModalidade tipoModalidade);

    Optional<Modalidade> findByNomeIgnoreCase(String nome);
}
