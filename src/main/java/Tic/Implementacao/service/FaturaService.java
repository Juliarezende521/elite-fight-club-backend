package Tic.Implementacao.service;

import Tic.Implementacao.model.Aluno;
import Tic.Implementacao.model.Fatura;
import Tic.Implementacao.model.StatusFatura;
import Tic.Implementacao.repository.AlunoRepository;
import Tic.Implementacao.repository.FaturaRepository;
import Tic.Implementacao.security.PermissaoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
public class FaturaService {

    private final FaturaRepository repository;
    private final AlunoRepository alunoRepository;
    private final PermissaoService permissaoService;

    public FaturaService(FaturaRepository repository, AlunoRepository alunoRepository, PermissaoService permissaoService) {
        this.repository = repository;
        this.alunoRepository = alunoRepository;
        this.permissaoService = permissaoService;
    }

    public List<Fatura> listarTodas() {
        return repository.findByAcademiaId(permissaoService.academiaAtualId());
    }

    public List<Fatura> listarPorAluno(Long alunoId) {
        return repository.findByAcademiaIdAndAlunoId(permissaoService.academiaAtualId(), alunoId);
    }

    public Fatura buscarPorId(Long id) {
        return repository.findByIdAndAcademiaId(id, permissaoService.academiaAtualId())
                .orElseThrow(() -> new RuntimeException("Fatura não encontrada com ID: " + id));
    }

    @Transactional
    public Fatura gerarFaturaMensal(Long alunoId, YearMonth mesReferencia) {
        Long academiaId = permissaoService.academiaAtualId();
        Aluno aluno = alunoRepository.findByIdAndAcademiaId(alunoId, academiaId)
                .orElseThrow(() -> new RuntimeException("Aluno não encontrado com ID: " + alunoId));

        if (!Boolean.TRUE.equals(aluno.getAtivo())) {
            throw new RuntimeException("Aluno inativo não recebe novas faturas");
        }

        LocalDate referencia = mesReferencia.atDay(1);
        return repository.findByAcademiaIdAndAlunoIdAndMesReferencia(academiaId, alunoId, referencia)
                .orElseGet(() -> criarFatura(aluno, mesReferencia));
    }

    @Transactional
    public void gerarFaturasMensais(YearMonth mesReferencia) {
        permissaoService.exigirAdminOuProfessor();
        Long academiaId = permissaoService.academiaAtualId();
        alunoRepository.findByAtivoTrueAndAcademiaId(academiaId).forEach(aluno -> gerarFaturaMensal(aluno.getId(), mesReferencia));
    }

    @Transactional
    public Fatura cancelar(Long id) {
        permissaoService.exigirAdminOuProfessor();
        Fatura fatura = buscarPorId(id);
        if (fatura.getStatus() == StatusFatura.PAGA) {
            throw new RuntimeException("Fatura paga não pode ser cancelada");
        }
        fatura.setStatus(StatusFatura.CANCELADA);
        fatura.setAtualizadoEm(LocalDateTime.now());
        return repository.save(fatura);
    }

    private Fatura criarFatura(Aluno aluno, YearMonth mesReferencia) {
        LocalDateTime agora = LocalDateTime.now();
        Fatura fatura = new Fatura();
        fatura.setAluno(aluno);
        fatura.setAcademia(aluno.getAcademia());
        fatura.setMesReferencia(mesReferencia.atDay(1));
        fatura.setDataVencimento(mesReferencia.atDay(aluno.getDiaVencimento()));
        fatura.setValor(aluno.getValorMensalidade());
        fatura.setStatus(StatusFatura.PENDENTE);
        fatura.setCriadoEm(agora);
        fatura.setAtualizadoEm(agora);
        return repository.save(fatura);
    }
}
