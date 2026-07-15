package Tic.Implementacao.controller;

import Tic.Implementacao.model.Financeiro;
import Tic.Implementacao.service.FinanceiroService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/financeiros")

public class FinanceiroController {


    private FinanceiroService service;

    public FinanceiroController(FinanceiroService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Financeiro>> listarTodos() {
        return ResponseEntity.ok(service.listarTodos());
    }


    @GetMapping("/{id}")
    public ResponseEntity<Optional<Financeiro>> buscarPorId(@PathVariable Long id) {

        Optional<Financeiro> financeiro = service.buscarPorId(id);

        if (financeiro.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(financeiro);
    }

    @GetMapping("/aluno/{alunoId}")
    public ResponseEntity<List<Financeiro>> listarPorAluno(@PathVariable Long alunoId) {
        return ResponseEntity.ok(service.listarPorAluno(alunoId));
    }

    @GetMapping("/modalidade/{modalidadeId}")
    public ResponseEntity<List<Financeiro>> listarPorModalidade(@PathVariable Long modalidadeId) {
        return ResponseEntity.ok(service.listarPorModalidade(modalidadeId));
    }

    @GetMapping("/tipo/{tipoModalidade}")
    public ResponseEntity<List<Financeiro>> listarPorTipoModalidade(@PathVariable String tipoModalidade) {
        return ResponseEntity.ok(service.listarPorTipoModalidade(tipoModalidade));
    }

    @GetMapping("/pagos")
    public ResponseEntity<List<Financeiro>> listarPagos() {
        return ResponseEntity.ok(service.listarPagos());
    }

    @GetMapping("/vencidos")
    public ResponseEntity<List<Financeiro>> listarVencidos() {
        return ResponseEntity.ok(service.listarVencidos());
    }

    @GetMapping("/vence-hoje")
    public ResponseEntity<List<Financeiro>> listarVencendoHoje() {
        return ResponseEntity.ok(service.listarVencendoHoje());
    }

    @GetMapping("/total-recebido")
    public ResponseEntity<Double> calcularTotalRecebido() {
        return ResponseEntity.ok(service.calcularTotalRecebido());
    }

    @GetMapping("/total-recebido/tipo/{tipoModalidade}")
    public ResponseEntity<Double> calcularTotalRecebidoPorTipo(@PathVariable String tipoModalidade) {
        return ResponseEntity.ok(service.calcularTotalRecebidoPorTipo(tipoModalidade));
    }


    @PostMapping
    public ResponseEntity<Financeiro> salva(@RequestBody Financeiro financeiro){
        Financeiro novo = service.salva(financeiro);
        return ResponseEntity.ok(novo);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remove(@PathVariable Long id){
        if(service.remove(id)){
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Financeiro> atualizaInformacoes(@PathVariable Long id, @RequestBody Financeiro alterado){
        Financeiro resposta = service.atualizarInformacoes(id, alterado);
        if(resposta != null){
            return ResponseEntity.ok(resposta);
        }
        return ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}/pagar")
    public ResponseEntity<Financeiro> registrarPagamento(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.registrarPagamento(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
