package Tic.Implementacao.service;

import Tic.Implementacao.dto.relatorio.financeiro.contabil.*;
import Tic.Implementacao.model.*;
import Tic.Implementacao.repository.PagamentoRepository;
import Tic.Implementacao.security.PermissaoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RelatorioContabilService {

    public static final String AVISO = "Relatório gerencial de apoio contábil. Não substitui documentos fiscais, declarações oficiais ou validação por profissional de contabilidade.";
    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2);

    private final PagamentoRepository pagamentoRepository;
    private final PermissaoService permissaoService;

    public RelatorioContabilService(PagamentoRepository pagamentoRepository, PermissaoService permissaoService) {
        this.pagamentoRepository = pagamentoRepository;
        this.permissaoService = permissaoService;
    }

    @Transactional(readOnly = true)
    public RelatorioContabilResponse gerar(LocalDate inicio, LocalDate fim, TipoCobranca naturezaReceita,
                                           MetodoPagamento metodoPagamento, StatusPagamento statusPagamento,
                                           Long alunoId, Long eventoGraduacaoId, String competencia) {
        permissaoService.exigirAdmin();
        Periodo periodo = periodo(inicio, fim);
        Academia academia = permissaoService.academiaAtual();
        LocalDate competenciaFiltro = parseCompetencia(competencia);
        List<LancamentoContabilResponse> lancamentos = carregarLancamentos(periodo, naturezaReceita, metodoPagamento,
                statusPagamento, alunoId, eventoGraduacaoId, competenciaFiltro);
        return new RelatorioContabilResponse(periodo.inicio(), periodo.fim(), academia.getNome(), academia.getCpfOuCnpj(),
                resumo(lancamentos), porNatureza(lancamentos), porMetodo(lancamentos), lancamentos, AVISO);
    }

    @Transactional(readOnly = true)
    public List<ResumoRecebimentosAlunoResponse> recebimentosPorAluno(Integer ano, TipoCobranca naturezaReceita) {
        permissaoService.exigirAdmin();
        if (ano == null || ano < 2000 || ano > 2100) {
            throw new RuntimeException("Ano válido é obrigatório");
        }
        Periodo periodo = new Periodo(LocalDate.of(ano, 1, 1), LocalDate.of(ano, 12, 31));
        return carregarLancamentos(periodo, naturezaReceita, null, StatusPagamento.PAGO, null, null, null).stream()
                .collect(Collectors.groupingBy(LancamentoContabilResponse::getAlunoId, LinkedHashMap::new, Collectors.toList()))
                .values().stream()
                .map(lista -> {
                    LancamentoContabilResponse primeiro = lista.get(0);
                    BigDecimal mensalidades = lista.stream()
                            .filter(l -> l.getNaturezaReceita() == TipoCobranca.MENSALIDADE)
                            .map(LancamentoContabilResponse::getValorBruto)
                            .reduce(ZERO, BigDecimal::add);
                    BigDecimal graduacoes = lista.stream()
                            .filter(l -> l.getNaturezaReceita() == TipoCobranca.GRADUACAO)
                            .map(LancamentoContabilResponse::getValorBruto)
                            .reduce(ZERO, BigDecimal::add);
                    return new ResumoRecebimentosAlunoResponse(primeiro.getAlunoId(), primeiro.getAlunoNome(),
                            primeiro.getAlunoDocumento(), normalizar(mensalidades), normalizar(graduacoes),
                            normalizar(mensalidades.add(graduacoes)), lista.size(),
                            lista.stream().map(LancamentoContabilResponse::getPagoEm).filter(Objects::nonNull).min(LocalDateTime::compareTo).orElse(null),
                            lista.stream().map(LancamentoContabilResponse::getPagoEm).filter(Objects::nonNull).max(LocalDateTime::compareTo).orElse(null));
                })
                .sorted(Comparator.comparing(ResumoRecebimentosAlunoResponse::getAlunoNome, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ResumoCompetenciaContabilResponse> competencias(Integer ano) {
        permissaoService.exigirAdmin();
        if (ano == null || ano < 2000 || ano > 2100) {
            throw new RuntimeException("Ano válido é obrigatório");
        }
        Periodo periodo = new Periodo(LocalDate.of(ano, 1, 1), LocalDate.of(ano, 12, 31));
        List<LancamentoContabilResponse> pagos = carregarLancamentos(periodo, null, null, null, null, null, null);
        Map<Integer, List<LancamentoContabilResponse>> porMes = pagos.stream()
                .filter(l -> l.getPagoEm() != null)
                .collect(Collectors.groupingBy(l -> l.getPagoEm().getMonthValue()));
        List<ResumoCompetenciaContabilResponse> resposta = new ArrayList<>();
        for (int mes = 1; mes <= 12; mes++) {
            List<LancamentoContabilResponse> lista = porMes.getOrDefault(mes, List.of());
            BigDecimal mensalidades = somaBrutoPago(lista, TipoCobranca.MENSALIDADE);
            BigDecimal graduacoes = somaBrutoPago(lista, TipoCobranca.GRADUACAO);
            BigDecimal estornos = lista.stream().map(LancamentoContabilResponse::getValorEstornado).reduce(ZERO, BigDecimal::add);
            BigDecimal bruto = mensalidades.add(graduacoes);
            resposta.add(new ResumoCompetenciaContabilResponse(mes, YearMonth.of(ano, mes).toString(),
                    normalizar(mensalidades), normalizar(graduacoes), normalizar(bruto), normalizar(estornos),
                    normalizar(bruto.subtract(estornos)),
                    (int) lista.stream().filter(l -> StatusPagamento.PAGO.name().equals(l.getStatusPagamento())).count()));
        }
        return resposta;
    }

    @Transactional(readOnly = true)
    public byte[] gerarCsv(LocalDate inicio, LocalDate fim, TipoCobranca naturezaReceita,
                           MetodoPagamento metodoPagamento, StatusPagamento statusPagamento,
                           Long alunoId, Long eventoGraduacaoId, String competencia) {
        RelatorioContabilResponse relatorio = gerar(inicio, fim, naturezaReceita, metodoPagamento, statusPagamento,
                alunoId, eventoGraduacaoId, competencia);
        StringBuilder csv = new StringBuilder();
        csv.append("Data do pagamento,Aluno,Documento,E-mail,Natureza da receita,Competência,Evento de graduação,Graduação destino,Valor bruto,Taxa,Estorno,Valor líquido,Método,Provedor,Status,Identificador externo\n");
        relatorio.getLancamentos().forEach(l -> csv.append(csv(l.getPagoEm())).append(',')
                .append(csv(l.getAlunoNome())).append(',')
                .append(csv(l.getAlunoDocumento())).append(',')
                .append(csv(l.getAlunoEmail())).append(',')
                .append(csv(l.getNaturezaReceita())).append(',')
                .append(csv(l.getCompetencia())).append(',')
                .append(csv(l.getEventoGraduacaoTitulo())).append(',')
                .append(csv(l.getGraduacaoDestino())).append(',')
                .append(csv(l.getValorBruto())).append(',')
                .append(csv(l.getTaxa())).append(',')
                .append(csv(l.getValorEstornado())).append(',')
                .append(csv(l.getValorLiquido())).append(',')
                .append(csv(l.getMetodoPagamento())).append(',')
                .append(csv(l.getProvedor())).append(',')
                .append(csv(l.getStatusPagamento())).append(',')
                .append(csv(l.getIdPagamentoExterno())).append('\n'));
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Transactional(readOnly = true)
    public byte[] gerarCsvAlunos(Integer ano, TipoCobranca naturezaReceita) {
        StringBuilder csv = new StringBuilder();
        csv.append("Aluno,Documento,Total mensalidades,Total graduações,Total pago,Quantidade de pagamentos,Primeiro pagamento,Último pagamento\n");
        recebimentosPorAluno(ano, naturezaReceita).forEach(a -> csv.append(csv(a.getAlunoNome())).append(',')
                .append(csv(a.getAlunoDocumento())).append(',')
                .append(csv(a.getTotalMensalidades())).append(',')
                .append(csv(a.getTotalGraduacoes())).append(',')
                .append(csv(a.getTotalPago())).append(',')
                .append(csv(a.getQuantidadePagamentos())).append(',')
                .append(csv(a.getPrimeiroPagamento())).append(',')
                .append(csv(a.getUltimoPagamento())).append('\n'));
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    public String nomeArquivo(LocalDate inicio, LocalDate fim) {
        return "relatorio-contabil-%s-%s.csv".formatted(inicio, fim);
    }

    public String nomeArquivoAlunos(Integer ano) {
        return "relatorio-contabil-alunos-%04d.csv".formatted(ano);
    }

    private List<LancamentoContabilResponse> carregarLancamentos(Periodo periodo, TipoCobranca naturezaReceita,
                                                                 MetodoPagamento metodoPagamento,
                                                                 StatusPagamento statusPagamento,
                                                                 Long alunoId, Long eventoGraduacaoId,
                                                                 LocalDate competencia) {
        Long academiaId = permissaoService.academiaAtualId();
        return pagamentoRepository.findParaRelatorioContabil(academiaId, periodo.inicio().atStartOfDay(), periodo.fim().plusDays(1).atStartOfDay())
                .stream()
                .filter(p -> p.getStatus() == StatusPagamento.PAGO || p.getStatus() == StatusPagamento.ESTORNADO)
                .filter(p -> naturezaReceita == null || p.getTipoCobranca() == naturezaReceita)
                .filter(p -> metodoPagamento == null || p.getMetodo() == metodoPagamento)
                .filter(p -> statusPagamento == null || p.getStatus() == statusPagamento)
                .filter(p -> alunoId == null || aluno(p).map(Aluno::getId).filter(alunoId::equals).isPresent())
                .filter(p -> eventoGraduacaoId == null || Optional.ofNullable(p.getInscricaoGraduacao())
                        .map(InscricaoGraduacao::getEvento).map(EventoGraduacao::getId).filter(eventoGraduacaoId::equals).isPresent())
                .filter(p -> competencia == null || competencia.equals(competencia(p)))
                .map(this::lancamento)
                .toList();
    }

    private LancamentoContabilResponse lancamento(Pagamento pagamento) {
        Aluno aluno = aluno(pagamento).orElse(null);
        Fatura fatura = pagamento.getFatura();
        InscricaoGraduacao inscricao = pagamento.getInscricaoGraduacao();
        EventoGraduacao evento = inscricao == null ? null : inscricao.getEvento();
        Graduacao graduacao = inscricao == null ? null : inscricao.getGraduacaoDestino();
        BigDecimal bruto = pagamento.getStatus() == StatusPagamento.PAGO ? valor(pagamento.getValor()) : ZERO;
        BigDecimal estorno = pagamento.getStatus() == StatusPagamento.ESTORNADO ? valor(pagamento.getValor()) : ZERO;
        BigDecimal taxa = ZERO;
        return new LancamentoContabilResponse(
                pagamento.getId(),
                pagamento.getTipoCobranca(),
                aluno == null ? null : aluno.getId(),
                aluno == null ? null : aluno.getNome(),
                aluno == null ? null : aluno.getEmail(),
                aluno == null ? null : aluno.getCpf(),
                fatura == null ? null : fatura.getId(),
                fatura == null ? null : fatura.getMesReferencia().toString(),
                fatura == null ? null : fatura.getDataVencimento(),
                inscricao == null ? null : inscricao.getId(),
                evento == null ? null : evento.getId(),
                evento == null ? null : evento.getTitulo(),
                graduacao == null ? null : graduacao.getNome(),
                bruto,
                taxa,
                estorno,
                normalizar(bruto.subtract(taxa).subtract(estorno)),
                pagamento.getMetodo() == null ? null : pagamento.getMetodo().name(),
                pagamento.getProvedor(),
                pagamento.getStatus() == null ? null : pagamento.getStatus().name(),
                pagamento.getIdPagamentoExterno(),
                pagamento.getCriadoEm(),
                pagamento.getPagoEm(),
                pagamento.getStatus() == StatusPagamento.ESTORNADO ? pagamento.getUltimoEventoEm() : null);
    }

    private ResumoRelatorioContabilResponse resumo(List<LancamentoContabilResponse> lancamentos) {
        BigDecimal bruto = lancamentos.stream().map(LancamentoContabilResponse::getValorBruto).reduce(ZERO, BigDecimal::add);
        BigDecimal estornos = lancamentos.stream().map(LancamentoContabilResponse::getValorEstornado).reduce(ZERO, BigDecimal::add);
        BigDecimal mensalidades = lancamentos.stream()
                .filter(l -> l.getNaturezaReceita() == TipoCobranca.MENSALIDADE)
                .map(LancamentoContabilResponse::getValorBruto).reduce(ZERO, BigDecimal::add);
        BigDecimal graduacoes = lancamentos.stream()
                .filter(l -> l.getNaturezaReceita() == TipoCobranca.GRADUACAO)
                .map(LancamentoContabilResponse::getValorBruto).reduce(ZERO, BigDecimal::add);
        return new ResumoRelatorioContabilResponse(normalizar(bruto), ZERO, normalizar(estornos),
                normalizar(bruto.subtract(estornos)), normalizar(mensalidades), normalizar(graduacoes), ZERO,
                (int) lancamentos.stream().filter(l -> StatusPagamento.PAGO.name().equals(l.getStatusPagamento())).count(),
                (int) lancamentos.stream().filter(l -> StatusPagamento.PAGO.name().equals(l.getStatusPagamento()))
                        .map(LancamentoContabilResponse::getAlunoId).filter(Objects::nonNull).distinct().count(),
                (int) lancamentos.stream().filter(l -> StatusPagamento.ESTORNADO.name().equals(l.getStatusPagamento())).count());
    }

    private List<ResumoNaturezaReceitaResponse> porNatureza(List<LancamentoContabilResponse> lancamentos) {
        Map<TipoCobranca, List<LancamentoContabilResponse>> grupos = new LinkedHashMap<>();
        grupos.put(TipoCobranca.MENSALIDADE, new ArrayList<>());
        grupos.put(TipoCobranca.GRADUACAO, new ArrayList<>());
        lancamentos.forEach(l -> grupos.computeIfAbsent(l.getNaturezaReceita(), k -> new ArrayList<>()).add(l));
        return grupos.entrySet().stream()
                .map(e -> {
                    BigDecimal bruto = e.getValue().stream().map(LancamentoContabilResponse::getValorBruto).reduce(ZERO, BigDecimal::add);
                    BigDecimal estorno = e.getValue().stream().map(LancamentoContabilResponse::getValorEstornado).reduce(ZERO, BigDecimal::add);
                    return new ResumoNaturezaReceitaResponse(e.getKey(),
                            (int) e.getValue().stream().filter(l -> StatusPagamento.PAGO.name().equals(l.getStatusPagamento())).count(),
                            normalizar(bruto), normalizar(estorno), normalizar(bruto.subtract(estorno)));
                })
                .toList();
    }

    private List<ResumoMetodoPagamentoContabilResponse> porMetodo(List<LancamentoContabilResponse> lancamentos) {
        return lancamentos.stream()
                .filter(l -> StatusPagamento.PAGO.name().equals(l.getStatusPagamento()))
                .collect(Collectors.groupingBy(LancamentoContabilResponse::getMetodoPagamento, LinkedHashMap::new, Collectors.toList()))
                .entrySet().stream()
                .map(e -> {
                    BigDecimal bruto = e.getValue().stream().map(LancamentoContabilResponse::getValorBruto).reduce(ZERO, BigDecimal::add);
                    return new ResumoMetodoPagamentoContabilResponse(e.getKey(), e.getValue().size(), normalizar(bruto), ZERO, normalizar(bruto));
                })
                .toList();
    }

    private BigDecimal somaBrutoPago(List<LancamentoContabilResponse> lancamentos, TipoCobranca tipo) {
        return lancamentos.stream()
                .filter(l -> l.getNaturezaReceita() == tipo)
                .filter(l -> StatusPagamento.PAGO.name().equals(l.getStatusPagamento()))
                .map(LancamentoContabilResponse::getValorBruto)
                .reduce(ZERO, BigDecimal::add);
    }

    private Optional<Aluno> aluno(Pagamento pagamento) {
        if (pagamento.getFatura() != null) {
            return Optional.ofNullable(pagamento.getFatura().getAluno());
        }
        return Optional.ofNullable(pagamento.getInscricaoGraduacao()).map(InscricaoGraduacao::getAluno);
    }

    private LocalDate competencia(Pagamento pagamento) {
        if (pagamento.getFatura() != null) {
            return pagamento.getFatura().getMesReferencia();
        }
        if (pagamento.getPagoEm() != null) {
            return pagamento.getPagoEm().toLocalDate().withDayOfMonth(1);
        }
        return null;
    }

    private LocalDate parseCompetencia(String competencia) {
        if (competencia == null || competencia.isBlank()) {
            return null;
        }
        return YearMonth.parse(competencia).atDay(1);
    }

    private Periodo periodo(LocalDate inicio, LocalDate fim) {
        if (inicio == null || fim == null || fim.isBefore(inicio)) {
            throw new RuntimeException("Período válido é obrigatório");
        }
        return new Periodo(inicio, fim);
    }

    private BigDecimal valor(BigDecimal valor) {
        return valor == null ? ZERO : normalizar(valor);
    }

    private BigDecimal normalizar(BigDecimal valor) {
        return valor == null ? ZERO : valor.setScale(2, RoundingMode.HALF_UP);
    }

    private String csv(Object value) {
        if (value == null) return "";
        String text = value.toString();
        if (text.contains(",") || text.contains("\"") || text.contains("\n")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }

    private record Periodo(LocalDate inicio, LocalDate fim) {
    }
}
