package Tic.Implementacao.repository;

import Tic.Implementacao.model.Notificacao;
import Tic.Implementacao.model.TipoNotificacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {

    List<Notificacao> findByAlunoId(Long alunoId);

    List<Notificacao> findByFinanceiroId(Long financeiroId);

    List<Notificacao> findByEnviadaFalse();

    boolean existsByFinanceiroIdAndTipoNotificacao(Long financeiroId, TipoNotificacao tipoNotificacao);
}
