package Tic.Implementacao.controller;

import Tic.Implementacao.dto.graduacao.convite.ConviteGraduacaoRequest;
import Tic.Implementacao.dto.graduacao.convite.ConviteGraduacaoResponse;
import Tic.Implementacao.dto.graduacao.avaliacao.*;
import Tic.Implementacao.dto.graduacao.evento.*;
import Tic.Implementacao.dto.graduacao.inscricao.InscricaoGraduacaoResponse;
import Tic.Implementacao.service.AvaliacaoGraduacaoService;
import Tic.Implementacao.service.EventoGraduacaoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/eventos-graduacao")
public class EventoGraduacaoController {

    private final EventoGraduacaoService service;
    private final AvaliacaoGraduacaoService avaliacaoService;

    public EventoGraduacaoController(EventoGraduacaoService service, AvaliacaoGraduacaoService avaliacaoService) {
        this.service = service;
        this.avaliacaoService = avaliacaoService;
    }

    @PostMapping
    public ResponseEntity<EventoGraduacaoResponse> criar(@Valid @RequestBody EventoGraduacaoRequest request) {
        return ResponseEntity.ok(service.criarEvento(request));
    }

    @GetMapping
    public ResponseEntity<List<EventoGraduacaoResponse>> listar() {
        return ResponseEntity.ok(service.listarEventos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventoGraduacaoResponse> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarEvento(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventoGraduacaoResponse> atualizar(@PathVariable Long id,
                                                              @Valid @RequestBody EventoGraduacaoRequest request) {
        return ResponseEntity.ok(service.atualizarEvento(id, request));
    }

    @PostMapping("/{id}/publicar")
    public ResponseEntity<EventoGraduacaoResponse> publicar(@PathVariable Long id,
                                                             @RequestBody(required = false) PublicarEventoGraduacaoRequest request) {
        return ResponseEntity.ok(service.publicarEvento(id, request == null ? new PublicarEventoGraduacaoRequest() : request));
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<EventoGraduacaoResponse> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(service.cancelarEvento(id));
    }

    @PostMapping("/{id}/prajieds")
    public ResponseEntity<EventoGraduacaoPrajiedResponse> adicionarPrajied(@PathVariable Long id,
                                                                            @Valid @RequestBody EventoGraduacaoPrajiedRequest request) {
        return ResponseEntity.ok(service.adicionarPrajied(id, request));
    }

    @PutMapping("/{id}/prajieds/{itemId}")
    public ResponseEntity<EventoGraduacaoPrajiedResponse> atualizarPrajied(@PathVariable Long id,
                                                                            @PathVariable Long itemId,
                                                                            @Valid @RequestBody EventoGraduacaoPrajiedRequest request) {
        return ResponseEntity.ok(service.atualizarPrajied(id, itemId, request));
    }

    @DeleteMapping("/{id}/prajieds/{itemId}")
    public ResponseEntity<Void> removerPrajied(@PathVariable Long id, @PathVariable Long itemId) {
        service.removerPrajied(id, itemId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/convites")
    public ResponseEntity<ConviteGraduacaoResponse> convidar(@PathVariable Long id,
                                                              @Valid @RequestBody ConviteGraduacaoRequest request) {
        return ResponseEntity.ok(service.criarConviteManual(id, request));
    }

    @GetMapping("/{id}/convites")
    public ResponseEntity<List<ConviteGraduacaoResponse>> listarConvites(@PathVariable Long id) {
        return ResponseEntity.ok(service.listarConvitesEvento(id));
    }

    @GetMapping("/{id}/inscricoes")
    public ResponseEntity<List<InscricaoGraduacaoResponse>> listarInscricoes(@PathVariable Long id) {
        return ResponseEntity.ok(service.listarInscricoesEvento(id));
    }

    @PutMapping("/{eventoId}/inscricoes/{inscricaoId}/presenca")
    public ResponseEntity<AvaliacaoGraduacaoResponse> registrarPresenca(@PathVariable Long eventoId,
                                                                         @PathVariable Long inscricaoId,
                                                                         @Valid @RequestBody RegistrarPresencaGraduacaoRequest request) {
        return ResponseEntity.ok(avaliacaoService.registrarPresenca(eventoId, inscricaoId, request));
    }

    @PutMapping("/{eventoId}/inscricoes/{inscricaoId}/avaliacao")
    public ResponseEntity<AvaliacaoGraduacaoResponse> registrarAvaliacao(@PathVariable Long eventoId,
                                                                          @PathVariable Long inscricaoId,
                                                                          @Valid @RequestBody RegistrarAvaliacaoGraduacaoRequest request) {
        return ResponseEntity.ok(avaliacaoService.registrarAvaliacao(eventoId, inscricaoId, request));
    }

    @PostMapping("/{eventoId}/inscricoes/{inscricaoId}/promover")
    public ResponseEntity<AvaliacaoGraduacaoResponse> promover(@PathVariable Long eventoId,
                                                                @PathVariable Long inscricaoId,
                                                                @Valid @RequestBody(required = false) PromoverAlunoEventoRequest request) {
        return ResponseEntity.ok(avaliacaoService.promoverAluno(eventoId, inscricaoId,
                request == null ? new PromoverAlunoEventoRequest() : request));
    }

    @GetMapping("/{eventoId}/painel")
    public ResponseEntity<PainelEventoGraduacaoResponse> painel(@PathVariable Long eventoId) {
        return ResponseEntity.ok(avaliacaoService.painel(eventoId));
    }

    @PostMapping("/{eventoId}/concluir")
    public ResponseEntity<PainelEventoGraduacaoResponse> concluir(@PathVariable Long eventoId,
                                                                   @RequestBody(required = false) ConcluirEventoGraduacaoRequest request) {
        return ResponseEntity.ok(avaliacaoService.concluirEvento(eventoId,
                request == null ? new ConcluirEventoGraduacaoRequest() : request));
    }
}
