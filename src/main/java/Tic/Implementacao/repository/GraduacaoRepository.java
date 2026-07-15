package Tic.Implementacao.repository;

import Tic.Implementacao.model.Graduacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Collection;

public interface GraduacaoRepository extends JpaRepository<Graduacao, Long> {

    List<Graduacao> findByAtivaTrueOrderByOrdemAsc();

    List<Graduacao> findByAcademiaIdOrderByOrdemAsc(Long academiaId);

    List<Graduacao> findByAcademiaIdAndAtivaTrueOrderByOrdemAsc(Long academiaId);

    Optional<Graduacao> findByGraduacaoBaseTrue();

    Optional<Graduacao> findByAcademiaIdAndGraduacaoBaseTrue(Long academiaId);

    Optional<Graduacao> findFirstByOrdemGreaterThanOrderByOrdemAsc(Integer ordem);

    Optional<Graduacao> findFirstByAcademiaIdAndOrdemGreaterThanOrderByOrdemAsc(Long academiaId, Integer ordem);

    Optional<Graduacao> findFirstByAcademiaIdAndAtivaTrueAndOrdemGreaterThanOrderByOrdemAsc(Long academiaId, Integer ordem);

    boolean existsByGraduacaoBaseTrue();

    boolean existsByAcademiaIdAndGraduacaoBaseTrue(Long academiaId);

    boolean existsByAcademiaIdAndGraduacaoBaseTrueAndAtivaTrue(Long academiaId);

    boolean existsByAcademiaIdAndGraduacaoBaseTrueAndAtivaTrueAndIdNot(Long academiaId, Long id);

    boolean existsByAcademiaIdAndNomeIgnoreCase(Long academiaId, String nome);

    boolean existsByAcademiaIdAndNomeIgnoreCaseAndIdNot(Long academiaId, String nome, Long id);

    boolean existsByAcademiaIdAndOrdem(Long academiaId, Integer ordem);

    boolean existsByAcademiaIdAndOrdemAndIdNot(Long academiaId, Integer ordem, Long id);

    Optional<Graduacao> findByIdAndAcademiaId(Long id, Long academiaId);

    List<Graduacao> findByIdInAndAcademiaId(Collection<Long> ids, Long academiaId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Graduacao g set g.ordem = :ordem where g.id = :id and g.academia.id = :academiaId")
    int atualizarOrdem(@Param("id") Long id, @Param("academiaId") Long academiaId, @Param("ordem") Integer ordem);
}
