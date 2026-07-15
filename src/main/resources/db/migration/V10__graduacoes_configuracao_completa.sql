ALTER TABLE graduacoes
    ADD COLUMN IF NOT EXISTS tempo_minimo_meses INTEGER NOT NULL DEFAULT 0;

ALTER TABLE graduacoes
    ADD COLUMN IF NOT EXISTS valor_padrao_graduacao NUMERIC(10, 2) NOT NULL DEFAULT 0;

ALTER TABLE graduacoes
    ADD COLUMN IF NOT EXISTS descricao VARCHAR(500);

UPDATE graduacoes
SET tempo_minimo_meses = 0
WHERE tempo_minimo_meses IS NULL;

UPDATE graduacoes
SET valor_padrao_graduacao = 0
WHERE valor_padrao_graduacao IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_graduacoes_academia_nome
    ON graduacoes (academia_id, lower(nome));

CREATE UNIQUE INDEX IF NOT EXISTS uk_graduacoes_academia_ordem
    ON graduacoes (academia_id, ordem);

CREATE UNIQUE INDEX IF NOT EXISTS uk_graduacoes_academia_base_ativa
    ON graduacoes (academia_id)
    WHERE graduacao_base = true AND ativa = true;
