-- Resetea la base completa, recrea todas las tablas e inserta los datos mockeados.

CREATE DATABASE IF NOT EXISTS fitexa CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE fitexa;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS routine_assignment_exercises;
DROP TABLE IF EXISTS routine_assignment_days;
DROP TABLE IF EXISTS routine_assignments;
DROP TABLE IF EXISTS routine_template_exercises;
DROP TABLE IF EXISTS routine_templates;
DROP TABLE IF EXISTS exercise_personal_record_entries;
DROP TABLE IF EXISTS exercise_personal_records;
DROP TABLE IF EXISTS gym_class_subscriptions;
DROP TABLE IF EXISTS user_payment_movements;
DROP TABLE IF EXISTS gym_class_days;
DROP TABLE IF EXISTS gym_classes;
DROP TABLE IF EXISTS exercises;
DROP TABLE IF EXISTS machines;
DROP TABLE IF EXISTS activities;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS plans;
SET FOREIGN_KEY_CHECKS = 1;
-- Crea todas las tablas del backend Fitexa sin insertar datos.

CREATE TABLE plans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(120) NOT NULL UNIQUE,
    price DECIMAL(12, 2) NOT NULL,
    max_days_per_week INT NOT NULL,
    active TINYINT(1) NOT NULL
);

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(120) NOT NULL,
    last_name VARCHAR(120) NOT NULL,
    dni VARCHAR(40) NOT NULL UNIQUE,
    address VARCHAR(250) NOT NULL,
    phone VARCHAR(80) NOT NULL,
    email VARCHAR(180) NOT NULL UNIQUE,
    password VARCHAR(120) NOT NULL,
    must_change_password TINYINT(1) NOT NULL DEFAULT 0,
    birth_date DATE NOT NULL,
    user_type VARCHAR(40) NOT NULL,
    plan_id BIGINT NULL,
    registration_date DATE NOT NULL,
    renewal_date DATE NOT NULL,
    payment_status VARCHAR(40) NOT NULL,
    CONSTRAINT fk_users_plan FOREIGN KEY (plan_id) REFERENCES plans(id) ON DELETE SET NULL
);

CREATE TABLE user_payment_movements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    plan_id BIGINT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    detail VARCHAR(255) NOT NULL,
    paid_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_payment_movements_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_payment_movements_plan FOREIGN KEY (plan_id) REFERENCES plans(id) ON DELETE SET NULL
);

CREATE TABLE activities (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(140) NOT NULL UNIQUE,
    description VARCHAR(500) NOT NULL
);

CREATE TABLE machines (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(140) NOT NULL UNIQUE,
    photo_url TEXT NOT NULL
);

CREATE TABLE exercises (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(140) NOT NULL,
    activity_id BIGINT NULL,
    machine_id BIGINT NULL,
    video_url TEXT NOT NULL,
    CONSTRAINT fk_exercises_activity FOREIGN KEY (activity_id) REFERENCES activities(id) ON DELETE SET NULL,
    CONSTRAINT fk_exercises_machine FOREIGN KEY (machine_id) REFERENCES machines(id) ON DELETE SET NULL
);

CREATE TABLE gym_classes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    class_name VARCHAR(180) NOT NULL,
    activity_id BIGINT NULL,
    max_capacity INT NULL,
    enrolled INT NOT NULL,
    teacher_id BIGINT NULL,
    date DATE NOT NULL,
    all_day TINYINT(1) NOT NULL DEFAULT 0,
    start_time TIME NULL,
    end_time TIME NULL,
    CONSTRAINT fk_classes_activity FOREIGN KEY (activity_id) REFERENCES activities(id) ON DELETE SET NULL,
    CONSTRAINT fk_classes_teacher FOREIGN KEY (teacher_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE gym_class_days (
    gym_class_id BIGINT NOT NULL,
    day VARCHAR(32) NOT NULL,
    PRIMARY KEY (gym_class_id, day),
    CONSTRAINT fk_class_days_class FOREIGN KEY (gym_class_id) REFERENCES gym_classes(id) ON DELETE CASCADE
);

CREATE TABLE gym_class_subscriptions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    gym_class_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    target_date DATE NOT NULL,
    subscribed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_class_subscriptions_class FOREIGN KEY (gym_class_id) REFERENCES gym_classes(id) ON DELETE CASCADE,
    CONSTRAINT fk_class_subscriptions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_gym_class_subscriptions_class_user_date UNIQUE (gym_class_id, user_id, target_date)
);

