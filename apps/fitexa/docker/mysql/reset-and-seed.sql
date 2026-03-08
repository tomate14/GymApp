CREATE DATABASE IF NOT EXISTS fitexa CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE fitexa;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS routine_assignment_exercises;
DROP TABLE IF EXISTS routine_assignment_days;
DROP TABLE IF EXISTS routine_assignments;
DROP TABLE IF EXISTS routine_template_exercises;
DROP TABLE IF EXISTS routine_templates;
DROP TABLE IF EXISTS gym_class_subscriptions;
DROP TABLE IF EXISTS gym_class_days;
DROP TABLE IF EXISTS gym_classes;
DROP TABLE IF EXISTS exercises;
DROP TABLE IF EXISTS machines;
DROP TABLE IF EXISTS activities;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS plans;
SET FOREIGN_KEY_CHECKS = 1;

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
    birth_date DATE NOT NULL,
    user_type VARCHAR(40) NOT NULL,
    plan_id BIGINT NULL,
    registration_date DATE NOT NULL,
    renewal_date DATE NOT NULL,
    payment_status VARCHAR(40) NOT NULL,
    CONSTRAINT fk_users_plan FOREIGN KEY (plan_id) REFERENCES plans(id) ON DELETE SET NULL
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
    subscribed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_class_subscriptions_class FOREIGN KEY (gym_class_id) REFERENCES gym_classes(id) ON DELETE CASCADE,
    CONSTRAINT fk_class_subscriptions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_gym_class_subscriptions_class_user UNIQUE (gym_class_id, user_id)
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
    routine_template_id BIGINT NOT NULL,
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
    (3, 'Libre', 36000.00, 7, 1),
    (4, 'Pase diario', 9000.00, 1, 1);

INSERT INTO users (
    id, first_name, last_name, dni, address, phone, email, birth_date,
    user_type, plan_id, registration_date, renewal_date, payment_status
) VALUES
    (1, 'Lucia', 'Gimenez', '32450987', 'Belgrano 145', '3415011002', 'lucia.gimenez@fitexa.com', '1992-09-05', 'PROFESOR', NULL, '2025-01-15', DATE_ADD(CURDATE(), INTERVAL 20 DAY), 'AL_DIA'),
    (2, 'Diego', 'Ferrero', '29877441', 'San Luis 932', '3416120044', 'diego.ferrero@fitexa.com', '1987-04-14', 'PROFESOR', NULL, '2025-02-10', DATE_ADD(CURDATE(), INTERVAL 25 DAY), 'AL_DIA'),
    (3, 'Agustina', 'Ruiz', '31244876', 'Mitre 1880', '3415772200', 'agustina.ruiz@fitexa.com', '1990-06-25', 'PROFESOR', NULL, '2025-03-01', DATE_ADD(CURDATE(), INTERVAL 18 DAY), 'AL_DIA'),
    (4, 'Nicolas', 'Vega', '30124577', 'Cordoba 522', '3415339981', 'nicolas.vega@fitexa.com', '1988-11-17', 'PROFESOR', NULL, '2025-02-18', DATE_ADD(CURDATE(), INTERVAL 22 DAY), 'AL_DIA'),
    (5, 'Carla', 'Ibanez', '31977551', 'Roca 310', '3415099877', 'carla.ibanez@fitexa.com', '1991-08-30', 'PROFESOR', NULL, '2025-04-08', DATE_ADD(CURDATE(), INTERVAL 27 DAY), 'AL_DIA'),
    (6, 'Pablo', 'Rossi', '28766331', 'Mendoza 640', '3415448877', 'pablo.rossi@fitexa.com', '1986-02-18', 'PROFESOR', NULL, '2025-01-28', DATE_ADD(CURDATE(), INTERVAL 19 DAY), 'AL_DIA');

