package Tic.Implementacao.service;

import Tic.Implementacao.dto.graduacao.relatorio.*;
import Tic.Implementacao.model.*;
import Tic.Implementacao.repository.*;
import Tic.Implementacao.security.PermissaoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class RelatorioGraduacaoService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final List<StatusInscricaoGraduacao> INSCRICOES_VALIDAS = List.of(
            StatusInscricaoGraduacao.PENDENTE_PAGAMENTO,
            StatusInscricaoGraduacao.PAGAMENTO_PROCESSANDO,
            StatusInscricaoGraduacao.PAGA,
            StatusInscricaoGraduacao.CONFIRMADA,
            StatusInscricaoGraduacao.EM_AVALIACAO,
            StatusInscricaoGraduacao.APROVADA,
            StatusInscricaoGraduacao.REPROVADA,
            StatusInscricaoGraduacao.CONCLUIDA
    );

    private final EventoGraduacaoRepository eventoRepository;
    private final EventoGraduacaoPrajiedRepository prajiedRepository;
    private final ConviteGraduacaoRepository conviteRepository;
    private final InscricaoGraduacaoRepository inscricaoRepository;
    private final PagamentoRepository pagamentoRepository;
    private final AvaliacaoGraduacaoRepository avaliacaoRepository;
    private final PermissaoService permissaoService;

    public RelatorioGraduacaoService(EventoGraduacaoRepository eventoRepository,
                                     EventoGraduacaoPrajiedRepository prajiedRepository,
                                     ConviteGraduacaoRepository conviteRepository,
                                     InscricaoGraduacaoRepository inscricaoRepository,
                                     PagamentoRepository pagamentoRepository,
                                     AvaliacaoGraduacaoRepository avaliacaoRepository,
                                     PermissaoService permissaoService) {
        this.eventoRepository = eventoRepository;
        this.prajiedRepository = prajiedRepository;
        this.conviteRepository = conviteRepository;
        this.inscricaoRepository = inscricaoRepository;
        this.pagamentoRepository = pagamentoRepository;
        this.avaliacaoRepository = avaliacaoRepository;
        this.permissaoService = permissaoService;
    }

    @Transactional(readOnly = true)
    public RelatorioEventoGraduacaoResponse gerarRelatorioEvento(Long eventoId) {
        permissaoService.exigirAdminOuProfessor();
        EventoGraduacao evento = buscarEvento(eventoId);
        DadosRelatorio dados = carregarDados(List.of(evento));
        return resumoEvento(evento, dados);
    }

    @Transactional(readOnly = true)
    public List<RelatorioParticipanteGraduacaoResponse> listarParticipantes(
            Long eventoId, FiltroRelatorioParticipanteGraduacao filtro) {
        permissaoService.exigirAdminOuProfessor();
        EventoGraduacao evento = buscarEvento(eventoId);
        DadosRelatorio dados = carregarDados(List.of(evento));
        return participantes(evento, dados, filtro);
    }

    @Transactional(readOnly = true)
    public byte[] gerarCsvParticipantes(Long eventoId, FiltroRelatorioParticipanteGraduacao filtro) {
        EventoGraduacao evento = buscarEvento(eventoId);
        StringBuilder csv = new StringBuilder();
        csv.append("Aluno,E-mail,Graduação atual,Graduação destino,Status da inscrição,Status do pagamento,Presença,Resultado,Promovido,Valor,Taxa,Valor líquido,Data da inscrição,Data do pagamento,Data da avaliação,Data da promoção\n");
        listarParticipantes(eventoId, filtro).forEach(p -> csv.append(linhaCsv(p)).append('\n'));
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Transactional(readOnly = true)
    public RelatorioGraduacoesPeriodoResponse gerarRelatorioPeriodo(LocalDate inicio, LocalDate fim, Long eventoId,
                                                                    Long graduacaoId, StatusEventoGraduacao statusEvento) {
        permissaoService.exigirAdminOuProfessor();
        Periodo periodo = normalizarPeriodo(inicio, fim);
        List<EventoGraduacao> eventos = eventoRepository.buscarParaRelatorio(
                permissaoService.academiaAtualId(), periodo.inicio(), periodo.fim(), eventoId, statusEvento);
        DadosRelatorio dados = carregarDados(eventos);
        List<ResumoEventoGraduacaoRelatorioResponse> resumos = eventos.stream()
                .filter(evento -> eventoContemGraduacao(evento, dados, graduacaoId))
                .map(evento -> resumoPeriodoEvento(evento, dados, graduacaoId))
                .toList();

        return new RelatorioGraduacoesPeriodoResponse(
                periodo.inicio(),
                periodo.fim(),
                resumos.size(),
                (int) eventos.stream()
                        .filter(evento -> eventoContemGraduacao(evento, dados, graduacaoId))
                        .filter(evento -> evento.getStatus() == StatusEventoGraduacao.CONCLUIDO)
                        .count(),
                somaInt(resumos, ResumoEventoGraduacaoRelatorioResponse::getInscritos),
                somaInt(resumos, ResumoEventoGraduacaoRelatorioResponse::getPagos),
                somaInt(resumos, ResumoEventoGraduacaoRelatorioResponse::getAprovados),
                somaInt(resumos, ResumoEventoGraduacaoRelatorioResponse::getPromovidos),
                somaBig(resumos, ResumoEventoGraduacaoRelatorioResponse::getReceitaBruta),
                ZERO,
                somaBig(resumos, ResumoEventoGraduacaoRelatorioResponse::getReceitaLiquida),
                resumos
        );
    }

    @Transactional(readOnly = true)
    public RelatorioFinanceiroGraduacaoResponse gerarRelatorioFinanceiro(LocalDate inicio, LocalDate fim,
                                                                         Long eventoId, Long graduacaoId) {
        permissaoService.exigirAdminOuProfessor();
        Periodo periodo = normalizarPeriodo(inicio, fim);
        List<EventoGraduacao> eventos = eventoRepository.buscarParaRelatorio(
                permissaoService.academiaAtualId(), periodo.inicio(), periodo.fim(), eventoId, null);
        DadosRelatorio dados = carregarDados(eventos);
        List<LancamentoFinanceiroGraduacaoResponse> lancamentos = dados.pagamentos().stream()
                .filter(p -> graduacaoId == null || p.getInscricaoGraduacao().getGraduacaoDestino().getId().equals(graduacaoId))
                .map(this::lancamento)
                .toList();
        BigDecimal bruto = lancamentos.stream()
                .filter(l -> StatusPagamento.PAGO.name().equals(l.getStatusPagamento()))
                .map(LancamentoFinanceiroGraduacaoResponse::getValorBruto)
                .reduce(ZERO, BigDecimal::add);
        BigDecimal estornado = lancamentos.stream()
                .filter(l -> StatusPagamento.ESTORNADO.name().equals(l.getStatusPagamento()))
                .map(LancamentoFinanceiroGraduacaoResponse::getValorBruto)
                .reduce(ZERO, BigDecimal::add);
        BigDecimal pendente = dados.inscricoes().stream()
                .filter(i -> graduacaoId == null || i.getGraduacaoDestino().getId().equals(graduacaoId))
                .filter(this::inscricaoPendenteFinanceira)
                .map(i -> valor(i.getValor()))
                .reduce(ZERO, BigDecimal::add);

        return new RelatorioFinanceiroGraduacaoResponse(periodo.inicio(), periodo.fim(), bruto, ZERO,
                bruto.subtract(estornado), estornado, pendente, lancamentos);
    }

    public String nomeArquivoCsv(Long eventoId) {
        EventoGraduacao evento = buscarEvento(eventoId);
        String titulo = evento.getTitulo() == null ? "evento" : evento.getTitulo().toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        if (titulo.isBlank()) {
            titulo = "evento";
        }
        return "participantes-graduacao-" + titulo + "-" + evento.getDataEvento() + ".csv";
    }

    private RelatorioEventoGraduacaoResponse resumoEvento(EventoGraduacao evento, DadosRelatorio dados) {
        List<ConviteGraduacao> convites = dados.convitesPorEvento().getOrDefault(evento.getId(), List.of());
        List<InscricaoGraduacao> inscricoes = dados.inscricoesPorEvento().getOrDefault(evento.getId(), List.of());
        List<AvaliacaoGraduacao> avaliacoes = dados.avaliacoesPorEvento().getOrDefault(evento.getId(), List.of());
        List<Pagamento> pagamentos = dados.pagamentosPorEvento().getOrDefault(evento.getId(), List.of());
        FinanceiroResumo financeiro = financeiro(inscricoes, pagamentos);
        int presentes = count(avaliacoes, a -> a.getStatusPresenca() == StatusPresencaGraduacao.PRESENTE);
        int aprovados = count(avaliacoes, a -> a.getResultado() == ResultadoAvaliacaoGraduacao.APROVADO);
        int promovidos = count(avaliacoes, a -> Boolean.TRUE.equals(a.getPromocaoConfirmada()));

        return new RelatorioEventoGraduacaoResponse(
                evento.getId(),
                evento.getTitulo(),
                evento.getDataEvento(),
                evento.getLocal(),
                evento.getStatus(),
                convites.size(),
                count(convites, c -> c.getStatus() == StatusConviteGraduacao.PENDENTE || c.getStatus() == StatusConviteGraduacao.VISUALIZADO),
                count(convites, c -> c.getStatus() == StatusConviteGraduacao.ACEITO),
                count(convites, c -> c.getStatus() == StatusConviteGraduacao.RECUSADO),
                inscricoes.size(),
                count(inscricoes, this::inscricaoPagaOperacional),
                count(inscricoes, this::inscricaoPendenteFinanceira),
                count(inscricoes, i -> i.getStatus() == StatusInscricaoGraduacao.CANCELADA),
                count(inscricoes, i -> i.getStatus() == StatusInscricaoGraduacao.EXPIRADA),
                presentes,
                count(avaliacoes, a -> a.getStatusPresenca() == StatusPresencaGraduacao.AUSENTE),
                count(avaliacoes, a -> a.getResultado() == ResultadoAvaliacaoGraduacao.PENDENTE),
                aprovados,
                count(avaliacoes, a -> a.getResultado() == ResultadoAvaliacaoGraduacao.REPROVADO),
                promovidos,
                aprovados - promovidos,
                financeiro.receitaBruta(),
                financeiro.taxas(),
                financeiro.receitaLiquida(),
                financeiro.pendente(),
                financeiro.estornado(),
                indicadores(convites, inscricoes, avaliacoes),
                porPrajied(evento, dados)
        );
    }

    private List<RelatorioPrajiedEventoResponse> porPrajied(EventoGraduacao evento, DadosRelatorio dados) {
        List<EventoGraduacaoPrajied> itens = dados.prajiedsPorEvento().getOrDefault(evento.getId(), List.of());
        List<ConviteGraduacao> convites = dados.convitesPorEvento().getOrDefault(evento.getId(), List.of());
        List<InscricaoGraduacao> inscricoes = dados.inscricoesPorEvento().getOrDefault(evento.getId(), List.of());
        List<AvaliacaoGraduacao> avaliacoes = dados.avaliacoesPorEvento().getOrDefault(evento.getId(), List.of());
        List<Pagamento> pagamentos = dados.pagamentosPorEvento().getOrDefault(evento.getId(), List.of());

        return itens.stream().map(item -> {
            Long graduacaoId = item.getGraduacaoDestino().getId();
            List<InscricaoGraduacao> inscricoesPrajied = filtrarPorGraduacao(inscricoes, graduacaoId);
            List<AvaliacaoGraduacao> avaliacoesPrajied = avaliacoes.stream()
                    .filter(a -> a.getGraduacaoDestino().getId().equals(graduacaoId))
                    .toList();
            FinanceiroResumo financeiro = financeiro(inscricoesPrajied, filtrarPagamentoPorGraduacao(pagamentos, graduacaoId));
            return new RelatorioPrajiedEventoResponse(
                    graduacaoId,
                    item.getGraduacaoDestino().getNome(),
                    item.getGraduacaoDestino().getCorHex(),
                    valor(item.getValor()),
                    count(convites, c -> c.getGraduacaoDestino().getId().equals(graduacaoId)),
                    inscricoesPrajied.size(),
                    count(inscricoesPrajied, this::inscricaoPagaOperacional),
                    count(avaliacoesPrajied, a -> a.getStatusPresenca() == StatusPresencaGraduacao.PRESENTE),
                    count(avaliacoesPrajied, a -> a.getResultado() == ResultadoAvaliacaoGraduacao.APROVADO),
                    count(avaliacoesPrajied, a -> a.getResultado() == ResultadoAvaliacaoGraduacao.REPROVADO),
                    count(avaliacoesPrajied, a -> Boolean.TRUE.equals(a.getPromocaoConfirmada())),
                    financeiro.receitaBruta(),
                    financeiro.receitaLiquida()
            );
        }).toList();
    }

    private List<RelatorioParticipanteGraduacaoResponse> participantes(EventoGraduacao evento, DadosRelatorio dados,
                                                                       FiltroRelatorioParticipanteGraduacao filtro) {
        Map<Long, ConviteGraduacao> convitesPorAluno = dados.convitesPorEvento().getOrDefault(evento.getId(), List.of())
                .stream()
                .collect(Collectors.toMap(c -> c.getAluno().getId(), Function.identity(), (a, b) -> a));
        Map<Long, AvaliacaoGraduacao> avaliacaoPorInscricao = dados.avaliacoesPorEvento().getOrDefault(evento.getId(), List.of())
                .stream()
                .filter(a -> a.getInscricao() != null)
                .collect(Collectors.toMap(a -> a.getInscricao().getId(), Function.identity(), (a, b) -> a));
        Map<Long, Pagamento> pagamentoPorInscricao = pagamentoPrincipalPorInscricao(dados.pagamentosPorEvento().getOrDefault(evento.getId(), List.of()));

        return dados.inscricoesPorEvento().getOrDefault(evento.getId(), List.of()).stream()
                .filter(i -> filtro.getGraduacaoDestinoId() == null || i.getGraduacaoDestino().getId().equals(filtro.getGraduacaoDestinoId()))
                .filter(i -> filtro.getStatusInscricao() == null || i.getStatus() == filtro.getStatusInscricao())
                .filter(i -> filtro.getStatusPagamento() == null || Optional.ofNullable(pagamentoPorInscricao.get(i.getId())).map(Pagamento::getStatus).orElse(null) == filtro.getStatusPagamento())
                .filter(i -> filtro.getStatusPresenca() == null || Optional.ofNullable(avaliacaoPorInscricao.get(i.getId())).map(AvaliacaoGraduacao::getStatusPresenca).orElse(StatusPresencaGraduacao.PENDENTE) == filtro.getStatusPresenca())
                .filter(i -> filtro.getResultadoAvaliacao() == null || Optional.ofNullable(avaliacaoPorInscricao.get(i.getId())).map(AvaliacaoGraduacao::getResultado).orElse(ResultadoAvaliacaoGraduacao.PENDENTE) == filtro.getResultadoAvaliacao())
                .filter(i -> filtro.getPromovido() == null || Optional.ofNullable(avaliacaoPorInscricao.get(i.getId())).map(a -> Boolean.TRUE.equals(a.getPromocaoConfirmada())).orElse(false).equals(filtro.getPromovido()))
                .map(i -> participante(i, convitesPorAluno.get(i.getAluno().getId()), pagamentoPorInscricao.get(i.getId()), avaliacaoPorInscricao.get(i.getId())))
                .toList();
    }

    private RelatorioParticipanteGraduacaoResponse participante(InscricaoGraduacao inscricao, ConviteGraduacao convite,
                                                               Pagamento pagamento, AvaliacaoGraduacao avaliacao) {
        BigDecimal taxa = ZERO;
        BigDecimal pago = pagamento != null && pagamento.getStatus() == StatusPagamento.PAGO ? valor(pagamento.getValor()) : ZERO;
        BigDecimal estorno = pagamento != null && pagamento.getStatus() == StatusPagamento.ESTORNADO ? valor(pagamento.getValor()) : ZERO;
        BigDecimal liquido = pago.subtract(taxa).subtract(estorno);
        return new RelatorioParticipanteGraduacaoResponse(
                inscricao.getAluno().getId(),
                inscricao.getAluno().getNome(),
                inscricao.getAluno().getEmail(),
                nomeGraduacao(inscricao.getAluno().getGraduacaoEntidade()),
                nomeGraduacao(inscricao.getGraduacaoDestino()),
                convite == null ? null : convite.getStatus().name(),
                inscricao.getStatus().name(),
                pagamento == null ? null : pagamento.getStatus().name(),
                avaliacao == null ? StatusPresencaGraduacao.PENDENTE.name() : avaliacao.getStatusPresenca().name(),
                avaliacao == null ? ResultadoAvaliacaoGraduacao.PENDENTE.name() : avaliacao.getResultado().name(),
                avaliacao != null && Boolean.TRUE.equals(avaliacao.getPromocaoConfirmada()),
                valor(inscricao.getValor()),
                taxa,
                liquido,
                inscricao.getInscritoEm(),
                pagamento == null ? null : pagamento.getPagoEm(),
                avaliacao == null ? null : avaliacao.getAvaliadoEm(),
                avaliacao == null ? null : avaliacao.getPromovidoEm()
        );
    }

    private ResumoEventoGraduacaoRelatorioResponse resumoPeriodoEvento(EventoGraduacao evento, DadosRelatorio dados, Long graduacaoId) {
        List<InscricaoGraduacao> inscricoes = filtrarPorGraduacao(
                dados.inscricoesPorEvento().getOrDefault(evento.getId(), List.of()), graduacaoId);
        List<AvaliacaoGraduacao> avaliacoes = filtrarAvaliacaoPorGraduacao(
                dados.avaliacoesPorEvento().getOrDefault(evento.getId(), List.of()), graduacaoId);
        List<Pagamento> pagamentos = filtrarPagamentoPorGraduacao(
                dados.pagamentosPorEvento().getOrDefault(evento.getId(), List.of()), graduacaoId);
        FinanceiroResumo financeiro = financeiro(inscricoes, pagamentos);
        return new ResumoEventoGraduacaoRelatorioResponse(
                evento.getId(),
                evento.getTitulo(),
                evento.getDataEvento(),
                evento.getStatus().name(),
                inscricoes.size(),
                count(inscricoes, this::inscricaoPagaOperacional),
                count(avaliacoes, a -> a.getStatusPresenca() == StatusPresencaGraduacao.PRESENTE),
                count(avaliacoes, a -> a.getResultado() == ResultadoAvaliacaoGraduacao.APROVADO),
                count(avaliacoes, a -> Boolean.TRUE.equals(a.getPromocaoConfirmada())),
                financeiro.receitaBruta(),
                financeiro.receitaLiquida()
        );
    }

    private LancamentoFinanceiroGraduacaoResponse lancamento(Pagamento pagamento) {
        InscricaoGraduacao inscricao = pagamento.getInscricaoGraduacao();
        BigDecimal taxa = ZERO;
        BigDecimal bruto = valor(pagamento.getValor());
        BigDecimal liquido = pagamento.getStatus() == StatusPagamento.PAGO ? bruto.subtract(taxa) : ZERO;
        if (pagamento.getStatus() == StatusPagamento.ESTORNADO) {
            liquido = bruto.negate();
        }
        return new LancamentoFinanceiroGraduacaoResponse(
                pagamento.getId(),
                inscricao.getId(),
                inscricao.getEvento().getId(),
                inscricao.getEvento().getTitulo(),
                inscricao.getAluno().getId(),
                inscricao.getAluno().getNome(),
                inscricao.getGraduacaoDestino().getNome(),
                bruto,
                taxa,
                liquido,
                pagamento.getStatus().name(),
                pagamento.getMetodo() == null ? null : pagamento.getMetodo().name(),
                pagamento.getProvedor(),
                pagamento.getIdPagamentoExterno(),
                pagamento.getPagoEm()
        );
    }

    private DadosRelatorio carregarDados(List<EventoGraduacao> eventos) {
        if (eventos.isEmpty()) {
            return DadosRelatorio.vazio();
        }
        Long academiaId = permissaoService.academiaAtualId();
        List<Long> eventoIds = eventos.stream().map(EventoGraduacao::getId).toList();
        List<EventoGraduacaoPrajied> prajieds = prajiedRepository.findAtivosByEventoIds(eventoIds);
        List<ConviteGraduacao> convites = conviteRepository.findByAcademiaIdAndEventoIdIn(academiaId, eventoIds);
        List<InscricaoGraduacao> inscricoes = inscricaoRepository.findByAcademiaIdAndEventoIdIn(academiaId, eventoIds);
        List<AvaliacaoGraduacao> avaliacoes = avaliacaoRepository.findByAcademiaIdAndEventoIdIn(academiaId, eventoIds);
        List<Pagamento> pagamentos = pagamentoRepository.findPagamentosGraduacaoPorEventos(academiaId, eventoIds);
        return new DadosRelatorio(
                eventos,
                prajieds,
                convites,
                inscricoes,
                avaliacoes,
                pagamentos,
                agruparPorEvento(prajieds, p -> p.getEvento().getId()),
                agruparPorEvento(convites, c -> c.getEvento().getId()),
                agruparPorEvento(inscricoes, i -> i.getEvento().getId()),
                agruparPorEvento(avaliacoes, a -> a.getEvento().getId()),
                agruparPorEvento(pagamentos, p -> p.getInscricaoGraduacao().getEvento().getId())
        );
    }

    private EventoGraduacao buscarEvento(Long eventoId) {
        return eventoRepository.findByIdAndAcademiaId(eventoId, permissaoService.academiaAtualId())
                .orElseThrow(() -> new RuntimeException("Evento de graduação não encontrado com ID: " + eventoId));
    }

    private FinanceiroResumo financeiro(List<InscricaoGraduacao> inscricoes, List<Pagamento> pagamentos) {
        BigDecimal bruto = pagamentos.stream()
                .filter(p -> p.getStatus() == StatusPagamento.PAGO)
                .map(p -> valor(p.getValor()))
                .reduce(ZERO, BigDecimal::add);
        BigDecimal estornado = pagamentos.stream()
                .filter(p -> p.getStatus() == StatusPagamento.ESTORNADO)
                .map(p -> valor(p.getValor()))
                .reduce(ZERO, BigDecimal::add);
        BigDecimal pendente = inscricoes.stream()
                .filter(this::inscricaoPendenteFinanceira)
                .map(i -> valor(i.getValor()))
                .reduce(ZERO, BigDecimal::add);
        return new FinanceiroResumo(bruto, ZERO, bruto.subtract(estornado), pendente, estornado);
    }

    private IndicadoresEventoGraduacaoResponse indicadores(List<ConviteGraduacao> convites,
                                                           List<InscricaoGraduacao> inscricoes,
                                                           List<AvaliacaoGraduacao> avaliacoes) {
        int convidados = convites.size();
        int aceitos = count(convites, c -> c.getStatus() == StatusConviteGraduacao.ACEITO);
        int inscricoesValorPositivo = count(inscricoes, i -> valor(i.getValor()).compareTo(ZERO) > 0);
        int pagos = count(inscricoes, this::inscricaoPagaOperacional);
        int inscricoesValidas = count(inscricoes, i -> INSCRICOES_VALIDAS.contains(i.getStatus()));
        int presentes = count(avaliacoes, a -> a.getStatusPresenca() == StatusPresencaGraduacao.PRESENTE);
        int presentesAvaliados = count(avaliacoes, a -> a.getStatusPresenca() == StatusPresencaGraduacao.PRESENTE
                && (a.getResultado() == ResultadoAvaliacaoGraduacao.APROVADO || a.getResultado() == ResultadoAvaliacaoGraduacao.REPROVADO));
        int aprovados = count(avaliacoes, a -> a.getResultado() == ResultadoAvaliacaoGraduacao.APROVADO);
        int promovidos = count(avaliacoes, a -> Boolean.TRUE.equals(a.getPromocaoConfirmada()));
        return new IndicadoresEventoGraduacaoResponse(
                percentual(aceitos, convidados),
                percentual(inscricoes.size(), convidados),
                percentual(pagos, inscricoesValorPositivo),
                percentual(presentes, inscricoesValidas),
                percentual(aprovados, presentesAvaliados),
                percentual(promovidos, aprovados)
        );
    }

    private boolean inscricaoPagaOperacional(InscricaoGraduacao inscricao) {
        return inscricao.getStatus() == StatusInscricaoGraduacao.PAGA
                || inscricao.getStatus() == StatusInscricaoGraduacao.CONFIRMADA
                || (inscricao.getPagamento() != null && inscricao.getPagamento().getStatus() == StatusPagamento.PAGO);
    }

    private boolean inscricaoPendenteFinanceira(InscricaoGraduacao inscricao) {
        return valor(inscricao.getValor()).compareTo(ZERO) > 0
                && (inscricao.getStatus() == StatusInscricaoGraduacao.PENDENTE_PAGAMENTO
                || inscricao.getStatus() == StatusInscricaoGraduacao.PAGAMENTO_PROCESSANDO);
    }

    private Map<Long, Pagamento> pagamentoPrincipalPorInscricao(List<Pagamento> pagamentos) {
        return pagamentos.stream()
                .sorted(Comparator.comparing((Pagamento p) -> p.getStatus() == StatusPagamento.PAGO ? 0 : 1)
                        .thenComparing(Pagamento::getCriadoEm, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toMap(p -> p.getInscricaoGraduacao().getId(), Function.identity(), (a, b) -> a));
    }

    private <T> Map<Long, List<T>> agruparPorEvento(List<T> itens, Function<T, Long> eventoId) {
        return itens.stream().collect(Collectors.groupingBy(eventoId));
    }

    private <T> int count(List<T> itens, java.util.function.Predicate<T> predicate) {
        return (int) itens.stream().filter(predicate).count();
    }

    private List<InscricaoGraduacao> filtrarPorGraduacao(List<InscricaoGraduacao> inscricoes, Long graduacaoId) {
        if (graduacaoId == null) {
            return inscricoes;
        }
        return inscricoes.stream().filter(i -> i.getGraduacaoDestino().getId().equals(graduacaoId)).toList();
    }

    private List<AvaliacaoGraduacao> filtrarAvaliacaoPorGraduacao(List<AvaliacaoGraduacao> avaliacoes, Long graduacaoId) {
        if (graduacaoId == null) {
            return avaliacoes;
        }
        return avaliacoes.stream().filter(a -> a.getGraduacaoDestino().getId().equals(graduacaoId)).toList();
    }

    private List<Pagamento> filtrarPagamentoPorGraduacao(List<Pagamento> pagamentos, Long graduacaoId) {
        if (graduacaoId == null) {
            return pagamentos;
        }
        return pagamentos.stream()
                .filter(p -> p.getInscricaoGraduacao().getGraduacaoDestino().getId().equals(graduacaoId))
                .toList();
    }

    private boolean eventoContemGraduacao(EventoGraduacao evento, DadosRelatorio dados, Long graduacaoId) {
        if (graduacaoId == null) {
            return true;
        }
        return dados.prajiedsPorEvento().getOrDefault(evento.getId(), List.of()).stream()
                .anyMatch(p -> p.getGraduacaoDestino().getId().equals(graduacaoId))
                || dados.inscricoesPorEvento().getOrDefault(evento.getId(), List.of()).stream()
                .anyMatch(i -> i.getGraduacaoDestino().getId().equals(graduacaoId));
    }

    private BigDecimal valor(BigDecimal valor) {
        return valor == null ? ZERO : valor;
    }

    private BigDecimal percentual(int numerador, int denominador) {
        if (denominador <= 0) {
            return ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(numerador)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(denominador), 2, RoundingMode.HALF_UP);
    }

    private Periodo normalizarPeriodo(LocalDate inicio, LocalDate fim) {
        LocalDate hoje = LocalDate.now();
        LocalDate inicioFinal = inicio == null ? hoje.withDayOfMonth(1) : inicio;
        LocalDate fimFinal = fim == null ? hoje : fim;
        if (fimFinal.isBefore(inicioFinal)) {
            throw new RuntimeException("Data final não pode ser anterior à data inicial");
        }
        return new Periodo(inicioFinal, fimFinal);
    }

    private int somaInt(List<ResumoEventoGraduacaoRelatorioResponse> itens,
                        Function<ResumoEventoGraduacaoRelatorioResponse, Integer> mapper) {
        return itens.stream().map(mapper).filter(Objects::nonNull).reduce(0, Integer::sum);
    }

    private BigDecimal somaBig(List<ResumoEventoGraduacaoRelatorioResponse> itens,
                               Function<ResumoEventoGraduacaoRelatorioResponse, BigDecimal> mapper) {
        return itens.stream().map(mapper).filter(Objects::nonNull).reduce(ZERO, BigDecimal::add);
    }

    private String nomeGraduacao(Graduacao graduacao) {
        return graduacao == null ? null : graduacao.getNome();
    }

    private String linhaCsv(RelatorioParticipanteGraduacaoResponse p) {
        return String.join(",",
                csv(p.getAlunoNome()),
                csv(p.getEmail()),
                csv(p.getGraduacaoAtual()),
                csv(p.getGraduacaoDestino()),
                csv(p.getStatusInscricao()),
                csv(p.getStatusPagamento()),
                csv(p.getStatusPresenca()),
                csv(p.getResultadoAvaliacao()),
                csv(String.valueOf(Boolean.TRUE.equals(p.getPromovido()))),
                csv(p.getValorInscricao()),
                csv(p.getTaxaPagamento()),
                csv(p.getValorLiquido()),
                csv(p.getInscritoEm()),
                csv(p.getPagoEm()),
                csv(p.getAvaliadoEm()),
                csv(p.getPromovidoEm())
        );
    }

    private String csv(Object valor) {
        String texto = valor == null ? "" : valor.toString();
        return "\"" + texto.replace("\"", "\"\"") + "\"";
    }

    private record Periodo(LocalDate inicio, LocalDate fim) {
    }

    private record FinanceiroResumo(BigDecimal receitaBruta, BigDecimal taxas, BigDecimal receitaLiquida,
                                    BigDecimal pendente, BigDecimal estornado) {
    }

    private record DadosRelatorio(List<EventoGraduacao> eventos,
                                  List<EventoGraduacaoPrajied> prajieds,
                                  List<ConviteGraduacao> convites,
                                  List<InscricaoGraduacao> inscricoes,
                                  List<AvaliacaoGraduacao> avaliacoes,
                                  List<Pagamento> pagamentos,
                                  Map<Long, List<EventoGraduacaoPrajied>> prajiedsPorEvento,
                                  Map<Long, List<ConviteGraduacao>> convitesPorEvento,
                                  Map<Long, List<InscricaoGraduacao>> inscricoesPorEvento,
                                  Map<Long, List<AvaliacaoGraduacao>> avaliacoesPorEvento,
                                  Map<Long, List<Pagamento>> pagamentosPorEvento) {
        private static DadosRelatorio vazio() {
            return new DadosRelatorio(List.of(), List.of(), List.of(), List.of(), List.of(), List.of(),
                    Map.of(), Map.of(), Map.of(), Map.of(), Map.of());
        }
    }
}
