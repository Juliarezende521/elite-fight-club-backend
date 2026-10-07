# Revisao de Seguranca - Correcoes Criticas 1

Branch: `security/critical-legacy-endpoints`

## Objetivo

Aplicar somente a primeira etapa de correcoes criticas identificadas na revisao de seguranca, sem alterar frontend, sem misturar com a branch `feature/professor-management` e sem quebrar os fluxos reais ja usados pela aplicacao.

## Diagnostico dos endpoints

### `/auth/registrar`

Problema encontrado:

- Estava liberado em `SecurityConfig` com `permitAll`.
- Aceitava entidade `Usuario` diretamente.
- Poderia permitir criacao local de usuarios fora dos fluxos oficiais com Firebase Auth.

Correcao aplicada:

- Removido o acesso publico.
- Endpoint passou a exigir `ADMIN`.
- Mesmo para `ADMIN`, retorna `410 GONE` com mensagem orientando uso dos fluxos oficiais.
- Nao cria usuario local por esse endpoint.

Substitutos oficiais:

- Criacao de aluno: `POST /api/alunos`.
- Criacao/gestao de professor: endpoints oficiais de professores quando a branch correspondente estiver integrada.
- Sessao atual: `GET /auth/me`.

### `/financeiros`

Problema encontrado:

- Controller legado retornava entidade JPA `Financeiro` diretamente.
- O modulo financeiro real do sistema ja usa faturas, pagamentos, preferencias e relatorios.
- Poderia expor dados antigos ou inconsistentes com o fluxo atual.

Correcao aplicada:

- Codigo Java legado removido: `FinanceiroController`, `FinanceiroService`, `FinanceiroRepository`, entidade `Financeiro` e teste do controller legado.
- O endpoint `/financeiros` nao esta mais disponivel em runtime.
- A tabela historica `financeiros` nao foi removida nesta etapa e permanecera temporariamente no banco.
- Nenhuma migration historica foi alterada.
- O fluxo financeiro moderno permanece baseado em faturas, pagamentos, preferencias e relatorios.

Substitutos oficiais:

- Faturas: `/api/faturas`.
- Pagamentos: `/api/pagamentos`.
- Relatorios financeiros: `/api/relatorios/financeiro`.
- Preferencias de pagamento do aluno: `/api/alunos/me/preferencia-pagamento`.

Pendencia:

- A remocao fisica futura da tabela `financeiros` deve ocorrer somente por migration propria, apos a estrategia de Flyway/baseline estar resolvida.
- A baseline consolidada futura nao deve reproduzir `Financeiro` legado como modelo canonico financeiro.

### `POST /api/webhooks/pagamentos/mock`

Problema encontrado:

- Endpoint mock de pagamento estava disponivel em runtime para qualquer usuario autenticado.
- Recebia `idPagamentoExterno` e `status` por parametro.
- Chamava diretamente o fluxo de alteracao de pagamento, podendo marcar pagamentos como `PAGO`.
- Um aluno autenticado que recebesse ou conhecesse o identificador externo do Pix poderia tentar confirmar indevidamente um pagamento.
- Para um SaaS multiacademia com fluxo financeiro real, isso representa risco alto e nao deve existir em producao.

Correcao aplicada:

- Endpoint mock removido do controller de webhooks.
- Metodo mock removido do service de pagamentos.
- O webhook real do Asaas foi preservado em `/api/webhooks/pagamentos/asaas`.
- O webhook real continua validando `asaas-access-token` com comparacao segura e registrando eventos externos para idempotencia.
- Testes garantem que a rota mock nao esta disponivel em runtime e que o webhook real permanece acessivel.

Pendencia:

- Caso seja necessario simular confirmacao de pagamentos em desenvolvimento, criar uma ferramenta administrativa/dev separada, explicitamente condicionada por profile/configuracao e nunca disponivel em producao.

### `/Relatorio`

Problema encontrado:

- Controller legado retornava entidade JPA `Relatorio` diretamente.
- O service legado dependia de `FinanceiroRepository` e `EvolucaoRepository`.
- A geracao financeira antiga podia consultar dados globais sem isolamento por academia.
- A geracao de evolucao antiga podia consultar dados fisicos por `alunoId` sem garantir tenant.
- A entidade `Relatorio` persiste texto generico sem `academia_id`, o que nao e adequado como modelo canonico para SaaS multiacademia.

