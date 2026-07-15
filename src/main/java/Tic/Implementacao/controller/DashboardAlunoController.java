package Tic.Implementacao.controller;

import Tic.Implementacao.dto.dashboard.aluno.DashboardAlunoResponse;
import Tic.Implementacao.service.DashboardAlunoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class DashboardAlunoController {

    private final DashboardAlunoService service;

    public DashboardAlunoController(DashboardAlunoService service) {
        this.service = service;
    }

    @GetMapping("/api/alunos/me/dashboard")
    public ResponseEntity<DashboardAlunoResponse> buscarMeuDashboard() {
        return ResponseEntity.ok(service.buscarDashboardDoAlunoAtual());
    }

    @GetMapping("/api/alunos/{alunoId}/dashboard")
    public ResponseEntity<DashboardAlunoResponse> buscarDashboardAluno(@PathVariable Long alunoId) {
        return ResponseEntity.ok(service.buscarDashboardDoAluno(alunoId));
    }
}
