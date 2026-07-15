# Frontend Ready - Elite Fight Club Backend v0.1.0

Este documento resume o estado do backend para iniciar o desenvolvimento do frontend React. A API Spring Boot esta organizada em camadas, usa Firebase Authentication para identidade, Spring Security para proteger rotas, Flyway para banco versionado e isolamento por academia nos services principais.

## Status Geral

- Backend validado para o marco `v0.1.0-backend`.
- Total de endpoints mapeados nos controllers: 275.
- Total de DTOs no backend: 132.
- Total de models/enums: 70.
- Migrations Flyway versionadas ate `V20__preferencias_pagamento_alunos.sql`.
- Provedor de pagamento desacoplado: `mock` por padrao e `asaas` para sandbox Pix.
- Scheduler financeiro preparado e desabilitado por padrao.

## Autenticacao e Perfis

A autenticacao real usa Firebase. O frontend deve enviar o token Firebase em todas as chamadas protegidas:

```http
Authorization: Bearer <firebase_id_token>
```

Perfis usados pelo backend:

- `ADMIN`: acesso administrativo completo da propria academia.
- `PROFESSOR`: operacao de agenda, alunos, graduacao, presencas e consultas permitidas.
- `ALUNO`: consulta e operacao apenas dos proprios dados.

Rotas publicas conhecidas:

- `POST /auth/registrar`
- `POST /api/webhooks/pagamentos/asaas`
- `GET /api/certificados/validar/{codigo}`

## Ordem Recomendada para o Frontend

1. Base do app: login Firebase, interceptador HTTP com Bearer token, tratamento global de erros.
2. Layout por perfil: Admin, Professor e Aluno.
3. Dashboard do aluno: financeiro, proximas aulas, check-ins, graduacao, peso e avaliacao corporal.
4. Agenda: horarios recorrentes, aulas concretas, reservas e presencas.
5. Financeiro: faturas, Pix, pagamentos manuais, preferencia de pagamento e notificacoes financeiras.
6. Check-in QR: sessao de QR fixo da academia e confirmacao do aluno.
7. Graduacao: Prajieds, elegibilidade, eventos, convites, inscricoes, avaliacao, promocao e certificados.
8. Evolucao fisica: peso, metas e avaliacao corporal.
9. Relatorios: financeiro mensal/anual, contabil e graduacao.
10. Administracao: planos, modalidades, professores e ajustes operacionais.

## Fluxo Financeiro

- `Fatura` representa a obrigacao financeira da mensalidade.
- `Pagamento` representa tentativa/confirmacao de pagamento.
- A fatura so deve ser considerada paga quando houver pagamento confirmado ou status consolidado pelo backend.
- Pix Asaas e webhook ja estao preparados.
- Preferencia de pagamento do aluno nao cria pagamento real sozinha.
- Pix Automatico e cartao recorrente ainda sao contextuais, sem integracao real nesta versao.

Endpoints principais:

- `GET /api/faturas/aluno/{alunoId}`
- `GET /api/alunos/me/faturas`
- `POST /api/faturas/gerar-mensal`
- `POST /api/faturas/{faturaId}/pix`
- `POST /api/faturas/{faturaId}/pagamento-manual`
- `POST /api/pagamentos/{pagamentoId}/conciliar`
- `GET /api/alunos/me/preferencia-pagamento`
- `PUT /api/alunos/me/preferencia-pagamento`
- `GET /api/notificacoes/financeiro/relatorio`

## Fluxo de Agenda

- `Plano` define tipo `TURMA` ou `PERSONAL`.
- `HorarioTurma` e `AgendaPersonal` representam recorrencia.
- `AulaTurma` e `AulaPersonal` representam aulas concretas por data.
- Reservas ainda se aplicam a aulas de turma.
- Presencas e check-ins ficam separados para preservar historico.

Endpoints principais:

- `POST /api/agenda/turmas`
- `POST /api/agenda/personais`
- `POST /api/aulas/gerar`
- `POST /api/aulas/gerar-periodo`
- `GET /api/agenda-professor/{professorId}/dia`
- `GET /api/agenda-professor/{professorId}/semana`
- `POST /api/aulas/turmas/{aulaId}/reservas`
- `PUT /api/aulas/turmas/{aulaId}/presencas/{alunoId}`

## Fluxo de Check-in

- QR fixo por academia.
- Sessao de check-in controla janela de tempo.
- Aluno confirma check-in via opcoes disponiveis.
- Historico de check-ins fica persistido.

Endpoints principais:

- `GET /api/check-in/qr/academia`
- `GET /api/check-in/qr/sessao`
- `GET /api/check-in/qr/opcoes`
- `POST /api/check-in/qr/confirmar`
- `GET /api/alunos/me/check-ins`

## Fluxo de Graduacao

- Prajieds configuraveis por academia.
- Elegibilidade considera check-ins e tempo minimo.
- Eventos de graduacao possuem convites, inscricoes, Pix, presenca, avaliacao e promocao.
- Certificados sao persistidos, validados por codigo e podem ser cancelados por admin.

Endpoints principais:

- `GET /api/graduacoes`
- `POST /api/graduacoes`
- `GET /api/graduacoes/alunos/{alunoId}/elegibilidade`
- `POST /api/eventos-graduacao`
- `POST /api/eventos-graduacao/{eventoId}/publicar`
- `POST /api/alunos/me/eventos-graduacao/{eventoId}/inscrever`
- `POST /api/alunos/me/inscricoes-graduacao/{inscricaoId}/pix`
- `GET /api/alunos/me/graduacoes`
- `GET /api/alunos/me/certificados`
- `GET /api/certificados/validar/{codigo}`

## Fluxo de Dashboard

Dashboard do aluno consolida:

- Prajied atual e progresso.
- Check-ins.
- Plano.
- Financeiro e Pix pendente.
- Preferencia de pagamento.
- Evolucao de peso.
- Meta de peso.
- Avaliacao corporal.
- Aulas de hoje e da semana.
- Horarios fixos de personal.

Endpoint principal:

- `GET /api/alunos/me/dashboard`