CREATE TABLE exercise_personal_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    exercise_id BIGINT NOT NULL,
    max_weight DOUBLE NOT NULL,
    recorded_at DATE NOT NULL,
    CONSTRAINT fk_exercise_personal_records_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_exercise_personal_records_exercise FOREIGN KEY (exercise_id) REFERENCES exercises(id) ON DELETE CASCADE,
    CONSTRAINT uq_exercise_personal_records_user_exercise UNIQUE (user_id, exercise_id)
);

CREATE TABLE exercise_personal_record_entries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    exercise_id BIGINT NOT NULL,
    weight DOUBLE NOT NULL,
    recorded_at DATE NOT NULL,
    CONSTRAINT fk_exercise_personal_record_entries_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_exercise_personal_record_entries_exercise FOREIGN KEY (exercise_id) REFERENCES exercises(id) ON DELETE CASCADE
);

CREATE TABLE routine_templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(180) NOT NULL,
    created_at DATE NOT NULL
);

CREATE TABLE routine_template_exercises (
    routine_template_id BIGINT NOT NULL,
    exercise_id BIGINT NOT NULL,
    PRIMARY KEY (routine_template_id, exercise_id),
    CONSTRAINT fk_rt_ex_template FOREIGN KEY (routine_template_id) REFERENCES routine_templates(id) ON DELETE CASCADE,
    CONSTRAINT fk_rt_ex_exercise FOREIGN KEY (exercise_id) REFERENCES exercises(id) ON DELETE CASCADE
);

CREATE TABLE routine_assignments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    routine_template_id BIGINT NULL,
    custom_name VARCHAR(180) NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    CONSTRAINT fk_ra_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_ra_template FOREIGN KEY (routine_template_id) REFERENCES routine_templates(id) ON DELETE CASCADE
);

CREATE TABLE routine_assignment_days (
    routine_assignment_id BIGINT NOT NULL,
    day VARCHAR(32) NOT NULL,
    PRIMARY KEY (routine_assignment_id, day),
    CONSTRAINT fk_ra_days_assignment FOREIGN KEY (routine_assignment_id) REFERENCES routine_assignments(id) ON DELETE CASCADE
);

CREATE TABLE routine_assignment_exercises (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    assignment_id BIGINT NOT NULL,
    exercise_id BIGINT NOT NULL,
    exercise_name VARCHAR(180) NOT NULL,
    sets INT NOT NULL,
    prescription_mode VARCHAR(40) NOT NULL,
    reps INT NULL,
    weight DOUBLE NULL,
    min_reps INT NULL,
    max_reps INT NULL,
    min_weight DOUBLE NULL,
    max_weight DOUBLE NULL,
    CONSTRAINT fk_rae_assignment FOREIGN KEY (assignment_id) REFERENCES routine_assignments(id) ON DELETE CASCADE,
    CONSTRAINT fk_rae_exercise FOREIGN KEY (exercise_id) REFERENCES exercises(id) ON DELETE CASCADE
);

CREATE INDEX idx_ra_user_dates ON routine_assignments(user_id, start_date, end_date);


INSERT INTO plans (id, name, price, max_days_per_week, active) VALUES
    (1, '2 veces por semana', 22000.00, 2, 1),
    (2, '3 veces por semana', 29000.00, 3, 1),
    (3, 'Libre', 36000.00, 7, 1);

