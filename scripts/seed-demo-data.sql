## Seed manual de demo para Fitexa
## No se ejecuta automaticamente.
## Requiere que bootstrap.sql ya haya creado la estructura base.
##
## Uso sugerido:
## docker exec -i fitexa-mysql mysql -uroot -proot < scripts/seed-demo-data.sql
##
## Credenciales:
## - admin@fitexa.com / admin
## - Todos los demas usuarios usan como password su propio email

USE fitexa;

SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;
SET collation_connection = 'utf8mb4_unicode_ci';

START TRANSACTION;

SET @today := CURDATE();
SET @admin_email := 'admin@fitexa.com';
SET @admin_user_id := (SELECT id FROM users WHERE email = @admin_email LIMIT 1);

SET @week_start := DATE_SUB(@today, INTERVAL WEEKDAY(@today) DAY);
SET @mon_prev2 := DATE_SUB(@week_start, INTERVAL 14 DAY);
SET @tue_prev2 := DATE_ADD(@mon_prev2, INTERVAL 1 DAY);
SET @wed_prev2 := DATE_ADD(@mon_prev2, INTERVAL 2 DAY);
SET @thu_prev2 := DATE_ADD(@mon_prev2, INTERVAL 3 DAY);
SET @fri_prev2 := DATE_ADD(@mon_prev2, INTERVAL 4 DAY);

SET @mon_prev1 := DATE_SUB(@week_start, INTERVAL 7 DAY);
SET @tue_prev1 := DATE_ADD(@mon_prev1, INTERVAL 1 DAY);
SET @wed_prev1 := DATE_ADD(@mon_prev1, INTERVAL 2 DAY);
SET @thu_prev1 := DATE_ADD(@mon_prev1, INTERVAL 3 DAY);
SET @fri_prev1 := DATE_ADD(@mon_prev1, INTERVAL 4 DAY);

SET @mon_this := @week_start;
SET @tue_this := DATE_ADD(@mon_this, INTERVAL 1 DAY);
SET @wed_this := DATE_ADD(@mon_this, INTERVAL 2 DAY);
SET @thu_this := DATE_ADD(@mon_this, INTERVAL 3 DAY);
SET @fri_this := DATE_ADD(@mon_this, INTERVAL 4 DAY);

SET @mon_next1 := DATE_ADD(@week_start, INTERVAL 7 DAY);
SET @tue_next1 := DATE_ADD(@mon_next1, INTERVAL 1 DAY);
SET @wed_next1 := DATE_ADD(@mon_next1, INTERVAL 2 DAY);
SET @thu_next1 := DATE_ADD(@mon_next1, INTERVAL 3 DAY);
SET @fri_next1 := DATE_ADD(@mon_next1, INTERVAL 4 DAY);

DELETE FROM gym_class_subscriptions;
DELETE FROM gym_class_days;
DELETE FROM gym_classes;
DELETE FROM exercise_personal_record_entries;
DELETE FROM exercise_personal_records;
DELETE FROM routine_assignment_exercises;
DELETE FROM routine_assignment_days;
DELETE FROM routine_assignments;
DELETE FROM routine_template_exercises;
DELETE FROM routine_templates;
DELETE FROM user_payment_movements WHERE @admin_user_id IS NULL OR user_id <> @admin_user_id;
DELETE FROM user_activities;
DELETE FROM exercises;
DELETE FROM machines;
DELETE FROM activities;
DELETE FROM plans;
DELETE FROM users WHERE email <> @admin_email;

DELETE FROM role_menu_permissions
WHERE role_id IN (
    SELECT id
    FROM roles
    WHERE name IN ('Recepcion', 'Coordinacion')
);

DELETE FROM roles
WHERE name IN ('Recepcion', 'Coordinacion')
  AND system_role = 0;

INSERT INTO plans (name, price, max_days_per_week, active) VALUES
('Plan Libre', 42000.00, 7, 1),
('Plan 4 veces', 36000.00, 4, 1),
('Plan 3 veces', 32000.00, 3, 1),
('Plan 2 veces', 28000.00, 2, 1);

INSERT INTO activities (name, description) VALUES
('Musculacion', 'Entrenamiento de fuerza y acondicionamiento en sala.'),
('Funcional', 'Clases dinamicas de fuerza, movilidad y resistencia.'),
('Crossfit', 'Entrenamientos intensos de fuerza, tecnica y condicion fisica.'),
('Yoga', 'Practicas guiadas de movilidad, respiracion y control corporal.'),
('Pilates', 'Trabajo de estabilidad, postura y control del movimiento.'),
('Boxeo', 'Clases tecnicas y de acondicionamiento con foco en boxeo.'),
('Spinning', 'Clases de ciclismo indoor por objetivos de resistencia.');

INSERT INTO machines (name, quantity, photo_url) VALUES
('Mancuernas', 10, 'https://images.unsplash.com/photo-1517836357463-d25dfeac3438?auto=format&fit=crop&w=900&q=80'),
('Barra Olimpica', 6, 'https://images.unsplash.com/photo-1534438327276-14e5300c3a48?auto=format&fit=crop&w=900&q=80'),
('Banco Plano', 4, 'https://images.unsplash.com/photo-1579758629938-03607ccdbaba?auto=format&fit=crop&w=900&q=80'),
('Polea Alta', 3, 'https://images.unsplash.com/photo-1583454110551-21f2fa2afe61?auto=format&fit=crop&w=900&q=80'),
('Prensa 45', 2, 'https://images.unsplash.com/photo-1517838277536-f5f99be501cd?auto=format&fit=crop&w=900&q=80'),
('Remo Bajo', 2, 'https://images.unsplash.com/photo-1598971639058-fab3c3109a00?auto=format&fit=crop&w=900&q=80'),
('Bicicleta Spinning', 12, 'https://images.unsplash.com/photo-1518611012118-696072aa579a?auto=format&fit=crop&w=900&q=80'),
('Bolsa de Boxeo', 4, 'https://images.unsplash.com/photo-1549719386-74dfcbf7dbed?auto=format&fit=crop&w=900&q=80'),
('Reformer', 6, 'https://images.unsplash.com/photo-1518310383802-640c2de311b2?auto=format&fit=crop&w=900&q=80'),
('Cinta', 5, 'https://images.unsplash.com/photo-1599058917765-a780eda07a3e?auto=format&fit=crop&w=900&q=80');

