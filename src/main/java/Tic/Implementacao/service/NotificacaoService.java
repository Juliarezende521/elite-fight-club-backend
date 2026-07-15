package Tic.Implementacao.service;

import Tic.Implementacao.dto.notificacao.NotificacaoRelatorioResponse;
import Tic.Implementacao.dto.notificacao.NotificacaoResponse;
import Tic.Implementacao.model.*;
import Tic.Implementacao.repository.*;
import Tic.Implementacao.security.PermissaoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class NotificacaoService {

    private static final String REF_EVENTO_GRADUACAO = "EVENTO_GRADUACAO";
    private static final String REF_INSCRICAO_GRADUACAO = "INSCRICAO_GRADUACAO";
    private static final String REF_AVALIACAO_GRADUACAO = "AVALIACAO_GRADUACAO";

    private final NotificacaoRepository repo;
    private final FinanceiroRepository financeiroRepository;
    private final UsuarioRepository usuarioRepository;
    private final ConviteGraduacaoRepository conviteGraduacaoRepository;
    private final InscricaoGraduacaoRepository inscricaoGraduacaoRepository;
    private final EventoGraduacaoRepository eventoGraduacaoRepository;
    private final PermissaoService permissaoService;
    private final Clock clock;
    private final long prazoInscricaoHoras;
    private final long lembreteEventoHoras;
    private final boolean notificacoesLegadoEnabled;

    public NotificacaoService(NotificacaoRepository repo,
                              FinanceiroRepository financeiroRepository,
                              UsuarioRepository usuarioRepository,
                              ConviteGraduacaoRepository conviteGraduacaoRepository,
                              InscricaoGraduacaoRepository inscricaoGraduacaoRepository,
                              EventoGraduacaoRepository eventoGraduacaoRepository,
                              PermissaoService permissaoService,
                              Clock clock,
                              @Value("${graduacao.notificacao.prazo-inscricao-horas:48}") long prazoInscricaoHoras,
                              @Value("${graduacao.notificacao.lembrete-evento-horas:24}") long lembreteEventoHoras,
                              @Value("${notificacoes.legado.enabled:false}") boolean notificacoesLegadoEnabled) {
        this.repo = repo;
        this.financeiroRepository = financeiroRepository;
        this.usuarioRepository = usuarioRepository;
        this.conviteGraduacaoRepository = conviteGraduacaoRepository;
        this.inscricaoGraduacaoRepository = inscricaoGraduacaoRepository;
        this.eventoGraduacaoRepository = eventoGraduacaoRepository;
        this.permissaoService = permissaoService;
        this.clock = clock;
        this.prazoInscricaoHoras = prazoInscricaoHoras;
        this.lembreteEventoHoras = lembreteEventoHoras;
        this.notificacoesLegadoEnabled = notificacoesLegadoEnabled;
    }

    public List<Notificacao> listarTodos(){
        return repo.findAll();
    }

    public Optional<Notificacao> buscarPorId(Long id){
        return repo.findById(id);
    }

    public List<Notificacao> listarPorAluno(Long alunoId) {
        return repo.findByAlunoId(alunoId);
    }

    public List<Notificacao> listarPendentes() {
        return repo.findByEnviadaFalse();
    }

    @Transactional(readOnly = true)
    public List<NotificacaoResponse> listarDoUsuarioAtual(boolean somenteNaoLidas) {
        Usuario usuario = exigirAlunoAtual();
        Long academiaId = permissaoService.academiaAtualId();
        List<Notificacao> notificacoes = somenteNaoLidas
                ? repo.findByDestinatarioIdAndAcademiaIdAndLidaFalseOrderByCriadaEmDesc(usuario.getId(), academiaId)
                : repo.findByDestinatarioIdAndAcademiaIdOrderByCriadaEmDesc(usuario.getId(), academiaId);
        return notificacoes.stream().map(NotificacaoResponse::from).toList();
    }

    @Transactional
    public NotificacaoResponse marcarComoLida(Long notificacaoId) {
        Usuario usuario = exigirAlunoAtual();
        Notificacao notificacao = repo.findByIdAndDestinatarioIdAndAcademiaId(
                        notificacaoId, usuario.getId(), permissaoService.academiaAtualId())
                .orElseThrow(() -> new AccessDeniedException("Notificação não encontrada para o aluno atual"));
        if (!Boolean.TRUE.equals(notificacao.getLida())) {
            notificacao.setLida(true);
            notificacao.setLidaEm(LocalDateTime.now(clock));
        }
        return NotificacaoResponse.from(repo.save(notificacao));
    }

    @Transactional
    public void marcarTodasComoLidas() {
        Usuario usuario = exigirAlunoAtual();
        LocalDateTime agora = LocalDateTime.now(clock);
        repo.findByDestinatarioIdAndAcademiaIdAndLidaFalse(usuario.getId(), permissaoService.academiaAtualId())
                .forEach(notificacao -> {
                    notificacao.setLida(true);
                    notificacao.setLidaEm(agora);
                    repo.save(notificacao);
                });
    }

    @Transactional(readOnly = true)
    public List<NotificacaoRelatorioResponse> relatorio(LocalDateTime inicio, LocalDateTime fim,
                                                        TipoNotificacao tipo, StatusNotificacao status,
                                                        Long alunoId, Long eventoId) {
        permissaoService.exigirAdminOuProfessor();
        return repo.buscarRelatorio(permissaoService.academiaAtualId(), inicio, fim, tipo, status, alunoId, eventoId)
                .stream()
                .map(NotificacaoRelatorioResponse::from)
                .toList();
    }

    @Transactional
    public boolean remove(Long id){
        if(repo.existsById(id)){
            repo.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional
    public Notificacao salva(Notificacao notificacao){
        LocalDateTime agora = LocalDateTime.now(clock);
        if (notificacao.getDataEnvio() == null) {
            notificacao.setDataEnvio(agora);
        }
        if (notificacao.getCriadaEm() == null) {
            notificacao.setCriadaEm(notificacao.getDataEnvio());
        }
        if (notificacao.getLida() == null) {
            notificacao.setLida(false);
        }
        if (notificacao.getStatus() == null) {
            notificacao.setStatus(notificacao.isEnviada() ? StatusNotificacao.ENVIADA : StatusNotificacao.PENDENTE);
        }
        return repo.save(notificacao);
    }

    @Transactional
    public Notificacao atualizarInformacoes(Long id, Notificacao alterado) {
        Notificacao notificacao = repo.findById(id).orElse(null);
        if (notificacao == null) return null;

        notificacao.setTipoNotificacao(alterado.getTipoNotificacao());
        notificacao.setTitulo(alterado.getTitulo());
        notificacao.setMensagem(alterado.getMensagem());
        notificacao.setDataEnvio(alterado.getDataEnvio());
        notificacao.setEnviada(alterado.isEnviada());
        notificacao.setAluno(alterado.getAluno());
        notificacao.setFinanceiro(alterado.getFinanceiro());
        notificacao.setStatus(alterado.getStatus());
        notificacao.setAgendadaPara(alterado.getAgendadaPara());

        return repo.save(notificacao);
    }

    @Transactional
    public Notificacao marcarComoEnviada(Long id) {
        Notificacao notificacao = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Notificação não encontrada com ID: " + id));

        notificacao.enviar();
        return repo.save(notificacao);
    }

    @Scheduled(cron = "0 0 8 * * *")
    @Transactional
    public void gerarNotificacoesAutomaticas() {
        if (!notificacoesLegadoEnabled) {
            return;
        }
        LocalDate hoje = LocalDate.now(clock);

        financeiroRepository.findByDataPagamentoIsNullAndDataVencimento(hoje)
                .forEach(financeiro -> criarNotificacaoSeNaoExistir(financeiro, TipoNotificacao.VENCIMENTO_HOJE,
                        "Mensalidade vence hoje."));

        financeiroRepository.findByDataPagamentoIsNullAndDataVencimento(hoje.minusDays(2))
                .forEach(financeiro -> criarNotificacaoSeNaoExistir(financeiro, TipoNotificacao.VENCIDO_2_DIAS,
                        "Mensalidade vencida há 2 dias."));

        financeiroRepository.findByDataPagamentoIsNullAndDataVencimentoBefore(hoje.minusDays(2))
                .forEach(financeiro -> criarNotificacaoSeNaoExistir(financeiro, TipoNotificacao.PAGAMENTO_ATRASADO,
                        "Mensalidade está atrasada."));
    }

    @Transactional
    public Notificacao criarNotificacaoSeNaoExistir(Financeiro financeiro, TipoNotificacao tipo, String mensagem) {
        if (financeiro.getId() != null && repo.existsByFinanceiroIdAndTipoNotificacao(financeiro.getId(), tipo)) {
            return null;
        }

        Notificacao notificacao = new Notificacao(tipo, mensagem);
        notificacao.setTitulo("Aviso financeiro");
        notificacao.setAluno(financeiro.getAluno());
        notificacao.setFinanceiro(financeiro);
        notificacao.setEnviada(false);
        notificacao.setStatus(StatusNotificacao.PENDENTE);
        if (financeiro.getAluno() != null) {
            notificacao.setAcademia(financeiro.getAluno().getAcademia());
            usuarioPorAluno(financeiro.getAluno()).ifPresent(notificacao::setDestinatario);
        }

        return salva(notificacao);
    }

    @Transactional
    public Notificacao notificarConviteGraduacao(ConviteGraduacao convite) {
        String mensagem = String.format(
                "Você recebeu um convite para o evento %s em %s, das %s às %s, no local %s. Próxima graduação: %s.",
                convite.getEvento().getTitulo(),
                convite.getEvento().getDataEvento(),
                convite.getEvento().getHoraInicio(),
                convite.getEvento().getHoraFim(),
                convite.getEvento().getLocal(),
                convite.getGraduacaoDestino().getNome()
        );
        return criarParaAluno(convite.getAcademia(), convite.getAluno(), TipoNotificacao.CONVITE_GRADUACAO_RECEBIDO,
                "Convite de graduação recebido", mensagem, REF_EVENTO_GRADUACAO, convite.getEvento().getId());
    }

    @Transactional
    public Notificacao notificarInscricaoGraduacao(InscricaoGraduacao inscricao) {
        if (inscricao.getStatus() == StatusInscricaoGraduacao.CONFIRMADA) {
            return criarParaAluno(inscricao.getAcademia(), inscricao.getAluno(), TipoNotificacao.INSCRICAO_GRADUACAO_CONFIRMADA,
                    "Inscrição confirmada",
                    "Sua inscrição no evento " + inscricao.getEvento().getTitulo() + " foi confirmada.",
                    REF_INSCRICAO_GRADUACAO, inscricao.getId());
        }
        if (inscricao.getStatus() == StatusInscricaoGraduacao.PENDENTE_PAGAMENTO) {
            return criarParaAluno(inscricao.getAcademia(), inscricao.getAluno(), TipoNotificacao.PAGAMENTO_GRADUACAO_PENDENTE,
                    "Pagamento pendente",
                    "Sua inscrição no evento " + inscricao.getEvento().getTitulo() + " está aguardando pagamento.",
                    REF_INSCRICAO_GRADUACAO, inscricao.getId());
        }
        return null;
    }

    @Transactional
    public Notificacao notificarPagamentoGraduacao(Pagamento pagamento, StatusPagamento statusAtual) {
        InscricaoGraduacao inscricao = pagamento.getInscricaoGraduacao();
        if (inscricao == null) {
            return null;
        }
        if (statusAtual == StatusPagamento.PAGO) {
            return criarParaAluno(inscricao.getAcademia(), inscricao.getAluno(), TipoNotificacao.PAGAMENTO_GRADUACAO_CONFIRMADO,
                    "Pagamento confirmado",
                    "O pagamento da sua inscrição no evento " + inscricao.getEvento().getTitulo() + " foi confirmado.",
                    REF_INSCRICAO_GRADUACAO, inscricao.getId());
        }
        if (statusAtual == StatusPagamento.FALHOU || statusAtual == StatusPagamento.CANCELADO) {
            return criarParaAluno(inscricao.getAcademia(), inscricao.getAluno(), TipoNotificacao.PAGAMENTO_GRADUACAO_FALHOU,
                    "Pagamento não confirmado",
                    "O pagamento da sua inscrição no evento " + inscricao.getEvento().getTitulo() + " não foi confirmado. Verifique sua inscrição.",
                    REF_INSCRICAO_GRADUACAO, inscricao.getId());
        }
        if (statusAtual == StatusPagamento.ESTORNADO) {
            return criarParaAluno(inscricao.getAcademia(), inscricao.getAluno(), TipoNotificacao.PAGAMENTO_GRADUACAO_PENDENTE,
                    "Inscrição voltou a pendente",
                    "O pagamento da sua inscrição no evento " + inscricao.getEvento().getTitulo() + " foi estornado e voltou a ficar pendente.",
                    REF_INSCRICAO_GRADUACAO, inscricao.getId());
        }
        return null;
    }

    @Transactional
    public void notificarEventoCancelado(EventoGraduacao evento) {
        Set<Long> usuariosNotificados = new HashSet<>();
        List<StatusConviteGraduacao> statusConvite = List.of(
                StatusConviteGraduacao.PENDENTE,
                StatusConviteGraduacao.VISUALIZADO,
                StatusConviteGraduacao.ACEITO,
                StatusConviteGraduacao.CANCELADO
        );
        conviteGraduacaoRepository.findByAcademiaIdAndEventoIdAndStatusIn(
                        evento.getAcademia().getId(), evento.getId(), statusConvite)
                .forEach(convite -> notificarCancelamentoUmaVez(evento, convite.getAluno(), usuariosNotificados));

        List<StatusInscricaoGraduacao> statusInscricao = List.of(
                StatusInscricaoGraduacao.PENDENTE_PAGAMENTO,
                StatusInscricaoGraduacao.PAGAMENTO_PROCESSANDO,
                StatusInscricaoGraduacao.CONFIRMADA,
                StatusInscricaoGraduacao.PAGA,
                StatusInscricaoGraduacao.EM_AVALIACAO,
                StatusInscricaoGraduacao.APROVADA,
                StatusInscricaoGraduacao.REPROVADA
        );
        inscricaoGraduacaoRepository.findByAcademiaIdAndEventoIdOrderByInscritoEmDesc(evento.getAcademia().getId(), evento.getId())
                .stream()
                .filter(inscricao -> statusInscricao.contains(inscricao.getStatus()))
                .forEach(inscricao -> notificarCancelamentoUmaVez(evento, inscricao.getAluno(), usuariosNotificados));
        cancelarNotificacoesFuturas(evento);
    }

    @Transactional
    public Notificacao notificarResultadoAvaliacao(AvaliacaoGraduacao avaliacao) {
        return criarParaAluno(avaliacao.getAcademia(), avaliacao.getAluno(), TipoNotificacao.RESULTADO_AVALIACAO_DISPONIVEL,
                "Resultado de avaliação disponível",
                "O resultado da sua avaliação no evento " + avaliacao.getEvento().getTitulo() + " está disponível.",
                REF_AVALIACAO_GRADUACAO, avaliacao.getId());
    }

    @Transactional
    public Notificacao notificarPromocaoConfirmada(AvaliacaoGraduacao avaliacao, Graduacao anterior, Graduacao destino) {
        return criarParaAluno(avaliacao.getAcademia(), avaliacao.getAluno(), TipoNotificacao.PROMOCAO_GRADUACAO_CONFIRMADA,
                "Promoção confirmada",
                "Sua graduação foi atualizada de " + anterior.getNome() + " para " + destino.getNome() + ".",
                REF_AVALIACAO_GRADUACAO, avaliacao.getId());
    }

    @Transactional
    public int gerarAvisosPrazoInscricaoProximo() {
        permissaoService.exigirAdminOuProfessor();
        LocalDateTime agora = LocalDateTime.now(clock);
        LocalDateTime limite = agora.plusHours(prazoInscricaoHoras);
        return eventoGraduacaoRepository
                .findByAcademiaIdAndStatusOrderByDataEventoAsc(permissaoService.academiaAtualId(), StatusEventoGraduacao.PUBLICADO)
                .stream()
                .filter(evento -> !evento.getFimInscricoes().isBefore(agora) && !evento.getFimInscricoes().isAfter(limite))
                .mapToInt(this::notificarPrazoInscricaoProximo)
                .sum();
    }

    @Transactional
    public int gerarLembretesEventoGraduacao() {
        permissaoService.exigirAdminOuProfessor();
        LocalDateTime agora = LocalDateTime.now(clock);
        LocalDateTime limite = agora.plusHours(lembreteEventoHoras);
        return eventoGraduacaoRepository
                .findByAcademiaIdAndStatusOrderByDataEventoAsc(permissaoService.academiaAtualId(), StatusEventoGraduacao.PUBLICADO)
                .stream()
                .filter(evento -> {
                    LocalDateTime inicioEvento = LocalDateTime.of(evento.getDataEvento(), evento.getHoraInicio());
                    return !inicioEvento.isBefore(agora) && !inicioEvento.isAfter(limite);
                })
                .mapToInt(this::notificarLembreteEvento)
                .sum();
    }

    private int notificarPrazoInscricaoProximo(EventoGraduacao evento) {
        return notificarAlunosEvento(evento, TipoNotificacao.PRAZO_INSCRICAO_PROXIMO,
                "Prazo de inscrição próximo",
                "O prazo de inscrição para o evento " + evento.getTitulo() + " termina em " + evento.getFimInscricoes() + ".");
    }

    private int notificarLembreteEvento(EventoGraduacao evento) {
        return notificarAlunosEvento(evento, TipoNotificacao.LEMBRETE_EVENTO_GRADUACAO,
                "Lembrete de evento",
                "O evento " + evento.getTitulo() + " acontece em " + evento.getDataEvento() + " às " + evento.getHoraInicio() + ".");
    }

    private int notificarAlunosEvento(EventoGraduacao evento, TipoNotificacao tipo, String titulo, String mensagem) {
        Set<Long> usuariosNotificados = new HashSet<>();
        conviteGraduacaoRepository.findByAcademiaIdAndEventoIdOrderByEnviadoEmDesc(evento.getAcademia().getId(), evento.getId())
                .forEach(convite -> criarParaAlunoSeNovo(evento, convite.getAluno(), tipo, titulo, mensagem, usuariosNotificados));
        inscricaoGraduacaoRepository.findByAcademiaIdAndEventoIdOrderByInscritoEmDesc(evento.getAcademia().getId(), evento.getId())
                .forEach(inscricao -> criarParaAlunoSeNovo(evento, inscricao.getAluno(), tipo, titulo, mensagem, usuariosNotificados));
        return usuariosNotificados.size();
    }

    private void criarParaAlunoSeNovo(EventoGraduacao evento, Aluno aluno, TipoNotificacao tipo, String titulo,
                                      String mensagem, Set<Long> usuariosNotificados) {
        Optional<Usuario> usuario = usuarioPorAluno(aluno);
        if (usuario.isEmpty() || !usuariosNotificados.add(usuario.get().getId())) {
            return;
        }
        criarSeNaoExistir(evento.getAcademia(), usuario.get(), tipo, titulo, mensagem, REF_EVENTO_GRADUACAO, evento.getId(), null);
    }

    private void notificarCancelamentoUmaVez(EventoGraduacao evento, Aluno aluno, Set<Long> usuariosNotificados) {
        Optional<Usuario> usuario = usuarioPorAluno(aluno);
        if (usuario.isEmpty() || !usuariosNotificados.add(usuario.get().getId())) {
            return;
        }
        criarSeNaoExistir(evento.getAcademia(), usuario.get(), TipoNotificacao.EVENTO_GRADUACAO_CANCELADO,
                "Evento de graduação cancelado",
                "O evento " + evento.getTitulo() + " foi cancelado.",
                REF_EVENTO_GRADUACAO, evento.getId(), null);
    }

    private void cancelarNotificacoesFuturas(EventoGraduacao evento) {
        repo.buscarRelatorio(evento.getAcademia().getId(), null, null, null, StatusNotificacao.PENDENTE, null, evento.getId())
                .forEach(notificacao -> {
                    notificacao.setStatus(StatusNotificacao.CANCELADA);
                    repo.save(notificacao);
                });
    }

    private Notificacao criarParaAluno(Academia academia, Aluno aluno, TipoNotificacao tipo, String titulo,
                                       String mensagem, String referenciaTipo, Long referenciaId) {
        return usuarioPorAluno(aluno)
                .map(usuario -> criarSeNaoExistir(academia, usuario, tipo, titulo, mensagem, referenciaTipo, referenciaId, null))
                .orElse(null);
    }

    @Transactional
    public Notificacao criarSeNaoExistir(Academia academia, Usuario destinatario, TipoNotificacao tipo, String titulo,
                                         String mensagem, String referenciaTipo, Long referenciaId,
                                         LocalDateTime agendadaPara) {
        if (academia == null || destinatario == null || tipo == null || referenciaTipo == null || referenciaId == null) {
            return null;
        }
        String chave = chaveIdempotencia(tipo, destinatario.getId(), referenciaTipo, referenciaId);
        Optional<Notificacao> existente = repo.findByChaveIdempotencia(chave);
        if (existente.isPresent()) {
            return existente.get();
        }

        LocalDateTime agora = LocalDateTime.now(clock);
        Notificacao notificacao = new Notificacao(tipo, mensagem);
        notificacao.setAcademia(academia);
        notificacao.setDestinatario(destinatario);
        notificacao.setAluno(destinatario.getAluno());
        notificacao.setTitulo(titulo);
        notificacao.setCriadaEm(agendadaPara == null ? agora : agora);
        notificacao.setDataEnvio(agendadaPara == null ? agora : agendadaPara);
        notificacao.setEnviada(agendadaPara == null);
        notificacao.setStatus(agendadaPara == null ? StatusNotificacao.ENVIADA : StatusNotificacao.PENDENTE);
        notificacao.setLida(false);
        notificacao.setReferenciaTipo(referenciaTipo);
        notificacao.setReferenciaId(referenciaId);
        notificacao.setChaveIdempotencia(chave);
        notificacao.setAgendadaPara(agendadaPara);
        return repo.save(notificacao);
    }

    private Optional<Usuario> usuarioPorAluno(Aluno aluno) {
        if (aluno == null || aluno.getId() == null || aluno.getAcademia() == null || aluno.getAcademia().getId() == null) {
            return Optional.empty();
        }
        return usuarioRepository.findByAlunoIdAndAcademiaId(aluno.getId(), aluno.getAcademia().getId());
    }

    private Usuario exigirAlunoAtual() {
        Usuario usuario = permissaoService.usuarioAtual();
        if (usuario.getPerfil() != PerfilUsuario.ALUNO || usuario.getAluno() == null) {
            throw new AccessDeniedException("Acesso permitido apenas para o aluno autenticado");
        }
        return usuario;
    }

    private String chaveIdempotencia(TipoNotificacao tipo, Long destinatarioId, String referenciaTipo, Long referenciaId) {
        return tipo.name() + ":" + destinatarioId + ":" + referenciaTipo + ":" + referenciaId;
    }
}
