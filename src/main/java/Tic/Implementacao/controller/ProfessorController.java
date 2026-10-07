package Tic.Implementacao.controller;

import Tic.Implementacao.dto.professor.*;
import Tic.Implementacao.model.TipoAtendimentoProfessor;
import Tic.Implementacao.service.ProfessorService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/professores")
public class ProfessorController {

    private final ProfessorService service;

    public ProfessorController(ProfessorService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<ProfessorResponse>> listar(
            @RequestParam(required = false) Long modalidadeId,
            @RequestParam(required = false) TipoAtendimentoProfessor tipoAtendimento) {
        return ResponseEntity.ok(service.listar(modalidadeId, tipoAtendimento));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfessorResponse> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscar(id));
    }

    @PostMapping
    public ResponseEntity<ProfessorResponse> criar(@Valid @RequestBody CriarProfessorRequest request) {
        ProfessorResponse response = service.criar(request);
        return ResponseEntity.created(URI.create("/api/professores/" + response.getId())).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProfessorResponse> atualizar(@PathVariable Long id,
                                                       @Valid @RequestBody AtualizarProfessorRequest request) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ProfessorResponse> alterarStatus(@PathVariable Long id,
                                                           @Valid @RequestBody AtualizarStatusProfessorRequest request) {
        return ResponseEntity.ok(service.alterarStatus(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/modalidades")
    public ResponseEntity<List<ProfessorModalidadeResponse>> listarModalidades(@PathVariable Long id) {
        return ResponseEntity.ok(service.listarModalidades(id));
    }

    @PutMapping("/{id}/modalidades")
    public ResponseEntity<ProfessorResponse> atualizarModalidades(
            @PathVariable Long id,
            @Valid @RequestBody AtualizarModalidadesProfessorRequest request) {
        return ResponseEntity.ok(service.atualizarModalidades(id, request));
    }
}