INSERT INTO exercises (name, activity_id, machine_id, video_url) VALUES
('Press banca con barra', (SELECT id FROM activities WHERE name = 'Musculacion'), (SELECT id FROM machines WHERE name = 'Banco Plano'), 'https://www.youtube.com/watch?v=rT7DgCr-3pg'),
('Press inclinado con mancuernas', (SELECT id FROM activities WHERE name = 'Musculacion'), (SELECT id FROM machines WHERE name = 'Mancuernas'), 'https://www.youtube.com/watch?v=8iPEnn-ltC8'),
('Aperturas con mancuernas', (SELECT id FROM activities WHERE name = 'Musculacion'), (SELECT id FROM machines WHERE name = 'Mancuernas'), 'https://www.youtube.com/watch?v=eozdVDA78K0'),
('Remo sentado en polea', (SELECT id FROM activities WHERE name = 'Musculacion'), (SELECT id FROM machines WHERE name = 'Remo Bajo'), 'https://www.youtube.com/watch?v=GZbfZ033f74'),
('Jalon al pecho', (SELECT id FROM activities WHERE name = 'Musculacion'), (SELECT id FROM machines WHERE name = 'Polea Alta'), 'https://www.youtube.com/watch?v=CAwf7n6Luuc'),
('Peso muerto rumano', (SELECT id FROM activities WHERE name = 'Musculacion'), (SELECT id FROM machines WHERE name = 'Barra Olimpica'), 'https://www.youtube.com/watch?v=2SHsk9AzdjA'),
('Prensa 45', (SELECT id FROM activities WHERE name = 'Musculacion'), (SELECT id FROM machines WHERE name = 'Prensa 45'), 'https://www.youtube.com/watch?v=IZxyjW7MPJQ'),
('Sentadilla goblet', (SELECT id FROM activities WHERE name = 'Funcional'), (SELECT id FROM machines WHERE name = 'Mancuernas'), 'https://www.youtube.com/watch?v=6xwGFn-J_Qw'),
('Caminata en cinta inclinada', (SELECT id FROM activities WHERE name = 'Funcional'), (SELECT id FROM machines WHERE name = 'Cinta'), 'https://www.youtube.com/watch?v=_kLBi8tF6K4'),
('Thruster con barra', (SELECT id FROM activities WHERE name = 'Crossfit'), (SELECT id FROM machines WHERE name = 'Barra Olimpica'), 'https://www.youtube.com/watch?v=L219ltL15zk'),
('Remo metabolico', (SELECT id FROM activities WHERE name = 'Crossfit'), (SELECT id FROM machines WHERE name = 'Remo Bajo'), 'https://www.youtube.com/watch?v=7cPhKwRF0fI'),
('Trabajo en reformer', (SELECT id FROM activities WHERE name = 'Pilates'), (SELECT id FROM machines WHERE name = 'Reformer'), 'https://www.youtube.com/watch?v=E9fR9X0x8v8'),
('Bolsa tecnica 3 rounds', (SELECT id FROM activities WHERE name = 'Boxeo'), (SELECT id FROM machines WHERE name = 'Bolsa de Boxeo'), 'https://www.youtube.com/watch?v=9hC5x7X1lE0'),
('Bloque de resistencia 45 min', (SELECT id FROM activities WHERE name = 'Spinning'), (SELECT id FROM machines WHERE name = 'Bicicleta Spinning'), 'https://www.youtube.com/watch?v=F1B9Fk_SgI0'),
('Saludo al sol guiado', (SELECT id FROM activities WHERE name = 'Yoga'), NULL, 'https://www.youtube.com/watch?v=6IUy0g7cY8s');

INSERT INTO roles (name, description, system_role) VALUES
('Recepcion', 'Rol operativo para recepcion y atencion diaria.', 0),
('Coordinacion', 'Rol de coordinacion para seguimiento comercial y operativo.', 0);

SET @admin_role_id := (SELECT id FROM roles WHERE name = 'Administrador' LIMIT 1);
SET @teacher_role_id := (SELECT id FROM roles WHERE name = 'Profesor' LIMIT 1);
SET @student_role_id := (SELECT id FROM roles WHERE name = 'Alumno' LIMIT 1);
SET @reception_role_id := (SELECT id FROM roles WHERE name = 'Recepcion' LIMIT 1);
SET @coord_role_id := (SELECT id FROM roles WHERE name = 'Coordinacion' LIMIT 1);

DELETE FROM role_menu_permissions WHERE role_id IN (@reception_role_id, @coord_role_id);

