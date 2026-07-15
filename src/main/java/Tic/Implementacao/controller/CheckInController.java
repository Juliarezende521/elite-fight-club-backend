package Tic.Implementacao.controller;

import Tic.Implementacao.dto.checkin.RegistrarCheckInRequest;
import Tic.Implementacao.model.CheckIn;
import Tic.Implementacao.service.CheckInService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/checkins")
public class CheckInController {

    private final CheckInService service;

    public CheckInController(CheckInService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<CheckIn>> listarTodos() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/aluno/{alunoId}")
    public ResponseEntity<List<CheckIn>> listarPorAluno(@PathVariable Long alunoId) {
        return ResponseEntity.ok(service.listarPorAluno(alunoId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Optional<CheckIn>> buscarPorId(@PathVariable Long id) {
        Optional<CheckIn> checkIn = service.buscarPorId(id);
        if (checkIn.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(checkIn);
    }

    @GetMapping("/aluno/{alunoId}/dashboard")
    public ResponseEntity<Map<String, Object>> montarDashboardAluno(@PathVariable Long alunoId) {
        try {
            return ResponseEntity.ok(service.montarDashboardAluno(alunoId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<CheckIn> salvar(@RequestBody CheckIn checkIn) {
        return ResponseEntity.ok(service.salvar(checkIn));
    }

    @PostMapping("/aluno/{alunoId}")
    public ResponseEntity<CheckIn> registrarCheckInAluno(@PathVariable Long alunoId) {
        try {
            RegistrarCheckInRequest request = new RegistrarCheckInRequest();
            request.setAlunoId(alunoId);
            return ResponseEntity.ok(service.registrarCheckIn(request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/registrar")
    public ResponseEntity<CheckIn> registrarCheckIn(@Valid @RequestBody RegistrarCheckInRequest request) {
        return ResponseEntity.ok(service.registrarCheckIn(request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CheckIn> atualizar(@PathVariable Long id, @RequestBody CheckIn dadosNovos) {
        try {
            return ResponseEntity.ok(service.atualizar(id, dadosNovos));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        if (service.remover(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
