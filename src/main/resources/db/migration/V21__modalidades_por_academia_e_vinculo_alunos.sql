DO $$
DECLARE
    default_academia_id BIGINT;
BEGIN
    SELECT id INTO default_academia_id
    FROM academias
    WHERE LOWER(nome) = LOWER('Elite Fight Club')
    LIMIT 1;

    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'modalidades') THEN
        ALTER TABLE modalidades ADD COLUMN IF NOT EXISTS academia_id BIGINT;
        UPDATE modalidades SET academia_id = default_academia_id WHERE academia_id IS NULL;
        ALTER TABLE modalidades ALTER COLUMN academia_id SET NOT NULL;

        CREATE INDEX IF NOT EXISTS idx_modalidades_academia ON modalidades (academia_id);
        CREATE INDEX IF NOT EXISTS idx_modalidades_academia_ativa ON modalidades (academia_id, ativa);

        IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_modalidades_academia') THEN
            ALTER TABLE modalidades
                ADD CONSTRAINT fk_modalidades_academia
                FOREIGN KEY (academia_id) REFERENCES academias(id);
        END IF;
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'alunos')
        AND EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'modalidades') THEN
        CREATE TABLE IF NOT EXISTS alunos_modalidades (
            aluno_id BIGINT NOT NULL,
            modalidade_id BIGINT NOT NULL
        );

        DELETE FROM alunos_modalidades atual
        USING alunos_modalidades duplicada
        WHERE atual.ctid < duplicada.ctid
            AND atual.aluno_id = duplicada.aluno_id
            AND atual.modalidade_id = duplicada.modalidade_id;

        CREATE UNIQUE INDEX IF NOT EXISTS uk_alunos_modalidades_aluno_modalidade
            ON alunos_modalidades (aluno_id, modalidade_id);
        CREATE INDEX IF NOT EXISTS idx_alunos_modalidades_modalidade
            ON alunos_modalidades (modalidade_id);

        IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_alunos_modalidades_aluno') THEN
            ALTER TABLE alunos_modalidades
                ADD CONSTRAINT fk_alunos_modalidades_aluno
                FOREIGN KEY (aluno_id) REFERENCES alunos(id);
        END IF;

        IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_alunos_modalidades_modalidade') THEN
            ALTER TABLE alunos_modalidades
                ADD CONSTRAINT fk_alunos_modalidades_modalidade
                FOREIGN KEY (modalidade_id) REFERENCES modalidades(id);
        END IF;
    END IF;
END $$;