INSERT INTO users (
    id, first_name, last_name, dni, address, phone, email, birth_date,
    user_type, plan_id, registration_date, renewal_date, payment_status
)
SELECT
    1000 + seq.n AS id,
    ELT(((seq.n - 1) % 25) + 1,
        'Mateo', 'Sofia', 'Tomas', 'Valentina', 'Benjamin',
        'Camila', 'Thiago', 'Martina', 'Franco', 'Julieta',
        'Santino', 'Mora', 'Facundo', 'Catalina', 'Bautista',
        'Olivia', 'Joaquin', 'Lola', 'Ignacio', 'Abril',
        'Lautaro', 'Violeta', 'Bruno', 'Renata', 'Emiliano'
    ) AS first_name,
    ELT(((seq.n - 1) % 20) + 1,
        'Gonzalez', 'Rodriguez', 'Fernandez', 'Lopez', 'Martinez',
        'Perez', 'Gomez', 'Diaz', 'Sanchez', 'Romero',
        'Alvarez', 'Torres', 'Ruiz', 'Silva', 'Flores',
        'Acosta', 'Suarez', 'Molina', 'Castro', 'Ramos'
    ) AS last_name,
    CAST(43000000 + seq.n * 17 AS CHAR) AS dni,
    CONCAT('Av. Entrenamiento ', 100 + seq.n) AS address,
    CONCAT('341', LPAD(5000000 + seq.n * 37, 7, '0')) AS phone,
    CONCAT('alumno', seq.n, '@fitexa.com') AS email,
    DATE_ADD('1988-01-01', INTERVAL seq.n * 137 DAY) AS birth_date,
    'ALUMNO' AS user_type,
    CASE
        WHEN MOD(seq.n, 13) = 0 THEN NULL
        WHEN MOD(seq.n, 6) IN (1, 2) THEN 1
        WHEN MOD(seq.n, 6) IN (3, 4) THEN 2
        ELSE 3
    END AS plan_id,
    DATE_ADD('2025-01-03', INTERVAL seq.n * 7 DAY) AS registration_date,
    CASE
        WHEN MOD(seq.n, 5) = 0 THEN DATE_SUB(CURDATE(), INTERVAL MOD(seq.n, 12) + 1 DAY)
        WHEN MOD(seq.n, 7) = 0 THEN DATE_ADD(CURDATE(), INTERVAL 1 DAY)
        ELSE DATE_ADD(CURDATE(), INTERVAL MOD(seq.n, 20) + 5 DAY)
    END AS renewal_date,
    CASE
        WHEN MOD(seq.n, 5) = 0 THEN 'ADEUDA'
        ELSE 'AL_DIA'
    END AS payment_status
FROM (
    SELECT (t.n * 10 + o.n) + 1 AS n
    FROM (
        SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL
        SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
    ) o
    CROSS JOIN (
        SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
    ) t
    WHERE (t.n * 10 + o.n) + 1 <= 50
) seq;

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
    (1, 'Hipertrofia inicial', DATE_SUB(CURDATE(), INTERVAL 160 DAY)),
    (2, 'Resistencia metabolica', DATE_SUB(CURDATE(), INTERVAL 150 DAY)),
    (3, 'Fuerza tren inferior', DATE_SUB(CURDATE(), INTERVAL 145 DAY)),
    (4, 'Movilidad y core', DATE_SUB(CURDATE(), INTERVAL 130 DAY)),
    (5, 'Acondicionamiento general', DATE_SUB(CURDATE(), INTERVAL 120 DAY)),
    (6, 'Cross training base', DATE_SUB(CURDATE(), INTERVAL 110 DAY)),
    (7, 'Spinning performance', DATE_SUB(CURDATE(), INTERVAL 100 DAY)),
    (8, 'Core y estabilidad', DATE_SUB(CURDATE(), INTERVAL 90 DAY)),
    (9, 'Fuerza torso', DATE_SUB(CURDATE(), INTERVAL 80 DAY)),
    (10, 'Movilidad premium', DATE_SUB(CURDATE(), INTERVAL 70 DAY));

INSERT INTO routine_template_exercises (routine_template_id, exercise_id) VALUES
    (1, 1), (1, 8), (1, 9), (1, 20),
    (2, 2), (2, 13), (2, 14), (2, 15),
    (3, 4), (3, 10), (3, 19), (3, 3),
    (4, 5), (4, 7), (4, 23), (4, 24),
    (5, 3), (5, 11), (5, 12), (5, 18),
    (6, 2), (6, 14), (6, 16), (6, 17),
    (7, 6), (7, 21), (7, 11),
    (8, 5), (8, 7), (8, 11), (8, 23),
    (9, 8), (9, 9), (9, 20), (9, 18),
    (10, 22), (10, 23), (10, 24), (10, 7);

