# Changelog

Todas as mudancas relevantes do backend Elite Fight Club sao agrupadas por marco funcional.

## v0.1.0-backend - Primeira versao estavel do backend

Marco de prontidao para inicio do Frontend React.

### Plataforma e seguranca

- Arquitetura Spring Boot em camadas.
- Spring Security com autenticacao via Firebase token.
- Perfis `ADMIN`, `PROFESSOR` e `ALUNO`.
- Isolamento por academia nos principais fluxos.
- Tratamento global de excecoes.
- Flyway com migrations versionadas.

### Alunos, professores e operacao

- Cadastro e manutencao de alunos, professores, modalidades e planos.
- Planos por turma ou personal.
- Preferencia de pagamento do aluno.
- Dashboard consolidado do aluno.

### Agenda, reservas e check-in

- Horarios recorrentes de turma.
- Agenda recorrente de personal.
- Geracao de aulas concretas por dia ou periodo.
- Reservas de aula de turma.
- Presencas e check-ins.
- Check-in por QR fixo da academia.
- Agenda diaria e semanal do professor.

### Financeiro e pagamentos

- Faturas de mensalidade.
- Pagamentos manuais.
- Criacao de cobranca Pix.
- Abstracao de provedor de pagamento.
- Mock provider para desenvolvimento.
- Integracao Asaas Sandbox.
- Webhook Asaas com idempotencia.
- Conciliacao de pagamento.
- Relatorios financeiros mensal e anual.
- Relatorio contabil de apoio.

### Notificacoes

- Notificacoes internas persistidas.
- Notificacoes automaticas de vencimento e atraso financeiro.
- Notificacoes por confirmacao, processamento e estorno de pagamento.
- Relatorio operacional de notificacoes financeiras.
- Scheduler preparado e desabilitado por padrao.

### Graduacao

- Configuracao de Prajieds/graduacoes.
- Elegibilidade por check-ins e tempo minimo.
- Historico de graduacao.
- Eventos de graduacao.
- Convites e inscricoes.
- Pagamento Pix de inscricao.
- Presenca em evento.
- Avaliacao tecnica.
- Promocao manual.
- Certificados de graduacao.
- Validacao publica de certificado.
- Relatorios operacionais e financeiros de eventos.

### Evolucao fisica

- Historico de peso.
- Metas de peso.
- Avaliacao corporal e bioimpedancia.
- Resumos para dashboard.

### Testes e build

- Suite automatizada com JUnit e Mockito.
- Build Maven validado com `sh mvnw clean package`.

## Proxima etapa

- Desenvolvimento completo do Frontend React.
- Refinar experiencia por perfil: Admin, Professor e Aluno.
- Evoluir design de dashboards e fluxos operacionais.
