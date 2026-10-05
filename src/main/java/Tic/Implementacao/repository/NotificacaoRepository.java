package Tic.Implementacao.repository;

import Tic.Implementacao.model.Notificacao;
import Tic.Implementacao.model.StatusNotificacao;
import Tic.Implementacao.model.TipoNotificacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {

    List<Notificacao> findByAlunoId(Long alunoId);

    List<Notificacao> findByEnviadaFalse();

    Optional<Notificacao> findByChaveIdempotencia(String chaveIdempotencia);

    Optional<Notificacao> findByIdAndDestinatarioIdAndAcademiaId(Long id, Long destinatarioId, Long academiaId);

    List<Notificacao> findByDestinatarioIdAndAcademiaIdOrderByCriadaEmDesc(Long destinatarioId, Long academiaId);

    List<Notificacao> findByDestinatarioIdAndAcademiaIdAndLidaFalseOrderByCriadaEmDesc(Long destinatarioId, Long academiaId);

    List<Notificacao> findByDestinatarioIdAndAcademiaIdAndLidaFalse(Long destinatarioId, Long academiaId);

    @Query("""
            select n from Notificacao n
            where n.academia.id = :academiaId
              and (:inicio is null or n.criadaEm >= :inicio)
              and (:fim is null or n.criadaEm <= :fim)
              and (:tipo is null or n.tipoNotificacao = :tipo)
              and (:status is null or n.status = :status)
              and (:alunoId is null or n.destinatario.aluno.id = :alunoId)
              and (:eventoId is null or (n.referenciaTipo = 'EVENTO_GRADUACAO' and n.referenciaId = :eventoId))
            order by n.criadaEm desc
            """)
    List<Notificacao> buscarRelatorio(@Param("academiaId") Long academiaId,
                                      @Param("inicio") LocalDateTime inicio,
                                      @Param("fim") LocalDateTime fim,
                                      @Param("tipo") TipoNotificacao tipo,
                                      @Param("status") StatusNotificacao status,
                                      @Param("alunoId") Long alunoId,
                                      @Param("eventoId") Long eventoId);

    @Query("""
            select n from Notificacao n
            where n.academia.id = :academiaId
              and n.referenciaTipo = 'FATURA'
              and (:inicio is null or n.criadaEm >= :inicio)
              and (:fim is null or n.criadaEm <= :fim)
              and (:tipo is null or n.tipoNotificacao = :tipo)
              and (:status is null or n.status = :status)
              and (:alunoId is null or n.aluno.id = :alunoId or n.destinatario.aluno.id = :alunoId)
              and (:faturaId is null or n.referenciaId = :faturaId)
              and (:somenteFalhas = false or n.status = Tic.Implementacao.model.StatusNotificacao.FALHOU)
            order by n.criadaEm desc
            """)
    List<Notificacao> buscarRelatorioFinanceiro(@Param("academiaId") Long academiaId,
                                                @Param("inicio") LocalDateTime inicio,
                                                @Param("fim") LocalDateTime fim,
                                                @Param("tipo") TipoNotificacao tipo,
                                                @Param("status") StatusNotificacao status,
                                                @Param("alunoId") Long alunoId,
                                                @Param("faturaId") Long faturaId,
                                                @Param("somenteFalhas") boolean somenteFalhas);
}
