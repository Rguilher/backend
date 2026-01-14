-- V4: Criação de índices para otimização de performance nas buscas de agendamento

-- 1. Índice Composto para Agenda do Profissional
-- Motivo: Otimiza a busca de conflitos e listagem de agenda (AppointmentService.listToday, etc)
-- Query afetada: WHERE professional_id = ? AND date_time BETWEEN ? AND ?
CREATE INDEX idx_appointments_professional_date
    ON tb_appointments (professional_id, date_time);

-- 2. Índice Composto para Histórico do Cliente
-- Motivo: Otimiza a listagem "Meus Agendamentos" e verificação de conflito do cliente
-- Query afetada: WHERE client_id = ? AND date_time BETWEEN ? AND ?
CREATE INDEX idx_appointments_client_date
    ON tb_appointments (client_id, date_time);

-- 3. Índice para Serviços Ativos
-- Motivo: Otimiza a listagem de serviços na tela inicial do app
-- Query afetada: WHERE active = true
CREATE INDEX idx_services_active
    ON tb_services (active);

-- Criamos índices compostos (FK + Data) porque nossas queries quase sempre filtram por data também.