CREATE TABLE preferencias_pagamento_alunos (
    id BIGSERIAL PRIMARY KEY,
    aluno_id BIGINT NOT NULL,
    academia_id BIGINT NOT NULL,
    forma_preferida VARCHAR(30) NOT NULL,
    ativa BOOLEAN NOT NULL DEFAULT TRUE,
    pagamento_automatico_autorizado BOOLEAN NOT NULL DEFAULT FALSE,
    status_autorizacao VARCHAR(30) NOT NULL DEFAULT 'NAO_SOLICITADA',
    provedor VARCHAR(60),
    identificador_externo_metodo VARCHAR(120),
    descricao_mascarada VARCHAR(120),
    criada_em TIMESTAMP NOT NULL,
    atualizada_em TIMESTAMP NOT NULL,
    criada_por_id BIGINT NOT NULL,
    atualizada_por_id BIGINT,
    CONSTRAINT uk_preferencia_pagamento_aluno UNIQUE (aluno_id),
    CONSTRAINT fk_preferencia_pagamento_aluno FOREIGN KEY (aluno_id) REFERENCES alunos(id),
    CONSTRAINT fk_preferencia_pagamento_academia FOREIGN KEY (academia_id) REFERENCES academias(id),
    CONSTRAINT fk_preferencia_pagamento_criada_por FOREIGN KEY (criada_por_id) REFERENCES usuarios(id),
    CONSTRAINT fk_preferencia_pagamento_atualizada_por FOREIGN KEY (atualizada_por_id) REFERENCES usuarios(id)
);

CREATE INDEX idx_preferencia_pagamento_academia_aluno ON preferencias_pagamento_alunos (academia_id, aluno_id);
CREATE INDEX idx_preferencia_pagamento_forma ON preferencias_pagamento_alunos (academia_id, forma_preferida);
CREATE INDEX idx_preferencia_pagamento_autorizacao ON preferencias_pagamento_alunos (academia_id, status_autorizacao);
