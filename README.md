-- ============================================================
-- TABLES POUR RENAULT APP (Auth + Véhicules + Rendez-vous)
-- ============================================================

-- 1. TABLE UTILISATEURS (clients, mécaniciens, admin)
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(255),
    is_active BOOLEAN DEFAULT true,
    email_verified BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. TABLE RÔLES (ADMIN, MECANICIEN, CLIENT)
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, role),
    CONSTRAINT fk_user_roles_user 
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 3. TABLE VÉHICULES (lié au client)
CREATE TABLE IF NOT EXISTS vehicles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    plate_number VARCHAR(255) NOT NULL UNIQUE,
    plate_type VARCHAR(50) NOT NULL,
    region_code VARCHAR(50),
    governorate_code VARCHAR(50),
    brand VARCHAR(255),
    model VARCHAR(255),
    year INTEGER,
    color VARCHAR(255),
    vin_number VARCHAR(255),
    horsepower INTEGER,
    fuel_type VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_vehicle_user 
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 4. TABLE RENDEZ-VOUS
CREATE TABLE IF NOT EXISTS appointments (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    vehicle_id BIGINT NOT NULL,
    mecanicien_id BIGINT,
    appointment_date TIMESTAMP NOT NULL,
    service_type VARCHAR(255),
    status VARCHAR(50) DEFAULT 'EN_ATTENTE',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_appointment_user 
        FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_appointment_vehicle 
        FOREIGN KEY (vehicle_id) REFERENCES vehicles(id),
    CONSTRAINT fk_appointment_mecanicien 
        FOREIGN KEY (mecanicien_id) REFERENCES users(id)
);

-- 5. INDEX POUR PERFORMANCE
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_vehicles_plate ON vehicles(plate_number);
CREATE INDEX IF NOT EXISTS idx_appointments_user ON appointments(user_id);

-- ============================================================
-- DONNÉES DE TEST
-- ============================================================

-- ADMIN
INSERT INTO users (email, password, first_name, last_name, is_active, email_verified)
VALUES (
    'admin@renault.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqQzBZN0UfGNEsKYGs5D9W4dKLzGq',
    'Admin', 'Renault', true, true
) ON CONFLICT (email) DO NOTHING;

INSERT INTO user_roles (user_id, role) 
SELECT id, 'ADMIN' FROM users WHERE email = 'admin@renault.com'
ON CONFLICT DO NOTHING;

-- MECANICIEN
INSERT INTO users (email, password, first_name, last_name, is_active, email_verified)
VALUES (
    'mecanicien@renault.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqQzBZN0UfGNEsKYGs5D9W4dKLzGq',
    'Karim', 'Ben Ali', true, true
) ON CONFLICT (email) DO NOTHING;

INSERT INTO user_roles (user_id, role) 
SELECT id, 'MECANICIEN' FROM users WHERE email = 'mecanicien@renault.com'
ON CONFLICT DO NOTHING;

-- CLIENT (propriétaire véhicule)
INSERT INTO users (email, password, first_name, last_name, phone_number, is_active, email_verified)
VALUES (
    'client@renault.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqQzBZN0UfGNEsKYGs5D9W4dKLzGq',
    'Mohamed', 'Trabelsi', '50123456', true, true
) ON CONFLICT (email) DO NOTHING;

INSERT INTO user_roles (user_id, role) 
SELECT id, 'CLIENT' FROM users WHERE email = 'client@renault.com'
ON CONFLICT DO NOTHING;

-- VÉHICULE DU CLIENT
INSERT INTO vehicles (user_id, plate_number, plate_type, region_code, governorate_code, brand, model, year, color, fuel_type)
SELECT 
    u.id, '123 TN 456', 'TUNISIAN', 'TN', 'Tunis', 'Renault', 'Clio', 2020, 'Rouge', 'Essence'
FROM users u WHERE u.email = 'client@renault.com'
ON CONFLICT (plate_number) DO NOTHING;

-- RENDEZ-VOUS
INSERT INTO appointments (user_id, vehicle_id, appointment_date, service_type, status, notes)
SELECT 
    u.id, v.id, '2025-05-10 09:00:00', 'Vidange', 'CONFIRME', 'Première vidange'
FROM users u, vehicles v 
WHERE u.email = 'client@renault.com' AND v.plate_number = '123 TN 456'
ON CONFLICT DO NOTHING;

SELECT '✅ Tables et données créées avec succès' AS status;