INSERT INTO role_menu_permissions (role_id, menu_item) VALUES
(@reception_role_id, 'AGENDA'),
(@reception_role_id, 'USERS'),
(@reception_role_id, 'PLANS'),
(@reception_role_id, 'CLASSES'),
(@coord_role_id, 'DASHBOARD'),
(@coord_role_id, 'AGENDA'),
(@coord_role_id, 'USERS'),
(@coord_role_id, 'PLANS'),
(@coord_role_id, 'ACTIVITIES'),
(@coord_role_id, 'CLASSES'),
(@coord_role_id, 'ROUTINES'),
(@coord_role_id, 'EXERCISES'),
(@coord_role_id, 'MACHINES');
INSERT INTO users (
    first_name, last_name, dni, address, phone, email, password, must_change_password,
    birth_date, user_type, role_id, plan_id, registration_date, renewal_date
) VALUES
('Lucia', 'Gimenez', '30111222', 'Av. Libertador 1450', '1130000001', 'lucia.gimenez@fitexa.com', '$2a$10$OYQj9E8HbzUuNtzmhI2bzOVKRT0ewOwwYI2VM93uHvbtsEztMurAm', 0, '1989-04-12', 'PROFESOR', @teacher_role_id, NULL, DATE_SUB(@today, INTERVAL 420 DAY), DATE_ADD(@today, INTERVAL 365 DAY)),
('Diego', 'Ferrero', '29888777', 'Olleros 845', '1130000002', 'diego.ferrero@fitexa.com', '$2a$10$x5LYmGNnflLEcx/jcsZA5.kpuicya6gvIvmU6vp47ULx2PAwTJdVW', 0, '1987-08-21', 'PROFESOR', @teacher_role_id, NULL, DATE_SUB(@today, INTERVAL 390 DAY), DATE_ADD(@today, INTERVAL 365 DAY)),
('Carla', 'Ibanez', '31222444', 'Migueletes 1120', '1130000003', 'carla.ibanez@fitexa.com', '$2a$10$isqfFNR8tgSNAu6Br8KrGe8u1e.8D2EsSUZJ/.5L39ZoC3TDCPL4m', 0, '1991-02-09', 'PROFESOR', @teacher_role_id, NULL, DATE_SUB(@today, INTERVAL 360 DAY), DATE_ADD(@today, INTERVAL 365 DAY)),
('Pablo', 'Rossi', '28999111', 'La Pampa 2130', '1130000004', 'pablo.rossi@fitexa.com', '$2a$10$Lfe7o1W3r9iHPQ8robXbJ.MKVzCOf50TXMyl7jM2D0ABI1aLEl5h.', 0, '1985-11-17', 'PROFESOR', @teacher_role_id, NULL, DATE_SUB(@today, INTERVAL 520 DAY), DATE_ADD(@today, INTERVAL 365 DAY)),
('Sofia', 'Mendez', '33555111', 'Quesada 980', '1130000005', 'sofia.mendez@fitexa.com', '$2a$10$MtYXT.14viyH80oWD6nRb.ltBGYzFUdlGo3wDbbtyQFIZiMu9r4Oq', 0, '1993-05-20', 'PROFESOR', @reception_role_id, NULL, DATE_SUB(@today, INTERVAL 210 DAY), DATE_ADD(@today, INTERVAL 365 DAY)),
('Tomas', 'Suarez', '32777111', 'Mendoza 440', '1130000006', 'tomas.suarez@fitexa.com', '$2a$10$h4c/ESEhex52NPP/WptRuu68Gj0LWQezVMjlXC9mF/80yDcUmAb4O', 0, '1990-09-14', 'PROFESOR', @coord_role_id, NULL, DATE_SUB(@today, INTERVAL 180 DAY), DATE_ADD(@today, INTERVAL 365 DAY)),
('Maxi', 'Roselli', '35123123', 'Juramento 2211', '1144441001', 'maxiroselli@gmail.com', '$2a$10$xzKw/a6FObhtNKuFYEbWOeECVEYb5xPyt3u3p9sbTGdjYhe3Kk4nO', 0, '1995-07-04', 'ALUMNO', @student_role_id, (SELECT id FROM plans WHERE name = 'Plan Libre'), DATE_SUB(@today, INTERVAL 240 DAY), DATE_ADD(@today, INTERVAL 10 DAY)),
('Valentina', 'Lopez', '37222999', 'Monroe 1890', '1144441002', 'valentina.lopez@gmail.com', '$2a$10$ekzTX7L44N9PFJ1P.dcg7.E8ZB6HzUIF/lvXKq3c7I5z/cBBtr4Bi', 0, '1998-03-13', 'ALUMNO', @student_role_id, (SELECT id FROM plans WHERE name = 'Plan 3 veces'), DATE_SUB(@today, INTERVAL 190 DAY), DATE_ADD(@today, INTERVAL 6 DAY)),
('Nicolas', 'Romero', '36666222', 'Cabildo 900', '1144441003', 'nicolas.romero@gmail.com', '$2a$10$TSjNP2M9ERQ/I7G32cJ7q.eGuEtMauZ2Ac7R1bBwjZ5YCbJASgeA2', 0, '1994-01-22', 'ALUMNO', @student_role_id, (SELECT id FROM plans WHERE name = 'Plan 4 veces'), DATE_SUB(@today, INTERVAL 170 DAY), DATE_ADD(@today, INTERVAL 12 DAY)),
('Camila', 'Torres', '38999123', '11 de Septiembre 730', '1144441004', 'camila.torres@gmail.com', '$2a$10$.c.nlE5jFabAe2ueiMikeuMu0dQAnNDqYLVlQQA5K445535hxwjIW', 0, '2000-06-18', 'ALUMNO', @student_role_id, (SELECT id FROM plans WHERE name = 'Plan 2 veces'), DATE_SUB(@today, INTERVAL 120 DAY), DATE_ADD(@today, INTERVAL 3 DAY)),
('Federico', 'Gomez', '34111888', 'Cramer 541', '1144441005', 'federico.gomez@gmail.com', '$2a$10$HpDS/oCU/j0qX2SSUvA5s.YJQgChyKeqEulBj.m1Hq8GFRU98Xy/.', 0, '1992-12-07', 'ALUMNO', @student_role_id, (SELECT id FROM plans WHERE name = 'Plan 3 veces'), DATE_SUB(@today, INTERVAL 95 DAY), DATE_ADD(@today, INTERVAL 1 DAY)),
('Julieta', 'Castro', '37777999', 'Moldes 1640', '1144441006', 'julieta.castro@gmail.com', '$2a$10$Bd9ifF6JG/xx6Zvce5SESe44tj1TcUZkzWZbRCEbOzSkdZVGTHjeO', 0, '1997-09-09', 'ALUMNO', @student_role_id, (SELECT id FROM plans WHERE name = 'Plan 2 veces'), DATE_SUB(@today, INTERVAL 80 DAY), DATE_SUB(@today, INTERVAL 5 DAY)),
('Bruno', 'Martinez', '36000111', 'Virrey del Pino 1500', '1144441007', 'bruno.martinez@gmail.com', '$2a$10$wQGVp4zDAD8YM7TiiRXrjOlxYs.i6NSJHW65O5fOXnpZsKDno3hEG', 0, '1993-11-01', 'ALUMNO', @student_role_id, (SELECT id FROM plans WHERE name = 'Plan Libre'), DATE_SUB(@today, INTERVAL 300 DAY), DATE_ADD(@today, INTERVAL 14 DAY)),
('Agustina', 'Ruiz', '39555222', 'Arcos 2010', '1144441008', 'agustina.ruiz@gmail.com', '$2a$10$r5PrlqyTal/lu4tPcl9yj.rG0GwbAPS1vg5IWdo5L6Jk9dXGw70Be', 0, '2001-04-26', 'ALUMNO', @student_role_id, (SELECT id FROM plans WHERE name = 'Plan 4 veces'), DATE_SUB(@today, INTERVAL 60 DAY), DATE_SUB(@today, INTERVAL 2 DAY));

