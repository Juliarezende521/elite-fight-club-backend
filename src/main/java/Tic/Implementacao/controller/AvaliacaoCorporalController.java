package Tic.Implementacao.controller;

import Tic.Implementacao.dto.evolucaofisica.avaliacao.*;
import Tic.Implementacao.service.AvaliacaoCorporalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class AvaliacaoCorporalController {

    private final AvaliacaoCorporalService service;

    public AvaliacaoCorporalController(AvaliacaoCorporalService service) {
        this.service = service;
    }

    @GetMapping("/api/alunos/me/avaliacoes-corporais")
    public List<AvaliacaoCorporalResponse> listarMinhas() {
        return service.listarDoAlunoAtual();
    }

    @GetMapping("/api/alunos/me/avaliacoes-corporais/{avaliacaoId}")
    public AvaliacaoCorporalResponse buscarMinha(@PathVariable Long avaliacaoId) {
        return service.buscarDoAlunoAtual(avaliacaoId);
    }

    @GetMapping("/api/alunos/me/avaliacoes-corporais/resumo")
    public ResumoAvaliacaoCorporalResponse resumoMinha() {
        return service.resumoDoAlunoAtual();
    }

    @GetMapping("/api/alunos/me/avaliacoes-corporais/comparativo")
    public ComparativoAvaliacoesResponse compararMinhas(@RequestParam Long avaliacaoInicialId,
                                                        @RequestParam Long avaliacaoFinalId) {
        return service.compararDoAlunoAtual(avaliacaoInicialId, avaliacaoFinalId);
    }

    @PostMapping("/api/alunos/{alunoId}/avaliacoes-corporais")
    public ResponseEntity<AvaliacaoCorporalResponse> cadastrar(@PathVariable Long alunoId,
                                                               @Valid @RequestBody CriarAvaliacaoCorporalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.cadastrar(alunoId, request));
    }

    @GetMapping("/api/alunos/{alunoId}/avaliacoes-corporais")
    public List<AvaliacaoCorporalResponse> listar(@PathVariable Long alunoId) {
        return service.listar(alunoId);
    }

    @GetMapping("/api/alunos/{alunoId}/avaliacoes-corporais/{avaliacaoId}")
    public AvaliacaoCorporalResponse buscar(@PathVariable Long alunoId, @PathVariable Long avaliacaoId) {
        return service.buscar(alunoId, avaliacaoId);
    }

    @GetMapping("/api/alunos/{alunoId}/avaliacoes-corporais/resumo")
    public ResumoAvaliacaoCorporalResponse resumo(@PathVariable Long alunoId) {
        return service.resumo(alunoId);
    }

    @GetMapping("/api/alunos/{alunoId}/avaliacoes-corporais/comparativo")
    public ComparativoAvaliacoesResponse comparar(@PathVariable Long alunoId,
                                                  @RequestParam Long avaliacaoInicialId,
                                                  @RequestParam Long avaliacaoFinalId) {
        return service.comparar(alunoId, avaliacaoInicialId, avaliacaoFinalId);
    }

    @PutMapping("/api/alunos/{alunoId}/avaliacoes-corporais/{avaliacaoId}")
    public AvaliacaoCorporalResponse atualizar(@PathVariable Long alunoId,
                                               @PathVariable Long avaliacaoId,
                                               @Valid @RequestBody AtualizarAvaliacaoCorporalRequest request) {
        return service.atualizar(alunoId, avaliacaoId, request);
    }

    @DeleteMapping("/api/alunos/{alunoId}/avaliacoes-corporais/{avaliacaoId}")
    public ResponseEntity<Void> excluir(@PathVariable Long alunoId, @PathVariable Long avaliacaoId) {
        service.excluirLogicamente(alunoId, avaliacaoId);
        return ResponseEntity.noContent().build();
    }
}
