package Tic.Implementacao.service;

import Tic.Implementacao.dto.graduacao.ElegibilidadeGraduacaoResponse;
import Tic.Implementacao.model.*;
import Tic.Implementacao.repository.*;
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

class CertificadoGraduacaoServiceTest {

    private static final Clock CLOCK = Clock.fixed(
            LocalDateTime.of(2026, 8, 10, 14, 0).atZone(ZoneId.systemDefault()).toInstant(),
            ZoneId.systemDefault()
    );

    @Test
    void emiteCertificadoParaPromocaoElegivelComNumeroECodigoUnicos() {
        Fixture fx = fixture(PerfilUsuario.PROFESSOR);
        AvaliacaoGraduacao avaliacao = avaliacaoElegivel(fx);
        when(fx.avaliacaoRepository.findByAcademiaIdAndEventoId(1L, 100L)).thenReturn(List.of(avaliacao));
        when(fx.certificadoRepository.findFirstByNumeroCertificadoStartingWithOrderByNumeroCertificadoDesc("EFC-2026-"))
                .thenReturn(Optional.empty());
        when(fx.certificadoRepository.existsByNumeroCertificado(anyString())).thenReturn(false);
        when(fx.certificadoRepository.existsByCodigoValidacao(anyString())).thenReturn(false);
        when(fx.certificadoRepository.save(any(CertificadoGraduacao.class))).thenAnswer(invocation -> {
            CertificadoGraduacao certificado = invocation.getArgument(0);
            certificado.setId(500L);
            return certificado;
        });

        var emitidos = fx.service.emitirCertificadosEventoConcluido(fx.evento);

        assertEquals(1, emitidos.size());
        assertEquals("EFC-2026-000001", emitidos.getFirst().getNumeroCertificado());
        assertTrue(emitidos.getFirst().getCodigoValidacao().matches("[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{4}"));
        verify(fx.certificadoRepository).save(any(CertificadoGraduacao.class));
    }

    @Test
    void naoEmiteParaReprovadoAusenteCanceladoOuPagamentoNaoConfirmado() {
        Fixture fx = fixture(PerfilUsuario.PROFESSOR);
        AvaliacaoGraduacao reprovado = avaliacaoElegivel(fx);
        reprovado.setResultado(ResultadoAvaliacaoGraduacao.REPROVADO);
        AvaliacaoGraduacao ausente = avaliacaoElegivel(fx);
        ausente.setId(91L);
        ausente.setStatusPresenca(StatusPresencaGraduacao.AUSENTE);
        AvaliacaoGraduacao cancelada = avaliacaoElegivel(fx);
        cancelada.setId(92L);
        cancelada.getInscricao().setStatus(StatusInscricaoGraduacao.CANCELADA);
        AvaliacaoGraduacao pendente = avaliacaoElegivel(fx);
        pendente.setId(93L);
        pendente.getInscricao().getPagamento().setStatus(StatusPagamento.PENDENTE);
        when(fx.avaliacaoRepository.findByAcademiaIdAndEventoId(1L, 100L)).thenReturn(List.of(reprovado, ausente, cancelada, pendente));

        assertTrue(fx.service.emitirCertificadosEventoConcluido(fx.evento).isEmpty());
        verify(fx.certificadoRepository, never()).save(any());
    }

    @Test
    void naoDuplicaCertificadoJaEmitido() {
        Fixture fx = fixture(PerfilUsuario.PROFESSOR);
        AvaliacaoGraduacao avaliacao = avaliacaoElegivel(fx);
        CertificadoGraduacao existente = certificado(fx, avaliacao);
        when(fx.avaliacaoRepository.findByAcademiaIdAndEventoId(1L, 100L)).thenReturn(List.of(avaliacao));
        when(fx.certificadoRepository.existsByAvaliacaoId(90L)).thenReturn(true);
        when(fx.certificadoRepository.findByAvaliacaoId(90L)).thenReturn(Optional.of(existente));

        var emitidos = fx.service.emitirCertificadosEventoConcluido(fx.evento);

        assertEquals(1, emitidos.size());
        assertEquals("EFC-2026-000001", emitidos.getFirst().getNumeroCertificado());
        verify(fx.certificadoRepository, never()).save(any());
    }

