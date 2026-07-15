package Tic.Implementacao.controller;
import Tic.Implementacao.dto.notificacao.NotificacaoRelatorioResponse;
import Tic.Implementacao.model.Notificacao;
import Tic.Implementacao.model.StatusNotificacao;
import Tic.Implementacao.model.TipoNotificacao;
import Tic.Implementacao.service.NotificacaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping({"/notificacoes", "/api/notificacoes"})
public class NotificacaoController {

    private NotificacaoService service;

    public NotificacaoController(NotificacaoService service){
        this.service = service;

    }

    @GetMapping
    public ResponseEntity<List<Notificacao>> listarTodos() {
        return ResponseEntity.ok(service.listarTodos());
    }


    @GetMapping("/{id}")
    public ResponseEntity<Optional<Notificacao>> buscarPorId(@PathVariable Long id) {

        Optional<Notificacao> notificacao = service.buscarPorId(id);

        if (notificacao.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(notificacao);
    }

    @GetMapping("/aluno/{alunoId}")
    public ResponseEntity<List<Notificacao>> listarPorAluno(@PathVariable Long alunoId) {
        return ResponseEntity.ok(service.listarPorAluno(alunoId));
    }

    @GetMapping("/pendentes")
    public ResponseEntity<List<Notificacao>> listarPendentes() {
        return ResponseEntity.ok(service.listarPendentes());
    }


    @PostMapping
    public ResponseEntity<Notificacao> salva(@RequestBody Notificacao notificacao){
        Notificacao nova = service.salva(notificacao);
        return ResponseEntity.ok(nova);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remove(@PathVariable Long id){
        if(service.remove(id)){
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Notificacao> atualizaInformacoes(@PathVariable Long id, @RequestBody Notificacao alterado){
        Notificacao resposta = service.atualizarInformacoes(id, alterado);
        if(resposta != null){
            return ResponseEntity.ok(resposta);
        }
        return ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}/enviar")
    public ResponseEntity<Notificacao> marcarComoEnviada(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.marcarComoEnviada(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/gerar-automaticas")
    public ResponseEntity<Void> gerarNotificacoesAutomaticas() {
        service.gerarNotificacoesAutomaticas();
        return ResponseEntity.noContent().build();
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
