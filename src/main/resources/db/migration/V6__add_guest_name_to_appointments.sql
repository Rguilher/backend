ALTER TABLE tb_appointments ALTER COLUMN client_id DROP NOT NULL;
ALTER TABLE tb_appointments ADD COLUMN guest_name VARCHAR(255);
ALTER TABLE tb_appointments ADD COLUMN guest_phone VARCHAR(20);