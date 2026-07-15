# Teste manual do Asaas Sandbox

Este guia prepara um teste ponta a ponta do Pix no Asaas Sandbox sem expor credenciais.

## 1. Conta Sandbox

Crie ou acesse uma conta em `https://sandbox.asaas.com/`. Use somente credenciais de Sandbox nesta branch.

## 2. Variáveis de ambiente

Configure:

```bash
export SPRING_PROFILES_ACTIVE=dev
export PAYMENT_PROVIDER=asaas
export ASAAS_BASE_URL=https://api-sandbox.asaas.com/v3
export ASAAS_API_KEY=
export ASAAS_WEBHOOK_TOKEN=
export APP_PUBLIC_URL=https://seu-endereco-publico
export FIREBASE_CREDENTIALS_PATH=/caminho/para/firebase-admin.json
```

Para voltar ao provedor isolado:

```bash
export PAYMENT_PROVIDER=mock
```

## 3. Iniciar o projeto

```bash
./mvnw spring-boot:run
```

Em produção, use `SPRING_PROFILES_ACTIVE=prod`; nesse perfil o Hibernate usa `validate` e o Flyway fica responsável pelo schema.

## 4. Token Firebase de teste

As APIs protegidas esperam:

```http
Authorization: Bearer TOKEN_FIREBASE
```

Gere o token no frontend ou por um fluxo de login Firebase de teste. Não use nem salve token em arquivos versionados.

## 5. Cadastrar aluno

Exemplo:

```bash
curl -X POST http://localhost:8080/api/alunos \
  -H "Authorization: Bearer TOKEN_FIREBASE" \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Aluno Sandbox",
    "email": "aluno.sandbox@example.com",
    "senhaInicial": "SenhaForte123!",
    "tipoAluno": "TURMA",
    "valorMensalidade": 180.00,
    "diaVencimento": 15,
    "idade": 20,
    "peso": 70,
    "altura": 1.75
  }'
```

O cadastro cria usuário no Firebase, aluno no banco e fatura inicial. A senha fica somente no Firebase; o PostgreSQL não armazena credencial de acesso.

## 6. Identificar a fatura

```bash
curl http://localhost:8080/api/faturas \
  -H "Authorization: Bearer TOKEN_FIREBASE"
```

Confira `id`, `valor`, `status`, `mesReferencia` e `dataVencimento`.

## 7. Gerar Pix

```bash
curl -X POST http://localhost:8080/api/faturas/ID_DA_FATURA/pix \
  -H "Authorization: Bearer TOKEN_FIREBASE"
```

A resposta contém `pagamentoId`, `idPagamentoExterno`, `pixCopiaCola`, `qrCodeBase64` e `dataExpiracao`. Não registre o Pix copia e cola completo em logs.

Se já existir Pix pendente válido para a mesma fatura e provedor, o endpoint retorna o pagamento existente.

## 8. Conferir no Asaas

No painel Sandbox, procure a cobrança pelo identificador externo retornado ou pelo cliente criado para o aluno. O cliente externo é salvo no campo `asaas_customer_id` do aluno e reutilizado nas próximas cobranças.

## 9. Configurar webhook

URL final:

```text
${APP_PUBLIC_URL}/api/webhooks/pagamentos/asaas
```

O Asaas não acessa `localhost`. Em desenvolvimento, use um túnel HTTPS confiável ou um servidor público de teste. Configure no Asaas o mesmo token de `ASAAS_WEBHOOK_TOKEN`; ele será enviado no header `asaas-access-token`.

## 10. Confirmar ou simular pagamento

Pague ou simule a cobrança no Sandbox. O webhook deve atualizar:

- `pagamentos.status`
- `pagamentos.pago_em`
- `pagamentos.ultimo_evento_externo`
- `faturas.status`
- `eventos_pagamento_externo`

Eventos desconhecidos são registrados, mas não alteram o financeiro automaticamente.

## 11. Verificar banco

Exemplos:

```sql
select id, status, valor, mes_referencia from faturas order by id desc;
select id, status, provedor, id_pagamento_externo, conciliado_em from pagamentos order by id desc;
select provedor, evento_externo_id, tipo_evento, processado_em from eventos_pagamento_externo order by id desc;
```

## 12. Testar idempotência

Reenvie o mesmo payload com o mesmo `id` e `event`. O registro em `eventos_pagamento_externo` impede processamento duplicado.

Se o payload vier sem `id`, a chave idempotente usada é:

```text
pagamentoExternoId:evento:status
```

## 13. Executar conciliação

```bash
curl -X POST http://localhost:8080/api/pagamentos/ID_DO_PAGAMENTO/conciliar \
  -H "Authorization: Bearer TOKEN_FIREBASE"
```

Somente `ADMIN` pode conciliar. A conciliação consulta o Asaas pelo ID externo e retorna status anterior e atual.

## 14. Voltar para mock

```bash
export PAYMENT_PROVIDER=mock
./mvnw spring-boot:run
```

## 15. Migrations

- `V1__baseline_academias.sql`: cria tabela `academias` e índice de nome.
- `V2__seed_academia_padrao.sql`: insere `Elite Fight Club` se ainda não existir.
- `V3__academia_id_entidades_principais.sql`: adiciona `academia_id`, preenche dados legados com a academia padrão, cria índices e FKs.
- `V4__financeiro_pagamentos_asaas.sql`: adiciona campos Asaas/pagamento manual/conciliação, evento externo e índice único de fatura por academia, aluno e competência.

Não há drop automático de tabelas. Antes de rodar em banco legado:

```bash
pg_dump -Fc -f backup_elite.dump NOME_DO_BANCO
```

Confira histórico:

```sql
select installed_rank, version, description, success, installed_on
from flyway_schema_history
order by installed_rank;
```

Se uma migration falhar, não apague dados manualmente sem backup. Corrija a causa, use `flyway repair` apenas quando entender o estado do banco, e rode novamente.

## 16. Erros comuns

- `ASAAS_API_KEY não configurada`: falta variável de ambiente.
- `Token de webhook Asaas inválido`: header `asaas-access-token` não bate com `ASAAS_WEBHOOK_TOKEN`.
- Pix não chega no webhook: `APP_PUBLIC_URL` não é público ou não usa HTTPS.
- Cobrança duplicada não aparece: o sistema reutiliza Pix pendente válido.
- Fatura não quita: confira se o webhook recebido foi `PAYMENT_RECEIVED`.
