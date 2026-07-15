package Tic.Implementacao.service;

import Tic.Implementacao.dto.relatorio.financeiro.*;
import Tic.Implementacao.dto.relatorio.financeiro.anual.*;
import Tic.Implementacao.model.*;
import Tic.Implementacao.repository.AlunoRepository;
import Tic.Implementacao.repository.FaturaRepository;
import Tic.Implementacao.repository.PagamentoRepository;
import Tic.Implementacao.security.PermissaoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RelatorioFinanceiroService {

    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2);

    private final FaturaRepository faturaRepository;
    private final PagamentoRepository pagamentoRepository;
    private final AlunoRepository alunoRepository;
    private final PermissaoService permissaoService;
    private final Clock clock;

    public RelatorioFinanceiroService(FaturaRepository faturaRepository,
                                      PagamentoRepository pagamentoRepository,
                                      AlunoRepository alunoRepository,
                                      PermissaoService permissaoService,
                                      Clock clock) {
        this.faturaRepository = faturaRepository;
        this.pagamentoRepository = pagamentoRepository;
        this.alunoRepository = alunoRepository;
        this.permissaoService = permissaoService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public RelatorioFinanceiroMensalResponse gerarMensal(Integer ano, Integer mes, StatusFatura statusFatura,
                                                          TipoPlano tipoPlano, MetodoPagamento metodoPagamento,
                                                          Long alunoId, Boolean somenteInadimplentes) {
        permissaoService.exigirAdminOuProfessor();
        Periodo periodo = periodo(ano, mes);
        Long academiaId = permissaoService.academiaAtualId();
        LocalDate hoje = LocalDate.now(clock);

        List<Aluno> alunosAtivos = alunoRepository.findByAtivoTrueAndAcademiaId(academiaId);
        List<Fatura> faturasMes = faturaRepository.findMensaisComAlunoPlano(academiaId, periodo.inicio());
        List<Fatura> faturasVencidasBase = faturaRepository.findPossiveisVencidasAntesDe(academiaId, hoje);
        Map<Long, List<Pagamento>> pagamentosPorFatura = pagamentosPorFatura(academiaId, juntarIds(faturasMes, faturasVencidasBase));

        Map<Long, Fatura> faturaMesPorAluno = faturasMes.stream().collect(Collectors.toMap(f -> f.getAluno().getId(), f -> f));
        Map<Long, BigDecimal> debitosVencidosPorAluno = debitosVencidosPorAluno(faturasVencidasBase, pagamentosPorFatura, hoje);
        Set<Long> alunosComDebitoAnterior = faturasVencidasBase.stream()
                .filter(f -> f.getMesReferencia().isBefore(periodo.inicio()))
                .filter(f -> saldo(f, pagamentosPorFatura.get(f.getId())).compareTo(ZERO) > 0)
                .map(f -> f.getAluno().getId())
                .collect(Collectors.toSet());

        List<AlunoFinanceiroMensalResponse> linhas = alunosAtivos.stream()
                .filter(a -> alunoId == null || Objects.equals(a.getId(), alunoId))
                .filter(a -> tipoPlano == null || (a.getPlano() != null && a.getPlano().getTipo() == tipoPlano))
                .map(a -> montarLinhaAluno(a, faturaMesPorAluno.get(a.getId()), pagamentosPorFatura,
                        alunosComDebitoAnterior.contains(a.getId()), debitosVencidosPorAluno.getOrDefault(a.getId(), ZERO), periodo, hoje))
                .filter(l -> statusFatura == null || statusFatura.name().equals(l.getStatusFatura()))
                .filter(l -> metodoPagamento == null || metodoPagamento.name().equals(l.getFormaPagamentoPrincipal()))
                .filter(l -> !Boolean.TRUE.equals(somenteInadimplentes) || Boolean.TRUE.equals(l.getVencida()) || Boolean.TRUE.equals(l.getPossuiDebitoAnterior()))
                .sorted(Comparator.comparing(AlunoFinanceiroMensalResponse::getAlunoNome, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();

        Set<Long> faturaIdsFiltradas = linhas.stream()
                .map(AlunoFinanceiroMensalResponse::getFaturaId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        List<Fatura> faturasFiltradas = faturasMes.stream().filter(f -> faturaIdsFiltradas.contains(f.getId())).toList();
        List<Pagamento> pagamentosFiltrados = faturaIdsFiltradas.stream()
                .flatMap(id -> pagamentosPorFatura.getOrDefault(id, List.of()).stream())
                .filter(p -> p.getTipoCobranca() == TipoCobranca.MENSALIDADE)
                .filter(p -> metodoPagamento == null || p.getMetodo() == metodoPagamento)
                .toList();

        ResumoFinanceiroMensalResponse resumo = montarResumo(linhas, faturasFiltradas, pagamentosFiltrados, hoje);
        return new RelatorioFinanceiroMensalResponse(ano, mes, periodo.competencia(), periodo.inicio(), periodo.fim(),
                resumo, agruparFormaPagamento(pagamentosFiltrados), agruparTipoPlano(linhas), linhas);
    }

    @Transactional(readOnly = true)
    public ResumoFinanceiroMensalResponse gerarResumoMensal(Integer ano, Integer mes) {
        return gerarMensal(ano, mes, null, null, null, null, false).getResumo();
    }

    @Transactional(readOnly = true)
    public List<AlunoInadimplenteResponse> listarInadimplencia(LocalDate dataReferencia) {
        permissaoService.exigirAdminOuProfessor();
        Long academiaId = permissaoService.academiaAtualId();
        LocalDate referencia = dataReferencia == null ? LocalDate.now(clock) : dataReferencia;
        List<Fatura> faturas = faturaRepository.findPossiveisVencidasAntesDe(academiaId, referencia);
        Map<Long, List<Pagamento>> pagamentos = pagamentosPorFatura(academiaId, faturas.stream().map(Fatura::getId).toList());
        Map<Long, List<Fatura>> vencidasPorAluno = faturas.stream()
                .filter(f -> saldo(f, pagamentos.get(f.getId())).compareTo(ZERO) > 0)
                .collect(Collectors.groupingBy(f -> f.getAluno().getId()));
        return vencidasPorAluno.values().stream()
                .map(lista -> inadimplente(lista, pagamentos, referencia))
                .sorted(Comparator.comparing(AlunoInadimplenteResponse::getVencimentoMaisAntigo))
                .toList();
    }

    @Transactional(readOnly = true)
    public byte[] gerarCsvMensal(Integer ano, Integer mes, StatusFatura statusFatura, TipoPlano tipoPlano,
                                 MetodoPagamento metodoPagamento, Long alunoId, Boolean somenteInadimplentes) {
        RelatorioFinanceiroMensalResponse relatorio = gerarMensal(ano, mes, statusFatura, tipoPlano, metodoPagamento, alunoId, somenteInadimplentes);
        StringBuilder csv = new StringBuilder();
        csv.append("Aluno,E-mail,Tipo de plano,Competência,Vencimento,Valor da fatura,Valor pago,Saldo,Status,Vencida,Débito anterior,Forma de pagamento,Data do último pagamento\n");
        relatorio.getAlunos().forEach(a -> csv.append(csv(a.getAlunoNome())).append(',')
                .append(csv(a.getEmail())).append(',')
                .append(csv(a.getTipoPlano())).append(',')
                .append(csv(a.getCompetencia())).append(',')
                .append(csv(a.getVencimento())).append(',')
                .append(csv(a.getValorFatura())).append(',')
                .append(csv(a.getValorPago())).append(',')
                .append(csv(a.getSaldo())).append(',')
                .append(csv(a.getStatusFatura())).append(',')
                .append(csv(a.getVencida())).append(',')
                .append(csv(a.getPossuiDebitoAnterior())).append(',')
                .append(csv(a.getFormaPagamentoPrincipal())).append(',')
                .append(csv(a.getUltimoPagamentoEm())).append('\n'));
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    public String nomeArquivoMensal(Integer ano, Integer mes) {
        return "relatorio-financeiro-%04d-%02d.csv".formatted(ano, mes);
    }

    @Transactional(readOnly = true)
    public RelatorioFinanceiroAnualResponse gerarAnual(Integer ano, TipoPlano tipoPlano,
                                                        MetodoPagamento metodoPagamento, Long alunoId,
                                                        Integer limiteInadimplentes) {
        permissaoService.exigirAdminOuProfessor();
        if (ano == null || ano < 2000 || ano > 2100) {
            throw new RuntimeException("Ano válido é obrigatório");
        }
        int limite = limiteInadimplentes == null ? 10 : limiteInadimplentes;
        if (limite < 1 || limite > 100) {
            throw new RuntimeException("O limite de inadimplentes deve ficar entre 1 e 100");
        }

        Long academiaId = permissaoService.academiaAtualId();
        LocalDate inicio = LocalDate.of(ano, 1, 1);
        LocalDate fimCompetencia = LocalDate.of(ano, 12, 1);
        LocalDate fim = LocalDate.of(ano, 12, 31);
        LocalDate hoje = LocalDate.now(clock);
        LocalDate dataReferencia = ano.equals(hoje.getYear()) ? hoje : fim;

        List<Aluno> alunosAtivos = alunoRepository.findByAtivoTrueAndAcademiaId(academiaId).stream()
                .filter(a -> alunoId == null || Objects.equals(a.getId(), alunoId))
                .filter(a -> tipoPlano == null || (a.getPlano() != null && a.getPlano().getTipo() == tipoPlano))
                .toList();
        Set<Long> alunosPermitidos = alunosAtivos.stream().map(Aluno::getId).collect(Collectors.toSet());

        List<Fatura> faturasAno = faturaRepository.findMensaisDoPeriodoComAlunoPlano(academiaId, inicio, fimCompetencia).stream()
                .filter(f -> alunosPermitidos.contains(f.getAluno().getId()))
                .toList();
        Map<Long, List<Pagamento>> pagamentosPorFatura = pagamentosPorFatura(academiaId, faturasAno.stream().map(Fatura::getId).toList());

        List<Fatura> faturasFiltradas = faturasAno.stream()
                .filter(f -> metodoPagamento == null || pagamentosPorFatura.getOrDefault(f.getId(), List.of()).stream()
                        .anyMatch(p -> p.getStatus() == StatusPagamento.PAGO && p.getMetodo() == metodoPagamento))
                .toList();
        Set<Long> faturasFiltradasIds = faturasFiltradas.stream().map(Fatura::getId).collect(Collectors.toSet());
        List<Pagamento> pagamentosFiltrados = faturasFiltradasIds.stream()
                .flatMap(id -> pagamentosPorFatura.getOrDefault(id, List.of()).stream())
                .filter(p -> p.getTipoCobranca() == TipoCobranca.MENSALIDADE)
                .filter(p -> metodoPagamento == null || p.getMetodo() == metodoPagamento)
                .toList();

        List<ResumoFinanceiroMesResponse> meses = montarMesesAnuais(ano, faturasFiltradas, pagamentosPorFatura,
                dataReferencia, metodoPagamento);
        ResumoFinanceiroAnualResponse resumo = montarResumoAnual(meses, faturasFiltradas, pagamentosFiltrados,
                pagamentosPorFatura, dataReferencia, metodoPagamento, alunosAtivos.size());
        return new RelatorioFinanceiroAnualResponse(ano, inicio, fim, resumo, meses,
                agruparFormaPagamentoAnual(pagamentosFiltrados, resumo.getTotalRecebido()),
                agruparTipoPlanoAnual(faturasFiltradas, pagamentosPorFatura, dataReferencia, metodoPagamento),
                maioresInadimplentesAnual(faturasFiltradas, pagamentosPorFatura, dataReferencia, limite),
                escolherMelhorMes(meses),
                escolherPiorMes(meses),
                escolherMesMaiorInadimplencia(meses));
    }

    @Transactional(readOnly = true)
    public ResumoFinanceiroAnualResponse gerarResumoAnual(Integer ano) {
        return gerarAnual(ano, null, null, null, 10).getResumo();
    }

    @Transactional(readOnly = true)
    public byte[] gerarCsvAnual(Integer ano, TipoPlano tipoPlano, MetodoPagamento metodoPagamento,
                                Long alunoId, Integer limiteInadimplentes) {
        RelatorioFinanceiroAnualResponse relatorio = gerarAnual(ano, tipoPlano, metodoPagamento, alunoId, limiteInadimplentes);
        StringBuilder csv = new StringBuilder();
        csv.append("Mês,Competência,Previsto,Recebido,Pendente,Vencido,Estornado,Receita líquida,Faturas pagas,Faturas pendentes,Faturas vencidas,Alunos inadimplentes,Taxa de recebimento,Taxa de inadimplência\n");
        relatorio.getMeses().forEach(m -> csv.append(csv(m.getNomeMes())).append(',')
                .append(csv(m.getCompetencia())).append(',')
                .append(csv(m.getTotalPrevisto())).append(',')
                .append(csv(m.getTotalRecebido())).append(',')
                .append(csv(m.getTotalPendente())).append(',')
                .append(csv(m.getTotalVencido())).append(',')
                .append(csv(m.getTotalEstornado())).append(',')
                .append(csv(m.getReceitaLiquida())).append(',')
                .append(csv(m.getFaturasPagas())).append(',')
                .append(csv(m.getFaturasPendentes())).append(',')
                .append(csv(m.getFaturasVencidas())).append(',')
                .append(csv(m.getAlunosInadimplentes())).append(',')
                .append(csv(m.getTaxaRecebimento())).append(',')
                .append(csv(m.getTaxaInadimplencia())).append('\n'));
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    public String nomeArquivoAnual(Integer ano) {
        return "relatorio-financeiro-anual-%04d.csv".formatted(ano);
    }

    private List<ResumoFinanceiroMesResponse> montarMesesAnuais(Integer ano, List<Fatura> faturas,
                                                                Map<Long, List<Pagamento>> pagamentosPorFatura,
                                                                LocalDate dataReferencia,
                                                                MetodoPagamento metodoPagamento) {
        Map<Integer, List<Fatura>> faturasPorMes = faturas.stream()
                .collect(Collectors.groupingBy(f -> f.getMesReferencia().getMonthValue()));
        List<ResumoFinanceiroMesResponse> meses = new ArrayList<>();
        BigDecimal recebidoAcumulado = ZERO;
        BigDecimal previstoAcumulado = ZERO;
        BigDecimal vencidoAcumulado = ZERO;
        ResumoFinanceiroMesResponse anterior = null;
        for (int mes = 1; mes <= 12; mes++) {
            List<Fatura> faturasMes = faturasPorMes.getOrDefault(mes, List.of());
            List<Pagamento> pagamentosMes = faturasMes.stream()
                    .flatMap(f -> pagamentosPorFatura.getOrDefault(f.getId(), List.of()).stream())
                    .filter(p -> metodoPagamento == null || p.getMetodo() == metodoPagamento)
                    .toList();
            BigDecimal previsto = somaFaturas(faturasMes.stream().filter(f -> f.getStatus() != StatusFatura.CANCELADA).toList());
            BigDecimal recebido = recebido(pagamentosMes);
            BigDecimal pendente = faturasMes.stream().map(f -> saldo(f, pagamentosMesDaFatura(f, pagamentosPorFatura, metodoPagamento)))
                    .reduce(ZERO, BigDecimal::add);
            BigDecimal vencido = faturasMes.stream()
                    .filter(f -> f.getDataVencimento().isBefore(dataReferencia))
                    .map(f -> saldo(f, pagamentosMesDaFatura(f, pagamentosPorFatura, metodoPagamento)))
                    .reduce(ZERO, BigDecimal::add);
            BigDecimal estornado = pagamentosMes.stream()
                    .filter(p -> p.getStatus() == StatusPagamento.ESTORNADO)
                    .map(Pagamento::getValor)
                    .reduce(ZERO, BigDecimal::add);
            BigDecimal receitaLiquida = normalizar(recebido.subtract(estornado));
            recebidoAcumulado = normalizar(recebidoAcumulado.add(recebido));
            previstoAcumulado = normalizar(previstoAcumulado.add(previsto));
            vencidoAcumulado = normalizar(vencidoAcumulado.add(vencido));

            ResumoFinanceiroMesResponse atual = new ResumoFinanceiroMesResponse(
                    mes,
                    YearMonth.of(ano, mes).getMonth().getDisplayName(TextStyle.FULL, Locale.forLanguageTag("pt-BR")),
                    YearMonth.of(ano, mes).toString(),
                    previsto,
                    recebido,
                    normalizar(pendente),
                    normalizar(vencido),
                    normalizar(estornado),
                    receitaLiquida,
                    count(faturasMes, StatusFatura.PAGA),
                    count(faturasMes, StatusFatura.PENDENTE),
                    count(faturasMes, StatusFatura.VENCIDA),
                    (int) faturasMes.stream()
                            .filter(f -> f.getDataVencimento().isBefore(dataReferencia))
                            .filter(f -> saldo(f, pagamentosMesDaFatura(f, pagamentosPorFatura, metodoPagamento)).compareTo(ZERO) > 0)
                            .map(f -> f.getAluno().getId())
                            .distinct()
                            .count(),
                    percentual(recebido, previsto),
                    percentual(vencido, previsto),
                    anterior == null ? null : variacaoPercentual(recebido, anterior.getTotalRecebido()),
                    anterior == null ? null : variacaoPercentual(previsto, anterior.getTotalPrevisto()),
                    anterior == null ? null : variacaoPercentual(vencido, anterior.getTotalVencido()),
                    recebidoAcumulado,
                    previstoAcumulado,
                    vencidoAcumulado);
            meses.add(atual);
            anterior = atual;
        }
        return meses;
    }

    private ResumoFinanceiroAnualResponse montarResumoAnual(List<ResumoFinanceiroMesResponse> meses, List<Fatura> faturas,
                                                            List<Pagamento> pagamentos,
                                                            Map<Long, List<Pagamento>> pagamentosPorFatura,
                                                            LocalDate dataReferencia,
                                                            MetodoPagamento metodoPagamento,
                                                            int alunosAtivosNoAno) {
        BigDecimal previsto = meses.stream().map(ResumoFinanceiroMesResponse::getTotalPrevisto).reduce(ZERO, BigDecimal::add);
        BigDecimal recebido = meses.stream().map(ResumoFinanceiroMesResponse::getTotalRecebido).reduce(ZERO, BigDecimal::add);
        BigDecimal pendente = meses.stream().map(ResumoFinanceiroMesResponse::getTotalPendente).reduce(ZERO, BigDecimal::add);
        BigDecimal vencido = meses.stream().map(ResumoFinanceiroMesResponse::getTotalVencido).reduce(ZERO, BigDecimal::add);
        BigDecimal estornado = meses.stream().map(ResumoFinanceiroMesResponse::getTotalEstornado).reduce(ZERO, BigDecimal::add);
        BigDecimal parcialmentePago = faturas.stream()
                .filter(f -> f.getStatus() != StatusFatura.CANCELADA)
                .filter(f -> saldo(f, pagamentos.stream().filter(p -> Objects.equals(p.getFatura().getId(), f.getId())).toList()).compareTo(ZERO) > 0)
                .map(f -> recebido(pagamentos.stream().filter(p -> Objects.equals(p.getFatura().getId(), f.getId())).toList()))
                .filter(v -> v.compareTo(ZERO) > 0)
                .reduce(ZERO, BigDecimal::add);
        BigDecimal cancelado = somaFaturas(faturas.stream().filter(f -> f.getStatus() == StatusFatura.CANCELADA).toList());
        int pagosConfirmados = (int) pagamentos.stream().filter(p -> p.getStatus() == StatusPagamento.PAGO).count();
        int alunosComInadimplencia = (int) faturas.stream()
                .filter(f -> f.getDataVencimento().isBefore(dataReferencia))
                .filter(f -> saldo(f, pagamentosMesDaFatura(f, pagamentosPorFatura, metodoPagamento)).compareTo(ZERO) > 0)
                .map(Fatura::getAluno)
                .map(Aluno::getId)
                .distinct()
                .count();
        return new ResumoFinanceiroAnualResponse(
                normalizar(previsto),
                normalizar(recebido),
                normalizar(pendente),
                normalizar(vencido),
                normalizar(parcialmentePago),
                cancelado,
                normalizar(estornado),
                ZERO,
                normalizar(recebido.subtract(estornado)),
                dividir(previsto, BigDecimal.valueOf(12)),
                dividir(recebido, BigDecimal.valueOf(12)),
                pagosConfirmados == 0 ? ZERO : dividir(recebido, BigDecimal.valueOf(pagosConfirmados)),
                percentual(recebido, previsto),
                percentual(vencido, previsto),
                faturas.size(),
                pagosConfirmados,
                alunosAtivosNoAno,
                alunosComInadimplencia);
    }

    private List<ResumoFormaPagamentoAnualResponse> agruparFormaPagamentoAnual(List<Pagamento> pagamentos,
                                                                               BigDecimal totalRecebido) {
        return pagamentos.stream().filter(p -> p.getStatus() == StatusPagamento.PAGO)
                .collect(Collectors.groupingBy(Pagamento::getMetodo, LinkedHashMap::new, Collectors.toList()))
                .entrySet().stream()
                .map(e -> {
                    BigDecimal bruto = e.getValue().stream().map(Pagamento::getValor).reduce(ZERO, BigDecimal::add);
                    return new ResumoFormaPagamentoAnualResponse(e.getKey().name(), e.getValue().size(),
                            normalizar(bruto), ZERO, normalizar(bruto), percentual(bruto, totalRecebido));
                })
                .toList();
    }

    private List<ResumoTipoPlanoAnualResponse> agruparTipoPlanoAnual(List<Fatura> faturas,
                                                                     Map<Long, List<Pagamento>> pagamentosPorFatura,
                                                                     LocalDate dataReferencia,
                                                                     MetodoPagamento metodoPagamento) {
        Map<String, List<Fatura>> grupos = new LinkedHashMap<>();
        grupos.put(TipoPlano.TURMA.name(), new ArrayList<>());
        grupos.put(TipoPlano.PERSONAL.name(), new ArrayList<>());
        grupos.put("SEM_PLANO", new ArrayList<>());
        faturas.stream()
                .collect(Collectors.groupingBy(f -> tipoPlano(f.getAluno()), LinkedHashMap::new, Collectors.toList()))
                .forEach((tipo, lista) -> grupos.computeIfAbsent(tipo, key -> new ArrayList<>()).addAll(lista));
        return grupos.entrySet().stream()
                .map(e -> {
                    List<Fatura> grupo = e.getValue();
                    List<Pagamento> pagamentos = grupo.stream()
                            .flatMap(f -> pagamentosMesDaFatura(f, pagamentosPorFatura, metodoPagamento).stream())
                            .toList();
                    BigDecimal previsto = somaFaturas(grupo.stream().filter(f -> f.getStatus() != StatusFatura.CANCELADA).toList());
                    BigDecimal recebido = recebido(pagamentos);
                    BigDecimal pendente = grupo.stream().map(f -> saldo(f, pagamentosMesDaFatura(f, pagamentosPorFatura, metodoPagamento)))
                            .reduce(ZERO, BigDecimal::add);
                    BigDecimal vencido = grupo.stream()
                            .filter(f -> f.getDataVencimento().isBefore(dataReferencia))
                            .map(f -> saldo(f, pagamentosMesDaFatura(f, pagamentosPorFatura, metodoPagamento)))
                            .reduce(ZERO, BigDecimal::add);
                    int pagamentosConfirmados = (int) pagamentos.stream().filter(p -> p.getStatus() == StatusPagamento.PAGO).count();
                    return new ResumoTipoPlanoAnualResponse(e.getKey(),
                            (int) grupo.stream().map(f -> f.getAluno().getId()).distinct().count(),
                            grupo.size(),
                            previsto,
                            recebido,
                            normalizar(pendente),
                            normalizar(vencido),
                            pagamentosConfirmados == 0 ? ZERO : dividir(recebido, BigDecimal.valueOf(pagamentosConfirmados)),
                            percentual(recebido, previsto),
                            (int) grupo.stream()
                                    .filter(f -> f.getDataVencimento().isBefore(dataReferencia))
                                    .filter(f -> saldo(f, pagamentosMesDaFatura(f, pagamentosPorFatura, metodoPagamento)).compareTo(ZERO) > 0)
                                    .map(f -> f.getAluno().getId())
                                    .distinct()
                                    .count());
                })
                .toList();
    }

    private List<AlunoInadimplenciaAnualResponse> maioresInadimplentesAnual(List<Fatura> faturas,
                                                                            Map<Long, List<Pagamento>> pagamentosPorFatura,
                                                                            LocalDate dataReferencia,
                                                                            int limite) {
        return faturas.stream()
                .filter(f -> f.getDataVencimento().isBefore(dataReferencia))
                .filter(f -> saldo(f, pagamentosPorFatura.get(f.getId())).compareTo(ZERO) > 0)
                .collect(Collectors.groupingBy(f -> f.getAluno().getId()))
                .values().stream()
                .map(lista -> {
                    Aluno aluno = lista.get(0).getAluno();
                    LocalDate antigo = lista.stream().map(Fatura::getDataVencimento).min(LocalDate::compareTo).orElse(dataReferencia);
                    BigDecimal total = lista.stream().map(f -> saldo(f, pagamentosPorFatura.get(f.getId()))).reduce(ZERO, BigDecimal::add);
                    int maiorAtraso = lista.stream()
                            .map(Fatura::getDataVencimento)
                            .mapToInt(v -> Math.toIntExact(ChronoUnit.DAYS.between(v, dataReferencia)))
                            .max()
                            .orElse(0);
                    return new AlunoInadimplenciaAnualResponse(aluno.getId(), aluno.getNome(), aluno.getEmail(), tipoPlano(aluno),
                            lista.size(), normalizar(total), antigo, maiorAtraso);
                })
                .sorted(Comparator.comparing(AlunoInadimplenciaAnualResponse::getTotalVencido).reversed()
                        .thenComparing(AlunoInadimplenciaAnualResponse::getVencimentoMaisAntigo))
                .limit(limite)
                .toList();
    }

    private List<Pagamento> pagamentosMesDaFatura(Fatura fatura, Map<Long, List<Pagamento>> pagamentosPorFatura,
                                                  MetodoPagamento metodoPagamento) {
        return pagamentosPorFatura.getOrDefault(fatura.getId(), List.of()).stream()
                .filter(p -> metodoPagamento == null || p.getMetodo() == metodoPagamento)
                .toList();
    }

    private Integer escolherMelhorMes(List<ResumoFinanceiroMesResponse> meses) {
        return meses.stream()
                .filter(this::temMovimentacao)
                .max(Comparator.comparing(ResumoFinanceiroMesResponse::getTotalRecebido)
                        .thenComparing(m -> -m.getMes()))
                .map(ResumoFinanceiroMesResponse::getMes)
                .orElse(null);
    }

    private Integer escolherPiorMes(List<ResumoFinanceiroMesResponse> meses) {
        return meses.stream()
                .filter(this::temMovimentacao)
                .min(Comparator.comparing(ResumoFinanceiroMesResponse::getTotalRecebido)
                        .thenComparing(ResumoFinanceiroMesResponse::getMes))
                .map(ResumoFinanceiroMesResponse::getMes)
                .orElse(null);
    }

    private Integer escolherMesMaiorInadimplencia(List<ResumoFinanceiroMesResponse> meses) {
        return meses.stream()
                .filter(m -> m.getTotalVencido().compareTo(ZERO) > 0)
                .max(Comparator.comparing(ResumoFinanceiroMesResponse::getTotalVencido)
                        .thenComparing(m -> -m.getMes()))
                .map(ResumoFinanceiroMesResponse::getMes)
                .orElse(null);
    }

    private boolean temMovimentacao(ResumoFinanceiroMesResponse mes) {
        return mes.getTotalPrevisto().compareTo(ZERO) > 0
                || mes.getTotalRecebido().compareTo(ZERO) > 0
                || mes.getTotalPendente().compareTo(ZERO) > 0
                || mes.getTotalVencido().compareTo(ZERO) > 0
                || mes.getTotalEstornado().compareTo(ZERO) > 0;
    }

    private BigDecimal percentual(BigDecimal valor, BigDecimal total) {
        if (total == null || total.compareTo(ZERO) == 0) {
            return ZERO;
        }
        return normalizar(valor.multiply(BigDecimal.valueOf(100)).divide(total, 2, RoundingMode.HALF_UP));
    }

    private BigDecimal variacaoPercentual(BigDecimal atual, BigDecimal anterior) {
        if (anterior == null || anterior.compareTo(ZERO) == 0) {
            return null;
        }
        return normalizar(atual.subtract(anterior).multiply(BigDecimal.valueOf(100)).divide(anterior, 2, RoundingMode.HALF_UP));
    }

    private BigDecimal dividir(BigDecimal valor, BigDecimal divisor) {
        if (divisor == null || divisor.compareTo(ZERO) == 0) {
            return ZERO;
        }
        return normalizar(valor.divide(divisor, 2, RoundingMode.HALF_UP));
    }

    private AlunoFinanceiroMensalResponse montarLinhaAluno(Aluno aluno, Fatura fatura, Map<Long, List<Pagamento>> pagamentosPorFatura,
                                                          boolean possuiDebitoAnterior, BigDecimal debitoVencido,
                                                          Periodo periodo, LocalDate hoje) {
        List<Pagamento> pagamentos = fatura == null ? List.of() : pagamentosPorFatura.getOrDefault(fatura.getId(), List.of());
        BigDecimal pago = recebido(pagamentos);
        BigDecimal saldo = fatura == null ? ZERO : saldo(fatura, pagamentos);
        boolean vencida = fatura != null && fatura.getDataVencimento().isBefore(hoje) && saldo.compareTo(ZERO) > 0
                && fatura.getStatus() != StatusFatura.CANCELADA;
        Optional<Pagamento> ultimoPago = pagamentos.stream()
                .filter(p -> p.getStatus() == StatusPagamento.PAGO)
                .max(Comparator.comparing(p -> p.getPagoEm() == null ? LocalDateTime.MIN : p.getPagoEm()));
        return new AlunoFinanceiroMensalResponse(aluno.getId(), aluno.getNome(), aluno.getEmail(), tipoPlano(aluno),
                aluno.getPlano() == null ? aluno.getValorMensalidade() : aluno.getPlano().getValor(), aluno.getDiaVencimento(),
                fatura == null ? null : fatura.getId(), periodo.competencia(), fatura == null ? null : fatura.getDataVencimento(),
                fatura == null ? ZERO : normalizar(fatura.getValor()), pago, saldo, fatura == null ? "SEM_FATURA" : fatura.getStatus().name(),
                vencida || debitoVencido.compareTo(ZERO) > 0, possuiDebitoAnterior,
                ultimoPago.map(p -> p.getMetodo().name()).orElse(null),
                ultimoPago.map(Pagamento::getPagoEm).orElse(null));
    }

    private ResumoFinanceiroMensalResponse montarResumo(List<AlunoFinanceiroMensalResponse> linhas, List<Fatura> faturas,
                                                       List<Pagamento> pagamentos, LocalDate hoje) {
        BigDecimal recebido = recebido(pagamentos);
        BigDecimal estornado = pagamentos.stream().filter(p -> p.getStatus() == StatusPagamento.ESTORNADO).map(Pagamento::getValor).reduce(ZERO, BigDecimal::add);
        BigDecimal taxas = ZERO;
        BigDecimal parcialRecebido = linhas.stream()
                .filter(l -> l.getFaturaId() != null && l.getSaldo().compareTo(ZERO) > 0 && l.getValorPago().compareTo(ZERO) > 0)
                .map(AlunoFinanceiroMensalResponse::getValorPago).reduce(ZERO, BigDecimal::add);
        return new ResumoFinanceiroMensalResponse(
                somaFaturas(faturas.stream().filter(f -> f.getStatus() != StatusFatura.CANCELADA).toList()),
                recebido,
                linhas.stream().map(AlunoFinanceiroMensalResponse::getSaldo).reduce(ZERO, BigDecimal::add),
                linhas.stream().filter(AlunoFinanceiroMensalResponse::getVencida).map(AlunoFinanceiroMensalResponse::getSaldo).reduce(ZERO, BigDecimal::add),
                normalizar(parcialRecebido),
                somaFaturas(faturas.stream().filter(f -> f.getStatus() == StatusFatura.CANCELADA).toList()),
                normalizar(estornado),
                taxas,
                normalizar(recebido.subtract(taxas).subtract(estornado)),
                faturas.size(),
                count(faturas, StatusFatura.PAGA),
                count(faturas, StatusFatura.PENDENTE),
                count(faturas, StatusFatura.VENCIDA),
                count(faturas, StatusFatura.PARCIALMENTE_PAGA),
                count(faturas, StatusFatura.CANCELADA),
                (int) linhas.stream().filter(l -> !l.getVencida()).count(),
                (int) linhas.stream().filter(AlunoFinanceiroMensalResponse::getVencida).count(),
                (int) linhas.stream().filter(l -> l.getFaturaId() == null).count());
    }

    private List<ResumoFormaPagamentoMensalResponse> agruparFormaPagamento(List<Pagamento> pagamentos) {
        return pagamentos.stream().filter(p -> p.getStatus() == StatusPagamento.PAGO)
                .collect(Collectors.groupingBy(Pagamento::getMetodo, LinkedHashMap::new, Collectors.toList()))
                .entrySet().stream()
                .map(e -> {
                    BigDecimal bruto = e.getValue().stream().map(Pagamento::getValor).reduce(ZERO, BigDecimal::add);
                    return new ResumoFormaPagamentoMensalResponse(e.getKey().name(), e.getValue().size(), normalizar(bruto), ZERO, normalizar(bruto));
                })
                .toList();
    }

    private List<ResumoTipoPlanoMensalResponse> agruparTipoPlano(List<AlunoFinanceiroMensalResponse> linhas) {
        return linhas.stream().collect(Collectors.groupingBy(AlunoFinanceiroMensalResponse::getTipoPlano, LinkedHashMap::new, Collectors.toList()))
                .entrySet().stream()
                .map(e -> new ResumoTipoPlanoMensalResponse(e.getKey(), e.getValue().size(),
                        e.getValue().stream().map(AlunoFinanceiroMensalResponse::getValorFatura).reduce(ZERO, BigDecimal::add),
                        e.getValue().stream().map(AlunoFinanceiroMensalResponse::getValorPago).reduce(ZERO, BigDecimal::add),
                        e.getValue().stream().map(AlunoFinanceiroMensalResponse::getSaldo).reduce(ZERO, BigDecimal::add),
                        (int) e.getValue().stream().filter(AlunoFinanceiroMensalResponse::getVencida).count()))
                .toList();
    }

    private AlunoInadimplenteResponse inadimplente(List<Fatura> faturas, Map<Long, List<Pagamento>> pagamentos, LocalDate ref) {
        Aluno aluno = faturas.get(0).getAluno();
        LocalDate antigo = faturas.stream().map(Fatura::getDataVencimento).min(LocalDate::compareTo).orElse(ref);
        BigDecimal total = faturas.stream().map(f -> saldo(f, pagamentos.get(f.getId()))).reduce(ZERO, BigDecimal::add);
        return new AlunoInadimplenteResponse(aluno.getId(), aluno.getNome(), aluno.getEmail(), faturas.size(), normalizar(total),
                antigo, Math.toIntExact(ChronoUnit.DAYS.between(antigo, ref)));
    }

    private Map<Long, BigDecimal> debitosVencidosPorAluno(List<Fatura> faturas, Map<Long, List<Pagamento>> pagamentos, LocalDate hoje) {
        return faturas.stream()
                .filter(f -> f.getDataVencimento().isBefore(hoje))
                .filter(f -> saldo(f, pagamentos.get(f.getId())).compareTo(ZERO) > 0)
                .collect(Collectors.groupingBy(f -> f.getAluno().getId(),
                        Collectors.reducing(ZERO, f -> saldo(f, pagamentos.get(f.getId())), BigDecimal::add)));
    }

    private Map<Long, List<Pagamento>> pagamentosPorFatura(Long academiaId, List<Long> faturaIds) {
        if (faturaIds == null || faturaIds.isEmpty()) {
            return Map.of();
        }
        return pagamentoRepository.findMensalidadesPorFaturas(academiaId, faturaIds).stream()
                .filter(p -> p.getFatura() != null)
                .filter(p -> p.getTipoCobranca() == TipoCobranca.MENSALIDADE)
                .collect(Collectors.groupingBy(p -> p.getFatura().getId()));
    }

    private List<Long> juntarIds(List<Fatura> a, List<Fatura> b) {
        Set<Long> ids = new LinkedHashSet<>();
        a.forEach(f -> ids.add(f.getId()));
        b.forEach(f -> ids.add(f.getId()));
        return new ArrayList<>(ids);
    }

    private BigDecimal saldo(Fatura fatura, List<Pagamento> pagamentos) {
        if (fatura.getStatus() == StatusFatura.CANCELADA) return ZERO;
        return normalizar(fatura.getValor().subtract(recebido(pagamentos)).max(ZERO));
    }

    private BigDecimal recebido(List<Pagamento> pagamentos) {
        if (pagamentos == null) return ZERO;
        return normalizar(pagamentos.stream().filter(p -> p.getStatus() == StatusPagamento.PAGO)
                .map(Pagamento::getValor).reduce(ZERO, BigDecimal::add));
    }

    private BigDecimal somaFaturas(List<Fatura> faturas) {
        return normalizar(faturas.stream().map(Fatura::getValor).reduce(ZERO, BigDecimal::add));
    }

    private int count(List<Fatura> faturas, StatusFatura status) {
        return (int) faturas.stream().filter(f -> f.getStatus() == status).count();
    }

    private String tipoPlano(Aluno aluno) {
        return aluno.getPlano() == null ? "SEM_PLANO" : aluno.getPlano().getTipo().name();
    }

    private Periodo periodo(Integer ano, Integer mes) {
        if (ano == null || mes == null || mes < 1 || mes > 12) {
            throw new RuntimeException("Ano e mês válidos são obrigatórios");
        }
        YearMonth ym = YearMonth.of(ano, mes);
        return new Periodo(ym.atDay(1), ym.atEndOfMonth(), ym.toString());
    }

    private String csv(Object value) {
        if (value == null) return "";
        String text = value.toString();
        if (text.contains(",") || text.contains("\"") || text.contains("\n")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }

    private BigDecimal normalizar(BigDecimal value) {
        return value == null ? ZERO : value.setScale(2, RoundingMode.HALF_UP);
    }

    private record Periodo(LocalDate inicio, LocalDate fim, String competencia) {
    }
}
