package Tic.Implementacao.controller;

import Tic.Implementacao.exception.ApiErrorResponse;
import Tic.Implementacao.security.PermissaoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/Relatorio")
@Deprecated
public class RelatorioController {

    private final PermissaoService permissaoService;

    public RelatorioController(PermissaoService permissaoService) {
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
                "Endpoints legados de relatórios foram desativados. Use os relatórios modernos em /api/relatorios.",
                Map.of()
        ));
    }
}
