package Tic.Implementacao.controller;

import Tic.Implementacao.dto.relatorio.financeiro.*;
import Tic.Implementacao.dto.relatorio.financeiro.anual.RelatorioFinanceiroAnualResponse;
import Tic.Implementacao.dto.relatorio.financeiro.anual.ResumoFinanceiroAnualResponse;
import Tic.Implementacao.model.MetodoPagamento;
import Tic.Implementacao.model.StatusFatura;
import Tic.Implementacao.model.TipoPlano;
import Tic.Implementacao.service.RelatorioFinanceiroService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
public class RelatorioFinanceiroController {

    private final RelatorioFinanceiroService service;

    public RelatorioFinanceiroController(RelatorioFinanceiroService service) {
        this.service = service;
    }

    @GetMapping("/api/relatorios/financeiro/mensal")
    public RelatorioFinanceiroMensalResponse mensal(@RequestParam Integer ano,
                                                    @RequestParam Integer mes,
                                                    @RequestParam(required = false) StatusFatura statusFatura,
                                                    @RequestParam(required = false) TipoPlano tipoPlano,
                                                    @RequestParam(required = false) MetodoPagamento metodoPagamento,
                                                    @RequestParam(required = false) Long alunoId,
                                                    @RequestParam(required = false) Boolean somenteInadimplentes) {
        return service.gerarMensal(ano, mes, statusFatura, tipoPlano, metodoPagamento, alunoId, somenteInadimplentes);
    }

    @GetMapping("/api/relatorios/financeiro/mensal/resumo")
    public ResumoFinanceiroMensalResponse resumoMensal(@RequestParam Integer ano, @RequestParam Integer mes) {
        return service.gerarResumoMensal(ano, mes);
    }

    @GetMapping("/api/relatorios/financeiro/anual")
    public RelatorioFinanceiroAnualResponse anual(@RequestParam Integer ano,
                                                  @RequestParam(required = false) TipoPlano tipoPlano,
                                                  @RequestParam(required = false) MetodoPagamento metodoPagamento,
                                                  @RequestParam(required = false) Long alunoId,
                                                  @RequestParam(required = false) Integer limiteInadimplentes) {
        return service.gerarAnual(ano, tipoPlano, metodoPagamento, alunoId, limiteInadimplentes);
    }

    @GetMapping("/api/relatorios/financeiro/anual/resumo")
    public ResumoFinanceiroAnualResponse resumoAnual(@RequestParam Integer ano) {
        return service.gerarResumoAnual(ano);
    }

    @GetMapping("/api/relatorios/financeiro/inadimplencia")
    public List<AlunoInadimplenteResponse> inadimplencia(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataReferencia) {
        return service.listarInadimplencia(dataReferencia);
    }

    @GetMapping("/api/relatorios/financeiro/mensal.csv")
    public ResponseEntity<byte[]> mensalCsv(@RequestParam Integer ano,
                                            @RequestParam Integer mes,
                                            @RequestParam(required = false) StatusFatura statusFatura,
                                            @RequestParam(required = false) TipoPlano tipoPlano,
                                            @RequestParam(required = false) MetodoPagamento metodoPagamento,
                                            @RequestParam(required = false) Long alunoId,
                                            @RequestParam(required = false) Boolean somenteInadimplentes) {
        byte[] csv = service.gerarCsvMensal(ano, mes, statusFatura, tipoPlano, metodoPagamento, alunoId, somenteInadimplentes);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + service.nomeArquivoMensal(ano, mes) + "\"")
                .contentType(new MediaType("text", "csv", java.nio.charset.StandardCharsets.UTF_8))
                .body(csv);
    }

    @GetMapping("/api/relatorios/financeiro/anual.csv")
    public ResponseEntity<byte[]> anualCsv(@RequestParam Integer ano,
                                           @RequestParam(required = false) TipoPlano tipoPlano,
                                           @RequestParam(required = false) MetodoPagamento metodoPagamento,
                                           @RequestParam(required = false) Long alunoId,
                                           @RequestParam(required = false) Integer limiteInadimplentes) {
        byte[] csv = service.gerarCsvAnual(ano, tipoPlano, metodoPagamento, alunoId, limiteInadimplentes);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + service.nomeArquivoAnual(ano) + "\"")
                .contentType(new MediaType("text", "csv", java.nio.charset.StandardCharsets.UTF_8))
                .body(csv);
    }
}
