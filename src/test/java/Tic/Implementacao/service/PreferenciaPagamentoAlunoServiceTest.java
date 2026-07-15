package Tic.Implementacao.service;

import Tic.Implementacao.dto.financeiro.preferencia.ContextoPagamentoAluno;
import Tic.Implementacao.dto.financeiro.preferencia.PreferenciaPagamentoRequest;
import Tic.Implementacao.dto.financeiro.preferencia.PreferenciaPagamentoResponse;
import Tic.Implementacao.model.*;
import Tic.Implementacao.repository.AlunoRepository;
import Tic.Implementacao.repository.PagamentoRepository;
import Tic.Implementacao.repository.PreferenciaPagamentoAlunoRepository;
import Tic.Implementacao.security.PermissaoService;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PreferenciaPagamentoAlunoServiceTest {

    private static final Clock CLOCK = Clock.fixed(
            LocalDateTime.of(2026, 7, 15, 9, 0).atZone(ZoneId.systemDefault()).toInstant(),
            ZoneId.systemDefault());

    @Test
    void alunoDefinePixBoletoEDinheiroSemCriarPagamentoOuFatura() {
        Fixture fx = fixture(PerfilUsuario.ALUNO);
        when(fx.preferenciaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PreferenciaPagamentoResponse pix = fx.service.salvarDoAlunoAtual(request(FormaPagamentoPreferida.PIX));
        PreferenciaPagamentoResponse boleto = fx.service.salvarDoAlunoAtual(request(FormaPagamentoPreferida.BOLETO));
        PreferenciaPagamentoResponse dinheiro = fx.service.salvarDoAlunoAtual(request(FormaPagamentoPreferida.DINHEIRO));

        assertEquals(FormaPagamentoPreferida.PIX, pix.getFormaPreferida());
        assertEquals(FormaPagamentoPreferida.BOLETO, boleto.getFormaPreferida());
        assertEquals(FormaPagamentoPreferida.DINHEIRO, dinheiro.getFormaPreferida());
        assertEquals(StatusAutorizacaoPagamento.NAO_SOLICITADA, dinheiro.getStatusAutorizacao());
        verify(fx.pagamentoRepository, never()).save(any());
    }

    @Test
    void pixAutomaticoComecaPendenteESemAutorizacaoAtiva() {
        Fixture fx = fixture(PerfilUsuario.ALUNO);
        when(fx.preferenciaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PreferenciaPagamentoResponse response = fx.service.salvarDoAlunoAtual(request(FormaPagamentoPreferida.PIX_AUTOMATICO));

        assertEquals(StatusAutorizacaoPagamento.PENDENTE, response.getStatusAutorizacao());
        assertFalse(response.getPagamentoAutomaticoAtivo());
    }

    @Test
    void cartaoNaoArmazenaTokenNemFicaAutorizadoPeloAluno() {
        Fixture fx = fixture(PerfilUsuario.ALUNO);
        when(fx.preferenciaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PreferenciaPagamentoResponse response = fx.service.salvarDoAlunoAtual(request(FormaPagamentoPreferida.CARTAO));

        assertEquals(StatusAutorizacaoPagamento.NAO_SOLICITADA, response.getStatusAutorizacao());
        assertFalse(response.getPagamentoAutomaticoAtivo());
        assertNull(response.getDescricaoMascarada());
    }

    @Test
    void adminAtualizaAutorizacaoExternaDeFormaControlada() {
        Fixture fx = fixture(PerfilUsuario.ADMIN);
        when(fx.alunoRepository.findByIdAndAcademiaId(20L, 1L)).thenReturn(Optional.of(fx.aluno));
        when(fx.preferenciaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PreferenciaPagamentoResponse response = fx.service.atualizarAutorizacaoExterna(
                20L, FormaPagamentoPreferida.PIX_AUTOMATICO, StatusAutorizacaoPagamento.ATIVA,
                "mandate_123", "Pix Automático autorizado");

        assertEquals(StatusAutorizacaoPagamento.ATIVA, response.getStatusAutorizacao());
        assertTrue(response.getPagamentoAutomaticoAtivo());
        assertEquals("Pix Automático autorizado", response.getDescricaoMascarada());
    }

    @Test
    void alunoConsultaSomentePropriaPreferenciaEProfessorConsultaDaAcademia() {
        Fixture aluno = fixture(PerfilUsuario.ALUNO);
        PreferenciaPagamentoAluno preferencia = preferencia(FormaPagamentoPreferida.PIX, aluno);
        when(aluno.preferenciaRepository.findByAcademiaIdAndAlunoIdAndAtivaTrue(1L, 20L)).thenReturn(Optional.of(preferencia));
        assertTrue(aluno.service.buscarDoAlunoAtual().isPresent());

        Fixture professor = fixture(PerfilUsuario.PROFESSOR);
        when(professor.alunoRepository.findByIdAndAcademiaId(20L, 1L)).thenReturn(Optional.of(professor.aluno));
        when(professor.preferenciaRepository.findByAcademiaIdAndAlunoIdAndAtivaTrue(1L, 20L))
                .thenReturn(Optional.of(preferencia(FormaPagamentoPreferida.BOLETO, professor)));
        assertEquals(FormaPagamentoPreferida.BOLETO, professor.service.buscarPorAluno(20L).get().getFormaPreferida());
    }

    @Test
    void bloqueiaUsuarioNaoAlunoNoEndpointAlunoEAlunoDeOutraAcademiaParaProfessor() {
        Fixture professor = fixture(PerfilUsuario.PROFESSOR);
        assertThrows(AccessDeniedException.class, () -> professor.service.buscarDoAlunoAtual());

        when(professor.alunoRepository.findByIdAndAcademiaId(99L, 1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> professor.service.buscarPorAluno(99L));
    }

    @Test
    void remocaoDesativaPreferenciaSemExcluirRegistro() {
        Fixture fx = fixture(PerfilUsuario.ALUNO);
        PreferenciaPagamentoAluno preferencia = preferencia(FormaPagamentoPreferida.PIX, fx);
        when(fx.preferenciaRepository.findByAcademiaIdAndAlunoIdAndAtivaTrue(1L, 20L)).thenReturn(Optional.of(preferencia));

        fx.service.removerDoAlunoAtual();

        assertFalse(preferencia.getAtiva());
        assertEquals(StatusAutorizacaoPagamento.CANCELADA, preferencia.getStatusAutorizacao());
        verify(fx.preferenciaRepository).save(preferencia);
    }

    @Test
    void contextoPagamentoIdentificaPixValidoProcessandoEAutomaticoAtivo() {
        Fixture fx = fixture(PerfilUsuario.ADMIN);
        PreferenciaPagamentoAluno preferencia = preferencia(FormaPagamentoPreferida.PIX_AUTOMATICO, fx);
        preferencia.setStatusAutorizacao(StatusAutorizacaoPagamento.ATIVA);
        preferencia.setPagamentoAutomaticoAutorizado(true);
        Fatura fatura = fatura(fx);
        Pagamento pix = pagamento(fx, fatura, StatusPagamento.PENDENTE);
        pix.setIdPagamentoExterno("pay_123");
        pix.setDataExpiracao(LocalDateTime.now(CLOCK).plusHours(2));
        Pagamento processando = pagamento(fx, fatura, StatusPagamento.PROCESSANDO);
        when(fx.preferenciaRepository.findByAcademiaIdAndAlunoIdAndAtivaTrue(1L, 20L)).thenReturn(Optional.of(preferencia));
        when(fx.pagamentoRepository.findMensalidadesComFaturaPorFaturas(1L, List.of(50L))).thenReturn(List.of(pix, processando));

        ContextoPagamentoAluno contexto = fx.service.obterContextoPagamento(fatura);

        assertEquals(FormaPagamentoPreferida.PIX_AUTOMATICO, contexto.getFormaPreferida());
        assertTrue(contexto.getPagamentoAutomaticoAtivo());
        assertTrue(contexto.getPossuiPixValido());
        assertTrue(contexto.getPossuiPagamentoProcessando());
    }

    private Fixture fixture(PerfilUsuario perfil) {
        PreferenciaPagamentoAlunoRepository preferenciaRepository = mock(PreferenciaPagamentoAlunoRepository.class);
        AlunoRepository alunoRepository = mock(AlunoRepository.class);
        PagamentoRepository pagamentoRepository = mock(PagamentoRepository.class);
        PermissaoService permissaoService = mock(PermissaoService.class);
        Academia academia = new Academia();
        academia.setId(1L);
        academia.setNome("Elite");
        Aluno aluno = new Aluno();
        aluno.setId(20L);
        aluno.setNome("Julia");
        aluno.setAcademia(academia);
        Usuario usuario = new Usuario();
        usuario.setId(10L);
        usuario.setNome("Usuário");
        usuario.setAcademia(academia);
        usuario.setPerfil(perfil);
        if (perfil == PerfilUsuario.ALUNO) {
            usuario.setAluno(aluno);
        }
        when(permissaoService.usuarioAtual()).thenReturn(usuario);
        when(permissaoService.academiaAtualId()).thenReturn(1L);
        PreferenciaPagamentoAlunoService service = new PreferenciaPagamentoAlunoService(
                preferenciaRepository, alunoRepository, pagamentoRepository, permissaoService, CLOCK);
        return new Fixture(service, preferenciaRepository, alunoRepository, pagamentoRepository, permissaoService,
                academia, aluno, usuario);
    }

    private PreferenciaPagamentoRequest request(FormaPagamentoPreferida forma) {
        PreferenciaPagamentoRequest request = new PreferenciaPagamentoRequest();
        request.setFormaPreferida(forma);
        return request;
    }

    private PreferenciaPagamentoAluno preferencia(FormaPagamentoPreferida forma, Fixture fx) {
        PreferenciaPagamentoAluno preferencia = new PreferenciaPagamentoAluno();
        preferencia.setId(5L);
        preferencia.setAcademia(fx.academia);
        preferencia.setAluno(fx.aluno);
        preferencia.setFormaPreferida(forma);
        preferencia.setAtiva(true);
        preferencia.setStatusAutorizacao(StatusAutorizacaoPagamento.NAO_SOLICITADA);
        preferencia.setPagamentoAutomaticoAutorizado(false);
        preferencia.setCriadaEm(LocalDateTime.now(CLOCK));
        preferencia.setAtualizadaEm(LocalDateTime.now(CLOCK));
        preferencia.setCriadaPor(fx.usuario);
        return preferencia;
    }

    private Fatura fatura(Fixture fx) {
        Fatura fatura = new Fatura();
        fatura.setId(50L);
        fatura.setAcademia(fx.academia);
        fatura.setAluno(fx.aluno);
        fatura.setValor(new BigDecimal("200.00"));
        fatura.setStatus(StatusFatura.PENDENTE);
        fatura.setDataVencimento(LocalDate.of(2026, 7, 15));
        fatura.setMesReferencia(LocalDate.of(2026, 7, 1));
        return fatura;
    }

    private Pagamento pagamento(Fixture fx, Fatura fatura, StatusPagamento status) {
        Pagamento pagamento = new Pagamento();
        pagamento.setAcademia(fx.academia);
        pagamento.setFatura(fatura);
        pagamento.setTipoCobranca(TipoCobranca.MENSALIDADE);
        pagamento.setMetodo(MetodoPagamento.PIX);
        pagamento.setStatus(status);
        pagamento.setValor(new BigDecimal("200.00"));
        return pagamento;
    }

    private record Fixture(PreferenciaPagamentoAlunoService service,
                           PreferenciaPagamentoAlunoRepository preferenciaRepository,
                           AlunoRepository alunoRepository,
                           PagamentoRepository pagamentoRepository,
                           PermissaoService permissaoService,
                           Academia academia,
                           Aluno aluno,
                           Usuario usuario) {
    }
}
