package Tic.Implementacao.repository;

import Tic.Implementacao.model.CertificadoGraduacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CertificadoGraduacaoRepository extends JpaRepository<CertificadoGraduacao, Long> {

    Optional<CertificadoGraduacao> findByIdAndAcademiaId(Long id, Long academiaId);

    Optional<CertificadoGraduacao> findByCodigoValidacao(String codigoValidacao);

    Optional<CertificadoGraduacao> findByAvaliacaoId(Long avaliacaoId);

    Optional<CertificadoGraduacao> findFirstByNumeroCertificadoStartingWithOrderByNumeroCertificadoDesc(String prefixo);

    boolean existsByNumeroCertificado(String numeroCertificado);

    boolean existsByCodigoValidacao(String codigoValidacao);

    boolean existsByAvaliacaoId(Long avaliacaoId);

    List<CertificadoGraduacao> findByAcademiaIdAndAlunoIdOrderByDataEmissaoDesc(Long academiaId, Long alunoId);

    List<CertificadoGraduacao> findByAcademiaIdOrderByDataEmissaoDesc(Long academiaId);

    List<CertificadoGraduacao> findTop5ByAcademiaIdAndAtivoTrueOrderByDataEmissaoDesc(Long academiaId);

    long countByAcademiaId(Long academiaId);

    long countByAcademiaIdAndAtivoFalse(Long academiaId);
}
