package Tic.Implementacao.service;

import Tic.Implementacao.dto.graduacao.*;
import Tic.Implementacao.model.*;
import Tic.Implementacao.repository.*;
import Tic.Implementacao.security.PermissaoService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class GraduacaoAlunoService {

    private final AlunoRepository alunoRepository;
    private final GraduacaoRepository graduacaoRepository;
    private final HistoricoGraduacaoRepository historicoRepository;
    private final CheckInRepository checkInRepository;
    private final ProfessorRepository professorRepository;
    private final GraduacaoService graduacaoService;
    private final PermissaoService permissaoService;
    private final Clock clock;

    public GraduacaoAlunoService(AlunoRepository alunoRepository,
                                 GraduacaoRepository graduacaoRepository,
                                 HistoricoGraduacaoRepository historicoRepository,
                                 CheckInRepository checkInRepository,
                                 ProfessorRepository professorRepository,
                                 GraduacaoService graduacaoService,
                                 PermissaoService permissaoService,
                                 Clock clock) {
        this.alunoRepository = alunoRepository;
        this.graduacaoRepository = graduacaoRepository;
        this.historicoRepository = historicoRepository;
        this.checkInRepository = checkInRepository;
        this.professorRepository = professorRepository;
        this.graduacaoService = graduacaoService;
        this.permissaoService = permissaoService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public ElegibilidadeGraduacaoResponse calcularElegibilidade(Long alunoId) {
        Long academiaId = permissaoService.academiaAtualId();
        Aluno aluno = buscarAlunoDaAcademia(alunoId, academiaId);
        return calcularElegibilidade(aluno);
    }

    @Transactional(readOnly = true)
    public ElegibilidadeGraduacaoResponse calcularElegibilidade(Aluno aluno) {
        Long academiaId = permissaoService.academiaAtualId();
        validarAlunoDaAcademia(aluno, academiaId);

        Graduacao atual = aluno.getGraduacaoEntidade();
        if (!Boolean.TRUE.equals(aluno.getAtivo())) {
            return respostaSemProxima(aluno, atual, "Aluno inativo");
        }
        if (atual == null || !Boolean.TRUE.equals(atual.getAtiva())) {
            return respostaSemProxima(aluno, atual, "Graduação atual inexistente ou inativa");
        }

        Optional<Graduacao> proximaOpt = graduacaoService.buscarProximaGraduacao(academiaId, atual);
        if (proximaOpt.isEmpty()) {
            return respostaSemProxima(aluno, atual, "Graduação mais alta");
        }

        Graduacao proxima = proximaOpt.get();
        LocalDate entrada = dataEntradaGraduacaoAtual(aluno, atual);
        int checkInsAtuais = Math.toIntExact(checkInRepository.countByAcademiaIdAndAlunoIdAndAtivoTrueAndDataHoraBetween(
                academiaId, aluno.getId(), entrada.atStartOfDay(), LocalDateTime.now(clock).plusNanos(1)));
        int checkInsNecessarios = Math.max(0, proxima.getCheckinsNecessarios());
        int tempoMinimo = Math.max(0, proxima.getTempoMinimoMeses());
        LocalDate dataMinima = entrada.plusMonths(tempoMinimo);
        boolean requisitoCheckIns = checkInsAtuais >= checkInsNecessarios;
        boolean requisitoTempo = !LocalDate.now(clock).isBefore(dataMinima);
        boolean elegivel = requisitoCheckIns && requisitoTempo;

        return new ElegibilidadeGraduacaoResponse(aluno.getId(), atual.getId(), atual.getNome(), proxima.getId(),
                proxima.getNome(), checkInsAtuais, checkInsNecessarios,
                Math.max(0, checkInsNecessarios - checkInsAtuais), tempoMinimo, entrada, dataMinima,
                requisitoCheckIns, requisitoTempo, elegivel,
                elegivel ? "Elegível para avaliação" : "Requisitos pendentes");
    }

    @Transactional
    public HistoricoGraduacaoResponse alterarGraduacao(Long alunoId, AlterarGraduacaoAlunoRequest request) {
        permissaoService.exigirAdminOuProfessor();
        Long academiaId = permissaoService.academiaAtualId();
        Usuario usuario = permissaoService.usuarioAtual();
        Aluno aluno = buscarAlunoDaAcademia(alunoId, academiaId);
        Graduacao novaGraduacao = buscarGraduacaoAtivaDaAcademia(request.getNovaGraduacaoId(), academiaId);
        HistoricoGraduacao historico = alterarGraduacaoValidada(aluno, novaGraduacao, request.getMotivo(),
                request.getObservacao(), false, usuario);
        return HistoricoGraduacaoResponse.from(historico);
    }

    @Transactional
    public HistoricoGraduacaoResponse promoverParaProximaGraduacao(Long alunoId, PromoverGraduacaoAlunoRequest request) {
        permissaoService.exigirAdminOuProfessor();
        Long academiaId = permissaoService.academiaAtualId();
        Usuario usuario = permissaoService.usuarioAtual();
        Aluno aluno = buscarAlunoDaAcademia(alunoId, academiaId);
        ElegibilidadeGraduacaoResponse elegibilidade = calcularElegibilidade(aluno);
        if (!Boolean.TRUE.equals(elegibilidade.getElegivelParaAvaliacao())) {
            throw new RuntimeException("Aluno não está elegível para promoção: " + elegibilidade.getMotivo());
        }
        Graduacao proxima = buscarGraduacaoAtivaDaAcademia(elegibilidade.getProximaGraduacaoId(), academiaId);
        HistoricoGraduacao historico = alterarGraduacaoValidada(aluno, proxima,
                MotivoAlteracaoGraduacao.APROVACAO_AVALIACAO, request.getObservacao(), true, usuario);
        return HistoricoGraduacaoResponse.from(historico);
    }

    @Transactional(readOnly = true)
    public List<AlunoElegivelGraduacaoResponse> listarElegiveis(Long graduacaoAtualId, Long proximaGraduacaoId,
                                                                Long professorId, boolean somenteElegiveis) {
        Long academiaId = permissaoService.academiaAtualId();
        if (professorId != null) {
            professorRepository.findByIdAndAcademiaId(professorId, academiaId)
                    .orElseThrow(() -> new RuntimeException("Professor não encontrado com ID: " + professorId));
        }
        return alunoRepository.findByAtivoTrueAndAcademiaId(academiaId)
                .stream()
                .map(this::calcularElegibilidade)
                .filter(e -> graduacaoAtualId == null || graduacaoAtualId.equals(e.getGraduacaoAtualId()))
                .filter(e -> proximaGraduacaoId == null || proximaGraduacaoId.equals(e.getProximaGraduacaoId()))
                .filter(e -> !somenteElegiveis || Boolean.TRUE.equals(e.getElegivelParaAvaliacao()))
                .map(e -> new AlunoElegivelGraduacaoResponse(e.getAlunoId(), nomeAluno(e.getAlunoId(), academiaId),
                        e.getGraduacaoAtualNome(), e.getProximaGraduacaoNome(), e.getCheckInsAtuais(),
                        e.getCheckInsNecessarios(), e.getCheckInsFaltantes(), e.getTempoMinimoMeses(),
                        e.getRequisitoCheckInsAtingido(), e.getRequisitoTempoAtingido(), e.getElegivelParaAvaliacao()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<HistoricoGraduacaoResponse> listarHistoricoAluno(Long alunoId) {
        Usuario usuario = permissaoService.usuarioAtual();
        Long academiaId = permissaoService.academiaAtualId();
        if (usuario.getPerfil() == PerfilUsuario.ALUNO) {
            if (usuario.getAluno() == null || !usuario.getAluno().getId().equals(alunoId)) {
                throw new AccessDeniedException("Aluno pode consultar apenas o próprio histórico");
            }
        } else {
            permissaoService.exigirAdminOuProfessor();
        }
        buscarAlunoDaAcademia(alunoId, academiaId);
        return historicoRepository.findByAcademiaIdAndAlunoIdOrderByAlteradoEmDesc(academiaId, alunoId)
                .stream()
                .map(HistoricoGraduacaoResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<HistoricoGraduacaoResponse> listarHistoricoAlunoAtual() {
        Usuario usuario = permissaoService.usuarioAtual();
        if (usuario.getPerfil() != PerfilUsuario.ALUNO || usuario.getAluno() == null) {
            throw new AccessDeniedException("Histórico próprio disponível apenas para aluno autenticado");
        }
        return listarHistoricoAluno(usuario.getAluno().getId());
    }

    @Transactional
    public void registrarHistoricoInicial(Aluno aluno, Usuario responsavel) {
        if (aluno.getGraduacaoEntidade() == null || aluno.getAcademia() == null || responsavel == null) {
            return;
        }
        if (historicoRepository.existsByAcademiaIdAndAlunoId(aluno.getAcademia().getId(), aluno.getId())) {
            return;
        }
        HistoricoGraduacao historico = novoHistorico(aluno, null, aluno.getGraduacaoEntidade(),
                MotivoAlteracaoGraduacao.CADASTRO_INICIAL, "Graduação inicial do aluno.", false, responsavel);
        historicoRepository.save(historico);
    }

    private HistoricoGraduacao alterarGraduacaoValidada(Aluno aluno, Graduacao novaGraduacao,
                                                        MotivoAlteracaoGraduacao motivo, String observacao,
                                                        boolean promocaoPorAvaliacao, Usuario usuario) {
        Graduacao atual = aluno.getGraduacaoEntidade();
        if (atual == null) {
            throw new RuntimeException("Aluno não possui graduação atual");
        }
        if (atual.getId().equals(novaGraduacao.getId())) {
            throw new RuntimeException("Aluno já está nessa graduação");
        }
        if (motivo == null) {
            throw new RuntimeException("Motivo da alteração é obrigatório");
        }
        boolean rebaixamento = novaGraduacao.getOrdem() < atual.getOrdem();
        if ((rebaixamento || motivo == MotivoAlteracaoGraduacao.REBAIXAMENTO)
                && (observacao == null || observacao.isBlank())) {
            throw new RuntimeException("Rebaixamento exige observação");
        }

        HistoricoGraduacao historico = novoHistorico(aluno, atual, novaGraduacao, motivo, observacao,
                promocaoPorAvaliacao, usuario);
        aluno.setGraduacaoEntidade(novaGraduacao);
        aluno.setGraduacao(novaGraduacao.getNome());
        historicoRepository.save(historico);
        alunoRepository.save(aluno);
        return historico;
    }

    private HistoricoGraduacao novoHistorico(Aluno aluno, Graduacao anterior, Graduacao nova,
                                             MotivoAlteracaoGraduacao motivo, String observacao,
                                             boolean promocaoPorAvaliacao, Usuario usuario) {
        HistoricoGraduacao historico = new HistoricoGraduacao();
        historico.setAcademia(aluno.getAcademia());
        historico.setAluno(aluno);
        historico.setGraduacaoAnterior(anterior);
        historico.setNovaGraduacao(nova);
        historico.setAlteradoEm(LocalDateTime.now(clock));
        historico.setAlteradoPor(usuario);
        historico.setMotivo(motivo);
        historico.setObservacao(observacao);
        historico.setPromocaoPorAvaliacao(promocaoPorAvaliacao);
        return historico;
    }

    private LocalDate dataEntradaGraduacaoAtual(Aluno aluno, Graduacao atual) {
        return historicoRepository.findFirstByAcademiaIdAndAlunoIdAndNovaGraduacaoIdOrderByAlteradoEmDesc(
                        aluno.getAcademia().getId(), aluno.getId(), atual.getId())
                .map(h -> h.getAlteradoEm().toLocalDate())
                .orElse(LocalDate.now(clock));
    }

    private ElegibilidadeGraduacaoResponse respostaSemProxima(Aluno aluno, Graduacao atual, String motivo) {
        return new ElegibilidadeGraduacaoResponse(aluno.getId(), atual == null ? null : atual.getId(),
                atual == null ? null : atual.getNome(), null, null, 0, 0, 0, 0,
                null, null, false, false, false, motivo);
    }

    private Aluno buscarAlunoDaAcademia(Long alunoId, Long academiaId) {
        return alunoRepository.findByIdAndAcademiaId(alunoId, academiaId)
                .orElseThrow(() -> new RuntimeException("Aluno não encontrado com ID: " + alunoId));
    }

    private Graduacao buscarGraduacaoAtivaDaAcademia(Long graduacaoId, Long academiaId) {
        return graduacaoRepository.findByIdAndAcademiaId(graduacaoId, academiaId)
                .filter(g -> Boolean.TRUE.equals(g.getAtiva()))
                .orElseThrow(() -> new RuntimeException("Graduação ativa não encontrada com ID: " + graduacaoId));
    }

    private void validarAlunoDaAcademia(Aluno aluno, Long academiaId) {
        if (aluno == null || aluno.getAcademia() == null || !academiaId.equals(aluno.getAcademia().getId())) {
            throw new RuntimeException("Aluno não pertence à academia atual");
        }
    }

    private String nomeAluno(Long alunoId, Long academiaId) {
        return alunoRepository.findByIdAndAcademiaId(alunoId, academiaId)
                .map(Aluno::getNome)
                .orElse("");
    }
}
