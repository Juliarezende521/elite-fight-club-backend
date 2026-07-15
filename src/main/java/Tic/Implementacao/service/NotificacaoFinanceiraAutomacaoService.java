package Tic.Implementacao.service;

import Tic.Implementacao.dto.notificacao.financeiro.NotificacaoFinanceiraRelatorioResponse;
import Tic.Implementacao.dto.notificacao.financeiro.ResultadoAutomacaoNotificacaoFinanceiraResponse;
import Tic.Implementacao.dto.financeiro.preferencia.ContextoPagamentoAluno;
import Tic.Implementacao.model.*;
import Tic.Implementacao.repository.*;
import Tic.Implementacao.security.PermissaoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class NotificacaoFinanceiraAutomacaoService {

    private static final String REF_FATURA = "FATURA";
    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2);

    private final FaturaRepository faturaRepository;
    private final PagamentoRepository pagamentoRepository;
    private final NotificacaoRepository notificacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PreferenciaPagamentoAlunoService preferenciaPagamentoAlunoService;
    private final PermissaoService permissaoService;
    private final Clock clock;

    public NotificacaoFinanceiraAutomacaoService(FaturaRepository faturaRepository,
                                                PagamentoRepository pagamentoRepository,
                                                NotificacaoRepository notificacaoRepository,
                                                UsuarioRepository usuarioRepository,
                                                PreferenciaPagamentoAlunoService preferenciaPagamentoAlunoService,
                                                PermissaoService permissaoService,
                                                Clock clock) {
        this.faturaRepository = faturaRepository;
        this.pagamentoRepository = pagamentoRepository;
        this.notificacaoRepository = notificacaoRepository;
        this.usuarioRepository = usuarioRepository;
        this.preferenciaPagamentoAlunoService = preferenciaPagamentoAlunoService;
        this.permissaoService = permissaoService;
        this.clock = clock;
    }

    @Transactional
    public ResultadoAutomacaoNotificacaoFinanceiraResponse gerarNotificacoesFinanceiras(LocalDate dataReferencia) {
        LocalDate data = data(dataReferencia);
        Resultado acumulado = new Resultado(data);
        acumulado.somar(processarVencimentos(data, false));
        acumulado.somar(processarAtrasos(data, false));
        return acumulado.response();
    }

    @Transactional
    public ResultadoAutomacaoNotificacaoFinanceiraResponse gerarLembretesDeVencimento(LocalDate dataReferencia) {
        return processarVencimentos(data(dataReferencia), true).response();
    }

    @Transactional
    public ResultadoAutomacaoNotificacaoFinanceiraResponse gerarAvisosDeAtraso(LocalDate dataReferencia) {
        return processarAtrasos(data(dataReferencia), true).response();
    }

    @Transactional
    public Notificacao notificarPagamentoMensalidade(Pagamento pagamento, StatusPagamento status) {
        if (pagamento == null || pagamento.getTipoCobranca() != TipoCobranca.MENSALIDADE || pagamento.getFatura() == null) {
            return null;
        }
        Fatura fatura = pagamento.getFatura();
        Usuario usuario = usuarioValido(fatura.getAluno(), fatura.getAcademia().getId()).orElse(null);
        if (usuario == null) {
            return null;
        }
        if (status == StatusPagamento.PAGO) {
            return criarNotificacao(fatura, usuario, TipoNotificacao.PAGAMENTO_MENSALIDADE_CONFIRMADO,
                    "Pagamento confirmado",
                    "Pagamento confirmado. Sua mensalidade de " + competenciaExtenso(fatura) + " foi recebida com sucesso.",
                    "PAGAMENTO_MENSALIDADE_CONFIRMADO:" + fatura.getId() + ":" + pagamento.getId());
        }
        if (status == StatusPagamento.ESTORNADO) {
            return criarNotificacao(fatura, usuario, TipoNotificacao.PAGAMENTO_MENSALIDADE_ESTORNADO,
                    "Pagamento estornado",
                    "O pagamento da sua mensalidade de " + competenciaExtenso(fatura) + " foi estornado. A fatura pode voltar a possuir saldo em aberto.",
                    "PAGAMENTO_MENSALIDADE_ESTORNADO:" + fatura.getId() + ":" + pagamento.getId());
        }
        if (status == StatusPagamento.CANCELADO || status == StatusPagamento.FALHOU) {
            return criarNotificacao(fatura, usuario, TipoNotificacao.PIX_MENSALIDADE_EXPIRADO,
                    "Cobrança indisponível",
                    "A cobrança Pix da sua mensalidade não está mais disponível. Consulte a área financeira para verificar as opções.",
                    "PIX_MENSALIDADE_EXPIRADO:" + fatura.getId() + ":" + pagamento.getId() + ":" + status.name());
        }
        if (status == StatusPagamento.PROCESSANDO) {
            return criarNotificacao(fatura, usuario, TipoNotificacao.PAGAMENTO_MENSALIDADE_PROCESSANDO,
                    "Pagamento em processamento",
                    "Seu pagamento está sendo processado. Aguarde a confirmação antes de iniciar outra tentativa.",
                    "PAGAMENTO_MENSALIDADE_PROCESSANDO:" + fatura.getId() + ":" + status.name());
        }
        return null;
    }

    @Transactional(readOnly = true)
    public List<NotificacaoFinanceiraRelatorioResponse> relatorio(LocalDateTime inicio, LocalDateTime fim,
                                                                  TipoNotificacao tipo, StatusNotificacao status,
                                                                  Long alunoId, Long faturaId,
                                                                  Boolean somenteFalhas) {
        permissaoService.exigirAdmin();
        Long academiaId = permissaoService.academiaAtualId();
        List<Notificacao> notificacoes = notificacaoRepository.buscarRelatorioFinanceiro(
                academiaId, inicio, fim, tipo, status, alunoId, faturaId, Boolean.TRUE.equals(somenteFalhas));
        Map<Long, Fatura> faturas = faturasPorId(academiaId, notificacoes.stream()
                .map(Notificacao::getReferenciaId)
                .filter(Objects::nonNull)
                .distinct()
                .toList());
        return notificacoes.stream()
                .map(n -> NotificacaoFinanceiraRelatorioResponse.from(n, faturas.get(n.getReferenciaId())))
                .toList();
    }

    private Resultado processarVencimentos(LocalDate dataReferencia, boolean exigirAdmin) {
        if (exigirAdmin) {
            permissaoService.exigirAdmin();
        }
        Long academiaId = permissaoService.academiaAtualId();
        List<Fatura> faturas = faturaRepository.findComAlunoByAcademiaIdAndDataVencimento(academiaId, dataReferencia);
        Dados dados = dados(academiaId, faturas);
        Resultado resultado = new Resultado(dataReferencia);
        faturas.forEach(fatura -> processarVencimento(fatura, dataReferencia, dados, resultado));
        return resultado;
    }

    private Resultado processarAtrasos(LocalDate dataReferencia, boolean exigirAdmin) {
        if (exigirAdmin) {
            permissaoService.exigirAdmin();
        }
        Long academiaId = permissaoService.academiaAtualId();
        LocalDate vencimentoAnalisado = dataReferencia.minusDays(2);
        List<Fatura> faturas = faturaRepository.findComAlunoByAcademiaIdAndDataVencimento(academiaId, vencimentoAnalisado);
        Dados dados = dados(academiaId, faturas);
        Resultado resultado = new Resultado(dataReferencia);
        faturas.forEach(fatura -> processarAtraso(fatura, dataReferencia, dados, resultado));
        return resultado;
    }

    private void processarVencimento(Fatura fatura, LocalDate dataReferencia, Dados dados, Resultado resultado) {
        resultado.analisada();
        List<Pagamento> pagamentos = dados.pagamentosPorFatura().getOrDefault(fatura.getId(), List.of());
        BigDecimal saldo = saldo(fatura, pagamentos);
        Motivo motivo = motivoIgnorarBase(fatura, saldo, dados);
        if (motivo != null) {
            resultado.ignorada(motivo);
            return;
        }
        Optional<Usuario> usuario = usuarioValido(fatura, dados);
        if (usuario.isEmpty()) {
            resultado.falha(Motivo.USUARIO_NAO_VINCULADO);
            return;
        }
        ContextoPagamentoAluno contexto = preferenciaPagamentoAlunoService.obterContextoPagamento(fatura);
        if (Boolean.TRUE.equals(contexto.getPossuiPagamentoProcessando())) {
            criarOuContar(fatura, usuario.get(), TipoNotificacao.PAGAMENTO_MENSALIDADE_PROCESSANDO,
                    "Pagamento em processamento",
                    "Seu pagamento está sendo processado. Aguarde a confirmação antes de iniciar outra tentativa.",
                    "PAGAMENTO_MENSALIDADE_PROCESSANDO:" + fatura.getId() + ":" + StatusPagamento.PROCESSANDO.name(), resultado);
            return;
        }
        String mensagem = Boolean.TRUE.equals(contexto.getPossuiPixValido())
                ? "Sua mensalidade vence hoje. Há um Pix disponível na área financeira."
                : "Sua mensalidade vence hoje. Acesse a área financeira para consultar as formas de pagamento.";
        mensagem += complementoPreferencia(contexto);
        if (saldo.compareTo(fatura.getValor()) < 0) {
            mensagem += " " + mensagemSaldo(saldo);
        }
        criarOuContar(fatura, usuario.get(), TipoNotificacao.VENCIMENTO_MENSALIDADE_HOJE,
                "Mensalidade vence hoje", mensagem,
                "VENCIMENTO_MENSALIDADE_HOJE:" + fatura.getId() + ":" + dataReferencia, resultado);
    }

    private void processarAtraso(Fatura fatura, LocalDate dataReferencia, Dados dados, Resultado resultado) {
        resultado.analisada();
        List<Pagamento> pagamentos = dados.pagamentosPorFatura().getOrDefault(fatura.getId(), List.of());
        BigDecimal saldo = saldo(fatura, pagamentos);
        Motivo motivo = motivoIgnorarBase(fatura, saldo, dados);
        if (motivo != null) {
            resultado.ignorada(motivo);
            return;
        }
        ContextoPagamentoAluno contexto = preferenciaPagamentoAlunoService.obterContextoPagamento(fatura);
        if (Boolean.TRUE.equals(contexto.getPossuiPagamentoProcessando())) {
            resultado.ignorada(Motivo.PAGAMENTO_PROCESSANDO);
            return;
        }
        Optional<Usuario> usuario = usuarioValido(fatura, dados);
        if (usuario.isEmpty()) {
            resultado.falha(Motivo.USUARIO_NAO_VINCULADO);
            return;
        }
        String mensagem = "Identificamos que sua mensalidade com vencimento em "
                + fatura.getDataVencimento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                + " ainda possui saldo em aberto. Consulte a área financeira para regularizar. "
                + mensagemSaldo(saldo)
                + complementoPreferencia(contexto);
        criarOuContar(fatura, usuario.get(), TipoNotificacao.MENSALIDADE_ATRASADA,
                "Mensalidade em atraso", mensagem,
                "MENSALIDADE_ATRASADA:" + fatura.getId() + ":" + dataReferencia, resultado);
    }

    private Motivo motivoIgnorarBase(Fatura fatura, BigDecimal saldo, Dados dados) {
        if (fatura.getStatus() == StatusFatura.CANCELADA) return Motivo.FATURA_CANCELADA;
        if (fatura.getStatus() == StatusFatura.PAGA) return Motivo.FATURA_PAGA;
        if (saldo.compareTo(ZERO) <= 0) return Motivo.SALDO_ZERADO;
        if (!Boolean.TRUE.equals(fatura.getAluno().getAtivo())) return Motivo.ALUNO_INATIVO;
        List<Pagamento> pagamentos = dados.pagamentosPorFatura().getOrDefault(fatura.getId(), List.of());
        if (recebido(pagamentos).compareTo(fatura.getValor()) >= 0) return Motivo.SALDO_ZERADO;
        return null;
    }

    private void criarOuContar(Fatura fatura, Usuario usuario, TipoNotificacao tipo, String titulo,
                               String mensagem, String chave, Resultado resultado) {
        if (notificacaoRepository.findByChaveIdempotencia(chave).isPresent()) {
            resultado.ignorada(Motivo.NOTIFICACAO_DUPLICADA);
            return;
        }
        criarNotificacao(fatura, usuario, tipo, titulo, mensagem, chave);
        resultado.criada();
    }

    private Notificacao criarNotificacao(Fatura fatura, Usuario usuario, TipoNotificacao tipo, String titulo,
                                         String mensagem, String chave) {
        if (notificacaoRepository.findByChaveIdempotencia(chave).isPresent()) {
            return notificacaoRepository.findByChaveIdempotencia(chave).get();
        }
        LocalDateTime agora = LocalDateTime.now(clock);
        Notificacao notificacao = new Notificacao(tipo, mensagem);
        notificacao.setAcademia(fatura.getAcademia());
        notificacao.setDestinatario(usuario);
        notificacao.setAluno(fatura.getAluno());
        notificacao.setTitulo(titulo);
        notificacao.setDataEnvio(agora);
        notificacao.setCriadaEm(agora);
        notificacao.setEnviada(true);
        notificacao.setStatus(StatusNotificacao.ENVIADA);
        notificacao.setLida(false);
        notificacao.setReferenciaTipo(REF_FATURA);
        notificacao.setReferenciaId(fatura.getId());
        notificacao.setChaveIdempotencia(chave);
        return notificacaoRepository.save(notificacao);
    }

    private Dados dados(Long academiaId, List<Fatura> faturas) {
        List<Long> faturaIds = faturas.stream().map(Fatura::getId).filter(Objects::nonNull).toList();
        List<Pagamento> pagamentos = faturaIds.isEmpty() ? List.of()
                : pagamentoRepository.findMensalidadesComFaturaPorFaturas(academiaId, faturaIds);
        Map<Long, List<Pagamento>> pagamentosPorFatura = pagamentos.stream()
                .collect(Collectors.groupingBy(p -> p.getFatura().getId()));
        List<Long> alunoIds = faturas.stream().map(Fatura::getAluno).map(Aluno::getId).filter(Objects::nonNull).distinct().toList();
        Map<Long, Usuario> usuariosPorAluno = alunoIds.isEmpty() ? Map.of()
                : usuarioRepository.findByAcademiaIdAndAlunoIdIn(academiaId, alunoIds).stream()
                .collect(Collectors.toMap(u -> u.getAluno().getId(), u -> u, (a, b) -> a));
        return new Dados(pagamentosPorFatura, usuariosPorAluno);
    }

    private Optional<Usuario> usuarioValido(Fatura fatura, Dados dados) {
        return Optional.ofNullable(dados.usuariosPorAluno().get(fatura.getAluno().getId()))
                .filter(u -> Boolean.TRUE.equals(u.getAtivo()))
                .filter(u -> u.getPerfil() == PerfilUsuario.ALUNO)
                .filter(u -> u.getAcademia() != null && Objects.equals(u.getAcademia().getId(), fatura.getAcademia().getId()));
    }

    private Optional<Usuario> usuarioValido(Aluno aluno, Long academiaId) {
        if (aluno == null || aluno.getId() == null) {
            return Optional.empty();
        }
        return usuarioRepository.findByAlunoIdAndAcademiaId(aluno.getId(), academiaId)
                .filter(u -> Boolean.TRUE.equals(u.getAtivo()))
                .filter(u -> u.getPerfil() == PerfilUsuario.ALUNO);
    }

    private String complementoPreferencia(ContextoPagamentoAluno contexto) {
        if (contexto == null || contexto.getFormaPreferida() == null) {
            return "";
        }
        if (contexto.getFormaPreferida() == FormaPagamentoPreferida.PIX_AUTOMATICO
                && !Boolean.TRUE.equals(contexto.getPagamentoAutomaticoAtivo())) {
            return " A autorização do Pix Automático ainda não está ativa.";
        }
        if (contexto.getFormaPreferida() == FormaPagamentoPreferida.CARTAO
                && contexto.getStatusAutorizacao() != StatusAutorizacaoPagamento.ATIVA) {
            return " O cartão ainda não possui autorização ativa no sistema.";
        }
        return "";
    }

    private BigDecimal saldo(Fatura fatura, List<Pagamento> pagamentos) {
        if (fatura.getStatus() == StatusFatura.CANCELADA) {
            return ZERO;
        }
        return fatura.getValor().subtract(recebido(pagamentos)).max(ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal recebido(List<Pagamento> pagamentos) {
        return pagamentos.stream()
                .filter(p -> p.getStatus() == StatusPagamento.PAGO)
                .map(Pagamento::getValor)
                .reduce(ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private String mensagemSaldo(BigDecimal saldo) {
        return "Sua mensalidade possui saldo pendente de R$ " + saldo.setScale(2, RoundingMode.HALF_UP).toString().replace(".", ",") + ".";
    }

    private String competenciaExtenso(Fatura fatura) {
        YearMonth ym = YearMonth.from(fatura.getMesReferencia());
        return ym.getMonth().getDisplayName(java.time.format.TextStyle.FULL, Locale.forLanguageTag("pt-BR"))
                + " de " + ym.getYear();
    }

    private LocalDate data(LocalDate dataReferencia) {
        return dataReferencia == null ? LocalDate.now(clock) : dataReferencia;
    }

    private Map<Long, Fatura> faturasPorId(Long academiaId, List<Long> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        return faturaRepository.findComAlunoByAcademiaIdAndIdIn(academiaId, ids).stream()
                .collect(Collectors.toMap(Fatura::getId, f -> f));
    }

    private record Dados(Map<Long, List<Pagamento>> pagamentosPorFatura, Map<Long, Usuario> usuariosPorAluno) {
    }

    private enum Motivo {
        FATURA_PAGA,
        SALDO_ZERADO,
        PAGAMENTO_PROCESSANDO,
        PIX_VALIDO_EXISTENTE,
        USUARIO_NAO_VINCULADO,
        ALUNO_INATIVO,
        NOTIFICACAO_DUPLICADA,
        FATURA_CANCELADA
    }

    private static class Resultado {
        private final LocalDate dataReferencia;
        private int faturasAnalisadas;
        private int notificacoesCriadas;
        private int notificacoesIgnoradas;
        private int falhas;
        private final Map<String, Integer> motivosIgnorados = new LinkedHashMap<>();

        Resultado(LocalDate dataReferencia) {
            this.dataReferencia = dataReferencia;
        }

        void analisada() { faturasAnalisadas++; }
        void criada() { notificacoesCriadas++; }
        void ignorada(Motivo motivo) {
            notificacoesIgnoradas++;
            motivosIgnorados.merge(motivo.name(), 1, Integer::sum);
        }
        void falha(Motivo motivo) {
            falhas++;
            motivosIgnorados.merge(motivo.name(), 1, Integer::sum);
        }
        void somar(Resultado outro) {
            faturasAnalisadas += outro.faturasAnalisadas;
            notificacoesCriadas += outro.notificacoesCriadas;
            notificacoesIgnoradas += outro.notificacoesIgnoradas;
            falhas += outro.falhas;
            outro.motivosIgnorados.forEach((k, v) -> motivosIgnorados.merge(k, v, Integer::sum));
        }
        ResultadoAutomacaoNotificacaoFinanceiraResponse response() {
            return new ResultadoAutomacaoNotificacaoFinanceiraResponse(dataReferencia, faturasAnalisadas,
                    notificacoesCriadas, notificacoesIgnoradas, falhas, motivosIgnorados);
        }
    }
}
