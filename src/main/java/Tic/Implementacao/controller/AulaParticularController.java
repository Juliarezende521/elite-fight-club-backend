package Tic.Implementacao.controller;

import Tic.Implementacao.model.AulaParticular;
import Tic.Implementacao.service.AulaParticularService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/aulas_particulares")
public class AulaParticularController {

    private final AulaParticularService service;

    public AulaParticularController(AulaParticularService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<AulaParticular>> listarTodas() {
        return ResponseEntity.ok(service.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Optional<AulaParticular>> buscarPorId(@PathVariable Long id) {

        Optional<AulaParticular> aula = service.buscarPorId(id);

        if (aula.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(aula);
    }

    @PostMapping
    public ResponseEntity<AulaParticular> salvar(@RequestBody AulaParticular aulaParticular) {

        AulaParticular nova = service.salvar(aulaParticular);

        return ResponseEntity.ok(nova);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {

        if (service.remover(id)) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<AulaParticular> atualizar(
            @PathVariable Long id,
            @RequestBody AulaParticular dadosNovos) {

        try {

            AulaParticular atualizada = service.atualizar(id, dadosNovos);

            return ResponseEntity.ok(atualizada);

        } catch (RuntimeException e) {

            return ResponseEntity.notFound().build();

        }
    }
}