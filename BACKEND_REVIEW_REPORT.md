# Relatorio de Revisao do Backend

Revisao realizada no marco `v0.1.0-backend`, antes do inicio do Frontend React.

## Objetivo da Revisao

Confirmar que o backend esta pronto para ser consumido pelo frontend e registrar pontos que podem ser melhorados com calma nas proximas etapas, sem alterar regras de negocio agora.

## Resultado Geral

- Backend organizado em camadas: `controller`, `service`, `repository`, `model`, `dto`, `security`, `payments` e `db/migration`.
- Autenticacao protegida por Firebase Authentication e Spring Security.
- Perfis usados: `ADMIN`, `PROFESSOR` e `ALUNO`.
- Multiacademia aplicada nos services principais.
- Flyway ativo, com migrations versionadas ate `V20__preferencias_pagamento_alunos.sql`.
- Asaas Sandbox e provider mock implementados para pagamentos Pix.
- Scheduler financeiro preparado e desabilitado por padrao.
- Documentacao para frontend criada em `FRONTEND_READY.md`.
- Changelog criado em `CHANGELOG.md`.

## Validacoes Executadas

- `sh mvnw test`: 292 testes, 0 falhas, 0 erros.
- `sh mvnw clean package`: BUILD SUCCESS.
- `git status`: limpo apos commit e push.
- `git diff --stat`: usado antes do commit para confirmar apenas alteracoes de documentacao.

## Cobertura de Testes Observada

Ha testes automatizados para os principais services:

- Agenda.
- Agenda do professor.
- Geracao de aulas.
- Reservas.
- Presencas.
- Check-in QR.
- Alunos.
- Financeiro e faturas.
- Pagamentos e webhooks.
- Preferencia de pagamento.
- Notificacoes financeiras.
- Graduacao.
- Eventos de graduacao.
- Avaliacoes de graduacao.
- Certificados.
- Evolucao de peso.
- Metas de peso.
- Avaliacao corporal.
- Relatorios financeiros.
- Relatorio contabil.
- Relatorios de graduacao.
- Dashboard do aluno.

O ponto de melhoria para depois e ampliar testes de controller e testes integrados de API, porque hoje a maior parte da seguranca e das regras esta bem coberta nos services.

## Observacoes Importantes para o Frontend

Tambem apareceu um ponto importante para documentar: ha APIs novas bem padronizadas com DTOs, mas alguns controllers legados ainda retornam entidades JPA diretamente. Isso deve ser tratado como oportunidade de melhoria futura, sem quebrar compatibilidade agora.

Na pratica:

- Endpoints novos de agenda, financeiro, graduacao, certificados, relatorios, notificacoes financeiras, dashboard e evolucao fisica usam DTOs com mais consistencia.
- Alguns endpoints legados ainda retornam ou recebem entidades como `Aluno`, `Professor`, `Turma`, `Aula`, `Modalidade`, `Relatorio`, `Notificacao` e `Campeonato`.
- Para o frontend v0.1, a recomendacao e priorizar endpoints novos com DTO sempre que existirem.
- Endpoints legados podem continuar funcionando, mas devem ser tratados como candidatos a refatoracao futura.

## Melhorias Futuras Recomendadas

### Correcoes criticas de seguranca - etapa 1

Foi aberta a branch `security/critical-legacy-endpoints` para tratar a primeira etapa de correcoes criticas sem misturar com a branch `feature/professor-management`.

Resumo registrado em `SECURITY_REVIEW.md`:

- `/auth/registrar` deixou de ser publico e foi desativado como cadastro local direto.
- O codigo Java de `/financeiros` legado foi removido para evitar exposicao de entidade JPA e dados financeiros antigos; a tabela historica permanece para etapa futura de migracao/baseline.
- CRUD legado de notificacoes foi bloqueado, preservando os endpoints oficiais de aluno e relatorio operacional.
- Endpoints legados de aluno por entidade JPA foram desativados.
- `DELETE /api/alunos/{id}` passou a inativar aluno e usuario local, sem apagar historico.
- Escrita de modalidades passou a exigir `ADMIN`.
- `RuntimeException` inesperada deixou de expor detalhes internos ao cliente.

Validacoes da etapa:

- `sh mvnw test`: 315 testes, 0 falhas, 0 erros.
- Novos testes cobrem endpoints legados bloqueados, erro generico seguro, modalidade restrita a admin e inativacao logica de aluno.

### 1. Padronizar DTOs dos controllers legados

Criar requests/responses especificos para controllers antigos que ainda usam entidades JPA diretamente.

Prioridade sugerida:

- `AlunoController` nos endpoints legados.
- `ProfessorController`.
- `TurmaController`.
- `AulaController`.
- `ModalidadeController`.
- `NotificacaoController` legado.
- `RelatorioController` legado.

Beneficio:

- Menos acoplamento entre frontend e banco.
- Menor risco de expor campos internos.
- Contratos de API mais estaveis.

### 2. Padronizar rotas antigas

Algumas rotas antigas ainda nao seguem totalmente o padrao `/api/...`, por exemplo rotas como `/professores`, `/turmas`, `/aulas`, `/modalidades` e `/Relatorio`.

