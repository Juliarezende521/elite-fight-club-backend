package Tic.Implementacao.service;

import Tic.Implementacao.model.*;
import Tic.Implementacao.repository.AlunoRepository;
import Tic.Implementacao.repository.FaturaRepository;
import Tic.Implementacao.security.PermissaoService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FaturaServiceTest {

    @Test
    void deveGerarFaturaMensalUmaUnicaVez() {
        FaturaRepository faturaRepository = mock(FaturaRepository.class);
        AlunoRepository alunoRepository = mock(AlunoRepository.class);
        PermissaoService permissaoService = mock(PermissaoService.class);
        Academia academia = academia();
        Aluno aluno = aluno(academia);

        when(permissaoService.academiaAtualId()).thenReturn(1L);
        when(alunoRepository.findByIdAndAcademiaId(10L, 1L)).thenReturn(Optional.of(aluno));
        when(faturaRepository.findByAcademiaIdAndAlunoIdAndMesReferencia(1L, 10L, LocalDate.of(2026, 7, 1)))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(new Fatura()));
        when(faturaRepository.save(any(Fatura.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FaturaService service = new FaturaService(faturaRepository, alunoRepository, permissaoService);

        Fatura fatura = service.gerarFaturaMensal(10L, YearMonth.of(2026, 7));
        service.gerarFaturaMensal(10L, YearMonth.of(2026, 7));

        assertEquals(BigDecimal.valueOf(180), fatura.getValor());
        assertEquals(LocalDate.of(2026, 7, 10), fatura.getDataVencimento());
        verify(faturaRepository, times(1)).save(any(Fatura.class));
    }

    @Test
    void deveDiferenciarCompetenciaComAnoEMes() {
        FaturaRepository faturaRepository = mock(FaturaRepository.class);
        AlunoRepository alunoRepository = mock(AlunoRepository.class);
        PermissaoService permissaoService = mock(PermissaoService.class);
        Academia academia = academia();
        Aluno aluno = aluno(academia);

        when(permissaoService.academiaAtualId()).thenReturn(1L);
        when(alunoRepository.findByIdAndAcademiaId(10L, 1L)).thenReturn(Optional.of(aluno));
        when(faturaRepository.findByAcademiaIdAndAlunoIdAndMesReferencia(1L, 10L, LocalDate.of(2026, 7, 1)))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(new Fatura()));
        when(faturaRepository.findByAcademiaIdAndAlunoIdAndMesReferencia(1L, 10L, LocalDate.of(2027, 7, 1)))
                .thenReturn(Optional.empty());
        when(faturaRepository.save(any(Fatura.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FaturaService service = new FaturaService(faturaRepository, alunoRepository, permissaoService);

        Fatura julho2026 = service.gerarFaturaMensal(10L, YearMonth.of(2026, 7));
        Fatura julho2027 = service.gerarFaturaMensal(10L, YearMonth.of(2027, 7));
        service.gerarFaturaMensal(10L, YearMonth.of(2026, 7));

        assertEquals(LocalDate.of(2026, 7, 1), julho2026.getMesReferencia());
        assertEquals(LocalDate.of(2027, 7, 1), julho2027.getMesReferencia());
        verify(faturaRepository, times(2)).save(any(Fatura.class));
    }

    @Test
    void naoDeveGerarFaturaParaAlunoInativo() {
        FaturaRepository faturaRepository = mock(FaturaRepository.class);
        AlunoRepository alunoRepository = mock(AlunoRepository.class);
        PermissaoService permissaoService = mock(PermissaoService.class);
        Aluno aluno = aluno(academia());
        aluno.setAtivo(false);

        when(permissaoService.academiaAtualId()).thenReturn(1L);
        when(alunoRepository.findByIdAndAcademiaId(10L, 1L)).thenReturn(Optional.of(aluno));

        FaturaService service = new FaturaService(faturaRepository, alunoRepository, permissaoService);

        assertThrows(RuntimeException.class, () -> service.gerarFaturaMensal(10L, YearMonth.of(2026, 7)));
        verify(faturaRepository, never()).save(any(Fatura.class));
    }

    private Academia academia() {
        Academia academia = new Academia();
        academia.setId(1L);
        return academia;
    }

    private Aluno aluno(Academia academia) {
        Aluno aluno = new Aluno();
        aluno.setId(10L);
        aluno.setAcademia(academia);
        aluno.setAtivo(true);
        aluno.setValorMensalidade(BigDecimal.valueOf(180));
        aluno.setDiaVencimento(10);
        return aluno;
    }
}