## Tratamento de Erros

O backend usa `GlobalExceptionHandler` para respostas padronizadas:

- `400 VALIDATION_ERROR` para Bean Validation.
- `400 BUSINESS_ERROR` para regras de negocio.
- `403 FORBIDDEN` para acesso negado.
- `409 CONFLICT` para conflitos conhecidos.

O frontend deve exibir `message` para o usuario e, quando existir, mapear `fields` para validacao de formulario.

## Observacoes de Prontidao

- APIs novas usam DTOs de request/response de forma consistente.
- Alguns controllers legados ainda retornam entidades JPA diretamente (`Aluno`, `Professor`, `Turma`, `Aula`, `Modalidade`, `Notificacao` legado). Para o frontend v0.1, preferir endpoints novos com DTO quando existirem.
- Multiacademia e permissoes ficam concentradas nos services; o frontend nunca deve enviar `academiaId` como fonte de verdade.
- Segredos do Asaas, Firebase e banco devem permanecer fora do repositorio.

## Inventario de Endpoints

A tabela abaixo foi gerada a partir dos controllers atuais. A coluna de perfil indica a regra esperada no frontend; a autorizacao definitiva fica no backend.

### Autenticação

| Método | URL | Perfil permitido | Entrada | Saída |
|---|---|---|---|---|
| `GET` | `/auth/me` | Autenticado | `-` | `Usuario` |
| `POST` | `/auth/registrar` | Público | `Usuario` | `Usuario` |

### Alunos

| Método | URL | Perfil permitido | Entrada | Saída |
|---|---|---|---|---|
| `GET` | `/api/alunos` | Autenticado; regra final no service | `-` | `List<Aluno>` |
| `POST` | `/api/alunos` | Autenticado; regra final no service | `CriarAlunoRequest` | `Aluno` |
| `POST` | `/api/alunos/legado` | Autenticado; regra final no service | `Aluno` | `Aluno` |
| `GET` | `/api/alunos/me/metas-peso` | ALUNO | `-` | `List<MetaPesoResponse>` |
| `POST` | `/api/alunos/me/metas-peso` | ALUNO | `CriarMetaPesoRequest` | `MetaPesoResponse` |
| `GET` | `/api/alunos/me/metas-peso/ativa` | ALUNO | `-` | `ProgressoMetaPesoResponse` |
| `GET` | `/api/alunos/me/metas-peso/{metaId}` | ALUNO | `-` | `MetaPesoResponse` |
| `PUT` | `/api/alunos/me/metas-peso/{metaId}` | ALUNO | `AtualizarMetaPesoRequest` | `MetaPesoResponse` |
| `PATCH` | `/api/alunos/me/metas-peso/{metaId}/cancelar` | ALUNO | `EncerrarMetaPesoRequest` | `MetaPesoResponse` |
| `POST` | `/api/alunos/me/metas-peso/{metaId}/substituir` | ALUNO | `EncerrarMetaPesoRequest` | `MetaPesoResponse` |
| `GET` | `/api/alunos/me/pesos` | ALUNO | `-` | `List<EvolucaoPesoResponse>` |
| `POST` | `/api/alunos/me/pesos` | ALUNO | `RegistrarPesoRequest` | `EvolucaoPesoResponse` |
| `GET` | `/api/alunos/me/pesos/resumo` | ALUNO | `-` | `ResumoEvolucaoPesoResponse` |
| `DELETE` | `/api/alunos/me/pesos/{registroId}` | ALUNO | `-` | `Void` |
| `PUT` | `/api/alunos/me/pesos/{registroId}` | ALUNO | `AtualizarPesoRequest` | `EvolucaoPesoResponse` |
| `GET` | `/api/alunos/modalidade/{modalidadeId}` | Autenticado; regra final no service | `-` | `List<Aluno>` |
| `GET` | `/api/alunos/tipo-modalidade/{tipoModalidade}` | Autenticado; regra final no service | `-` | `List<Aluno>` |
| `GET` | `/api/alunos/{alunoId}/metas-peso` | Autenticado; regra final no service | `-` | `List<MetaPesoResponse>` |
| `POST` | `/api/alunos/{alunoId}/metas-peso` | Autenticado; regra final no service | `CriarMetaPesoRequest` | `MetaPesoResponse` |
| `GET` | `/api/alunos/{alunoId}/metas-peso/ativa` | Autenticado; regra final no service | `-` | `ProgressoMetaPesoResponse` |
| `GET` | `/api/alunos/{alunoId}/metas-peso/{metaId}` | Autenticado; regra final no service | `-` | `MetaPesoResponse` |
| `PUT` | `/api/alunos/{alunoId}/metas-peso/{metaId}` | Autenticado; regra final no service | `AtualizarMetaPesoRequest` | `MetaPesoResponse` |
| `PATCH` | `/api/alunos/{alunoId}/metas-peso/{metaId}/cancelar` | Autenticado; regra final no service | `EncerrarMetaPesoRequest` | `MetaPesoResponse` |
| `POST` | `/api/alunos/{alunoId}/metas-peso/{metaId}/substituir` | Autenticado; regra final no service | `EncerrarMetaPesoRequest` | `MetaPesoResponse` |
| `GET` | `/api/alunos/{alunoId}/pesos` | Autenticado; regra final no service | `-` | `List<EvolucaoPesoResponse>` |
| `POST` | `/api/alunos/{alunoId}/pesos` | Autenticado; regra final no service | `RegistrarPesoRequest` | `EvolucaoPesoResponse` |
| `GET` | `/api/alunos/{alunoId}/pesos/resumo` | Autenticado; regra final no service | `-` | `ResumoEvolucaoPesoResponse` |
| `DELETE` | `/api/alunos/{alunoId}/pesos/{registroId}` | Autenticado; regra final no service | `-` | `Void` |
| `PUT` | `/api/alunos/{alunoId}/pesos/{registroId}` | Autenticado; regra final no service | `AtualizarPesoRequest` | `EvolucaoPesoResponse` |
| `DELETE` | `/api/alunos/{id}` | Autenticado; regra final no service | `-` | `Void` |
| `GET` | `/api/alunos/{id}` | Autenticado; regra final no service | `-` | `Optional<Aluno>` |
| `PATCH` | `/api/alunos/{id}` | Autenticado; regra final no service | `Aluno` | `Aluno` |