INSERT INTO user_activities (user_id, activity_id)
SELECT u.id, a.id
FROM users u
JOIN activities a ON a.name IN ('Musculacion', 'Crossfit')
WHERE u.email = 'maxiroselli@gmail.com';

INSERT INTO user_activities (user_id, activity_id)
SELECT u.id, a.id
FROM users u
JOIN activities a ON a.name IN ('Yoga', 'Pilates')
WHERE u.email = 'valentina.lopez@gmail.com';

INSERT INTO user_activities (user_id, activity_id)
SELECT u.id, a.id
FROM users u
JOIN activities a ON a.name IN ('Boxeo', 'Funcional')
WHERE u.email = 'nicolas.romero@gmail.com';

INSERT INTO user_activities (user_id, activity_id)
SELECT u.id, a.id
FROM users u
JOIN activities a ON a.name IN ('Spinning', 'Yoga')
WHERE u.email = 'camila.torres@gmail.com';

INSERT INTO user_activities (user_id, activity_id)
SELECT u.id, a.id
FROM users u
JOIN activities a ON a.name IN ('Musculacion', 'Funcional')
WHERE u.email = 'federico.gomez@gmail.com';

INSERT INTO user_activities (user_id, activity_id)
SELECT u.id, a.id
FROM users u
JOIN activities a ON a.name IN ('Pilates', 'Spinning')
WHERE u.email = 'julieta.castro@gmail.com';

INSERT INTO user_activities (user_id, activity_id)
SELECT u.id, a.id
FROM users u
JOIN activities a ON a.name IN ('Crossfit', 'Musculacion', 'Boxeo')
WHERE u.email = 'bruno.martinez@gmail.com';

INSERT INTO user_activities (user_id, activity_id)
SELECT u.id, a.id
FROM users u
JOIN activities a ON a.name IN ('Yoga', 'Pilates', 'Funcional')
WHERE u.email = 'agustina.ruiz@gmail.com';

SET @lucia_id := (SELECT id FROM users WHERE email = 'lucia.gimenez@fitexa.com' LIMIT 1);
SET @diego_id := (SELECT id FROM users WHERE email = 'diego.ferrero@fitexa.com' LIMIT 1);
SET @carla_id := (SELECT id FROM users WHERE email = 'carla.ibanez@fitexa.com' LIMIT 1);
SET @pablo_id := (SELECT id FROM users WHERE email = 'pablo.rossi@fitexa.com' LIMIT 1);

SET @max_id := (SELECT id FROM users WHERE email = 'maxiroselli@gmail.com' LIMIT 1);
SET @vale_id := (SELECT id FROM users WHERE email = 'valentina.lopez@gmail.com' LIMIT 1);
SET @nico_id := (SELECT id FROM users WHERE email = 'nicolas.romero@gmail.com' LIMIT 1);
SET @cami_id := (SELECT id FROM users WHERE email = 'camila.torres@gmail.com' LIMIT 1);
SET @fede_id := (SELECT id FROM users WHERE email = 'federico.gomez@gmail.com' LIMIT 1);
SET @juli_id := (SELECT id FROM users WHERE email = 'julieta.castro@gmail.com' LIMIT 1);
SET @bruno_id := (SELECT id FROM users WHERE email = 'bruno.martinez@gmail.com' LIMIT 1);
SET @agus_id := (SELECT id FROM users WHERE email = 'agustina.ruiz@gmail.com' LIMIT 1);
INSERT INTO gym_classes (class_name, activity_id, max_capacity, enrolled, teacher_id, date, all_day, start_time, end_time) VALUES
('Crossfit Morning', (SELECT id FROM activities WHERE name = 'Crossfit'), 18, 0, @lucia_id, DATE_SUB(@today, INTERVAL 28 DAY), 0, '08:00:00', '09:00:00'),
('Yoga Flow', (SELECT id FROM activities WHERE name = 'Yoga'), 20, 0, @carla_id, DATE_SUB(@today, INTERVAL 28 DAY), 0, '19:00:00', '20:00:00'),
('Pilates Control', (SELECT id FROM activities WHERE name = 'Pilates'), 14, 0, @carla_id, DATE_SUB(@today, INTERVAL 28 DAY), 0, '10:00:00', '11:00:00'),
('Box Funcional', (SELECT id FROM activities WHERE name = 'Boxeo'), 16, 0, @diego_id, DATE_SUB(@today, INTERVAL 28 DAY), 0, '20:00:00', '21:00:00'),
('Spinning Power', (SELECT id FROM activities WHERE name = 'Spinning'), 24, 0, @pablo_id, DATE_SUB(@today, INTERVAL 28 DAY), 0, '18:00:00', '19:00:00'),
('Musculacion Guiada', (SELECT id FROM activities WHERE name = 'Musculacion'), 18, 0, @lucia_id, DATE_SUB(@today, INTERVAL 28 DAY), 0, '18:00:00', '19:00:00'),
('Funcional Noon', (SELECT id FROM activities WHERE name = 'Funcional'), 16, 0, @diego_id, DATE_SUB(@today, INTERVAL 28 DAY), 0, '13:00:00', '14:00:00');

