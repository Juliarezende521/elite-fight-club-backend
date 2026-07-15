package Tic.Implementacao.controller;

import Tic.Implementacao.dto.agenda.PlanoRequest;
import Tic.Implementacao.model.Plano;
import Tic.Implementacao.service.PlanoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/planos")
public class PlanoController {

    private final PlanoService service;

    public PlanoController(PlanoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Plano>> listarTodos() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @PostMapping
    public ResponseEntity<Plano> salvar(@Valid @RequestBody PlanoRequest request) {
        return ResponseEntity.ok(service.salvar(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Plano> atualizar(@PathVariable Long id, @Valid @RequestBody PlanoRequest request) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Plano> alterarStatus(@PathVariable Long id, @RequestParam boolean ativo) {
        return ResponseEntity.ok(service.alterarStatus(id, ativo));
    }
}
