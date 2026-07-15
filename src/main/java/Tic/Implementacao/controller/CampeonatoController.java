package Tic.Implementacao.controller;

import Tic.Implementacao.model.Campeonato;
import Tic.Implementacao.service.CampeonatoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/campeonatos")

public class CampeonatoController {


    private CampeonatoService service;

    public CampeonatoController(CampeonatoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Campeonato>> listar(){
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Optional<Campeonato>> exibirInformacao(@PathVariable Long id){
        Optional<Campeonato> campeonato = service.exibirInformacoes(id);
        if(campeonato.isEmpty()){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(campeonato);
    }

    @PostMapping
    public ResponseEntity<Campeonato> salva(@RequestBody Campeonato campeonato){
        Campeonato camp = service.salva(campeonato);
        if(campeonato !=null){
            URI uri = URI.create("/campeonato"+campeonato.getId());
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remove(@PathVariable Long id){
        if(service.remove(id)){
            ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Campeonato> atualizaInformacoes(@PathVariable Long id, @RequestBody Campeonato alterado){
        Campeonato resposta = service.atualizarInformacoes(id, alterado);
        if(resposta != null){
            return ResponseEntity.ok(resposta);
        }
        return ResponseEntity.notFound().build();
    }
}
