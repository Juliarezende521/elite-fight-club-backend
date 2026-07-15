package Tic.Implementacao.payments.asaas;

import Tic.Implementacao.model.Aluno;
import Tic.Implementacao.model.Fatura;
import Tic.Implementacao.model.InscricaoGraduacao;
import Tic.Implementacao.payments.asaas.dto.AsaasCustomerResponse;
import Tic.Implementacao.payments.asaas.dto.AsaasPaymentResponse;
import Tic.Implementacao.payments.asaas.dto.AsaasPixQrCodeResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@ConditionalOnProperty(name = "payment.provider", havingValue = "asaas")
public class AsaasClient {

    private static final Logger logger = LoggerFactory.getLogger(AsaasClient.class);

    private final AsaasProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public AsaasClient(AsaasProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public AsaasCustomerResponse criarCliente(Aluno aluno) {
        Map<String, Object> body = new HashMap<>();
        body.put("name", aluno.getNome());
        body.put("email", aluno.getEmail());
        body.put("externalReference", "academia_" + aluno.getAcademia().getId() + "_aluno_" + aluno.getId());
        return post("/customers", body, AsaasCustomerResponse.class);
    }

    public AsaasPaymentResponse criarCobrancaPix(Fatura fatura, String customerId) {
        Map<String, Object> body = new HashMap<>();
        body.put("customer", customerId);
        body.put("billingType", "PIX");
        body.put("value", fatura.getValor().setScale(2, RoundingMode.HALF_UP));
        body.put("dueDate", fatura.getDataVencimento().toString());
        body.put("description", "Mensalidade " + fatura.getMesReferencia());
        body.put("externalReference", "academia_" + fatura.getAcademia().getId() + "_fatura_" + fatura.getId());
        return post("/payments", body, AsaasPaymentResponse.class);
    }

    public AsaasPaymentResponse criarCobrancaPixGraduacao(InscricaoGraduacao inscricao, String customerId) {
        Map<String, Object> body = new HashMap<>();
        body.put("customer", customerId);
        body.put("billingType", "PIX");
        body.put("value", inscricao.getValor().setScale(2, RoundingMode.HALF_UP));
        body.put("dueDate", inscricao.getEvento().getFimInscricoes().toLocalDate().toString());
        body.put("description", "Inscrição graduação " + inscricao.getEvento().getTitulo());
        body.put("externalReference", "academia_" + inscricao.getAcademia().getId() + "_inscricao_graduacao_" + inscricao.getId());
        return post("/payments", body, AsaasPaymentResponse.class);
    }

    public AsaasPixQrCodeResponse buscarQrCodePix(String paymentId) {
        return get("/payments/" + paymentId + "/pixQrCode", AsaasPixQrCodeResponse.class);
    }

    public AsaasPaymentResponse consultarPagamento(String paymentId) {
        return get("/payments/" + paymentId, AsaasPaymentResponse.class);
    }

    public void cancelarPagamento(String paymentId) {
        delete("/payments/" + paymentId);
    }

    private <T> T post(String path, Object body, Class<T> responseType) {
        return send("POST", path, toJson(body), responseType);
    }

    private <T> T get(String path, Class<T> responseType) {
        return send("GET", path, null, responseType);
    }

    private void delete(String path) {
        send("DELETE", path, null, Void.class);
    }

    private <T> T send(String method, String path, String body, Class<T> responseType) {
        validarConfiguracao();
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(properties.getBaseUrl() + path))
                    .header("accept", "application/json")
                    .header("access_token", properties.getApiKey());

            if (body != null) {
                builder.header("content-type", "application/json")
                        .method(method, HttpRequest.BodyPublishers.ofString(body));
            } else {
                builder.method(method, HttpRequest.BodyPublishers.noBody());
            }

            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                logger.warn("Chamada ao Asaas Sandbox falhou: metodo={}, path={}, status={}", method, path, response.statusCode());
                throw new RuntimeException("Erro ao chamar Asaas Sandbox. Status: " + response.statusCode() + ". " + extrairMensagemErro(response.body()));
            }
            if (responseType == Void.class) {
                return null;
            }
            return objectMapper.readValue(response.body(), responseType);
        } catch (IOException e) {
            throw new RuntimeException("Erro de comunicação com Asaas Sandbox", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Chamada ao Asaas Sandbox interrompida", e);
        }
    }

    private String toJson(Object body) {
        try {
            return objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erro ao montar requisição Asaas", e);
        }
    }

    private void validarConfiguracao() {
        if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            throw new RuntimeException("ASAAS_API_KEY não configurada");
        }
    }

    private String extrairMensagemErro(String body) {
        if (body == null || body.isBlank()) {
            return "Resposta sem detalhes.";
        }
        try {
            JsonNode root = objectMapper.readTree(body);
            JsonNode errors = root.path("errors");
            if (errors.isArray() && !errors.isEmpty()) {
                JsonNode first = errors.get(0);
                String code = first.path("code").asText("");
                String description = first.path("description").asText("");
                return ("Asaas " + code + ": " + description).trim();
            }
        } catch (Exception ignored) {
        }
        return "Resposta de erro recebida do Asaas.";
    }
}
