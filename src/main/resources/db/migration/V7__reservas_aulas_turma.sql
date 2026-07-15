ALTER TABLE alunos ADD COLUMN IF NOT EXISTS plano_id BIGINT;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'planos') THEN
        ALTER TABLE alunos
            ADD CONSTRAINT fk_alunos_plano
            FOREIGN KEY (plano_id) REFERENCES planos(id);
    END IF;
EXCEPTION
    WHEN duplicate_object THEN NULL;
END $$;

CREATE TABLE IF NOT EXISTS reservas_aula (
    id BIGSERIAL PRIMARY KEY,
    academia_id BIGINT NOT NULL,
    aula_turma_id BIGINT NOT NULL,
    aluno_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL,
    reservado_em TIMESTAMP NOT NULL,
    cancelado_em TIMESTAMP,
    chave_reserva_ativa VARCHAR(80),
    CONSTRAINT fk_reservas_academia FOREIGN KEY (academia_id) REFERENCES academias(id),
    CONSTRAINT fk_reservas_aula_turma FOREIGN KEY (aula_turma_id) REFERENCES aulas_turma(id),
    CONSTRAINT fk_reservas_aluno FOREIGN KEY (aluno_id) REFERENCES alunos(id)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_reserva_ativa
    ON reservas_aula(chave_reserva_ativa)
    WHERE chave_reserva_ativa IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_reserva_aula
    ON reservas_aula(academia_id, aula_turma_id);

CREATE INDEX IF NOT EXISTS idx_reserva_aluno
    ON reservas_aula(academia_id, aluno_id);
