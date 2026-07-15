package Tic.Implementacao.controller;

import Tic.Implementacao.model.Professor;
import Tic.Implementacao.service.ProfessorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/professores")
public class ProfessorController {

    private ProfessorService service;

    public ProfessorController(ProfessorService service) {
        this.service = service;
    }


    //GET
    @GetMapping
    public ResponseEntity<List<Professor>> listar(){
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Optional<Professor>> exibirIdentificacao(@PathVariable Long id){
        Optional<Professor> professor = service.exibirIdentificacao(id);
        if(professor.isEmpty()){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(professor);
    }

    @PostMapping
    public ResponseEntity<Professor> salva(@RequestBody Professor professor){
        Professor prof =  service.salva(professor);
        if(prof !=null){
            URI uri = URI.create("/professor"+prof.getId());
            return ResponseEntity.noContent().build();
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
    public ResponseEntity<Professor> atualizarDados(@PathVariable Long id, @RequestBody Professor alterado){
        Professor resposta = service.atualizarDados(id, alterado);
        if(resposta != null){
            return ResponseEntity.ok(resposta);
        }
        return ResponseEntity.notFound().build();

    }



}
