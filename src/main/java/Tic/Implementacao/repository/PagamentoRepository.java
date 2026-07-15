package Tic.Implementacao.repository;

import Tic.Implementacao.model.Pagamento;
import Tic.Implementacao.model.MetodoPagamento;
import Tic.Implementacao.model.StatusPagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
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
}
