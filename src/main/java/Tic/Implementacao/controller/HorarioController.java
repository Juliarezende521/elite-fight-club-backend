package Tic.Implementacao.controller;

import Tic.Implementacao.model.Horario;
import Tic.Implementacao.service.HorarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/horarios")
public class HorarioController {

    private final HorarioService service;

    public HorarioController(HorarioService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Horario>> listarTodos() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Optional<Horario>> buscarPorId(@PathVariable Long id) {

        Optional<Horario> horario = service.buscarPorId(id);

        if (horario.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(horario);
    }

    @PostMapping
    public ResponseEntity<Horario> salvar(@RequestBody Horario horario) {

        Horario novo = service.salvar(horario);

        return ResponseEntity.ok(novo);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {

        if (service.remover(id)) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Horario> atualizar(
            @PathVariable Long id,
            @RequestBody Horario dadosNovos) {

        try {

            Horario atualizado = service.atualizar(id, dadosNovos);

            return ResponseEntity.ok(atualizado);

        } catch (RuntimeException e) {

            return ResponseEntity.notFound().build();

        }
    }
}