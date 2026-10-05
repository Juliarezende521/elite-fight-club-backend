package Tic.Implementacao.controller;

import Tic.Implementacao.exception.ApiErrorResponse;
import Tic.Implementacao.security.PermissaoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/Evolucao")
@Deprecated
public class EvolucaoController {

    private final PermissaoService permissaoService;

    public EvolucaoController(PermissaoService permissaoService) {
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
                "Endpoints legados de evolução foram desativados. Use os recursos modernos de evolução física.",
                Map.of()
        ));
    }
}
