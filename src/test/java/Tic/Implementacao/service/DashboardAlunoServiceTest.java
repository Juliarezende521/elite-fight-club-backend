package Tic.Implementacao.service;

import Tic.Implementacao.dto.dashboard.aluno.DashboardAlunoResponse;
import Tic.Implementacao.dto.dashboard.aluno.ProgressoMetaPesoDashboardResponse;
import Tic.Implementacao.dto.dashboard.aluno.ResumoPesoDashboardResponse;
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

class DashboardAlunoServiceTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 7, 14);

    @Test
    void dashboardAlunoComPlanoTurma() {
        Fixture fx = fixture(PerfilUsuario.ALUNO, TipoPlano.TURMA);
        when(fx.reservaRepository.countReservasConfirmadasNaSemana(1L, 20L, StatusReserva.CONFIRMADA,
                LocalDate.of(2026, 7, 13), LocalDate.of(2026, 7, 19))).thenReturn(1L);

        DashboardAlunoResponse response = fx.service.buscarDashboardDoAlunoAtual();

        assertEquals(20L, response.getAlunoId());
        assertEquals(TipoPlano.TURMA, response.getPlano().getTipo());
        assertEquals(1, response.getPlano().getReservasConfirmadasNaSemana());
        assertEquals(2, response.getPlano().getAulasRestantesNaSemana());
    }

    @Test
    void dashboardAlunoComPlanoPersonalMostraHorariosFixos() {
        Fixture fx = fixture(PerfilUsuario.ALUNO, TipoPlano.PERSONAL);
        when(fx.agendaPersonalRepository.findByAcademiaIdAndAlunoIdAndAtivoTrue(1L, 20L)).thenReturn(List.of(agendaPersonal(fx)));

        DashboardAlunoResponse response = fx.service.buscarDashboardDoAlunoAtual();

        assertEquals(TipoPlano.PERSONAL, response.getPlano().getTipo());
        assertEquals(1, response.getHorariosFixosPersonal().size());
    }

    @Test
    void dashboardAlunoSemPlano() {
        Fixture fx = fixture(PerfilUsuario.ALUNO, TipoPlano.TURMA);
        fx.aluno.setPlano(null);

        DashboardAlunoResponse response = fx.service.buscarDashboardDoAlunoAtual();

        assertNull(response.getPlano());
    }

    @Test
    void dashboardIncluiResumoPesoQuandoExisteENullQuandoNaoExiste() {
        Fixture fx = fixture(PerfilUsuario.ALUNO, TipoPlano.TURMA);
        ResumoPesoDashboardResponse resumo = new ResumoPesoDashboardResponse(BigDecimal.valueOf(70),
                HOJE, BigDecimal.valueOf(-2), BigDecimal.valueOf(70), BigDecimal.valueOf(72), 2L);
        when(fx.evolucaoPesoService.buscarResumoDashboard(20L)).thenReturn(resumo);

        assertEquals(BigDecimal.valueOf(70), fx.service.buscarDashboardDoAlunoAtual().getEvolucaoPeso().getPesoAtual());

        Fixture semPeso = fixture(PerfilUsuario.ALUNO, TipoPlano.TURMA);
        assertNull(semPeso.service.buscarDashboardDoAlunoAtual().getEvolucaoPeso());
    }

    @Test
    void dashboardIncluiMetaPesoQuandoExisteENullQuandoNaoExiste() {
        Fixture fx = fixture(PerfilUsuario.ALUNO, TipoPlano.TURMA);
        ProgressoMetaPesoDashboardResponse metaPeso = new ProgressoMetaPesoDashboardResponse(80L,
                BigDecimal.valueOf(70), BigDecimal.valueOf(68), BigDecimal.valueOf(2),
                BigDecimal.valueOf(50), "REDUCAO", false, StatusMetaPeso.ATIVA);
        when(fx.metaPesoService.buscarResumoDashboard(20L)).thenReturn(metaPeso);

        assertEquals(80L, fx.service.buscarDashboardDoAlunoAtual().getMetaPeso().getMetaId());

        Fixture semMeta = fixture(PerfilUsuario.ALUNO, TipoPlano.TURMA);
        assertNull(semMeta.service.buscarDashboardDoAlunoAtual().getMetaPeso());
    }

    @Test
    void calculaPrajiedBaseProximoEProgressoParcial() {
        Fixture fx = fixture(PerfilUsuario.ALUNO, TipoPlano.TURMA);
        when(fx.graduacaoAlunoService.calcularElegibilidade(fx.aluno))
                .thenReturn(elegibilidade(fx, 5, 10, false));

        DashboardAlunoResponse response = fx.service.buscarDashboardDoAlunoAtual();

        assertEquals("Branco", response.getPrajied().getNome());
        assertEquals("Azul", response.getPrajied().getProximoPrajiedNome());
        assertEquals(5, response.getPrajied().getCheckInsFaltantes());
        assertFalse(response.getPrajied().getElegivelParaAvaliacao());
    }

    @Test
    void elegivelParaAvaliacaoQuandoAtingeMeta() {
        Fixture fx = fixture(PerfilUsuario.ALUNO, TipoPlano.TURMA);
        when(fx.graduacaoAlunoService.calcularElegibilidade(fx.aluno))
                .thenReturn(elegibilidade(fx, 10, 10, true));

        assertTrue(fx.service.buscarDashboardDoAlunoAtual().getPrajied().getElegivelParaAvaliacao());
    }

    @Test
    void graduacaoMaisAltaSemProxima() {
        Fixture fx = fixture(PerfilUsuario.ALUNO, TipoPlano.TURMA);
        when(fx.graduacaoAlunoService.calcularElegibilidade(fx.aluno))
                .thenReturn(new ElegibilidadeGraduacaoResponse(20L, fx.atual.getId(), fx.atual.getNome(),
                        null, null, 0, 0, 0, 0, null, null, false, false, false,
                        "Graduação mais alta"));

        DashboardAlunoResponse response = fx.service.buscarDashboardDoAlunoAtual();

        assertNull(response.getPrajied().getProximoPrajiedId());
        assertEquals(0, response.getPrajied().getCheckInsFaltantes());
    }

    @Test
    void resumoCheckinsIgnoraInativosPelosContadoresAtivos() {
        Fixture fx = fixture(PerfilUsuario.ALUNO, TipoPlano.TURMA);
        when(fx.checkInRepository.countByAcademiaIdAndAlunoIdAndAtivoTrue(1L, 20L)).thenReturn(8L);
        when(fx.checkInRepository.countByAcademiaIdAndAlunoIdAndAtivoTrueAndDataHoraBetween(eq(1L), eq(20L), any(), any()))
                .thenReturn(2L, 1L);
        when(fx.checkInRepository.findFirstByAcademiaIdAndAlunoIdAndAtivoTrueOrderByDataHoraDesc(1L, 20L))
                .thenReturn(Optional.of(checkIn(fx)));

        DashboardAlunoResponse response = fx.service.buscarDashboardDoAlunoAtual();

        assertEquals(8, response.getCheckIns().getTotal());
        assertEquals(2, response.getCheckIns().getNoMesAtual());
        assertEquals(1, response.getCheckIns().getNaSemanaAtual());
        assertNotNull(response.getCheckIns().getUltimoCheckInEm());
    }

    @Test
    void financeiroEmDiaEVencidoParcialPixPendente() {
        Fixture fx = fixture(PerfilUsuario.ALUNO, TipoPlano.TURMA);
        Fatura paga = fatura(fx, StatusFatura.PAGA, HOJE.plusDays(5), BigDecimal.valueOf(180));
        when(fx.faturaRepository.findFirstByAcademiaIdAndAlunoIdAndMesReferenciaOrderByDataVencimentoDesc(1L, 20L, HOJE.withDayOfMonth(1)))
                .thenReturn(Optional.of(paga));
        when(fx.pagamentoRepository.somarPagamentosConfirmadosPorFatura(paga.getId())).thenReturn(BigDecimal.valueOf(180));
        assertTrue(fx.service.buscarDashboardDoAlunoAtual().getFinanceiro().getEmDia());

        Fatura vencida = fatura(fx, StatusFatura.PARCIALMENTE_PAGA, HOJE.minusDays(2), BigDecimal.valueOf(180));
        when(fx.faturaRepository.findByAcademiaIdAndAlunoIdAndStatusInOrderByDataVencimentoAsc(eq(1L), eq(20L), any()))
                .thenReturn(List.of(vencida));
        when(fx.pagamentoRepository.somarPagamentosConfirmadosPorFatura(vencida.getId())).thenReturn(BigDecimal.valueOf(80));
        when(fx.pagamentoRepository.findFirstByAcademiaIdAndFaturaIdAndMetodoAndStatusInOrderByCriadoEmDesc(eq(1L), eq(vencida.getId()), eq(MetodoPagamento.PIX), any()))
                .thenReturn(Optional.of(pagamentoPix(vencida)));

        DashboardAlunoResponse response = fx.service.buscarDashboardDoAlunoAtual();

        assertFalse(response.getFinanceiro().getEmDia());
        assertEquals(BigDecimal.valueOf(100), response.getFinanceiro().getSaldo());
        assertTrue(response.getFinanceiro().getPossuiPixPendente());
    }

    @Test
    void pagamentoProcessandoNaoQuitaFatura() {
        Fixture fx = fixture(PerfilUsuario.ALUNO, TipoPlano.TURMA);
        Fatura fatura = fatura(fx, StatusFatura.PENDENTE, HOJE.minusDays(1), BigDecimal.valueOf(180));
        when(fx.faturaRepository.findByAcademiaIdAndAlunoIdAndStatusInOrderByDataVencimentoAsc(eq(1L), eq(20L), any()))
                .thenReturn(List.of(fatura));
        when(fx.pagamentoRepository.somarPagamentosConfirmadosPorFatura(fatura.getId())).thenReturn(BigDecimal.ZERO);
        when(fx.pagamentoRepository.findFirstByAcademiaIdAndFaturaIdAndMetodoAndStatusInOrderByCriadoEmDesc(eq(1L), eq(fatura.getId()), eq(MetodoPagamento.PIX), any()))
                .thenReturn(Optional.of(pagamentoPix(fatura)));

        DashboardAlunoResponse response = fx.service.buscarDashboardDoAlunoAtual();

        assertFalse(response.getFinanceiro().getEmDia());
        assertEquals(BigDecimal.valueOf(180), response.getFinanceiro().getSaldo());
    }

    @Test
    void proximaAulaDeTurmaEPersonalEAulaCanceladaIgnorada() {
        Fixture fx = fixture(PerfilUsuario.ALUNO, TipoPlano.TURMA);
        ReservaAula reserva = reserva(fx, aulaTurma(fx, 10L, HOJE, LocalTime.of(19, 0), StatusAula.AGENDADA));
        ReservaAula cancelada = reserva(fx, aulaTurma(fx, 11L, HOJE, LocalTime.of(18, 0), StatusAula.CANCELADA));
        when(fx.reservaRepository.buscarReservasConfirmadasComAulaNoPeriodo(eq(1L), eq(20L), eq(StatusReserva.CONFIRMADA), any(), any()))
                .thenReturn(List.of(cancelada, reserva));
        when(fx.sessaoCheckInRepository.existsByAcademiaIdAndAulaTurmaIdAndStatusAndExpiraEmAfter(eq(1L), eq(10L), eq(StatusSessaoCheckIn.ABERTA), any()))
                .thenReturn(true);

        DashboardAlunoResponse response = fx.service.buscarDashboardDoAlunoAtual();

        assertEquals(1, response.getAulasHoje().size());
        assertEquals(10L, response.getProximaAula().getAulaId());
        assertTrue(response.getProximaAula().getSessaoCheckInAberta());

        fx.aluno.getPlano().setTipo(TipoPlano.PERSONAL);
        when(fx.reservaRepository.buscarReservasConfirmadasComAulaNoPeriodo(eq(1L), eq(20L), eq(StatusReserva.CONFIRMADA), any(), any()))
                .thenReturn(List.of());
        when(fx.aulaPersonalRepository.findByAcademiaIdAndAlunoIdAndDataBetween(eq(1L), eq(20L), any(), any()))
                .thenReturn(List.of(aulaPersonal(fx, 30L, HOJE, LocalTime.of(20, 0), StatusAula.AGENDADA)));
        assertEquals("PERSONAL", fx.service.buscarDashboardDoAlunoAtual().getProximaAula().getTipo());
    }

    @Test
    void reservaCanceladaIgnoradaPorqueBuscaSomenteConfirmada() {
        Fixture fx = fixture(PerfilUsuario.ALUNO, TipoPlano.TURMA);

        DashboardAlunoResponse response = fx.service.buscarDashboardDoAlunoAtual();

        assertTrue(response.getAulasSemana().isEmpty());
        verify(fx.reservaRepository).buscarReservasConfirmadasComAulaNoPeriodo(eq(1L), eq(20L), eq(StatusReserva.CONFIRMADA), any(), any());
    }

    @Test
    void bloqueiaAlunoDeOutraAcademiaEUsuarioSemVinculo() {
        Fixture fx = fixture(PerfilUsuario.ALUNO, TipoPlano.TURMA);
        when(fx.alunoRepository.findByIdAndAcademiaId(20L, 1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> fx.service.buscarDashboardDoAlunoAtual());

        Fixture semAluno = fixture(PerfilUsuario.ALUNO, TipoPlano.TURMA);
        semAluno.usuario.setAluno(null);
        assertThrows(AccessDeniedException.class, () -> semAluno.service.buscarDashboardDoAlunoAtual());
    }

    @Test
    void adminEProfessorConsultamEndpointOpcional() {
        Fixture admin = fixture(PerfilUsuario.ADMIN, TipoPlano.TURMA);
        assertEquals(20L, admin.service.buscarDashboardDoAluno(20L).getAlunoId());

        Fixture professor = fixture(PerfilUsuario.PROFESSOR, TipoPlano.TURMA);
        assertEquals(20L, professor.service.buscarDashboardDoAluno(20L).getAlunoId());
    }

    @Test
    void consultaNaoAlteraDadosEUsaBuscaEmLoteParaCheckinsDasAulas() {
        Fixture fx = fixture(PerfilUsuario.ALUNO, TipoPlano.TURMA);
        when(fx.reservaRepository.buscarReservasConfirmadasComAulaNoPeriodo(eq(1L), eq(20L), eq(StatusReserva.CONFIRMADA), any(), any()))
                .thenReturn(List.of(reserva(fx, aulaTurma(fx, 10L, HOJE, LocalTime.of(19, 0), StatusAula.AGENDADA))));

        fx.service.buscarDashboardDoAlunoAtual();

        verify(fx.checkInRepository).findByAcademiaIdAndAlunoIdAndAtivoTrueAndChaveCheckInAtivoIn(eq(1L), eq(20L), any());
        verify(fx.alunoRepository, never()).save(any());
        verify(fx.faturaRepository, never()).save(any());
        verify(fx.pagamentoRepository, never()).save(any());
    }

    private Fixture fixture(PerfilUsuario perfil, TipoPlano tipoPlano) {
        AlunoRepository alunoRepository = mock(AlunoRepository.class);
        GraduacaoRepository graduacaoRepository = mock(GraduacaoRepository.class);
        CheckInRepository checkInRepository = mock(CheckInRepository.class);
        ReservaAulaRepository reservaRepository = mock(ReservaAulaRepository.class);
        AulaPersonalRepository aulaPersonalRepository = mock(AulaPersonalRepository.class);
        AgendaPersonalRepository agendaPersonalRepository = mock(AgendaPersonalRepository.class);
        FaturaRepository faturaRepository = mock(FaturaRepository.class);
        PagamentoRepository pagamentoRepository = mock(PagamentoRepository.class);
        SessaoCheckInRepository sessaoCheckInRepository = mock(SessaoCheckInRepository.class);
        PresencaAulaRepository presencaAulaRepository = mock(PresencaAulaRepository.class);
        GraduacaoAlunoService graduacaoAlunoService = mock(GraduacaoAlunoService.class);
        EvolucaoPesoService evolucaoPesoService = mock(EvolucaoPesoService.class);
        MetaPesoService metaPesoService = mock(MetaPesoService.class);
        PermissaoService permissaoService = mock(PermissaoService.class);
        Clock clock = Clock.fixed(HOJE.atTime(12, 0).atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());

        Academia academia = academia();
        Graduacao atual = graduacao(1L, "Branco", 1, 0, academia);
        Graduacao proxima = graduacao(2L, "Azul", 2, 10, academia);
        Aluno aluno = aluno(academia, atual, tipoPlano);
        Usuario usuario = usuario(perfil, academia, aluno);

        when(permissaoService.usuarioAtual()).thenReturn(usuario);
        when(permissaoService.academiaAtualId()).thenReturn(1L);
        when(alunoRepository.findByIdAndAcademiaId(20L, 1L)).thenReturn(Optional.of(aluno));
        when(graduacaoAlunoService.calcularElegibilidade(aluno)).thenReturn(elegibilidade(aluno, atual, proxima, 0, 10, false));
        when(checkInRepository.countByAcademiaIdAndAlunoIdAndAtivoTrue(anyLong(), anyLong())).thenReturn(0L);
        when(checkInRepository.countByAcademiaIdAndAlunoIdAndAtivoTrueAndDataHoraBetween(anyLong(), anyLong(), any(), any())).thenReturn(0L);
        when(checkInRepository.findByAcademiaIdAndAlunoIdAndAtivoTrueAndChaveCheckInAtivoIn(anyLong(), anyLong(), any())).thenReturn(List.of());
        when(reservaRepository.buscarReservasConfirmadasComAulaNoPeriodo(anyLong(), anyLong(), any(), any(), any())).thenReturn(List.of());
        when(aulaPersonalRepository.findByAcademiaIdAndAlunoIdAndDataBetween(anyLong(), anyLong(), any(), any())).thenReturn(List.of());
        when(agendaPersonalRepository.findByAcademiaIdAndAlunoIdAndAtivoTrue(anyLong(), anyLong())).thenReturn(List.of());
        when(faturaRepository.findByAcademiaIdAndAlunoIdAndStatusInOrderByDataVencimentoAsc(anyLong(), anyLong(), any())).thenReturn(List.of());
        when(pagamentoRepository.somarPagamentosConfirmadosPorFatura(anyLong())).thenReturn(BigDecimal.ZERO);
        when(pagamentoRepository.findFirstByAcademiaIdAndFaturaIdAndMetodoAndStatusInOrderByCriadoEmDesc(anyLong(), anyLong(), any(), any())).thenReturn(Optional.empty());
        when(evolucaoPesoService.buscarResumoDashboard(20L)).thenReturn(null);
        when(metaPesoService.buscarResumoDashboard(20L)).thenReturn(null);

        DashboardAlunoService service = new DashboardAlunoService(alunoRepository, graduacaoRepository, checkInRepository,
                reservaRepository, aulaPersonalRepository, agendaPersonalRepository, faturaRepository, pagamentoRepository,
                sessaoCheckInRepository, presencaAulaRepository, graduacaoAlunoService, evolucaoPesoService,
                metaPesoService, permissaoService, clock);
        return new Fixture(service, alunoRepository, graduacaoRepository, checkInRepository, reservaRepository,
                aulaPersonalRepository, agendaPersonalRepository, faturaRepository, pagamentoRepository,
                sessaoCheckInRepository, presencaAulaRepository, graduacaoAlunoService, evolucaoPesoService,
                metaPesoService, permissaoService, academia, aluno, atual, proxima, usuario);
    }

    private Academia academia() {
        Academia academia = new Academia();
        academia.setId(1L);
        academia.setNome("Elite");
        return academia;
    }

    private Graduacao graduacao(Long id, String nome, int ordem, int meta, Academia academia) {
        Graduacao graduacao = new Graduacao();
        graduacao.setId(id);
        graduacao.setNome(nome);
        graduacao.setCorHex("#FFFFFF");
        graduacao.setOrdem(ordem);
        graduacao.setCheckinsNecessarios(meta);
        graduacao.setAtiva(true);
        graduacao.setAcademia(academia);
        return graduacao;
    }

    private Aluno aluno(Academia academia, Graduacao graduacao, TipoPlano tipoPlano) {
        Plano plano = new Plano();
        plano.setId(5L);
        plano.setAcademia(academia);
        plano.setTipo(tipoPlano);
        plano.setQuantidadeAulasSemana(3);
        plano.setValor(BigDecimal.valueOf(180));
        plano.setAtivo(true);

        Aluno aluno = new Aluno();
        aluno.setId(20L);
        aluno.setNome("Julia");
        aluno.setAtivo(true);
        aluno.setAcademia(academia);
        aluno.setGraduacaoEntidade(graduacao);
        aluno.setPlano(plano);
        return aluno;
    }

    private Usuario usuario(PerfilUsuario perfil, Academia academia, Aluno aluno) {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setPerfil(perfil);
        usuario.setAcademia(academia);
        if (perfil == PerfilUsuario.ALUNO) {
            usuario.setAluno(aluno);
        }
        return usuario;
    }

    private Professor professor(Academia academia) {
        Professor professor = new Professor();
        professor.setId(99L);
        professor.setNome("Professor");
        professor.setAcademia(academia);
        return professor;
    }

    private AulaTurma aulaTurma(Fixture fx, Long id, LocalDate data, LocalTime inicio, StatusAula status) {
        Modalidade modalidade = new Modalidade();
        modalidade.setId(8L);
        modalidade.setNome("Muay Thai");
        AulaTurma aula = new AulaTurma();
        aula.setId(id);
        aula.setAcademia(fx.academia);
        aula.setProfessor(professor(fx.academia));
        aula.setModalidade(modalidade);
        aula.setData(data);
        aula.setHoraInicio(inicio);
        aula.setHoraFim(inicio.plusHours(1));
        aula.setStatus(status);
        aula.setCapacidadeMaxima(10);
        return aula;
    }

    private AulaPersonal aulaPersonal(Fixture fx, Long id, LocalDate data, LocalTime inicio, StatusAula status) {
        AulaPersonal aula = new AulaPersonal();
        aula.setId(id);
        aula.setAcademia(fx.academia);
        aula.setAluno(fx.aluno);
        aula.setProfessor(professor(fx.academia));
        aula.setData(data);
        aula.setHoraInicio(inicio);
        aula.setHoraFim(inicio.plusHours(1));
        aula.setStatus(status);
        return aula;
    }

    private ReservaAula reserva(Fixture fx, AulaTurma aula) {
        ReservaAula reserva = new ReservaAula();
        reserva.setId(7L);
        reserva.setAcademia(fx.academia);
        reserva.setAluno(fx.aluno);
        reserva.setAulaTurma(aula);
        reserva.setStatus(StatusReserva.CONFIRMADA);
        reserva.setReservadoEm(HOJE.atTime(8, 0));
        return reserva;
    }

    private AgendaPersonal agendaPersonal(Fixture fx) {
        AgendaPersonal agenda = new AgendaPersonal();
        agenda.setId(15L);
        agenda.setAcademia(fx.academia);
        agenda.setAluno(fx.aluno);
        agenda.setProfessor(professor(fx.academia));
        agenda.setDiaSemana(DayOfWeek.TUESDAY);
        agenda.setHoraInicial(LocalTime.of(9, 0));
        agenda.setHoraFinal(LocalTime.of(10, 0));
        agenda.setDataInicio(HOJE.minusMonths(1));
        agenda.setAtivo(true);
        return agenda;
    }

    private Fatura fatura(Fixture fx, StatusFatura status, LocalDate vencimento, BigDecimal valor) {
        Fatura fatura = new Fatura();
        fatura.setId(vencimento.isBefore(HOJE) ? 31L : 30L);
        fatura.setAcademia(fx.academia);
        fatura.setAluno(fx.aluno);
        fatura.setMesReferencia(HOJE.withDayOfMonth(1));
        fatura.setDataVencimento(vencimento);
        fatura.setValor(valor);
        fatura.setStatus(status);
        return fatura;
    }

    private Pagamento pagamentoPix(Fatura fatura) {
        Pagamento pagamento = new Pagamento();
        pagamento.setId(40L);
        pagamento.setFatura(fatura);
        pagamento.setMetodo(MetodoPagamento.PIX);
        pagamento.setStatus(StatusPagamento.PROCESSANDO);
        pagamento.setDataExpiracao(HOJE.atTime(23, 59));
        return pagamento;
    }

    private CheckIn checkIn(Fixture fx) {
        CheckIn checkIn = new CheckIn();
        checkIn.setId(60L);
        checkIn.setAcademia(fx.academia);
        checkIn.setAluno(fx.aluno);
        checkIn.setAtivo(true);
        checkIn.setDataHora(HOJE.atTime(10, 0));
        return checkIn;
    }

    private ElegibilidadeGraduacaoResponse elegibilidade(Fixture fx, int atuais, int meta, boolean elegivel) {
        return elegibilidade(fx.aluno, fx.atual, fx.proxima, atuais, meta, elegivel);
    }

    private ElegibilidadeGraduacaoResponse elegibilidade(Aluno aluno, Graduacao atual, Graduacao proxima,
                                                        int atuais, int meta, boolean elegivel) {
        return new ElegibilidadeGraduacaoResponse(aluno.getId(), atual.getId(), atual.getNome(),
                proxima.getId(), proxima.getNome(), atuais, meta, Math.max(0, meta - atuais),
                proxima.getTempoMinimoMeses(), HOJE.minusMonths(3), HOJE.minusMonths(3).plusMonths(proxima.getTempoMinimoMeses()),
                atuais >= meta, true, elegivel, elegivel ? "Elegível para avaliação" : "Requisitos pendentes");
    }

    private record Fixture(DashboardAlunoService service,
                           AlunoRepository alunoRepository,
                           GraduacaoRepository graduacaoRepository,
                           CheckInRepository checkInRepository,
                           ReservaAulaRepository reservaRepository,
                           AulaPersonalRepository aulaPersonalRepository,
                           AgendaPersonalRepository agendaPersonalRepository,
                           FaturaRepository faturaRepository,
                           PagamentoRepository pagamentoRepository,
                           SessaoCheckInRepository sessaoCheckInRepository,
                           PresencaAulaRepository presencaAulaRepository,
                           GraduacaoAlunoService graduacaoAlunoService,
                           EvolucaoPesoService evolucaoPesoService,
                           MetaPesoService metaPesoService,
                           PermissaoService permissaoService,
                           Academia academia,
                           Aluno aluno,
                           Graduacao atual,
                           Graduacao proxima,
                           Usuario usuario) {
    }
}
