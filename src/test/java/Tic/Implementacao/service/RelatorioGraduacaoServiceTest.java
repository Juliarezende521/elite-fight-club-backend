package Tic.Implementacao.service;

import Tic.Implementacao.dto.graduacao.relatorio.FiltroRelatorioParticipanteGraduacao;
import Tic.Implementacao.model.*;
import Tic.Implementacao.repository.*;
import Tic.Implementacao.security.PermissaoService;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RelatorioGraduacaoServiceTest {

    @Test
    void eventoSemParticipantesRetornaZerosEDivisaoSegura() {
        Fixture fx = fixture();
        when(fx.eventoRepository.findByIdAndAcademiaId(100L, 1L)).thenReturn(Optional.of(fx.evento));
        dados(List.of(fx.evento), List.of(), List.of(), List.of(), List.of(), List.of(), fx);

        var response = fx.service.gerarRelatorioEvento(100L);

        assertEquals(0, response.getTotalConvidados());
        assertEquals(0, response.getTotalInscritos());
        assertEquals(BigDecimal.ZERO, response.getReceitaBruta());
        assertEquals(new BigDecimal("0.00"), response.getIndicadores().getTaxaPagamento());
    }

    @Test
    void relatorioEventoConsolidaConvitesInscricoesPresencaAvaliacaoPromocaoEFinanceiro() {
        Fixture fx = fixture();
        Aluno aluno2 = aluno(21L, "Bruna", fx.academia, fx.branca);
        ConviteGraduacao convitePendente = convite(51L, fx.evento, aluno2, fx.branca, fx.azul, StatusConviteGraduacao.PENDENTE);
        InscricaoGraduacao inscricaoPaga = inscricao(70L, fx.evento, fx.aluno, fx.azul, BigDecimal.valueOf(120), StatusInscricaoGraduacao.PAGA);
        InscricaoGraduacao inscricaoPendente = inscricao(71L, fx.evento, aluno2, fx.azul, BigDecimal.valueOf(120), StatusInscricaoGraduacao.PENDENTE_PAGAMENTO);
        Pagamento pago = pagamento(80L, inscricaoPaga, StatusPagamento.PAGO, BigDecimal.valueOf(120));
        Pagamento estornado = pagamento(81L, inscricaoPaga, StatusPagamento.ESTORNADO, BigDecimal.valueOf(20));
        inscricaoPaga.setPagamento(pago);
        AvaliacaoGraduacao avaliacao = avaliacao(90L, inscricaoPaga, StatusPresencaGraduacao.PRESENTE, ResultadoAvaliacaoGraduacao.APROVADO, true);
        when(fx.eventoRepository.findByIdAndAcademiaId(100L, 1L)).thenReturn(Optional.of(fx.evento));
        dados(List.of(fx.evento), List.of(item(fx.evento, fx.azul, BigDecimal.valueOf(120))),
                List.of(fx.convite, convitePendente), List.of(inscricaoPaga, inscricaoPendente),
                List.of(avaliacao), List.of(pago, estornado), fx);

        var response = fx.service.gerarRelatorioEvento(100L);

        assertEquals(2, response.getTotalConvidados());
        assertEquals(1, response.getConvitesAceitos());
        assertEquals(2, response.getTotalInscritos());
        assertEquals(1, response.getInscricoesPagas());
        assertEquals(1, response.getInscricoesPendentes());
        assertEquals(1, response.getPresentes());
        assertEquals(1, response.getAprovados());
        assertEquals(1, response.getPromovidos());
        assertEquals(BigDecimal.valueOf(120), response.getReceitaBruta());
        assertEquals(BigDecimal.valueOf(20), response.getValoresEstornados());
        assertEquals(BigDecimal.valueOf(100), response.getReceitaLiquida());
        assertEquals(BigDecimal.valueOf(120), response.getValoresPendentes());
        assertEquals(new BigDecimal("50.00"), response.getIndicadores().getTaxaAceitacaoConvite());
        assertEquals(new BigDecimal("50.00"), response.getIndicadores().getTaxaPagamento());
    }

    @Test
    void agrupaPorPrajiedEFiltraParticipantes() {
        Fixture fx = fixture();
        Graduacao roxa = graduacao(3L, "Roxa", "#663399", fx.academia);
        InscricaoGraduacao inscricaoAzul = inscricao(70L, fx.evento, fx.aluno, fx.azul, BigDecimal.valueOf(120), StatusInscricaoGraduacao.PAGA);
        InscricaoGraduacao inscricaoRoxa = inscricao(71L, fx.evento, aluno(22L, "Carlos", fx.academia, fx.azul), roxa, BigDecimal.valueOf(150), StatusInscricaoGraduacao.CONFIRMADA);
        Pagamento pagoAzul = pagamento(80L, inscricaoAzul, StatusPagamento.PAGO, BigDecimal.valueOf(120));
        inscricaoAzul.setPagamento(pagoAzul);
        when(fx.eventoRepository.findByIdAndAcademiaId(100L, 1L)).thenReturn(Optional.of(fx.evento));
        dados(List.of(fx.evento), List.of(item(fx.evento, fx.azul, BigDecimal.valueOf(120)), item(fx.evento, roxa, BigDecimal.valueOf(150))),
                List.of(fx.convite), List.of(inscricaoAzul, inscricaoRoxa), List.of(), List.of(pagoAzul), fx);

        var response = fx.service.gerarRelatorioEvento(100L);
        var participantesAzul = fx.service.listarParticipantes(100L,
                new FiltroRelatorioParticipanteGraduacao(2L, null, StatusPagamento.PAGO, null, null, null));

        assertEquals(2, response.getPorPrajied().size());
        assertEquals(1, response.getPorPrajied().get(0).getInscritos());
        assertEquals(BigDecimal.valueOf(120), response.getPorPrajied().get(0).getReceitaBruta());
        assertEquals(1, participantesAzul.size());
        assertEquals("Ana", participantesAzul.getFirst().getAlunoNome());
    }

    @Test
    void listaDetalhadaECsvNaoExibemPixOuQrCode() {
        Fixture fx = fixture();
        InscricaoGraduacao inscricao = inscricao(70L, fx.evento, fx.aluno, fx.azul, BigDecimal.valueOf(120), StatusInscricaoGraduacao.PAGA);
        Pagamento pago = pagamento(80L, inscricao, StatusPagamento.PAGO, BigDecimal.valueOf(120));
        pago.setPixCopiaCola("codigo-pix-sensivel");
        pago.setPixQrCodeBase64("qr-code-sensivel");
        inscricao.setPagamento(pago);
        AvaliacaoGraduacao avaliacao = avaliacao(90L, inscricao, StatusPresencaGraduacao.PRESENTE, ResultadoAvaliacaoGraduacao.REPROVADO, false);
        when(fx.eventoRepository.findByIdAndAcademiaId(100L, 1L)).thenReturn(Optional.of(fx.evento));
        dados(List.of(fx.evento), List.of(item(fx.evento, fx.azul, BigDecimal.valueOf(120))), List.of(fx.convite),
                List.of(inscricao), List.of(avaliacao), List.of(pago), fx);

        var participantes = fx.service.listarParticipantes(100L,
                new FiltroRelatorioParticipanteGraduacao(null, null, null, StatusPresencaGraduacao.PRESENTE, ResultadoAvaliacaoGraduacao.REPROVADO, false));
        String csv = new String(fx.service.gerarCsvParticipantes(100L,
                new FiltroRelatorioParticipanteGraduacao(null, null, null, null, null, null)));

        assertEquals(1, participantes.size());
        assertTrue(csv.startsWith("Aluno,E-mail"));
        assertTrue(csv.contains("\"Ana\""));
        assertFalse(csv.contains("codigo-pix-sensivel"));
        assertFalse(csv.contains("qr-code-sensivel"));
    }

    @Test
    void relatorioPeriodoFiltraEventoGraduacaoStatusEConsolidaTotais() {
        Fixture fx = fixture();
        EventoGraduacao evento2 = evento(101L, fx.academia, StatusEventoGraduacao.CONCLUIDO);
        InscricaoGraduacao inscricao = inscricao(70L, fx.evento, fx.aluno, fx.azul, BigDecimal.valueOf(120), StatusInscricaoGraduacao.PAGA);
        Pagamento pago = pagamento(80L, inscricao, StatusPagamento.PAGO, BigDecimal.valueOf(120));
        AvaliacaoGraduacao avaliacao = avaliacao(90L, inscricao, StatusPresencaGraduacao.PRESENTE, ResultadoAvaliacaoGraduacao.APROVADO, false);
        when(fx.eventoRepository.buscarParaRelatorio(1L, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 8, 31), null, StatusEventoGraduacao.PUBLICADO))
                .thenReturn(List.of(fx.evento, evento2));
        dados(List.of(fx.evento, evento2), List.of(item(fx.evento, fx.azul, BigDecimal.valueOf(120))),
                List.of(), List.of(inscricao), List.of(avaliacao), List.of(pago), fx);

        var response = fx.service.gerarRelatorioPeriodo(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 8, 31),
                null, 2L, StatusEventoGraduacao.PUBLICADO);

        assertEquals(1, response.getTotalEventos());
        assertEquals(1, response.getTotalInscricoes());
        assertEquals(1, response.getTotalAprovados());
        assertEquals(BigDecimal.valueOf(120), response.getReceitaBruta());
    }

    @Test
    void relatorioFinanceiroNaoMisturaMensalidadeEPendenteNaoContaReceita() {
        Fixture fx = fixture();
        InscricaoGraduacao paga = inscricao(70L, fx.evento, fx.aluno, fx.azul, BigDecimal.valueOf(120), StatusInscricaoGraduacao.PAGA);
        InscricaoGraduacao pendente = inscricao(71L, fx.evento, aluno(23L, "Dani", fx.academia, fx.branca), fx.azul, BigDecimal.valueOf(90), StatusInscricaoGraduacao.PENDENTE_PAGAMENTO);
        Pagamento pagoGraduacao = pagamento(80L, paga, StatusPagamento.PAGO, BigDecimal.valueOf(120));
        Pagamento pagamentoMensalidade = pagamento(81L, paga, StatusPagamento.PAGO, BigDecimal.valueOf(999));
        pagamentoMensalidade.setTipoCobranca(TipoCobranca.MENSALIDADE);
        when(fx.eventoRepository.buscarParaRelatorio(1L, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 8, 31), null, null))
                .thenReturn(List.of(fx.evento));
        dados(List.of(fx.evento), List.of(item(fx.evento, fx.azul, BigDecimal.valueOf(120))),
                List.of(), List.of(paga, pendente), List.of(), List.of(pagoGraduacao), fx);

        var response = fx.service.gerarRelatorioFinanceiro(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 8, 31), null, null);

        assertEquals(BigDecimal.valueOf(120), response.getTotalBruto());
        assertEquals(BigDecimal.valueOf(90), response.getTotalPendente());
        assertEquals(1, response.getLancamentos().size());
        verify(fx.pagamentoRepository).findPagamentosGraduacaoPorEventos(1L, List.of(100L));
    }

    @Test
    void bloqueiaAlunoEEventoDeOutraAcademia() {
        Fixture fx = fixture();
        doThrow(new AccessDeniedException("bloqueado")).when(fx.permissaoService).exigirAdminOuProfessor();
        assertThrows(AccessDeniedException.class, () -> fx.service.gerarRelatorioEvento(100L));

        reset(fx.permissaoService);
        when(fx.permissaoService.academiaAtualId()).thenReturn(2L);
        when(fx.eventoRepository.findByIdAndAcademiaId(100L, 2L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> fx.service.gerarRelatorioEvento(100L));
    }

    @Test
    void consultaNaoAlteraDados() {
        Fixture fx = fixture();
        when(fx.eventoRepository.findByIdAndAcademiaId(100L, 1L)).thenReturn(Optional.of(fx.evento));
        dados(List.of(fx.evento), List.of(), List.of(), List.of(), List.of(), List.of(), fx);

        fx.service.gerarRelatorioEvento(100L);

        verify(fx.eventoRepository, never()).save(any());
        verify(fx.inscricaoRepository, never()).save(any());
        verify(fx.pagamentoRepository, never()).save(any());
        verify(fx.avaliacaoRepository, never()).save(any());
    }

    private Fixture fixture() {
        EventoGraduacaoRepository eventoRepository = mock(EventoGraduacaoRepository.class);
        EventoGraduacaoPrajiedRepository prajiedRepository = mock(EventoGraduacaoPrajiedRepository.class);
        ConviteGraduacaoRepository conviteRepository = mock(ConviteGraduacaoRepository.class);
        InscricaoGraduacaoRepository inscricaoRepository = mock(InscricaoGraduacaoRepository.class);
        PagamentoRepository pagamentoRepository = mock(PagamentoRepository.class);
        AvaliacaoGraduacaoRepository avaliacaoRepository = mock(AvaliacaoGraduacaoRepository.class);
        PermissaoService permissaoService = mock(PermissaoService.class);
        RelatorioGraduacaoService service = new RelatorioGraduacaoService(eventoRepository, prajiedRepository,
                conviteRepository, inscricaoRepository, pagamentoRepository, avaliacaoRepository, permissaoService);
        Academia academia = new Academia();
        academia.setId(1L);
        Graduacao branca = graduacao(1L, "Branco", "#FFFFFF", academia);
        Graduacao azul = graduacao(2L, "Azul", "#0000FF", academia);
        Aluno aluno = aluno(20L, "Ana", academia, branca);
        EventoGraduacao evento = evento(100L, academia, StatusEventoGraduacao.PUBLICADO);
        ConviteGraduacao convite = convite(50L, evento, aluno, branca, azul, StatusConviteGraduacao.ACEITO);
        when(permissaoService.academiaAtualId()).thenReturn(1L);
        return new Fixture(service, eventoRepository, prajiedRepository, conviteRepository, inscricaoRepository,
                pagamentoRepository, avaliacaoRepository, permissaoService, academia, branca, azul, aluno, evento, convite);
    }

    private void dados(List<EventoGraduacao> eventos, List<EventoGraduacaoPrajied> prajieds,
                       List<ConviteGraduacao> convites, List<InscricaoGraduacao> inscricoes,
                       List<AvaliacaoGraduacao> avaliacoes, List<Pagamento> pagamentos, Fixture fx) {
        List<Long> ids = eventos.stream().map(EventoGraduacao::getId).toList();
        when(fx.prajiedRepository.findAtivosByEventoIds(ids)).thenReturn(prajieds);
        when(fx.conviteRepository.findByAcademiaIdAndEventoIdIn(1L, ids)).thenReturn(convites);
        when(fx.inscricaoRepository.findByAcademiaIdAndEventoIdIn(1L, ids)).thenReturn(inscricoes);
        when(fx.avaliacaoRepository.findByAcademiaIdAndEventoIdIn(1L, ids)).thenReturn(avaliacoes);
        when(fx.pagamentoRepository.findPagamentosGraduacaoPorEventos(1L, ids)).thenReturn(pagamentos);
    }

    private EventoGraduacao evento(Long id, Academia academia, StatusEventoGraduacao status) {
        EventoGraduacao evento = new EventoGraduacao();
        evento.setId(id);
        evento.setAcademia(academia);
        evento.setTitulo("Graduação Elite");
        evento.setDataEvento(LocalDate.of(2026, 8, 10));
        evento.setHoraInicio(LocalTime.of(9, 0));
        evento.setHoraFim(LocalTime.of(12, 0));
        evento.setLocal("Tatame");
        evento.setStatus(status);
        return evento;
    }

    private EventoGraduacaoPrajied item(EventoGraduacao evento, Graduacao graduacao, BigDecimal valor) {
        EventoGraduacaoPrajied item = new EventoGraduacaoPrajied();
        item.setId(graduacao.getId());
        item.setEvento(evento);
        item.setGraduacaoDestino(graduacao);
        item.setValor(valor);
        item.setAtivo(true);
        return item;
    }

    private Graduacao graduacao(Long id, String nome, String cor, Academia academia) {
        Graduacao graduacao = new Graduacao();
        graduacao.setId(id);
        graduacao.setNome(nome);
        graduacao.setCorHex(cor);
        graduacao.setOrdem(id.intValue());
        graduacao.setAcademia(academia);
        return graduacao;
    }

    private Aluno aluno(Long id, String nome, Academia academia, Graduacao atual) {
        Aluno aluno = new Aluno();
        aluno.setId(id);
        aluno.setNome(nome);
        aluno.setEmail(nome.toLowerCase() + "@email.com");
        aluno.setAcademia(academia);
        aluno.setGraduacaoEntidade(atual);
        return aluno;
    }

    private ConviteGraduacao convite(Long id, EventoGraduacao evento, Aluno aluno, Graduacao atual,
                                     Graduacao destino, StatusConviteGraduacao status) {
        ConviteGraduacao convite = new ConviteGraduacao();
        convite.setId(id);
        convite.setAcademia(evento.getAcademia());
        convite.setEvento(evento);
        convite.setAluno(aluno);
        convite.setGraduacaoAtual(atual);
        convite.setGraduacaoDestino(destino);
        convite.setStatus(status);
        convite.setEnviadoEm(LocalDateTime.of(2026, 7, 1, 9, 0));
        return convite;
    }

    private InscricaoGraduacao inscricao(Long id, EventoGraduacao evento, Aluno aluno, Graduacao destino,
                                         BigDecimal valor, StatusInscricaoGraduacao status) {
        InscricaoGraduacao inscricao = new InscricaoGraduacao();
        inscricao.setId(id);
        inscricao.setAcademia(evento.getAcademia());
        inscricao.setEvento(evento);
        inscricao.setAluno(aluno);
        inscricao.setGraduacaoDestino(destino);
        inscricao.setValor(valor);
        inscricao.setStatus(status);
        inscricao.setInscritoEm(LocalDateTime.of(2026, 7, 2, 10, 0));
        return inscricao;
    }

    private Pagamento pagamento(Long id, InscricaoGraduacao inscricao, StatusPagamento status, BigDecimal valor) {
        Pagamento pagamento = new Pagamento();
        pagamento.setId(id);
        pagamento.setAcademia(inscricao.getAcademia());
        pagamento.setInscricaoGraduacao(inscricao);
        pagamento.setTipoCobranca(TipoCobranca.GRADUACAO);
        pagamento.setMetodo(MetodoPagamento.PIX);
        pagamento.setProvedor("ASAAS");
        pagamento.setIdPagamentoExterno("pay_" + id);
        pagamento.setStatus(status);
        pagamento.setValor(valor);
        pagamento.setCriadoEm(LocalDateTime.of(2026, 7, 2, 11, 0));
        pagamento.setPagoEm(status == StatusPagamento.PAGO ? LocalDateTime.of(2026, 7, 2, 11, 30) : null);
        return pagamento;
    }

    private AvaliacaoGraduacao avaliacao(Long id, InscricaoGraduacao inscricao, StatusPresencaGraduacao presenca,
                                         ResultadoAvaliacaoGraduacao resultado, boolean promovido) {
        AvaliacaoGraduacao avaliacao = new AvaliacaoGraduacao();
        avaliacao.setId(id);
        avaliacao.setAcademia(inscricao.getAcademia());
        avaliacao.setInscricao(inscricao);
        avaliacao.setEvento(inscricao.getEvento());
        avaliacao.setAluno(inscricao.getAluno());
        avaliacao.setGraduacaoAtual(inscricao.getAluno().getGraduacaoEntidade());
        avaliacao.setGraduacaoDestino(inscricao.getGraduacaoDestino());
        avaliacao.setStatusPresenca(presenca);
        avaliacao.setResultado(resultado);
        avaliacao.setPromocaoConfirmada(promovido);
        avaliacao.setAvaliadoEm(LocalDateTime.of(2026, 8, 10, 12, 0));
        avaliacao.setPromovidoEm(promovido ? LocalDateTime.of(2026, 8, 10, 13, 0) : null);
        return avaliacao;
    }

    private record Fixture(RelatorioGraduacaoService service,
                           EventoGraduacaoRepository eventoRepository,
                           EventoGraduacaoPrajiedRepository prajiedRepository,
                           ConviteGraduacaoRepository conviteRepository,
                           InscricaoGraduacaoRepository inscricaoRepository,
                           PagamentoRepository pagamentoRepository,
                           AvaliacaoGraduacaoRepository avaliacaoRepository,
                           PermissaoService permissaoService,
                           Academia academia,
                           Graduacao branca,
                           Graduacao azul,
                           Aluno aluno,
                           EventoGraduacao evento,
                           ConviteGraduacao convite) {
    }
}