    @Test
    void validacaoPublicaRetornaValidoCanceladoOuInvalido() {
        Fixture fx = fixture(PerfilUsuario.ALUNO);
        CertificadoGraduacao ativo = certificado(fx, avaliacaoElegivel(fx));
        CertificadoGraduacao cancelado = certificado(fx, avaliacaoElegivel(fx));
        cancelado.setAtivo(false);
        when(fx.certificadoRepository.findByCodigoValidacao("VALIDO")).thenReturn(Optional.of(ativo));
        when(fx.certificadoRepository.findByCodigoValidacao("CANCELADO")).thenReturn(Optional.of(cancelado));
        when(fx.certificadoRepository.findByCodigoValidacao("X")).thenReturn(Optional.empty());

        assertEquals("VALIDO", fx.service.validarPublicamente("VALIDO").getStatus());
        assertEquals("CANCELADO", fx.service.validarPublicamente("CANCELADO").getStatus());
        assertEquals("INVALIDO", fx.service.validarPublicamente("X").getStatus());
    }

    @Test
    void adminCancelaCertificadoEAlunoNaoCancela() {
        Fixture fx = fixture(PerfilUsuario.ADMIN);
        CertificadoGraduacao certificado = certificado(fx, avaliacaoElegivel(fx));
        when(fx.certificadoRepository.findByIdAndAcademiaId(500L, 1L)).thenReturn(Optional.of(certificado));
        when(fx.certificadoRepository.save(any(CertificadoGraduacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = fx.service.cancelarCertificado(500L, "Erro operacional");

        assertFalse(response.getAtivo());
        assertEquals("Erro operacional", certificado.getMotivoCancelamento());

        Fixture aluno = fixture(PerfilUsuario.ALUNO);
        doThrow(new AccessDeniedException("admin")).when(aluno.permissaoService).exigirAdmin();
        assertThrows(AccessDeniedException.class, () -> aluno.service.cancelarCertificado(500L, "x"));
    }

    @Test
    void consultaCertificadoRespeitaAlunoEAcademia() {
        Fixture fx = fixture(PerfilUsuario.ALUNO);
        CertificadoGraduacao certificado = certificado(fx, avaliacaoElegivel(fx));
        when(fx.certificadoRepository.findById(500L)).thenReturn(Optional.of(certificado));

        assertEquals(500L, fx.service.buscarCertificado(500L).getId());

        Aluno outroAluno = aluno(21L, "Outro", fx.academia, fx.branca);
        certificado.setAluno(outroAluno);
        assertThrows(AccessDeniedException.class, () -> fx.service.buscarCertificado(500L));

        Fixture professor = fixture(PerfilUsuario.PROFESSOR);
        CertificadoGraduacao outraAcademia = certificado(professor, avaliacaoElegivel(professor));
        outraAcademia.getAcademia().setId(2L);
        when(professor.certificadoRepository.findById(500L)).thenReturn(Optional.of(outraAcademia));
        assertThrows(AccessDeniedException.class, () -> professor.service.buscarCertificado(500L));
    }

    @Test
    void historicoCompletoResumoAlunoEExportacaoCsv() {
        Fixture fx = fixture(PerfilUsuario.ALUNO);
        AvaliacaoGraduacao avaliacao = avaliacaoElegivel(fx);
        CertificadoGraduacao certificado = certificado(fx, avaliacao);
        when(fx.alunoRepository.findByIdAndAcademiaId(20L, 1L)).thenReturn(Optional.of(fx.aluno));
        when(fx.certificadoRepository.findByAcademiaIdAndAlunoIdOrderByDataEmissaoDesc(1L, 20L)).thenReturn(List.of(certificado));
        when(fx.historicoRepository.findByAcademiaIdAndAlunoIdOrderByAlteradoEmAsc(1L, 20L)).thenReturn(List.of(fx.historico));
        when(fx.historicoRepository.findByAcademiaIdAndAlunoIdOrderByAlteradoEmDesc(1L, 20L)).thenReturn(List.of(fx.historico));
        when(fx.graduacaoAlunoService.calcularElegibilidade(fx.aluno)).thenReturn(new ElegibilidadeGraduacaoResponse(
                20L, 2L, "Azul", 3L, "Roxa", 12, 20, 8, 0,
                LocalDate.of(2026, 8, 10), LocalDate.of(2026, 8, 10),
                false, true, false, "Check-ins pendentes"));

        var historico = fx.service.historicoCompletoAluno(20L);
        var resumo = fx.service.graduacoesDoAlunoAtual();
        String csv = new String(fx.service.exportarHistoricoCompletoCsv(20L));
        String certificadosCsv = new String(fx.service.exportarCertificadosCsv());

        assertEquals(1, historico.size());
        assertTrue(historico.getFirst().getCertificadoEmitido());
        assertEquals("Roxa", resumo.getProximaGraduacao());
        assertEquals(8, resumo.getCheckInsRestantes());
        assertTrue(csv.contains("Número do certificado"));
        assertTrue(certificadosCsv.contains("EFC-2026-000001"));
    }

    @Test
    void dashboardProfessorContaEmitidosCanceladosEAguardando() {
        Fixture fx = fixture(PerfilUsuario.PROFESSOR);
        AvaliacaoGraduacao aguardando = avaliacaoElegivel(fx);
        aguardando.setId(99L);
        when(fx.certificadoRepository.countByAcademiaId(1L)).thenReturn(10L);
        when(fx.certificadoRepository.countByAcademiaIdAndAtivoFalse(1L)).thenReturn(2L);
        when(fx.certificadoRepository.findTop5ByAcademiaIdAndAtivoTrueOrderByDataEmissaoDesc(1L))
                .thenReturn(List.of(certificado(fx, avaliacaoElegivel(fx))));
        when(fx.avaliacaoRepository.findByAcademiaIdAndPromocaoConfirmadaTrueAndEventoStatus(1L, StatusEventoGraduacao.CONCLUIDO))
                .thenReturn(List.of(aguardando));
        when(fx.certificadoRepository.existsByAvaliacaoId(99L)).thenReturn(false);

        var dashboard = fx.service.dashboardProfessor();

        assertEquals(10L, dashboard.getTotalCertificadosEmitidos());
        assertEquals(2L, dashboard.getCertificadosCancelados());
        assertEquals(1, dashboard.getAlunosAguardandoCertificado());
        assertEquals(1, dashboard.getUltimasPromocoes().size());
    }

    private Fixture fixture(PerfilUsuario perfil) {
        CertificadoGraduacaoRepository certificadoRepository = mock(CertificadoGraduacaoRepository.class);
        AvaliacaoGraduacaoRepository avaliacaoRepository = mock(AvaliacaoGraduacaoRepository.class);
        HistoricoGraduacaoRepository historicoRepository = mock(HistoricoGraduacaoRepository.class);
        AlunoRepository alunoRepository = mock(AlunoRepository.class);
        CheckInRepository checkInRepository = mock(CheckInRepository.class);
        GraduacaoAlunoService graduacaoAlunoService = mock(GraduacaoAlunoService.class);
        PermissaoService permissaoService = mock(PermissaoService.class);
        CertificadoGraduacaoService service = new CertificadoGraduacaoService(certificadoRepository, avaliacaoRepository,
                historicoRepository, alunoRepository, checkInRepository, graduacaoAlunoService, permissaoService, CLOCK);

        Academia academia = new Academia();
        academia.setId(1L);
        academia.setNome("Elite Fight Club");
        Graduacao branca = graduacao(1L, "Branco", academia);
        Graduacao azul = graduacao(2L, "Azul", academia);
        Aluno aluno = aluno(20L, "Ana", academia, branca);
        Usuario usuario = usuario(10L, perfil, academia, aluno);
        EventoGraduacao evento = evento(100L, academia);
        HistoricoGraduacao historico = historico(300L, academia, aluno, branca, azul, usuario);

        when(permissaoService.usuarioAtual()).thenReturn(usuario);
        when(permissaoService.academiaAtualId()).thenReturn(1L);
        when(permissaoService.academiaAtual()).thenReturn(academia);
        return new Fixture(service, certificadoRepository, avaliacaoRepository, historicoRepository, alunoRepository,
                checkInRepository, graduacaoAlunoService, permissaoService, academia, branca, azul, aluno, usuario, evento, historico);
    }

    private AvaliacaoGraduacao avaliacaoElegivel(Fixture fx) {
        InscricaoGraduacao inscricao = new InscricaoGraduacao();
        inscricao.setId(70L);
        inscricao.setAcademia(fx.academia);
        inscricao.setAluno(fx.aluno);
        inscricao.setEvento(fx.evento);
        inscricao.setGraduacaoDestino(fx.azul);
        inscricao.setValor(BigDecimal.valueOf(120));
        inscricao.setStatus(StatusInscricaoGraduacao.CONCLUIDA);
        Pagamento pagamento = new Pagamento();
        pagamento.setId(80L);
        pagamento.setStatus(StatusPagamento.PAGO);
        pagamento.setValor(BigDecimal.valueOf(120));
        inscricao.setPagamento(pagamento);

        AvaliacaoGraduacao avaliacao = new AvaliacaoGraduacao();
        avaliacao.setId(90L);
        avaliacao.setAcademia(fx.academia);
        avaliacao.setAluno(fx.aluno);
        avaliacao.setEvento(fx.evento);
        avaliacao.setInscricao(inscricao);
        avaliacao.setGraduacaoAtual(fx.branca);
        avaliacao.setGraduacaoDestino(fx.azul);
        avaliacao.setStatusPresenca(StatusPresencaGraduacao.PRESENTE);
        avaliacao.setResultado(ResultadoAvaliacaoGraduacao.APROVADO);
        avaliacao.setPromocaoConfirmada(true);
        avaliacao.setHistoricoGerado(fx.historico);
        avaliacao.setPromovidoPor(fx.usuario);
        avaliacao.setNotaTecnica(BigDecimal.valueOf(9));
        avaliacao.setObservacaoPromocao("Excelente evolução");
        return avaliacao;
    }

    private CertificadoGraduacao certificado(Fixture fx, AvaliacaoGraduacao avaliacao) {
        CertificadoGraduacao certificado = new CertificadoGraduacao();
        certificado.setId(500L);
        certificado.setAcademia(fx.academia);
        certificado.setAluno(fx.aluno);
        certificado.setEvento(fx.evento);
        certificado.setGraduacaoAnterior(fx.branca);
        certificado.setGraduacaoNova(fx.azul);
        certificado.setAvaliacao(avaliacao);
        certificado.setHistorico(fx.historico);
        certificado.setNumeroCertificado("EFC-2026-000001");
        certificado.setCodigoValidacao("7F2D-91AE-42BC");
        certificado.setDataEmissao(LocalDateTime.now(CLOCK));
        certificado.setEmitidoPor(fx.usuario);
        certificado.setAtivo(true);
        return certificado;
    }

    private Graduacao graduacao(Long id, String nome, Academia academia) {
        Graduacao graduacao = new Graduacao();
        graduacao.setId(id);
        graduacao.setNome(nome);
        graduacao.setCorHex("#FFFFFF");
        graduacao.setOrdem(id.intValue());
        graduacao.setAcademia(academia);
        return graduacao;
    }

    private Aluno aluno(Long id, String nome, Academia academia, Graduacao graduacao) {
        Aluno aluno = new Aluno();
        aluno.setId(id);
        aluno.setNome(nome);
        aluno.setEmail(nome.toLowerCase() + "@email.com");
        aluno.setAcademia(academia);
        aluno.setGraduacaoEntidade(graduacao);
        aluno.setAtivo(true);
        return aluno;
    }

    private Usuario usuario(Long id, PerfilUsuario perfil, Academia academia, Aluno aluno) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNome(perfil.name());
        usuario.setPerfil(perfil);
        usuario.setAcademia(academia);
        usuario.setAluno(perfil == PerfilUsuario.ALUNO ? aluno : null);
        return usuario;
    }

    private EventoGraduacao evento(Long id, Academia academia) {
        EventoGraduacao evento = new EventoGraduacao();
        evento.setId(id);
        evento.setAcademia(academia);
        evento.setTitulo("Graduação Elite");
        evento.setDataEvento(LocalDate.of(2026, 8, 10));
        evento.setHoraInicio(LocalTime.of(9, 0));
        evento.setHoraFim(LocalTime.of(12, 0));
        evento.setLocal("Tatame");
        evento.setStatus(StatusEventoGraduacao.CONCLUIDO);
        return evento;
    }

    private HistoricoGraduacao historico(Long id, Academia academia, Aluno aluno, Graduacao anterior,
                                         Graduacao nova, Usuario usuario) {
        HistoricoGraduacao historico = new HistoricoGraduacao();
        historico.setId(id);
        historico.setAcademia(academia);
        historico.setAluno(aluno);
        historico.setGraduacaoAnterior(anterior);
        historico.setNovaGraduacao(nova);
        historico.setAlteradoEm(LocalDateTime.now(CLOCK));
        historico.setAlteradoPor(usuario);
        historico.setMotivo(MotivoAlteracaoGraduacao.APROVACAO_AVALIACAO);
        historico.setObservacao("Promovido no evento");
        historico.setPromocaoPorAvaliacao(true);
        return historico;
    }

    private record Fixture(CertificadoGraduacaoService service,
                           CertificadoGraduacaoRepository certificadoRepository,
                           AvaliacaoGraduacaoRepository avaliacaoRepository,
                           HistoricoGraduacaoRepository historicoRepository,
                           AlunoRepository alunoRepository,
                           CheckInRepository checkInRepository,
                           GraduacaoAlunoService graduacaoAlunoService,
                           PermissaoService permissaoService,
                           Academia academia,
                           Graduacao branca,
                           Graduacao azul,
                           Aluno aluno,
                           Usuario usuario,
                           EventoGraduacao evento,
                           HistoricoGraduacao historico) {
    }
}
