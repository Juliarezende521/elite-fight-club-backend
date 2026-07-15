package Tic.Implementacao.controller;

import Tic.Implementacao.dto.graduacao.GraduacaoRequest;
import Tic.Implementacao.dto.graduacao.GraduacaoResponse;
import Tic.Implementacao.dto.graduacao.AlunoElegivelGraduacaoResponse;
import Tic.Implementacao.dto.graduacao.ReordenarGraduacoesRequest;
import Tic.Implementacao.service.GraduacaoAlunoService;
import Tic.Implementacao.service.GraduacaoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/graduacoes")
public class GraduacaoController {

    private final GraduacaoService service;
    private final GraduacaoAlunoService graduacaoAlunoService;

    public GraduacaoController(GraduacaoService service, GraduacaoAlunoService graduacaoAlunoService) {
        this.service = service;
        this.graduacaoAlunoService = graduacaoAlunoService;
    }

    @GetMapping
    public ResponseEntity<List<GraduacaoResponse>> listarTodas() {
        return ResponseEntity.ok(service.listarTodas());
    }

    @GetMapping("/tipo/{tipoGraduacao}")
    public ResponseEntity<List<GraduacaoResponse>> listarPorTipo(@PathVariable String tipoGraduacao) {
        return ResponseEntity.ok(service.listarAtivas());
    }

    @GetMapping("/elegiveis")
    public ResponseEntity<List<AlunoElegivelGraduacaoResponse>> listarElegiveis(
            @RequestParam(required = false) Long graduacaoAtualId,
            @RequestParam(required = false) Long proximaGraduacaoId,
            @RequestParam(required = false) Long professorId,
            @RequestParam(defaultValue = "true") boolean somenteElegiveis) {
        return ResponseEntity.ok(graduacaoAlunoService.listarElegiveis(
                graduacaoAtualId, proximaGraduacaoId, professorId, somenteElegiveis));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GraduacaoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<GraduacaoResponse> salvar(@Valid @RequestBody GraduacaoRequest graduacao) {
        return ResponseEntity.ok(service.salvar(graduacao));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GraduacaoResponse> atualizar(@PathVariable Long id, @Valid @RequestBody GraduacaoRequest dadosNovos) {
        return ResponseEntity.ok(service.atualizar(id, dadosNovos));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<GraduacaoResponse> alterarStatus(@PathVariable Long id, @RequestParam boolean ativa) {
        return ResponseEntity.ok(service.alterarStatus(id, ativa));
    }

    @PutMapping("/reordenar")
    public ResponseEntity<List<GraduacaoResponse>> reordenar(@Valid @RequestBody ReordenarGraduacoesRequest request) {
        return ResponseEntity.ok(service.reordenar(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        if (service.remover(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
