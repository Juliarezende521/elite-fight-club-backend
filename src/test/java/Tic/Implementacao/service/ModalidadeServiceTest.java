package Tic.Implementacao.service;

import Tic.Implementacao.model.Academia;
import Tic.Implementacao.model.Modalidade;
import Tic.Implementacao.repository.ModalidadeRepository;
import Tic.Implementacao.security.PermissaoService;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ModalidadeServiceTest {

    @Test
    void adminCriaModalidadeNaAcademiaAtual() {
        ModalidadeRepository repository = mock(ModalidadeRepository.class);
        PermissaoService permissaoService = mock(PermissaoService.class);
        Academia academia = academia();
        Modalidade modalidade = modalidade(academia);

        when(permissaoService.academiaAtual()).thenReturn(academia);
        when(repository.save(modalidade)).thenReturn(modalidade);

        ModalidadeService service = new ModalidadeService(repository, permissaoService);
        Modalidade salva = service.salvar(modalidade);

        assertEquals(academia, salva.getAcademia());
        verify(permissaoService).exigirAdmin();
        verify(repository).save(modalidade);
    }

    @Test
    void professorNaoCriaNemEditaModalidade() {
        ModalidadeRepository repository = mock(ModalidadeRepository.class);
        PermissaoService permissaoService = mock(PermissaoService.class);
        doThrow(new AccessDeniedException("Acesso permitido apenas para ADMIN"))
                .when(permissaoService).exigirAdmin();

        ModalidadeService service = new ModalidadeService(repository, permissaoService);

        assertThrows(AccessDeniedException.class, () -> service.salvar(modalidade(academia())));
        assertThrows(AccessDeniedException.class, () -> service.atualizar(1L, modalidade(academia())));
        verify(repository, never()).save(any());
    }

    @Test
    void modalidadeDeOutraAcademiaNaoEEncontrada() {
        ModalidadeRepository repository = mock(ModalidadeRepository.class);
        PermissaoService permissaoService = mock(PermissaoService.class);
        Academia outra = academia();
        outra.setId(2L);
        Modalidade modalidade = modalidade(outra);

        when(permissaoService.academiaAtualId()).thenReturn(1L);
        when(repository.findById(9L)).thenReturn(Optional.of(modalidade));

        ModalidadeService service = new ModalidadeService(repository, permissaoService);

        assertTrue(service.buscarPorId(9L).isEmpty());
    }

    private Academia academia() {
        Academia academia = new Academia();
        academia.setId(1L);
        academia.setNome("Elite");
        return academia;
    }

    private Modalidade modalidade(Academia academia) {
        Modalidade modalidade = new Modalidade();
        modalidade.setId(9L);
        modalidade.setNome("Muay Thai");
        modalidade.setAcademia(academia);
        modalidade.setAtiva(true);
        return modalidade;
    }
}
