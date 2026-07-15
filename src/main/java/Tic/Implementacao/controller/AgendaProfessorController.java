package Tic.Implementacao.controller;

import Tic.Implementacao.dto.agenda.professor.AgendaSemanalProfessorResponse;
import Tic.Implementacao.dto.agenda.professor.DetalheAulaPersonalProfessorResponse;
import Tic.Implementacao.dto.agenda.professor.DetalheAulaTurmaProfessorResponse;
import Tic.Implementacao.dto.agenda.professor.ResumoAgendaProfessorResponse;
import Tic.Implementacao.service.AgendaProfessorService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/agenda-professor")
public class AgendaProfessorController {

    private final AgendaProfessorService service;

    public AgendaProfessorController(AgendaProfessorService service) {
        this.service = service;
    }

    @GetMapping("/{professorId}/dia")
    public ResponseEntity<ResumoAgendaProfessorResponse> buscarAgendaDiaria(
            @PathVariable Long professorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return ResponseEntity.ok(service.buscarAgendaDiaria(professorId, data));
    }

    @GetMapping("/{professorId}/semana")
    public ResponseEntity<AgendaSemanalProfessorResponse> buscarAgendaSemanal(
            @PathVariable Long professorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio) {
        return ResponseEntity.ok(service.buscarAgendaSemanal(professorId, inicio));
    }

    @GetMapping("/aulas/turmas/{aulaId}")
    public ResponseEntity<DetalheAulaTurmaProfessorResponse> buscarDetalheTurma(@PathVariable Long aulaId) {
        return ResponseEntity.ok(service.buscarDetalheTurma(aulaId));
    }

    @GetMapping("/aulas/personais/{aulaId}")
    public ResponseEntity<DetalheAulaPersonalProfessorResponse> buscarDetalhePersonal(@PathVariable Long aulaId) {
        return ResponseEntity.ok(service.buscarDetalhePersonal(aulaId));
    }
}