INSERT INTO users (
    id, first_name, last_name, dni, address, phone, email, password, must_change_password,
    birth_date, user_type, plan_id, registration_date, renewal_date, payment_status
) VALUES
    (1, 'Lucia', 'Gimenez', '32450987', 'Belgrano 145', '3415011002', 'lucia.gimenez@fitexa.com', '$2a$10$YbErXCYxoUpeUBp5slGG8.lMA7nAznLCGND/EEnO8WkkRspqlKOSy', 0, '1992-09-05', 'PROFESOR', NULL, '2025-02-10', '2026-03-20', 'AL_DIA'),
    (2, 'Diego', 'Ferrero', '29877441', 'San Luis 932', '3416120044', 'diego.ferrero@fitexa.com', '$2a$10$5tIHmy6asaOU6cnupwJ/aupfW505xKJ9XoVpFwBwO9v5NGAb13lWO', 0, '1987-04-14', 'PROFESOR', NULL, '2025-02-20', '2026-03-22', 'AL_DIA'),
    (3, 'Agustina', 'Ruiz', '31244876', 'Mitre 1880', '3415772200', 'agustina.ruiz@fitexa.com', '$2a$10$lz5wUNfCASVvXHRGj5vUK.BJyZMMzHcNvYN9.e0GP3sHnTCOx/mm6', 0, '1990-06-25', 'PROFESOR', NULL, '2025-03-05', '2026-03-25', 'AL_DIA'),
    (4, 'Nicolas', 'Vega', '30124577', 'Cordoba 522', '3415339981', 'nicolas.vega@fitexa.com', '$2a$10$PjWtYrSZ3P69Ab5ajOZ4.uzE0nof6knfiRZkFuONYfROkNRnTqKka', 0, '1988-11-17', 'PROFESOR', NULL, '2025-03-18', '2026-03-24', 'AL_DIA'),
    (5, 'Carla', 'Ibanez', '31977551', 'Roca 310', '3415099877', 'carla.ibanez@fitexa.com', '$2a$10$uryjiK.ddiJOV57t5rdjwOucAcSo6ubpMQBjEnGl6TCwSjeV6hR.a', 0, '1991-08-30', 'PROFESOR', NULL, '2025-03-28', '2026-03-28', 'AL_DIA'),
    (1001, 'Maxi', 'Roselli', '33555111', 'Oroño 250', '3416001001', 'maxiroselli@gmail.com', '$2a$10$SpeYBueseIfgAt3OugKhDek8kUYP.5SV0/lqMxT0/kynvMGRAEP2W', 0, '1992-11-07', 'ALUMNO', 3, '2025-04-12', '2026-03-18', 'AL_DIA'),
    (1002, 'Ana', 'Lopez', '35444222', 'Italia 455', '3416001002', 'ana.lopez@gmail.com', '$2a$10$3kBbzCelX87g/62NuScCMeg1/caXEx5ys3PCO10Y65S0bdadcu/7S', 0, '1995-03-09', 'ALUMNO', 1, '2025-04-29', '2026-03-11', 'AL_DIA'),
    (1003, 'Bruno', 'Diaz', '36666333', 'Mendoza 640', '3416001003', 'bruno.diaz@gmail.com', '$2a$10$jk7pBQwd8E/zWjZyri0UiOzY8TYxPNdJBtO9gyuw0Z9fWGoBIi6ru', 0, '1993-08-22', 'ALUMNO', 2, '2025-05-15', '2026-03-07', 'ADEUDA'),
    (1004, 'Sofia', 'Perez', '37777444', 'Brown 210', '3416001004', 'sofia.perez@gmail.com', '$2a$10$SeybWAyBHgE8.CZelTt9huQoLX4SA/OavZfC6Jon7oNzx6ab8mp2S', 0, '1998-01-14', 'ALUMNO', 1, '2025-06-03', '2026-03-14', 'AL_DIA'),
    (1005, 'Laura', 'Gomez', '38888555', 'Mitre 920', '3416001005', 'laura.gomez@gmail.com', '$2a$10$mkU.AH9tU0qjpq.sjqbeluAe0HKzNVzltg0tYcgjMTbpf1X63AWTy', 0, '1991-05-02', 'ALUMNO', 2, '2025-06-27', '2026-03-05', 'ADEUDA'),
    (1006, 'Martin', 'Suarez', '39999666', 'Cordoba 1100', '3416001006', 'martin.suarez@gmail.com', '$2a$10$.iNuNO25C.E.3fjUwj0ytONPjk4QzCjCuglJSDQoPQIE2E3cR0pqa', 0, '1989-09-17', 'ALUMNO', 3, '2025-07-11', '2026-03-20', 'AL_DIA'),
    (1007, 'Julieta', 'Fernandez', '40111777', 'San Juan 744', '3416001007', 'julieta.fernandez@gmail.com', '$2a$10$LfkVhRWaL0m5w7U5IJ6ume8I8cwDcmjw7Bv4GQZIDoUfSuV6xT8C2', 0, '1997-04-25', 'ALUMNO', 1, '2025-08-02', '2026-03-10', 'AL_DIA'),
    (1008, 'Tomas', 'Romero', '41222888', 'Catamarca 190', '3416001008', 'tomas.romero@gmail.com', '$2a$10$M/sr65hmxGzio7QyR7koru7PwK1VeX/9R/CdYQ/O.VXyO6f7dJ1zy', 0, '1994-12-11', 'ALUMNO', 2, '2025-08-21', '2026-03-08', 'ADEUDA'),
    (1009, 'Camila', 'Moreno', '42333999', '3 de Febrero 80', '3416001009', 'camila.moreno@gmail.com', '$2a$10$dpoPAXVD54POca2FwxNsCeQWJ8KMkDhwK7HRVBcTMVNE4btp5FUDq', 0, '1996-06-18', 'ALUMNO', 3, '2025-09-12', '2026-03-16', 'AL_DIA'),
    (1010, 'Facundo', 'Acosta', '43444111', 'Santa Fe 515', '3416001010', 'facundo.acosta@gmail.com', '$2a$10$E/XhN9dBcs68xUonah4Ks.nlSKA5IrLoNTHYQA2OEHWZPShknYGZy', 0, '1990-10-30', 'ALUMNO', 1, '2025-10-01', '2026-03-13', 'AL_DIA');