Correcao aplicada:

- Codigo Java legado removido: `RelatorioController`, `RelatorioService`, `RelatorioRepository` e entidade `Relatorio`.
- `RelatorioService` foi desacoplado de `FinanceiroRepository` e `EvolucaoRepository`.
- A tabela historica `relatorios` nao foi removida nesta etapa e permanecera temporariamente no banco.
- A auditoria local anterior indicou `0` registros em `relatorios`.
- Nenhuma migration historica foi alterada.
- Relatorios modernos permanecem ativos.

Substitutos oficiais:

- Relatorios financeiros: `/api/relatorios/financeiro`.
- Relatorio contabil: `/api/relatorios/financeiro/contabil`.
- Relatorios de graduacao: `/api/relatorios/graduacoes` e `/api/eventos-graduacao/{id}/relatorio`.
- Evolucao fisica moderna: `/api/alunos/me/pesos` e `/api/alunos/{id}/pesos`.

Pendencia:

- A remocao fisica futura da tabela `relatorios` deve ocorrer somente por migration propria, apos a estrategia de Flyway/baseline estar resolvida.
- A baseline consolidada futura nao deve reproduzir `Relatorio` generico sem tenant como modelo canonico de relatorios.

### `/Evolucao`

Problema encontrado:

- Controller legado retornava entidade JPA `Evolucao` diretamente.
- O endpoint permitia listar, criar, atualizar e excluir registros de evolucao fisica pelo fluxo antigo.
- A entidade legada nao possui `academia_id` direto e nao atende ao isolamento multiacademia esperado para SaaS.
- Os dados envolvidos podem conter informacoes fisicas sensiveis, como peso, bioimpedancia e observacoes.

Correcao aplicada:

- Endpoint legado mantido apenas como compatibilidade tecnica, mas desativado.
- Qualquer metodo HTTP em `/Evolucao` e subrotas retorna `410 GONE` para `ADMIN`.
- Perfis sem permissao administrativa continuam bloqueados.
- A tabela `evolucoes` e a entidade legada foram preservadas temporariamente.
- A auditoria local anterior indicou `0` registros em `evolucoes`.

Substitutos oficiais:

- Evolucao de peso: `/api/alunos/me/pesos` e `/api/alunos/{id}/pesos`.
- Metas de peso, avaliacoes corporais, medidas corporais e bioimpedancia permanecem nos modelos modernos dedicados.

Pendencia:

- A remocao fisica de `Evolucao`, `EvolucaoRepository`, `EvolucaoService` e tabela `evolucoes` deve ocorrer somente em etapa futura coordenada com Flyway/baseline.
- A tela `aluno.evolucao` do frontend ainda usa mock e deve ser integrada ao modelo moderno em etapa propria.

### `/notificacoes` e CRUD legado de notificacoes

Problema encontrado:

- Havia alias legado `/notificacoes`.
- Havia CRUD legado de notificacoes expondo entidade JPA diretamente.
- Isso conflitava com o fluxo novo de notificacoes persistidas por usuario, academia e idempotencia.

Correcao aplicada:

- Removido o alias `/notificacoes`.
- Mantido somente o prefixo operacional `/api/notificacoes`.
- CRUD legado em `/api/notificacoes`, `/api/notificacoes/{id}`, `/api/notificacoes/aluno/{alunoId}`, `/api/notificacoes/pendentes`, `/api/notificacoes/{id}/enviar` e `/api/notificacoes/gerar-automaticas` retorna `410 GONE`.
- Relatorio operacional e geracao manual de avisos/lembretes de graduacao continuam funcionando.

Substitutos oficiais:

- Aluno consulta notificacoes: `GET /api/alunos/me/notificacoes`.
- Aluno marca como lida: `PATCH /api/alunos/me/notificacoes/{id}/lida`.
- Aluno marca todas como lidas: `PATCH /api/alunos/me/notificacoes/marcar-todas-lidas`.
- Professor/admin consulta relatorio operacional: `GET /api/notificacoes/relatorio`.

### Endpoints legados de alunos

Problema encontrado:

