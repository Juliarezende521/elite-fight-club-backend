package Tic.Implementacao.controller;

import Tic.Implementacao.model.Relatorio;
import Tic.Implementacao.service.RelatorioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/Relatorio")
public class RelatorioController {

    private final RelatorioService service;

    public RelatorioController(RelatorioService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Relatorio>> listarTodos() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/tipo/{tipoRelatorio}")
    public ResponseEntity<List<Relatorio>> listarPorTipo(@PathVariable String tipoRelatorio) {
        return ResponseEntity.ok(service.listarPorTipo(tipoRelatorio));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Optional<Relatorio>> buscarPorId(@PathVariable Long id) {

        Optional<Relatorio> relatorio = service.buscarPorId(id);

        if (relatorio.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(relatorio);
    }

    @PostMapping
    public ResponseEntity<Relatorio> salvar(@RequestBody Relatorio relatorio) {

        Relatorio novo = service.salvar(relatorio);

        return ResponseEntity.ok(novo);
    }

    @PostMapping("/financeiro")
    public ResponseEntity<Relatorio> gerarRelatorioFinanceiroConsolidado() {
        return ResponseEntity.ok(service.gerarRelatorioFinanceiro("CONSOLIDADO"));
    }

    @PostMapping("/financeiro/tipo/{tipoModalidade}")
    public ResponseEntity<Relatorio> gerarRelatorioFinanceiroPorTipo(@PathVariable String tipoModalidade) {
        return ResponseEntity.ok(service.gerarRelatorioFinanceiro(tipoModalidade));
    }

    @PostMapping("/evolucao/aluno/{alunoId}")
    public ResponseEntity<Relatorio> gerarRelatorioEvolucaoAluno(@PathVariable Long alunoId) {
        return ResponseEntity.ok(service.gerarRelatorioEvolucaoAluno(alunoId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {

        if (service.remover(id)) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Relatorio> atualizar(
            @PathVariable Long id,
            @RequestBody Relatorio dadosNovos) {

        try {

            Relatorio atualizado = service.atualizar(id, dadosNovos);

            return ResponseEntity.ok(atualizado);

        } catch (RuntimeException e) {

            return ResponseEntity.notFound().build();

        }
    }
}
