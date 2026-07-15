package Tic.Implementacao.repository;

import Tic.Implementacao.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByFirebaseUid(String firebaseUid);

    Optional<Usuario> findByAlunoIdAndAcademiaId(Long alunoId, Long academiaId);

    List<Usuario> findByAcademiaIdAndAlunoIdIn(Long academiaId, List<Long> alunoIds);

    boolean existsByEmail(String email);

    boolean existsByEmailAndAcademiaId(String email, Long academiaId);
}
