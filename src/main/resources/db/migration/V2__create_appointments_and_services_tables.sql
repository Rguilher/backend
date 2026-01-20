-- 1. Tabela de Serviços
CREATE TABLE tb_services
(
    id           SERIAL PRIMARY KEY,
    name         VARCHAR(100)   NOT NULL,
    description  VARCHAR(255)   NOT NULL,
    price        NUMERIC(19, 2) NOT NULL,
    duration_min INTEGER        NOT NULL,
    active       BOOLEAN        NOT NULL DEFAULT TRUE
);

-- 2. Tabela de Agendamentos
CREATE TABLE tb_appointments
(
    id              SERIAL PRIMARY KEY,
    client_id       BIGINT      NOT NULL,
    professional_id BIGINT      NOT NULL,
    service_id      BIGINT      NOT NULL,
    date_time       TIMESTAMP   NOT NULL,
    status          VARCHAR(20) NOT NULL,
    observation     TEXT,

    -- Chaves Estrangeiras (FK)
    CONSTRAINT fk_appointments_client FOREIGN KEY (client_id) REFERENCES tb_users (id),
    CONSTRAINT fk_appointments_professional FOREIGN KEY (professional_id) REFERENCES tb_users (id),
    CONSTRAINT fk_appointments_service FOREIGN KEY (service_id) REFERENCES tb_services (id)
);