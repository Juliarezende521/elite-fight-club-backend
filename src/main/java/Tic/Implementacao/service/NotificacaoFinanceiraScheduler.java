package Tic.Implementacao.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;

@Component
public class NotificacaoFinanceiraScheduler {

    private final NotificacaoFinanceiraAutomacaoService service;
    private final Clock clock;
    private final boolean enabled;
    private final String timeZone;

    public NotificacaoFinanceiraScheduler(NotificacaoFinanceiraAutomacaoService service,
                                          Clock clock,
                                          @Value("${financeiro.notificacoes.enabled:false}") boolean enabled,
                                          @Value("${financeiro.notificacoes.time-zone:America/Sao_Paulo}") String timeZone) {
        this.service = service;
        this.clock = clock;
        this.enabled = enabled;
        this.timeZone = timeZone;
    }

    @Scheduled(cron = "${financeiro.notificacoes.cron:0 0 9 * * *}", zone = "${financeiro.notificacoes.time-zone:America/Sao_Paulo}")
    public void executarDiariamente() {
        if (!enabled) {
            return;
        }
        service.gerarNotificacoesFinanceiras(LocalDate.now(clock.withZone(ZoneId.of(timeZone))));
    }
}
