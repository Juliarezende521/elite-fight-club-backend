package Tic.Implementacao.controller;

import Tic.Implementacao.dto.checkinqr.*;
import Tic.Implementacao.service.CheckInQrService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class CheckInQrController {

    private final CheckInQrService service;

    public CheckInQrController(CheckInQrService service) {
        this.service = service;
    }

    @GetMapping("/api/academias/{academiaId}/check-in-qr")
    public ResponseEntity<QrAcademiaResponse> consultarQrAcademia(@PathVariable Long academiaId) {
        return ResponseEntity.ok(service.consultarQrAcademia(academiaId));
    }

    @PostMapping("/api/academias/{academiaId}/check-in-qr/regenerar")
    public ResponseEntity<QrAcademiaResponse> regenerarQrAcademia(@PathVariable Long academiaId) {
        return ResponseEntity.ok(service.regenerarQrAcademia(academiaId));
    }

    @PostMapping("/api/aulas/turmas/{aulaId}/sessao-check-in")
    public ResponseEntity<SessaoCheckInResponse> abrirSessaoTurma(@PathVariable Long aulaId) {
        return ResponseEntity.ok(service.abrirSessaoTurma(aulaId));
    }

    @PostMapping("/api/aulas/personais/{aulaId}/sessao-check-in")
    public ResponseEntity<SessaoCheckInResponse> abrirSessaoPersonal(@PathVariable Long aulaId) {
        return ResponseEntity.ok(service.abrirSessaoPersonal(aulaId));
    }

    @PatchMapping("/api/sessoes-check-in/{sessaoId}/encerrar")
    public ResponseEntity<SessaoCheckInResponse> encerrarSessao(@PathVariable Long sessaoId) {
        return ResponseEntity.ok(service.encerrarSessao(sessaoId));
    }

    @GetMapping("/api/aulas/turmas/{aulaId}/sessao-check-in")
    public ResponseEntity<SessaoCheckInResponse> consultarSessaoTurma(@PathVariable Long aulaId) {
        return ResponseEntity.ok(service.consultarSessaoTurma(aulaId));
    }

    @GetMapping("/api/aulas/personais/{aulaId}/sessao-check-in")
    public ResponseEntity<SessaoCheckInResponse> consultarSessaoPersonal(@PathVariable Long aulaId) {
        return ResponseEntity.ok(service.consultarSessaoPersonal(aulaId));
    }

    @PostMapping("/api/check-in/qr/{codigoPublico}/opcoes")
    public ResponseEntity<OpcoesCheckInQrResponse> listarOpcoes(@PathVariable String codigoPublico) {
        return ResponseEntity.ok(service.listarOpcoes(codigoPublico));
    }

    @PostMapping("/api/check-in/qr/{codigoPublico}/confirmar")
    public ResponseEntity<ConfirmarCheckInQrResponse> confirmar(
            @PathVariable String codigoPublico,
            @Valid @RequestBody ConfirmarCheckInQrRequest request) {
        return ResponseEntity.ok(service.confirmar(codigoPublico, request));
    }
}
