DO $$
DECLARE
    default_academia_id BIGINT;
BEGIN
    SELECT id INTO default_academia_id
    FROM academias
    WHERE LOWER(nome) = LOWER('Elite Fight Club')
    LIMIT 1;

    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'alunos') THEN
        ALTER TABLE alunos ADD COLUMN IF NOT EXISTS academia_id BIGINT;
        UPDATE alunos SET academia_id = default_academia_id WHERE academia_id IS NULL;
        ALTER TABLE alunos ALTER COLUMN academia_id SET NOT NULL;
        CREATE INDEX IF NOT EXISTS idx_alunos_academia ON alunos (academia_id);
        IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_alunos_academia') THEN
            ALTER TABLE alunos ADD CONSTRAINT fk_alunos_academia FOREIGN KEY (academia_id) REFERENCES academias(id);
        END IF;
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'professores') THEN
        ALTER TABLE professores ADD COLUMN IF NOT EXISTS academia_id BIGINT;
        UPDATE professores SET academia_id = default_academia_id WHERE academia_id IS NULL;
        ALTER TABLE professores ALTER COLUMN academia_id SET NOT NULL;
        CREATE INDEX IF NOT EXISTS idx_professores_academia ON professores (academia_id);
        IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_professores_academia') THEN
            ALTER TABLE professores ADD CONSTRAINT fk_professores_academia FOREIGN KEY (academia_id) REFERENCES academias(id);
        END IF;
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'turmas') THEN
        ALTER TABLE turmas ADD COLUMN IF NOT EXISTS academia_id BIGINT;
        UPDATE turmas SET academia_id = default_academia_id WHERE academia_id IS NULL;
        ALTER TABLE turmas ALTER COLUMN academia_id SET NOT NULL;
        CREATE INDEX IF NOT EXISTS idx_turmas_academia ON turmas (academia_id);
        IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_turmas_academia') THEN
            ALTER TABLE turmas ADD CONSTRAINT fk_turmas_academia FOREIGN KEY (academia_id) REFERENCES academias(id);
        END IF;
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'graduacoes') THEN
        ALTER TABLE graduacoes ADD COLUMN IF NOT EXISTS academia_id BIGINT;
        UPDATE graduacoes SET academia_id = default_academia_id WHERE academia_id IS NULL;
        ALTER TABLE graduacoes ALTER COLUMN academia_id SET NOT NULL;
        CREATE INDEX IF NOT EXISTS idx_graduacoes_academia ON graduacoes (academia_id);
        IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_graduacoes_academia') THEN
            ALTER TABLE graduacoes ADD CONSTRAINT fk_graduacoes_academia FOREIGN KEY (academia_id) REFERENCES academias(id);
        END IF;
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'check_ins') THEN
        ALTER TABLE check_ins ADD COLUMN IF NOT EXISTS academia_id BIGINT;
        UPDATE check_ins SET academia_id = default_academia_id WHERE academia_id IS NULL;
        CREATE INDEX IF NOT EXISTS idx_check_ins_academia ON check_ins (academia_id);
        IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_check_ins_academia') THEN
            ALTER TABLE check_ins ADD CONSTRAINT fk_check_ins_academia FOREIGN KEY (academia_id) REFERENCES academias(id);
        END IF;
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'faturas') THEN
        ALTER TABLE faturas ADD COLUMN IF NOT EXISTS academia_id BIGINT;
        UPDATE faturas SET academia_id = default_academia_id WHERE academia_id IS NULL;
        ALTER TABLE faturas ALTER COLUMN academia_id SET NOT NULL;
        CREATE INDEX IF NOT EXISTS idx_faturas_academia ON faturas (academia_id);
        IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_faturas_academia') THEN
            ALTER TABLE faturas ADD CONSTRAINT fk_faturas_academia FOREIGN KEY (academia_id) REFERENCES academias(id);
        END IF;
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'pagamentos') THEN
        ALTER TABLE pagamentos ADD COLUMN IF NOT EXISTS academia_id BIGINT;
        UPDATE pagamentos SET academia_id = default_academia_id WHERE academia_id IS NULL;
        ALTER TABLE pagamentos ALTER COLUMN academia_id SET NOT NULL;
        CREATE INDEX IF NOT EXISTS idx_pagamentos_academia ON pagamentos (academia_id);
        IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_pagamentos_academia') THEN
            ALTER TABLE pagamentos ADD CONSTRAINT fk_pagamentos_academia FOREIGN KEY (academia_id) REFERENCES academias(id);
        END IF;
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'usuarios') THEN
        ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS academia_id BIGINT;
        UPDATE usuarios SET academia_id = default_academia_id WHERE academia_id IS NULL;
        CREATE INDEX IF NOT EXISTS idx_usuarios_academia ON usuarios (academia_id);
        IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_usuarios_academia') THEN
            ALTER TABLE usuarios ADD CONSTRAINT fk_usuarios_academia FOREIGN KEY (academia_id) REFERENCES academias(id);
        END IF;
    END IF;
END $$;
