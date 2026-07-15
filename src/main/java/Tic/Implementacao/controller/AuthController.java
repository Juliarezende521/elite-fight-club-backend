package Tic.Implementacao.controller;

import Tic.Implementacao.model.Usuario;
import Tic.Implementacao.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UsuarioService service;

    public AuthController(UsuarioService service) {
        this.service = service;
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
    public ResponseEntity<Usuario> usuarioLogado(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }

        Optional<Usuario> usuario = service.buscarPorEmail(authentication.getName());
        if (usuario.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(usuario.get());
    }
}
