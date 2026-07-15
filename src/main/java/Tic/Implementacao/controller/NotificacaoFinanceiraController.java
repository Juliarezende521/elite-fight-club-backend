package Tic.Implementacao.controller;

import Tic.Implementacao.dto.notificacao.financeiro.NotificacaoFinanceiraRelatorioResponse;
import Tic.Implementacao.dto.notificacao.financeiro.ResultadoAutomacaoNotificacaoFinanceiraResponse;
import Tic.Implementacao.model.StatusNotificacao;
import Tic.Implementacao.model.TipoNotificacao;
import Tic.Implementacao.service.NotificacaoFinanceiraAutomacaoService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/notificacoes/financeiro")
public class NotificacaoFinanceiraController {

    private final NotificacaoFinanceiraAutomacaoService service;

    public NotificacaoFinanceiraController(NotificacaoFinanceiraAutomacaoService service) {
        this.service = service;
    }

    @PostMapping("/processar")
    public ResultadoAutomacaoNotificacaoFinanceiraResponse processar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return service.gerarNotificacoesFinanceiras(data);
    }

    @PostMapping("/processar-vencimentos")
    public ResultadoAutomacaoNotificacaoFinanceiraResponse processarVencimentos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return service.gerarLembretesDeVencimento(data);
    }

    @PostMapping("/processar-atrasos")
    public ResultadoAutomacaoNotificacaoFinanceiraResponse processarAtrasos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return service.gerarAvisosDeAtraso(data);
    }

    @GetMapping("/relatorio")
    public List<NotificacaoFinanceiraRelatorioResponse> relatorio(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim,
            @RequestParam(required = false) TipoNotificacao tipo,
            @RequestParam(required = false) StatusNotificacao status,
            @RequestParam(required = false) Long alunoId,
            @RequestParam(required = false) Long faturaId,
            @RequestParam(required = false) Boolean somenteFalhas) {
        return service.relatorio(inicio, fim, tipo, status, alunoId, faturaId, somenteFalhas);
    }
}
