ALTER TABLE tb_users ADD COLUMN recovery_code VARCHAR(6);
ALTER TABLE tb_users ADD COLUMN recovery_code_expiry TIMESTAMP;