-- Escenario semanal realista para el calendario:
-- 20 clases totales por semana y maximo 2 clases por franja horaria.
INSERT INTO gym_classes (id, class_name, activity_id, max_capacity, enrolled, teacher_id, date, all_day, start_time, end_time) VALUES
    (1, 'Musculacion Morning', 1, NULL, 0, NULL, '2026-03-02', 1, NULL, NULL),
    (2, 'Crossfit Morning', 2, 18, 0, 1, '2026-03-02', 0, '08:00:00', '09:00:00'),
    (3, 'Pilates Suelo', 3, 20, 0, 3, '2026-03-03', 0, '09:30:00', '10:30:00'),
    (4, 'Yoga Flow', 6, 22, 0, 5, '2026-03-03', 0, '09:30:00', '10:30:00'),
    (5, 'Spinning Power', 4, 18, 0, 5, '2026-03-02', 0, '19:00:00', '20:00:00'),
    (6, 'Funcional Express', 5, 16, 0, 3, '2026-03-03', 0, '07:15:00', '08:00:00'),
    (7, 'HIIT Mediodia', 7, 14, 0, 2, '2026-03-04', 0, '12:30:00', '13:15:00'),
    (8, 'Stretching Recovery', 8, 25, 0, 5, '2026-03-07', 1, NULL, NULL),
    (9, 'Core y Movilidad', 8, 20, 0, 6, '2026-03-05', 0, '20:00:00', '20:45:00'),
    (10, 'Box Funcional', 5, 18, 0, 4, '2026-03-06', 0, '19:00:00', '20:00:00'),
    (11, 'Musculacion Intermedio', 1, NULL, 0, NULL, '2026-03-03', 0, '18:00:00', '19:00:00'),
    (12, 'Crossfit Evening', 2, 16, 0, 1, '2026-03-03', 0, '18:00:00', '19:00:00');

INSERT INTO gym_class_days (gym_class_id, day) VALUES
    (1, 'LUNES'), (1, 'MIERCOLES'),
    (2, 'LUNES'), (2, 'MIERCOLES'),
    (3, 'MARTES'), (3, 'JUEVES'),
    (4, 'MARTES'), (4, 'JUEVES'),
    (5, 'LUNES'), (5, 'VIERNES'),
    (6, 'MARTES'), (6, 'VIERNES'),
    (7, 'MIERCOLES'),
    (8, 'SABADO'),
    (9, 'JUEVES'),
    (10, 'VIERNES'),
    (11, 'MARTES'), (11, 'JUEVES'),
    (12, 'MARTES'), (12, 'JUEVES');

CREATE TEMPORARY TABLE tmp_class_candidates AS
SELECT
    gc.id AS gym_class_id,
    u.id AS user_id,
    ROW_NUMBER() OVER (
        PARTITION BY gc.id
        ORDER BY MOD((u.id * 31) + (gc.id * 17), 10007)
    ) AS rn,
    CASE
        WHEN gc.max_capacity IS NULL THEN 22 + MOD(gc.id, 12)
        ELSE GREATEST(6, FLOOR(gc.max_capacity * (0.55 + (MOD(gc.id, 35) / 100))))
    END AS target
FROM gym_classes gc
JOIN users u ON u.user_type = 'ALUMNO';

INSERT INTO gym_class_subscriptions (gym_class_id, user_id, subscribed_at)
SELECT
    c.gym_class_id,
    c.user_id,
    TIMESTAMP(gc.date, COALESCE(gc.start_time, '08:00:00')) - INTERVAL (1 + MOD(c.user_id + c.gym_class_id, 9)) DAY
FROM tmp_class_candidates c
JOIN gym_classes gc ON gc.id = c.gym_class_id
WHERE c.rn <= c.target;

DROP TEMPORARY TABLE tmp_class_candidates;

UPDATE gym_classes gc
SET enrolled = (
    SELECT COUNT(*)
    FROM gym_class_subscriptions gcs
    WHERE gcs.gym_class_id = gc.id
);

CREATE TEMPORARY TABLE tmp_students AS
SELECT
    u.id,
    ROW_NUMBER() OVER (ORDER BY u.id) AS rn
FROM users u
WHERE u.user_type = 'ALUMNO';

INSERT INTO routine_assignments (id, user_id, routine_template_id, start_date, end_date)
SELECT
    ts.rn AS id,
    ts.id AS user_id,
    1 + MOD(ts.rn - 1, 10) AS routine_template_id,
    DATE_SUB(CURDATE(), INTERVAL (10 + MOD(ts.rn, 20)) DAY) AS start_date,
    DATE_ADD(CURDATE(), INTERVAL (20 + MOD(ts.rn, 40)) DAY) AS end_date
