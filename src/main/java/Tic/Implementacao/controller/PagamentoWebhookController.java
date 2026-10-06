package Tic.Implementacao.controller;

import Tic.Implementacao.service.PagamentoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks/pagamentos")
public class PagamentoWebhookController {

    private final PagamentoService service;

    public PagamentoWebhookController(PagamentoService service) {
        this.service = service;
    }

    @PostMapping("/asaas")
    public ResponseEntity<Void> receberWebhookAsaas(@RequestHeader(name = "asaas-access-token", required = false) String token,
                                                    @RequestBody String payload) {
        service.processarWebhookAsaas(token, payload);
        return ResponseEntity.ok().build();
    }
}
