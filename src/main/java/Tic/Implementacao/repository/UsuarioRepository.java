package Tic.Implementacao.repository;

import Tic.Implementacao.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByFirebaseUid(String firebaseUid);

    Optional<Usuario> findByAlunoIdAndAcademiaId(Long alunoId, Long academiaId);

    boolean existsByEmail(String email);

    boolean existsByEmailAndAcademiaId(String email, Long academiaId);
}
