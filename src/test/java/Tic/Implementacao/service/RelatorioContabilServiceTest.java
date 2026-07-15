package Tic.Implementacao.service;

import Tic.Implementacao.dto.relatorio.financeiro.contabil.*;
import Tic.Implementacao.model.*;
import Tic.Implementacao.repository.PagamentoRepository;
import Tic.Implementacao.security.PermissaoService;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RelatorioContabilServiceTest {

    @Test
    void periodoSemRecebimentosRetornaResumoZeradoComAviso() {
        Fixture fx = fixture();
        when(fx.pagamentoRepository.findParaRelatorioContabil(eq(1L), any(), any())).thenReturn(List.of());

        RelatorioContabilResponse response = fx.service.gerar(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31),
                null, null, null, null, null, null);

        assertEquals(new BigDecimal("0.00"), response.getResumo().getTotalRecebidoBruto());
        assertEquals(new BigDecimal("0.00"), response.getResumo().getTotalTaxas());
        assertEquals(2, response.getPorNatureza().size());
        assertTrue(response.getAviso().contains("apoio contábil"));
    }

    @Test
    void separaMensalidadeGraduacaoEstornoEIgnoraStatusNaoRecebido() {
        Fixture fx = fixture();
        Fatura fatura = fatura(fx, 101L, fx.alunoTurma, LocalDate.of(2025, 12, 1));
        InscricaoGraduacao inscricao = inscricao(fx, 201L, fx.alunoPersonal);
        when(fx.pagamentoRepository.findParaRelatorioContabil(eq(1L), any(), any())).thenReturn(List.of(
                pagamentoMensalidade(fx, 1L, fatura, StatusPagamento.PAGO, "200", LocalDateTime.of(2026, 1, 5, 10, 0)),
                pagamentoGraduacao(fx, 2L, inscricao, StatusPagamento.PAGO, "300", LocalDateTime.of(2026, 1, 6, 10, 0)),
                pagamentoMensalidade(fx, 3L, fatura, StatusPagamento.ESTORNADO, "50", LocalDateTime.of(2026, 1, 7, 10, 0)),
                pagamentoMensalidade(fx, 4L, fatura, StatusPagamento.PENDENTE, "999", LocalDateTime.of(2026, 1, 8, 10, 0)),
                pagamentoMensalidade(fx, 5L, fatura, StatusPagamento.PROCESSANDO, "999", LocalDateTime.of(2026, 1, 8, 11, 0)),
                pagamentoMensalidade(fx, 6L, fatura, StatusPagamento.CANCELADO, "999", LocalDateTime.of(2026, 1, 8, 12, 0))
        ));

        RelatorioContabilResponse response = fx.service.gerar(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31),
                null, null, null, null, null, null);

        assertEquals(new BigDecimal("500.00"), response.getResumo().getTotalRecebidoBruto());
        assertEquals(new BigDecimal("50.00"), response.getResumo().getTotalEstornado());
        assertEquals(new BigDecimal("450.00"), response.getResumo().getTotalLiquido());
        assertEquals(new BigDecimal("200.00"), response.getResumo().getTotalMensalidades());
        assertEquals(new BigDecimal("300.00"), response.getResumo().getTotalGraduacoes());
        assertEquals(2, response.getResumo().getQuantidadePagamentos());
        assertEquals(1, response.getResumo().getQuantidadeEstornos());
        assertEquals("2025-12-01", response.getLancamentos().get(0).getCompetencia());
    }

    @Test
    void aplicaFiltrosDeNaturezaMetodoAlunoEventoStatusECompetencia() {
        Fixture fx = fixture();
        Fatura fatura = fatura(fx, 101L, fx.alunoTurma, LocalDate.of(2026, 1, 1));
        InscricaoGraduacao inscricao = inscricao(fx, 201L, fx.alunoPersonal);
        Pagamento mensalidadePix = pagamentoMensalidade(fx, 1L, fatura, StatusPagamento.PAGO, "200", LocalDateTime.of(2026, 1, 5, 10, 0));
        Pagamento graduacaoDinheiro = pagamentoGraduacao(fx, 2L, inscricao, StatusPagamento.PAGO, "300", LocalDateTime.of(2026, 1, 6, 10, 0));
        when(fx.pagamentoRepository.findParaRelatorioContabil(eq(1L), any(), any())).thenReturn(List.of(mensalidadePix, graduacaoDinheiro));

        RelatorioContabilResponse response = fx.service.gerar(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31),
                TipoCobranca.GRADUACAO, MetodoPagamento.DINHEIRO, StatusPagamento.PAGO, fx.alunoPersonal.getId(),
                fx.evento.getId(), "2026-01");

        assertEquals(1, response.getLancamentos().size());
        assertEquals(TipoCobranca.GRADUACAO, response.getLancamentos().get(0).getNaturezaReceita());
        assertEquals("Amarela", response.getLancamentos().get(0).getGraduacaoDestino());
    }

    @Test
    void agrupamentosPorAlunoCompetenciaECsvsFuncionam() {
        Fixture fx = fixture();
        Fatura janeiro = fatura(fx, 101L, fx.alunoTurma, LocalDate.of(2026, 1, 1));
        InscricaoGraduacao inscricao = inscricao(fx, 201L, fx.alunoPersonal);
        when(fx.pagamentoRepository.findParaRelatorioContabil(eq(1L), any(), any())).thenReturn(List.of(
                pagamentoMensalidade(fx, 1L, janeiro, StatusPagamento.PAGO, "200", LocalDateTime.of(2026, 1, 5, 10, 0)),
                pagamentoGraduacao(fx, 2L, inscricao, StatusPagamento.PAGO, "300", LocalDateTime.of(2026, 2, 6, 10, 0))
        ));

        List<ResumoRecebimentosAlunoResponse> alunos = fx.service.recebimentosPorAluno(2026, null);
        List<ResumoCompetenciaContabilResponse> competencias = fx.service.competencias(2026);
        String csv = new String(fx.service.gerarCsv(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31),
                null, null, null, null, null, null), StandardCharsets.UTF_8);
        String csvAlunos = new String(fx.service.gerarCsvAlunos(2026, null), StandardCharsets.UTF_8);

        assertEquals(2, alunos.size());
        assertEquals(12, competencias.size());
        assertEquals(new BigDecimal("200.00"), competencias.get(0).getTotalMensalidades());
        assertEquals(new BigDecimal("300.00"), competencias.get(1).getTotalGraduacoes());
        assertTrue(csv.startsWith("Data do pagamento,Aluno,Documento"));
        assertTrue(csvAlunos.startsWith("Aluno,Documento,Total mensalidades"));
        assertEquals("relatorio-contabil-2026-01-01-2026-12-31.csv", fx.service.nomeArquivo(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)));
    }

    @Test
    void bloqueiaProfessorAlunoEConsultaNaoAlteraDados() {
        Fixture fx = fixture();
        doThrow(new AccessDeniedException("admin apenas")).when(fx.permissaoService).exigirAdmin();

        assertThrows(AccessDeniedException.class, () -> fx.service.gerar(LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 31), null, null, null, null, null, null));

        Fixture consulta = fixture();
        when(consulta.pagamentoRepository.findParaRelatorioContabil(eq(1L), any(), any())).thenReturn(List.of());
        consulta.service.gerar(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), null, null, null, null, null, null);

        verify(consulta.pagamentoRepository, never()).save(any());
    }

    private Fixture fixture() {
        PagamentoRepository pagamentoRepository = mock(PagamentoRepository.class);
        PermissaoService permissaoService = mock(PermissaoService.class);
        Academia academia = new Academia();
        academia.setId(1L);
        academia.setNome("Elite");
        academia.setCpfOuCnpj("12.345.678/0001-90");
        Aluno alunoTurma = aluno(10L, "Turma", "turma@email.com", "11122233344", academia);
        Aluno alunoPersonal = aluno(20L, "Personal", "personal@email.com", "55566677788", academia);
        EventoGraduacao evento = new EventoGraduacao();
        evento.setId(30L);
        evento.setAcademia(academia);
        evento.setTitulo("Graduação 2026");
        Graduacao graduacao = new Graduacao();
        graduacao.setId(40L);
        graduacao.setNome("Amarela");
        graduacao.setCorHex("#FFFF00");
        graduacao.setAcademia(academia);
        when(permissaoService.academiaAtual()).thenReturn(academia);
        when(permissaoService.academiaAtualId()).thenReturn(1L);
        RelatorioContabilService service = new RelatorioContabilService(pagamentoRepository, permissaoService);
        return new Fixture(service, pagamentoRepository, permissaoService, academia, alunoTurma, alunoPersonal, evento, graduacao);
    }

    private Aluno aluno(Long id, String nome, String email, String cpf, Academia academia) {
        Aluno aluno = new Aluno();
        aluno.setId(id);
        aluno.setNome(nome);
        aluno.setCpf(cpf);
        aluno.setEmail(email);
        aluno.setAcademia(academia);
        aluno.setAtivo(true);
        aluno.setDiaVencimento(10);
        aluno.setValorMensalidade(new BigDecimal("200"));
        return aluno;
    }

    private Fatura fatura(Fixture fx, Long id, Aluno aluno, LocalDate competencia) {
        Fatura fatura = new Fatura();
        fatura.setId(id);
        fatura.setAcademia(fx.academia);
        fatura.setAluno(aluno);
        fatura.setMesReferencia(competencia);
        fatura.setDataVencimento(competencia.plusDays(9));
        fatura.setValor(new BigDecimal("200"));
        fatura.setStatus(StatusFatura.PAGA);
        return fatura;
    }

    private InscricaoGraduacao inscricao(Fixture fx, Long id, Aluno aluno) {
        InscricaoGraduacao inscricao = new InscricaoGraduacao();
        inscricao.setId(id);
        inscricao.setAcademia(fx.academia);
        inscricao.setAluno(aluno);
        inscricao.setEvento(fx.evento);
        inscricao.setGraduacaoDestino(fx.graduacao);
        inscricao.setValor(new BigDecimal("300"));
        return inscricao;
    }

    private Pagamento pagamentoMensalidade(Fixture fx, Long id, Fatura fatura, StatusPagamento status,
                                           String valor, LocalDateTime pagoEm) {
        Pagamento pagamento = pagamentoBase(fx, id, status, valor, pagoEm);
        pagamento.setTipoCobranca(TipoCobranca.MENSALIDADE);
        pagamento.setFatura(fatura);
        pagamento.setMetodo(MetodoPagamento.PIX);
        return pagamento;
    }

    private Pagamento pagamentoGraduacao(Fixture fx, Long id, InscricaoGraduacao inscricao, StatusPagamento status,
                                         String valor, LocalDateTime pagoEm) {
        Pagamento pagamento = pagamentoBase(fx, id, status, valor, pagoEm);
        pagamento.setTipoCobranca(TipoCobranca.GRADUACAO);
        pagamento.setInscricaoGraduacao(inscricao);
        pagamento.setMetodo(MetodoPagamento.DINHEIRO);
        return pagamento;
    }

    private Pagamento pagamentoBase(Fixture fx, Long id, StatusPagamento status, String valor, LocalDateTime pagoEm) {
        Pagamento pagamento = new Pagamento();
        pagamento.setId(id);
        pagamento.setAcademia(fx.academia);
        pagamento.setValor(new BigDecimal(valor));
        pagamento.setStatus(status);
        pagamento.setProvedor("ASAAS");
        pagamento.setIdPagamentoExterno("pay_" + id);
        pagamento.setCriadoEm(pagoEm.minusHours(1));
        pagamento.setPagoEm(pagoEm);
        pagamento.setUltimoEventoEm(pagoEm.plusDays(1));
        return pagamento;
    }

    private record Fixture(RelatorioContabilService service,
                           PagamentoRepository pagamentoRepository,
                           PermissaoService permissaoService,
                           Academia academia,
                           Aluno alunoTurma,
                           Aluno alunoPersonal,
                           EventoGraduacao evento,
                           Graduacao graduacao) {
    }
}
