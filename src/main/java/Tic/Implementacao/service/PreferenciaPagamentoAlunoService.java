package Tic.Implementacao.service;

import Tic.Implementacao.dto.financeiro.preferencia.ContextoPagamentoAluno;
import Tic.Implementacao.dto.financeiro.preferencia.PreferenciaPagamentoRequest;
import Tic.Implementacao.dto.financeiro.preferencia.PreferenciaPagamentoResponse;
import Tic.Implementacao.dto.financeiro.preferencia.ResumoPreferenciaPagamentoResponse;
import Tic.Implementacao.model.*;
import Tic.Implementacao.repository.AlunoRepository;
import Tic.Implementacao.repository.PagamentoRepository;
import Tic.Implementacao.repository.PreferenciaPagamentoAlunoRepository;
import Tic.Implementacao.security.PermissaoService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PreferenciaPagamentoAlunoService {

    private final PreferenciaPagamentoAlunoRepository preferenciaRepository;
    private final AlunoRepository alunoRepository;
    private final PagamentoRepository pagamentoRepository;
    private final PermissaoService permissaoService;
    private final Clock clock;

    public PreferenciaPagamentoAlunoService(PreferenciaPagamentoAlunoRepository preferenciaRepository,
                                            AlunoRepository alunoRepository,
                                            PagamentoRepository pagamentoRepository,
                                            PermissaoService permissaoService,
                                            Clock clock) {
        this.preferenciaRepository = preferenciaRepository;
        this.alunoRepository = alunoRepository;
        this.pagamentoRepository = pagamentoRepository;
        this.permissaoService = permissaoService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public Optional<PreferenciaPagamentoResponse> buscarDoAlunoAtual() {
        Usuario usuario = alunoAtual();
        Long academiaId = permissaoService.academiaAtualId();
        return preferenciaRepository.findByAcademiaIdAndAlunoIdAndAtivaTrue(academiaId, usuario.getAluno().getId())
                .map(PreferenciaPagamentoResponse::from);
    }

    @Transactional
    public PreferenciaPagamentoResponse salvarDoAlunoAtual(PreferenciaPagamentoRequest request) {
        Usuario usuario = alunoAtual();
        Long academiaId = permissaoService.academiaAtualId();
        Aluno aluno = usuario.getAluno();
        PreferenciaPagamentoAluno preferencia = preferenciaRepository.findByAcademiaIdAndAlunoId(academiaId, aluno.getId())
                .orElseGet(() -> novaPreferencia(aluno, usuario));

        aplicarFormaEscolhida(preferencia, request.getFormaPreferida(), usuario);
        return PreferenciaPagamentoResponse.from(preferenciaRepository.save(preferencia));
    }

    @Transactional
    public void removerDoAlunoAtual() {
        Usuario usuario = alunoAtual();
        Long academiaId = permissaoService.academiaAtualId();
        preferenciaRepository.findByAcademiaIdAndAlunoIdAndAtivaTrue(academiaId, usuario.getAluno().getId())
                .ifPresent(preferencia -> {
                    preferencia.setAtiva(false);
                    preferencia.setPagamentoAutomaticoAutorizado(false);
                    preferencia.setStatusAutorizacao(StatusAutorizacaoPagamento.CANCELADA);
                    preferencia.setAtualizadaEm(LocalDateTime.now(clock));
                    preferencia.setAtualizadaPor(usuario);
                    preferenciaRepository.save(preferencia);
                });
    }

    @Transactional(readOnly = true)
    public Optional<PreferenciaPagamentoResponse> buscarPorAluno(Long alunoId) {
        permissaoService.exigirAdminOuProfessor();
        Long academiaId = permissaoService.academiaAtualId();
        alunoRepository.findByIdAndAcademiaId(alunoId, academiaId)
                .orElseThrow(() -> new RuntimeException("Aluno não encontrado com ID: " + alunoId));
        return preferenciaRepository.findByAcademiaIdAndAlunoIdAndAtivaTrue(academiaId, alunoId)
                .map(PreferenciaPagamentoResponse::from);
    }

    @Transactional
    public PreferenciaPagamentoResponse atualizarAutorizacaoExterna(Long alunoId, FormaPagamentoPreferida forma,
                                                                    StatusAutorizacaoPagamento status,
                                                                    String identificadorExterno,
                                                                    String descricaoMascarada) {
        permissaoService.exigirAdmin();
        Usuario usuario = permissaoService.usuarioAtual();
        Long academiaId = permissaoService.academiaAtualId();
        Aluno aluno = alunoRepository.findByIdAndAcademiaId(alunoId, academiaId)
                .orElseThrow(() -> new RuntimeException("Aluno não encontrado com ID: " + alunoId));
        PreferenciaPagamentoAluno preferencia = preferenciaRepository.findByAcademiaIdAndAlunoId(academiaId, alunoId)
                .orElseGet(() -> novaPreferencia(aluno, usuario));

        preferencia.setFormaPreferida(forma);
        preferencia.setStatusAutorizacao(status);
        preferencia.setIdentificadorExternoMetodo(vazioParaNulo(identificadorExterno));
        preferencia.setDescricaoMascarada(vazioParaNulo(descricaoMascarada));
        preferencia.setPagamentoAutomaticoAutorizado(status == StatusAutorizacaoPagamento.ATIVA);
        preferencia.setAtiva(true);
        preferencia.setAtualizadaEm(LocalDateTime.now(clock));
        preferencia.setAtualizadaPor(usuario);
        return PreferenciaPagamentoResponse.from(preferenciaRepository.save(preferencia));
    }

    @Transactional(readOnly = true)
    public ResumoPreferenciaPagamentoResponse resumoDashboard(Long academiaId, Long alunoId) {
        return preferenciaRepository.findByAcademiaIdAndAlunoIdAndAtivaTrue(academiaId, alunoId)
                .map(preferencia -> new ResumoPreferenciaPagamentoResponse(
                        preferencia.getFormaPreferida().name(),
                        preferencia.getStatusAutorizacao().name(),
                        preferencia.getStatusAutorizacao() == StatusAutorizacaoPagamento.ATIVA
                                && Boolean.TRUE.equals(preferencia.getPagamentoAutomaticoAutorizado()),
                        preferencia.getDescricaoMascarada()))
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public ContextoPagamentoAluno obterContextoPagamento(Fatura fatura) {
        if (fatura == null || fatura.getAcademia() == null || fatura.getAluno() == null || fatura.getId() == null) {
            return contextoVazio();
        }
        Long academiaId = fatura.getAcademia().getId();
        Long alunoId = fatura.getAluno().getId();
        Optional<PreferenciaPagamentoAluno> preferencia = preferenciaRepository
                .findByAcademiaIdAndAlunoIdAndAtivaTrue(academiaId, alunoId);
        List<Pagamento> pagamentos = pagamentoRepository.findMensalidadesComFaturaPorFaturas(academiaId, List.of(fatura.getId()));
        return montarContextoPagamento(fatura, pagamentos, preferencia.orElse(null));
    }

    public ContextoPagamentoAluno montarContextoPagamento(Fatura fatura, List<Pagamento> pagamentos,
                                                          PreferenciaPagamentoAluno preferencia) {
        if (fatura == null) {
            return contextoVazio();
        }
        List<Pagamento> pagamentosSeguros = pagamentos == null ? List.of() : pagamentos;
        boolean possuiProcessando = pagamentosSeguros.stream().anyMatch(p -> p.getStatus() == StatusPagamento.PROCESSANDO);
        boolean possuiPixValido = pagamentosSeguros.stream().anyMatch(this::pixPendenteValido);
        boolean possuiBoletoValido = pagamentosSeguros.stream().anyMatch(this::boletoPendenteValido);

        if (preferencia == null) {
            return new ContextoPagamentoAluno(null, null, false, possuiProcessando, possuiPixValido, possuiBoletoValido);
        }
        boolean automaticoAtivo = preferencia.getStatusAutorizacao() == StatusAutorizacaoPagamento.ATIVA
                && Boolean.TRUE.equals(preferencia.getPagamentoAutomaticoAutorizado());
        return new ContextoPagamentoAluno(preferencia.getFormaPreferida(), preferencia.getStatusAutorizacao(), automaticoAtivo,
                possuiProcessando, possuiPixValido, possuiBoletoValido);
    }

    private PreferenciaPagamentoAluno novaPreferencia(Aluno aluno, Usuario usuario) {
        LocalDateTime agora = LocalDateTime.now(clock);
        PreferenciaPagamentoAluno preferencia = new PreferenciaPagamentoAluno();
        preferencia.setAluno(aluno);
        preferencia.setAcademia(aluno.getAcademia());
        preferencia.setCriadaPor(usuario);
        preferencia.setAtualizadaPor(usuario);
        preferencia.setCriadaEm(agora);
        preferencia.setAtualizadaEm(agora);
        return preferencia;
    }

    private void aplicarFormaEscolhida(PreferenciaPagamentoAluno preferencia, FormaPagamentoPreferida forma, Usuario usuario) {
        preferencia.setFormaPreferida(forma);
        preferencia.setAtiva(true);
        preferencia.setPagamentoAutomaticoAutorizado(false);
        preferencia.setStatusAutorizacao(statusInicial(forma));
        preferencia.setProvedor(null);
        preferencia.setIdentificadorExternoMetodo(null);
        preferencia.setDescricaoMascarada(null);
        preferencia.setAtualizadaPor(usuario);
        preferencia.setAtualizadaEm(LocalDateTime.now(clock));
    }

    private StatusAutorizacaoPagamento statusInicial(FormaPagamentoPreferida forma) {
        return forma == FormaPagamentoPreferida.PIX_AUTOMATICO
                ? StatusAutorizacaoPagamento.PENDENTE
                : StatusAutorizacaoPagamento.NAO_SOLICITADA;
    }

    private Usuario alunoAtual() {
        Usuario usuario = permissaoService.usuarioAtual();
        if (usuario.getPerfil() != PerfilUsuario.ALUNO || usuario.getAluno() == null) {
            throw new AccessDeniedException("Preferência de pagamento disponível apenas para aluno autenticado");
        }
        return usuario;
    }

    private boolean pixPendenteValido(Pagamento pagamento) {
        return pagamento.getStatus() == StatusPagamento.PENDENTE
                && pagamento.getMetodo() == MetodoPagamento.PIX
                && pagamento.getDataExpiracao() != null
                && pagamento.getDataExpiracao().isAfter(LocalDateTime.now(clock))
                && pagamento.getIdPagamentoExterno() != null
                && !pagamento.getIdPagamentoExterno().isBlank();
    }

    private boolean boletoPendenteValido(Pagamento pagamento) {
        return pagamento.getStatus() == StatusPagamento.PENDENTE
                && pagamento.getMetodo() == MetodoPagamento.BOLETO
                && pagamento.getDataExpiracao() != null
                && pagamento.getDataExpiracao().isAfter(LocalDateTime.now(clock))
                && pagamento.getIdPagamentoExterno() != null
                && !pagamento.getIdPagamentoExterno().isBlank();
    }

    private ContextoPagamentoAluno contextoVazio() {
        return new ContextoPagamentoAluno(null, null, false, false, false);
    }

    private String vazioParaNulo(String valor) {
        return valor == null || valor.isBlank() ? null : valor;
    }
}
