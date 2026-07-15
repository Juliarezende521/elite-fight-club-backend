package Tic.Implementacao.config;

import Tic.Implementacao.model.Academia;
import Tic.Implementacao.model.Graduacao;
import Tic.Implementacao.model.StatusAcademia;
import Tic.Implementacao.repository.AcademiaRepository;
import Tic.Implementacao.repository.GraduacaoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class ApplicationDataInitializer implements CommandLineRunner {

    private static final String ACADEMIA_PADRAO = "Elite Fight Club";

    private final AcademiaRepository academiaRepository;
    private final GraduacaoRepository graduacaoRepository;

    public ApplicationDataInitializer(AcademiaRepository academiaRepository, GraduacaoRepository graduacaoRepository) {
        this.academiaRepository = academiaRepository;
        this.graduacaoRepository = graduacaoRepository;
    }

    @Override
    public void run(String... args) {
        Academia academia = academiaRepository.findByNomeIgnoreCase(ACADEMIA_PADRAO)
                .orElseGet(this::criarAcademiaPadrao);

        graduacaoRepository.findByAcademiaIdAndGraduacaoBaseTrue(academia.getId())
                .orElseGet(() -> criarGraduacaoBase(academia));
    }

    private Academia criarAcademiaPadrao() {
        Academia academia = new Academia();
        academia.setNome(ACADEMIA_PADRAO);
        academia.setStatus(StatusAcademia.ATIVA);
        academia.setDataCriacao(LocalDateTime.now());
        return academiaRepository.save(academia);
    }

    private Graduacao criarGraduacaoBase(Academia academia) {
        Graduacao graduacao = new Graduacao();
        graduacao.setNome("Branco");
        graduacao.setCorHex("#FFFFFF");
        graduacao.setOrdem(1);
        graduacao.setCheckinsNecessarios(0);
        graduacao.setTempoMinimoMeses(0);
        graduacao.setValorPadraoGraduacao(BigDecimal.ZERO);
        graduacao.setDescricao("Graduação inicial padrão");
        graduacao.setGraduacaoBase(true);
        graduacao.setAtiva(true);
        graduacao.setAcademia(academia);
        return graduacaoRepository.save(graduacao);
    }
}
