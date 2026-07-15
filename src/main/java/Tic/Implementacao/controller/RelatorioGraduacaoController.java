package Tic.Implementacao.controller;

import Tic.Implementacao.dto.graduacao.relatorio.*;
import Tic.Implementacao.model.ResultadoAvaliacaoGraduacao;
import Tic.Implementacao.model.StatusEventoGraduacao;
import Tic.Implementacao.model.StatusInscricaoGraduacao;
import Tic.Implementacao.model.StatusPagamento;
import Tic.Implementacao.model.StatusPresencaGraduacao;
import Tic.Implementacao.service.RelatorioGraduacaoService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@RestController
public class RelatorioGraduacaoController {

    private final RelatorioGraduacaoService service;

    public RelatorioGraduacaoController(RelatorioGraduacaoService service) {
        this.service = service;
    }

    @GetMapping("/api/eventos-graduacao/{eventoId}/relatorio")
    public ResponseEntity<RelatorioEventoGraduacaoResponse> relatorioEvento(@PathVariable Long eventoId) {
        return ResponseEntity.ok(service.gerarRelatorioEvento(eventoId));
    }

    @GetMapping("/api/eventos-graduacao/{eventoId}/relatorio/participantes")
    public ResponseEntity<List<RelatorioParticipanteGraduacaoResponse>> participantes(
            @PathVariable Long eventoId,
            @RequestParam(required = false) Long graduacaoDestinoId,
            @RequestParam(required = false) StatusInscricaoGraduacao statusInscricao,
            @RequestParam(required = false) StatusPagamento statusPagamento,
            @RequestParam(required = false) StatusPresencaGraduacao statusPresenca,
            @RequestParam(required = false) ResultadoAvaliacaoGraduacao resultadoAvaliacao,
            @RequestParam(required = false) Boolean promovido) {
        return ResponseEntity.ok(service.listarParticipantes(eventoId,
                new FiltroRelatorioParticipanteGraduacao(graduacaoDestinoId, statusInscricao, statusPagamento,
                        statusPresenca, resultadoAvaliacao, promovido)));
    }

    @GetMapping(value = "/api/eventos-graduacao/{eventoId}/relatorio/participantes.csv", produces = "text/csv;charset=UTF-8")
    public ResponseEntity<byte[]> participantesCsv(
            @PathVariable Long eventoId,
            @RequestParam(required = false) Long graduacaoDestinoId,
            @RequestParam(required = false) StatusInscricaoGraduacao statusInscricao,
            @RequestParam(required = false) StatusPagamento statusPagamento,
            @RequestParam(required = false) StatusPresencaGraduacao statusPresenca,
            @RequestParam(required = false) ResultadoAvaliacaoGraduacao resultadoAvaliacao,
            @RequestParam(required = false) Boolean promovido) {
        byte[] csv = service.gerarCsvParticipantes(eventoId,
                new FiltroRelatorioParticipanteGraduacao(graduacaoDestinoId, statusInscricao, statusPagamento,
                        statusPresenca, resultadoAvaliacao, promovido));
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(service.nomeArquivoCsv(eventoId), StandardCharsets.UTF_8)
                                .build()
                                .toString())
                .body(csv);
    }

    @GetMapping("/api/relatorios/graduacoes")
    public ResponseEntity<RelatorioGraduacoesPeriodoResponse> relatorioPeriodo(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(required = false) Long eventoId,
            @RequestParam(required = false) Long graduacaoId,
            @RequestParam(required = false) StatusEventoGraduacao statusEvento) {
        return ResponseEntity.ok(service.gerarRelatorioPeriodo(inicio, fim, eventoId, graduacaoId, statusEvento));
    }

    @GetMapping("/api/relatorios/graduacoes/financeiro")
    public ResponseEntity<RelatorioFinanceiroGraduacaoResponse> relatorioFinanceiro(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(required = false) Long eventoId,
            @RequestParam(required = false) Long graduacaoId) {
        return ResponseEntity.ok(service.gerarRelatorioFinanceiro(inicio, fim, eventoId, graduacaoId));
    }
}
