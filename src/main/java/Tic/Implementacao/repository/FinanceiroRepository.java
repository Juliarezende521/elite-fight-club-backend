package Tic.Implementacao.repository;

import Tic.Implementacao.model.Financeiro;
import Tic.Implementacao.model.StatusPagamento;
import Tic.Implementacao.model.TipoModalidade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface FinanceiroRepository extends JpaRepository<Financeiro, Long> {

    List<Financeiro> findByAlunoId(Long alunoId);

    List<Financeiro> findByModalidadeId(Long modalidadeId);

    List<Financeiro> findByModalidadeTipoModalidade(TipoModalidade tipoModalidade);

    List<Financeiro> findByStatusPagamento(StatusPagamento statusPagamento);

    List<Financeiro> findByDataPagamentoIsNotNull();

    List<Financeiro> findByDataPagamentoIsNullAndDataVencimentoBefore(LocalDate data);

    List<Financeiro> findByDataPagamentoIsNullAndDataVencimento(LocalDate data);

    List<Financeiro> findByDataPagamentoIsNullAndDataVencimentoBetween(LocalDate inicio, LocalDate fim);
}
