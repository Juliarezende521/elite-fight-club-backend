CREATE EXTENSION IF NOT EXISTS pgcrypto;

ALTER TABLE academias ADD COLUMN IF NOT EXISTS codigo_check_in_publico VARCHAR(80);

UPDATE academias
SET codigo_check_in_publico = encode(gen_random_bytes(24), 'hex')
WHERE codigo_check_in_publico IS NULL OR codigo_check_in_publico = '';

CREATE UNIQUE INDEX IF NOT EXISTS uk_academias_codigo_check_in_publico
    ON academias(codigo_check_in_publico)
    WHERE codigo_check_in_publico IS NOT NULL;

CREATE TABLE IF NOT EXISTS sessoes_check_in (
    id BIGSERIAL PRIMARY KEY,
    academia_id BIGINT NOT NULL,
    aula_turma_id BIGINT,
    aula_personal_id BIGINT,
    status VARCHAR(30) NOT NULL,
    aberta_em TIMESTAMP NOT NULL,
    expira_em TIMESTAMP NOT NULL,
    encerrada_em TIMESTAMP,
    aberta_por_id BIGINT NOT NULL,
    chave_sessao_ativa VARCHAR(80),
    CONSTRAINT chk_sessao_tipo_aula CHECK (
        (aula_turma_id IS NOT NULL AND aula_personal_id IS NULL)
        OR (aula_turma_id IS NULL AND aula_personal_id IS NOT NULL)
    ),
    CONSTRAINT fk_sessao_academia FOREIGN KEY (academia_id) REFERENCES academias(id),
    CONSTRAINT fk_sessao_aula_turma FOREIGN KEY (aula_turma_id) REFERENCES aulas_turma(id),
    CONSTRAINT fk_sessao_aula_personal FOREIGN KEY (aula_personal_id) REFERENCES aulas_personal(id),
    CONSTRAINT fk_sessao_aberta_por FOREIGN KEY (aberta_por_id) REFERENCES usuarios(id)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_sessao_ativa
    ON sessoes_check_in(chave_sessao_ativa)
    WHERE chave_sessao_ativa IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_sessao_academia_status
    ON sessoes_check_in(academia_id, status, expira_em);

CREATE INDEX IF NOT EXISTS idx_sessao_aula_turma
    ON sessoes_check_in(academia_id, aula_turma_id);

CREATE INDEX IF NOT EXISTS idx_sessao_aula_personal
    ON sessoes_check_in(academia_id, aula_personal_id);
