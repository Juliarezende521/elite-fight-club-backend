package Tic.Implementacao.repository;

import Tic.Implementacao.model.Relatorio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RelatorioRepository extends JpaRepository<Relatorio, Long> {

    List<Relatorio> findByTipoRelatorioIgnoreCase(String tipoRelatorio);
}
