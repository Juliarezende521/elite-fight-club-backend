package Tic.Implementacao.controller;

import Tic.Implementacao.dto.aula.CancelarAulaRequest;
import Tic.Implementacao.dto.aula.ItemAulaProfessorResponse;
import Tic.Implementacao.dto.aula.RemarcarAulaRequest;
import Tic.Implementacao.model.AulaPersonal;
import Tic.Implementacao.model.AulaTurma;
import Tic.Implementacao.service.AulaAgendaService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/aulas")
public class AulaAgendaController {

    private final AulaAgendaService service;

    public AulaAgendaController(AulaAgendaService service) {
        this.service = service;
    }

    @PostMapping("/turmas/horarios/{horarioTurmaId}")
    public ResponseEntity<AulaTurma> gerarAulaTurma(
            @PathVariable Long horarioTurmaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return ResponseEntity.ok(service.gerarAulaTurma(horarioTurmaId, data));
    }

    @PostMapping("/personais/agendas/{agendaPersonalId}")
    public ResponseEntity<AulaPersonal> gerarAulaPersonal(
            @PathVariable Long agendaPersonalId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return ResponseEntity.ok(service.gerarAulaPersonal(agendaPersonalId, data));
    }

    @PostMapping("/gerar")
    public ResponseEntity<Void> gerarAulasDoDia(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        service.gerarAulasDoDia(data);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/gerar-periodo")
    public ResponseEntity<Void> gerarAulasDoPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        service.gerarAulasDoPeriodo(inicio, fim);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/professores/{professorId}")
    public ResponseEntity<List<ItemAulaProfessorResponse>> listarAulasProfessorPorData(
            @PathVariable Long professorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return ResponseEntity.ok(service.listarAulasProfessorPorData(professorId, data));
    }

    @GetMapping("/professores/{professorId}/semana")
    public ResponseEntity<List<ItemAulaProfessorResponse>> listarAulasProfessorSemana(
            @PathVariable Long professorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio) {
        return ResponseEntity.ok(service.listarAulasProfessorSemana(professorId, inicio));
    }

    @PatchMapping("/turmas/{id}/cancelar")
    public ResponseEntity<AulaTurma> cancelarAulaTurma(@PathVariable Long id, @RequestBody(required = false) CancelarAulaRequest request) {
        String observacao = request == null ? null : request.getObservacao();
        return ResponseEntity.ok(service.cancelarAulaTurma(id, observacao));
    }

    @PatchMapping("/personais/{id}/cancelar")
    public ResponseEntity<AulaPersonal> cancelarAulaPersonal(@PathVariable Long id, @RequestBody(required = false) CancelarAulaRequest request) {
        String observacao = request == null ? null : request.getObservacao();
        return ResponseEntity.ok(service.cancelarAulaPersonal(id, observacao));
    }

    @PatchMapping("/turmas/{id}/remarcar")
    public ResponseEntity<AulaTurma> remarcarAulaTurma(@PathVariable Long id, @Valid @RequestBody RemarcarAulaRequest request) {
        return ResponseEntity.ok(service.remarcarAulaTurma(id, request));
    }

    @PatchMapping("/personais/{id}/remarcar")
    public ResponseEntity<AulaPersonal> remarcarAulaPersonal(@PathVariable Long id, @Valid @RequestBody RemarcarAulaRequest request) {
        return ResponseEntity.ok(service.remarcarAulaPersonal(id, request));
    }
}
