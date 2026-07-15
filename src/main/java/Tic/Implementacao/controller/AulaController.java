package Tic.Implementacao.controller;

import Tic.Implementacao.model.Aula;
import Tic.Implementacao.service.AulaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/aulas")
public class AulaController {

    private final AulaService service;

    public AulaController(AulaService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Aula>> listarTodas() {
        return ResponseEntity.ok(service.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Optional<Aula>> buscarPorId(@PathVariable Long id) {

        Optional<Aula> aula = service.buscarPorId(id);

        if (aula.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(aula);
    }

    @PostMapping
    public ResponseEntity<Aula> salvar(@RequestBody Aula aula) {

        Aula nova = service.salvar(aula);

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
    public ResponseEntity<Aula> atualizar(
            @PathVariable Long id,
            @RequestBody Aula dadosNovos) {

        try {

            Aula atualizada = service.atualizar(id, dadosNovos);

            return ResponseEntity.ok(atualizada);

        } catch (RuntimeException e) {

            return ResponseEntity.notFound().build();

        }
    }
}