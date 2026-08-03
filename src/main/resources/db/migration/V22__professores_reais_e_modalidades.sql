ALTER TABLE professores
    ADD COLUMN IF NOT EXISTS ativo BOOLEAN;

UPDATE professores
SET ativo = TRUE
WHERE ativo IS NULL;

ALTER TABLE professores
    ALTER COLUMN ativo SET NOT NULL;

CREATE TABLE IF NOT EXISTS professores_modalidades (
    id BIGSERIAL PRIMARY KEY,
    academia_id BIGINT NOT NULL,
    professor_id BIGINT NOT NULL,
    modalidade_id BIGINT NOT NULL,
    permite_turma BOOLEAN NOT NULL DEFAULT FALSE,
    permite_personal BOOLEAN NOT NULL DEFAULT FALSE,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_professores_modalidades_academia
        FOREIGN KEY (academia_id) REFERENCES academias(id),
    CONSTRAINT fk_professores_modalidades_professor
        FOREIGN KEY (professor_id) REFERENCES professores(id),
    CONSTRAINT fk_professores_modalidades_modalidade
        FOREIGN KEY (modalidade_id) REFERENCES modalidades(id),
    CONSTRAINT ck_professores_modalidades_permissao
        CHECK (permite_turma = TRUE OR permite_personal = TRUE),
    CONSTRAINT uk_professor_modalidade_academia
        UNIQUE (academia_id, professor_id, modalidade_id)
);

CREATE INDEX IF NOT EXISTS idx_professores_modalidades_academia_professor
    ON professores_modalidades (academia_id, professor_id);

CREATE INDEX IF NOT EXISTS idx_professores_modalidades_academia_modalidade
    ON professores_modalidades (academia_id, modalidade_id);

CREATE INDEX IF NOT EXISTS idx_professores_modalidades_turma
    ON professores_modalidades (academia_id, modalidade_id, permite_turma, ativo);

CREATE INDEX IF NOT EXISTS idx_professores_modalidades_personal
    ON professores_modalidades (academia_id, modalidade_id, permite_personal, ativo);

ALTER TABLE agendas_personal
    ADD COLUMN IF NOT EXISTS modalidade_id BIGINT;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_agendas_personal_modalidade'
    ) THEN
        ALTER TABLE agendas_personal
            ADD CONSTRAINT fk_agendas_personal_modalidade
            FOREIGN KEY (modalidade_id) REFERENCES modalidades(id);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_agendas_personal_modalidade
    ON agendas_personal (academia_id, modalidade_id);

ALTER TABLE aulas_personal
    ADD COLUMN IF NOT EXISTS modalidade_id BIGINT;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_aulas_personal_modalidade'
    ) THEN
        ALTER TABLE aulas_personal
            ADD CONSTRAINT fk_aulas_personal_modalidade
            FOREIGN KEY (modalidade_id) REFERENCES modalidades(id);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_aulas_personal_modalidade
    ON aulas_personal (academia_id, modalidade_id);