INSERT INTO user_payment_movements (id, user_id, plan_id, amount, detail, paid_at) VALUES
    (1, 1001, 3, 36000.00, 'Renovacion de cuota', '2026-01-18 09:15:00'),
    (2, 1001, 3, 36000.00, 'Renovacion de cuota', '2026-02-18 09:20:00'),
    (3, 1002, 1, 22000.00, 'Renovacion de cuota', '2026-01-12 18:10:00'),
    (4, 1002, 1, 22000.00, 'Renovacion de cuota', '2026-02-12 18:15:00'),
    (5, 1003, 2, 29000.00, 'Renovacion de cuota', '2026-01-07 08:30:00'),
    (6, 1004, 1, 22000.00, 'Renovacion de cuota', '2026-01-14 19:00:00'),
    (7, 1004, 1, 22000.00, 'Renovacion de cuota', '2026-02-14 19:05:00'),
    (8, 1005, 2, 29000.00, 'Renovacion de cuota', '2026-01-05 10:00:00'),
    (9, 1006, 3, 36000.00, 'Renovacion de cuota', '2026-01-20 07:40:00'),
    (10, 1006, 3, 36000.00, 'Renovacion de cuota', '2026-02-20 07:45:00'),
    (11, 1007, 1, 22000.00, 'Renovacion de cuota', '2026-01-10 12:05:00'),
    (12, 1007, 1, 22000.00, 'Renovacion de cuota', '2026-02-10 12:10:00'),
    (13, 1008, 2, 29000.00, 'Renovacion de cuota', '2026-01-08 16:25:00'),
    (14, 1009, 3, 36000.00, 'Renovacion de cuota', '2026-01-16 09:50:00'),
    (15, 1009, 3, 36000.00, 'Renovacion de cuota', '2026-02-16 09:55:00'),
    (16, 1010, 1, 22000.00, 'Renovacion de cuota', '2026-01-13 17:30:00'),
    (17, 1010, 1, 22000.00, 'Renovacion de cuota', '2026-02-13 17:35:00');

INSERT INTO activities (id, name, description) VALUES
    (1, 'Musculacion', 'Sala libre con seguimiento mensual'),
    (2, 'Crossfit', 'Entrenamiento funcional de alta intensidad'),
    (3, 'Pilates', 'Clases de movilidad y control corporal'),
    (4, 'Spinning', 'Trabajo cardiovascular guiado en bicicleta fija'),
    (5, 'Funcional', 'Circuitos de fuerza y resistencia'),
    (6, 'Yoga', 'Practica de movilidad, respiracion y flexibilidad'),
    (7, 'HIIT', 'Intervalos cortos de alta intensidad'),
    (8, 'Stretching', 'Flexibilidad y recuperacion activa');