INSERT INTO gym_class_days (gym_class_id, day)
SELECT id, 'LUNES' FROM gym_classes WHERE class_name IN ('Crossfit Morning', 'Pilates Control', 'Musculacion Guiada', 'Funcional Noon')
UNION ALL
SELECT id, 'MARTES' FROM gym_classes WHERE class_name IN ('Yoga Flow', 'Box Funcional', 'Spinning Power')
UNION ALL
SELECT id, 'MIERCOLES' FROM gym_classes WHERE class_name IN ('Crossfit Morning', 'Pilates Control', 'Musculacion Guiada', 'Funcional Noon')
UNION ALL
SELECT id, 'JUEVES' FROM gym_classes WHERE class_name IN ('Yoga Flow', 'Box Funcional', 'Spinning Power')
UNION ALL
SELECT id, 'VIERNES' FROM gym_classes WHERE class_name IN ('Crossfit Morning', 'Musculacion Guiada');

SET @crossfit_class_id := (SELECT id FROM gym_classes WHERE class_name = 'Crossfit Morning' LIMIT 1);
SET @yoga_class_id := (SELECT id FROM gym_classes WHERE class_name = 'Yoga Flow' LIMIT 1);
SET @pilates_class_id := (SELECT id FROM gym_classes WHERE class_name = 'Pilates Control' LIMIT 1);
SET @box_class_id := (SELECT id FROM gym_classes WHERE class_name = 'Box Funcional' LIMIT 1);
SET @spin_class_id := (SELECT id FROM gym_classes WHERE class_name = 'Spinning Power' LIMIT 1);
SET @muscu_class_id := (SELECT id FROM gym_classes WHERE class_name = 'Musculacion Guiada' LIMIT 1);
SET @funcional_class_id := (SELECT id FROM gym_classes WHERE class_name = 'Funcional Noon' LIMIT 1);

INSERT INTO gym_class_subscriptions (gym_class_id, user_id, target_date, subscribed_at, attendance_present, attendance_marked_at) VALUES
(@muscu_class_id, @max_id, @mon_prev2, TIMESTAMP(@mon_prev2, '09:15:00'), 1, TIMESTAMP(@mon_prev2, '19:05:00')),
(@muscu_class_id, @max_id, @wed_prev2, TIMESTAMP(@wed_prev2, '09:15:00'), 1, TIMESTAMP(@wed_prev2, '19:07:00')),
(@muscu_class_id, @max_id, @mon_prev1, TIMESTAMP(@mon_prev1, '09:20:00'), 1, TIMESTAMP(@mon_prev1, '19:03:00')),
(@muscu_class_id, @max_id, @wed_prev1, TIMESTAMP(@wed_prev1, '09:20:00'), 0, TIMESTAMP(@wed_prev1, '19:10:00')),
(@muscu_class_id, @max_id, @mon_this, TIMESTAMP(@mon_this, '09:10:00'), 1, TIMESTAMP(@mon_this, '19:02:00')),
(@crossfit_class_id, @bruno_id, @mon_prev2, TIMESTAMP(@mon_prev2, '07:30:00'), 1, TIMESTAMP(@mon_prev2, '09:01:00')),
(@crossfit_class_id, @bruno_id, @fri_prev2, TIMESTAMP(@fri_prev2, '07:30:00'), 1, TIMESTAMP(@fri_prev2, '09:00:00')),
(@crossfit_class_id, @bruno_id, @mon_prev1, TIMESTAMP(@mon_prev1, '07:32:00'), 1, TIMESTAMP(@mon_prev1, '09:01:00')),
(@crossfit_class_id, @bruno_id, @fri_prev1, TIMESTAMP(@fri_prev1, '07:32:00'), 0, TIMESTAMP(@fri_prev1, '09:04:00')),
(@crossfit_class_id, @bruno_id, @mon_this, TIMESTAMP(@mon_this, '07:35:00'), 1, TIMESTAMP(@mon_this, '09:02:00')),
(@yoga_class_id, @vale_id, @tue_prev2, TIMESTAMP(@tue_prev2, '10:10:00'), 1, TIMESTAMP(@tue_prev2, '20:02:00')),
(@yoga_class_id, @vale_id, @thu_prev2, TIMESTAMP(@thu_prev2, '10:10:00'), 1, TIMESTAMP(@thu_prev2, '20:01:00')),
(@yoga_class_id, @vale_id, @tue_prev1, TIMESTAMP(@tue_prev1, '10:12:00'), 1, TIMESTAMP(@tue_prev1, '20:00:00')),
(@yoga_class_id, @vale_id, @thu_prev1, TIMESTAMP(@thu_prev1, '10:12:00'), 1, TIMESTAMP(@thu_prev1, '20:00:00')),
(@pilates_class_id, @vale_id, @mon_prev1, TIMESTAMP(@mon_prev1, '08:25:00'), 1, TIMESTAMP(@mon_prev1, '11:03:00')),
(@pilates_class_id, @juli_id, @wed_prev1, TIMESTAMP(@wed_prev1, '08:30:00'), 1, TIMESTAMP(@wed_prev1, '11:02:00')),
(@pilates_class_id, @juli_id, @mon_this, TIMESTAMP(@mon_this, '08:30:00'), 0, TIMESTAMP(@mon_this, '11:00:00')),
(@box_class_id, @nico_id, @tue_prev2, TIMESTAMP(@tue_prev2, '15:00:00'), 1, TIMESTAMP(@tue_prev2, '21:04:00')),
(@box_class_id, @nico_id, @thu_prev2, TIMESTAMP(@thu_prev2, '15:00:00'), 1, TIMESTAMP(@thu_prev2, '21:02:00')),
(@box_class_id, @nico_id, @tue_prev1, TIMESTAMP(@tue_prev1, '15:10:00'), 1, TIMESTAMP(@tue_prev1, '21:05:00')),
(@spin_class_id, @cami_id, @tue_prev2, TIMESTAMP(@tue_prev2, '11:20:00'), 1, TIMESTAMP(@tue_prev2, '19:03:00')),
(@spin_class_id, @cami_id, @thu_prev2, TIMESTAMP(@thu_prev2, '11:20:00'), 1, TIMESTAMP(@thu_prev2, '19:00:00')),
(@spin_class_id, @cami_id, @tue_prev1, TIMESTAMP(@tue_prev1, '11:25:00'), 1, TIMESTAMP(@tue_prev1, '19:01:00')),
(@funcional_class_id, @fede_id, @mon_prev2, TIMESTAMP(@mon_prev2, '10:00:00'), 1, TIMESTAMP(@mon_prev2, '14:00:00')),
(@funcional_class_id, @fede_id, @wed_prev2, TIMESTAMP(@wed_prev2, '10:00:00'), 1, TIMESTAMP(@wed_prev2, '14:01:00')),
(@funcional_class_id, @fede_id, @mon_prev1, TIMESTAMP(@mon_prev1, '10:02:00'), 1, TIMESTAMP(@mon_prev1, '14:02:00')),
(@funcional_class_id, @agus_id, @wed_prev1, TIMESTAMP(@wed_prev1, '10:05:00'), 1, TIMESTAMP(@wed_prev1, '14:00:00')),
(@muscu_class_id, @fede_id, @fri_prev1, TIMESTAMP(@fri_prev1, '11:00:00'), 1, TIMESTAMP(@fri_prev1, '19:05:00')),
(@yoga_class_id, @agus_id, @thu_prev1, TIMESTAMP(@thu_prev1, '11:00:00'), 1, TIMESTAMP(@thu_prev1, '20:00:00')),
(@muscu_class_id, @max_id, @wed_this, TIMESTAMP(@today, '09:30:00'), NULL, NULL),
(@box_class_id, @nico_id, @thu_this, TIMESTAMP(@today, '09:35:00'), NULL, NULL),
(@spin_class_id, @cami_id, @thu_this, TIMESTAMP(@today, '09:35:00'), NULL, NULL),
(@crossfit_class_id, @bruno_id, @wed_this, TIMESTAMP(@today, '08:00:00'), NULL, NULL),
(@yoga_class_id, @vale_id, @thu_this, TIMESTAMP(@today, '08:10:00'), NULL, NULL),
(@pilates_class_id, @juli_id, @wed_this, TIMESTAMP(@today, '08:10:00'), NULL, NULL),
(@funcional_class_id, @agus_id, @wed_this, TIMESTAMP(@today, '08:20:00'), NULL, NULL),
(@muscu_class_id, @fede_id, @fri_this, TIMESTAMP(@today, '09:00:00'), NULL, NULL),
(@muscu_class_id, @max_id, @mon_next1, TIMESTAMP(@today, '09:45:00'), NULL, NULL),
(@crossfit_class_id, @bruno_id, @mon_next1, TIMESTAMP(@today, '09:45:00'), NULL, NULL),
(@spin_class_id, @cami_id, @tue_next1, TIMESTAMP(@today, '09:45:00'), NULL, NULL);