### Professores

| Método | URL | Perfil permitido | Entrada | Saída |
|---|---|---|---|---|
| `GET` | `/professores` | Autenticado; regra final no service | `-` | `List<Professor>` |
| `POST` | `/professores` | Autenticado; regra final no service | `Professor` | `Professor` |
| `DELETE` | `/professores/{id}` | Autenticado; regra final no service | `-` | `Void` |
| `GET` | `/professores/{id}` | Autenticado; regra final no service | `-` | `Optional<Professor>` |
| `PATCH` | `/professores/{id}` | Autenticado; regra final no service | `Professor` | `Professor` |

### Agenda

| Método | URL | Perfil permitido | Entrada | Saída |
|---|---|---|---|---|
| `GET` | `/api/agenda-professor/aulas/personais/{aulaId}` | Autenticado; regra final no service | `-` | `DetalheAulaPersonalProfessorResponse` |
| `GET` | `/api/agenda-professor/aulas/turmas/{aulaId}` | Autenticado; regra final no service | `-` | `DetalheAulaTurmaProfessorResponse` |
| `GET` | `/api/agenda-professor/{professorId}/dia` | Autenticado; regra final no service | `-` | `ResumoAgendaProfessorResponse` |
| `GET` | `/api/agenda-professor/{professorId}/semana` | Autenticado; regra final no service | `-` | `AgendaSemanalProfessorResponse` |
| `POST` | `/api/agenda/personais` | ADMIN/PROFESSOR conforme service | `AgendaPersonalRequest` | `AgendaPersonal` |
| `PUT` | `/api/agenda/personais/{id}` | ADMIN/PROFESSOR conforme service | `AgendaPersonalRequest` | `AgendaPersonal` |
| `PATCH` | `/api/agenda/personais/{id}/status` | ADMIN/PROFESSOR conforme service | `-` | `AgendaPersonal` |
| `GET` | `/api/agenda/professores/{professorId}` | Autenticado; regra final no service | `-` | `List<ItemAgendaProfessorResponse>` |
| `POST` | `/api/agenda/turmas` | ADMIN/PROFESSOR conforme service | `HorarioTurmaRequest` | `HorarioTurma` |
| `PUT` | `/api/agenda/turmas/{id}` | ADMIN/PROFESSOR conforme service | `HorarioTurmaRequest` | `HorarioTurma` |
| `PATCH` | `/api/agenda/turmas/{id}/status` | ADMIN/PROFESSOR conforme service | `-` | `HorarioTurma` |
| `GET` | `/api/alunos/me/reservas` | ALUNO | `-` | `List<ReservaAulaResponse>` |
| `POST` | `/api/aulas/gerar` | ADMIN/PROFESSOR conforme service | `-` | `Void` |
| `POST` | `/api/aulas/gerar-periodo` | ADMIN/PROFESSOR conforme service | `-` | `Void` |
| `POST` | `/api/aulas/personais/agendas/{agendaPersonalId}` | ADMIN/PROFESSOR conforme service | `-` | `AulaPersonal` |
| `PATCH` | `/api/aulas/personais/{id}/cancelar` | ADMIN/PROFESSOR conforme service | `CancelarAulaRequest` | `AulaPersonal` |
| `PATCH` | `/api/aulas/personais/{id}/remarcar` | ADMIN/PROFESSOR conforme service | `RemarcarAulaRequest` | `AulaPersonal` |
| `GET` | `/api/aulas/professores/{professorId}` | Autenticado; regra final no service | `-` | `List<ItemAulaProfessorResponse>` |
| `GET` | `/api/aulas/professores/{professorId}/semana` | Autenticado; regra final no service | `-` | `List<ItemAulaProfessorResponse>` |
| `POST` | `/api/aulas/turmas/horarios/{horarioTurmaId}` | ADMIN/PROFESSOR conforme service | `-` | `AulaTurma` |
| `GET` | `/api/aulas/turmas/{aulaId}/ocupacao` | Autenticado; regra final no service | `-` | `OcupacaoAulaResponse` |
| `GET` | `/api/aulas/turmas/{aulaId}/reservas` | Autenticado; regra final no service | `-` | `List<ReservaAulaResponse>` |
| `POST` | `/api/aulas/turmas/{aulaId}/reservas` | ADMIN/PROFESSOR conforme service | `-` | `ReservaAula` |
| `DELETE` | `/api/aulas/turmas/{aulaId}/reservas/minha` | ADMIN/PROFESSOR conforme service | `-` | `ReservaAula` |
| `PATCH` | `/api/aulas/turmas/{id}/cancelar` | ADMIN/PROFESSOR conforme service | `CancelarAulaRequest` | `AulaTurma` |
| `PATCH` | `/api/aulas/turmas/{id}/remarcar` | ADMIN/PROFESSOR conforme service | `RemarcarAulaRequest` | `AulaTurma` |
| `GET` | `/api/planos` | Autenticado; regra final no service | `-` | `List<Plano>` |
| `POST` | `/api/planos` | ADMIN/PROFESSOR conforme service | `PlanoRequest` | `Plano` |
| `PUT` | `/api/planos/{id}` | ADMIN/PROFESSOR conforme service | `PlanoRequest` | `Plano` |
| `PATCH` | `/api/planos/{id}/status` | ADMIN/PROFESSOR conforme service | `-` | `Plano` |
| `GET` | `/aulas` | Autenticado; regra final no service | `-` | `List<Aula>` |
| `POST` | `/aulas` | ADMIN/PROFESSOR conforme service | `Aula` | `Aula` |
| `DELETE` | `/aulas/{id}` | ADMIN/PROFESSOR conforme service | `-` | `Void` |
| `GET` | `/aulas/{id}` | Autenticado; regra final no service | `-` | `Optional<Aula>` |
| `PATCH` | `/aulas/{id}` | ADMIN/PROFESSOR conforme service | `Aula` | `Aula` |
| `GET` | `/aulas_particulares` | Autenticado; regra final no service | `-` | `List<AulaParticular>` |
| `POST` | `/aulas_particulares` | ADMIN/PROFESSOR conforme service | `AulaParticular` | `AulaParticular` |
| `DELETE` | `/aulas_particulares/{id}` | ADMIN/PROFESSOR conforme service | `-` | `Void` |
| `GET` | `/aulas_particulares/{id}` | Autenticado; regra final no service | `-` | `Optional<AulaParticular>` |
| `PATCH` | `/aulas_particulares/{id}` | ADMIN/PROFESSOR conforme service | `AulaParticular` | `AulaParticular` |
| `GET` | `/horarios` | Autenticado; regra final no service | `-` | `List<Horario>` |
| `POST` | `/horarios` | Autenticado; regra final no service | `Horario` | `Horario` |
| `DELETE` | `/horarios/{id}` | Autenticado; regra final no service | `-` | `Void` |
| `GET` | `/horarios/{id}` | Autenticado; regra final no service | `-` | `Optional<Horario>` |
| `PATCH` | `/horarios/{id}` | Autenticado; regra final no service | `Horario` | `Horario` |
| `GET` | `/turmas` | Autenticado; regra final no service | `-` | `List<Turma>` |
| `POST` | `/turmas` | Autenticado; regra final no service | `Turma` | `Turma` |
| `DELETE` | `/turmas/{id}` | Autenticado; regra final no service | `-` | `Void` |
| `GET` | `/turmas/{id}` | Autenticado; regra final no service | `-` | `Optional<Turma>` |
| `PATCH` | `/turmas/{id}` | Autenticado; regra final no service | `Turma` | `Turma` |

