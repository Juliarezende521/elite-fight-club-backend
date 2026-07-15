package Tic.Implementacao.payments;

import Tic.Implementacao.dto.financeiro.CobrancaPixResponse;
import Tic.Implementacao.model.Aluno;
import Tic.Implementacao.model.Fatura;
import Tic.Implementacao.model.InscricaoGraduacao;
import Tic.Implementacao.model.StatusPagamento;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "payment.provider", havingValue = "mock", matchIfMissing = true)
public class MockProvedorPagamento implements ProvedorPagamento {

    @Override
    public String nome() {
        return "MOCK";
    }

    @Override
    public String criarCliente(Aluno aluno) {
        return "mock_customer_" + aluno.getId();
    }

    @Override
    public CobrancaPixResponse criarCobrancaPix(Fatura fatura) {
        String externo = "mock_pix_" + UUID.randomUUID();
        return new CobrancaPixResponse(
                null,
                fatura.getId(),
                StatusPagamento.PENDENTE,
                fatura.getValor(),
                externo,
                "000201MOCKPIX" + fatura.getId() + externo,
                "https://mock.local/pix/" + externo + "/qrcode",
                null,
                LocalDateTime.now().plusDays(1)
        );
    }

    @Override
    public CobrancaPixResponse criarCobrancaPixGraduacao(InscricaoGraduacao inscricao) {
        String externo = "mock_pix_grad_" + UUID.randomUUID();
        return new CobrancaPixResponse(
                null,
                null,
                StatusPagamento.PENDENTE,
                inscricao.getValor(),
                externo,
                "000201MOCKPIXGRAD" + inscricao.getId() + externo,
                "https://mock.local/pix/graduacao/" + externo + "/qrcode",
                null,
                LocalDateTime.now().plusDays(1)
        );
    }

    @Override
    public StatusPagamento consultarPagamento(String idPagamentoExterno) {
        return StatusPagamento.PENDENTE;
    }

    @Override
    public void cancelarPagamento(String idPagamentoExterno) {
    }
}
