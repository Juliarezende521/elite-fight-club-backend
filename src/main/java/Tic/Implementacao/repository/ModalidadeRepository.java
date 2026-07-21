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

    List<Modalidade> findByAcademiaId(Long academiaId);

    List<Modalidade> findByAcademiaIdAndAtivaTrue(Long academiaId);

    List<Modalidade> findByTipoModalidade(TipoModalidade tipoModalidade);

    List<Modalidade> findByAcademiaIdAndTipoModalidade(Long academiaId, TipoModalidade tipoModalidade);

    List<Modalidade> findByIdInAndAcademiaId(List<Long> ids, Long academiaId);

    Optional<Modalidade> findByIdAndAcademiaId(Long id, Long academiaId);

    Optional<Modalidade> findByNomeIgnoreCase(String nome);
}