### Check-in e Presenças

| Método | URL | Perfil permitido | Entrada | Saída |
|---|---|---|---|---|
| `GET` | `/api/academias/{academiaId}/check-in-qr` | Autenticado; regra final no service | `-` | `QrAcademiaResponse` |
| `POST` | `/api/academias/{academiaId}/check-in-qr/regenerar` | Autenticado; regra final no service | `-` | `QrAcademiaResponse` |
| `GET` | `/api/alunos/me/check-ins` | ALUNO | `-` | `HistoricoCheckInAlunoResponse` |
| `GET` | `/api/alunos/{alunoId}/check-ins` | Autenticado; regra final no service | `-` | `HistoricoCheckInAlunoResponse` |
| `GET` | `/api/aulas/personais/{aulaId}/presenca` | Autenticado; regra final no service | `-` | `PresencaAlunoResponse` |
| `PUT` | `/api/aulas/personais/{aulaId}/presenca` | ADMIN/PROFESSOR conforme service | `RegistrarPresencaRequest` | `PresencaAlunoResponse` |
| `GET` | `/api/aulas/personais/{aulaId}/sessao-check-in` | Autenticado; regra final no service | `-` | `SessaoCheckInResponse` |
| `POST` | `/api/aulas/personais/{aulaId}/sessao-check-in` | ADMIN/PROFESSOR conforme service | `-` | `SessaoCheckInResponse` |
| `GET` | `/api/aulas/turmas/{aulaId}/presencas` | Autenticado; regra final no service | `-` | `ListaPresencasAulaResponse` |
| `PUT` | `/api/aulas/turmas/{aulaId}/presencas/{alunoId}` | ADMIN/PROFESSOR conforme service | `RegistrarPresencaRequest` | `PresencaAlunoResponse` |
| `GET` | `/api/aulas/turmas/{aulaId}/sessao-check-in` | Autenticado; regra final no service | `-` | `SessaoCheckInResponse` |
| `POST` | `/api/aulas/turmas/{aulaId}/sessao-check-in` | ADMIN/PROFESSOR conforme service | `-` | `SessaoCheckInResponse` |
| `POST` | `/api/check-in/qr/{codigoPublico}/confirmar` | Autenticado; regra final no service | `ConfirmarCheckInQrRequest` | `ConfirmarCheckInQrResponse` |
| `POST` | `/api/check-in/qr/{codigoPublico}/opcoes` | Autenticado; regra final no service | `-` | `OpcoesCheckInQrResponse` |
| `GET` | `/api/checkins` | Autenticado; regra final no service | `-` | `List<CheckIn>` |
| `POST` | `/api/checkins` | Autenticado; regra final no service | `CheckIn` | `CheckIn` |
| `GET` | `/api/checkins/aluno/{alunoId}` | Autenticado; regra final no service | `-` | `List<CheckIn>` |
| `POST` | `/api/checkins/aluno/{alunoId}` | Autenticado; regra final no service | `-` | `CheckIn` |
| `POST` | `/api/checkins/registrar` | Autenticado; regra final no service | `RegistrarCheckInRequest` | `CheckIn` |
| `DELETE` | `/api/checkins/{id}` | Autenticado; regra final no service | `-` | `Void` |
| `GET` | `/api/checkins/{id}` | Autenticado; regra final no service | `-` | `Optional<CheckIn>` |
| `PATCH` | `/api/checkins/{id}` | Autenticado; regra final no service | `CheckIn` | `CheckIn` |
| `PATCH` | `/api/sessoes-check-in/{sessaoId}/encerrar` | Autenticado; regra final no service | `-` | `SessaoCheckInResponse` |

### Financeiro

