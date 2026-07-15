package Tic.Implementacao.controller;

import Tic.Implementacao.dto.agenda.AgendaPersonalRequest;
import Tic.Implementacao.dto.agenda.HorarioTurmaRequest;
import Tic.Implementacao.dto.agenda.ItemAgendaProfessorResponse;
import Tic.Implementacao.model.AgendaPersonal;
import Tic.Implementacao.model.HorarioTurma;
import Tic.Implementacao.service.AgendaService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/agenda")
public class AgendaController {

    private final AgendaService service;

    public AgendaController(AgendaService service) {
        this.service = service;
    }

    @PostMapping("/turmas")
    public ResponseEntity<HorarioTurma> cadastrarHorarioTurma(@Valid @RequestBody HorarioTurmaRequest request) {
        return ResponseEntity.ok(service.cadastrarHorarioTurma(request));
    }

    @PutMapping("/turmas/{id}")
    public ResponseEntity<HorarioTurma> atualizarHorarioTurma(@PathVariable Long id, @Valid @RequestBody HorarioTurmaRequest request) {
        return ResponseEntity.ok(service.atualizarHorarioTurma(id, request));
    }

    @PatchMapping("/turmas/{id}/status")
    public ResponseEntity<HorarioTurma> alterarStatusHorarioTurma(@PathVariable Long id, @RequestParam boolean ativo) {
        return ResponseEntity.ok(service.alterarStatusHorarioTurma(id, ativo));
    }

    @PostMapping("/personais")
    public ResponseEntity<AgendaPersonal> cadastrarAgendaPersonal(@Valid @RequestBody AgendaPersonalRequest request) {
        return ResponseEntity.ok(service.cadastrarAgendaPersonal(request));
    }

    @PutMapping("/personais/{id}")
    public ResponseEntity<AgendaPersonal> atualizarAgendaPersonal(@PathVariable Long id, @Valid @RequestBody AgendaPersonalRequest request) {
        return ResponseEntity.ok(service.atualizarAgendaPersonal(id, request));
    }

    @PatchMapping("/personais/{id}/status")
    public ResponseEntity<AgendaPersonal> alterarStatusAgendaPersonal(@PathVariable Long id, @RequestParam boolean ativo) {
        return ResponseEntity.ok(service.alterarStatusAgendaPersonal(id, ativo));
    }

    @GetMapping("/professores/{professorId}")
    public ResponseEntity<List<ItemAgendaProfessorResponse>> listarAgendaProfessor(
            @PathVariable Long professorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return ResponseEntity.ok(service.listarAgendaProfessorPorData(professorId, data));
    }
}
