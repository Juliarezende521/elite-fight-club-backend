package Tic.Implementacao.controller;

import Tic.Implementacao.model.Evolucao;
import Tic.Implementacao.service.EvolucaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/Evolucao")
public class EvolucaoController {

    private final EvolucaoService service;

    public EvolucaoController(EvolucaoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Evolucao>> listarTodas() {
        return ResponseEntity.ok(service.listarTodas());
    }

    @GetMapping("/aluno/{alunoId}")
    public ResponseEntity<List<Evolucao>> listarPorAluno(@PathVariable Long alunoId) {
        return ResponseEntity.ok(service.listarPorAluno(alunoId));
    }

    @GetMapping("/aluno/{alunoId}/linha-do-tempo")
    public ResponseEntity<List<Evolucao>> listarLinhaDoTempoPorAluno(@PathVariable Long alunoId) {
        return ResponseEntity.ok(service.listarLinhaDoTempoPorAluno(alunoId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Optional<Evolucao>> buscarPorId(@PathVariable Long id) {

        Optional<Evolucao> evolucao = service.buscarPorId(id);

        if (evolucao.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(evolucao);
    }

    @PostMapping
    public ResponseEntity<Evolucao> salvar(@RequestBody Evolucao evolucao) {

        Evolucao nova = service.salvar(evolucao);

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
    public ResponseEntity<Evolucao> atualizar(
            @PathVariable Long id,
            @RequestBody Evolucao dadosNovos) {

        try {

            Evolucao atualizada = service.atualizar(id, dadosNovos);

            return ResponseEntity.ok(atualizada);

        } catch (RuntimeException e) {

            return ResponseEntity.notFound().build();

        }
    }
}
