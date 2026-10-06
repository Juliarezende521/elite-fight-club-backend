package Tic.Implementacao.service;

import Tic.Implementacao.dto.financeiro.CobrancaPixResponse;
import Tic.Implementacao.dto.financeiro.ConciliacaoPagamentoResponse;
import Tic.Implementacao.dto.financeiro.PagamentoManualRequest;
import Tic.Implementacao.dto.graduacao.inscricao.CriarPixInscricaoGraduacaoResponse;
import Tic.Implementacao.model.*;
import Tic.Implementacao.payments.ProvedorPagamento;
import Tic.Implementacao.payments.asaas.AsaasProperties;
import Tic.Implementacao.payments.asaas.AsaasStatusMapper;
import Tic.Implementacao.repository.AlunoRepository;
import Tic.Implementacao.repository.EventoPagamentoExternoRepository;
import Tic.Implementacao.repository.InscricaoGraduacaoRepository;
import Tic.Implementacao.repository.PagamentoRepository;
import Tic.Implementacao.security.PermissaoSistema;
import Tic.Implementacao.security.PermissaoService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PagamentoService {

    private static final Logger logger = LoggerFactory.getLogger(PagamentoService.class);

    private final PagamentoRepository repository;
    private final EventoPagamentoExternoRepository eventoRepository;
    private final AlunoRepository alunoRepository;
    private final InscricaoGraduacaoRepository inscricaoGraduacaoRepository;
    private final FaturaService faturaService;
    private final ProvedorPagamento provedorPagamento;
    private final PermissaoService permissaoService;
    private final NotificacaoService notificacaoService;
    private final NotificacaoFinanceiraAutomacaoService notificacaoFinanceiraAutomacaoService;
    private final ObjectMapper objectMapper;
    private final AsaasProperties asaasProperties;
    private final Map<Long, Object> locksPorFatura = new ConcurrentHashMap<>();
    private final Map<Long, Object> locksPorInscricao = new ConcurrentHashMap<>();
    private final Map<Long, Object> locksPorAluno = new ConcurrentHashMap<>();

    public PagamentoService(PagamentoRepository repository, EventoPagamentoExternoRepository eventoRepository,
                            AlunoRepository alunoRepository, InscricaoGraduacaoRepository inscricaoGraduacaoRepository,
                            FaturaService faturaService,
                            ProvedorPagamento provedorPagamento, PermissaoService permissaoService,
                            NotificacaoService notificacaoService,
                            NotificacaoFinanceiraAutomacaoService notificacaoFinanceiraAutomacaoService,
                            ObjectMapper objectMapper, AsaasProperties asaasProperties) {
        this.repository = repository;
        this.eventoRepository = eventoRepository;
        this.alunoRepository = alunoRepository;
        this.inscricaoGraduacaoRepository = inscricaoGraduacaoRepository;
        this.faturaService = faturaService;
        this.provedorPagamento = provedorPagamento;
        this.permissaoService = permissaoService;
        this.notificacaoService = notificacaoService;
        this.notificacaoFinanceiraAutomacaoService = notificacaoFinanceiraAutomacaoService;
        this.objectMapper = objectMapper;
        this.asaasProperties = asaasProperties;
    }

    public List<Pagamento> listarPorFatura(Long faturaId) {
        return repository.findByAcademiaIdAndFaturaId(permissaoService.academiaAtualId(), faturaId);
    }

    @Transactional
    public CobrancaPixResponse criarCobrancaPix(Long faturaId) {
        Object lock = locksPorFatura.computeIfAbsent(faturaId, ignored -> new Object());
        synchronized (lock) {
            Fatura fatura = faturaService.buscarPorId(faturaId);
            validarFaturaPermitePix(fatura);

            Pagamento existente = buscarPixPendenteValido(fatura);
            if (existente != null) {
                logger.info("Pix pendente reutilizado: pagamentoId={}, faturaId={}, provedor={}, academiaId={}",
                        existente.getId(), fatura.getId(), existente.getProvedor(), fatura.getAcademia().getId());
                return respostaPix(existente);
            }

            garantirClienteExterno(fatura.getAluno());

            Pagamento pagamento = new Pagamento();
            pagamento.setFatura(fatura);
            pagamento.setTipoCobranca(TipoCobranca.MENSALIDADE);
            pagamento.setAcademia(fatura.getAcademia());
            pagamento.setValor(saldoRestante(fatura));
            pagamento.setMetodo(MetodoPagamento.PIX);
            pagamento.setStatus(StatusPagamento.PENDENTE);
            pagamento.setProvedor(provedorPagamento.nome());
            pagamento.setCriadoEm(LocalDateTime.now());
            pagamento = repository.save(pagamento);

            CobrancaPixResponse cobranca = provedorPagamento.criarCobrancaPix(fatura);
            pagamento.setIdPagamentoExterno(cobranca.getIdPagamentoExterno());
            pagamento.setPixCopiaCola(cobranca.getPixCopiaCola());
            pagamento.setPixQrCodeUrl(cobranca.getPixQrCodeUrl());
            pagamento.setPixQrCodeBase64(cobranca.getQrCodeBase64());
            pagamento.setDataExpiracao(cobranca.getDataExpiracao());
            pagamento = repository.save(pagamento);

            logger.info("Pix criado: pagamentoId={}, faturaId={}, provedor={}, academiaId={}, status={}",
                    pagamento.getId(), fatura.getId(), pagamento.getProvedor(), fatura.getAcademia().getId(), pagamento.getStatus());
            return respostaPix(pagamento);
        }
    }

    @Transactional
    public CriarPixInscricaoGraduacaoResponse criarPixInscricaoGraduacao(Long inscricaoId) {
        Usuario usuario = permissaoService.usuarioAtual();
        if (usuario.getPerfil() != PerfilUsuario.ALUNO || usuario.getAluno() == null) {
            throw new AccessDeniedException("Apenas o aluno autenticado pode criar Pix da própria inscrição");
        }
        Object lock = locksPorInscricao.computeIfAbsent(inscricaoId, ignored -> new Object());
        synchronized (lock) {
            Long academiaId = permissaoService.academiaAtualId();
            InscricaoGraduacao inscricao = inscricaoGraduacaoRepository
                    .findByIdAndAcademiaIdAndAlunoIdForUpdate(inscricaoId, academiaId, usuario.getAluno().getId())
                    .orElseThrow(() -> new RuntimeException("Inscrição de graduação não encontrada"));
            validarInscricaoPermitePix(inscricao);

            Pagamento existente = buscarPixGraduacaoPendenteValido(inscricao);
            if (existente != null) {
                return respostaPixInscricao(inscricao, existente);
            }

            garantirClienteExterno(inscricao.getAluno());

            Pagamento pagamento = new Pagamento();
            pagamento.setTipoCobranca(TipoCobranca.GRADUACAO);
            pagamento.setInscricaoGraduacao(inscricao);
            pagamento.setAcademia(inscricao.getAcademia());
            pagamento.setValor(inscricao.getValor());
            pagamento.setMetodo(MetodoPagamento.PIX);
            pagamento.setStatus(StatusPagamento.PENDENTE);
            pagamento.setProvedor(provedorPagamento.nome());
            pagamento.setCriadoEm(LocalDateTime.now());
            pagamento = repository.save(pagamento);

            CobrancaPixResponse cobranca = provedorPagamento.criarCobrancaPixGraduacao(inscricao);
            pagamento.setIdPagamentoExterno(cobranca.getIdPagamentoExterno());
            pagamento.setPixCopiaCola(cobranca.getPixCopiaCola());
            pagamento.setPixQrCodeUrl(cobranca.getPixQrCodeUrl());
            pagamento.setPixQrCodeBase64(cobranca.getQrCodeBase64());
            pagamento.setDataExpiracao(cobranca.getDataExpiracao());
            pagamento = repository.save(pagamento);

            inscricao.setPagamento(pagamento);
            inscricao.setStatus(StatusInscricaoGraduacao.PAGAMENTO_PROCESSANDO);
            inscricaoGraduacaoRepository.save(inscricao);
            return respostaPixInscricao(inscricao, pagamento);
        }
    }

    @Transactional
    public Pagamento registrarPagamentoManual(Long faturaId, PagamentoManualRequest request) {
        permissaoService.exigirPermissao(PermissaoSistema.PAGAMENTO_CONFIRMAR);
        Usuario usuario = permissaoService.usuarioAtual();
        Fatura fatura = faturaService.buscarPorId(faturaId);

        if (fatura.getStatus() == StatusFatura.PAGA) {
            throw new RuntimeException("Fatura já está paga");
        }

        if (fatura.getStatus() == StatusFatura.CANCELADA) {
            throw new RuntimeException("Fatura cancelada não pode receber pagamento");
        }

        if (!metodoManualPermitido(request.getMetodo())) {
            throw new RuntimeException("Forma de pagamento manual inválida");
        }

        validarValorPagamentoManual(fatura, request.getValor());

        Pagamento pagamento = new Pagamento();
        pagamento.setFatura(fatura);
        pagamento.setTipoCobranca(TipoCobranca.MENSALIDADE);
        pagamento.setAcademia(fatura.getAcademia());
        pagamento.setValor(request.getValor());
        pagamento.setMetodo(request.getMetodo());
        pagamento.setStatus(StatusPagamento.PAGO);
        pagamento.setProvedor("MANUAL");
        pagamento.setPagoEm(request.getDataPagamento());
        pagamento.setCriadoEm(LocalDateTime.now());
        pagamento.setObservacao(request.getObservacao());
        pagamento.setConfirmadoPor(usuario);

        atualizarStatusFaturaPorSaldo(fatura, pagamento, request.getValor());

        Pagamento salvo = repository.save(pagamento);
        notificacaoFinanceiraAutomacaoService.notificarPagamentoMensalidade(salvo, StatusPagamento.PAGO);
        return salvo;
    }

    @Transactional
    public void processarWebhookAsaas(String token, String payload) {
        validarWebhookToken(token);
        JsonNode root = parse(payload);
        String eventoId = texto(root, "id");
        String tipoEvento = texto(root, "event");
        JsonNode paymentNode = root.path("payment");
        String pagamentoExternoId = texto(paymentNode, "id");
        if (eventoId == null || eventoId.isBlank()) {
            eventoId = pagamentoExternoId + ":" + tipoEvento + ":" + texto(paymentNode, "status");
        }

        if (eventoRepository.existsByProvedorAndEventoExternoIdAndTipoEvento("ASAAS", eventoId, tipoEvento)) {
            return;
        }

        Pagamento pagamento = repository.findByIdPagamentoExterno(pagamentoExternoId)
                .orElseThrow(() -> new RuntimeException("Pagamento externo não localizado"));

        EventoPagamentoExterno evento = new EventoPagamentoExterno();
        evento.setProvedor("ASAAS");
        evento.setEventoExternoId(eventoId);
        evento.setTipoEvento(tipoEvento);
        evento.setPagamentoExternoId(pagamentoExternoId);
        evento.setPagamento(pagamento);
        evento.setProcessadoEm(LocalDateTime.now());
        eventoRepository.save(evento);

        aplicarEventoAsaas(pagamento, tipoEvento, texto(paymentNode, "status"), valor(paymentNode, "value"));
        repository.save(pagamento);
    }

    private void garantirClienteExterno(Aluno aluno) {
        if (aluno.getAsaasCustomerId() != null && !aluno.getAsaasCustomerId().isBlank()) {
            return;
        }
        Object lock = locksPorAluno.computeIfAbsent(aluno.getId(), ignored -> new Object());
        synchronized (lock) {
            if (aluno.getAsaasCustomerId() != null && !aluno.getAsaasCustomerId().isBlank()) {
                return;
            }
            String customerId = provedorPagamento.criarCliente(aluno);
            aluno.setAsaasCustomerId(customerId);
            alunoRepository.save(aluno);
            logger.info("Cliente externo criado: alunoId={}, academiaId={}, provedor={}",
                    aluno.getId(), aluno.getAcademia().getId(), provedorPagamento.nome());
        }
    }

    private void aplicarEventoAsaas(Pagamento pagamento, String tipoEvento, String statusAsaas, BigDecimal valorRecebido) {
        StatusPagamento statusAnterior = pagamento.getStatus();
        StatusFatura statusFaturaAnterior = pagamento.getFatura() == null ? null : pagamento.getFatura().getStatus();
        StatusPagamento status = AsaasStatusMapper.paraStatusInterno(statusAsaas);
        pagamento.setWebhookStatus(statusAsaas);
        pagamento.setUltimoEventoExterno(tipoEvento);
        pagamento.setUltimoEventoEm(LocalDateTime.now());

        if (AsaasStatusMapper.eventoConfirmaPagamento(tipoEvento)) {
            validarValorWebhook(pagamento, valorRecebido);
            pagamento.setStatus(StatusPagamento.PAGO);
            pagamento.setPagoEm(LocalDateTime.now());
            if (pagamento.getTipoCobranca() == TipoCobranca.GRADUACAO) {
                atualizarInscricaoPorPagamento(pagamento, StatusPagamento.PAGO);
            } else {
                atualizarStatusFaturaAposConfirmacao(pagamento.getFatura(), pagamento);
                notificacaoFinanceiraAutomacaoService.notificarPagamentoMensalidade(pagamento, StatusPagamento.PAGO);
            }
            logEventoProcessado(pagamento, tipoEvento, statusAnterior, pagamento.getStatus(), statusFaturaAnterior);
            return;
        }

        if (AsaasStatusMapper.eventoPagamentoEmAnalise(tipoEvento)) {
            pagamento.setStatus(StatusPagamento.PROCESSANDO);
            if (pagamento.getTipoCobranca() == TipoCobranca.GRADUACAO) {
                atualizarInscricaoPorPagamento(pagamento, StatusPagamento.PROCESSANDO);
            } else {
                notificacaoFinanceiraAutomacaoService.notificarPagamentoMensalidade(pagamento, StatusPagamento.PROCESSANDO);
            }
            logEventoProcessado(pagamento, tipoEvento, statusAnterior, pagamento.getStatus(), statusFaturaAnterior);
            return;
        }

        if (AsaasStatusMapper.eventoVencimento(tipoEvento)) {
            pagamento.setStatus(StatusPagamento.FALHOU);
            if (pagamento.getTipoCobranca() == TipoCobranca.GRADUACAO) {
                atualizarInscricaoPorPagamento(pagamento, StatusPagamento.FALHOU);
            } else {
                pagamento.getFatura().setStatus(StatusFatura.VENCIDA);
                pagamento.getFatura().setAtualizadoEm(LocalDateTime.now());
                notificacaoFinanceiraAutomacaoService.notificarPagamentoMensalidade(pagamento, StatusPagamento.FALHOU);
            }
            logEventoProcessado(pagamento, tipoEvento, statusAnterior, pagamento.getStatus(), statusFaturaAnterior);
            return;
        }

        if (AsaasStatusMapper.eventoCancelamento(tipoEvento)) {
            pagamento.setStatus(StatusPagamento.CANCELADO);
            if (pagamento.getTipoCobranca() == TipoCobranca.GRADUACAO) {
                atualizarInscricaoPorPagamento(pagamento, StatusPagamento.CANCELADO);
            } else {
                pagamento.getFatura().setStatus(StatusFatura.CANCELADA);
                pagamento.getFatura().setAtualizadoEm(LocalDateTime.now());
                notificacaoFinanceiraAutomacaoService.notificarPagamentoMensalidade(pagamento, StatusPagamento.CANCELADO);
            }
            logEventoProcessado(pagamento, tipoEvento, statusAnterior, pagamento.getStatus(), statusFaturaAnterior);
            return;
        }

        if (AsaasStatusMapper.eventoEstorno(tipoEvento)) {
            pagamento.setStatus(StatusPagamento.ESTORNADO);
            if (pagamento.getTipoCobranca() == TipoCobranca.GRADUACAO) {
                atualizarInscricaoPorPagamento(pagamento, StatusPagamento.ESTORNADO);
            } else {
                pagamento.getFatura().setStatus(StatusFatura.PENDENTE);
                pagamento.getFatura().setAtualizadoEm(LocalDateTime.now());
                notificacaoFinanceiraAutomacaoService.notificarPagamentoMensalidade(pagamento, StatusPagamento.ESTORNADO);
            }
            logEventoProcessado(pagamento, tipoEvento, statusAnterior, pagamento.getStatus(), statusFaturaAnterior);
            return;
        }

        logger.info("Evento Asaas desconhecido registrado sem alteração financeira: pagamentoId={}, evento={}, statusAsaas={}, academiaId={}",
                pagamento.getId(), tipoEvento, statusAsaas, pagamento.getAcademia().getId());
    }

    private void validarWebhookToken(String token) {
        String esperado = asaasProperties.getWebhookToken();
        if (esperado == null || esperado.isBlank()) {
            throw new RuntimeException("ASAAS_WEBHOOK_TOKEN não configurado");
        }
        if (token == null || !MessageDigest.isEqual(token.getBytes(StandardCharsets.UTF_8), esperado.getBytes(StandardCharsets.UTF_8))) {
            throw new AccessDeniedException("Token de webhook Asaas inválido");
        }
    }

    private JsonNode parse(String payload) {
        try {
            return objectMapper.readTree(payload);
        } catch (Exception e) {
            throw new RuntimeException("Payload de webhook inválido", e);
        }
    }

    private String texto(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? null : value.asText();
    }

    @Transactional
    public ConciliacaoPagamentoResponse conciliar(Long pagamentoId) {
        permissaoService.exigirAdmin();
        Long academiaId = permissaoService.academiaAtualId();
        Pagamento pagamento = repository.findByIdAndAcademiaId(pagamentoId, academiaId)
                .orElseThrow(() -> new RuntimeException("Pagamento não encontrado com ID: " + pagamentoId));

        if (pagamento.getIdPagamentoExterno() == null || pagamento.getIdPagamentoExterno().isBlank()) {
            throw new RuntimeException("Pagamento não possui identificador externo para conciliação");
        }

        StatusPagamento statusPagamentoAnterior = pagamento.getStatus();
        StatusFatura statusFaturaAnterior = pagamento.getFatura() == null ? null : pagamento.getFatura().getStatus();
        StatusInscricaoGraduacao statusInscricaoAnterior = pagamento.getInscricaoGraduacao() == null ? null : pagamento.getInscricaoGraduacao().getStatus();
        StatusPagamento statusAtual = provedorPagamento.consultarPagamento(pagamento.getIdPagamentoExterno());
        pagamento.setStatus(statusAtual);
        pagamento.setConciliadoEm(LocalDateTime.now());

        if (pagamento.getTipoCobranca() == TipoCobranca.GRADUACAO) {
            atualizarInscricaoPorPagamento(pagamento, statusAtual);
        } else {
            if (statusAtual == StatusPagamento.PAGO) {
                pagamento.setPagoEm(pagamento.getPagoEm() == null ? LocalDateTime.now() : pagamento.getPagoEm());
                atualizarStatusFaturaAposConfirmacao(pagamento.getFatura(), pagamento);
                notificacaoFinanceiraAutomacaoService.notificarPagamentoMensalidade(pagamento, StatusPagamento.PAGO);
            } else if (statusAtual == StatusPagamento.CANCELADO) {
                pagamento.getFatura().setStatus(StatusFatura.CANCELADA);
                pagamento.getFatura().setAtualizadoEm(LocalDateTime.now());
                notificacaoFinanceiraAutomacaoService.notificarPagamentoMensalidade(pagamento, StatusPagamento.CANCELADO);
            } else if (statusAtual == StatusPagamento.ESTORNADO) {
                pagamento.getFatura().setStatus(StatusFatura.PENDENTE);
                pagamento.getFatura().setAtualizadoEm(LocalDateTime.now());
                notificacaoFinanceiraAutomacaoService.notificarPagamentoMensalidade(pagamento, StatusPagamento.ESTORNADO);
            } else if (statusAtual == StatusPagamento.PROCESSANDO) {
                notificacaoFinanceiraAutomacaoService.notificarPagamentoMensalidade(pagamento, StatusPagamento.PROCESSANDO);
            }
        }

        repository.save(pagamento);
        logger.info("Pagamento conciliado: pagamentoId={}, tipoCobranca={}, faturaId={}, inscricaoId={}, statusAnterior={}, statusAtual={}, academiaId={}",
                pagamento.getId(), pagamento.getTipoCobranca(),
                pagamento.getFatura() == null ? null : pagamento.getFatura().getId(),
                pagamento.getInscricaoGraduacao() == null ? null : pagamento.getInscricaoGraduacao().getId(),
                statusPagamentoAnterior, pagamento.getStatus(), academiaId);
        return new ConciliacaoPagamentoResponse(
                pagamento.getId(),
                pagamento.getFatura() == null ? null : pagamento.getFatura().getId(),
                pagamento.getInscricaoGraduacao() == null ? null : pagamento.getInscricaoGraduacao().getId(),
                pagamento.getTipoCobranca(),
                statusPagamentoAnterior,
                pagamento.getStatus(),
                statusFaturaAnterior,
                pagamento.getFatura() == null ? null : pagamento.getFatura().getStatus(),
                statusInscricaoAnterior,
                pagamento.getInscricaoGraduacao() == null ? null : pagamento.getInscricaoGraduacao().getStatus(),
                pagamento.getConciliadoEm()
        );
    }

    private void validarFaturaPermitePix(Fatura fatura) {
        if (fatura.getStatus() == StatusFatura.PAGA) {
            throw new RuntimeException("Fatura já está paga");
        }
        if (fatura.getStatus() == StatusFatura.CANCELADA) {
            throw new RuntimeException("Fatura cancelada não permite cobrança Pix");
        }
        if (saldoRestante(fatura).compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Fatura não possui saldo pendente");
        }
    }

    private Pagamento buscarPixPendenteValido(Fatura fatura) {
        return repository.findFirstByAcademiaIdAndFaturaIdAndProvedorAndMetodoAndStatusAndIdPagamentoExternoIsNotNullOrderByCriadoEmDesc(
                        fatura.getAcademia().getId(), fatura.getId(), provedorPagamento.nome(), MetodoPagamento.PIX, StatusPagamento.PENDENTE)
                .filter(this::pixAindaValido)
                .orElse(null);
    }

    private Pagamento buscarPixGraduacaoPendenteValido(InscricaoGraduacao inscricao) {
        return repository.findFirstByAcademiaIdAndInscricaoGraduacaoIdAndProvedorAndMetodoAndStatusInAndIdPagamentoExternoIsNotNullOrderByCriadoEmDesc(
                        inscricao.getAcademia().getId(), inscricao.getId(), provedorPagamento.nome(), MetodoPagamento.PIX,
                        List.of(StatusPagamento.PENDENTE, StatusPagamento.PROCESSANDO))
                .filter(this::pixAindaValido)
                .orElse(null);
    }

    private boolean pixAindaValido(Pagamento pagamento) {
        return pagamento.getDataExpiracao() != null
                && pagamento.getDataExpiracao().isAfter(LocalDateTime.now())
                && ((pagamento.getPixCopiaCola() != null && !pagamento.getPixCopiaCola().isBlank())
                || (pagamento.getPixQrCodeBase64() != null && !pagamento.getPixQrCodeBase64().isBlank())
                || (pagamento.getPixQrCodeUrl() != null && !pagamento.getPixQrCodeUrl().isBlank()));
    }

    private CobrancaPixResponse respostaPix(Pagamento pagamento) {
        return new CobrancaPixResponse(
                pagamento.getId(),
                pagamento.getFatura().getId(),
                pagamento.getStatus(),
                pagamento.getValor(),
                pagamento.getIdPagamentoExterno(),
                pagamento.getPixCopiaCola(),
                pagamento.getPixQrCodeUrl(),
                pagamento.getPixQrCodeBase64(),
                pagamento.getDataExpiracao()
        );
    }

    private CriarPixInscricaoGraduacaoResponse respostaPixInscricao(InscricaoGraduacao inscricao, Pagamento pagamento) {
        return new CriarPixInscricaoGraduacaoResponse(
                inscricao.getId(),
                inscricao.getEvento().getId(),
                inscricao.getGraduacaoDestino().getNome(),
                inscricao.getValor(),
                inscricao.getStatus(),
                pagamento.getId(),
                pagamento.getStatus(),
                pagamento.getIdPagamentoExterno(),
                pagamento.getPixCopiaCola(),
                pagamento.getPixQrCodeUrl(),
                pagamento.getPixQrCodeBase64(),
                pagamento.getDataExpiracao()
        );
    }

    private void validarInscricaoPermitePix(InscricaoGraduacao inscricao) {
        if (inscricao.getEvento().getStatus() == StatusEventoGraduacao.CANCELADO) {
            throw new RuntimeException("Evento cancelado não permite cobrança");
        }
        if (inscricao.getEvento().getStatus() != StatusEventoGraduacao.PUBLICADO) {
            throw new RuntimeException("Evento não publicado não permite cobrança");
        }
        if (inscricao.getStatus() == StatusInscricaoGraduacao.CANCELADA
                || inscricao.getStatus() == StatusInscricaoGraduacao.EXPIRADA
                || inscricao.getStatus() == StatusInscricaoGraduacao.CONCLUIDA) {
            throw new RuntimeException("Inscrição não permite nova cobrança Pix");
        }
        if (inscricao.getStatus() == StatusInscricaoGraduacao.PAGA || inscricao.getStatus() == StatusInscricaoGraduacao.CONFIRMADA) {
            throw new RuntimeException("Inscrição já está confirmada financeiramente");
        }
        if (inscricao.getValor() == null || inscricao.getValor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Inscrição sem valor pendente para Pix");
        }
    }

    private void atualizarInscricaoPorPagamento(Pagamento pagamento, StatusPagamento statusAtual) {
        InscricaoGraduacao inscricao = pagamento.getInscricaoGraduacao();
        if (inscricao == null) {
            return;
        }
        if (statusAtual == StatusPagamento.PAGO) {
            pagamento.setPagoEm(pagamento.getPagoEm() == null ? LocalDateTime.now() : pagamento.getPagoEm());
            inscricao.setStatus(StatusInscricaoGraduacao.PAGA);
            inscricao.setPagoEm(pagamento.getPagoEm());
            inscricao.setPagamento(pagamento);
        } else if (statusAtual == StatusPagamento.PROCESSANDO) {
            inscricao.setStatus(StatusInscricaoGraduacao.PAGAMENTO_PROCESSANDO);
            inscricao.setPagamento(pagamento);
        } else if (statusAtual == StatusPagamento.FALHOU
                || statusAtual == StatusPagamento.CANCELADO
                || statusAtual == StatusPagamento.ESTORNADO) {
            inscricao.setStatus(StatusInscricaoGraduacao.PENDENTE_PAGAMENTO);
            inscricao.setPagoEm(null);
        }
        inscricaoGraduacaoRepository.save(inscricao);
        notificacaoService.notificarPagamentoGraduacao(pagamento, statusAtual);
    }

    private void validarValorPagamentoManual(Fatura fatura, BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Valor do pagamento precisa ser maior que zero");
        }
        BigDecimal saldo = saldoRestante(fatura);
        if (valor.compareTo(saldo) > 0) {
            throw new RuntimeException("Pagamento maior que o saldo da fatura não é permitido neste MVP");
        }
    }

    private void validarValorWebhook(Pagamento pagamento, BigDecimal valorRecebido) {
        if (valorRecebido != null && valorRecebido.compareTo(pagamento.getValor()) != 0) {
            throw new RuntimeException("Valor confirmado pelo provedor diverge do pagamento local");
        }
    }

    private void atualizarStatusFaturaPorSaldo(Fatura fatura, Pagamento pagamento, BigDecimal valorAtual) {
        BigDecimal pagoAntes = valorOuZero(repository.somarPagamentosConfirmadosPorFatura(fatura.getId()));
        BigDecimal totalPago = pagoAntes.add(valorAtual);
        atualizarStatusFatura(fatura, totalPago);
    }

    private void atualizarStatusFaturaAposConfirmacao(Fatura fatura, Pagamento pagamento) {
        BigDecimal pagoAntes = pagamento.getId() == null
                ? valorOuZero(repository.somarPagamentosConfirmadosPorFatura(fatura.getId()))
                : valorOuZero(repository.somarPagamentosConfirmadosPorFaturaIgnorandoPagamento(fatura.getId(), pagamento.getId()));
        atualizarStatusFatura(fatura, pagoAntes.add(pagamento.getValor()));
    }

    private void atualizarStatusFatura(Fatura fatura, BigDecimal totalPago) {
        if (totalPago.compareTo(fatura.getValor()) >= 0) {
            fatura.setStatus(StatusFatura.PAGA);
        } else if (totalPago.compareTo(BigDecimal.ZERO) > 0) {
            fatura.setStatus(StatusFatura.PARCIALMENTE_PAGA);
        } else {
            fatura.setStatus(StatusFatura.PENDENTE);
        }
        fatura.setAtualizadoEm(LocalDateTime.now());
    }

    private BigDecimal saldoRestante(Fatura fatura) {
        BigDecimal pago = valorOuZero(repository.somarPagamentosConfirmadosPorFatura(fatura.getId()));
        BigDecimal saldo = fatura.getValor().subtract(pago);
        return saldo.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : saldo;
    }

    private BigDecimal valor(JsonNode node, String field) {
        JsonNode value = node.path(field);
        if (value.isMissingNode() || value.isNull()) {
            return null;
        }
        return value.decimalValue();
    }

    private BigDecimal valorOuZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private void logEventoProcessado(Pagamento pagamento, String tipoEvento, StatusPagamento statusAnterior,
                                     StatusPagamento statusAtual, StatusFatura statusFaturaAnterior) {
        logger.info("Evento Asaas processado: pagamentoId={}, tipoCobranca={}, faturaId={}, inscricaoId={}, evento={}, statusAnterior={}, statusAtual={}, faturaAnterior={}, faturaAtual={}, academiaId={}",
                pagamento.getId(), pagamento.getTipoCobranca(),
                pagamento.getFatura() == null ? null : pagamento.getFatura().getId(),
                pagamento.getInscricaoGraduacao() == null ? null : pagamento.getInscricaoGraduacao().getId(),
                tipoEvento, statusAnterior, statusAtual,
                statusFaturaAnterior, pagamento.getFatura() == null ? null : pagamento.getFatura().getStatus(),
                pagamento.getAcademia().getId());
    }

    private boolean metodoManualPermitido(MetodoPagamento metodo) {
        return metodo == MetodoPagamento.DINHEIRO
                || metodo == MetodoPagamento.PIX_MANUAL
                || metodo == MetodoPagamento.TRANSFERENCIA
                || metodo == MetodoPagamento.CARTAO_PRESENCIAL
                || metodo == MetodoPagamento.OUTRO;
    }
}