| Método | URL | Perfil permitido | Entrada | Saída |
|---|---|---|---|---|
| `GET` | `/api/faturas` | Autenticado; regra final no service | `-` | `List<Fatura>` |
| `GET` | `/api/faturas/aluno/{alunoId}` | Autenticado; regra final no service | `-` | `List<Fatura>` |
| `POST` | `/api/faturas/aluno/{alunoId}/mensal/{mesReferencia}` | Autenticado; regra final no service | `-` | `Fatura` |
| `POST` | `/api/faturas/mensais/{mesReferencia}` | Autenticado; regra final no service | `-` | `Void` |
| `POST` | `/api/faturas/{faturaId}/pix` | Autenticado; regra final no service | `-` | `CobrancaPixResponse` |
| `PATCH` | `/api/faturas/{id}/cancelar` | Autenticado; regra final no service | `-` | `Fatura` |
| `GET` | `/financeiros` | Autenticado; regra final no service | `-` | `List<Financeiro>` |
| `POST` | `/financeiros` | Autenticado; regra final no service | `Financeiro` | `Financeiro` |
| `GET` | `/financeiros/aluno/{alunoId}` | Autenticado; regra final no service | `-` | `List<Financeiro>` |
| `GET` | `/financeiros/modalidade/{modalidadeId}` | Autenticado; regra final no service | `-` | `List<Financeiro>` |
| `GET` | `/financeiros/pagos` | Autenticado; regra final no service | `-` | `List<Financeiro>` |
| `GET` | `/financeiros/tipo/{tipoModalidade}` | Autenticado; regra final no service | `-` | `List<Financeiro>` |
| `GET` | `/financeiros/total-recebido` | Autenticado; regra final no service | `-` | `Double` |
| `GET` | `/financeiros/total-recebido/tipo/{tipoModalidade}` | Autenticado; regra final no service | `-` | `Double` |
| `GET` | `/financeiros/vence-hoje` | Autenticado; regra final no service | `-` | `List<Financeiro>` |
| `GET` | `/financeiros/vencidos` | Autenticado; regra final no service | `-` | `List<Financeiro>` |
| `DELETE` | `/financeiros/{id}` | Autenticado; regra final no service | `-` | `Void` |
| `GET` | `/financeiros/{id}` | Autenticado; regra final no service | `-` | `Optional<Financeiro>` |
| `PATCH` | `/financeiros/{id}` | Autenticado; regra final no service | `Financeiro` | `Financeiro` |
| `PATCH` | `/financeiros/{id}/pagar` | Autenticado; regra final no service | `-` | `Financeiro` |

### Pagamentos

| Método | URL | Perfil permitido | Entrada | Saída |
|---|---|---|---|---|
| `DELETE` | `/api/alunos/me/preferencia-pagamento` | ALUNO | `-` | `Void` |
| `GET` | `/api/alunos/me/preferencia-pagamento` | ALUNO | `-` | `PreferenciaPagamentoResponse` |
| `PUT` | `/api/alunos/me/preferencia-pagamento` | ALUNO | `PreferenciaPagamentoRequest` | `PreferenciaPagamentoResponse` |
| `GET` | `/api/alunos/{alunoId}/preferencia-pagamento` | Autenticado; regra final no service | `-` | `PreferenciaPagamentoResponse` |
| `POST` | `/api/faturas/{faturaId}/pagamento-manual` | Autenticado; regra final no service | `PagamentoManualRequest` | `Pagamento` |
| `GET` | `/api/faturas/{faturaId}/pagamentos` | Autenticado; regra final no service | `-` | `List<Pagamento>` |
| `POST` | `/api/pagamentos/{pagamentoId}/conciliar` | Autenticado; regra final no service | `-` | `ConciliacaoPagamentoResponse` |
| `POST` | `/api/webhooks/pagamentos/asaas` | Público | `String` | `Void` |
| `POST` | `/api/webhooks/pagamentos/mock` | Público/integração validada por token | `-` | `Void` |

### Graduações e Eventos

