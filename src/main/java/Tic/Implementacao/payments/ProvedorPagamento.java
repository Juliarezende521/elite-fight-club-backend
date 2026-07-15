package Tic.Implementacao.payments;

import Tic.Implementacao.dto.financeiro.CobrancaPixResponse;
import Tic.Implementacao.model.Aluno;
import Tic.Implementacao.model.Fatura;
import Tic.Implementacao.model.InscricaoGraduacao;
import Tic.Implementacao.model.StatusPagamento;

public interface ProvedorPagamento {

    String nome();

    String criarCliente(Aluno aluno);

    CobrancaPixResponse criarCobrancaPix(Fatura fatura);

    CobrancaPixResponse criarCobrancaPixGraduacao(InscricaoGraduacao inscricao);

    StatusPagamento consultarPagamento(String idPagamentoExterno);

    void cancelarPagamento(String idPagamentoExterno);
}
