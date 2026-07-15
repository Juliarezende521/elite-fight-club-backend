package Tic.Implementacao.controller;

import Tic.Implementacao.dto.evolucaofisica.meta.*;
import Tic.Implementacao.service.MetaPesoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class MetaPesoController {

    private final MetaPesoService metaPesoService;

    public MetaPesoController(MetaPesoService metaPesoService) {
        this.metaPesoService = metaPesoService;
    }

    @PostMapping("/api/alunos/me/metas-peso")
    public ResponseEntity<MetaPesoResponse> criarMetaDoAlunoAtual(@Valid @RequestBody CriarMetaPesoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(metaPesoService.criarMetaDoAlunoAtual(request));
    }

    @GetMapping("/api/alunos/me/metas-peso")
    public List<MetaPesoResponse> listarMetasDoAlunoAtual() {
        return metaPesoService.listarMetasDoAlunoAtual();
    }

    @GetMapping("/api/alunos/me/metas-peso/ativa")
    public ProgressoMetaPesoResponse buscarMetaAtivaDoAlunoAtual() {
        return metaPesoService.buscarMetaAtivaDoAlunoAtual();
    }

    @GetMapping("/api/alunos/me/metas-peso/{metaId}")
    public MetaPesoResponse buscarMetaDoAlunoAtual(@PathVariable Long metaId) {
        return metaPesoService.buscarMetaDoAlunoAtual(metaId);
    }

    @PutMapping("/api/alunos/me/metas-peso/{metaId}")
    public MetaPesoResponse atualizarMetaDoAlunoAtual(@PathVariable Long metaId,
                                                      @Valid @RequestBody AtualizarMetaPesoRequest request) {
        return metaPesoService.atualizarMetaDoAlunoAtual(metaId, request);
    }

    @PatchMapping("/api/alunos/me/metas-peso/{metaId}/cancelar")
    public MetaPesoResponse cancelarMetaDoAlunoAtual(@PathVariable Long metaId,
                                                     @RequestBody(required = false) EncerrarMetaPesoRequest request) {
        return metaPesoService.cancelarMetaDoAlunoAtual(metaId, request);
    }

    @PostMapping("/api/alunos/me/metas-peso/{metaId}/substituir")
    public MetaPesoResponse substituirMetaDoAlunoAtual(@PathVariable Long metaId,
                                                       @Valid @RequestBody EncerrarMetaPesoRequest request) {
        return metaPesoService.substituirMetaDoAlunoAtual(metaId, request);
    }

    @PostMapping("/api/alunos/{alunoId}/metas-peso")
    public ResponseEntity<MetaPesoResponse> criarMeta(@PathVariable Long alunoId,
                                                      @Valid @RequestBody CriarMetaPesoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(metaPesoService.criarMeta(alunoId, request));
    }

    @GetMapping("/api/alunos/{alunoId}/metas-peso")
    public List<MetaPesoResponse> listarMetas(@PathVariable Long alunoId) {
        return metaPesoService.listarMetas(alunoId);
    }

    @GetMapping("/api/alunos/{alunoId}/metas-peso/ativa")
    public ProgressoMetaPesoResponse buscarMetaAtiva(@PathVariable Long alunoId) {
        return metaPesoService.buscarMetaAtiva(alunoId);
    }

    @GetMapping("/api/alunos/{alunoId}/metas-peso/{metaId}")
    public MetaPesoResponse buscarMeta(@PathVariable Long alunoId, @PathVariable Long metaId) {
        return metaPesoService.buscarMeta(alunoId, metaId);
    }

    @PutMapping("/api/alunos/{alunoId}/metas-peso/{metaId}")
    public MetaPesoResponse atualizarMeta(@PathVariable Long alunoId,
                                          @PathVariable Long metaId,
                                          @Valid @RequestBody AtualizarMetaPesoRequest request) {
        return metaPesoService.atualizarMeta(alunoId, metaId, request);
    }

    @PatchMapping("/api/alunos/{alunoId}/metas-peso/{metaId}/cancelar")
    public MetaPesoResponse cancelarMeta(@PathVariable Long alunoId,
                                         @PathVariable Long metaId,
                                         @RequestBody(required = false) EncerrarMetaPesoRequest request) {
        return metaPesoService.cancelarMeta(alunoId, metaId, request);
    }

    @PostMapping("/api/alunos/{alunoId}/metas-peso/{metaId}/substituir")
    public MetaPesoResponse substituirMeta(@PathVariable Long alunoId,
                                           @PathVariable Long metaId,
                                           @Valid @RequestBody EncerrarMetaPesoRequest request) {
        return metaPesoService.substituirMeta(alunoId, metaId, request);
    }
}
