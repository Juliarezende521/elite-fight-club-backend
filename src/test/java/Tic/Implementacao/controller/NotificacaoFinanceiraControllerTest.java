package Tic.Implementacao.controller;

import Tic.Implementacao.dto.notificacao.financeiro.ResultadoAutomacaoNotificacaoFinanceiraResponse;
import Tic.Implementacao.service.NotificacaoFinanceiraAutomacaoService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class NotificacaoFinanceiraControllerTest {

    @Test
    void endpointManualProcessarDelegaParaService() {
        NotificacaoFinanceiraAutomacaoService service = mock(NotificacaoFinanceiraAutomacaoService.class);
        NotificacaoFinanceiraController controller = new NotificacaoFinanceiraController(service);
        LocalDate data = LocalDate.of(2026, 7, 15);
        ResultadoAutomacaoNotificacaoFinanceiraResponse esperado =
                new ResultadoAutomacaoNotificacaoFinanceiraResponse(data, 1, 1, 0, 0, Map.of());
        when(service.gerarNotificacoesFinanceiras(data)).thenReturn(esperado);

        ResultadoAutomacaoNotificacaoFinanceiraResponse response = controller.processar(data);

        assertEquals(esperado, response);
        verify(service).gerarNotificacoesFinanceiras(data);
    }

    @Test
    void endpointManualProcessarVencimentosDelegaParaService() {
        NotificacaoFinanceiraAutomacaoService service = mock(NotificacaoFinanceiraAutomacaoService.class);
        NotificacaoFinanceiraController controller = new NotificacaoFinanceiraController(service);
        LocalDate data = LocalDate.of(2026, 7, 15);
        ResultadoAutomacaoNotificacaoFinanceiraResponse esperado =
                new ResultadoAutomacaoNotificacaoFinanceiraResponse(data, 1, 1, 0, 0, Map.of());
        when(service.gerarLembretesDeVencimento(data)).thenReturn(esperado);

        ResultadoAutomacaoNotificacaoFinanceiraResponse response = controller.processarVencimentos(data);

        assertEquals(esperado, response);
        verify(service).gerarLembretesDeVencimento(data);
    }
}
