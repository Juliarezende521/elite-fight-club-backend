package Tic.Implementacao.repository;

import Tic.Implementacao.model.Pagamento;
import Tic.Implementacao.model.MetodoPagamento;
import Tic.Implementacao.model.StatusPagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {

    List<Pagamento> findByFaturaId(Long faturaId);

    List<Pagamento> findByAcademiaIdAndFaturaId(Long academiaId, Long faturaId);

    Optional<Pagamento> findByIdPagamentoExterno(String idPagamentoExterno);

    Optional<Pagamento> findByAcademiaIdAndIdPagamentoExterno(Long academiaId, String idPagamentoExterno);

    Optional<Pagamento> findByIdAndAcademiaId(Long id, Long academiaId);

    Optional<Pagamento> findFirstByAcademiaIdAndFaturaIdAndProvedorAndMetodoAndStatusAndIdPagamentoExternoIsNotNullOrderByCriadoEmDesc(
            Long academiaId, Long faturaId, String provedor, MetodoPagamento metodo, StatusPagamento status);

    Optional<Pagamento> findFirstByAcademiaIdAndFaturaIdAndMetodoAndStatusInOrderByCriadoEmDesc(
            Long academiaId, Long faturaId, MetodoPagamento metodo, List<StatusPagamento> status);

    Optional<Pagamento> findFirstByAcademiaIdAndInscricaoGraduacaoIdAndProvedorAndMetodoAndStatusInAndIdPagamentoExternoIsNotNullOrderByCriadoEmDesc(
            Long academiaId, Long inscricaoGraduacaoId, String provedor, MetodoPagamento metodo, List<StatusPagamento> status);

    @Query("select coalesce(sum(p.valor), 0) from Pagamento p where p.fatura.id = :faturaId and p.status = Tic.Implementacao.model.StatusPagamento.PAGO")
    BigDecimal somarPagamentosConfirmadosPorFatura(Long faturaId);

    @Query("select coalesce(sum(p.valor), 0) from Pagamento p where p.fatura.id = :faturaId and p.status = Tic.Implementacao.model.StatusPagamento.PAGO and p.id <> :pagamentoIdIgnorado")
    BigDecimal somarPagamentosConfirmadosPorFaturaIgnorandoPagamento(Long faturaId, Long pagamentoIdIgnorado);

    @Query("""
            select p from Pagamento p
            join fetch p.inscricaoGraduacao i
            join fetch i.evento e
            join fetch i.aluno a
            join fetch i.graduacaoDestino g
            where p.academia.id = :academiaId
              and p.tipoCobranca = Tic.Implementacao.model.TipoCobranca.GRADUACAO
              and e.id in :eventoIds
            order by p.criadoEm desc
            """)
    List<Pagamento> findPagamentosGraduacaoPorEventos(@Param("academiaId") Long academiaId,
                                                       @Param("eventoIds") List<Long> eventoIds);

    @Query("""
            select p from Pagamento p
            join fetch p.fatura f
            where p.academia.id = :academiaId
              and p.tipoCobranca = Tic.Implementacao.model.TipoCobranca.MENSALIDADE
              and f.id in :faturaIds
            """)
    List<Pagamento> findMensalidadesPorFaturas(@Param("academiaId") Long academiaId,
                                               @Param("faturaIds") List<Long> faturaIds);

    @Query("""
            select p from Pagamento p
            join fetch p.fatura f
            where p.academia.id = :academiaId
              and p.tipoCobranca = Tic.Implementacao.model.TipoCobranca.MENSALIDADE
              and f.id in :faturaIds
            order by p.criadoEm desc
            """)
    List<Pagamento> findMensalidadesComFaturaPorFaturas(@Param("academiaId") Long academiaId,
                                                        @Param("faturaIds") List<Long> faturaIds);

    @Query("""
            select distinct p from Pagamento p
            left join fetch p.fatura f
            left join fetch f.aluno fa
            left join fetch p.inscricaoGraduacao i
            left join fetch i.evento e
            left join fetch i.aluno ia
            left join fetch i.graduacaoDestino g
            where p.academia.id = :academiaId
              and coalesce(p.pagoEm, p.criadoEm) >= :inicio
              and coalesce(p.pagoEm, p.criadoEm) < :fimExclusivo
            order by coalesce(p.pagoEm, p.criadoEm), p.id
            """)
    List<Pagamento> findParaRelatorioContabil(@Param("academiaId") Long academiaId,
                                              @Param("inicio") LocalDateTime inicio,
                                              @Param("fimExclusivo") LocalDateTime fimExclusivo);
}