INSERT INTO machines (id, name, photo_url) VALUES
    (1, 'Smith Machine', 'https://images.unsplash.com/photo-1583454110551-21f2fa2afe61?auto=format&fit=crop&w=900&q=80'),
    (2, 'Remo bajo', 'https://images.unsplash.com/photo-1584466977773-e625c37cdd50?auto=format&fit=crop&w=900&q=80'),
    (3, 'Prensa 45', 'https://images.unsplash.com/photo-1518611012118-696072aa579a?auto=format&fit=crop&w=900&q=80'),
    (4, 'Polea alta', 'https://images.unsplash.com/photo-1571019613576-2b22c76fd955?auto=format&fit=crop&w=900&q=80'),
    (5, 'Banco inclinado', 'https://images.unsplash.com/photo-1549476464-37392f717541?auto=format&fit=crop&w=900&q=80'),
    (6, 'Bicicleta fija', 'https://images.unsplash.com/photo-1517838277536-f5f99be501cd?auto=format&fit=crop&w=900&q=80'),
    (7, 'Cinta de correr', 'https://images.unsplash.com/photo-1571902943202-507ec2618e8f?auto=format&fit=crop&w=900&q=80'),
    (8, 'Eliptico', 'https://images.unsplash.com/photo-1517963628607-235ccdd5476b?auto=format&fit=crop&w=900&q=80'),
    (9, 'Jalon al pecho', 'https://images.unsplash.com/photo-1549570652-97324981a6fd?auto=format&fit=crop&w=900&q=80'),
    (10, 'Extensiones cuadriceps', 'https://images.unsplash.com/photo-1517964101697-5ed5f9f7ad66?auto=format&fit=crop&w=900&q=80');

INSERT INTO exercises (id, name, activity_id, machine_id, video_url) VALUES
    (1, 'Sentadilla en Smith', 1, 1, 'https://www.youtube.com/watch?v=SW_C1A-rejs'),
    (2, 'Burpee con salto', 2, NULL, 'https://www.youtube.com/watch?v=TU8QYVW0gDU'),
    (3, 'Remo en polea baja', 1, 2, 'https://www.youtube.com/watch?v=GZbfZ033f74'),
    (4, 'Prensa de piernas', 1, 3, 'https://www.youtube.com/watch?v=IZxyjW7MPJQ'),
    (5, 'Plancha frontal', 5, NULL, 'https://www.youtube.com/watch?v=pSHjTRCQxIw'),
    (6, 'Sprint en bicicleta', 4, 6, 'https://www.youtube.com/watch?v=RjQf3Q9M9jE'),
    (7, 'Bird-dog controlado', 6, NULL, 'https://www.youtube.com/watch?v=wiFNA3sqjCA'),
    (8, 'Press inclinado con mancuernas', 1, 5, 'https://www.youtube.com/watch?v=8iPEnn-ltC8'),
    (9, 'Jalon al pecho agarre ancho', 1, 9, 'https://www.youtube.com/watch?v=CAwf7n6Luuc'),
    (10, 'Peso muerto rumano', 1, NULL, 'https://www.youtube.com/watch?v=2SHsk9AzdjA'),
    (11, 'Abdominal bicicleta', 5, NULL, 'https://www.youtube.com/watch?v=9FGilxCbdz8'),
    (12, 'Fondos en banco', 5, NULL, 'https://www.youtube.com/watch?v=tKjcgfu44sI'),
    (13, 'Goblet squat', 2, NULL, 'https://www.youtube.com/watch?v=6xwGFn-J_QM'),
    (14, 'Thruster con barra', 2, NULL, 'https://www.youtube.com/watch?v=L219ltL15zk'),
    (15, 'Kettlebell swing', 2, NULL, 'https://www.youtube.com/watch?v=ys1wz0M_BmM'),
    (16, 'Mountain climbers', 7, NULL, 'https://www.youtube.com/watch?v=nmwgirgXLYM'),
    (17, 'Saltos al cajon', 7, NULL, 'https://www.youtube.com/watch?v=52r3xU6-0vI'),
    (18, 'Remo invertido', 5, NULL, 'https://www.youtube.com/watch?v=J4Q6F2Qd8d8'),
    (19, 'Extensiones de cuadriceps', 1, 10, 'https://www.youtube.com/watch?v=YyvSfVjQeL0'),
    (20, 'Press militar', 1, NULL, 'https://www.youtube.com/watch?v=B-aVuyhvLHU'),
    (21, 'Spinning intervalos', 4, 6, 'https://www.youtube.com/watch?v=Q2I7fA_u8hQ'),
    (22, 'Postura del guerrero', 6, NULL, 'https://www.youtube.com/watch?v=Jqj5nA4V7vQ'),
    (23, 'Movilidad de cadera', 8, NULL, 'https://www.youtube.com/watch?v=0QwM9vSxJkg'),
    (24, 'Estiramiento isquios', 8, NULL, 'https://www.youtube.com/watch?v=mY7a1KQxD2w');

