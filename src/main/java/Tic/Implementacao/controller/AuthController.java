package Tic.Implementacao.controller;

import Tic.Implementacao.dto.auth.AuthenticatedUserResponse;
import Tic.Implementacao.model.Usuario;
import Tic.Implementacao.repository.UsuarioRepository;
import Tic.Implementacao.service.UsuarioService;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UsuarioService service;
    private final UsuarioRepository usuarioRepository;

    public AuthController(UsuarioService service, UsuarioRepository usuarioRepository) {
        this.service = service;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping("/registrar")
    public ResponseEntity<Usuario> registrar(@Valid @RequestBody Usuario usuario) {
        try {
            return ResponseEntity.ok(service.salvar(usuario));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
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
