INSERT INTO academias (nome, cpf_ou_cnpj, email, telefone, status, data_criacao)
SELECT 'Elite Fight Club', NULL, NULL, NULL, 'ATIVA', CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM academias WHERE LOWER(nome) = LOWER('Elite Fight Club')
);
