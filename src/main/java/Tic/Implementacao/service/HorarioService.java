package Tic.Implementacao.service;

import Tic.Implementacao.model.Horario;
import Tic.Implementacao.repository.HorarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class HorarioService {

    private final HorarioRepository repository;

    public HorarioService(HorarioRepository repository) {
        this.repository = repository;
    }

    public List<Horario> listarTodos() {
        return repository.findAll();
    }

    public Optional<Horario> buscarPorId(Long id) {
        return repository.findById(id);
    }

    @Transactional
    public Horario salvar(Horario horario) {
        return repository.save(horario);
    }

    @Transactional
    public Horario atualizar(Long id, Horario dadosNovos) {

        Horario horario = repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Horário não encontrado com ID: " + id));

        horario.setDiaSemana(dadosNovos.getDiaSemana());
        horario.setHoraInicio(dadosNovos.getHoraInicio());
        horario.setHoraFim(dadosNovos.getHoraFim());
        horario.setLimiteAlunos(dadosNovos.getLimiteAlunos());
        horario.setModalidade(dadosNovos.getModalidade());

        return repository.save(horario);
    }

    @Transactional
    public boolean remover(Long id) {

        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }

        return false;
    }
}