INSERT INTO routine_templates (name, created_at) VALUES
('Empuje Superior', DATE_SUB(@today, INTERVAL 45 DAY)),
('Espalda y Traccion', DATE_SUB(@today, INTERVAL 45 DAY)),
('Piernas y Potencia', DATE_SUB(@today, INTERVAL 40 DAY)),
('Full Body Funcional', DATE_SUB(@today, INTERVAL 35 DAY)),
('Core y Resistencia', DATE_SUB(@today, INTERVAL 20 DAY));

SET @tpl_push_id := (SELECT id FROM routine_templates WHERE name = 'Empuje Superior' LIMIT 1);
SET @tpl_pull_id := (SELECT id FROM routine_templates WHERE name = 'Espalda y Traccion' LIMIT 1);
SET @tpl_legs_id := (SELECT id FROM routine_templates WHERE name = 'Piernas y Potencia' LIMIT 1);
SET @tpl_full_id := (SELECT id FROM routine_templates WHERE name = 'Full Body Funcional' LIMIT 1);
SET @tpl_core_id := (SELECT id FROM routine_templates WHERE name = 'Core y Resistencia' LIMIT 1);

SET @press_banca_id := (SELECT id FROM exercises WHERE name = 'Press banca con barra' LIMIT 1);
SET @press_inclinado_id := (SELECT id FROM exercises WHERE name = 'Press inclinado con mancuernas' LIMIT 1);
SET @aperturas_id := (SELECT id FROM exercises WHERE name = 'Aperturas con mancuernas' LIMIT 1);
SET @remo_sentado_id := (SELECT id FROM exercises WHERE name = 'Remo sentado en polea' LIMIT 1);
SET @jalon_id := (SELECT id FROM exercises WHERE name = 'Jalon al pecho' LIMIT 1);
SET @rumano_id := (SELECT id FROM exercises WHERE name = 'Peso muerto rumano' LIMIT 1);
SET @prensa_id := (SELECT id FROM exercises WHERE name = 'Prensa 45' LIMIT 1);
SET @goblet_id := (SELECT id FROM exercises WHERE name = 'Sentadilla goblet' LIMIT 1);
SET @cinta_id := (SELECT id FROM exercises WHERE name = 'Caminata en cinta inclinada' LIMIT 1);
SET @thruster_id := (SELECT id FROM exercises WHERE name = 'Thruster con barra' LIMIT 1);
SET @remo_metabolico_id := (SELECT id FROM exercises WHERE name = 'Remo metabolico' LIMIT 1);
SET @reformer_id := (SELECT id FROM exercises WHERE name = 'Trabajo en reformer' LIMIT 1);
INSERT INTO routine_template_exercises (routine_template_id, exercise_id) VALUES
(@tpl_push_id, @press_banca_id),
(@tpl_push_id, @press_inclinado_id),
(@tpl_push_id, @aperturas_id),
(@tpl_pull_id, @remo_sentado_id),
(@tpl_pull_id, @jalon_id),
(@tpl_pull_id, @rumano_id),
(@tpl_legs_id, @prensa_id),
(@tpl_legs_id, @goblet_id),
(@tpl_legs_id, @rumano_id),
(@tpl_full_id, @goblet_id),
(@tpl_full_id, @cinta_id),
(@tpl_full_id, @press_inclinado_id),
(@tpl_core_id, @cinta_id),
(@tpl_core_id, @reformer_id),
(@tpl_core_id, @remo_metabolico_id);