- `POST /api/alunos/legado` recebia entidade JPA diretamente.
- `PATCH /api/alunos/{id}` usava fluxo antigo de atualizacao parcial por entidade.
- `DELETE /api/alunos/{id}` removia fisicamente o aluno.

Correcao aplicada:

- `POST /api/alunos/legado` retorna `410 GONE`.
- `PATCH /api/alunos/{id}` retorna `410 GONE`.
- `DELETE /api/alunos/{id}` agora faz inativacao logica do aluno e do usuario local vinculado, preservando historico.

Substitutos oficiais:

- Cadastro: `POST /api/alunos`.
- Edicao completa: `PUT /api/alunos/{id}`.
- Modalidades contratadas: `PUT /api/alunos/{id}/modalidades`.
- Consulta autenticada do aluno: endpoints `/api/alunos/me/...`.

### Modalidades

Problema encontrado:

- Cadastro, edicao e remocao de modalidades aceitavam `ADMIN` ou `PROFESSOR`.
- Para a etapa atual, a gestao de modalidades da academia deve ser administrativa.

Correcao aplicada:

- `salvar`, `atualizar` e `remover` em `ModalidadeService` passaram a exigir `ADMIN`.
- Listagens e leituras continuam disponiveis conforme fluxo ja existente.

### Tratamento global de erros

Problema encontrado:

- `RuntimeException` generica retornava `ex.getMessage()` diretamente ao cliente.
- Isso poderia expor detalhes internos, SQL, nomes de tabela, tokens ou mensagens tecnicas.

Correcao aplicada:

- Criada `NegocioException` para mensagens de negocio controladas.
- `NegocioException` e `IllegalArgumentException` continuam retornando mensagem segura de negocio.
- `RuntimeException` inesperada agora e registrada no log e retorna mensagem generica ao cliente.
- `AccessDeniedException` e erros de validacao continuam padronizados.

## Compatibilidade com o frontend atual

Foi verificado que o frontend atual nao usa:

- `/auth/registrar`.
- `/financeiros`.
- CRUD legado de `/api/notificacoes`.
- `/api/alunos/legado`.

Rotas reais usadas pelo frontend e preservadas:

- `/auth/me`.
- `/api/alunos`.
- `/api/alunos/{id}` via `PUT`.
- `/api/alunos/{id}/modalidades`.
- `/api/alunos/me/notificacoes`.
- `/api/faturas/aluno/{alunoId}`.
- `/modalidades`.

## Testes adicionados

- `AuthControllerTest`: bloqueio do cadastro local direto.
- `NotificacaoControllerTest`: CRUD legado desativado, relatorio preservado e bloqueio por perfil.
- `ModalidadeServiceTest`: somente `ADMIN` cria/edita modalidade.
- `GlobalExceptionHandlerTest`: erro inesperado nao expoe detalhes internos.
- `AlunoServiceTest`: exclusao de aluno virou inativacao logica com sincronizacao do usuario local.

## Validacoes executadas

- `sh mvnw test`: 315 testes, 0 falhas, 0 erros.
- `git diff --check`: aprovado.
- `sh mvnw clean package`: BUILD SUCCESS, 315 testes, 0 falhas, 0 erros.

## Regras para proximas implementacoes

- Evitar novos controllers retornando entidades JPA diretamente.
- Usar DTOs para requests e responses.
- Nao abrir endpoint sensivel com `permitAll`, exceto rotas publicas realmente justificadas.
- Nao aceitar `academiaId`, `destinatarioId`, `role` ou permissao vindos do frontend como fonte de verdade.
- Validar perfil e academia no backend.
- Preferir inativacao logica quando houver historico financeiro, agenda, graduacao ou certificado.
- Mensagens tecnicas devem ficar no log, nao na resposta da API.

## Pendencias futuras

- Migrar controllers legados restantes para DTOs sem quebrar compatibilidade.
- Criar testes de controller com MockMvc para as rotas administrativas mais sensiveis.
- Avaliar `@PreAuthorize` em endpoints administrativos mantendo validacao de academia nos services.
- Padronizar rotas antigas em `/api/...` com plano de depreciacao.
- Revisar contratos de erro em documentacao compartilhada com o frontend.
