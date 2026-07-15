package Tic.Implementacao.controller;

import Tic.Implementacao.model.Fatura;
import Tic.Implementacao.service.FaturaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/faturas")
public class FaturaController {

    private final FaturaService service;

    public FaturaController(FaturaService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Fatura>> listarTodas() {
        return ResponseEntity.ok(service.listarTodas());
    }

    @GetMapping("/aluno/{alunoId}")
    public ResponseEntity<List<Fatura>> listarPorAluno(@PathVariable Long alunoId) {
        return ResponseEntity.ok(service.listarPorAluno(alunoId));
    }

    @PostMapping("/aluno/{alunoId}/mensal/{mesReferencia}")
    public ResponseEntity<Fatura> gerarFaturaMensal(@PathVariable Long alunoId, @PathVariable String mesReferencia) {
        return ResponseEntity.ok(service.gerarFaturaMensal(alunoId, YearMonth.parse(mesReferencia)));
    }

    @PostMapping("/mensais/{mesReferencia}")
    public ResponseEntity<Void> gerarFaturasMensais(@PathVariable String mesReferencia) {
        service.gerarFaturasMensais(YearMonth.parse(mesReferencia));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<Fatura> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(service.cancelar(id));
    }
}
