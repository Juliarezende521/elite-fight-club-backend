# Elite Fight Club Backend

Backend Spring Boot da plataforma de gestao Elite Fight Club.

O projeto esta em desenvolvimento e consolida o nucleo de uma plataforma para academias de luta, com controle de alunos, planos, financeiro, agenda, presencas, check-ins e graduacoes.

## Problema Resolvido

Academias de luta costumam controlar alunos, mensalidades, presencas, aulas e graduacoes em ferramentas separadas. Este backend centraliza esses fluxos em uma API organizada por camadas, com persistencia em banco, controle de permissao e preparacao para integracao de pagamentos via Pix.

## Tecnologias

- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA
- Spring Security
- Bean Validation
- Flyway
- Maven
- PostgreSQL em desenvolvimento/producao
- H2 em testes automatizados
- Firebase Admin para autenticacao
- Asaas Sandbox para Pix
- JUnit e Mockito

## Arquitetura

O backend segue arquitetura em camadas:

- `model`: entidades JPA e enums do dominio.
- `repository`: interfaces Spring Data JPA.
- `service`: regras de negocio, validacoes e integracoes.
- `controller`: endpoints REST.
- `dto`: objetos de entrada e resposta da API.
- `security`: autenticacao e permissoes.
- `payments`: abstracao de provedor de pagamento e implementacao Asaas.
- `db/migration`: migrations versionadas do banco.

O projeto Spring Boot fica na raiz deste repositorio.

## Modulos Implementados

- Autenticacao e permissoes por perfil.
- Isolamento por academia.
- Alunos, professores, modalidades e planos.
- Financeiro, faturas e pagamentos.
- Integracao Pix com Asaas Sandbox.
- Webhook idempotente e conciliacao de pagamentos.
- Agenda recorrente de turmas e personal.
- Geracao de aulas concretas.
- Reservas de aulas de turma.
- Agenda unificada do professor.
- Presenca e check-in.
- QR fixo por academia.
- Dashboard consolidado do aluno.
- Configuracao de Prajieds/graduacoes.
- Elegibilidade e historico de graduacao.
- Eventos de graduacao.
- Convites e inscricoes em eventos de graduacao.
- Pagamento Pix de inscricao de graduacao.

## Configuracao por Ambiente

Credenciais e segredos nao sao versionados. Use variaveis de ambiente ou um arquivo local ignorado pelo Git.

Existe um arquivo de exemplo seguro:

```text
.env.example
```

Variaveis principais:

```text
SPRING_PROFILES_ACTIVE=dev

DATABASE_URL=
DATABASE_USERNAME=
DATABASE_PASSWORD=

FIREBASE_CREDENTIALS_PATH=

PAYMENT_PROVIDER=mock
ASAAS_BASE_URL=https://api-sandbox.asaas.com/v3
ASAAS_API_KEY=
ASAAS_WEBHOOK_TOKEN=

APP_PUBLIC_URL=
CHECKIN_MINUTES_BEFORE=15
CHECKIN_MINUTES_AFTER=15
CHECKIN_FRONTEND_BASE_URL=http://localhost:3000
```

## Como Executar

Execute a aplicacao:

```bash
sh mvnw spring-boot:run
```

Por padrao, o perfil ativo e `dev`. Configure `DATABASE_URL`, `DATABASE_USERNAME` e `DATABASE_PASSWORD` para usar PostgreSQL local.

## Testes

Para rodar os testes:

```bash
sh mvnw test
```

Para validar o pacote completo:

```bash
sh mvnw clean package
```

## Asaas Sandbox

O projeto possui uma camada de pagamentos desacoplada por provedor. Em desenvolvimento, o provedor padrao pode ser `mock`. Para testar Pix com Asaas Sandbox:

```text
PAYMENT_PROVIDER=asaas
ASAAS_BASE_URL=https://api-sandbox.asaas.com/v3
ASAAS_API_KEY=
ASAAS_WEBHOOK_TOKEN=
```

Nunca versione a chave da API nem o token de webhook. A documentacao complementar de teste esta em:

```text
docs/TESTE_ASAAS_SANDBOX.md
```

## Status Atual

O backend ja possui o nucleo funcional da plataforma, com migrations, testes automatizados e integracoes preparadas para ambiente sandbox. O projeto ainda esta em desenvolvimento e os proximos passos incluem amadurecimento das regras operacionais, revisao de seguranca para producao e construcao do frontend.

## Roadmap

- Finalizar fluxos administrativos de estorno e cancelamento financeiro.
- Evoluir notificacoes operacionais.
- Criar frontend para professor, aluno e administrador.
- Adicionar observabilidade e auditoria.
- Preparar deploy em ambiente de homologacao.