INSERT INTO routine_templates (id, name, created_at) VALUES
    (1, 'Hipertrofia inicial', '2025-12-10'),
    (2, 'Resistencia metabolica', '2025-12-18'),
    (3, 'Fuerza tren inferior', '2025-12-26'),
    (4, 'Movilidad y core', '2026-01-05'),
    (5, 'Acondicionamiento general', '2026-01-12'),
    (6, 'Spinning performance', '2026-01-20');

INSERT INTO routine_template_exercises (routine_template_id, exercise_id) VALUES
    (1, 1), (1, 8), (1, 9), (1, 20),
    (2, 2), (2, 13), (2, 15), (2, 16),
    (3, 4), (3, 10), (3, 19), (3, 3),
    (4, 5), (4, 7), (4, 23), (4, 24),
    (5, 3), (5, 11), (5, 12), (5, 18),
    (6, 6), (6, 21), (6, 11), (6, 5);

INSERT INTO gym_classes (id, class_name, activity_id, max_capacity, enrolled, teacher_id, date, all_day, start_time, end_time) VALUES
    (1, 'Crossfit Morning', 2, 6, 0, 1, '2026-02-09', 0, '08:00:00', '09:00:00'),
    (2, 'Pilates Suelo', 3, 5, 0, 3, '2026-02-10', 0, '09:30:00', '10:30:00'),
    (3, 'Yoga Flow', 6, 6, 0, 5, '2026-02-10', 0, '09:30:00', '10:30:00'),
    (4, 'Spinning Power', 4, 5, 0, 5, '2026-02-09', 0, '19:00:00', '20:00:00'),
    (5, 'Funcional Express', 5, 4, 0, 3, '2026-02-10', 0, '07:15:00', '08:00:00'),
    (6, 'HIIT Mediodia', 7, 4, 0, 2, '2026-02-11', 0, '12:30:00', '13:15:00'),
    (7, 'Core y Movilidad', 8, 5, 0, 2, '2026-02-12', 0, '20:00:00', '20:45:00'),
    (8, 'Box Funcional', 5, 5, 0, 4, '2026-02-13', 0, '19:00:00', '20:00:00'),
    (9, 'Musculacion Intermedio', 1, 8, 0, 1, '2026-02-10', 0, '18:00:00', '19:00:00'),
    (10, 'Crossfit Evening', 2, 6, 0, 2, '2026-02-10', 0, '18:00:00', '19:00:00'),
    (11, 'Stretching Recovery', 8, 8, 0, 4, '2026-02-14', 1, NULL, NULL),
    (12, 'Musculacion Open', 1, NULL, 0, NULL, '2026-02-09', 1, NULL, NULL);

INSERT INTO gym_class_days (gym_class_id, day) VALUES
    (1, 'LUNES'), (1, 'MIERCOLES'),
    (2, 'MARTES'), (2, 'JUEVES'),
    (3, 'MARTES'), (3, 'JUEVES'),
    (4, 'LUNES'), (4, 'VIERNES'),
    (5, 'MARTES'), (5, 'VIERNES'),
    (6, 'MIERCOLES'),
    (7, 'JUEVES'),
    (8, 'VIERNES'),
    (9, 'MARTES'), (9, 'JUEVES'),
    (10, 'MARTES'), (10, 'JUEVES'),
    (11, 'SABADO'),
    (12, 'LUNES'), (12, 'MIERCOLES'), (12, 'VIERNES');

CREATE TEMPORARY TABLE tmp_weeks (
    monday_date DATE NOT NULL PRIMARY KEY
);

INSERT INTO tmp_weeks (monday_date) VALUES
    ('2026-02-09'),
    ('2026-02-16'),
    ('2026-02-23'),
    ('2026-03-02'),
    ('2026-03-09');

CREATE TEMPORARY TABLE tmp_subscription_patterns (
    user_id BIGINT NOT NULL,
    gym_class_id BIGINT NOT NULL,
    day_offset INT NOT NULL
);