INSERT INTO routine_assignments (user_id, routine_template_id, custom_name, start_date, end_date) VALUES
(@max_id, @tpl_push_id, NULL, DATE_SUB(@today, INTERVAL 28 DAY), DATE_ADD(@today, INTERVAL 14 DAY)),
(@max_id, @tpl_pull_id, NULL, DATE_SUB(@today, INTERVAL 28 DAY), DATE_ADD(@today, INTERVAL 14 DAY)),
(@vale_id, @tpl_core_id, NULL, DATE_SUB(@today, INTERVAL 21 DAY), DATE_ADD(@today, INTERVAL 21 DAY)),
(@nico_id, @tpl_legs_id, NULL, DATE_SUB(@today, INTERVAL 28 DAY), DATE_ADD(@today, INTERVAL 7 DAY)),
(@cami_id, @tpl_full_id, NULL, DATE_SUB(@today, INTERVAL 14 DAY), DATE_ADD(@today, INTERVAL 21 DAY)),
(@fede_id, @tpl_full_id, NULL, DATE_SUB(@today, INTERVAL 21 DAY), DATE_ADD(@today, INTERVAL 14 DAY)),
(@juli_id, @tpl_core_id, NULL, DATE_SUB(@today, INTERVAL 35 DAY), DATE_SUB(@today, INTERVAL 5 DAY)),
(@bruno_id, NULL, 'Rutina Custom Competencia', DATE_SUB(@today, INTERVAL 14 DAY), DATE_ADD(@today, INTERVAL 14 DAY));

SET @ra_max_push := (SELECT id FROM routine_assignments WHERE user_id = @max_id AND routine_template_id = @tpl_push_id LIMIT 1);
SET @ra_max_pull := (SELECT id FROM routine_assignments WHERE user_id = @max_id AND routine_template_id = @tpl_pull_id LIMIT 1);
SET @ra_vale_core := (SELECT id FROM routine_assignments WHERE user_id = @vale_id AND routine_template_id = @tpl_core_id LIMIT 1);
SET @ra_nico_legs := (SELECT id FROM routine_assignments WHERE user_id = @nico_id AND routine_template_id = @tpl_legs_id LIMIT 1);
SET @ra_cami_full := (SELECT id FROM routine_assignments WHERE user_id = @cami_id AND routine_template_id = @tpl_full_id LIMIT 1);
SET @ra_fede_full := (SELECT id FROM routine_assignments WHERE user_id = @fede_id AND routine_template_id = @tpl_full_id LIMIT 1);
SET @ra_juli_core := (SELECT id FROM routine_assignments WHERE user_id = @juli_id AND routine_template_id = @tpl_core_id LIMIT 1);
SET @ra_bruno_custom := (SELECT id FROM routine_assignments WHERE user_id = @bruno_id AND custom_name = 'Rutina Custom Competencia' LIMIT 1);

INSERT INTO routine_assignment_days (routine_assignment_id, day) VALUES
(@ra_max_push, 'LUNES'),
(@ra_max_push, 'MIERCOLES'),
(@ra_max_pull, 'VIERNES'),
(@ra_vale_core, 'MARTES'),
(@ra_vale_core, 'JUEVES'),
(@ra_nico_legs, 'LUNES'),
(@ra_nico_legs, 'MIERCOLES'),
(@ra_nico_legs, 'VIERNES'),
(@ra_cami_full, 'MARTES'),
(@ra_cami_full, 'JUEVES'),
(@ra_fede_full, 'LUNES'),
(@ra_fede_full, 'MIERCOLES'),
(@ra_fede_full, 'VIERNES'),
(@ra_juli_core, 'LUNES'),
(@ra_juli_core, 'JUEVES'),
(@ra_bruno_custom, 'LUNES'),
(@ra_bruno_custom, 'MIERCOLES'),
(@ra_bruno_custom, 'VIERNES');

INSERT INTO routine_assignment_exercises (
    assignment_id, exercise_id, exercise_name, sets, prescription_mode,
    reps, weight, min_reps, max_reps, min_weight, max_weight
) VALUES
(@ra_max_push, @press_banca_id, 'Press banca con barra', 4, 'FIJO', 8, 65, NULL, NULL, NULL, NULL),
(@ra_max_push, @press_inclinado_id, 'Press inclinado con mancuernas', 4, 'FIJO', 10, 24, NULL, NULL, NULL, NULL),
(@ra_max_push, @aperturas_id, 'Aperturas con mancuernas', 3, 'DINAMICO', NULL, NULL, 12, 15, 10, 14),
(@ra_max_pull, @remo_sentado_id, 'Remo sentado en polea', 4, 'FIJO', 10, 55, NULL, NULL, NULL, NULL),
(@ra_max_pull, @jalon_id, 'Jalon al pecho', 4, 'FIJO', 10, 50, NULL, NULL, NULL, NULL),
(@ra_max_pull, @rumano_id, 'Peso muerto rumano', 3, 'FIJO', 8, 75, NULL, NULL, NULL, NULL),
(@ra_vale_core, @cinta_id, 'Caminata en cinta inclinada', 3, 'DINAMICO', NULL, NULL, 12, 15, NULL, NULL),
(@ra_vale_core, @reformer_id, 'Trabajo en reformer', 4, 'FIJO', 12, NULL, NULL, NULL, NULL, NULL),
(@ra_vale_core, @remo_metabolico_id, 'Remo metabolico', 3, 'DINAMICO', NULL, NULL, 8, 12, NULL, NULL),
(@ra_nico_legs, @prensa_id, 'Prensa 45', 4, 'FIJO', 10, 120, NULL, NULL, NULL, NULL),
(@ra_nico_legs, @goblet_id, 'Sentadilla goblet', 3, 'FIJO', 12, 26, NULL, NULL, NULL, NULL),
(@ra_nico_legs, @rumano_id, 'Peso muerto rumano', 4, 'FIJO', 8, 85, NULL, NULL, NULL, NULL),
(@ra_cami_full, @goblet_id, 'Sentadilla goblet', 3, 'FIJO', 12, 18, NULL, NULL, NULL, NULL),
(@ra_cami_full, @cinta_id, 'Caminata en cinta inclinada', 3, 'DINAMICO', NULL, NULL, 15, 20, NULL, NULL),
(@ra_cami_full, @press_inclinado_id, 'Press inclinado con mancuernas', 3, 'FIJO', 10, 14, NULL, NULL, NULL, NULL),
(@ra_fede_full, @goblet_id, 'Sentadilla goblet', 4, 'FIJO', 10, 24, NULL, NULL, NULL, NULL),
(@ra_fede_full, @cinta_id, 'Caminata en cinta inclinada', 3, 'DINAMICO', NULL, NULL, 12, 18, NULL, NULL),
(@ra_fede_full, @press_banca_id, 'Press banca con barra', 4, 'FIJO', 8, 55, NULL, NULL, NULL, NULL),
(@ra_juli_core, @reformer_id, 'Trabajo en reformer', 4, 'FIJO', 12, NULL, NULL, NULL, NULL, NULL),
(@ra_juli_core, @cinta_id, 'Caminata en cinta inclinada', 3, 'DINAMICO', NULL, NULL, 15, 18, NULL, NULL),
(@ra_juli_core, @remo_metabolico_id, 'Remo metabolico', 3, 'DINAMICO', NULL, NULL, 8, 10, NULL, NULL),
(@ra_bruno_custom, @thruster_id, 'Thruster con barra', 5, 'FIJO', 6, 50, NULL, NULL, NULL, NULL),
(@ra_bruno_custom, @remo_sentado_id, 'Remo sentado en polea', 4, 'FIJO', 10, 60, NULL, NULL, NULL, NULL),
(@ra_bruno_custom, @press_inclinado_id, 'Press inclinado con mancuernas', 4, 'FIJO', 10, 26, NULL, NULL, NULL, NULL);