| Método | URL | Perfil permitido | Entrada | Saída |
|---|---|---|---|---|
| `GET` | `/api/alunos/me/avaliacoes-graduacao` | ALUNO | `-` | `List<AvaliacaoGraduacaoResponse>` |
| `GET` | `/api/alunos/me/avaliacoes-graduacao/{avaliacaoId}` | ALUNO | `-` | `AvaliacaoGraduacaoResponse` |
| `GET` | `/api/alunos/me/convites-graduacao` | ALUNO | `-` | `List<ConviteGraduacaoResponse>` |
| `POST` | `/api/alunos/me/convites-graduacao/{conviteId}/aceitar` | ALUNO | `-` | `ConviteGraduacaoResponse` |
| `POST` | `/api/alunos/me/convites-graduacao/{conviteId}/recusar` | ALUNO | `-` | `ConviteGraduacaoResponse` |
| `GET` | `/api/alunos/me/eventos-graduacao` | ALUNO | `-` | `List<EventoGraduacaoAlunoResponse>` |
| `GET` | `/api/alunos/me/eventos-graduacao/{eventoId}` | ALUNO | `-` | `EventoGraduacaoAlunoResponse` |
| `POST` | `/api/alunos/me/eventos-graduacao/{eventoId}/inscrever` | ALUNO | `-` | `InscricaoGraduacaoResponse` |
| `GET` | `/api/alunos/me/graduacoes` | ALUNO | `-` | `GraduacaoAlunoResumoResponse` |
| `GET` | `/api/alunos/me/graduacoes/historico` | ALUNO | `-` | `List<HistoricoGraduacaoResponse>` |
| `GET` | `/api/alunos/me/inscricoes-graduacao` | ALUNO | `-` | `List<InscricaoGraduacaoResponse>` |
| `PATCH` | `/api/alunos/me/inscricoes-graduacao/{inscricaoId}/cancelar` | ALUNO | `-` | `InscricaoGraduacaoResponse` |
| `POST` | `/api/alunos/me/inscricoes-graduacao/{inscricaoId}/pix` | ALUNO | `-` | `CriarPixInscricaoGraduacaoResponse` |
| `POST` | `/api/alunos/{alunoId}/graduacao` | ADMIN/PROFESSOR conforme service | `AlterarGraduacaoAlunoRequest` | `HistoricoGraduacaoResponse` |
| `POST` | `/api/alunos/{alunoId}/graduacao/promover` | ADMIN/PROFESSOR conforme service | `PromoverGraduacaoAlunoRequest` | `HistoricoGraduacaoResponse` |
| `GET` | `/api/alunos/{alunoId}/graduacoes/historico` | Autenticado; regra final no service | `-` | `List<HistoricoGraduacaoResponse>` |
| `GET` | `/api/alunos/{alunoId}/graduacoes/historico-completo` | Autenticado; regra final no service | `-` | `List<HistoricoGraduacaoCompletoResponse>` |
| `GET` | `/api/alunos/{alunoId}/graduacoes/historico-completo.csv` | Autenticado; regra final no service | `-` | `byte[]` |
| `GET` | `/api/eventos-graduacao` | Autenticado; regra final no service | `-` | `List<EventoGraduacaoResponse>` |
| `POST` | `/api/eventos-graduacao` | ADMIN/PROFESSOR conforme service | `EventoGraduacaoRequest` | `EventoGraduacaoResponse` |
| `POST` | `/api/eventos-graduacao/{eventoId}/concluir` | ADMIN/PROFESSOR conforme service | `ConcluirEventoGraduacaoRequest` | `PainelEventoGraduacaoResponse` |
| `PUT` | `/api/eventos-graduacao/{eventoId}/inscricoes/{inscricaoId}/avaliacao` | ADMIN/PROFESSOR conforme service | `RegistrarAvaliacaoGraduacaoRequest` | `AvaliacaoGraduacaoResponse` |
| `PUT` | `/api/eventos-graduacao/{eventoId}/inscricoes/{inscricaoId}/presenca` | ADMIN/PROFESSOR conforme service | `RegistrarPresencaGraduacaoRequest` | `AvaliacaoGraduacaoResponse` |
| `POST` | `/api/eventos-graduacao/{eventoId}/inscricoes/{inscricaoId}/promover` | ADMIN/PROFESSOR conforme service | `PromoverAlunoEventoRequest` | `AvaliacaoGraduacaoResponse` |
| `GET` | `/api/eventos-graduacao/{eventoId}/painel` | Autenticado; regra final no service | `-` | `PainelEventoGraduacaoResponse` |
| `GET` | `/api/eventos-graduacao/{id}` | Autenticado; regra final no service | `-` | `EventoGraduacaoResponse` |
| `PUT` | `/api/eventos-graduacao/{id}` | ADMIN/PROFESSOR conforme service | `EventoGraduacaoRequest` | `EventoGraduacaoResponse` |
| `PATCH` | `/api/eventos-graduacao/{id}/cancelar` | ADMIN/PROFESSOR conforme service | `-` | `EventoGraduacaoResponse` |
| `GET` | `/api/eventos-graduacao/{id}/convites` | Autenticado; regra final no service | `-` | `List<ConviteGraduacaoResponse>` |
| `POST` | `/api/eventos-graduacao/{id}/convites` | ADMIN/PROFESSOR conforme service | `ConviteGraduacaoRequest` | `ConviteGraduacaoResponse` |
| `GET` | `/api/eventos-graduacao/{id}/inscricoes` | Autenticado; regra final no service | `-` | `List<InscricaoGraduacaoResponse>` |
| `POST` | `/api/eventos-graduacao/{id}/prajieds` | ADMIN/PROFESSOR conforme service | `EventoGraduacaoPrajiedRequest` | `EventoGraduacaoPrajiedResponse` |
| `DELETE` | `/api/eventos-graduacao/{id}/prajieds/{itemId}` | ADMIN/PROFESSOR conforme service | `-` | `Void` |
| `PUT` | `/api/eventos-graduacao/{id}/prajieds/{itemId}` | ADMIN/PROFESSOR conforme service | `EventoGraduacaoPrajiedRequest` | `EventoGraduacaoPrajiedResponse` |
| `POST` | `/api/eventos-graduacao/{id}/publicar` | ADMIN/PROFESSOR conforme service | `PublicarEventoGraduacaoRequest` | `EventoGraduacaoResponse` |
| `GET` | `/api/graduacoes` | Autenticado; regra final no service | `-` | `List<GraduacaoResponse>` |
| `POST` | `/api/graduacoes` | Autenticado; regra final no service | `GraduacaoRequest` | `GraduacaoResponse` |
| `GET` | `/api/graduacoes/elegiveis` | Autenticado; regra final no service | `-` | `List<AlunoElegivelGraduacaoResponse>` |
| `PUT` | `/api/graduacoes/reordenar` | Autenticado; regra final no service | `ReordenarGraduacoesRequest` | `List<GraduacaoResponse>` |
| `GET` | `/api/graduacoes/tipo/{tipoGraduacao}` | Autenticado; regra final no service | `-` | `List<GraduacaoResponse>` |
| `DELETE` | `/api/graduacoes/{id}` | Autenticado; regra final no service | `-` | `Void` |
| `GET` | `/api/graduacoes/{id}` | Autenticado; regra final no service | `-` | `GraduacaoResponse` |
| `PUT` | `/api/graduacoes/{id}` | Autenticado; regra final no service | `GraduacaoRequest` | `GraduacaoResponse` |
| `PATCH` | `/api/graduacoes/{id}/status` | Autenticado; regra final no service | `-` | `GraduacaoResponse` |

### Certificados

| Método | URL | Perfil permitido | Entrada | Saída |
|---|---|---|---|---|
| `GET` | `/api/alunos/me/certificados` | ALUNO | `-` | `List<CertificadoGraduacaoResponse>` |
| `GET` | `/api/certificados.csv` | Autenticado; regra final no service | `-` | `byte[]` |
| `GET` | `/api/certificados/validar/{codigo}` | Público | `-` | `CertificadoValidacaoResponse` |
| `GET` | `/api/certificados/{id}` | Autenticado; regra final no service | `-` | `CertificadoGraduacaoResponse` |
| `PATCH` | `/api/certificados/{id}/cancelar` | ADMIN/PROFESSOR conforme service | `CancelarCertificadoRequest` | `CertificadoGraduacaoResponse` |

### Evolução Física

