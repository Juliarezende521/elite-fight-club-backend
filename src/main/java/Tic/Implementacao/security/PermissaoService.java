package Tic.Implementacao.security;

import Tic.Implementacao.model.PerfilUsuario;
import Tic.Implementacao.model.Usuario;
import Tic.Implementacao.model.Academia;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class PermissaoService {

    public Usuario usuarioAtual() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Usuario usuario)) {
            throw new RuntimeException("Usuário não autenticado");
        }
        return usuario;
    }

    public void exigirAdminOuProfessor() {
        Usuario usuario = usuarioAtual();
        if (usuario.getPerfil() != PerfilUsuario.ADMIN && usuario.getPerfil() != PerfilUsuario.PROFESSOR) {
            throw new AccessDeniedException("Acesso permitido apenas para ADMIN ou PROFESSOR");
        }
    }

    public Academia academiaAtual() {
        Usuario usuario = usuarioAtual();
        if (usuario.getAcademia() == null) {
            throw new RuntimeException("Usuário autenticado não possui academia vinculada");
        }
        return usuario.getAcademia();
    }

    public Long academiaAtualId() {
        return academiaAtual().getId();
    }

    public void exigirAdmin() {
        Usuario usuario = usuarioAtual();
        if (usuario.getPerfil() != PerfilUsuario.ADMIN) {
            throw new AccessDeniedException("Acesso permitido apenas para ADMIN");
        }
    }

    public void exigirPermissao(PermissaoSistema permissao) {
        if (!temPermissao(permissao)) {
            throw new AccessDeniedException("Usuário sem permissão: " + permissao.name());
        }
    }

    public boolean temPermissao(PermissaoSistema permissao) {
        Usuario usuario = usuarioAtual();
        if (permissao == PermissaoSistema.PAGAMENTO_CONFIRMAR) {
            return usuario.getPerfil() == PerfilUsuario.ADMIN;
        }
        return false;
    }
}
