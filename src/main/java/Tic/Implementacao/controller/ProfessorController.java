package Tic.Implementacao.controller;

import Tic.Implementacao.dto.professor.ProfessorRequest;
import Tic.Implementacao.dto.professor.ProfessorResponse;
import Tic.Implementacao.exception.ApiErrorResponse;
import Tic.Implementacao.service.ProfessorService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/professores")
public class ProfessorController {
    private final ProfessorService service;
    public ProfessorController(ProfessorService service) { this.service = service; }
    @GetMapping
    public List<ProfessorResponse> listar() { return service.listar(); }
    @GetMapping("/{id}")
    public ProfessorResponse exibirIdentificacao(@PathVariable Long id) { return service.exibirIdentificacao(id); }
    @PostMapping
    public ResponseEntity<ProfessorResponse> salva(@Valid @RequestBody ProfessorRequest request) {
        ProfessorResponse professor = service.salva(request);
        return ResponseEntity.created(URI.create("/professores/" + professor.id())).body(professor);
    }
    @PatchMapping("/{id}")
    public ProfessorResponse atualizarDados(@PathVariable Long id, @Valid @RequestBody ProfessorRequest request) {
        return service.atualizarDados(id, request);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remove(@PathVariable Long id) {
        service.remove(id);
        return ResponseEntity.noContent().build();
    }
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> naoEncontrado(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode()).body(
                new ApiErrorResponse(ex.getStatusCode().value(), "NOT_FOUND", "Professor nao encontrado", Map.of()));
    }
}