Recomendacao:

- Manter rotas atuais por compatibilidade.
- Criar, futuramente, rotas novas em `/api/...`.
- Depreciar as antigas apenas depois que o frontend estiver usando as novas.

### 3. Padronizar codigos HTTP

Alguns `POST` retornam `200 OK`, mesmo quando criam recurso. Em uma etapa futura, pode ser melhor usar:

- `201 Created` para criacao.
- `204 No Content` para exclusao ou processamento sem corpo.
- `404 Not Found` para recurso inexistente.
- `409 Conflict` para duplicidades/conflitos.
- `422 Unprocessable Entity` se for adotado para regras de negocio mais especificas.

Como isso pode afetar o frontend, a recomendacao e ajustar com cuidado e de forma planejada.

### 4. Melhorar erros de negocio por tipo

O `GlobalExceptionHandler` ja evita stacktrace para o cliente e padroniza validacao, conflito e acesso negado.

Melhoria futura:

- Criar excecoes especificas para `NotFound`, `Unauthorized`, `UnprocessableEntity` e regras de dominio.
- Evitar que todo `RuntimeException` vire `400 BUSINESS_ERROR`.

### 5. Reforcar testes de controller e seguranca

Os services estao bem cobertos, mas os controllers podem ganhar testes com MockMvc/WebMvcTest para validar:

- Status HTTP.
- Payload de erro.
- Permissoes por perfil.
- Serializacao dos DTOs.
- Rotas publicas.
- Bloqueio de aluno acessando dados de outro aluno.

### 6. Revisar exposicao de IDs internos

Muitos DTOs retornam IDs internos, o que e normal nesta fase para o frontend navegar entre recursos.

Melhoria futura:

- Avaliar onde o frontend realmente precisa desses IDs.
- Evitar expor IDs de entidades auxiliares quando nao forem necessarios.

### 7. Evoluir permissao declarativa

Hoje a protecao global exige autenticacao e varias regras ficam nos services via `PermissaoService`.

Melhoria futura:

- Avaliar uso de anotacoes como `@PreAuthorize` em endpoints administrativos.
- Manter a validacao por academia nos services, que continua sendo essencial.

### 8. Performance e N+1

Foram encontrados bons sinais em relatorios e automacoes, com uso de consultas em lote e joins em pontos importantes.

Pontos para observar com o frontend:

- Listagens legadas que retornam entidades podem carregar relacoes lazy conforme o uso.
- Telas com muitas informacoes devem usar endpoints consolidados, como dashboards e relatorios, em vez de varias chamadas pequenas.
- Ao construir telas grandes, monitorar consultas SQL e tempo de resposta.

### 9. Documentar contratos de erro para o frontend

O frontend deve usar:

- `message` para mensagem principal.
- `fields` para erros de validacao por campo.
- `code` para decisao tecnica quando necessario.

Melhoria futura:

- Criar exemplos de erro em `FRONTEND_READY.md` ou em uma documentacao OpenAPI.

### 10. Considerar OpenAPI/Swagger

O inventario de endpoints foi gerado em Markdown. Para evolucao do frontend, pode ser interessante adicionar OpenAPI/Swagger futuramente.

Beneficio:

- Contratos mais claros.
- Teste manual mais facil.
- Possibilidade de gerar tipos TypeScript.

### 11. Ampliar cadastro completo do aluno

Manter o cadastro simplificado na fase atual de testes e planejar uma etapa futura para dados completos do aluno, sem tornar esses campos obrigatorios agora.

Dados pessoais:

- CPF.
- Data de nascimento.
- Telefone/WhatsApp.
- Foto do aluno.

Endereco:

- CEP.
- Logradouro.
- Numero.
- Complemento.
- Bairro.
- Cidade.
- Estado.

Emergencia e saude:

- Contato de emergencia.
- Nome e contato do responsavel, quando o aluno for menor de idade.
- Observacoes medicas.
- Restricoes, lesoes ou limitacoes para treino.

Documentos e consentimentos:

- Aceite de termos de uso.
- Aceite de responsabilidade.
- Aceite de tratamento de dados.

Dados administrativos:

- Data de matricula.
- Observacoes administrativas.

Essa melhoria deve ser feita em uma fase propria, com banco, DTOs, validacoes e formularios planejados em conjunto.

## Pontos Bons Encontrados

- Separacao clara entre fatura e pagamento.
- Webhook de pagamento com idempotencia.
- Preferencia de pagamento separada de pagamento real.
- Notificacoes financeiras sem criar cobrancas automaticamente.
- Certificados com validacao publica.
- Check-in QR separado de presenca.
- Graduacao com ciclo completo: evento, convite, inscricao, pagamento, presenca, avaliacao, promocao, historico e certificado.
- Relatorios financeiros e contabeis separados.
- Scheduler financeiro desabilitado por padrao, evitando execucao acidental.
- Segredos mantidos fora do repositorio.

## Decisao da Revisao

O backend esta pronto para iniciar o Frontend React.

As melhorias acima devem ser tratadas como backlog tecnico, principalmente depois que o frontend revelar quais contratos precisam ser mais estaveis, enxutos ou padronizados.
