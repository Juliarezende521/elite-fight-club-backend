package Tic.Implementacao.controller;

import Tic.Implementacao.dto.auth.AuthenticatedUserResponse;
import Tic.Implementacao.exception.ApiErrorResponse;
import Tic.Implementacao.model.Usuario;
import Tic.Implementacao.repository.UsuarioRepository;
import Tic.Implementacao.security.PermissaoService;
import Tic.Implementacao.service.UsuarioService;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UsuarioService service;
    private final UsuarioRepository usuarioRepository;
    private final PermissaoService permissaoService;

    public AuthController(UsuarioService service, UsuarioRepository usuarioRepository,
                          PermissaoService permissaoService) {
        this.service = service;
        this.usuarioRepository = usuarioRepository;
        this.permissaoService = permissaoService;
    }

    @PostMapping("/registrar")
    public ResponseEntity<ApiErrorResponse> registrar(@RequestBody(required = false) Map<String, Object> ignored) {
        permissaoService.exigirAdmin();
        return ResponseEntity.status(HttpStatus.GONE).body(new ApiErrorResponse(
                410,
                "ENDPOINT_DISABLED",
                "Cadastro local direto foi desativado. Use os fluxos oficiais de cadastro.",
                Map.of()
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<AuthenticatedUserResponse> usuarioLogado(
            Authentication authentication,
            HttpServletRequest request) {
        if (authentication != null && authentication.getPrincipal() instanceof Usuario usuario) {
            return ResponseEntity.ok(AuthenticatedUserResponse.from(usuario));
        }

        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ") || FirebaseApp.getApps().isEmpty()) {
            return ResponseEntity.status(401).build();
        }

        try {
            FirebaseToken firebaseToken = FirebaseAuth.getInstance().verifyIdToken(authorization.substring(7));
            return usuarioRepository.findByFirebaseUid(firebaseToken.getUid())
                    .map(usuario -> ResponseEntity.ok(AuthenticatedUserResponse.from(usuario)))
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(401).build();
        }
    }
}
