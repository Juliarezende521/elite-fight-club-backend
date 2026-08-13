package Tic.Implementacao.controller;

import Tic.Implementacao.dto.notificacao.NotificacaoRelatorioResponse;
import Tic.Implementacao.exception.ApiErrorResponse;
import Tic.Implementacao.model.StatusNotificacao;
import Tic.Implementacao.model.TipoNotificacao;
import Tic.Implementacao.security.PermissaoService;
import Tic.Implementacao.service.NotificacaoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notificacoes")
public class NotificacaoController {

    private final NotificacaoService service;
    private final PermissaoService permissaoService;

    public NotificacaoController(NotificacaoService service, PermissaoService permissaoService){
        this.service = service;
        this.permissaoService = permissaoService;
    }

    @RequestMapping(value = {"", "/{id}", "/aluno/{alunoId}", "/pendentes", "/{id}/enviar", "/gerar-automaticas"}, method = {
            RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE
    })
    public ResponseEntity<ApiErrorResponse> endpointLegadoDesativado() {
        permissaoService.exigirAdmin();
        return ResponseEntity.status(HttpStatus.GONE).body(new ApiErrorResponse(
                410,
                "LEGACY_ENDPOINT_DISABLED",
                "Endpoints legados de notificações foram desativados. Use /api/alunos/me/notificacoes e /api/notificacoes/relatorio.",
                Map.of()
        ));
    }

    @GetMapping("/relatorio")
    public ResponseEntity<List<NotificacaoRelatorioResponse>> relatorio(@RequestParam(required = false) LocalDateTime inicio,
                                                                         @RequestParam(required = false) LocalDateTime fim,
                                                                         @RequestParam(required = false) TipoNotificacao tipo,
                                                                         @RequestParam(required = false) StatusNotificacao status,
                                                                         @RequestParam(required = false) Long alunoId,
                                                                         @RequestParam(required = false) Long eventoId) {
        return ResponseEntity.ok(service.relatorio(inicio, fim, tipo, status, alunoId, eventoId));
    }

    @PostMapping("/graduacao/gerar-avisos-prazo")
    public ResponseEntity<Integer> gerarAvisosPrazoInscricao() {
        return ResponseEntity.ok(service.gerarAvisosPrazoInscricaoProximo());
    }

    @PostMapping("/graduacao/gerar-lembretes-evento")
    public ResponseEntity<Integer> gerarLembretesEvento() {
        return ResponseEntity.ok(service.gerarLembretesEventoGraduacao());
    }
}
