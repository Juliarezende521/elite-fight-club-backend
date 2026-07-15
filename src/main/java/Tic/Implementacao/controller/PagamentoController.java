package Tic.Implementacao.controller;

import Tic.Implementacao.dto.financeiro.CobrancaPixResponse;
import Tic.Implementacao.dto.financeiro.ConciliacaoPagamentoResponse;
import Tic.Implementacao.dto.financeiro.PagamentoManualRequest;
import Tic.Implementacao.model.Pagamento;
import Tic.Implementacao.service.PagamentoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class PagamentoController {

    private final PagamentoService service;

    public PagamentoController(PagamentoService service) {
        this.service = service;
    }

    @GetMapping("/faturas/{faturaId}/pagamentos")
    public ResponseEntity<List<Pagamento>> listarPorFatura(@PathVariable Long faturaId) {
        return ResponseEntity.ok(service.listarPorFatura(faturaId));
    }

    @PostMapping("/faturas/{faturaId}/pix")
    public ResponseEntity<CobrancaPixResponse> criarCobrancaPix(@PathVariable Long faturaId) {
        return ResponseEntity.ok(service.criarCobrancaPix(faturaId));
    }

    @PostMapping("/faturas/{faturaId}/pagamento-manual")
    public ResponseEntity<Pagamento> registrarPagamentoManual(
            @PathVariable Long faturaId,
            @Valid @RequestBody PagamentoManualRequest request) {
        return ResponseEntity.ok(service.registrarPagamentoManual(faturaId, request));
    }

    @PostMapping("/pagamentos/{pagamentoId}/conciliar")
    public ResponseEntity<ConciliacaoPagamentoResponse> conciliar(@PathVariable Long pagamentoId) {
        return ResponseEntity.ok(service.conciliar(pagamentoId));
    }
}
