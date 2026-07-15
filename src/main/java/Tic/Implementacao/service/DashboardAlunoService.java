package Tic.Implementacao.service;

import Tic.Implementacao.dto.dashboard.aluno.*;
import Tic.Implementacao.dto.graduacao.ElegibilidadeGraduacaoResponse;
import Tic.Implementacao.model.*;
import Tic.Implementacao.repository.*;
import Tic.Implementacao.security.PermissaoService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DashboardAlunoService {

    private final AlunoRepository alunoRepository;
    private final GraduacaoRepository graduacaoRepository;
    private final CheckInRepository checkInRepository;
    private final ReservaAulaRepository reservaRepository;
    private final AulaPersonalRepository aulaPersonalRepository;
    private final AgendaPersonalRepository agendaPersonalRepository;
    private final FaturaRepository faturaRepository;
    private final PagamentoRepository pagamentoRepository;
    private final SessaoCheckInRepository sessaoCheckInRepository;
    private final PresencaAulaRepository presencaAulaRepository;
    private final GraduacaoAlunoService graduacaoAlunoService;
    private final EvolucaoPesoService evolucaoPesoService;
    private final MetaPesoService metaPesoService;
    private final AvaliacaoCorporalService avaliacaoCorporalService;
    private final PermissaoService permissaoService;
    private final Clock clock;

    public DashboardAlunoService(AlunoRepository alunoRepository,
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
                                 AvaliacaoCorporalService avaliacaoCorporalService,
                                 PermissaoService permissaoService,
                                 Clock clock) {
        this.alunoRepository = alunoRepository;
        this.graduacaoRepository = graduacaoRepository;
        this.checkInRepository = checkInRepository;
        this.reservaRepository = reservaRepository;
        this.aulaPersonalRepository = aulaPersonalRepository;
        this.agendaPersonalRepository = agendaPersonalRepository;
        this.faturaRepository = faturaRepository;
        this.pagamentoRepository = pagamentoRepository;
        this.sessaoCheckInRepository = sessaoCheckInRepository;
        this.presencaAulaRepository = presencaAulaRepository;
        this.graduacaoAlunoService = graduacaoAlunoService;
        this.evolucaoPesoService = evolucaoPesoService;
        this.metaPesoService = metaPesoService;
        this.avaliacaoCorporalService = avaliacaoCorporalService;
        this.permissaoService = permissaoService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public DashboardAlunoResponse buscarDashboardDoAlunoAtual() {
        Usuario usuario = permissaoService.usuarioAtual();
        if (usuario.getPerfil() != PerfilUsuario.ALUNO || usuario.getAluno() == null) {
            throw new AccessDeniedException("Dashboard disponível apenas para aluno autenticado");
        }
        return montarDashboard(usuario.getAluno().getId());
    }

    @Transactional(readOnly = true)
    public DashboardAlunoResponse buscarDashboardDoAluno(Long alunoId) {
        permissaoService.exigirAdminOuProfessor();
        return montarDashboard(alunoId);
    }

    private DashboardAlunoResponse montarDashboard(Long alunoId) {
        Long academiaId = permissaoService.academiaAtualId();
        Aluno aluno = alunoRepository.findByIdAndAcademiaId(alunoId, academiaId)
                .orElseThrow(() -> new RuntimeException("Aluno não encontrado com ID: " + alunoId));

        LocalDate hoje = LocalDate.now(clock);
        LocalDate inicioSemana = hoje.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate fimSemana = hoje.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        ResumoCheckInResponse checkIns = montarCheckIns(academiaId, aluno, hoje, inicioSemana, fimSemana);
        ResumoPrajiedResponse prajied = montarPrajied(academiaId, aluno, checkIns.getTotal());
        ResumoPlanoAlunoResponse plano = montarPlano(academiaId, aluno, inicioSemana, fimSemana);
        ResumoFinanceiroAlunoResponse financeiro = montarFinanceiro(academiaId, aluno, hoje);
        ResumoPesoDashboardResponse evolucaoPeso = evolucaoPesoService.buscarResumoDashboard(aluno.getId());
        ProgressoMetaPesoDashboardResponse metaPeso = metaPesoService.buscarResumoDashboard(aluno.getId());
        ResumoAvaliacaoCorporalDashboardResponse avaliacaoCorporal = avaliacaoCorporalService.buscarResumoDashboard(aluno.getId());

        List<AulaAlunoResponse> aulasSemana = montarAulasSemana(academiaId, aluno, inicioSemana, fimSemana);
        List<AulaAlunoResponse> aulasHoje = aulasSemana.stream()
                .filter(aula -> hoje.equals(aula.getData()))
                .toList();
        ProximaAulaAlunoResponse proxima = montarProximaAula(academiaId, aulasSemana, hoje);
        List<HorarioPersonalAlunoResponse> horariosPersonal = montarHorariosPersonal(academiaId, aluno);

        return new DashboardAlunoResponse(aluno.getId(), aluno.getNome(), aluno.getAtivo(), prajied, checkIns, plano,
                financeiro, evolucaoPeso, metaPeso, avaliacaoCorporal, proxima, aulasHoje, aulasSemana, horariosPersonal);
    }

    private ResumoCheckInResponse montarCheckIns(Long academiaId, Aluno aluno, LocalDate hoje, LocalDate inicioSemana, LocalDate fimSemana) {
        LocalDateTime inicioMes = hoje.withDayOfMonth(1).atStartOfDay();
        LocalDateTime inicioProximoMes = hoje.withDayOfMonth(1).plusMonths(1).atStartOfDay();
        int total = Math.toIntExact(checkInRepository.countByAcademiaIdAndAlunoIdAndAtivoTrue(academiaId, aluno.getId()));
        int mes = Math.toIntExact(checkInRepository.countByAcademiaIdAndAlunoIdAndAtivoTrueAndDataHoraBetween(
                academiaId, aluno.getId(), inicioMes, inicioProximoMes));
        int semana = Math.toIntExact(checkInRepository.countByAcademiaIdAndAlunoIdAndAtivoTrueAndDataHoraBetween(
                academiaId, aluno.getId(), inicioSemana.atStartOfDay(), fimSemana.plusDays(1).atStartOfDay()));
        LocalDateTime ultimo = checkInRepository.findFirstByAcademiaIdAndAlunoIdAndAtivoTrueOrderByDataHoraDesc(academiaId, aluno.getId())
                .map(CheckIn::getDataHora)
                .orElse(null);
        return new ResumoCheckInResponse(total, mes, semana, ultimo);
    }

    private ResumoPrajiedResponse montarPrajied(Long academiaId, Aluno aluno, int totalCheckIns) {
        Graduacao atual = aluno.getGraduacaoEntidade();
        if (atual == null) {
            atual = graduacaoRepository.findByAcademiaIdAndGraduacaoBaseTrue(academiaId).orElse(null);
        }
        if (atual == null) {
            return null;
        }
        ElegibilidadeGraduacaoResponse elegibilidade = graduacaoAlunoService.calcularElegibilidade(aluno);
        int meta = elegibilidade.getCheckInsNecessarios() == null ? 0 : elegibilidade.getCheckInsNecessarios();
        int checkInsAtuais = elegibilidade.getCheckInsAtuais() == null ? totalCheckIns : elegibilidade.getCheckInsAtuais();
        BigDecimal percentual = meta == 0
                ? BigDecimal.valueOf(100)
                : BigDecimal.valueOf(checkInsAtuais)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(meta), 2, RoundingMode.HALF_UP)
                .min(BigDecimal.valueOf(100));
        return new ResumoPrajiedResponse(atual.getId(), atual.getNome(), atual.getCorHex(), atual.getOrdem(),
                checkInsAtuais, meta, elegibilidade.getCheckInsFaltantes(), elegibilidade.getProximaGraduacaoId(),
                elegibilidade.getProximaGraduacaoNome(), percentual, elegibilidade.getElegivelParaAvaliacao(),
                elegibilidade.getTempoMinimoMeses(), elegibilidade.getDataEntradaGraduacaoAtual(),
                elegibilidade.getDataMinimaElegibilidade(), elegibilidade.getRequisitoCheckInsAtingido(),
                elegibilidade.getRequisitoTempoAtingido(), elegibilidade.getMotivo());
    }

    private ResumoPlanoAlunoResponse montarPlano(Long academiaId, Aluno aluno, LocalDate inicioSemana, LocalDate fimSemana) {
        Plano plano = aluno.getPlano();
        if (plano == null) {
            return null;
        }
        int reservas = 0;
        if (plano.getTipo() == TipoPlano.TURMA) {
            reservas = Math.toIntExact(reservaRepository.countReservasConfirmadasNaSemana(
                    academiaId, aluno.getId(), StatusReserva.CONFIRMADA, inicioSemana, fimSemana));
        }
        int limite = plano.getQuantidadeAulasSemana() == null ? 0 : plano.getQuantidadeAulasSemana();
        return new ResumoPlanoAlunoResponse(plano.getId(), plano.getTipo().name(), plano.getTipo(), limite,
                plano.getValor(), plano.getAtivo(), reservas, Math.max(0, limite - reservas));
    }

    private ResumoFinanceiroAlunoResponse montarFinanceiro(Long academiaId, Aluno aluno, LocalDate hoje) {
        Fatura fatura = selecionarFaturaRelevante(academiaId, aluno, hoje);
        if (fatura == null) {
            return new ResumoFinanceiroAlunoResponse(true, null, null, null, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, "SEM_FATURA", false, null, null);
        }
        BigDecimal pago = pagamentoRepository.somarPagamentosConfirmadosPorFatura(fatura.getId());
        BigDecimal saldo = fatura.getValor().subtract(pago).max(BigDecimal.ZERO);
        Optional<Pagamento> pix = pagamentoRepository.findFirstByAcademiaIdAndFaturaIdAndMetodoAndStatusInOrderByCriadoEmDesc(
                academiaId, fatura.getId(), MetodoPagamento.PIX, List.of(StatusPagamento.PENDENTE, StatusPagamento.PROCESSANDO));
        boolean vencida = fatura.getDataVencimento().isBefore(hoje) && saldo.compareTo(BigDecimal.ZERO) > 0
                && fatura.getStatus() != StatusFatura.PAGA && fatura.getStatus() != StatusFatura.CANCELADA;
        boolean emDia = !vencida && saldo.compareTo(BigDecimal.ZERO) == 0;
        return new ResumoFinanceiroAlunoResponse(emDia, fatura.getId(), fatura.getMesReferencia().toString(),
                fatura.getDataVencimento(), fatura.getValor(), pago, saldo, fatura.getStatus().name(),
                pix.isPresent(), pix.map(Pagamento::getId).orElse(null), pix.map(Pagamento::getDataExpiracao).orElse(null));
    }

    private Fatura selecionarFaturaRelevante(Long academiaId, Aluno aluno, LocalDate hoje) {
        List<Fatura> pendentes = faturaRepository.findByAcademiaIdAndAlunoIdAndStatusInOrderByDataVencimentoAsc(
                academiaId, aluno.getId(), List.of(StatusFatura.VENCIDA, StatusFatura.PARCIALMENTE_PAGA, StatusFatura.PENDENTE));
        Optional<Fatura> vencida = pendentes.stream()
                .filter(fatura -> fatura.getDataVencimento().isBefore(hoje))
                .findFirst();
        if (vencida.isPresent()) {
            return vencida.get();
        }
        return faturaRepository.findFirstByAcademiaIdAndAlunoIdAndMesReferenciaOrderByDataVencimentoDesc(
                academiaId, aluno.getId(), hoje.withDayOfMonth(1))
                .orElse(pendentes.stream().findFirst().orElse(null));
    }

    private List<AulaAlunoResponse> montarAulasSemana(Long academiaId, Aluno aluno, LocalDate inicio, LocalDate fim) {
        List<ReservaAula> reservas = reservaRepository.buscarReservasConfirmadasComAulaNoPeriodo(
                academiaId, aluno.getId(), StatusReserva.CONFIRMADA, inicio, fim);
        List<AulaPersonal> pessoais = aulaPersonalRepository.findByAcademiaIdAndAlunoIdAndDataBetween(academiaId, aluno.getId(), inicio, fim);

        List<String> chaves = new ArrayList<>();
        reservas.forEach(r -> chaves.add(chave("TURMA", r.getAulaTurma().getId(), aluno.getId())));
        pessoais.forEach(a -> chaves.add(chave("PERSONAL", a.getId(), aluno.getId())));
        Map<String, PresencaAula> presencas = chaves.isEmpty() ? Map.of() : presencaAulaRepository
                .findByAcademiaIdAndAlunoIdAndChavePresencaIn(academiaId, aluno.getId(), chaves)
                .stream()
                .collect(Collectors.toMap(PresencaAula::getChavePresenca, Function.identity()));
        Map<String, CheckIn> checkIns = chaves.isEmpty() ? Map.of() : checkInRepository
                .findByAcademiaIdAndAlunoIdAndAtivoTrueAndChaveCheckInAtivoIn(academiaId, aluno.getId(), chaves)
                .stream()
                .collect(Collectors.toMap(CheckIn::getChaveCheckInAtivo, Function.identity()));

        List<AulaAlunoResponse> aulas = new ArrayList<>();
        for (ReservaAula reserva : reservas) {
            AulaTurma aula = reserva.getAulaTurma();
            if (aula.getStatus() == StatusAula.CANCELADA) {
                continue;
            }
            String chave = chave("TURMA", aula.getId(), aluno.getId());
            aulas.add(new AulaAlunoResponse(aula.getId(), "TURMA", aula.getData(), aula.getHoraInicio(), aula.getHoraFim(),
                    aula.getStatus().name(), aula.getModalidade().getNome(), aula.getProfessor().getNome(), true,
                    checkIns.containsKey(chave),
                    presencas.get(chave) == null ? null : presencas.get(chave).getStatus().name()));
        }
        for (AulaPersonal aula : pessoais) {
            if (aula.getStatus() == StatusAula.CANCELADA) {
                continue;
            }
            String chave = chave("PERSONAL", aula.getId(), aluno.getId());
            aulas.add(new AulaAlunoResponse(aula.getId(), "PERSONAL", aula.getData(), aula.getHoraInicio(), aula.getHoraFim(),
                    aula.getStatus().name(), null, aula.getProfessor().getNome(), false,
                    checkIns.containsKey(chave),
                    presencas.get(chave) == null ? null : presencas.get(chave).getStatus().name()));
        }
        aulas.sort(Comparator.comparing(AulaAlunoResponse::getData)
                .thenComparing(AulaAlunoResponse::getHoraInicio)
                .thenComparing(AulaAlunoResponse::getHoraFim));
        return aulas;
    }

    private ProximaAulaAlunoResponse montarProximaAula(Long academiaId, List<AulaAlunoResponse> aulasSemana, LocalDate hoje) {
        LocalDateTime agora = LocalDateTime.now(clock);
        return aulasSemana.stream()
                .filter(aula -> aula.getStatus().equals(StatusAula.AGENDADA.name()))
                .filter(aula -> !LocalDateTime.of(aula.getData(), aula.getHoraFim()).isBefore(agora))
                .findFirst()
                .map(aula -> new ProximaAulaAlunoResponse(aula.getAulaId(), aula.getTipo(), aula.getData(),
                        aula.getHoraInicio(), aula.getHoraFim(), aula.getModalidade(), aula.getProfessorNome(),
                        aula.getReservada(), sessaoAberta(academiaId, aula)))
                .orElse(null);
    }

    private boolean sessaoAberta(Long academiaId, AulaAlunoResponse aula) {
        LocalDateTime agora = LocalDateTime.now(clock);
        if ("TURMA".equals(aula.getTipo())) {
            return sessaoCheckInRepository.existsByAcademiaIdAndAulaTurmaIdAndStatusAndExpiraEmAfter(
                    academiaId, aula.getAulaId(), StatusSessaoCheckIn.ABERTA, agora);
        }
        return sessaoCheckInRepository.existsByAcademiaIdAndAulaPersonalIdAndStatusAndExpiraEmAfter(
                academiaId, aula.getAulaId(), StatusSessaoCheckIn.ABERTA, agora);
    }

    private List<HorarioPersonalAlunoResponse> montarHorariosPersonal(Long academiaId, Aluno aluno) {
        if (aluno.getPlano() == null || aluno.getPlano().getTipo() != TipoPlano.PERSONAL) {
            return List.of();
        }
        return agendaPersonalRepository.findByAcademiaIdAndAlunoIdAndAtivoTrue(academiaId, aluno.getId())
                .stream()
                .map(agenda -> new HorarioPersonalAlunoResponse(agenda.getId(), agenda.getDiaSemana(),
                        agenda.getHoraInicial(), agenda.getHoraFinal(), agenda.getProfessor().getNome(),
                        agenda.getDataInicio(), agenda.getAtivo()))
                .toList();
    }

    private String chave(String tipo, Long aulaId, Long alunoId) {
        return tipo + ":" + aulaId + ":" + alunoId;
    }
}
