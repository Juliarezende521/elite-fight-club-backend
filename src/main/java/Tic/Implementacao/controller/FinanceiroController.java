package Tic.Implementacao.controller;

import Tic.Implementacao.exception.ApiErrorResponse;
import Tic.Implementacao.security.PermissaoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/financeiros")
@Deprecated
public class FinanceiroController {

    private final PermissaoService permissaoService;

    public FinanceiroController(PermissaoService permissaoService) {
        this.permissaoService = permissaoService;
    }

    @RequestMapping(value = {"", "/**"}, method = {
            RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE
    })
    public ResponseEntity<ApiErrorResponse> endpointLegadoDesativado() {
        permissaoService.exigirAdmin();
        return ResponseEntity.status(HttpStatus.GONE).body(new ApiErrorResponse(
                410,
                "LEGACY_ENDPOINT_DISABLED",
                "Endpoints financeiros legados foram desativados. Use /api/faturas, /api/pagamentos e /api/relatorios/financeiro.",
                Map.of()
        ));
    }
}
