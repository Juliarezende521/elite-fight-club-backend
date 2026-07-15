package Tic.Implementacao.service;

import Tic.Implementacao.dto.graduacao.avaliacao.*;
import Tic.Implementacao.model.*;
import Tic.Implementacao.repository.*;
import Tic.Implementacao.security.PermissaoService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AvaliacaoGraduacaoService {

    private final AvaliacaoGraduacaoRepository avaliacaoRepository;
    private final EventoGraduacaoRepository eventoRepository;
    private final InscricaoGraduacaoRepository inscricaoRepository;
    private final EventoGraduacaoPrajiedRepository eventoPrajiedRepository;
    private final AlunoRepository alunoRepository;
    private final HistoricoGraduacaoRepository historicoRepository;
    private final NotificacaoService notificacaoService;
    private final CertificadoGraduacaoService certificadoGraduacaoService;
    private final PermissaoService permissaoService;
    private final Clock clock;

    public AvaliacaoGraduacaoService(AvaliacaoGraduacaoRepository avaliacaoRepository,
                                     EventoGraduacaoRepository eventoRepository,
                                     InscricaoGraduacaoRepository inscricaoRepository,
                                     EventoGraduacaoPrajiedRepository eventoPrajiedRepository,
                                     AlunoRepository alunoRepository,
                                     HistoricoGraduacaoRepository historicoRepository,
                                     NotificacaoService notificacaoService,
                                     CertificadoGraduacaoService certificadoGraduacaoService,
                                     PermissaoService permissaoService,
                                     Clock clock) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.eventoRepository = eventoRepository;
        this.inscricaoRepository = inscricaoRepository;
        this.eventoPrajiedRepository = eventoPrajiedRepository;
        this.alunoRepository = alunoRepository;
        this.historicoRepository = historicoRepository;
        this.notificacaoService = notificacaoService;
        this.certificadoGraduacaoService = certificadoGraduacaoService;
        this.permissaoService = permissaoService;
        this.clock = clock;
    }

    @Transactional
    public AvaliacaoGraduacaoResponse registrarPresenca(Long eventoId, Long inscricaoId,
                                                        RegistrarPresencaGraduacaoRequest request) {
        permissaoService.exigirAdminOuProfessor();
        validarStatusPresenca(request.getStatus());
        EventoGraduacao evento = buscarEventoParaAlteracao(eventoId);
        InscricaoGraduacao inscricao = buscarInscricaoParaAlteracao(eventoId, inscricaoId);
        validarInscricaoAvaliavel(evento, inscricao, request.getObservacao());

        AvaliacaoGraduacao avaliacao = buscarOuCriarAvaliacao(inscricao);
        StatusPresencaGraduacao anterior = avaliacao.getStatusPresenca();
        LocalDateTime agora = LocalDateTime.now(clock);
        if (anterior != request.getStatus() || !textoIgual(avaliacao.getObservacaoPresenca(), request.getObservacao())) {
            avaliacao.setStatusPresencaAnterior(anterior);
            avaliacao.setStatusPresencaAtual(request.getStatus());
            avaliacao.setStatusPresenca(request.getStatus());
            avaliacao.setObservacaoPresenca(request.getObservacao());
            avaliacao.setPresencaRegistradaEm(agora);
            avaliacao.setPresencaRegistradaPor(permissaoService.usuarioAtual());
            avaliacao.setAtualizadoEm(agora);
        }

        if (request.getStatus() == StatusPresencaGraduacao.PRESENTE) {
            if (inscricao.getStatus() != StatusInscricaoGraduacao.APROVADA
                    && inscricao.getStatus() != StatusInscricaoGraduacao.REPROVADA
                    && inscricao.getStatus() != StatusInscricaoGraduacao.CONCLUIDA) {
                inscricao.setStatus(StatusInscricaoGraduacao.EM_AVALIACAO);
            }
        } else if (request.getStatus() == StatusPresencaGraduacao.AUSENTE) {
            inscricao.setStatus(StatusInscricaoGraduacao.AUSENTE);
            avaliacao.setResultado(ResultadoAvaliacaoGraduacao.NAO_AVALIADO);
            avaliacao.setResultadoAtual(ResultadoAvaliacaoGraduacao.NAO_AVALIADO);
        }

        inscricaoRepository.save(inscricao);
        AvaliacaoGraduacao salva = avaliacaoRepository.save(avaliacao);
        notificacaoService.notificarResultadoAvaliacao(salva);
        return AvaliacaoGraduacaoResponse.from(salva);
    }

    @Transactional
    public AvaliacaoGraduacaoResponse registrarAvaliacao(Long eventoId, Long inscricaoId,
                                                         RegistrarAvaliacaoGraduacaoRequest request) {
        permissaoService.exigirAdminOuProfessor();
        validarResultado(request);
        EventoGraduacao evento = buscarEventoParaAlteracao(eventoId);
        InscricaoGraduacao inscricao = buscarInscricaoParaAlteracao(eventoId, inscricaoId);
        validarInscricaoAvaliavel(evento, inscricao, request.getObservacaoTecnica());
        AvaliacaoGraduacao avaliacao = buscarOuCriarAvaliacao(inscricao);
        if (avaliacao.getStatusPresenca() != StatusPresencaGraduacao.PRESENTE) {
            throw new RuntimeException("Aluno precisa estar presente para ser avaliado");
        }

        ResultadoAvaliacaoGraduacao anterior = avaliacao.getResultado();
        LocalDateTime agora = LocalDateTime.now(clock);
        avaliacao.setResultadoAnterior(anterior);
        avaliacao.setResultadoAtual(request.getResultado());
        avaliacao.setResultado(request.getResultado());
        avaliacao.setNotaTecnica(request.getNotaTecnica());
        avaliacao.setObservacaoTecnica(request.getObservacaoTecnica());
        avaliacao.setAvaliadoEm(agora);
        avaliacao.setAvaliadoPor(permissaoService.usuarioAtual());
        avaliacao.setAtualizadoEm(agora);

        if (request.getResultado() == ResultadoAvaliacaoGraduacao.APROVADO) {
            inscricao.setStatus(StatusInscricaoGraduacao.APROVADA);
        } else if (request.getResultado() == ResultadoAvaliacaoGraduacao.REPROVADO) {
            inscricao.setStatus(StatusInscricaoGraduacao.REPROVADA);
        } else {
            inscricao.setStatus(StatusInscricaoGraduacao.EM_AVALIACAO);
        }

        inscricaoRepository.save(inscricao);
        AvaliacaoGraduacao salva = avaliacaoRepository.save(avaliacao);
        notificacaoService.notificarResultadoAvaliacao(salva);
        return AvaliacaoGraduacaoResponse.from(salva);
    }

    @Transactional
    public AvaliacaoGraduacaoResponse promoverAluno(Long eventoId, Long inscricaoId, PromoverAlunoEventoRequest request) {
        permissaoService.exigirAdminOuProfessor();
        buscarEventoParaAlteracao(eventoId);
        InscricaoGraduacao inscricao = buscarInscricaoParaAlteracao(eventoId, inscricaoId);
        AvaliacaoGraduacao avaliacao = avaliacaoRepository
                .findByAcademiaIdAndInscricaoIdForUpdate(permissaoService.academiaAtualId(), inscricaoId)
                .orElseThrow(() -> new RuntimeException("Avaliação não encontrada para a inscrição"));
        if (Boolean.TRUE.equals(avaliacao.getPromocaoConfirmada())) {
            return AvaliacaoGraduacaoResponse.from(avaliacao);
        }
        if (avaliacao.getStatusPresenca() != StatusPresencaGraduacao.PRESENTE) {
            throw new RuntimeException("Promoção exige presença confirmada");
        }
        if (avaliacao.getResultado() != ResultadoAvaliacaoGraduacao.APROVADO) {
            throw new RuntimeException("Somente alunos aprovados podem ser promovidos");
        }

        Aluno aluno = alunoRepository.findByIdAndAcademiaIdForUpdate(avaliacao.getAluno().getId(), permissaoService.academiaAtualId())
                .orElseThrow(() -> new RuntimeException("Aluno não encontrado para promoção"));
        Graduacao atualDoAluno = aluno.getGraduacaoEntidade();
        if (atualDoAluno == null || !atualDoAluno.getId().equals(avaliacao.getGraduacaoAtual().getId())) {
            throw new RuntimeException("Graduação atual do aluno mudou após a avaliação. Promoção bloqueada para revisão administrativa");
        }
        Graduacao destino = avaliacao.getGraduacaoDestino();
        if (!Boolean.TRUE.equals(destino.getAtiva())) {
            throw new RuntimeException("Graduação de destino está inativa");
        }
        if (destino.getAcademia() == null || !destino.getAcademia().getId().equals(permissaoService.academiaAtualId())) {
            throw new RuntimeException("Graduação de destino não pertence à academia atual");
        }
        if (destino.getOrdem() <= atualDoAluno.getOrdem()) {
            throw new RuntimeException("Graduação de destino precisa ser superior à graduação atual");
        }

        LocalDateTime agora = LocalDateTime.now(clock);
        HistoricoGraduacao historico = new HistoricoGraduacao();
        historico.setAcademia(avaliacao.getAcademia());
        historico.setAluno(aluno);
        historico.setGraduacaoAnterior(atualDoAluno);
        historico.setNovaGraduacao(destino);
        historico.setAlteradoEm(agora);
        historico.setAlteradoPor(permissaoService.usuarioAtual());
        historico.setMotivo(MotivoAlteracaoGraduacao.APROVACAO_AVALIACAO);
        historico.setObservacao(request == null ? null : request.getObservacao());
        historico.setPromocaoPorAvaliacao(true);
        historicoRepository.save(historico);

        aluno.setGraduacaoEntidade(destino);
        aluno.setGraduacao(destino.getNome());
        alunoRepository.save(aluno);

        avaliacao.setPromocaoConfirmada(true);
        avaliacao.setPromovidoEm(agora);
        avaliacao.setPromovidoPor(permissaoService.usuarioAtual());
        avaliacao.setHistoricoGerado(historico);
        avaliacao.setHistoricoGeradoFlag(true);
        avaliacao.setObservacaoPromocao(request == null ? null : request.getObservacao());
        avaliacao.setAtualizadoEm(agora);
        inscricao.setStatus(StatusInscricaoGraduacao.CONCLUIDA);
        inscricaoRepository.save(inscricao);
        AvaliacaoGraduacao salva = avaliacaoRepository.save(avaliacao);
        notificacaoService.notificarPromocaoConfirmada(salva, atualDoAluno, destino);
        return AvaliacaoGraduacaoResponse.from(salva);
    }

    @Transactional(readOnly = true)
    public PainelEventoGraduacaoResponse painel(Long eventoId) {
        permissaoService.exigirAdminOuProfessor();
        Long academiaId = permissaoService.academiaAtualId();
        EventoGraduacao evento = eventoRepository.findByIdAndAcademiaId(eventoId, academiaId)
                .orElseThrow(() -> new RuntimeException("Evento de graduação não encontrado com ID: " + eventoId));
        List<InscricaoGraduacao> inscricoes = inscricaoRepository.findByAcademiaIdAndEventoIdOrderByInscritoEmDesc(academiaId, eventoId);
        Map<Long, AvaliacaoGraduacao> avaliacoes = avaliacaoRepository.findByAcademiaIdAndEventoId(academiaId, eventoId)
                .stream()
                .collect(Collectors.toMap(a -> a.getInscricao().getId(), Function.identity()));

        List<ParticipanteGraduacaoResponse> participantes = inscricoes.stream()
                .map(i -> participante(i, avaliacoes.get(i.getId())))
                .toList();

        int totalPagos = (int) inscricoes.stream().filter(this::inscricaoFinanceiramenteOk).count();
        int presentes = (int) participantes.stream().filter(p -> p.getStatusPresenca() == StatusPresencaGraduacao.PRESENTE).count();
        int ausentes = (int) participantes.stream().filter(p -> p.getStatusPresenca() == StatusPresencaGraduacao.AUSENTE).count();
        int aprovados = (int) participantes.stream().filter(p -> p.getResultado() == ResultadoAvaliacaoGraduacao.APROVADO).count();
        int reprovados = (int) participantes.stream().filter(p -> p.getResultado() == ResultadoAvaliacaoGraduacao.REPROVADO).count();
        int promovidos = (int) participantes.stream().filter(p -> Boolean.TRUE.equals(p.getPromocaoConfirmada())).count();
        int pendentesAvaliacao = (int) participantes.stream()
                .filter(p -> p.getStatusPresenca() == StatusPresencaGraduacao.PRESENTE)
                .filter(p -> p.getResultado() == ResultadoAvaliacaoGraduacao.PENDENTE)
                .count();
        int aguardandoPromocao = (int) participantes.stream()
                .filter(p -> p.getResultado() == ResultadoAvaliacaoGraduacao.APROVADO)
                .filter(p -> !Boolean.TRUE.equals(p.getPromocaoConfirmada()))
                .count();

        return new PainelEventoGraduacaoResponse(evento.getId(), evento.getTitulo(), evento.getDataEvento(),
                evento.getStatus(), inscricoes.size(), totalPagos, presentes, ausentes, pendentesAvaliacao,
                aprovados, reprovados, promovidos, aguardandoPromocao, participantes);
    }

    @Transactional
    public PainelEventoGraduacaoResponse concluirEvento(Long eventoId, ConcluirEventoGraduacaoRequest request) {
        permissaoService.exigirAdminOuProfessor();
        Long academiaId = permissaoService.academiaAtualId();
        EventoGraduacao evento = eventoRepository.findByIdAndAcademiaIdForUpdate(eventoId, academiaId)
                .orElseThrow(() -> new RuntimeException("Evento de graduação não encontrado com ID: " + eventoId));
        if (evento.getStatus() == StatusEventoGraduacao.CANCELADO) {
            throw new RuntimeException("Evento cancelado não pode ser concluído");
        }
        if (LocalDate.now(clock).isBefore(evento.getDataEvento())) {
            throw new RuntimeException("Evento ainda não ocorreu");
        }
        boolean confirmarPendencias = request != null && Boolean.TRUE.equals(request.getConfirmarPendencias());
        List<InscricaoGraduacao> inscricoes = inscricaoRepository.findByAcademiaIdAndEventoIdOrderByInscritoEmDesc(academiaId, eventoId);
        for (InscricaoGraduacao inscricao : inscricoes) {
            if (inscricao.getStatus() == StatusInscricaoGraduacao.CANCELADA || inscricao.getStatus() == StatusInscricaoGraduacao.EXPIRADA) {
                continue;
            }
            AvaliacaoGraduacao avaliacao = buscarOuCriarAvaliacao(inscricao);
            if (avaliacao.getStatusPresenca() == StatusPresencaGraduacao.PENDENTE) {
                avaliacao.setStatusPresenca(StatusPresencaGraduacao.AUSENTE);
                avaliacao.setStatusPresencaAtual(StatusPresencaGraduacao.AUSENTE);
                avaliacao.setResultado(ResultadoAvaliacaoGraduacao.NAO_AVALIADO);
                avaliacao.setResultadoAtual(ResultadoAvaliacaoGraduacao.NAO_AVALIADO);
                avaliacao.setAtualizadoEm(LocalDateTime.now(clock));
                inscricao.setStatus(StatusInscricaoGraduacao.AUSENTE);
                avaliacaoRepository.save(avaliacao);
                inscricaoRepository.save(inscricao);
            }
            if (avaliacao.getResultado() == ResultadoAvaliacaoGraduacao.APROVADO
                    && !Boolean.TRUE.equals(avaliacao.getPromocaoConfirmada())
                    && !confirmarPendencias) {
                throw new RuntimeException("Evento possui aprovados aguardando confirmação de promoção");
            }
        }
        evento.setStatus(StatusEventoGraduacao.CONCLUIDO);
        evento.setAtualizadoEm(LocalDateTime.now(clock));
        eventoRepository.save(evento);
        certificadoGraduacaoService.emitirCertificadosEventoConcluido(evento);
        return painel(eventoId);
    }

    @Transactional(readOnly = true)
    public List<AvaliacaoGraduacaoResponse> listarAvaliacoesAlunoAtual() {
        Usuario usuario = alunoAtualUsuario();
        return avaliacaoRepository.findByAcademiaIdAndAlunoIdOrderByEventoDataEventoDesc(
                        permissaoService.academiaAtualId(), usuario.getAluno().getId())
                .stream()
                .map(AvaliacaoGraduacaoResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public AvaliacaoGraduacaoResponse buscarAvaliacaoAlunoAtual(Long avaliacaoId) {
        Usuario usuario = alunoAtualUsuario();
        AvaliacaoGraduacao avaliacao = avaliacaoRepository.findByAcademiaIdAndIdAndAlunoId(
                        permissaoService.academiaAtualId(), avaliacaoId, usuario.getAluno().getId())
                .orElseThrow(() -> new RuntimeException("Avaliação não encontrada"));
        return AvaliacaoGraduacaoResponse.from(avaliacao);
    }

    private AvaliacaoGraduacao buscarOuCriarAvaliacao(InscricaoGraduacao inscricao) {
        return avaliacaoRepository.findByAcademiaIdAndInscricaoIdForUpdate(inscricao.getAcademia().getId(), inscricao.getId())
                .orElseGet(() -> novaAvaliacao(inscricao));
    }

    private AvaliacaoGraduacao novaAvaliacao(InscricaoGraduacao inscricao) {
        Graduacao atual = inscricao.getAluno().getGraduacaoEntidade();
        if (atual == null) {
            throw new RuntimeException("Aluno não possui graduação atual para avaliação");
        }
        LocalDateTime agora = LocalDateTime.now(clock);
        AvaliacaoGraduacao avaliacao = new AvaliacaoGraduacao();
        avaliacao.setAcademia(inscricao.getAcademia());
        avaliacao.setInscricao(inscricao);
        avaliacao.setEvento(inscricao.getEvento());
        avaliacao.setAluno(inscricao.getAluno());
        avaliacao.setGraduacaoAtual(atual);
        avaliacao.setGraduacaoDestino(inscricao.getGraduacaoDestino());
        avaliacao.setStatusPresenca(StatusPresencaGraduacao.PENDENTE);
        avaliacao.setStatusPresencaAtual(StatusPresencaGraduacao.PENDENTE);
        avaliacao.setResultado(ResultadoAvaliacaoGraduacao.PENDENTE);
        avaliacao.setResultadoAtual(ResultadoAvaliacaoGraduacao.PENDENTE);
        avaliacao.setPromocaoConfirmada(false);
        avaliacao.setHistoricoGeradoFlag(false);
        avaliacao.setCriadoEm(agora);
        avaliacao.setAtualizadoEm(agora);
        return avaliacaoRepository.save(avaliacao);
    }

    private EventoGraduacao buscarEventoParaAlteracao(Long eventoId) {
        EventoGraduacao evento = eventoRepository.findByIdAndAcademiaIdForUpdate(eventoId, permissaoService.academiaAtualId())
                .orElseThrow(() -> new RuntimeException("Evento de graduação não encontrado com ID: " + eventoId));
        if (evento.getStatus() == StatusEventoGraduacao.RASCUNHO
                || evento.getStatus() == StatusEventoGraduacao.CANCELADO
                || evento.getStatus() == StatusEventoGraduacao.CONCLUIDO) {
            throw new RuntimeException("Evento não permite avaliação nesse status");
        }
        return evento;
    }

    private InscricaoGraduacao buscarInscricaoParaAlteracao(Long eventoId, Long inscricaoId) {
        return inscricaoRepository.findByIdAndAcademiaIdAndEventoIdForUpdate(
                        inscricaoId, permissaoService.academiaAtualId(), eventoId)
                .orElseThrow(() -> new RuntimeException("Inscrição não encontrada para o evento"));
    }

    private void validarInscricaoAvaliavel(EventoGraduacao evento, InscricaoGraduacao inscricao, String justificativa) {
        if (!inscricao.getEvento().getId().equals(evento.getId())) {
            throw new RuntimeException("Inscrição não pertence ao evento");
        }
        if (inscricao.getStatus() == StatusInscricaoGraduacao.CANCELADA
                || inscricao.getStatus() == StatusInscricaoGraduacao.EXPIRADA) {
            throw new RuntimeException("Inscrição cancelada ou expirada não pode ser avaliada");
        }
        if (!Boolean.TRUE.equals(inscricao.getAluno().getAtivo())) {
            throw new RuntimeException("Aluno inativo não pode ser avaliado");
        }
        if (eventoPrajiedRepository.findByEventoIdAndGraduacaoDestinoIdAndAtivoTrue(
                evento.getId(), inscricao.getGraduacaoDestino().getId()).isEmpty()) {
            throw new RuntimeException("Graduação de destino não está disponível no evento");
        }
        if (!inscricaoFinanceiramenteOk(inscricao)) {
            Usuario usuario = permissaoService.usuarioAtual();
            if (usuario.getPerfil() != PerfilUsuario.ADMIN || justificativa == null || justificativa.isBlank()) {
                throw new RuntimeException("Avaliação de inscrição com pagamento pendente exige ADMIN e justificativa");
            }
        }
    }

    private boolean inscricaoFinanceiramenteOk(InscricaoGraduacao inscricao) {
        if (inscricao.getValor() == null || inscricao.getValor().compareTo(BigDecimal.ZERO) == 0) {
            return inscricao.getStatus() == StatusInscricaoGraduacao.CONFIRMADA
                    || inscricao.getStatus() == StatusInscricaoGraduacao.EM_AVALIACAO
                    || inscricao.getStatus() == StatusInscricaoGraduacao.APROVADA
                    || inscricao.getStatus() == StatusInscricaoGraduacao.REPROVADA
                    || inscricao.getStatus() == StatusInscricaoGraduacao.CONCLUIDA;
        }
        if (inscricao.getPagamento() != null && inscricao.getPagamento().getStatus() == StatusPagamento.PAGO) {
            return true;
        }
        return inscricao.getStatus() == StatusInscricaoGraduacao.PAGA
                || inscricao.getStatus() == StatusInscricaoGraduacao.EM_AVALIACAO
                || inscricao.getStatus() == StatusInscricaoGraduacao.APROVADA
                || inscricao.getStatus() == StatusInscricaoGraduacao.REPROVADA
                || inscricao.getStatus() == StatusInscricaoGraduacao.CONCLUIDA;
    }

    private ParticipanteGraduacaoResponse participante(InscricaoGraduacao inscricao, AvaliacaoGraduacao avaliacao) {
        StatusPresencaGraduacao presenca = avaliacao == null ? StatusPresencaGraduacao.PENDENTE : avaliacao.getStatusPresenca();
        ResultadoAvaliacaoGraduacao resultado = avaliacao == null ? ResultadoAvaliacaoGraduacao.PENDENTE : avaliacao.getResultado();
        Boolean promocaoConfirmada = avaliacao != null && Boolean.TRUE.equals(avaliacao.getPromocaoConfirmada());
        LocalDateTime promovidoEm = avaliacao == null ? null : avaliacao.getPromovidoEm();
        String graduacaoAtual = avaliacao == null
                ? nomeGraduacao(inscricao.getAluno().getGraduacaoEntidade())
                : avaliacao.getGraduacaoAtual().getNome();
        StatusPagamento statusPagamento = inscricao.getPagamento() == null ? null : inscricao.getPagamento().getStatus();
        return new ParticipanteGraduacaoResponse(inscricao.getId(), inscricao.getAluno().getId(), inscricao.getAluno().getNome(),
                graduacaoAtual, inscricao.getGraduacaoDestino().getNome(), statusPagamento, inscricao.getStatus(),
                presenca, resultado, promocaoConfirmada, promovidoEm);
    }

    private void validarStatusPresenca(StatusPresencaGraduacao status) {
        if (status == null || status == StatusPresencaGraduacao.PENDENTE) {
            throw new RuntimeException("Status de presença inválido para registro");
        }
    }

    private void validarResultado(RegistrarAvaliacaoGraduacaoRequest request) {
        if (request.getResultado() == ResultadoAvaliacaoGraduacao.PENDENTE
                || request.getResultado() == ResultadoAvaliacaoGraduacao.NAO_AVALIADO) {
            throw new RuntimeException("Resultado inválido para avaliação técnica");
        }
        if (request.getNotaTecnica() != null
                && (request.getNotaTecnica().compareTo(BigDecimal.ZERO) < 0
                || request.getNotaTecnica().compareTo(BigDecimal.TEN) > 0)) {
            throw new RuntimeException("Nota técnica deve estar entre 0 e 10");
        }
        if (request.getResultado() == ResultadoAvaliacaoGraduacao.REPROVADO
                && (request.getObservacaoTecnica() == null || request.getObservacaoTecnica().isBlank())) {
            throw new RuntimeException("Reprovação exige observação técnica");
        }
    }

    private Usuario alunoAtualUsuario() {
        Usuario usuario = permissaoService.usuarioAtual();
        if (usuario.getPerfil() != PerfilUsuario.ALUNO || usuario.getAluno() == null) {
            throw new AccessDeniedException("Acesso permitido apenas para o aluno autenticado");
        }
        return usuario;
    }

    private String nomeGraduacao(Graduacao graduacao) {
        return graduacao == null ? null : graduacao.getNome();
    }

    private boolean textoIgual(String atual, String novo) {
        String a = atual == null ? "" : atual;
        String b = novo == null ? "" : novo;
        return a.equals(b);
    }
}