| Método | URL | Perfil permitido | Entrada | Saída |
|---|---|---|---|---|
| `GET` | `/Evolucao` | Autenticado; regra final no service | `-` | `List<Evolucao>` |
| `POST` | `/Evolucao` | Autenticado; regra final no service | `Evolucao` | `Evolucao` |
| `GET` | `/Evolucao/aluno/{alunoId}` | Autenticado; regra final no service | `-` | `List<Evolucao>` |
| `GET` | `/Evolucao/aluno/{alunoId}/linha-do-tempo` | Autenticado; regra final no service | `-` | `List<Evolucao>` |
| `DELETE` | `/Evolucao/{id}` | Autenticado; regra final no service | `-` | `Void` |
| `GET` | `/Evolucao/{id}` | Autenticado; regra final no service | `-` | `Optional<Evolucao>` |
| `PATCH` | `/Evolucao/{id}` | Autenticado; regra final no service | `Evolucao` | `Evolucao` |
| `GET` | `/api/alunos/me/avaliacoes-corporais` | ALUNO | `-` | `List<AvaliacaoCorporalResponse>` |
| `GET` | `/api/alunos/me/avaliacoes-corporais/comparativo` | ALUNO | `-` | `ComparativoAvaliacoesResponse` |
| `GET` | `/api/alunos/me/avaliacoes-corporais/resumo` | ALUNO | `-` | `ResumoAvaliacaoCorporalResponse` |
| `GET` | `/api/alunos/me/avaliacoes-corporais/{avaliacaoId}` | ALUNO | `-` | `AvaliacaoCorporalResponse` |
| `GET` | `/api/alunos/{alunoId}/avaliacoes-corporais` | Autenticado; regra final no service | `-` | `List<AvaliacaoCorporalResponse>` |
| `POST` | `/api/alunos/{alunoId}/avaliacoes-corporais` | Autenticado; regra final no service | `CriarAvaliacaoCorporalRequest` | `AvaliacaoCorporalResponse` |
| `GET` | `/api/alunos/{alunoId}/avaliacoes-corporais/comparativo` | Autenticado; regra final no service | `-` | `ComparativoAvaliacoesResponse` |
| `GET` | `/api/alunos/{alunoId}/avaliacoes-corporais/resumo` | Autenticado; regra final no service | `-` | `ResumoAvaliacaoCorporalResponse` |
| `DELETE` | `/api/alunos/{alunoId}/avaliacoes-corporais/{avaliacaoId}` | Autenticado; regra final no service | `-` | `Void` |
| `GET` | `/api/alunos/{alunoId}/avaliacoes-corporais/{avaliacaoId}` | Autenticado; regra final no service | `-` | `AvaliacaoCorporalResponse` |
| `PUT` | `/api/alunos/{alunoId}/avaliacoes-corporais/{avaliacaoId}` | Autenticado; regra final no service | `AtualizarAvaliacaoCorporalRequest` | `AvaliacaoCorporalResponse` |

### Relatórios

| Método | URL | Perfil permitido | Entrada | Saída |
|---|---|---|---|---|
| `GET` | `/Relatorio` | Autenticado; regra final no service | `-` | `List<Relatorio>` |
| `POST` | `/Relatorio` | ADMIN/PROFESSOR conforme service | `Relatorio` | `Relatorio` |
| `POST` | `/Relatorio/evolucao/aluno/{alunoId}` | ADMIN/PROFESSOR conforme service | `-` | `Relatorio` |
| `POST` | `/Relatorio/financeiro` | ADMIN/PROFESSOR conforme service | `-` | `Relatorio` |
| `POST` | `/Relatorio/financeiro/tipo/{tipoModalidade}` | ADMIN/PROFESSOR conforme service | `-` | `Relatorio` |
| `GET` | `/Relatorio/tipo/{tipoRelatorio}` | Autenticado; regra final no service | `-` | `List<Relatorio>` |
| `DELETE` | `/Relatorio/{id}` | ADMIN/PROFESSOR conforme service | `-` | `Void` |
| `GET` | `/Relatorio/{id}` | Autenticado; regra final no service | `-` | `Optional<Relatorio>` |
| `PATCH` | `/Relatorio/{id}` | ADMIN/PROFESSOR conforme service | `Relatorio` | `Relatorio` |
| `GET` | `/api/eventos-graduacao/{eventoId}/relatorio` | Autenticado; regra final no service | `-` | `RelatorioEventoGraduacaoResponse` |
| `GET` | `/api/eventos-graduacao/{eventoId}/relatorio/participantes` | Autenticado; regra final no service | `-` | `-` |
| `GET` | `/api/eventos-graduacao/{eventoId}/relatorio/participantes.csv` | Autenticado; regra final no service | `-` | `-` |
| `GET` | `/api/relatorios/financeiro/anual` | Autenticado; regra final no service | `-` | `-` |
| `GET` | `/api/relatorios/financeiro/anual.csv` | Autenticado; regra final no service | `-` | `byte[]` |
| `GET` | `/api/relatorios/financeiro/anual/resumo` | Autenticado; regra final no service | `-` | `ResumoFinanceiroAnualResponse` |
| `GET` | `/api/relatorios/financeiro/contabil` | Autenticado; regra final no service | `-` | `-` |
| `GET` | `/api/relatorios/financeiro/contabil.csv` | Autenticado; regra final no service | `-` | `-` |
| `GET` | `/api/relatorios/financeiro/contabil/alunos` | Autenticado; regra final no service | `-` | `List<ResumoRecebimentosAlunoResponse>` |
| `GET` | `/api/relatorios/financeiro/contabil/alunos.csv` | Autenticado; regra final no service | `-` | `byte[]` |
| `GET` | `/api/relatorios/financeiro/contabil/competencias` | Autenticado; regra final no service | `-` | `List<ResumoCompetenciaContabilResponse>` |
| `GET` | `/api/relatorios/financeiro/inadimplencia` | Autenticado; regra final no service | `-` | `List<AlunoInadimplenteResponse>` |
| `GET` | `/api/relatorios/financeiro/mensal` | Autenticado; regra final no service | `-` | `-` |
| `GET` | `/api/relatorios/financeiro/mensal.csv` | Autenticado; regra final no service | `-` | `-` |
| `GET` | `/api/relatorios/financeiro/mensal/resumo` | Autenticado; regra final no service | `-` | `ResumoFinanceiroMensalResponse` |
| `GET` | `/api/relatorios/graduacoes` | Autenticado; regra final no service | `-` | `RelatorioGraduacoesPeriodoResponse` |
| `GET` | `/api/relatorios/graduacoes/financeiro` | Autenticado; regra final no service | `-` | `RelatorioFinanceiroGraduacaoResponse` |

