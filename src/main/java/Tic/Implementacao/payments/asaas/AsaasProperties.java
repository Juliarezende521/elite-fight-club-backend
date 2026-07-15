package Tic.Implementacao.payments.asaas;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AsaasProperties {

    private final String baseUrl;
    private final String apiKey;
    private final String webhookToken;

    public AsaasProperties(
            @Value("${asaas.base-url:https://api-sandbox.asaas.com/v3}") String baseUrl,
            @Value("${asaas.api-key:}") String apiKey,
            @Value("${asaas.webhook-token:}") String webhookToken) {
        this.baseUrl = normalizarBaseUrl(baseUrl);
        this.apiKey = apiKey;
        this.webhookToken = webhookToken;
    }

    public String getBaseUrl() { return baseUrl; }
    public String getApiKey() { return apiKey; }
    public String getWebhookToken() { return webhookToken; }

    private String normalizarBaseUrl(String value) {
        if (value == null || value.isBlank()) {
            return "https://api-sandbox.asaas.com/v3";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