INSERT INTO exercise_personal_record_entries (user_id, exercise_id, weight, recorded_at) VALUES
(@max_id, @press_banca_id, 60, DATE_SUB(@today, INTERVAL 18 DAY)),
(@max_id, @press_banca_id, 65, DATE_SUB(@today, INTERVAL 10 DAY)),
(@max_id, @press_inclinado_id, 24, DATE_SUB(@today, INTERVAL 8 DAY)),
(@max_id, @remo_sentado_id, 55, DATE_SUB(@today, INTERVAL 5 DAY)),
(@vale_id, @reformer_id, 1, DATE_SUB(@today, INTERVAL 14 DAY)),
(@nico_id, @prensa_id, 120, DATE_SUB(@today, INTERVAL 12 DAY)),
(@nico_id, @rumano_id, 85, DATE_SUB(@today, INTERVAL 7 DAY)),
(@cami_id, @press_inclinado_id, 14, DATE_SUB(@today, INTERVAL 4 DAY)),
(@fede_id, @press_banca_id, 55, DATE_SUB(@today, INTERVAL 9 DAY)),
(@bruno_id, @thruster_id, 50, DATE_SUB(@today, INTERVAL 6 DAY));

INSERT INTO exercise_personal_records (user_id, exercise_id, max_weight, recorded_at) VALUES
(@max_id, @press_banca_id, 65, DATE_SUB(@today, INTERVAL 10 DAY)),
(@max_id, @press_inclinado_id, 24, DATE_SUB(@today, INTERVAL 8 DAY)),
(@max_id, @remo_sentado_id, 55, DATE_SUB(@today, INTERVAL 5 DAY)),
(@vale_id, @reformer_id, 1, DATE_SUB(@today, INTERVAL 14 DAY)),
(@nico_id, @prensa_id, 120, DATE_SUB(@today, INTERVAL 12 DAY)),
(@nico_id, @rumano_id, 85, DATE_SUB(@today, INTERVAL 7 DAY)),
(@cami_id, @press_inclinado_id, 14, DATE_SUB(@today, INTERVAL 4 DAY)),
(@fede_id, @press_banca_id, 55, DATE_SUB(@today, INTERVAL 9 DAY)),
(@bruno_id, @thruster_id, 50, DATE_SUB(@today, INTERVAL 6 DAY));

INSERT INTO user_payment_movements (user_id, plan_id, amount, detail, paid_at) VALUES
(@max_id, (SELECT plan_id FROM users WHERE id = @max_id), 42000.00, 'Renovacion de cuota', TIMESTAMP(DATE_SUB(@today, INTERVAL 20 DAY), '10:12:00')),
(@max_id, (SELECT plan_id FROM users WHERE id = @max_id), 42000.00, 'Renovacion de cuota', TIMESTAMP(DATE_SUB(@today, INTERVAL 50 DAY), '09:05:00')),
(@vale_id, (SELECT plan_id FROM users WHERE id = @vale_id), 32000.00, 'Renovacion de cuota', TIMESTAMP(DATE_SUB(@today, INTERVAL 24 DAY), '11:10:00')),
(@nico_id, (SELECT plan_id FROM users WHERE id = @nico_id), 36000.00, 'Renovacion de cuota', TIMESTAMP(DATE_SUB(@today, INTERVAL 18 DAY), '12:22:00')),
(@cami_id, (SELECT plan_id FROM users WHERE id = @cami_id), 28000.00, 'Renovacion de cuota', TIMESTAMP(DATE_SUB(@today, INTERVAL 27 DAY), '16:45:00')),
(@fede_id, (SELECT plan_id FROM users WHERE id = @fede_id), 32000.00, 'Renovacion de cuota', TIMESTAMP(DATE_SUB(@today, INTERVAL 29 DAY), '18:30:00')),
(@bruno_id, (SELECT plan_id FROM users WHERE id = @bruno_id), 42000.00, 'Renovacion de cuota', TIMESTAMP(DATE_SUB(@today, INTERVAL 9 DAY), '08:40:00')),
(@juli_id, (SELECT plan_id FROM users WHERE id = @juli_id), 28000.00, 'Pago parcial pendiente de regularizacion', TIMESTAMP(DATE_SUB(@today, INTERVAL 34 DAY), '14:15:00')),
(@agus_id, (SELECT plan_id FROM users WHERE id = @agus_id), 36000.00, 'Pago recibido fuera de termino', TIMESTAMP(DATE_SUB(@today, INTERVAL 41 DAY), '13:05:00'));

UPDATE users
SET must_change_password = 0
WHERE email <> @admin_email;

COMMIT;

