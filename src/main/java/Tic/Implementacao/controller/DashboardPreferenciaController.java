package Tic.Implementacao.controller;

import Tic.Implementacao.model.DashboardPreferencia;
import Tic.Implementacao.service.DashboardPreferenciaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/dashboard-preferencias")
public class DashboardPreferenciaController {

    private final DashboardPreferenciaService service;

    public DashboardPreferenciaController(DashboardPreferenciaService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<DashboardPreferencia>> listarTodas() {
        return ResponseEntity.ok(service.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Optional<DashboardPreferencia>> buscarPorId(@PathVariable Long id) {
        Optional<DashboardPreferencia> preferencia = service.buscarPorId(id);
        if (preferencia.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(preferencia);
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<Optional<DashboardPreferencia>> buscarPorUsuario(@PathVariable Long usuarioId) {
        Optional<DashboardPreferencia> preferencia = service.buscarPorUsuario(usuarioId);
        if (preferencia.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(preferencia);
    }

    @PostMapping
    public ResponseEntity<DashboardPreferencia> salvar(@Valid @RequestBody DashboardPreferencia preferencia) {
        try {
            return ResponseEntity.ok(service.salvar(preferencia));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/usuario/{usuarioId}")
    public ResponseEntity<DashboardPreferencia> salvarOuAtualizarPorUsuario(
            @PathVariable Long usuarioId,
            @Valid @RequestBody DashboardPreferencia preferencia) {
        return ResponseEntity.ok(service.salvarOuAtualizarPorUsuario(usuarioId, preferencia));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<DashboardPreferencia> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody DashboardPreferencia dadosNovos) {
        try {
            return ResponseEntity.ok(service.atualizar(id, dadosNovos));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        if (service.remover(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
