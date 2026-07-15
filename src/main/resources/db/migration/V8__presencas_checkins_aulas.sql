CREATE TABLE IF NOT EXISTS presencas_aula (
    id BIGSERIAL PRIMARY KEY,
    academia_id BIGINT NOT NULL,
    aluno_id BIGINT NOT NULL,
    aula_turma_id BIGINT,
    aula_personal_id BIGINT,
    status VARCHAR(30) NOT NULL,
    status_anterior VARCHAR(30),
    registrado_em TIMESTAMP NOT NULL,
    registrado_por_id BIGINT,
    observacao VARCHAR(500),
    chave_presenca VARCHAR(80) NOT NULL,
    CONSTRAINT uk_presenca_aula_aluno UNIQUE (chave_presenca),
    CONSTRAINT chk_presenca_tipo_aula CHECK (
        (aula_turma_id IS NOT NULL AND aula_personal_id IS NULL)
        OR (aula_turma_id IS NULL AND aula_personal_id IS NOT NULL)
    ),
    CONSTRAINT fk_presenca_academia FOREIGN KEY (academia_id) REFERENCES academias(id),
    CONSTRAINT fk_presenca_aluno FOREIGN KEY (aluno_id) REFERENCES alunos(id),
    CONSTRAINT fk_presenca_aula_turma FOREIGN KEY (aula_turma_id) REFERENCES aulas_turma(id),
    CONSTRAINT fk_presenca_aula_personal FOREIGN KEY (aula_personal_id) REFERENCES aulas_personal(id),
    CONSTRAINT fk_presenca_registrado_por FOREIGN KEY (registrado_por_id) REFERENCES usuarios(id)
);

ALTER TABLE checkins ADD COLUMN IF NOT EXISTS aula_turma_id BIGINT;
ALTER TABLE checkins ADD COLUMN IF NOT EXISTS aula_personal_id BIGINT;
ALTER TABLE checkins ADD COLUMN IF NOT EXISTS confirmado_por_id BIGINT;
ALTER TABLE checkins ADD COLUMN IF NOT EXISTS ativo BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE checkins ADD COLUMN IF NOT EXISTS invalidado_em TIMESTAMP;
ALTER TABLE checkins ADD COLUMN IF NOT EXISTS observacao VARCHAR(500);
ALTER TABLE checkins ADD COLUMN IF NOT EXISTS chave_checkin_ativo VARCHAR(80);

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_checkin_aula_turma') THEN
        ALTER TABLE checkins ADD CONSTRAINT fk_checkin_aula_turma FOREIGN KEY (aula_turma_id) REFERENCES aulas_turma(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_checkin_aula_personal') THEN
        ALTER TABLE checkins ADD CONSTRAINT fk_checkin_aula_personal FOREIGN KEY (aula_personal_id) REFERENCES aulas_personal(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_checkin_confirmado_por') THEN
        ALTER TABLE checkins ADD CONSTRAINT fk_checkin_confirmado_por FOREIGN KEY (confirmado_por_id) REFERENCES usuarios(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'chk_checkin_tipo_aula') THEN
        ALTER TABLE checkins ADD CONSTRAINT chk_checkin_tipo_aula CHECK (
            (aula_turma_id IS NOT NULL AND aula_personal_id IS NULL)
            OR (aula_turma_id IS NULL AND aula_personal_id IS NOT NULL)
            OR (aula_turma_id IS NULL AND aula_personal_id IS NULL)
        );
    END IF;
END $$;

CREATE UNIQUE INDEX IF NOT EXISTS uk_checkin_ativo
    ON checkins(chave_checkin_ativo)
    WHERE chave_checkin_ativo IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_presenca_turma
    ON presencas_aula(academia_id, aula_turma_id);

CREATE INDEX IF NOT EXISTS idx_presenca_personal
    ON presencas_aula(academia_id, aula_personal_id);

CREATE INDEX IF NOT EXISTS idx_presenca_aluno
    ON presencas_aula(academia_id, aluno_id);

CREATE INDEX IF NOT EXISTS idx_checkin_aluno_data
    ON checkins(academia_id, aluno_id, data_hora);

CREATE INDEX IF NOT EXISTS idx_checkin_aula_turma
    ON checkins(academia_id, aula_turma_id);

CREATE INDEX IF NOT EXISTS idx_checkin_aula_personal
    ON checkins(academia_id, aula_personal_id);
