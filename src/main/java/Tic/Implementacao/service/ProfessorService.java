package Tic.Implementacao.service;

import Tic.Implementacao.dto.professor.*;
import Tic.Implementacao.model.*;
import Tic.Implementacao.repository.ModalidadeRepository;
import Tic.Implementacao.repository.ProfessorModalidadeRepository;
import Tic.Implementacao.repository.ProfessorRepository;
import Tic.Implementacao.repository.UsuarioRepository;
import Tic.Implementacao.security.PermissaoService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProfessorService {

    private final ProfessorRepository professorRepository;
    private final ProfessorModalidadeRepository professorModalidadeRepository;
    private final ModalidadeRepository modalidadeRepository;
    private final UsuarioRepository usuarioRepository;
    private final FirebaseAuthService firebaseAuthService;
    private final PermissaoService permissaoService;
    private final TransactionTemplate transactionTemplate;

    public ProfessorService(ProfessorRepository professorRepository,
                            ProfessorModalidadeRepository professorModalidadeRepository,
                            ModalidadeRepository modalidadeRepository,
                            UsuarioRepository usuarioRepository,
                            FirebaseAuthService firebaseAuthService,
                            PermissaoService permissaoService,
                            PlatformTransactionManager transactionManager) {
        this.professorRepository = professorRepository;
        this.professorModalidadeRepository = professorModalidadeRepository;
        this.modalidadeRepository = modalidadeRepository;
        this.usuarioRepository = usuarioRepository;
        this.firebaseAuthService = firebaseAuthService;
        this.permissaoService = permissaoService;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    public List<ProfessorResponse> listar(Long modalidadeId, TipoAtendimentoProfessor tipoAtendimento) {
        Usuario usuario = permissaoService.usuarioAtual();
        Long academiaId = permissaoService.academiaAtualId();

        if (usuario.getPerfil() == PerfilUsuario.PROFESSOR) {
            Professor professor = exigirProfessorDoUsuario(usuario, academiaId);
            return List.of(toResponse(professor));
        }
        if (usuario.getPerfil() != PerfilUsuario.ADMIN) {
            throw new AccessDeniedException("Acesso permitido apenas para ADMIN ou PROFESSOR");
        }

        List<Professor> professores = professorRepository.findAllByAcademiaIdOrderByNomeAsc(academiaId);
        return professores.stream()
                .filter(professor -> filtrarPorModalidadeEAtendimento(professor, academiaId, modalidadeId, tipoAtendimento))
                .map(this::toResponse)
                .toList();
    }

    public ProfessorResponse buscar(Long id) {
        Long academiaId = permissaoService.academiaAtualId();
        Professor professor = buscarProfessor(id, academiaId);
        Usuario usuario = permissaoService.usuarioAtual();
        if (usuario.getPerfil() == PerfilUsuario.PROFESSOR
                && (usuario.getProfessor() == null || !usuario.getProfessor().getId().equals(id))) {
            throw new AccessDeniedException("Professor pode consultar apenas o próprio cadastro");
        }
        if (usuario.getPerfil() == PerfilUsuario.ALUNO) {
            throw new AccessDeniedException("Acesso permitido apenas para ADMIN ou PROFESSOR");
        }
        return toResponse(professor);
    }

    public ProfessorResponse criar(CriarProfessorRequest request) {
        permissaoService.exigirAdmin();
        Academia academia = permissaoService.academiaAtual();
        validarEmailDisponivel(request.getEmail(), null, academia.getId());
        validarModalidades(request.getModalidades(), academia.getId());

        String nome = normalizarObrigatorio(request.getNome(), "Nome é obrigatório");
        String email = normalizarObrigatorio(request.getEmail(), "E-mail é obrigatório");
        String firebaseUid = firebaseAuthService.criarUsuario(nome, email, request.getSenhaInicial());

        try {
            Professor professor = transactionTemplate.execute(status ->
                    salvarProfessorComUsuario(request, firebaseUid, academia));
            return toResponse(professor);
        } catch (RuntimeException e) {
            firebaseAuthService.excluirUsuario(firebaseUid);
            throw e;
        }
    }

    protected Professor salvarProfessorComUsuario(CriarProfessorRequest request, String firebaseUid, Academia academia) {
        Professor professor = new Professor();
        professor.setNome(normalizarObrigatorio(request.getNome(), "Nome é obrigatório"));
        professor.setCpf(normalizarObrigatorio(request.getCpf(), "CPF é obrigatório"));
        professor.setContato(normalizarOpcional(request.getContato()));
        professor.setEspecialidade(criarResumoEspecialidades(request.getModalidades(), academia.getId()));
        professor.setAtivo(true);
        professor.setAcademia(academia);
        professor = professorRepository.save(professor);

        Usuario usuario = new Usuario();
        usuario.setNome(professor.getNome());
        usuario.setEmail(normalizarObrigatorio(request.getEmail(), "E-mail é obrigatório"));
        usuario.setSenha(null);
        usuario.setFirebaseUid(firebaseUid);
        usuario.setPerfil(PerfilUsuario.PROFESSOR);
        usuario.setAtivo(true);
        usuario.setProfessor(professor);
        usuario.setAcademia(academia);
        usuarioRepository.save(usuario);

        aplicarModalidades(professor, request.getModalidades(), academia);
        return professor;
    }

    @Transactional
    public ProfessorResponse atualizar(Long id, AtualizarProfessorRequest request) {
        permissaoService.exigirAdmin();
        Academia academia = permissaoService.academiaAtual();
        Professor professor = buscarProfessor(id, academia.getId());
        validarEmailDisponivel(request.getEmail(), professor.getId(), academia.getId());
        validarModalidades(request.getModalidades(), academia.getId());

        Usuario usuario = usuarioRepository.findByProfessorIdAndAcademiaId(professor.getId(), academia.getId())
                .orElseThrow(() -> new RuntimeException("Usuário do professor não encontrado"));

        String nome = normalizarObrigatorio(request.getNome(), "Nome é obrigatório");
        String email = normalizarObrigatorio(request.getEmail(), "E-mail é obrigatório");
        boolean deveAtualizarFirebase = usuario.getFirebaseUid() != null
                && !usuario.getFirebaseUid().isBlank()
                && (!nome.equals(usuario.getNome()) || !email.equalsIgnoreCase(usuario.getEmail()));

        if (deveAtualizarFirebase) {
            firebaseAuthService.atualizarUsuario(usuario.getFirebaseUid(), nome, email);
        }

        professor.setNome(nome);
        professor.setCpf(normalizarObrigatorio(request.getCpf(), "CPF é obrigatório"));
        professor.setContato(normalizarOpcional(request.getContato()));
        professor.setEspecialidade(criarResumoEspecialidades(request.getModalidades(), academia.getId()));
        professor.setAtivo(request.getAtivo());

        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setAtivo(request.getAtivo());
        usuarioRepository.save(usuario);

        aplicarModalidades(professor, request.getModalidades(), academia);
        return toResponse(professorRepository.save(professor));
    }

    @Transactional
    public ProfessorResponse alterarStatus(Long id, AtualizarStatusProfessorRequest request) {
        permissaoService.exigirAdmin();
        Long academiaId = permissaoService.academiaAtualId();
        Professor professor = buscarProfessor(id, academiaId);
        professor.setAtivo(request.getAtivo());
        usuarioRepository.findByProfessorIdAndAcademiaId(professor.getId(), academiaId)
                .ifPresent(usuario -> {
                    usuario.setAtivo(request.getAtivo());
                    usuarioRepository.save(usuario);
                });
        return toResponse(professorRepository.save(professor));
    }

    @Transactional
    public boolean remover(Long id) {
        AtualizarStatusProfessorRequest request = new AtualizarStatusProfessorRequest();
        request.setAtivo(false);
        alterarStatus(id, request);
        return true;
    }

    public List<ProfessorModalidadeResponse> listarModalidades(Long professorId) {
        buscar(professorId);
        return professorModalidadeRepository
                .findByAcademiaIdAndProfessorId(permissaoService.academiaAtualId(), professorId)
                .stream()
                .map(this::toModalidadeResponse)
                .toList();
    }

    @Transactional
    public ProfessorResponse atualizarModalidades(Long professorId, AtualizarModalidadesProfessorRequest request) {
        permissaoService.exigirAdmin();
        Academia academia = permissaoService.academiaAtual();
        Professor professor = buscarProfessor(professorId, academia.getId());
        validarModalidades(request.getModalidades(), academia.getId());
        professor.setEspecialidade(criarResumoEspecialidades(request.getModalidades(), academia.getId()));
        aplicarModalidades(professor, request.getModalidades(), academia);
        return toResponse(professorRepository.save(professor));
    }

    private boolean filtrarPorModalidadeEAtendimento(Professor professor, Long academiaId,
                                                     Long modalidadeId, TipoAtendimentoProfessor tipoAtendimento) {
        if (modalidadeId == null && tipoAtendimento == null) {
            return true;
        }
        return professorModalidadeRepository.findByAcademiaIdAndProfessorIdAndAtivoTrue(academiaId, professor.getId())
                .stream()
                .anyMatch(vinculo -> {
                    boolean modalidadeOk = modalidadeId == null || modalidadeId.equals(vinculo.getModalidade().getId());
                    boolean atendimentoOk = tipoAtendimento == null
                            || (tipoAtendimento == TipoAtendimentoProfessor.TURMA && Boolean.TRUE.equals(vinculo.getPermiteTurma()))
                            || (tipoAtendimento == TipoAtendimentoProfessor.PERSONAL && Boolean.TRUE.equals(vinculo.getPermitePersonal()));
                    return modalidadeOk && atendimentoOk;
                });
    }

    private Professor exigirProfessorDoUsuario(Usuario usuario, Long academiaId) {
        if (usuario.getProfessor() == null) {
            throw new AccessDeniedException("Usuário PROFESSOR sem professor vinculado");
        }
        return buscarProfessor(usuario.getProfessor().getId(), academiaId);
    }

    private Professor buscarProfessor(Long id, Long academiaId) {
        return professorRepository.findByIdAndAcademiaId(id, academiaId)
                .orElseThrow(() -> new RuntimeException("Professor não encontrado com ID: " + id));
    }

    private void validarEmailDisponivel(String email, Long professorAtualId, Long academiaId) {
        String emailNormalizado = normalizarObrigatorio(email, "E-mail é obrigatório");
        usuarioRepository.findByEmail(emailNormalizado)
                .filter(usuario -> usuario.getProfessor() == null
                        || professorAtualId == null
                        || !professorAtualId.equals(usuario.getProfessor().getId()))
                .ifPresent(usuario -> {
                    throw new RuntimeException("E-mail já cadastrado");
                });
    }

    private void validarModalidades(List<ProfessorModalidadeRequest> requests, Long academiaId) {
        if (requests == null || requests.isEmpty()) {
            throw new RuntimeException("Informe ao menos uma modalidade ministrada");
        }

        Set<Long> idsUnicos = new LinkedHashSet<>();
        for (ProfessorModalidadeRequest request : requests) {
            if (request.getModalidadeId() == null) {
                throw new RuntimeException("Modalidade é obrigatória");
            }
            if (!idsUnicos.add(request.getModalidadeId())) {
                throw new RuntimeException("Modalidade duplicada para o professor");
            }
            if (!Boolean.TRUE.equals(request.getPermiteTurma()) && !Boolean.TRUE.equals(request.getPermitePersonal())) {
                throw new RuntimeException("Professor deve permitir turma ou personal em cada modalidade");
            }
        }

        List<Modalidade> modalidades = modalidadeRepository.findByIdInAndAcademiaId(new ArrayList<>(idsUnicos), academiaId);
        if (modalidades.size() != idsUnicos.size()) {
            throw new RuntimeException("Uma ou mais modalidades não pertencem à academia atual");
        }
        modalidades.forEach(modalidade -> {
            if (!modalidade.isAtiva()) {
                throw new RuntimeException("Modalidade inativa não pode ser vinculada ao professor");
            }
        });
    }

    private void aplicarModalidades(Professor professor, List<ProfessorModalidadeRequest> requests, Academia academia) {
        Map<Long, ProfessorModalidade> existentes = professorModalidadeRepository
                .findByAcademiaIdAndProfessorId(academia.getId(), professor.getId())
                .stream()
                .collect(Collectors.toMap(vinculo -> vinculo.getModalidade().getId(), Function.identity()));

        Set<Long> solicitadas = requests.stream()
                .map(ProfessorModalidadeRequest::getModalidadeId)
                .collect(Collectors.toSet());

        existentes.values().stream()
                .filter(vinculo -> !solicitadas.contains(vinculo.getModalidade().getId()))
                .forEach(vinculo -> {
                    vinculo.setAtivo(false);
                    professorModalidadeRepository.save(vinculo);
                });

        Map<Long, Modalidade> modalidades = modalidadeRepository
                .findByIdInAndAcademiaId(new ArrayList<>(solicitadas), academia.getId())
                .stream()
                .collect(Collectors.toMap(Modalidade::getId, Function.identity()));

        for (ProfessorModalidadeRequest request : requests) {
            ProfessorModalidade vinculo = existentes.getOrDefault(request.getModalidadeId(), new ProfessorModalidade());
            vinculo.setAcademia(academia);
            vinculo.setProfessor(professor);
            vinculo.setModalidade(modalidades.get(request.getModalidadeId()));
            vinculo.setPermiteTurma(request.getPermiteTurma());
            vinculo.setPermitePersonal(request.getPermitePersonal());
            vinculo.setAtivo(true);
            professorModalidadeRepository.save(vinculo);
        }
    }

    private String criarResumoEspecialidades(List<ProfessorModalidadeRequest> requests, Long academiaId) {
        if (requests == null || requests.isEmpty()) {
            return null;
        }
        List<Long> ids = requests.stream().map(ProfessorModalidadeRequest::getModalidadeId).toList();
        Map<Long, Modalidade> modalidades = modalidadeRepository.findByIdInAndAcademiaId(ids, academiaId).stream()
                .collect(Collectors.toMap(Modalidade::getId, Function.identity()));
        return requests.stream()
                .map(request -> {
                    Modalidade modalidade = modalidades.get(request.getModalidadeId());
                    return modalidade == null ? null : modalidade.getNome();
                })
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.joining(", "));
    }

    private ProfessorResponse toResponse(Professor professor) {
        Long academiaId = professor.getAcademia() == null ? null : professor.getAcademia().getId();
        String academiaNome = professor.getAcademia() == null ? null : professor.getAcademia().getNome();
        String email = usuarioRepository.findByProfessorIdAndAcademiaId(professor.getId(), academiaId)
                .map(Usuario::getEmail)
                .orElse(null);
        List<ProfessorModalidadeResponse> modalidades = professorModalidadeRepository
                .findByAcademiaIdAndProfessorId(academiaId, professor.getId())
                .stream()
                .map(this::toModalidadeResponse)
                .toList();
        return new ProfessorResponse(
                professor.getId(),
                professor.getNome(),
                professor.getCpf(),
                email,
                professor.getContato(),
                professor.getAtivo(),
                academiaId,
                academiaNome,
                modalidades
        );
    }

    private ProfessorModalidadeResponse toModalidadeResponse(ProfessorModalidade vinculo) {
        Modalidade modalidade = vinculo.getModalidade();
        return new ProfessorModalidadeResponse(
                vinculo.getId(),
                modalidade.getId(),
                modalidade.getNome(),
                vinculo.getPermiteTurma(),
                vinculo.getPermitePersonal(),
                vinculo.getAtivo()
        );
    }

    private String normalizarObrigatorio(String valor, String mensagem) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new RuntimeException(mensagem);
        }
        return valor.trim();
    }

    private String normalizarOpcional(String valor) {
        return valor == null ? null : valor.trim();
    }
}
