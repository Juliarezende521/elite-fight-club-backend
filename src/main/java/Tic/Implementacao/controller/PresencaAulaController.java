package Tic.Implementacao.controller;

import Tic.Implementacao.dto.presenca.*;
import Tic.Implementacao.service.PresencaAulaService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
public class PresencaAulaController {

    private final PresencaAulaService service;

    public PresencaAulaController(PresencaAulaService service) {
        this.service = service;
    }

    @PutMapping("/api/aulas/turmas/{aulaId}/presencas/{alunoId}")
    public ResponseEntity<PresencaAlunoResponse> registrarPresencaTurma(
            @PathVariable Long aulaId,
            @PathVariable Long alunoId,
            @Valid @RequestBody RegistrarPresencaRequest request) {
        return ResponseEntity.ok(service.registrarPresencaTurma(aulaId, alunoId, request));
    }

    @PutMapping("/api/aulas/personais/{aulaId}/presenca")
    public ResponseEntity<PresencaAlunoResponse> registrarPresencaPersonal(
            @PathVariable Long aulaId,
            @Valid @RequestBody RegistrarPresencaRequest request) {
        return ResponseEntity.ok(service.registrarPresencaPersonal(aulaId, request));
    }

    @GetMapping("/api/aulas/turmas/{aulaId}/presencas")
    public ResponseEntity<ListaPresencasAulaResponse> listarPresencasTurma(@PathVariable Long aulaId) {
        return ResponseEntity.ok(service.listarPresencasTurma(aulaId));
    }

    @GetMapping("/api/aulas/personais/{aulaId}/presenca")
    public ResponseEntity<PresencaAlunoResponse> consultarPresencaPersonal(@PathVariable Long aulaId) {
        return ResponseEntity.ok(service.consultarPresencaPersonal(aulaId));
    }

    @GetMapping("/api/alunos/{alunoId}/check-ins")
    public ResponseEntity<HistoricoCheckInAlunoResponse> listarHistoricoAluno(
            @PathVariable Long alunoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return ResponseEntity.ok(service.listarHistoricoAluno(alunoId, inicio, fim));
    }

    @GetMapping("/api/alunos/me/check-ins")
    public ResponseEntity<HistoricoCheckInAlunoResponse> listarMeuHistorico(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return ResponseEntity.ok(service.listarMeuHistorico(inicio, fim));
    }
}
