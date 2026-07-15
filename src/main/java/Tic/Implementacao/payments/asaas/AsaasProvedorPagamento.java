package Tic.Implementacao.payments.asaas;

import Tic.Implementacao.dto.financeiro.CobrancaPixResponse;
import Tic.Implementacao.model.Aluno;
import Tic.Implementacao.model.Fatura;
import Tic.Implementacao.model.InscricaoGraduacao;
import Tic.Implementacao.model.StatusPagamento;
import Tic.Implementacao.payments.ProvedorPagamento;
import Tic.Implementacao.payments.asaas.dto.AsaasPaymentResponse;
import Tic.Implementacao.payments.asaas.dto.AsaasPixQrCodeResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Component
@ConditionalOnProperty(name = "payment.provider", havingValue = "asaas")
public class AsaasProvedorPagamento implements ProvedorPagamento {

    private final AsaasClient client;

    public AsaasProvedorPagamento(AsaasClient client) {
        this.client = client;
    }

    @Override
    public String nome() {
        return "ASAAS";
    }

    @Override
    public String criarCliente(Aluno aluno) {
        return client.criarCliente(aluno).getId();
    }

    @Override
    public CobrancaPixResponse criarCobrancaPix(Fatura fatura) {
        AsaasPaymentResponse payment = client.criarCobrancaPix(fatura, fatura.getAluno().getAsaasCustomerId());
        AsaasPixQrCodeResponse qrCode = client.buscarQrCodePix(payment.getId());
        return new CobrancaPixResponse(
                null,
                fatura.getId(),
                AsaasStatusMapper.paraStatusInterno(payment.getStatus()),
                fatura.getValor(),
                payment.getId(),
                qrCode.getPayload(),
                null,
                qrCode.getEncodedImage(),
                parseData(qrCode.getExpirationDate())
        );
    }

    @Override
    public CobrancaPixResponse criarCobrancaPixGraduacao(InscricaoGraduacao inscricao) {
        AsaasPaymentResponse payment = client.criarCobrancaPixGraduacao(inscricao, inscricao.getAluno().getAsaasCustomerId());
        AsaasPixQrCodeResponse qrCode = client.buscarQrCodePix(payment.getId());
        return new CobrancaPixResponse(
                null,
                null,
                AsaasStatusMapper.paraStatusInterno(payment.getStatus()),
                inscricao.getValor(),
                payment.getId(),
                qrCode.getPayload(),
                null,
                qrCode.getEncodedImage(),
                parseData(qrCode.getExpirationDate())
        );
    }

    @Override
    public StatusPagamento consultarPagamento(String idPagamentoExterno) {
        return AsaasStatusMapper.paraStatusInterno(client.consultarPagamento(idPagamentoExterno).getStatus());
    }

    @Override
    public void cancelarPagamento(String idPagamentoExterno) {
        client.cancelarPagamento(idPagamentoExterno);
    }

    private LocalDateTime parseData(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return OffsetDateTime.parse(value).toLocalDateTime();
        } catch (RuntimeException ignored) {
            return LocalDateTime.parse(value);
        }
    }
}
