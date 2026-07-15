package Tic.Implementacao.repository;

import Tic.Implementacao.model.SessaoCheckIn;
import Tic.Implementacao.model.StatusSessaoCheckIn;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SessaoCheckInRepository extends JpaRepository<SessaoCheckIn, Long> {

    Optional<SessaoCheckIn> findByAcademiaIdAndChaveSessaoAtivaAndStatus(Long academiaId, String chaveSessaoAtiva, StatusSessaoCheckIn status);

    @EntityGraph(attributePaths = {
            "academia", "aulaTurma", "aulaTurma.professor", "aulaTurma.modalidade",
            "aulaPersonal", "aulaPersonal.professor", "aulaPersonal.aluno", "abertaPor"
    })
    Optional<SessaoCheckIn> findByIdAndAcademiaId(Long id, Long academiaId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {
            "academia", "aulaTurma", "aulaTurma.professor", "aulaTurma.modalidade",
            "aulaPersonal", "aulaPersonal.professor", "aulaPersonal.aluno", "abertaPor"
    })
    @Query("select s from SessaoCheckIn s where s.id = :id and s.academia.id = :academiaId")
    Optional<SessaoCheckIn> findByIdAndAcademiaIdForUpdate(@Param("id") Long id, @Param("academiaId") Long academiaId);

    @EntityGraph(attributePaths = {
            "academia", "aulaTurma", "aulaTurma.professor", "aulaTurma.modalidade",
            "aulaPersonal", "aulaPersonal.professor", "aulaPersonal.aluno", "abertaPor"
    })
    @Query("""
            select s
            from SessaoCheckIn s
            where s.academia.id = :academiaId
              and s.status = :status
              and s.expiraEm >= :agora
            """)
    List<SessaoCheckIn> buscarAbertasValidas(@Param("academiaId") Long academiaId,
                                             @Param("status") StatusSessaoCheckIn status,
                                             @Param("agora") LocalDateTime agora);

    Optional<SessaoCheckIn> findByAcademiaIdAndAulaTurmaIdAndStatus(Long academiaId, Long aulaTurmaId, StatusSessaoCheckIn status);

    Optional<SessaoCheckIn> findByAcademiaIdAndAulaPersonalIdAndStatus(Long academiaId, Long aulaPersonalId, StatusSessaoCheckIn status);

    boolean existsByAcademiaIdAndAulaTurmaIdAndStatusAndExpiraEmAfter(Long academiaId, Long aulaTurmaId, StatusSessaoCheckIn status, LocalDateTime agora);

    boolean existsByAcademiaIdAndAulaPersonalIdAndStatusAndExpiraEmAfter(Long academiaId, Long aulaPersonalId, StatusSessaoCheckIn status, LocalDateTime agora);
}
