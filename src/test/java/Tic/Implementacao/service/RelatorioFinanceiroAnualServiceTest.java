package Tic.Implementacao.service;

import Tic.Implementacao.dto.relatorio.financeiro.anual.RelatorioFinanceiroAnualResponse;
import Tic.Implementacao.dto.relatorio.financeiro.anual.ResumoFinanceiroMesResponse;
import Tic.Implementacao.model.*;
import Tic.Implementacao.repository.AlunoRepository;
import Tic.Implementacao.repository.FaturaRepository;
import Tic.Implementacao.repository.PagamentoRepository;
import Tic.Implementacao.security.PermissaoService;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RelatorioFinanceiroAnualServiceTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 7, 31);
    private static final Clock CLOCK = Clock.fixed(HOJE.atTime(12, 0).atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());

    @Test
    void anoSemMovimentacaoRetornaDozeMesesZerados() {
        Fixture fx = fixture();
        when(fx.alunoRepository.findByAtivoTrueAndAcademiaId(1L)).thenReturn(List.of(fx.alunoTurma));
        when(fx.faturaRepository.findMensaisDoPeriodoComAlunoPlano(1L, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 1))).thenReturn(List.of());

        RelatorioFinanceiroAnualResponse response = fx.service.gerarAnual(2026, null, null, null, null);

        assertEquals(12, response.getMeses().size());
        assertEquals(new BigDecimal("0.00"), response.getResumo().getTotalPrevisto());
        assertEquals(new BigDecimal("0.00"), response.getResumo().getTaxaRecebimento());
        assertNull(response.getMelhorMesReceita());
        assertNull(response.getPiorMesReceita());
        assertEquals(3, response.getPorTipoPlano().size());
        verify(fx.faturaRepository, never()).save(any());
        verify(fx.pagamentoRepository, never()).save(any());
    }

    @Test
    void consolidaTotaisAnuaisIgnorandoPagamentoNaoConfirmadoEGraduacao() {
        Fixture fx = fixture();
        Fatura janeiro = fatura(fx, 101L, fx.alunoTurma, StatusFatura.PAGA, "200", 1, LocalDate.of(2026, 1, 10));
        Fatura fevereiro = fatura(fx, 102L, fx.alunoPersonal, StatusFatura.PARCIALMENTE_PAGA, "300", 2, LocalDate.of(2026, 2, 10));
        Fatura cancelada = fatura(fx, 103L, fx.alunoSemPlano, StatusFatura.CANCELADA, "150", 3, LocalDate.of(2026, 3, 10));
        when(fx.alunoRepository.findByAtivoTrueAndAcademiaId(1L)).thenReturn(List.of(fx.alunoTurma, fx.alunoPersonal, fx.alunoSemPlano));
        when(fx.faturaRepository.findMensaisDoPeriodoComAlunoPlano(1L, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 1)))
                .thenReturn(List.of(janeiro, fevereiro, cancelada));
        when(fx.pagamentoRepository.findMensalidadesPorFaturas(eq(1L), anyList())).thenReturn(List.of(
                pagamento(janeiro, 1L, MetodoPagamento.PIX, StatusPagamento.PAGO, "200"),
                pagamento(fevereiro, 2L, MetodoPagamento.DINHEIRO, StatusPagamento.PAGO, "120"),
                pagamento(fevereiro, 3L, MetodoPagamento.PIX, StatusPagamento.PENDENTE, "50"),
                pagamento(fevereiro, 4L, MetodoPagamento.CARTAO, StatusPagamento.PROCESSANDO, "50"),
                pagamento(fevereiro, 5L, MetodoPagamento.PIX, StatusPagamento.ESTORNADO, "20"),
                pagamentoGraduacao(fevereiro, 6L, "999")
        ));

        RelatorioFinanceiroAnualResponse response = fx.service.gerarAnual(2026, null, null, null, 10);

        assertEquals(new BigDecimal("500.00"), response.getResumo().getTotalPrevisto());
        assertEquals(new BigDecimal("320.00"), response.getResumo().getTotalRecebido());
        assertEquals(new BigDecimal("180.00"), response.getResumo().getTotalPendente());
        assertEquals(new BigDecimal("180.00"), response.getResumo().getTotalVencido());
        assertEquals(new BigDecimal("120.00"), response.getResumo().getTotalParcialmentePago());
        assertEquals(new BigDecimal("150.00"), response.getResumo().getTotalCancelado());
        assertEquals(new BigDecimal("20.00"), response.getResumo().getTotalEstornado());
        assertEquals(new BigDecimal("300.00"), response.getResumo().getReceitaLiquida());
        assertEquals(new BigDecimal("64.00"), response.getResumo().getTaxaRecebimento());
        assertEquals(new BigDecimal("36.00"), response.getResumo().getTaxaInadimplencia());
        assertEquals(new BigDecimal("160.00"), response.getResumo().getTicketMedioRecebido());
        assertEquals(1, response.getMelhorMesReceita());
        assertEquals(2, response.getPiorMesReceita());
        assertEquals(2, response.getMesMaiorInadimplencia());
        assertEquals(1, response.getMaioresInadimplentes().size());
        assertEquals(fx.alunoPersonal.getId(), response.getMaioresInadimplentes().get(0).getAlunoId());
    }

    @Test
    void calculaVariacaoAcumuladoAgrupamentosEFiltros() {
        Fixture fx = fixture();
        Fatura janeiroTurma = fatura(fx, 101L, fx.alunoTurma, StatusFatura.PAGA, "200", 1, LocalDate.of(2026, 1, 10));
        Fatura fevereiroTurma = fatura(fx, 102L, fx.alunoTurma, StatusFatura.PAGA, "200", 2, LocalDate.of(2026, 2, 10));
        Fatura marcoPersonal = fatura(fx, 103L, fx.alunoPersonal, StatusFatura.PAGA, "300", 3, LocalDate.of(2026, 3, 10));
        when(fx.alunoRepository.findByAtivoTrueAndAcademiaId(1L)).thenReturn(List.of(fx.alunoTurma, fx.alunoPersonal));
        when(fx.faturaRepository.findMensaisDoPeriodoComAlunoPlano(1L, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 1)))
                .thenReturn(List.of(janeiroTurma, fevereiroTurma, marcoPersonal));
        when(fx.pagamentoRepository.findMensalidadesPorFaturas(eq(1L), anyList())).thenReturn(List.of(
                pagamento(janeiroTurma, 1L, MetodoPagamento.PIX, StatusPagamento.PAGO, "100"),
                pagamento(fevereiroTurma, 2L, MetodoPagamento.PIX, StatusPagamento.PAGO, "200"),
                pagamento(marcoPersonal, 3L, MetodoPagamento.DINHEIRO, StatusPagamento.PAGO, "300")
        ));

        RelatorioFinanceiroAnualResponse response = fx.service.gerarAnual(2026, TipoPlano.TURMA, MetodoPagamento.PIX, null, 10);

        assertEquals(new BigDecimal("400.00"), response.getResumo().getTotalPrevisto());
        assertEquals(new BigDecimal("300.00"), response.getResumo().getTotalRecebido());
        assertEquals(2, response.getResumo().getQuantidadeFaturas());
        assertEquals(1, response.getPorFormaPagamento().size());
        assertEquals("PIX", response.getPorFormaPagamento().get(0).getMetodoPagamento());
        ResumoFinanceiroMesResponse fevereiro = response.getMeses().get(1);
        assertEquals(new BigDecimal("100.00"), fevereiro.getVariacaoReceitaPercentual());
        assertEquals(new BigDecimal("300.00"), fevereiro.getRecebidoAcumulado());
        assertTrue(response.getPorTipoPlano().stream().anyMatch(t -> t.getTipoPlano().equals("PERSONAL") && t.getQuantidadeFaturas() == 0));
    }

    @Test
    void limitaMaioresInadimplentesEValidaLimite() {
        Fixture fx = fixture();
        Fatura turma = fatura(fx, 101L, fx.alunoTurma, StatusFatura.VENCIDA, "200", 1, LocalDate.of(2026, 1, 10));
        Fatura personal = fatura(fx, 102L, fx.alunoPersonal, StatusFatura.VENCIDA, "300", 1, LocalDate.of(2026, 1, 12));
        when(fx.alunoRepository.findByAtivoTrueAndAcademiaId(1L)).thenReturn(List.of(fx.alunoTurma, fx.alunoPersonal));
        when(fx.faturaRepository.findMensaisDoPeriodoComAlunoPlano(1L, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 1)))
                .thenReturn(List.of(turma, personal));
        when(fx.pagamentoRepository.findMensalidadesPorFaturas(eq(1L), anyList())).thenReturn(List.of());

        RelatorioFinanceiroAnualResponse response = fx.service.gerarAnual(2026, null, null, null, 1);

        assertEquals(1, response.getMaioresInadimplentes().size());
        assertEquals(fx.alunoPersonal.getId(), response.getMaioresInadimplentes().get(0).getAlunoId());
        assertThrows(RuntimeException.class, () -> fx.service.gerarAnual(2026, null, null, null, 0));
    }

    @Test
    void csvAnualPossuiCabecalhoENomeArquivo() {
        Fixture fx = fixture();
        fx.alunoTurma.setNome("Aluno, Teste");
        Fatura fatura = fatura(fx, 101L, fx.alunoTurma, StatusFatura.PAGA, "200", 1, LocalDate.of(2026, 1, 10));
        when(fx.alunoRepository.findByAtivoTrueAndAcademiaId(1L)).thenReturn(List.of(fx.alunoTurma));
        when(fx.faturaRepository.findMensaisDoPeriodoComAlunoPlano(1L, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 1))).thenReturn(List.of(fatura));
        when(fx.pagamentoRepository.findMensalidadesPorFaturas(eq(1L), anyList())).thenReturn(List.of(pagamento(fatura, 1L, MetodoPagamento.PIX, StatusPagamento.PAGO, "200")));

        String csv = new String(fx.service.gerarCsvAnual(2026, null, null, null, 10), StandardCharsets.UTF_8);

        assertTrue(csv.startsWith("Mês,Competência,Previsto"));
        assertTrue(csv.contains("janeiro,2026-01,200.00"));
        assertEquals("relatorio-financeiro-anual-2026.csv", fx.service.nomeArquivoAnual(2026));
    }

    @Test
    void segurancaBloqueiaAlunoEConsultaNaoAlteraDados() {
        Fixture fx = fixture();
        doThrow(new AccessDeniedException("bloqueado")).when(fx.permissaoService).exigirAdminOuProfessor();
        assertThrows(AccessDeniedException.class, () -> fx.service.gerarAnual(2026, null, null, null, null));

        Fixture consulta = fixture();
        when(consulta.alunoRepository.findByAtivoTrueAndAcademiaId(1L)).thenReturn(List.of());
        when(consulta.faturaRepository.findMensaisDoPeriodoComAlunoPlano(1L, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 1))).thenReturn(List.of());
        consulta.service.gerarAnual(2026, null, null, null, null);

        verify(consulta.faturaRepository, never()).save(any());
        verify(consulta.pagamentoRepository, never()).save(any());
        verify(consulta.alunoRepository, never()).save(any());
    }

    private Fixture fixture() {
        FaturaRepository faturaRepository = mock(FaturaRepository.class);
        PagamentoRepository pagamentoRepository = mock(PagamentoRepository.class);
        AlunoRepository alunoRepository = mock(AlunoRepository.class);
        PermissaoService permissaoService = mock(PermissaoService.class);
        Academia academia = new Academia();
        academia.setId(1L);
        academia.setNome("Elite");
        Aluno alunoTurma = aluno(10L, "Turma", "turma@email.com", academia, TipoPlano.TURMA, new BigDecimal("200"));
        Aluno alunoPersonal = aluno(20L, "Personal", "personal@email.com", academia, TipoPlano.PERSONAL, new BigDecimal("300"));
        Aluno alunoSemPlano = aluno(30L, "Sem Plano", "sem@email.com", academia, null, new BigDecimal("150"));
        when(permissaoService.academiaAtualId()).thenReturn(1L);
        RelatorioFinanceiroService service = new RelatorioFinanceiroService(faturaRepository, pagamentoRepository,
                alunoRepository, permissaoService, CLOCK);
        return new Fixture(service, faturaRepository, pagamentoRepository, alunoRepository, permissaoService,
                academia, alunoTurma, alunoPersonal, alunoSemPlano);
    }

    private Aluno aluno(Long id, String nome, String email, Academia academia, TipoPlano tipoPlano, BigDecimal valor) {
        Aluno aluno = new Aluno();
        aluno.setId(id);
        aluno.setNome(nome);
        aluno.setEmail(email);
        aluno.setAcademia(academia);
        aluno.setAtivo(true);
        aluno.setDiaVencimento(10);
        aluno.setValorMensalidade(valor);
        if (tipoPlano != null) {
            Plano plano = new Plano();
            plano.setId(id + 100);
            plano.setAcademia(academia);
            plano.setTipo(tipoPlano);
            plano.setValor(valor);
            plano.setQuantidadeAulasSemana(3);
            plano.setAtivo(true);
            aluno.setPlano(plano);
        }
        return aluno;
    }

    private Fatura fatura(Fixture fx, Long id, Aluno aluno, StatusFatura status, String valor, int mes, LocalDate vencimento) {
        Fatura fatura = new Fatura();
        fatura.setId(id);
        fatura.setAcademia(fx.academia);
        fatura.setAluno(aluno);
        fatura.setMesReferencia(LocalDate.of(2026, mes, 1));
        fatura.setDataVencimento(vencimento);
        fatura.setValor(new BigDecimal(valor));
        fatura.setStatus(status);
        return fatura;
    }

    private Pagamento pagamento(Fatura fatura, Long id, MetodoPagamento metodo, StatusPagamento status, String valor) {
        Pagamento pagamento = new Pagamento();
        pagamento.setId(id);
        pagamento.setAcademia(fatura.getAcademia());
        pagamento.setFatura(fatura);
        pagamento.setTipoCobranca(TipoCobranca.MENSALIDADE);
        pagamento.setMetodo(metodo);
        pagamento.setStatus(status);
        pagamento.setValor(new BigDecimal(valor));
        pagamento.setPagoEm(HOJE.atTime(9, 0).plusMinutes(id));
        return pagamento;
    }

    private Pagamento pagamentoGraduacao(Fatura fatura, Long id, String valor) {
        Pagamento pagamento = pagamento(fatura, id, MetodoPagamento.PIX, StatusPagamento.PAGO, valor);
        pagamento.setTipoCobranca(TipoCobranca.GRADUACAO);
        return pagamento;
    }

    private record Fixture(RelatorioFinanceiroService service,
                           FaturaRepository faturaRepository,
                           PagamentoRepository pagamentoRepository,
                           AlunoRepository alunoRepository,
                           PermissaoService permissaoService,
                           Academia academia,
                           Aluno alunoTurma,
                           Aluno alunoPersonal,
                           Aluno alunoSemPlano) {
    }
}
