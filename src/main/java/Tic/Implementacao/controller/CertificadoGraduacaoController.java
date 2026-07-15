package Tic.Implementacao.controller;

import Tic.Implementacao.dto.graduacao.certificado.*;
import Tic.Implementacao.service.CertificadoGraduacaoService;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;

@RestController
public class CertificadoGraduacaoController {

    private final CertificadoGraduacaoService service;

    public CertificadoGraduacaoController(CertificadoGraduacaoService service) {
        this.service = service;
    }

    @GetMapping("/api/certificados/{id}")
    public ResponseEntity<CertificadoGraduacaoResponse> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarCertificado(id));
    }

    @GetMapping("/api/certificados/validar/{codigo}")
    public ResponseEntity<CertificadoValidacaoResponse> validar(@PathVariable String codigo) {
        return ResponseEntity.ok(service.validarPublicamente(codigo));
    }

    @PatchMapping("/api/certificados/{id}/cancelar")
    public ResponseEntity<CertificadoGraduacaoResponse> cancelar(@PathVariable Long id,
                                                                 @Valid @RequestBody CancelarCertificadoRequest request) {
        return ResponseEntity.ok(service.cancelarCertificado(id, request.getMotivo()));
    }

    @GetMapping(value = "/api/certificados.csv", produces = "text/csv;charset=UTF-8")
    public ResponseEntity<byte[]> exportarCertificados() {
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename("certificados-graduacao.csv", StandardCharsets.UTF_8)
                                .build()
                                .toString())
                .body(service.exportarCertificadosCsv());
    }

    @GetMapping("/api/graduacoes/dashboard-professor")
    public ResponseEntity<DashboardProfessorGraduacaoResponse> dashboardProfessor() {
        return ResponseEntity.ok(service.dashboardProfessor());
    }
}
