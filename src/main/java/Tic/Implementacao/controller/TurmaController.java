package Tic.Implementacao.controller;

import Tic.Implementacao.model.Turma;
import Tic.Implementacao.service.TurmaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/turmas")
public class TurmaController {

    private TurmaService service;

    public TurmaController(TurmaService service){
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Turma>> listar(){
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Optional<Turma>> buscarPorId(@PathVariable Long id){
        Optional<Turma> turma = service.buscarPorId(id);

        if(turma.isEmpty()){
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(turma);
    }

    @PostMapping
    public ResponseEntity<Turma> salva(@RequestBody Turma turma){
        Turma nova = service.salva(turma);

        if(nova != null){
            URI uri = URI.create("/turma/" + nova.getId());
            return ResponseEntity.created(uri).body(nova);
        }

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remove(@PathVariable Long id){
        if(service.remove(id)){
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Turma> atualizarDados(
            @PathVariable Long id,
            @RequestBody Turma alterado){

        Turma resposta = service.atualizarDados(id, alterado);

        if(resposta != null){
            return ResponseEntity.ok(resposta);
        }

        return ResponseEntity.notFound().build();
    }
}