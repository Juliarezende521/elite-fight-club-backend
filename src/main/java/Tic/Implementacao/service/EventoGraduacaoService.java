package Tic.Implementacao.service;

import Tic.Implementacao.dto.graduacao.ElegibilidadeGraduacaoResponse;
import Tic.Implementacao.dto.graduacao.convite.*;
import Tic.Implementacao.dto.graduacao.evento.*;
import Tic.Implementacao.dto.graduacao.inscricao.InscricaoGraduacaoResponse;
import Tic.Implementacao.model.*;
import Tic.Implementacao.repository.*;
import Tic.Implementacao.security.PermissaoService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class EventoGraduacaoService {

    private static final List<StatusInscricaoGraduacao> STATUS_INSCRICAO_ATIVA = List.of(
            StatusInscricaoGraduacao.PENDENTE_PAGAMENTO,
            StatusInscricaoGraduacao.CONFIRMADA
    );
    private static final List<StatusConviteGraduacao> STATUS_CONVITE_RESPONSIVO = List.of(
            StatusConviteGraduacao.PENDENTE,
            StatusConviteGraduacao.VISUALIZADO,
            StatusConviteGraduacao.ACEITO
    );

    private final EventoGraduacaoRepository eventoRepository;
    private final EventoGraduacaoPrajiedRepository prajiedRepository;
    private final ConviteGraduacaoRepository conviteRepository;
    private final InscricaoGraduacaoRepository inscricaoRepository;
    private final AlunoRepository alunoRepository;
    private final GraduacaoRepository graduacaoRepository;
    private final GraduacaoAlunoService graduacaoAlunoService;
    private final PermissaoService permissaoService;
    private final Clock clock;

    public EventoGraduacaoService(EventoGraduacaoRepository eventoRepository,
                                  EventoGraduacaoPrajiedRepository prajiedRepository,
                                  ConviteGraduacaoRepository conviteRepository,
                                  InscricaoGraduacaoRepository inscricaoRepository,
                                  AlunoRepository alunoRepository,
                                  GraduacaoRepository graduacaoRepository,
                                  GraduacaoAlunoService graduacaoAlunoService,
                                  PermissaoService permissaoService,
                                  Clock clock) {
        this.eventoRepository = eventoRepository;
        this.prajiedRepository = prajiedRepository;
        this.conviteRepository = conviteRepository;
        this.inscricaoRepository = inscricaoRepository;
        this.alunoRepository = alunoRepository;
        this.graduacaoRepository = graduacaoRepository;
        this.graduacaoAlunoService = graduacaoAlunoService;
        this.permissaoService = permissaoService;
        this.clock = clock;
    }

    @Transactional
    public EventoGraduacaoResponse criarEvento(EventoGraduacaoRequest request) {
        permissaoService.exigirAdminOuProfessor();
        validarDadosEvento(request, true);
        LocalDateTime agora = LocalDateTime.now(clock);
        EventoGraduacao evento = new EventoGraduacao();
        preencherEvento(evento, request);
        evento.setAcademia(permissaoService.academiaAtual());
        evento.setCriadoPor(permissaoService.usuarioAtual());
        evento.setCriadoEm(agora);
        evento.setAtualizadoEm(agora);
        evento.setStatus(request.getStatus() == null ? StatusEventoGraduacao.RASCUNHO : request.getStatus());
        if (evento.getStatus() == StatusEventoGraduacao.CONCLUIDO || evento.getStatus() == StatusEventoGraduacao.CANCELADO) {
            throw new RuntimeException("Evento novo não pode iniciar cancelado ou concluído");
        }
        return EventoGraduacaoResponse.from(eventoRepository.save(evento));
    }

    @Transactional(readOnly = true)
    public List<EventoGraduacaoResponse> listarEventos() {
        return eventoRepository.findByAcademiaIdOrderByDataEventoDesc(permissaoService.academiaAtualId())
                .stream()
                .map(EventoGraduacaoResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public EventoGraduacaoResponse buscarEvento(Long id) {
        return EventoGraduacaoResponse.from(buscarEventoDaAcademia(id));
    }

    @Transactional
    public EventoGraduacaoResponse atualizarEvento(Long id, EventoGraduacaoRequest request) {
        permissaoService.exigirAdminOuProfessor();
        validarDadosEvento(request, false);
        EventoGraduacao evento = buscarEventoDaAcademia(id);
        if (evento.getStatus() == StatusEventoGraduacao.CONCLUIDO) {
            throw new RuntimeException("Evento concluído não pode ser editado");
        }
        preencherEvento(evento, request);
        evento.setAtualizadoEm(LocalDateTime.now(clock));
        if (request.getStatus() != null) {
            evento.setStatus(request.getStatus());
        }
        return EventoGraduacaoResponse.from(eventoRepository.save(evento));
    }

    @Transactional
    public EventoGraduacaoResponse publicarEvento(Long eventoId, PublicarEventoGraduacaoRequest request) {
        permissaoService.exigirAdminOuProfessor();
        Long academiaId = permissaoService.academiaAtualId();
        EventoGraduacao evento = buscarEventoDaAcademia(eventoId);
        if (evento.getStatus() == StatusEventoGraduacao.CONCLUIDO || evento.getStatus() == StatusEventoGraduacao.CANCELADO) {
            throw new RuntimeException("Evento não pode ser publicado nesse status");
        }
        if (prajiedRepository.findByEventoIdAndAtivoTrueOrderByGraduacaoDestinoOrdemAsc(eventoId).isEmpty()) {
            throw new RuntimeException("Evento precisa ter pelo menos um Prajied disponível");
        }
        evento.setStatus(StatusEventoGraduacao.PUBLICADO);
        evento.setAtualizadoEm(LocalDateTime.now(clock));
        eventoRepository.save(evento);
        if (Boolean.TRUE.equals(request.getCriarConvitesAutomaticos())) {
            criarConvitesAutomaticos(evento, academiaId);
        }
        return EventoGraduacaoResponse.from(evento);
    }

    @Transactional
    public EventoGraduacaoResponse cancelarEvento(Long eventoId) {
        permissaoService.exigirAdminOuProfessor();
        Long academiaId = permissaoService.academiaAtualId();
        EventoGraduacao evento = buscarEventoDaAcademia(eventoId);
        if (evento.getStatus() == StatusEventoGraduacao.CONCLUIDO) {
            throw new RuntimeException("Evento concluído não pode ser cancelado");
        }
        evento.setStatus(StatusEventoGraduacao.CANCELADO);
        evento.setAtualizadoEm(LocalDateTime.now(clock));
        conviteRepository.findByAcademiaIdAndEventoIdAndStatusIn(academiaId, eventoId,
                        List.of(StatusConviteGraduacao.PENDENTE, StatusConviteGraduacao.VISUALIZADO))
                .forEach(convite -> {
                    convite.setStatus(StatusConviteGraduacao.CANCELADO);
                    convite.setRespondidoEm(LocalDateTime.now(clock));
                    conviteRepository.save(convite);
                });
        return EventoGraduacaoResponse.from(eventoRepository.save(evento));
    }

    @Transactional
    public EventoGraduacaoPrajiedResponse adicionarPrajied(Long eventoId, EventoGraduacaoPrajiedRequest request) {
        permissaoService.exigirAdminOuProfessor();
        EventoGraduacao evento = buscarEventoDaAcademia(eventoId);
        Graduacao graduacao = buscarGraduacaoAtiva(request.getGraduacaoDestinoId());
        if (!graduacao.getAcademia().getId().equals(evento.getAcademia().getId())) {
            throw new RuntimeException("Graduação não pertence à academia do evento");
        }
        if (prajiedRepository.existsByEventoIdAndGraduacaoDestinoId(eventoId, graduacao.getId())) {
            throw new RuntimeException("Prajied já cadastrado nesse evento");
        }
        EventoGraduacaoPrajied item = new EventoGraduacaoPrajied();
        item.setEvento(evento);
        item.setGraduacaoDestino(graduacao);
        item.setValor(valorEventoOuPadrao(request.getValor(), graduacao));
        item.setLimiteVagas(request.getLimiteVagas());
        item.setAtivo(request.getAtivo() == null || request.getAtivo());
        return EventoGraduacaoPrajiedResponse.from(prajiedRepository.save(item));
    }

    @Transactional
    public EventoGraduacaoPrajiedResponse atualizarPrajied(Long eventoId, Long itemId, EventoGraduacaoPrajiedRequest request) {
        permissaoService.exigirAdminOuProfessor();
        buscarEventoDaAcademia(eventoId);
        EventoGraduacaoPrajied item = prajiedRepository.findByIdAndEventoIdAndEventoAcademiaId(itemId, eventoId, permissaoService.academiaAtualId())
                .orElseThrow(() -> new RuntimeException("Prajied do evento não encontrado"));
        Graduacao graduacao = buscarGraduacaoAtiva(request.getGraduacaoDestinoId());
        item.setGraduacaoDestino(graduacao);
        item.setValor(valorEventoOuPadrao(request.getValor(), graduacao));
        item.setLimiteVagas(request.getLimiteVagas());
        item.setAtivo(request.getAtivo() == null || request.getAtivo());
        return EventoGraduacaoPrajiedResponse.from(prajiedRepository.save(item));
    }

    @Transactional
    public void removerPrajied(Long eventoId, Long itemId) {
        permissaoService.exigirAdminOuProfessor();
        buscarEventoDaAcademia(eventoId);
        EventoGraduacaoPrajied item = prajiedRepository.findByIdAndEventoIdAndEventoAcademiaId(itemId, eventoId, permissaoService.academiaAtualId())
                .orElseThrow(() -> new RuntimeException("Prajied do evento não encontrado"));
        item.setAtivo(false);
        prajiedRepository.save(item);
    }

    @Transactional
    public ConviteGraduacaoResponse criarConviteManual(Long eventoId, ConviteGraduacaoRequest request) {
        permissaoService.exigirAdminOuProfessor();
        Long academiaId = permissaoService.academiaAtualId();
        EventoGraduacao evento = buscarEventoDaAcademia(eventoId);
        Aluno aluno = buscarAlunoDaAcademia(request.getAlunoId(), academiaId);
        Graduacao destino = buscarGraduacaoAtiva(request.getGraduacaoDestinoId());
        garantirPrajiedDisponivel(eventoId, destino.getId());
        ElegibilidadeGraduacaoResponse elegibilidade = graduacaoAlunoService.calcularElegibilidade(aluno);
        boolean destinoEhProxima = destino.getId().equals(elegibilidade.getProximaGraduacaoId());
        boolean elegivel = Boolean.TRUE.equals(elegibilidade.getElegivelParaAvaliacao()) && destinoEhProxima;
        if (!elegivel && (request.getJustificativa() == null || request.getJustificativa().isBlank())) {
            throw new RuntimeException("Convite manual para aluno não elegível exige justificativa");
        }
        return ConviteGraduacaoResponse.from(criarConvite(evento, aluno, destino, request.getJustificativa()));
    }

    @Transactional(readOnly = true)
    public List<ConviteGraduacaoResponse> listarConvitesEvento(Long eventoId) {
        Long academiaId = permissaoService.academiaAtualId();
        buscarEventoDaAcademia(eventoId);
        return conviteRepository.findByAcademiaIdAndEventoIdOrderByEnviadoEmDesc(academiaId, eventoId)
                .stream()
                .map(ConviteGraduacaoResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InscricaoGraduacaoResponse> listarInscricoesEvento(Long eventoId) {
        Long academiaId = permissaoService.academiaAtualId();
        buscarEventoDaAcademia(eventoId);
        return inscricaoRepository.findByAcademiaIdAndEventoIdOrderByInscritoEmDesc(academiaId, eventoId)
                .stream()
                .map(InscricaoGraduacaoResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EventoGraduacaoAlunoResponse> listarEventosDoAlunoAtual() {
        Usuario usuario = alunoAtualUsuario();
        Long academiaId = permissaoService.academiaAtualId();
        return eventoRepository.findByAcademiaIdAndStatusOrderByDataEventoAsc(academiaId, StatusEventoGraduacao.PUBLICADO)
                .stream()
                .map(evento -> montarEventoAluno(evento, usuario.getAluno()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    @Transactional(readOnly = true)
    public EventoGraduacaoAlunoResponse buscarEventoDoAlunoAtual(Long eventoId) {
        Usuario usuario = alunoAtualUsuario();
        EventoGraduacao evento = buscarEventoDaAcademia(eventoId);
        if (evento.getStatus() != StatusEventoGraduacao.PUBLICADO) {
            throw new RuntimeException("Evento não disponível para alunos");
        }
        return montarEventoAluno(evento, usuario.getAluno())
                .orElseThrow(() -> new RuntimeException("Evento não disponível para este aluno"));
    }

    @Transactional(readOnly = true)
    public List<ConviteGraduacaoResponse> listarConvitesDoAlunoAtual() {
        Usuario usuario = alunoAtualUsuario();
        return conviteRepository.findByAcademiaIdAndAlunoIdOrderByEnviadoEmDesc(
                        permissaoService.academiaAtualId(), usuario.getAluno().getId())
                .stream()
                .map(ConviteGraduacaoResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InscricaoGraduacaoResponse> listarInscricoesDoAlunoAtual() {
        Usuario usuario = alunoAtualUsuario();
        return inscricaoRepository.findByAcademiaIdAndAlunoIdOrderByInscritoEmDesc(
                        permissaoService.academiaAtualId(), usuario.getAluno().getId())
                .stream()
                .map(InscricaoGraduacaoResponse::from)
                .toList();
    }

    @Transactional
    public ConviteGraduacaoResponse aceitarConvite(Long conviteId) {
        ConviteGraduacao convite = buscarConviteDoAluno(conviteId);
        if (convite.getEvento().getStatus() == StatusEventoGraduacao.CANCELADO) {
            throw new RuntimeException("Evento cancelado não aceita resposta");
        }
        convite.setStatus(StatusConviteGraduacao.ACEITO);
        convite.setVisualizadoEm(convite.getVisualizadoEm() == null ? LocalDateTime.now(clock) : convite.getVisualizadoEm());
        convite.setRespondidoEm(LocalDateTime.now(clock));
        return ConviteGraduacaoResponse.from(conviteRepository.save(convite));
    }

    @Transactional
    public ConviteGraduacaoResponse recusarConvite(Long conviteId) {
        ConviteGraduacao convite = buscarConviteDoAluno(conviteId);
        convite.setStatus(StatusConviteGraduacao.RECUSADO);
        convite.setVisualizadoEm(convite.getVisualizadoEm() == null ? LocalDateTime.now(clock) : convite.getVisualizadoEm());
        convite.setRespondidoEm(LocalDateTime.now(clock));
        return ConviteGraduacaoResponse.from(conviteRepository.save(convite));
    }

    @Transactional
    public InscricaoGraduacaoResponse inscreverAlunoAtual(Long eventoId) {
        Usuario usuario = alunoAtualUsuario();
        Long academiaId = permissaoService.academiaAtualId();
        EventoGraduacao evento = eventoRepository.findByIdAndAcademiaIdForUpdate(eventoId, academiaId)
                .orElseThrow(() -> new RuntimeException("Evento não encontrado com ID: " + eventoId));
        validarEventoInscricaoAberta(evento);
        Aluno aluno = usuario.getAluno();
        if (inscricaoRepository.findFirstByAcademiaIdAndEventoIdAndAlunoIdAndStatusInOrderByInscritoEmDesc(
                academiaId, eventoId, aluno.getId(), STATUS_INSCRICAO_ATIVA).isPresent()) {
            throw new RuntimeException("Aluno já possui inscrição ativa nesse evento");
        }
        ElegibilidadeGraduacaoResponse elegibilidade = graduacaoAlunoService.calcularElegibilidade(aluno);
        Long destinoId = elegibilidade.getProximaGraduacaoId();
        ConviteGraduacao convite = conviteRepository.findFirstByAcademiaIdAndEventoIdAndAlunoIdAndStatusInOrderByEnviadoEmDesc(
                        academiaId, eventoId, aluno.getId(), List.of(StatusConviteGraduacao.ACEITO))
                .orElse(null);
        if (convite != null) {
            destinoId = convite.getGraduacaoDestino().getId();
        }
        if (destinoId == null) {
            throw new RuntimeException("Aluno não possui próxima graduação disponível para inscrição");
        }
        EventoGraduacaoPrajied prajiedEvento = prajiedRepository.findDisponivelForUpdate(eventoId, destinoId)
                .orElseThrow(() -> new RuntimeException("Graduação de destino não disponível no evento"));
        if (convite == null && !Boolean.TRUE.equals(elegibilidade.getElegivelParaAvaliacao())) {
            throw new RuntimeException("Aluno não está elegível e não possui convite aceito");
        }
        validarCapacidade(academiaId, evento, prajiedEvento);
        InscricaoGraduacao inscricao = new InscricaoGraduacao();
        inscricao.setAcademia(evento.getAcademia());
        inscricao.setEvento(evento);
        inscricao.setAluno(aluno);
        inscricao.setGraduacaoDestino(prajiedEvento.getGraduacaoDestino());
        inscricao.setConvite(convite);
        inscricao.setValor(prajiedEvento.getValor());
        inscricao.setInscritoEm(LocalDateTime.now(clock));
        inscricao.setStatus(prajiedEvento.getValor().compareTo(BigDecimal.ZERO) == 0
                ? StatusInscricaoGraduacao.CONFIRMADA
                : StatusInscricaoGraduacao.PENDENTE_PAGAMENTO);
        inscricao.setChaveInscricaoAtiva(chaveInscricao(evento.getId(), aluno.getId()));
        return InscricaoGraduacaoResponse.from(inscricaoRepository.save(inscricao));
    }

    @Transactional
    public InscricaoGraduacaoResponse cancelarInscricaoAlunoAtual(Long inscricaoId) {
        Usuario usuario = alunoAtualUsuario();
        InscricaoGraduacao inscricao = inscricaoRepository.findByIdAndAcademiaIdAndAlunoId(
                        inscricaoId, permissaoService.academiaAtualId(), usuario.getAluno().getId())
                .orElseThrow(() -> new RuntimeException("Inscrição não encontrada"));
        if (LocalDateTime.now(clock).isAfter(inscricao.getEvento().getFimInscricoes())) {
            throw new RuntimeException("Prazo de cancelamento encerrado");
        }
        if (inscricao.getStatus() == StatusInscricaoGraduacao.PAGA || inscricao.getStatus() == StatusInscricaoGraduacao.CONFIRMADA) {
            throw new RuntimeException("Inscrição paga não pode ser cancelada sem fluxo administrativo de estorno");
        }
        inscricao.setStatus(StatusInscricaoGraduacao.CANCELADA);
        inscricao.setCanceladoEm(LocalDateTime.now(clock));
        inscricao.setChaveInscricaoAtiva(null);
        return InscricaoGraduacaoResponse.from(inscricaoRepository.save(inscricao));
    }

    private void criarConvitesAutomaticos(EventoGraduacao evento, Long academiaId) {
        List<EventoGraduacaoPrajied> itens = prajiedRepository.findByEventoIdAndAtivoTrueOrderByGraduacaoDestinoOrdemAsc(evento.getId());
        List<Long> destinos = itens.stream().map(item -> item.getGraduacaoDestino().getId()).toList();
        for (Aluno aluno : alunoRepository.findByAtivoTrueAndAcademiaId(academiaId)) {
            ElegibilidadeGraduacaoResponse elegibilidade = graduacaoAlunoService.calcularElegibilidade(aluno);
            if (Boolean.TRUE.equals(elegibilidade.getElegivelParaAvaliacao())
                    && destinos.contains(elegibilidade.getProximaGraduacaoId())) {
                Graduacao destino = buscarGraduacaoAtiva(elegibilidade.getProximaGraduacaoId());
                if (!conviteRepository.existsByAcademiaIdAndEventoIdAndAlunoIdAndGraduacaoDestinoId(
                        academiaId, evento.getId(), aluno.getId(), destino.getId())) {
                    criarConvite(evento, aluno, destino, null);
                }
            }
        }
    }

    private ConviteGraduacao criarConvite(EventoGraduacao evento, Aluno aluno, Graduacao destino, String justificativa) {
        Long academiaId = evento.getAcademia().getId();
        if (conviteRepository.existsByAcademiaIdAndEventoIdAndAlunoIdAndGraduacaoDestinoId(
                academiaId, evento.getId(), aluno.getId(), destino.getId())) {
            throw new RuntimeException("Convite já existe para esse aluno, evento e graduação");
        }
        ConviteGraduacao convite = new ConviteGraduacao();
        convite.setAcademia(evento.getAcademia());
        convite.setEvento(evento);
        convite.setAluno(aluno);
        convite.setGraduacaoAtual(aluno.getGraduacaoEntidade());
        convite.setGraduacaoDestino(destino);
        convite.setStatus(StatusConviteGraduacao.PENDENTE);
        convite.setEnviadoEm(LocalDateTime.now(clock));
        convite.setEnviadoPor(permissaoService.usuarioAtual());
        convite.setJustificativa(justificativa);
        return conviteRepository.save(convite);
    }

    private Optional<EventoGraduacaoAlunoResponse> montarEventoAluno(EventoGraduacao evento, Aluno aluno) {
        ElegibilidadeGraduacaoResponse elegibilidade = graduacaoAlunoService.calcularElegibilidade(aluno);
        Long destinoId = elegibilidade.getProximaGraduacaoId();
        ConviteGraduacao convite = conviteRepository.findFirstByAcademiaIdAndEventoIdAndAlunoIdAndStatusInOrderByEnviadoEmDesc(
                        evento.getAcademia().getId(), evento.getId(), aluno.getId(), STATUS_CONVITE_RESPONSIVO)
                .orElse(null);
        if (convite != null) {
            destinoId = convite.getGraduacaoDestino().getId();
        }
        if (destinoId == null) {
            return Optional.empty();
        }
        Optional<EventoGraduacaoPrajied> item = prajiedRepository.findByEventoIdAndGraduacaoDestinoIdAndAtivoTrue(evento.getId(), destinoId);
        if (item.isEmpty()) {
            return Optional.empty();
        }
        StatusInscricaoGraduacao statusInscricao = inscricaoRepository
                .findFirstByAcademiaIdAndEventoIdAndAlunoIdAndStatusInOrderByInscritoEmDesc(
                        evento.getAcademia().getId(), evento.getId(), aluno.getId(), STATUS_INSCRICAO_ATIVA)
                .map(InscricaoGraduacao::getStatus)
                .orElse(null);
        return Optional.of(new EventoGraduacaoAlunoResponse(evento.getId(), evento.getTitulo(), evento.getDescricao(),
                evento.getDataEvento(), evento.getHoraInicio(), evento.getHoraFim(), evento.getLocal(),
                evento.getFimInscricoes(), elegibilidade.getGraduacaoAtualNome(), elegibilidade.getProximaGraduacaoNome(),
                elegibilidade.getCheckInsAtuais(), elegibilidade.getCheckInsNecessarios(),
                elegibilidade.getElegivelParaAvaliacao(), item.get().getValor(),
                convite == null ? null : convite.getStatus(), statusInscricao));
    }

    private void validarDadosEvento(EventoGraduacaoRequest request, boolean criacao) {
        LocalDateTime inicioEvento = LocalDateTime.of(request.getDataEvento(), request.getHoraInicio());
        if (criacao && !request.getDataEvento().isAfter(LocalDateTime.now(clock).toLocalDate())) {
            throw new RuntimeException("Data do evento deve ser futura");
        }
        if (!request.getHoraFim().isAfter(request.getHoraInicio())) {
            throw new RuntimeException("Horário final deve ser posterior ao inicial");
        }
        if (request.getFimInscricoes().isBefore(request.getInicioInscricoes())) {
            throw new RuntimeException("Fim das inscrições deve ser posterior ao início");
        }
        if (request.getFimInscricoes().isAfter(inicioEvento)) {
            throw new RuntimeException("Fim das inscrições deve ocorrer até o início do evento");
        }
    }

    private void validarEventoInscricaoAberta(EventoGraduacao evento) {
        LocalDateTime agora = LocalDateTime.now(clock);
        if (evento.getStatus() != StatusEventoGraduacao.PUBLICADO) {
            throw new RuntimeException("Evento não publicado");
        }
        if (agora.isBefore(evento.getInicioInscricoes()) || agora.isAfter(evento.getFimInscricoes())) {
            throw new RuntimeException("Inscrições encerradas ou ainda não abertas");
        }
    }

    private void validarCapacidade(Long academiaId, EventoGraduacao evento, EventoGraduacaoPrajied prajiedEvento) {
        if (evento.getLimiteParticipantes() != null) {
            long total = inscricaoRepository.countByAcademiaIdAndEventoIdAndStatusIn(
                    academiaId, evento.getId(), STATUS_INSCRICAO_ATIVA);
            if (total >= evento.getLimiteParticipantes()) {
                throw new RuntimeException("Limite geral de participantes atingido");
            }
        }
        if (prajiedEvento.getLimiteVagas() != null) {
            long totalPrajied = inscricaoRepository.countByAcademiaIdAndEventoIdAndGraduacaoDestinoIdAndStatusIn(
                    academiaId, evento.getId(), prajiedEvento.getGraduacaoDestino().getId(), STATUS_INSCRICAO_ATIVA);
            if (totalPrajied >= prajiedEvento.getLimiteVagas()) {
                throw new RuntimeException("Limite de vagas do Prajied atingido");
            }
        }
    }

    private void preencherEvento(EventoGraduacao evento, EventoGraduacaoRequest request) {
        evento.setTitulo(request.getTitulo());
        evento.setDescricao(request.getDescricao());
        evento.setDataEvento(request.getDataEvento());
        evento.setHoraInicio(request.getHoraInicio());
        evento.setHoraFim(request.getHoraFim());
        evento.setLocal(request.getLocal());
        evento.setInicioInscricoes(request.getInicioInscricoes());
        evento.setFimInscricoes(request.getFimInscricoes());
        evento.setLimiteParticipantes(request.getLimiteParticipantes());
    }

    private EventoGraduacao buscarEventoDaAcademia(Long eventoId) {
        return eventoRepository.findByIdAndAcademiaId(eventoId, permissaoService.academiaAtualId())
                .orElseThrow(() -> new RuntimeException("Evento de graduação não encontrado com ID: " + eventoId));
    }

    private Aluno buscarAlunoDaAcademia(Long alunoId, Long academiaId) {
        return alunoRepository.findByIdAndAcademiaId(alunoId, academiaId)
                .orElseThrow(() -> new RuntimeException("Aluno não encontrado com ID: " + alunoId));
    }

    private Graduacao buscarGraduacaoAtiva(Long graduacaoId) {
        return graduacaoRepository.findByIdAndAcademiaId(graduacaoId, permissaoService.academiaAtualId())
                .filter(g -> Boolean.TRUE.equals(g.getAtiva()))
                .orElseThrow(() -> new RuntimeException("Graduação ativa não encontrada com ID: " + graduacaoId));
    }

    private EventoGraduacaoPrajied garantirPrajiedDisponivel(Long eventoId, Long graduacaoDestinoId) {
        return prajiedRepository.findByEventoIdAndGraduacaoDestinoIdAndAtivoTrue(eventoId, graduacaoDestinoId)
                .orElseThrow(() -> new RuntimeException("Graduação de destino não está disponível no evento"));
    }

    private BigDecimal valorEventoOuPadrao(BigDecimal valor, Graduacao graduacao) {
        BigDecimal definido = valor == null ? graduacao.getValorPadraoGraduacao() : valor;
        if (definido == null || definido.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Valor do Prajied no evento não pode ser negativo");
        }
        return definido;
    }

    private Usuario alunoAtualUsuario() {
        Usuario usuario = permissaoService.usuarioAtual();
        if (usuario.getPerfil() != PerfilUsuario.ALUNO || usuario.getAluno() == null) {
            throw new AccessDeniedException("Acesso permitido apenas para o aluno autenticado");
        }
        return usuario;
    }

    private ConviteGraduacao buscarConviteDoAluno(Long conviteId) {
        Usuario usuario = alunoAtualUsuario();
        return conviteRepository.findByIdAndAcademiaIdAndAlunoId(
                        conviteId, permissaoService.academiaAtualId(), usuario.getAluno().getId())
                .orElseThrow(() -> new RuntimeException("Convite não encontrado"));
    }

    private String chaveInscricao(Long eventoId, Long alunoId) {
        return eventoId + ":" + alunoId;
    }
}
