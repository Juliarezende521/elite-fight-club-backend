package Tic.Implementacao.controller;

import Tic.Implementacao.model.Modalidade;
import Tic.Implementacao.service.ModalidadeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/modalidades")
public class ModalidadeController {

    private final ModalidadeService service;

    public ModalidadeController(ModalidadeService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Modalidade>> listarTodas() {
        return ResponseEntity.ok(service.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Optional<Modalidade>> buscarPorId(@PathVariable Long id) {
        Optional<Modalidade> modalidade = service.buscarPorId(id);
        if (modalidade.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(modalidade);
    }

    @GetMapping("/tipo/{tipoModalidade}")
    public ResponseEntity<List<Modalidade>> listarPorTipo(@PathVariable String tipoModalidade) {
        return ResponseEntity.ok(service.listarPorTipo(tipoModalidade));
    }

    @PostMapping
    public ResponseEntity<Modalidade> salvar(@RequestBody Modalidade modalidade) {
        Modalidade nova = service.salvar(modalidade);
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
    public ResponseEntity<Modalidade> atualizar(@PathVariable Long id, @RequestBody Modalidade dadosNovos) {
        try {
            Modalidade atualizada = service.atualizar(id, dadosNovos);
            return ResponseEntity.ok(atualizada);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
