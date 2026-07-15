package Tic.Implementacao.service;

import Tic.Implementacao.dto.relatorio.financeiro.*;
import Tic.Implementacao.model.*;
import Tic.Implementacao.repository.AlunoRepository;
import Tic.Implementacao.repository.FaturaRepository;
import Tic.Implementacao.repository.PagamentoRepository;
import Tic.Implementacao.security.PermissaoService;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RelatorioFinanceiroServiceTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 7, 31);
    private static final Clock CLOCK = Clock.fixed(HOJE.atTime(12, 0).atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());

    @Test
    void mesSemFaturasMostraAlunosSemFatura() {
        Fixture fx = fixture();
        when(fx.alunoRepository.findByAtivoTrueAndAcademiaId(1L)).thenReturn(List.of(fx.alunoTurma));
        when(fx.faturaRepository.findMensaisComAlunoPlano(1L, LocalDate.of(2026, 7, 1))).thenReturn(List.of());
        when(fx.faturaRepository.findPossiveisVencidasAntesDe(1L, HOJE)).thenReturn(List.of());

        RelatorioFinanceiroMensalResponse response = fx.service.gerarMensal(2026, 7, null, null, null, null, false);

        assertEquals(1, response.getResumo().getAlunosSemFatura());
        assertEquals(BigDecimal.ZERO.setScale(2), response.getResumo().getTotalPrevisto());
        assertEquals("SEM_FATURA", response.getAlunos().get(0).getStatusFatura());
    }

    @Test
    void calculaPrevistoRecebidoPendenteParcialVencidoCanceladoEstornoELiquido() {
        Fixture fx = fixture();
        Fatura paga = fatura(fx, 101L, fx.alunoTurma, StatusFatura.PAGA, new BigDecimal("200"), HOJE.plusDays(5));
        Fatura parcial = fatura(fx, 102L, fx.alunoPersonal, StatusFatura.PARCIALMENTE_PAGA, new BigDecimal("300"), HOJE.minusDays(2));
        Fatura cancelada = fatura(fx, 103L, fx.alunoSemPlano, StatusFatura.CANCELADA, new BigDecimal("150"), HOJE.minusDays(1));
        when(fx.alunoRepository.findByAtivoTrueAndAcademiaId(1L)).thenReturn(List.of(fx.alunoTurma, fx.alunoPersonal, fx.alunoSemPlano));
        when(fx.faturaRepository.findMensaisComAlunoPlano(1L, LocalDate.of(2026, 7, 1))).thenReturn(List.of(paga, parcial, cancelada));
        when(fx.faturaRepository.findPossiveisVencidasAntesDe(1L, HOJE)).thenReturn(List.of(parcial));
        when(fx.pagamentoRepository.findMensalidadesPorFaturas(eq(1L), anyList())).thenReturn(List.of(
                pagamento(paga, 1L, MetodoPagamento.PIX, StatusPagamento.PAGO, "200"),
                pagamento(parcial, 2L, MetodoPagamento.DINHEIRO, StatusPagamento.PAGO, "120"),
                pagamento(parcial, 3L, MetodoPagamento.PIX, StatusPagamento.PENDENTE, "50"),
                pagamento(parcial, 4L, MetodoPagamento.CARTAO, StatusPagamento.PROCESSANDO, "50"),
                pagamento(parcial, 5L, MetodoPagamento.PIX, StatusPagamento.ESTORNADO, "20"),
                pagamentoGraduacao(paga, 6L, "999")
        ));

        RelatorioFinanceiroMensalResponse response = fx.service.gerarMensal(2026, 7, null, null, null, null, false);
        ResumoFinanceiroMensalResponse resumo = response.getResumo();

        assertEquals(new BigDecimal("500.00"), resumo.getTotalPrevisto());
        assertEquals(new BigDecimal("320.00"), resumo.getTotalRecebido());
        assertEquals(new BigDecimal("180.00"), resumo.getTotalPendente());
        assertEquals(new BigDecimal("180.00"), resumo.getTotalVencido());
        assertEquals(new BigDecimal("120.00"), resumo.getTotalParcialmentePago());
        assertEquals(new BigDecimal("150.00"), resumo.getTotalCancelado());
        assertEquals(new BigDecimal("20.00"), resumo.getTotalEstornado());
        assertEquals(new BigDecimal("0.00"), resumo.getTotalTaxas());
        assertEquals(new BigDecimal("300.00"), resumo.getReceitaLiquida());
        assertEquals(1, resumo.getFaturasPagas());
        assertEquals(1, resumo.getFaturasParcialmentePagas());
        assertEquals(1, resumo.getFaturasCanceladas());
        assertEquals(1, resumo.getAlunosInadimplentes());
        assertEquals(2, resumo.getAlunosAdimplentes());
    }

    @Test
    void agrupaPorMetodoETipoPlanoComSemPlano() {
        Fixture fx = fixture();
        Fatura turma = fatura(fx, 101L, fx.alunoTurma, StatusFatura.PAGA, new BigDecimal("200"), HOJE);
        Fatura personal = fatura(fx, 102L, fx.alunoPersonal, StatusFatura.PENDENTE, new BigDecimal("300"), HOJE.plusDays(3));
        Fatura semPlano = fatura(fx, 103L, fx.alunoSemPlano, StatusFatura.PAGA, new BigDecimal("150"), HOJE);
        when(fx.alunoRepository.findByAtivoTrueAndAcademiaId(1L)).thenReturn(List.of(fx.alunoTurma, fx.alunoPersonal, fx.alunoSemPlano));
        when(fx.faturaRepository.findMensaisComAlunoPlano(1L, LocalDate.of(2026, 7, 1))).thenReturn(List.of(turma, personal, semPlano));
        when(fx.faturaRepository.findPossiveisVencidasAntesDe(1L, HOJE)).thenReturn(List.of());
        when(fx.pagamentoRepository.findMensalidadesPorFaturas(eq(1L), anyList())).thenReturn(List.of(
                pagamento(turma, 1L, MetodoPagamento.PIX, StatusPagamento.PAGO, "200"),
                pagamento(semPlano, 2L, MetodoPagamento.DINHEIRO, StatusPagamento.PAGO, "150")
        ));

        RelatorioFinanceiroMensalResponse response = fx.service.gerarMensal(2026, 7, null, null, null, null, false);

        assertEquals(2, response.getPorFormaPagamento().size());
        assertTrue(response.getPorTipoPlano().stream().anyMatch(t -> t.getTipoPlano().equals("TURMA")));
        assertTrue(response.getPorTipoPlano().stream().anyMatch(t -> t.getTipoPlano().equals("PERSONAL")));
        assertTrue(response.getPorTipoPlano().stream().anyMatch(t -> t.getTipoPlano().equals("SEM_PLANO")));
    }

    @Test
    void filtrosRefletemTotaisFiltrados() {
        Fixture fx = fixture();
        Fatura turma = fatura(fx, 101L, fx.alunoTurma, StatusFatura.PAGA, new BigDecimal("200"), HOJE);
        Fatura personal = fatura(fx, 102L, fx.alunoPersonal, StatusFatura.PENDENTE, new BigDecimal("300"), HOJE.minusDays(2));
        when(fx.alunoRepository.findByAtivoTrueAndAcademiaId(1L)).thenReturn(List.of(fx.alunoTurma, fx.alunoPersonal));
        when(fx.faturaRepository.findMensaisComAlunoPlano(1L, LocalDate.of(2026, 7, 1))).thenReturn(List.of(turma, personal));
        when(fx.faturaRepository.findPossiveisVencidasAntesDe(1L, HOJE)).thenReturn(List.of(personal));
        when(fx.pagamentoRepository.findMensalidadesPorFaturas(eq(1L), anyList())).thenReturn(List.of(
                pagamento(turma, 1L, MetodoPagamento.PIX, StatusPagamento.PAGO, "200")
        ));

        RelatorioFinanceiroMensalResponse response = fx.service.gerarMensal(2026, 7, StatusFatura.PENDENTE, TipoPlano.PERSONAL, null, null, true);

        assertEquals(1, response.getAlunos().size());
        assertEquals("Personal", response.getAlunos().get(0).getAlunoNome());
        assertEquals(new BigDecimal("300.00"), response.getResumo().getTotalPrevisto());
    }

    @Test
    void inadimplenciaConsideraDebitoAnterior() {
        Fixture fx = fixture();
        Fatura antiga = fatura(fx, 201L, fx.alunoPersonal, StatusFatura.VENCIDA, new BigDecimal("300"), HOJE.minusDays(10));
        antiga.setMesReferencia(LocalDate.of(2026, 6, 1));
        when(fx.faturaRepository.findPossiveisVencidasAntesDe(1L, HOJE)).thenReturn(List.of(antiga));
        when(fx.pagamentoRepository.findMensalidadesPorFaturas(eq(1L), anyList())).thenReturn(List.of(
                pagamento(antiga, 1L, MetodoPagamento.PIX, StatusPagamento.PAGO, "100")
        ));

        List<AlunoInadimplenteResponse> response = fx.service.listarInadimplencia(HOJE);

        assertEquals(1, response.size());
        assertEquals(new BigDecimal("200.00"), response.get(0).getTotalVencido());
        assertEquals(10, response.get(0).getDiasEmAtraso());
    }

    @Test
    void csvPossuiCabecalhoEscapeENomeArquivo() {
        Fixture fx = fixture();
        fx.alunoTurma.setNome("Aluno, Teste");
        Fatura fatura = fatura(fx, 101L, fx.alunoTurma, StatusFatura.PAGA, new BigDecimal("200"), HOJE);
        when(fx.alunoRepository.findByAtivoTrueAndAcademiaId(1L)).thenReturn(List.of(fx.alunoTurma));
        when(fx.faturaRepository.findMensaisComAlunoPlano(1L, LocalDate.of(2026, 7, 1))).thenReturn(List.of(fatura));
        when(fx.faturaRepository.findPossiveisVencidasAntesDe(1L, HOJE)).thenReturn(List.of());
        when(fx.pagamentoRepository.findMensalidadesPorFaturas(eq(1L), anyList())).thenReturn(List.of(pagamento(fatura, 1L, MetodoPagamento.PIX, StatusPagamento.PAGO, "200")));

        String csv = new String(fx.service.gerarCsvMensal(2026, 7, null, null, null, null, false), java.nio.charset.StandardCharsets.UTF_8);

        assertTrue(csv.startsWith("Aluno,E-mail,Tipo de plano"));
        assertTrue(csv.contains("\"Aluno, Teste\""));
        assertEquals("relatorio-financeiro-2026-07.csv", fx.service.nomeArquivoMensal(2026, 7));
    }

    @Test
    void segurancaAlunoBloqueadoProfessorSemPermissaoEConsultaNaoAlteraDados() {
        Fixture fx = fixture();
        doThrow(new AccessDeniedException("bloqueado")).when(fx.permissaoService).exigirAdminOuProfessor();
        assertThrows(AccessDeniedException.class, () -> fx.service.gerarMensal(2026, 7, null, null, null, null, false));

        Fixture consulta = fixture();
        when(consulta.alunoRepository.findByAtivoTrueAndAcademiaId(1L)).thenReturn(List.of());
        when(consulta.faturaRepository.findMensaisComAlunoPlano(1L, LocalDate.of(2026, 7, 1))).thenReturn(List.of());
        when(consulta.faturaRepository.findPossiveisVencidasAntesDe(1L, HOJE)).thenReturn(List.of());
        consulta.service.gerarMensal(2026, 7, null, null, null, null, false);

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

    private Fatura fatura(Fixture fx, Long id, Aluno aluno, StatusFatura status, BigDecimal valor, LocalDate vencimento) {
        Fatura fatura = new Fatura();
        fatura.setId(id);
        fatura.setAcademia(fx.academia);
        fatura.setAluno(aluno);
        fatura.setMesReferencia(LocalDate.of(2026, 7, 1));
        fatura.setDataVencimento(vencimento);
        fatura.setValor(valor);
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
