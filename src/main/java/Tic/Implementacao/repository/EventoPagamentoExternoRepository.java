package Tic.Implementacao.repository;

import Tic.Implementacao.model.EventoPagamentoExterno;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventoPagamentoExternoRepository extends JpaRepository<EventoPagamentoExterno, Long> {

    boolean existsByProvedorAndEventoExternoIdAndTipoEvento(String provedor, String eventoExternoId, String tipoEvento);
}