INSERT INTO tmp_subscription_patterns (user_id, gym_class_id, day_offset) VALUES
    (1001, 1, 0), (1001, 2, 1), (1001, 8, 4), (1001, 11, 5),
    (1002, 4, 0), (1002, 3, 3),
    (1003, 5, 1), (1003, 6, 2), (1003, 8, 4),
    (1004, 2, 1), (1004, 4, 4),
    (1005, 1, 0), (1005, 7, 3), (1005, 11, 5),
    (1006, 10, 1), (1006, 3, 3), (1006, 8, 4),
    (1007, 5, 1), (1007, 9, 3),
    (1008, 4, 0), (1008, 2, 1), (1008, 10, 3),
    (1009, 1, 0), (1009, 6, 2), (1009, 7, 3), (1009, 8, 4),
    (1010, 3, 1), (1010, 5, 4);

INSERT INTO gym_class_subscriptions (gym_class_id, user_id, target_date, subscribed_at)
SELECT
    p.gym_class_id,
    p.user_id,
    DATE_ADD(w.monday_date, INTERVAL p.day_offset DAY) AS target_date,
    TIMESTAMP(DATE_ADD(w.monday_date, INTERVAL p.day_offset DAY), COALESCE(gc.start_time, '08:00:00')) - INTERVAL 2 DAY AS subscribed_at
FROM tmp_subscription_patterns p
JOIN tmp_weeks w ON 1 = 1
JOIN gym_classes gc ON gc.id = p.gym_class_id
WHERE DATE_ADD(w.monday_date, INTERVAL p.day_offset DAY) <= '2026-03-09';

DROP TEMPORARY TABLE tmp_subscription_patterns;
DROP TEMPORARY TABLE tmp_weeks;

UPDATE gym_classes gc
SET enrolled = (
    SELECT COUNT(*)
    FROM gym_class_subscriptions gcs
    WHERE gcs.gym_class_id = gc.id
      AND gcs.target_date = gc.date
);

INSERT INTO routine_assignments (id, user_id, routine_template_id, start_date, end_date) VALUES
    (1, 1001, 1, '2026-02-10', '2026-03-31'),
    (2, 1002, 2, '2026-02-17', '2026-03-28'),
    (3, 1003, 3, '2026-02-12', '2026-03-26'),
    (4, 1004, 4, '2026-02-18', '2026-03-30'),
    (5, 1005, 5, '2026-02-15', '2026-03-27'),
    (6, 1006, 1, '2026-02-20', '2026-03-31'),
    (7, 1007, 2, '2026-02-14', '2026-03-29'),
    (8, 1008, 3, '2026-02-21', '2026-03-30'),
    (9, 1009, 4, '2026-02-11', '2026-03-31'),
    (10, 1010, 5, '2026-02-16', '2026-03-25'),
    (101, 1001, 6, '2025-12-01', '2026-01-31'),
    (102, 1003, 2, '2025-11-10', '2026-02-05'),
    (103, 1005, 4, '2025-12-15', '2026-02-12'),
    (104, 1008, 1, '2025-11-20', '2026-02-15'),
    (201, 1002, 5, '2026-04-01', '2026-05-10'),
    (202, 1006, 6, '2026-04-05', '2026-05-20');

INSERT INTO routine_assignment_days (routine_assignment_id, day) VALUES
    (1, 'LUNES'), (1, 'MIERCOLES'), (1, 'VIERNES'),
    (2, 'MARTES'), (2, 'JUEVES'),
    (3, 'LUNES'), (3, 'MIERCOLES'), (3, 'VIERNES'),
    (4, 'MARTES'), (4, 'JUEVES'), (4, 'SABADO'),
    (5, 'LUNES'), (5, 'MIERCOLES'), (5, 'VIERNES'),
    (6, 'MARTES'), (6, 'JUEVES'), (6, 'SABADO'),
    (7, 'LUNES'), (7, 'MIERCOLES'),
    (8, 'MARTES'), (8, 'JUEVES'), (8, 'SABADO'),
    (9, 'LUNES'), (9, 'MIERCOLES'), (9, 'VIERNES'),
    (10, 'MARTES'), (10, 'JUEVES'), (10, 'VIERNES'),
    (101, 'MARTES'), (101, 'JUEVES'),
    (102, 'LUNES'), (102, 'MIERCOLES'),
    (103, 'MARTES'), (103, 'JUEVES'),
    (104, 'LUNES'), (104, 'MIERCOLES'), (104, 'VIERNES'),
    (201, 'LUNES'), (201, 'MIERCOLES'), (201, 'VIERNES'),
    (202, 'MARTES'), (202, 'JUEVES');

