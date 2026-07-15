package Tic.Implementacao.controller;

import Tic.Implementacao.dto.evolucaofisica.AtualizarPesoRequest;
import Tic.Implementacao.dto.evolucaofisica.EvolucaoPesoResponse;
import Tic.Implementacao.dto.evolucaofisica.RegistrarPesoRequest;
import Tic.Implementacao.dto.evolucaofisica.ResumoEvolucaoPesoResponse;
import Tic.Implementacao.service.EvolucaoPesoService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
public class EvolucaoPesoController {

    private final EvolucaoPesoService evolucaoPesoService;

    public EvolucaoPesoController(EvolucaoPesoService evolucaoPesoService) {
        this.evolucaoPesoService = evolucaoPesoService;
    }

    @PostMapping("/api/alunos/me/pesos")
    public ResponseEntity<EvolucaoPesoResponse> registrarPesoDoAlunoAtual(@Valid @RequestBody RegistrarPesoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(evolucaoPesoService.registrarPesoDoAlunoAtual(request));
    }

    @GetMapping("/api/alunos/me/pesos")
    public List<EvolucaoPesoResponse> listarPesosDoAlunoAtual(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return evolucaoPesoService.listarHistoricoDoAlunoAtual(inicio, fim);
    }

    @GetMapping("/api/alunos/me/pesos/resumo")
    public ResumoEvolucaoPesoResponse resumoPesosDoAlunoAtual() {
        return evolucaoPesoService.buscarResumoDoAlunoAtual();
    }

    @PutMapping("/api/alunos/me/pesos/{registroId}")
    public EvolucaoPesoResponse atualizarPesoDoAlunoAtual(@PathVariable Long registroId,
                                                          @Valid @RequestBody AtualizarPesoRequest request) {
        return evolucaoPesoService.atualizarPesoDoAlunoAtual(registroId, request);
    }

    @DeleteMapping("/api/alunos/me/pesos/{registroId}")
    public ResponseEntity<Void> excluirPesoDoAlunoAtual(@PathVariable Long registroId) {
        evolucaoPesoService.excluirLogicamenteDoAlunoAtual(registroId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/alunos/{alunoId}/pesos")
    public ResponseEntity<EvolucaoPesoResponse> registrarPeso(@PathVariable Long alunoId,
                                                              @Valid @RequestBody RegistrarPesoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(evolucaoPesoService.registrarPeso(alunoId, request));
    }

    @GetMapping("/api/alunos/{alunoId}/pesos")
    public List<EvolucaoPesoResponse> listarPesos(
            @PathVariable Long alunoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return evolucaoPesoService.listarHistorico(alunoId, inicio, fim);
    }

    @GetMapping("/api/alunos/{alunoId}/pesos/resumo")
    public ResumoEvolucaoPesoResponse resumoPesos(@PathVariable Long alunoId) {
        return evolucaoPesoService.buscarResumo(alunoId);
    }

    @PutMapping("/api/alunos/{alunoId}/pesos/{registroId}")
    public EvolucaoPesoResponse atualizarPeso(@PathVariable Long alunoId,
                                              @PathVariable Long registroId,
                                              @Valid @RequestBody AtualizarPesoRequest request) {
        return evolucaoPesoService.atualizarPeso(alunoId, registroId, request);
    }

    @DeleteMapping("/api/alunos/{alunoId}/pesos/{registroId}")
    public ResponseEntity<Void> excluirPeso(@PathVariable Long alunoId, @PathVariable Long registroId) {
        evolucaoPesoService.excluirLogicamente(alunoId, registroId);
        return ResponseEntity.noContent().build();
    }
}
