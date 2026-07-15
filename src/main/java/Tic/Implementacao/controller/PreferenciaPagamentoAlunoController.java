package Tic.Implementacao.controller;

import Tic.Implementacao.dto.financeiro.preferencia.PreferenciaPagamentoRequest;
import Tic.Implementacao.dto.financeiro.preferencia.PreferenciaPagamentoResponse;
import Tic.Implementacao.service.PreferenciaPagamentoAlunoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class PreferenciaPagamentoAlunoController {

    private final PreferenciaPagamentoAlunoService service;

    public PreferenciaPagamentoAlunoController(PreferenciaPagamentoAlunoService service) {
        this.service = service;
    }

    @GetMapping("/api/alunos/me/preferencia-pagamento")
    public ResponseEntity<PreferenciaPagamentoResponse> buscarMinhaPreferencia() {
        return service.buscarDoAlunoAtual()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PutMapping("/api/alunos/me/preferencia-pagamento")
    public ResponseEntity<PreferenciaPagamentoResponse> salvarMinhaPreferencia(
            @Valid @RequestBody PreferenciaPagamentoRequest request) {
        return ResponseEntity.ok(service.salvarDoAlunoAtual(request));
    }

    @DeleteMapping("/api/alunos/me/preferencia-pagamento")
    public ResponseEntity<Void> removerMinhaPreferencia() {
        service.removerDoAlunoAtual();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/alunos/{alunoId}/preferencia-pagamento")
    public ResponseEntity<PreferenciaPagamentoResponse> buscarPorAluno(@PathVariable Long alunoId) {
        return service.buscarPorAluno(alunoId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
