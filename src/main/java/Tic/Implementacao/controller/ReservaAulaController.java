package Tic.Implementacao.controller;

import Tic.Implementacao.dto.reserva.OcupacaoAulaResponse;
import Tic.Implementacao.dto.reserva.ReservaAulaResponse;
import Tic.Implementacao.model.ReservaAula;
import Tic.Implementacao.service.ReservaAulaService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
public class ReservaAulaController {

    private final ReservaAulaService service;

    public ReservaAulaController(ReservaAulaService service) {
        this.service = service;
    }

    @PostMapping("/api/aulas/turmas/{aulaId}/reservas")
    public ResponseEntity<ReservaAula> reservarMinhaVaga(@PathVariable Long aulaId) {
        return ResponseEntity.ok(service.reservarMinhaVaga(aulaId));
    }

    @DeleteMapping("/api/aulas/turmas/{aulaId}/reservas/minha")
    public ResponseEntity<ReservaAula> cancelarMinhaReserva(@PathVariable Long aulaId) {
        return ResponseEntity.ok(service.cancelarMinhaReserva(aulaId));
    }

    @GetMapping("/api/aulas/turmas/{aulaId}/reservas")
    public ResponseEntity<List<ReservaAulaResponse>> listarReservasDaAula(@PathVariable Long aulaId) {
        return ResponseEntity.ok(service.listarReservasDaAula(aulaId));
    }

    @GetMapping("/api/alunos/me/reservas")
    public ResponseEntity<List<ReservaAulaResponse>> listarMinhasReservas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return ResponseEntity.ok(service.listarMinhasReservas(inicio, fim));
    }

    @GetMapping("/api/aulas/turmas/{aulaId}/ocupacao")
    public ResponseEntity<OcupacaoAulaResponse> consultarOcupacao(@PathVariable Long aulaId) {
        return ResponseEntity.ok(service.consultarOcupacao(aulaId));
    }
}
