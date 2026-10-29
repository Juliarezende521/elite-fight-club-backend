# Estabilizacao do CRUD legado de professores

Base: origin/main 7b3fe4078dda74ca2a902b433be809884a92d934.
Branch: codex/stabilize-professor-security. Data: 2026-10-29.

O CRUD /professores aceitava entidade JPA e so verificava academia; qualquer perfil
autenticado podia executar escrita. Agora todas as operacoes desse CRUD exigem
ADMIN ativo. As agendas modernas continuam usando seus proprios services.

## Contrato restrito

- GET /professores: 200, lista da academia autenticada.
- GET /professores/{id}: 200 ou 404.
- POST /professores: 201 com Location e DTO.
- PATCH /professores/{id}: 200; nome e CPF obrigatorios, campos opcionais substituidos.
- DELETE /professores/{id}: 204; mantem comportamento de exclusao preexistente.
- Sem autenticacao: 401; perfil incorreto/inativo: 403; campos invalidos: 400.
- ID inexistente ou de outra academia: 404, sem revelar existencia em outro tenant.

Entrada ProfessorRequest: nome (obrigatorio, ate 255), cpf (11 digitos),
especialidade e contato (opcionais, ate 500). CPF tem validacao de formato,
nao de digitos verificadores. Campos id, academia, usuario e perfil nao integram
o DTO e nao alteram os vinculos controlados pelo servidor.

Saida ProfessorResponse: id, nome, especialidade, contato. Nao inclui CPF,
entidade Academia, Usuario, senha ou relacoes JPA.

Nao cria Firebase, ProfessorModalidade, status ativo ou /api/professores.
O modulo completo pendente de professores devera adaptar seu contrato ao integrar
esta branch; nao escolher automaticamente um dos lados em eventuais conflitos
de ProfessorController/ProfessorService. Nenhuma migration ou entidade foi alterada.

ProfessorSecurityTest cobre ADMIN, ALUNO, PROFESSOR, ausencia de autenticacao,
outra academia, mass assignment, entrada invalida e registro inexistente.
ProfessorAuthenticationIntegrationTest verifica 401 pela cadeia real de filtros.
Suíte usa H2; validacao PostgreSQL/Flyway em banco vazio continua pendente.
