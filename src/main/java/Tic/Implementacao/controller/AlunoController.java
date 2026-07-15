package Tic.Implementacao.controller;


import Tic.Implementacao.dto.aluno.CriarAlunoRequest;
import Tic.Implementacao.dto.graduacao.AlterarGraduacaoAlunoRequest;
import Tic.Implementacao.dto.graduacao.HistoricoGraduacaoResponse;
import Tic.Implementacao.dto.graduacao.PromoverGraduacaoAlunoRequest;
import Tic.Implementacao.dto.graduacao.convite.ConviteGraduacaoResponse;
import Tic.Implementacao.dto.graduacao.evento.EventoGraduacaoAlunoResponse;
import Tic.Implementacao.dto.graduacao.inscricao.InscricaoGraduacaoResponse;
import Tic.Implementacao.dto.graduacao.inscricao.CriarPixInscricaoGraduacaoResponse;
import Tic.Implementacao.model.Aluno;
import Tic.Implementacao.service.AlunoService;
import Tic.Implementacao.service.EventoGraduacaoService;
import Tic.Implementacao.service.GraduacaoAlunoService;
import Tic.Implementacao.service.PagamentoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/alunos")
public class AlunoController {

    private AlunoService service;
    private final GraduacaoAlunoService graduacaoAlunoService;
    private final EventoGraduacaoService eventoGraduacaoService;
    private final PagamentoService pagamentoService;

    public AlunoController(AlunoService service, GraduacaoAlunoService graduacaoAlunoService,
                           EventoGraduacaoService eventoGraduacaoService,
                           PagamentoService pagamentoService){
        this.service = service;
        this.graduacaoAlunoService = graduacaoAlunoService;
        this.eventoGraduacaoService = eventoGraduacaoService;
        this.pagamentoService = pagamentoService;
    }

    //GET
    @GetMapping
    public ResponseEntity<List<Aluno>> listar(){
        return ResponseEntity.ok(service.listar());
    }

    //GET ID

    @GetMapping("/{id}")
    public ResponseEntity<Optional<Aluno>> exibirIdentificacao(@PathVariable Long id){
        Optional<Aluno> alu = service.exibirIdentificacao(id);
        if(alu.isEmpty()){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(alu);
    }

    @GetMapping("/modalidade/{modalidadeId}")
    public ResponseEntity<List<Aluno>> listarPorModalidade(@PathVariable Long modalidadeId) {
        return ResponseEntity.ok(service.listarPorModalidade(modalidadeId));
    }

    @GetMapping("/tipo-modalidade/{tipoModalidade}")
    public ResponseEntity<List<Aluno>> listarPorTipoModalidade(@PathVariable String tipoModalidade) {
        return ResponseEntity.ok(service.listarPorTipoModalidade(tipoModalidade));
    }

    @PostMapping
    public ResponseEntity<Aluno> criarAluno(@Valid @RequestBody CriarAlunoRequest request){
        return ResponseEntity.ok(service.criarAluno(request));
    }

    @PostMapping("/legado")
    public ResponseEntity<Aluno> salva(@RequestBody Aluno aluno){
        return ResponseEntity.ok(service.salva(aluno));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remove(@PathVariable Long id){
        if(service.remove(id)){
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Aluno> atualizarDados(@PathVariable Long id, @RequestBody Aluno alterado){
        Aluno resposta = service.atualizarDados(id, alterado);
        if(resposta != null){
            return ResponseEntity.ok(resposta);
        }
        return ResponseEntity.notFound().build();

    }

    @PostMapping("/{alunoId}/graduacao")
    public ResponseEntity<HistoricoGraduacaoResponse> alterarGraduacao(@PathVariable Long alunoId,
                                                                        @Valid @RequestBody AlterarGraduacaoAlunoRequest request) {
        return ResponseEntity.ok(graduacaoAlunoService.alterarGraduacao(alunoId, request));
    }

    @PostMapping("/{alunoId}/graduacao/promover")
    public ResponseEntity<HistoricoGraduacaoResponse> promoverGraduacao(@PathVariable Long alunoId,
                                                                         @Valid @RequestBody PromoverGraduacaoAlunoRequest request) {
        return ResponseEntity.ok(graduacaoAlunoService.promoverParaProximaGraduacao(alunoId, request));
    }

    @GetMapping("/{alunoId}/graduacoes/historico")
    public ResponseEntity<List<HistoricoGraduacaoResponse>> historicoGraduacoes(@PathVariable Long alunoId) {
        return ResponseEntity.ok(graduacaoAlunoService.listarHistoricoAluno(alunoId));
    }

    @GetMapping("/me/graduacoes/historico")
    public ResponseEntity<List<HistoricoGraduacaoResponse>> meuHistoricoGraduacoes() {
        return ResponseEntity.ok(graduacaoAlunoService.listarHistoricoAlunoAtual());
    }

    @GetMapping("/me/eventos-graduacao")
    public ResponseEntity<List<EventoGraduacaoAlunoResponse>> meusEventosGraduacao() {
        return ResponseEntity.ok(eventoGraduacaoService.listarEventosDoAlunoAtual());
    }

    @GetMapping("/me/eventos-graduacao/{eventoId}")
    public ResponseEntity<EventoGraduacaoAlunoResponse> meuEventoGraduacao(@PathVariable Long eventoId) {
        return ResponseEntity.ok(eventoGraduacaoService.buscarEventoDoAlunoAtual(eventoId));
    }

    @GetMapping("/me/convites-graduacao")
    public ResponseEntity<List<ConviteGraduacaoResponse>> meusConvitesGraduacao() {
        return ResponseEntity.ok(eventoGraduacaoService.listarConvitesDoAlunoAtual());
    }

    @PostMapping("/me/convites-graduacao/{conviteId}/aceitar")
    public ResponseEntity<ConviteGraduacaoResponse> aceitarConviteGraduacao(@PathVariable Long conviteId) {
        return ResponseEntity.ok(eventoGraduacaoService.aceitarConvite(conviteId));
    }

    @PostMapping("/me/convites-graduacao/{conviteId}/recusar")
    public ResponseEntity<ConviteGraduacaoResponse> recusarConviteGraduacao(@PathVariable Long conviteId) {
        return ResponseEntity.ok(eventoGraduacaoService.recusarConvite(conviteId));
    }

    @GetMapping("/me/inscricoes-graduacao")
    public ResponseEntity<List<InscricaoGraduacaoResponse>> minhasInscricoesGraduacao() {
        return ResponseEntity.ok(eventoGraduacaoService.listarInscricoesDoAlunoAtual());
    }

    @PostMapping("/me/eventos-graduacao/{eventoId}/inscrever")
    public ResponseEntity<InscricaoGraduacaoResponse> inscreverEventoGraduacao(@PathVariable Long eventoId) {
        return ResponseEntity.ok(eventoGraduacaoService.inscreverAlunoAtual(eventoId));
    }

    @PostMapping("/me/inscricoes-graduacao/{inscricaoId}/pix")
    public ResponseEntity<CriarPixInscricaoGraduacaoResponse> criarPixInscricaoGraduacao(@PathVariable Long inscricaoId) {
        return ResponseEntity.ok(pagamentoService.criarPixInscricaoGraduacao(inscricaoId));
    }

    @PatchMapping("/me/inscricoes-graduacao/{inscricaoId}/cancelar")
    public ResponseEntity<InscricaoGraduacaoResponse> cancelarInscricaoGraduacao(@PathVariable Long inscricaoId) {
        return ResponseEntity.ok(eventoGraduacaoService.cancelarInscricaoAlunoAtual(inscricaoId));
    }






    

}