INSERT INTO routine_assignment_exercises (
    assignment_id, exercise_id, exercise_name, sets, prescription_mode,
    reps, weight, min_reps, max_reps, min_weight, max_weight
)
SELECT
    ra.id AS assignment_id,
    e.id AS exercise_id,
    e.name AS exercise_name,
    3 + MOD(ra.id + e.id, 2) AS sets,
    CASE WHEN MOD(ra.id + e.id, 2) = 0 THEN 'FIJO' ELSE 'DINAMICO' END AS prescription_mode,
    CASE WHEN MOD(ra.id + e.id, 2) = 0 THEN 8 + MOD(ra.id + e.id, 6) ELSE NULL END AS reps,
    CASE
        WHEN MOD(ra.id + e.id, 2) = 0 THEN
            CASE WHEN e.machine_id IS NULL THEN 0 ELSE 20 + MOD(ra.id * e.id, 35) END
        ELSE NULL
    END AS weight,
    CASE WHEN MOD(ra.id + e.id, 2) = 1 THEN 8 + MOD(ra.id + e.id, 4) ELSE NULL END AS min_reps,
    CASE WHEN MOD(ra.id + e.id, 2) = 1 THEN 12 + MOD(ra.id + e.id, 5) ELSE NULL END AS max_reps,
    CASE
        WHEN MOD(ra.id + e.id, 2) = 1 THEN
            CASE WHEN e.machine_id IS NULL THEN 0 ELSE 10 + MOD(ra.id + e.id, 15) END
        ELSE NULL
    END AS min_weight,
    CASE
        WHEN MOD(ra.id + e.id, 2) = 1 THEN
            CASE WHEN e.machine_id IS NULL THEN 0 ELSE 25 + MOD(ra.id + e.id, 20) END
        ELSE NULL
    END AS max_weight
FROM routine_assignments ra
JOIN routine_template_exercises rte ON rte.routine_template_id = ra.routine_template_id
JOIN exercises e ON e.id = rte.exercise_id;

INSERT INTO exercise_personal_records (user_id, exercise_id, max_weight, recorded_at) VALUES
    (1001, 1, 85.0, '2026-02-28'),
    (1001, 8, 32.5, '2026-03-04'),
    (1001, 9, 55.0, '2026-03-01'),
    (1003, 4, 150.0, '2026-02-24'),
    (1003, 3, 52.5, '2026-02-27'),
    (1006, 6, 0.0, '2026-02-26'),
    (1006, 21, 0.0, '2026-03-03'),
    (1010, 5, 0.0, '2026-03-02');

INSERT INTO exercise_personal_record_entries (user_id, exercise_id, weight, recorded_at) VALUES
    (1001, 1, 70.0, '2026-01-31'),
    (1001, 1, 77.5, '2026-02-14'),
    (1001, 1, 82.5, '2026-02-21'),
    (1001, 1, 85.0, '2026-02-28'),
    (1001, 8, 24.0, '2026-02-07'),
    (1001, 8, 28.0, '2026-02-20'),
    (1001, 8, 32.5, '2026-03-04'),
    (1001, 9, 45.0, '2026-02-08'),
    (1001, 9, 50.0, '2026-02-22'),
    (1001, 9, 55.0, '2026-03-01'),
    (1003, 4, 120.0, '2026-01-28'),
    (1003, 4, 135.0, '2026-02-10'),
    (1003, 4, 150.0, '2026-02-24'),
    (1003, 3, 40.0, '2026-02-01'),
    (1003, 3, 47.5, '2026-02-16'),
    (1003, 3, 52.5, '2026-02-27'),
    (1006, 6, 0.0, '2026-02-05'),
    (1006, 6, 0.0, '2026-02-26'),
    (1006, 21, 0.0, '2026-02-12'),
    (1006, 21, 0.0, '2026-03-03'),
    (1010, 5, 0.0, '2026-02-09'),
    (1010, 5, 0.0, '2026-03-02');


