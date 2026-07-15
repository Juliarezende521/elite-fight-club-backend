package Tic.Implementacao.repository;

import Tic.Implementacao.model.ConviteGraduacao;
import Tic.Implementacao.model.StatusConviteGraduacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ConviteGraduacaoRepository extends JpaRepository<ConviteGraduacao, Long> {

    List<ConviteGraduacao> findByAcademiaIdAndEventoIdOrderByEnviadoEmDesc(Long academiaId, Long eventoId);

    List<ConviteGraduacao> findByAcademiaIdAndEventoIdIn(Long academiaId, Collection<Long> eventoIds);

    List<ConviteGraduacao> findByAcademiaIdAndAlunoIdOrderByEnviadoEmDesc(Long academiaId, Long alunoId);

    Optional<ConviteGraduacao> findByIdAndAcademiaIdAndAlunoId(Long id, Long academiaId, Long alunoId);

    Optional<ConviteGraduacao> findByAcademiaIdAndEventoIdAndAlunoIdAndGraduacaoDestinoId(
            Long academiaId, Long eventoId, Long alunoId, Long graduacaoDestinoId);

    Optional<ConviteGraduacao> findFirstByAcademiaIdAndEventoIdAndAlunoIdAndStatusInOrderByEnviadoEmDesc(
            Long academiaId, Long eventoId, Long alunoId, Collection<StatusConviteGraduacao> status);

    boolean existsByAcademiaIdAndEventoIdAndAlunoIdAndGraduacaoDestinoId(
            Long academiaId, Long eventoId, Long alunoId, Long graduacaoDestinoId);

    List<ConviteGraduacao> findByAcademiaIdAndEventoIdAndStatusIn(
            Long academiaId, Long eventoId, Collection<StatusConviteGraduacao> status);
}
