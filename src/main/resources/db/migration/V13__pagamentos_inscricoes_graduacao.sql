ALTER TABLE pagamentos
    ADD COLUMN IF NOT EXISTS tipo_cobranca VARCHAR(30) NOT NULL DEFAULT 'MENSALIDADE';

ALTER TABLE pagamentos
    ADD COLUMN IF NOT EXISTS inscricao_graduacao_id BIGINT;

ALTER TABLE pagamentos
    ALTER COLUMN fatura_id DROP NOT NULL;

ALTER TABLE inscricoes_graduacao
    ADD COLUMN IF NOT EXISTS pagamento_id BIGINT;

ALTER TABLE inscricoes_graduacao
    ADD COLUMN IF NOT EXISTS pago_em TIMESTAMP;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_pagamentos_inscricao_graduacao') THEN
        ALTER TABLE pagamentos
            ADD CONSTRAINT fk_pagamentos_inscricao_graduacao
            FOREIGN KEY (inscricao_graduacao_id) REFERENCES inscricoes_graduacao(id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_inscricoes_graduacao_pagamento') THEN
        ALTER TABLE inscricoes_graduacao
            ADD CONSTRAINT fk_inscricoes_graduacao_pagamento
            FOREIGN KEY (pagamento_id) REFERENCES pagamentos(id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'ck_pagamentos_origem_unica') THEN
        ALTER TABLE pagamentos
            ADD CONSTRAINT ck_pagamentos_origem_unica CHECK (
                (tipo_cobranca = 'MENSALIDADE' AND fatura_id IS NOT NULL AND inscricao_graduacao_id IS NULL)
                OR
                (tipo_cobranca = 'GRADUACAO' AND fatura_id IS NULL AND inscricao_graduacao_id IS NOT NULL)
            );
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_pagamentos_inscricao_graduacao
    ON pagamentos (inscricao_graduacao_id);

CREATE INDEX IF NOT EXISTS idx_pagamentos_tipo_cobranca
    ON pagamentos (academia_id, tipo_cobranca);

CREATE INDEX IF NOT EXISTS idx_inscricoes_graduacao_pagamento
    ON inscricoes_graduacao (pagamento_id);
