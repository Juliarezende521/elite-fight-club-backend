package Tic.Implementacao.service;

import Tic.Implementacao.model.Financeiro;
import Tic.Implementacao.model.Notificacao;
import Tic.Implementacao.model.TipoNotificacao;
import Tic.Implementacao.repository.FinanceiroRepository;
import Tic.Implementacao.repository.NotificacaoRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class NotificacaoService {

    private final NotificacaoRepository repo;
    private final FinanceiroRepository financeiroRepository;

    public NotificacaoService(NotificacaoRepository repo, FinanceiroRepository financeiroRepository){
        this.repo = repo;
        this.financeiroRepository = financeiroRepository;
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
        if (notificacao.getDataEnvio() == null) {
            notificacao.setDataEnvio(LocalDateTime.now());
        }
        return repo.save(notificacao);
    }

    @Transactional
    public Notificacao atualizarInformacoes(Long id, Notificacao alterado) {
        Notificacao notificacao = repo.findById(id).orElse(null);
        if (notificacao == null) return null;

        notificacao.setTipoNotificacao(alterado.getTipoNotificacao());
        notificacao.setMensagem(alterado.getMensagem());
        notificacao.setDataEnvio(alterado.getDataEnvio());
        notificacao.setEnviada(alterado.isEnviada());
        notificacao.setAluno(alterado.getAluno());
        notificacao.setFinanceiro(alterado.getFinanceiro());

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
        LocalDate hoje = LocalDate.now();

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
        notificacao.setAluno(financeiro.getAluno());
        notificacao.setFinanceiro(financeiro);
        notificacao.setEnviada(false);

        return repo.save(notificacao);
    }
}