FROM tmp_students ts
WHERE ts.rn <= 35;

INSERT INTO routine_assignments (id, user_id, routine_template_id, start_date, end_date)
SELECT
    100 + ts.rn AS id,
    ts.id AS user_id,
    1 + MOD(ts.rn + 2, 10) AS routine_template_id,
    DATE_SUB(CURDATE(), INTERVAL (120 + MOD(ts.rn, 60)) DAY) AS start_date,
    DATE_SUB(CURDATE(), INTERVAL (10 + MOD(ts.rn, 30)) DAY) AS end_date
FROM tmp_students ts
WHERE ts.rn BETWEEN 20 AND 50;

INSERT INTO routine_assignments (id, user_id, routine_template_id, start_date, end_date)
SELECT
    200 + ts.rn AS id,
    ts.id AS user_id,
    1 + MOD(ts.rn + 4, 10) AS routine_template_id,
    DATE_ADD(CURDATE(), INTERVAL (7 + MOD(ts.rn, 25)) DAY) AS start_date,
    DATE_ADD(CURDATE(), INTERVAL (45 + MOD(ts.rn, 45)) DAY) AS end_date
FROM tmp_students ts
WHERE ts.rn <= 15;

DROP TEMPORARY TABLE tmp_students;

INSERT INTO routine_assignment_days (routine_assignment_id, day)
SELECT id, 'LUNES' FROM routine_assignments;
INSERT INTO routine_assignment_days (routine_assignment_id, day)
SELECT id, 'MIERCOLES' FROM routine_assignments WHERE MOD(id, 2) = 0;
INSERT INTO routine_assignment_days (routine_assignment_id, day)
SELECT id, 'VIERNES' FROM routine_assignments WHERE MOD(id, 3) = 0;
INSERT INTO routine_assignment_days (routine_assignment_id, day)
SELECT id, 'MARTES' FROM routine_assignments WHERE MOD(id, 4) = 0;
INSERT INTO routine_assignment_days (routine_assignment_id, day)
SELECT id, 'JUEVES' FROM routine_assignments WHERE MOD(id, 5) = 0;
INSERT INTO routine_assignment_days (routine_assignment_id, day)
SELECT id, 'SABADO' FROM routine_assignments WHERE MOD(id, 7) = 0;

INSERT INTO routine_assignment_exercises (
    assignment_id, exercise_id, exercise_name, sets, prescription_mode,
    reps, weight, min_reps, max_reps, min_weight, max_weight
)
SELECT
    ra.id AS assignment_id,
    e.id AS exercise_id,
    e.name AS exercise_name,
    3 + MOD(ra.id + e.id, 3) AS sets,
    CASE
        WHEN MOD(ra.id + e.id, 2) = 0 THEN 'FIJO'
        ELSE 'DINAMICO'
    END AS prescription_mode,
    CASE
        WHEN MOD(ra.id + e.id, 2) = 0 THEN 8 + MOD(ra.id + e.id, 8)
        ELSE NULL
    END AS reps,
    CASE
        WHEN MOD(ra.id + e.id, 2) = 0 THEN
            CASE
                WHEN e.machine_id IS NULL THEN 0
                ELSE 15 + MOD((ra.id * e.id), 45)
            END
        ELSE NULL
    END AS weight,
    CASE
        WHEN MOD(ra.id + e.id, 2) = 1 THEN 8 + MOD(ra.id + e.id, 6)
        ELSE NULL
    END AS min_reps,
    CASE
        WHEN MOD(ra.id + e.id, 2) = 1 THEN 12 + MOD(ra.id + e.id, 8)
        ELSE NULL
    END AS max_reps,
    CASE
        WHEN MOD(ra.id + e.id, 2) = 1 THEN
            CASE
                WHEN e.machine_id IS NULL THEN 0
                ELSE 10 + MOD((ra.id + e.id), 25)
            END
        ELSE NULL
    END AS min_weight,
    CASE
        WHEN MOD(ra.id + e.id, 2) = 1 THEN
            CASE
                WHEN e.machine_id IS NULL THEN 0
                ELSE 25 + MOD((ra.id + e.id), 35)
            END
        ELSE NULL
    END AS max_weight
FROM routine_assignments ra
JOIN routine_template_exercises rte ON rte.routine_template_id = ra.routine_template_id
JOIN exercises e ON e.id = rte.exercise_id;
