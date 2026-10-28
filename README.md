# Elite Fight Club — Backend

[![Backend CI](https://github.com/Juliarezende521/elite-fight-club-backend/actions/workflows/ci.yml/badge.svg)](https://github.com/Juliarezende521/elite-fight-club-backend/actions/workflows/ci.yml)

API REST do **Elite Fight Club**, um projeto pessoal de gestão para academias de artes marciais. O sistema centraliza alunos, aulas, presenças, mensalidades, evolução física e graduações em uma plataforma multiacademia.

> Projeto pessoal em desenvolvimento, independente dos trabalhos acadêmicos realizados em grupo.

## Status

O backend possui um núcleo funcional estável, com autenticação, autorização por perfil, isolamento por academia, migrations e testes automatizados. A integração com o frontend React está em andamento.

- Build e testes executados automaticamente pelo GitHub Actions.
- Pagamentos externos configurados somente para o ambiente sandbox.
- Credenciais e segredos não são versionados.
- Deploy público ainda não realizado.

## Problema abordado

Academias de luta frequentemente controlam alunos, mensalidades, aulas, presenças e graduações em ferramentas separadas. O Elite Fight Club explora uma solução única, organizada por módulos e preparada para diferentes perfis de acesso.

## Funcionalidades implementadas

- Autenticação com Firebase e permissões para administrador, professor e aluno.
- Isolamento de dados por academia.
- Gestão de alunos, professores, modalidades e planos.
- Agenda de turmas e aulas particulares.
- Reservas, presença e check-in por QR Code.
- Faturas, pagamentos, preferências de pagamento e conciliação.
- Integração Pix com Asaas Sandbox e webhook idempotente.
- Notificações financeiras automáticas.
- Dashboard do aluno.
- Evolução física, peso, metas e avaliação corporal.
- Graduações, eventos, avaliações, certificados e relatórios.
- Relatórios financeiros e contábeis.

## Tecnologias

- Java 21
- Spring Boot 4
- Spring Web MVC
- Spring Data JPA
- Spring Security
- Bean Validation
- Flyway
- PostgreSQL
- H2 para testes
- Firebase Admin
- Asaas Sandbox
- Maven
- JUnit e Mockito

## Arquitetura

O projeto utiliza uma arquitetura em camadas:

- `controller`: endpoints REST.
- `dto`: contratos de entrada e saída.
- `service`: regras de negócio e autorização.
- `repository`: persistência com Spring Data JPA.
- `model`: entidades e enums do domínio.
- `security`: autenticação e controle de acesso.
- `payments`: abstração dos provedores de pagamento.
- `db/migration`: evolução versionada do banco.

A documentação técnica complementar está disponível em:

- `FRONTEND_READY.md`
- `BACKEND_REVIEW_REPORT.md`
- `SECURITY_REVIEW.md`
- `docs/TESTE_ASAAS_SANDBOX.md`

## Configuração local

Pré-requisitos:

- Java 21
- PostgreSQL
- Maven Wrapper incluído no projeto

Crie suas variáveis a partir de `.env.example`. Os principais valores são:

```env
SPRING_PROFILES_ACTIVE=dev
DATABASE_URL=jdbc:postgresql://localhost:5432/elite
DATABASE_USERNAME=
DATABASE_PASSWORD=
FIREBASE_CREDENTIALS_PATH=
PAYMENT_PROVIDER=mock
ASAAS_API_KEY=
ASAAS_WEBHOOK_TOKEN=
APP_CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173
```

Nunca versione chaves do Firebase, tokens do Asaas ou senhas do banco.

Execute a aplicação:

```bash
./mvnw spring-boot:run
```

No Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

## Testes e qualidade

```bash
./mvnw clean verify
```

O workflow `Backend CI` repete essa validação automaticamente a cada push e pull request para a branch principal.

## Frontend

O frontend está sendo desenvolvido separadamente em:

[elite-fight-club-front](https://github.com/Juliarezende521/elite-fight-club-front)

## Processo de desenvolvimento

Ferramentas de inteligência artificial são utilizadas como apoio para pesquisa, revisão e implementação. As decisões de arquitetura, a validação das regras, os testes e a evolução do produto são acompanhados e revisados durante o desenvolvimento.

## Próximas etapas

- Concluir a integração dos módulos do frontend com a API.
- Ampliar testes integrados com PostgreSQL.
- Adicionar documentação OpenAPI.
- Refinar auditoria e observabilidade.
- Preparar um ambiente gratuito de demonstração, se houver uma opção adequada.
