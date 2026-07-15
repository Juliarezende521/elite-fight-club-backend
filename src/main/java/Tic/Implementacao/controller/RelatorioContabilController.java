package Tic.Implementacao.controller;

import Tic.Implementacao.dto.relatorio.financeiro.contabil.*;
import Tic.Implementacao.model.MetodoPagamento;
import Tic.Implementacao.model.StatusPagamento;
import Tic.Implementacao.model.TipoCobranca;
import Tic.Implementacao.service.RelatorioContabilService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
public class RelatorioContabilController {

    private final RelatorioContabilService service;

    public RelatorioContabilController(RelatorioContabilService service) {
        this.service = service;
    }

    @GetMapping("/api/relatorios/financeiro/contabil")
    public RelatorioContabilResponse contabil(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(required = false) TipoCobranca naturezaReceita,
            @RequestParam(required = false) MetodoPagamento metodoPagamento,
            @RequestParam(required = false) StatusPagamento statusPagamento,
            @RequestParam(required = false) Long alunoId,
            @RequestParam(required = false) Long eventoGraduacaoId,
            @RequestParam(required = false) String competencia) {
        return service.gerar(inicio, fim, naturezaReceita, metodoPagamento, statusPagamento, alunoId, eventoGraduacaoId, competencia);
    }

    @GetMapping("/api/relatorios/financeiro/contabil/alunos")
    public List<ResumoRecebimentosAlunoResponse> alunos(@RequestParam Integer ano,
                                                        @RequestParam(required = false) TipoCobranca naturezaReceita) {
        return service.recebimentosPorAluno(ano, naturezaReceita);
    }

    @GetMapping("/api/relatorios/financeiro/contabil/competencias")
    public List<ResumoCompetenciaContabilResponse> competencias(@RequestParam Integer ano) {
        return service.competencias(ano);
    }

    @GetMapping("/api/relatorios/financeiro/contabil.csv")
    public ResponseEntity<byte[]> contabilCsv(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(required = false) TipoCobranca naturezaReceita,
            @RequestParam(required = false) MetodoPagamento metodoPagamento,
            @RequestParam(required = false) StatusPagamento statusPagamento,
            @RequestParam(required = false) Long alunoId,
            @RequestParam(required = false) Long eventoGraduacaoId,
            @RequestParam(required = false) String competencia) {
        byte[] csv = service.gerarCsv(inicio, fim, naturezaReceita, metodoPagamento, statusPagamento,
                alunoId, eventoGraduacaoId, competencia);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + service.nomeArquivo(inicio, fim) + "\"")
                .contentType(new MediaType("text", "csv", java.nio.charset.StandardCharsets.UTF_8))
                .body(csv);
    }

    @GetMapping("/api/relatorios/financeiro/contabil/alunos.csv")
    public ResponseEntity<byte[]> alunosCsv(@RequestParam Integer ano,
                                            @RequestParam(required = false) TipoCobranca naturezaReceita) {
        byte[] csv = service.gerarCsvAlunos(ano, naturezaReceita);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + service.nomeArquivoAlunos(ano) + "\"")
                .contentType(new MediaType("text", "csv", java.nio.charset.StandardCharsets.UTF_8))
                .body(csv);
    }
}