### Notificações

| Método | URL | Perfil permitido | Entrada | Saída |
|---|---|---|---|---|
| `GET` | `/api/alunos/me/notificacoes` | ALUNO | `-` | `List<NotificacaoResponse>` |
| `PATCH` | `/api/alunos/me/notificacoes/marcar-todas-lidas` | ALUNO | `-` | `Void` |
| `PATCH` | `/api/alunos/me/notificacoes/{id}/lida` | ALUNO | `-` | `NotificacaoResponse` |
| `POST` | `/api/notificacoes/financeiro/processar` | ADMIN/PROFESSOR conforme service | `-` | `ResultadoAutomacaoNotificacaoFinanceiraResponse` |
| `POST` | `/api/notificacoes/financeiro/processar-atrasos` | ADMIN/PROFESSOR conforme service | `-` | `ResultadoAutomacaoNotificacaoFinanceiraResponse` |
| `POST` | `/api/notificacoes/financeiro/processar-vencimentos` | ADMIN/PROFESSOR conforme service | `-` | `ResultadoAutomacaoNotificacaoFinanceiraResponse` |
| `GET` | `/api/notificacoes/financeiro/relatorio` | Autenticado; regra final no service | `-` | `-` |
| `GET` | `/notificacoes` | Autenticado; regra final no service | `-` | `List<Notificacao>` |
| `POST` | `/notificacoes` | Autenticado; regra final no service | `Notificacao` | `Notificacao` |
| `GET` | `/notificacoes/aluno/{alunoId}` | Autenticado; regra final no service | `-` | `List<Notificacao>` |
| `POST` | `/notificacoes/gerar-automaticas` | Autenticado; regra final no service | `-` | `Void` |
| `POST` | `/notificacoes/graduacao/gerar-avisos-prazo` | ADMIN/PROFESSOR conforme service | `-` | `Integer` |
| `POST` | `/notificacoes/graduacao/gerar-lembretes-evento` | ADMIN/PROFESSOR conforme service | `-` | `Integer` |
| `GET` | `/notificacoes/pendentes` | Autenticado; regra final no service | `-` | `List<Notificacao>` |
| `GET` | `/notificacoes/relatorio` | Autenticado; regra final no service | `-` | `-` |
| `DELETE` | `/notificacoes/{id}` | Autenticado; regra final no service | `-` | `Void` |
| `GET` | `/notificacoes/{id}` | Autenticado; regra final no service | `-` | `Optional<Notificacao>` |
| `PATCH` | `/notificacoes/{id}` | Autenticado; regra final no service | `Notificacao` | `Notificacao` |
| `PATCH` | `/notificacoes/{id}/enviar` | Autenticado; regra final no service | `-` | `Notificacao` |

### Dashboard

| Método | URL | Perfil permitido | Entrada | Saída |
|---|---|---|---|---|
| `GET` | `/api/alunos/me/dashboard` | ALUNO | `-` | `DashboardAlunoResponse` |
| `GET` | `/api/alunos/{alunoId}/dashboard` | Autenticado; regra final no service | `-` | `DashboardAlunoResponse` |
| `GET` | `/api/checkins/aluno/{alunoId}/dashboard` | Autenticado; regra final no service | `-` | `Map<String, Object>` |
| `GET` | `/api/graduacoes/dashboard-professor` | Autenticado; regra final no service | `-` | `DashboardProfessorGraduacaoResponse` |
| `GET` | `/dashboard-preferencias` | Autenticado; regra final no service | `-` | `List<DashboardPreferencia>` |
| `POST` | `/dashboard-preferencias` | Autenticado; regra final no service | `DashboardPreferencia` | `DashboardPreferencia` |
| `GET` | `/dashboard-preferencias/usuario/{usuarioId}` | Autenticado; regra final no service | `-` | `Optional<DashboardPreferencia>` |
| `PUT` | `/dashboard-preferencias/usuario/{usuarioId}` | Autenticado; regra final no service | `DashboardPreferencia` | `DashboardPreferencia` |
| `DELETE` | `/dashboard-preferencias/{id}` | Autenticado; regra final no service | `-` | `Void` |
| `GET` | `/dashboard-preferencias/{id}` | Autenticado; regra final no service | `-` | `Optional<DashboardPreferencia>` |
| `PATCH` | `/dashboard-preferencias/{id}` | Autenticado; regra final no service | `DashboardPreferencia` | `DashboardPreferencia` |

### Administração

| Método | URL | Perfil permitido | Entrada | Saída |
|---|---|---|---|---|
| `GET` | `/campeonatos` | Autenticado; regra final no service | `-` | `List<Campeonato>` |
| `POST` | `/campeonatos` | Autenticado; regra final no service | `Campeonato` | `Campeonato` |
| `DELETE` | `/campeonatos/{id}` | Autenticado; regra final no service | `-` | `Void` |
| `GET` | `/campeonatos/{id}` | Autenticado; regra final no service | `-` | `Optional<Campeonato>` |
| `PATCH` | `/campeonatos/{id}` | Autenticado; regra final no service | `Campeonato` | `Campeonato` |
| `GET` | `/modalidades` | Autenticado; regra final no service | `-` | `List<Modalidade>` |
| `POST` | `/modalidades` | Autenticado; regra final no service | `Modalidade` | `Modalidade` |
| `GET` | `/modalidades/tipo/{tipoModalidade}` | Autenticado; regra final no service | `-` | `List<Modalidade>` |
| `DELETE` | `/modalidades/{id}` | Autenticado; regra final no service | `-` | `Void` |
| `GET` | `/modalidades/{id}` | Autenticado; regra final no service | `-` | `Optional<Modalidade>` |
| `PATCH` | `/modalidades/{id}` | Autenticado; regra final no service | `Modalidade` | `Modalidade` |

