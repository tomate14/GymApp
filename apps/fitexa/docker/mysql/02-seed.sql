USE fitexa;

SET @default_password = '$2a$10$6OsWrIYxuxnqxvkW8bAV3.4HM4mH5q.BlycoT6WaGYSbE0paov0ii';

INSERT INTO clients (name, slug, active, created_at) VALUES
    ('Fitexa Central', 'fitexa-central', 1, NOW()),
    ('Powerhouse Norte', 'powerhouse-norte', 1, NOW());

SET @client_fitexa = (SELECT id FROM clients WHERE slug = 'fitexa-central');
SET @client_powerhouse = (SELECT id FROM clients WHERE slug = 'powerhouse-norte');

INSERT INTO billing_plans (code, name, description, price, currency_id, frequency, frequency_type, active, created_at, updated_at) VALUES
    ('basic-monthly', 'Fitexa Basico', 'Gestion esencial para gimnasios chicos.', 29900.00, 'ARS', 1, 'MONTHS', 1, NOW(), NOW()),
    ('pro-monthly', 'Fitexa Pro', 'Gestion completa con panel, mobile y reportes.', 49900.00, 'ARS', 1, 'MONTHS', 1, NOW(), NOW());

SET @billing_plan_pro = (SELECT id FROM billing_plans WHERE code = 'pro-monthly');

INSERT INTO mercado_pago_accounts (
    name,
    encrypted_access_token,
    encrypted_public_key,
    encrypted_webhook_secret,
    public_base_url,
    app_base_url,
    sandbox_mode,
    active,
    created_at,
    updated_at
) VALUES (
    'default',
    NULL,
    NULL,
    NULL,
    'http://localhost:8080',
    'http://localhost:4200',
    1,
    1,
    NOW(),
    NOW()
);

INSERT INTO client_subscriptions (
    client_id,
    billing_plan_id,
    status,
    payer_email,
    next_payment_at,
    current_period_start,
    current_period_end,
    activated_at,
    created_at,
    updated_at
) VALUES
    (@client_fitexa, @billing_plan_pro, 'ACTIVE', 'admin@fitexa.com', DATE_ADD(NOW(), INTERVAL 1 MONTH), NOW(), DATE_ADD(NOW(), INTERVAL 1 MONTH), NOW(), NOW(), NOW()),
    (@client_powerhouse, @billing_plan_pro, 'ACTIVE', 'admin@powerhouse.com', DATE_ADD(NOW(), INTERVAL 1 MONTH), NOW(), DATE_ADD(NOW(), INTERVAL 1 MONTH), NOW(), NOW(), NOW());

INSERT INTO plans (client_id, name, price, max_days_per_week, active) VALUES
    (@client_fitexa, '2 veces por semana', 22000.00, 2, 1),
    (@client_fitexa, '3 veces por semana', 28000.00, 3, 1),
    (@client_fitexa, 'Libre', 36000.00, 7, 1),
    (@client_powerhouse, '2 veces por semana', 25000.00, 2, 1),
    (@client_powerhouse, '3 veces por semana', 31000.00, 3, 1),
    (@client_powerhouse, 'Libre', 39000.00, 7, 1);

INSERT INTO roles (client_id, name, description, system_role) VALUES
    (@client_fitexa, 'Administrador', 'Acceso completo al panel de administracion.', 1),
    (@client_fitexa, 'Profesor', 'Acceso al panel operativo excepto dashboard y roles y permisos.', 1),
    (@client_fitexa, 'Alumno', 'Rol base sin acceso visible al panel de administracion.', 1),
    (@client_powerhouse, 'Administrador', 'Acceso completo al panel de administracion.', 1),
    (@client_powerhouse, 'Profesor', 'Acceso al panel operativo excepto dashboard y roles y permisos.', 1),
    (@client_powerhouse, 'Alumno', 'Rol base sin acceso visible al panel de administracion.', 1);

SET @fitexa_admin_role = (SELECT id FROM roles WHERE client_id = @client_fitexa AND name = 'Administrador');
SET @fitexa_teacher_role = (SELECT id FROM roles WHERE client_id = @client_fitexa AND name = 'Profesor');
SET @fitexa_student_role = (SELECT id FROM roles WHERE client_id = @client_fitexa AND name = 'Alumno');
SET @powerhouse_admin_role = (SELECT id FROM roles WHERE client_id = @client_powerhouse AND name = 'Administrador');
SET @powerhouse_teacher_role = (SELECT id FROM roles WHERE client_id = @client_powerhouse AND name = 'Profesor');
SET @powerhouse_student_role = (SELECT id FROM roles WHERE client_id = @client_powerhouse AND name = 'Alumno');

INSERT INTO role_menu_permissions (role_id, menu_item) VALUES
    (@fitexa_admin_role, 'DASHBOARD'), (@fitexa_admin_role, 'AGENDA'), (@fitexa_admin_role, 'USERS'),
    (@fitexa_admin_role, 'PLANS'), (@fitexa_admin_role, 'ACTIVITIES'), (@fitexa_admin_role, 'CLASSES'),
    (@fitexa_admin_role, 'ROUTINES'), (@fitexa_admin_role, 'EXERCISES'), (@fitexa_admin_role, 'MACHINES'),
    (@fitexa_admin_role, 'ROLES_PERMISSIONS'),
    (@fitexa_teacher_role, 'AGENDA'), (@fitexa_teacher_role, 'USERS'), (@fitexa_teacher_role, 'PLANS'),
    (@fitexa_teacher_role, 'ACTIVITIES'), (@fitexa_teacher_role, 'CLASSES'), (@fitexa_teacher_role, 'ROUTINES'),
    (@fitexa_teacher_role, 'EXERCISES'), (@fitexa_teacher_role, 'MACHINES'),
    (@powerhouse_admin_role, 'DASHBOARD'), (@powerhouse_admin_role, 'AGENDA'), (@powerhouse_admin_role, 'USERS'),
    (@powerhouse_admin_role, 'PLANS'), (@powerhouse_admin_role, 'ACTIVITIES'), (@powerhouse_admin_role, 'CLASSES'),
    (@powerhouse_admin_role, 'ROUTINES'), (@powerhouse_admin_role, 'EXERCISES'), (@powerhouse_admin_role, 'MACHINES'),
    (@powerhouse_admin_role, 'ROLES_PERMISSIONS'),
    (@powerhouse_teacher_role, 'AGENDA'), (@powerhouse_teacher_role, 'USERS'), (@powerhouse_teacher_role, 'PLANS'),
    (@powerhouse_teacher_role, 'ACTIVITIES'), (@powerhouse_teacher_role, 'CLASSES'), (@powerhouse_teacher_role, 'ROUTINES'),
    (@powerhouse_teacher_role, 'EXERCISES'), (@powerhouse_teacher_role, 'MACHINES');

SET @fitexa_plan_2 = (SELECT id FROM plans WHERE client_id = @client_fitexa AND name = '2 veces por semana');
SET @fitexa_plan_3 = (SELECT id FROM plans WHERE client_id = @client_fitexa AND name = '3 veces por semana');
SET @fitexa_plan_free = (SELECT id FROM plans WHERE client_id = @client_fitexa AND name = 'Libre');
SET @powerhouse_plan_2 = (SELECT id FROM plans WHERE client_id = @client_powerhouse AND name = '2 veces por semana');
SET @powerhouse_plan_3 = (SELECT id FROM plans WHERE client_id = @client_powerhouse AND name = '3 veces por semana');
SET @powerhouse_plan_free = (SELECT id FROM plans WHERE client_id = @client_powerhouse AND name = 'Libre');

INSERT INTO activities (client_id, name, description) VALUES
    (@client_fitexa, 'Musculacion', 'Sala principal de entrenamiento con maquinas y peso libre.'),
    (@client_fitexa, 'Crossfit', 'Clases de alta intensidad con trabajo funcional.'),
    (@client_fitexa, 'Pilates', 'Trabajo de movilidad, control y postura.'),
    (@client_fitexa, 'Spinning', 'Clases grupales sobre bicicletas indoor.'),
    (@client_fitexa, 'Funcional', 'Entrenamiento general con estaciones.'),
    (@client_powerhouse, 'Musculacion', 'Sala de fuerza orientada a hipertrofia.'),
    (@client_powerhouse, 'Yoga', 'Clases de movilidad y respiracion.'),
    (@client_powerhouse, 'Funcional', 'Circuitos de fuerza y cardio.');

INSERT INTO machines (client_id, name, quantity, photo_url) VALUES
    (@client_fitexa, 'Press banca', 2, 'https://images.unsplash.com/photo-1571019614242-c5c5dee9f50b'),
    (@client_fitexa, 'Prensa 45', 1, 'https://images.unsplash.com/photo-1517836357463-d25dfeac3438'),
    (@client_fitexa, 'Polea alta', 2, 'https://images.unsplash.com/photo-1518611012118-696072aa579a'),
    (@client_fitexa, 'Remo bajo', 1, 'https://images.unsplash.com/photo-1517963879433-6ad2b056d712'),
    (@client_fitexa, 'Bicicleta indoor', 8, 'https://images.unsplash.com/photo-1517838277536-f5f99be501cd'),
    (@client_powerhouse, 'Rack sentadillas', 2, 'https://images.unsplash.com/photo-1534438327276-14e5300c3a48'),
    (@client_powerhouse, 'Remo ergometro', 3, 'https://images.unsplash.com/photo-1540497077202-7c8a3999166f');

SET @act_fitexa_muscu = (SELECT id FROM activities WHERE client_id = @client_fitexa AND name = 'Musculacion');
SET @act_fitexa_crossfit = (SELECT id FROM activities WHERE client_id = @client_fitexa AND name = 'Crossfit');
SET @act_fitexa_pilates = (SELECT id FROM activities WHERE client_id = @client_fitexa AND name = 'Pilates');
SET @act_fitexa_spinning = (SELECT id FROM activities WHERE client_id = @client_fitexa AND name = 'Spinning');
SET @act_fitexa_funcional = (SELECT id FROM activities WHERE client_id = @client_fitexa AND name = 'Funcional');
SET @act_powerhouse_muscu = (SELECT id FROM activities WHERE client_id = @client_powerhouse AND name = 'Musculacion');
SET @act_powerhouse_yoga = (SELECT id FROM activities WHERE client_id = @client_powerhouse AND name = 'Yoga');
SET @act_powerhouse_funcional = (SELECT id FROM activities WHERE client_id = @client_powerhouse AND name = 'Funcional');

SET @machine_press = (SELECT id FROM machines WHERE client_id = @client_fitexa AND name = 'Press banca');
SET @machine_prensa = (SELECT id FROM machines WHERE client_id = @client_fitexa AND name = 'Prensa 45');
SET @machine_polea = (SELECT id FROM machines WHERE client_id = @client_fitexa AND name = 'Polea alta');
SET @machine_remo = (SELECT id FROM machines WHERE client_id = @client_fitexa AND name = 'Remo bajo');
SET @machine_bike = (SELECT id FROM machines WHERE client_id = @client_fitexa AND name = 'Bicicleta indoor');
SET @machine_rack = (SELECT id FROM machines WHERE client_id = @client_powerhouse AND name = 'Rack sentadillas');
SET @machine_rower = (SELECT id FROM machines WHERE client_id = @client_powerhouse AND name = 'Remo ergometro');

INSERT INTO exercises (client_id, name, activity_id, machine_id, video_url) VALUES
    (@client_fitexa, 'Press plano con barra', @act_fitexa_muscu, @machine_press, 'https://www.youtube.com/watch?v=rT7DgCr-3pg'),
    (@client_fitexa, 'Sentadilla en prensa', @act_fitexa_muscu, @machine_prensa, 'https://www.youtube.com/watch?v=IZxyjW7MPJQ'),
    (@client_fitexa, 'Jalon al pecho', @act_fitexa_muscu, @machine_polea, 'https://www.youtube.com/watch?v=CAwf7n6Luuc'),
    (@client_fitexa, 'Remo sentado', @act_fitexa_muscu, @machine_remo, 'https://www.youtube.com/watch?v=GZbfZ033f74'),
    (@client_fitexa, 'Burpees', @act_fitexa_crossfit, NULL, 'https://www.youtube.com/watch?v=TU8QYVW0gDU'),
    (@client_fitexa, 'Air squat', @act_fitexa_crossfit, NULL, 'https://www.youtube.com/watch?v=aclHkVaku9U'),
    (@client_fitexa, 'Hundred', @act_fitexa_pilates, NULL, 'https://www.youtube.com/watch?v=1N1Q2qW5M30'),
    (@client_fitexa, 'Pedaleo en zona 3', @act_fitexa_spinning, @machine_bike, 'https://www.youtube.com/watch?v=-hSma-BRzoo'),
    (@client_fitexa, 'Swing con mancuerna', @act_fitexa_funcional, NULL, 'https://www.youtube.com/watch?v=YSxHifyIrv0'),
    (@client_powerhouse, 'Sentadilla trasera', @act_powerhouse_muscu, @machine_rack, 'https://www.youtube.com/watch?v=ultWZbUMPL8'),
    (@client_powerhouse, 'Remo continuo', @act_powerhouse_funcional, @machine_rower, 'https://www.youtube.com/watch?v=Y1GhfJoWLko'),
    (@client_powerhouse, 'Saludo al sol', @act_powerhouse_yoga, NULL, 'https://www.youtube.com/watch?v=6IUyf8rQw7I');

SET @fitexa_ex_press = (SELECT id FROM exercises WHERE client_id = @client_fitexa AND name = 'Press plano con barra');
SET @fitexa_ex_prensa = (SELECT id FROM exercises WHERE client_id = @client_fitexa AND name = 'Sentadilla en prensa');
SET @fitexa_ex_jalon = (SELECT id FROM exercises WHERE client_id = @client_fitexa AND name = 'Jalon al pecho');
SET @fitexa_ex_remo = (SELECT id FROM exercises WHERE client_id = @client_fitexa AND name = 'Remo sentado');
SET @fitexa_ex_burpees = (SELECT id FROM exercises WHERE client_id = @client_fitexa AND name = 'Burpees');
SET @fitexa_ex_squat = (SELECT id FROM exercises WHERE client_id = @client_fitexa AND name = 'Air squat');
SET @fitexa_ex_hundred = (SELECT id FROM exercises WHERE client_id = @client_fitexa AND name = 'Hundred');
SET @fitexa_ex_spin = (SELECT id FROM exercises WHERE client_id = @client_fitexa AND name = 'Pedaleo en zona 3');
SET @fitexa_ex_swing = (SELECT id FROM exercises WHERE client_id = @client_fitexa AND name = 'Swing con mancuerna');
SET @powerhouse_ex_squat = (SELECT id FROM exercises WHERE client_id = @client_powerhouse AND name = 'Sentadilla trasera');
SET @powerhouse_ex_row = (SELECT id FROM exercises WHERE client_id = @client_powerhouse AND name = 'Remo continuo');
SET @powerhouse_ex_yoga = (SELECT id FROM exercises WHERE client_id = @client_powerhouse AND name = 'Saludo al sol');

INSERT INTO users (client_id, first_name, last_name, dni, address, phone, email, password, must_change_password, birth_date, user_type, role_id, plan_id, registration_date, renewal_date) VALUES
    (@client_fitexa, 'Admin', 'Fitexa', '10000000', 'Av. Central 100', '1150000000', 'admin@fitexa.com', @default_password, 0, '1990-01-01', 'ADMIN', @fitexa_admin_role, NULL, '2026-01-01', '2099-12-31'),
    (@client_fitexa, 'Sofia', 'Benitez', '20000001', 'Mitre 120', '1150000001', 'sofia.benitez@fitexa.com', @default_password, 0, '1988-04-12', 'PROFESOR', @fitexa_teacher_role, NULL, '2026-01-10', '2099-12-31'),
    (@client_fitexa, 'Diego', 'Ferreyra', '20000002', 'Rivadavia 342', '1150000002', 'diego.ferreyra@fitexa.com', @default_password, 0, '1985-06-22', 'PROFESOR', @fitexa_teacher_role, NULL, '2026-01-10', '2099-12-31'),
    (@client_fitexa, 'Paula', 'Mendez', '20000003', 'Laprida 550', '1150000003', 'paula.mendez@fitexa.com', @default_password, 0, '1991-09-17', 'PROFESOR', @fitexa_teacher_role, NULL, '2026-01-10', '2099-12-31'),
    (@client_fitexa, 'Matias', 'Lopez', '30000001', 'Belgrano 100', '1160000001', 'matias.lopez@fitexa.com', @default_password, 0, '1998-03-15', 'ALUMNO', @fitexa_student_role, @fitexa_plan_3, '2026-01-15', '2026-05-18'),
    (@client_fitexa, 'Maria', 'Gomez', '30000002', 'Belgrano 102', '1160000002', 'maria.gomez@fitexa.com', @default_password, 0, '1996-08-21', 'ALUMNO', @fitexa_student_role, @fitexa_plan_free, '2026-01-20', '2026-05-27'),
    (@client_fitexa, 'Lucia', 'Perez', '30000003', 'Belgrano 104', '1160000003', 'lucia.perez@fitexa.com', @default_password, 0, '1994-11-05', 'ALUMNO', @fitexa_student_role, @fitexa_plan_2, '2026-02-02', '2026-05-09'),
    (@client_fitexa, 'Nicolas', 'Suarez', '30000004', 'Belgrano 106', '1160000004', 'nicolas.suarez@fitexa.com', @default_password, 0, '1992-07-03', 'ALUMNO', @fitexa_student_role, @fitexa_plan_3, '2026-02-12', '2026-05-25'),
    (@client_fitexa, 'Camila', 'Ruiz', '30000005', 'Belgrano 108', '1160000005', 'camila.ruiz@fitexa.com', @default_password, 0, '1999-02-09', 'ALUMNO', @fitexa_student_role, @fitexa_plan_free, '2026-02-18', '2026-05-29'),
    (@client_fitexa, 'Julian', 'Martinez', '30000006', 'Belgrano 110', '1160000006', 'julian.martinez@fitexa.com', @default_password, 0, '1991-10-10', 'ALUMNO', @fitexa_student_role, @fitexa_plan_2, '2026-03-01', '2026-05-02'),
    (@client_fitexa, 'Valentina', 'Cabrera', '30000007', 'Belgrano 112', '1160000007', 'valentina.cabrera@fitexa.com', @default_password, 0, '1997-12-14', 'ALUMNO', @fitexa_student_role, @fitexa_plan_3, '2026-03-05', '2026-05-14'),
    (@client_fitexa, 'Federico', 'Alonso', '30000008', 'Belgrano 114', '1160000008', 'federico.alonso@fitexa.com', @default_password, 0, '1989-05-19', 'ALUMNO', @fitexa_student_role, @fitexa_plan_free, '2026-03-09', '2026-05-30'),
    (@client_fitexa, 'Agustina', 'Torres', '30000009', 'Belgrano 116', '1160000009', 'agustina.torres@fitexa.com', @default_password, 0, '1995-01-07', 'ALUMNO', @fitexa_student_role, @fitexa_plan_2, '2026-03-12', '2026-05-06'),
    (@client_fitexa, 'Bruno', 'Sosa', '30000010', 'Belgrano 118', '1160000010', 'bruno.sosa@fitexa.com', @default_password, 0, '2000-06-01', 'ALUMNO', @fitexa_student_role, @fitexa_plan_3, '2026-03-20', '2026-05-21'),
    (@client_powerhouse, 'Admin', 'Powerhouse', '40000000', 'Av. Norte 500', '1170000000', 'admin@powerhouse.com', @default_password, 0, '1987-02-02', 'ADMIN', @powerhouse_admin_role, NULL, '2026-01-01', '2099-12-31'),
    (@client_powerhouse, 'Martin', 'Rios', '40000001', 'Av. Norte 520', '1170000001', 'martin.rios@powerhouse.com', @default_password, 0, '1986-05-12', 'PROFESOR', @powerhouse_teacher_role, NULL, '2026-01-10', '2099-12-31'),
    (@client_powerhouse, 'Elena', 'Paz', '40000002', 'Av. Norte 530', '1170000002', 'elena.paz@powerhouse.com', @default_password, 0, '1993-09-19', 'ALUMNO', @powerhouse_student_role, @powerhouse_plan_2, '2026-02-10', '2026-05-11'),
    (@client_powerhouse, 'Tomas', 'Borda', '40000003', 'Av. Norte 540', '1170000003', 'tomas.borda@powerhouse.com', @default_password, 0, '1998-07-23', 'ALUMNO', @powerhouse_student_role, @powerhouse_plan_free, '2026-03-15', '2026-05-28');

INSERT INTO user_activities (user_id, activity_id)
SELECT u.id, a.id FROM users u JOIN activities a ON a.client_id = u.client_id
WHERE u.email = 'matias.lopez@fitexa.com' AND a.name IN ('Musculacion', 'Funcional');

INSERT INTO user_activities (user_id, activity_id)
SELECT u.id, a.id FROM users u JOIN activities a ON a.client_id = u.client_id
WHERE u.email = 'maria.gomez@fitexa.com' AND a.name IN ('Musculacion', 'Pilates', 'Spinning');

INSERT INTO user_activities (user_id, activity_id)
SELECT u.id, a.id FROM users u JOIN activities a ON a.client_id = u.client_id
WHERE u.email = 'lucia.perez@fitexa.com' AND a.name IN ('Crossfit', 'Funcional');

INSERT INTO user_activities (user_id, activity_id)
SELECT u.id, a.id FROM users u JOIN activities a ON a.client_id = u.client_id
WHERE u.email = 'nicolas.suarez@fitexa.com' AND a.name IN ('Musculacion', 'Crossfit');

INSERT INTO user_activities (user_id, activity_id)
SELECT u.id, a.id FROM users u JOIN activities a ON a.client_id = u.client_id
WHERE u.email = 'camila.ruiz@fitexa.com' AND a.name IN ('Musculacion', 'Spinning', 'Pilates');

INSERT INTO user_activities (user_id, activity_id)
SELECT u.id, a.id FROM users u JOIN activities a ON a.client_id = u.client_id
WHERE u.email = 'julian.martinez@fitexa.com' AND a.name IN ('Musculacion');

INSERT INTO user_activities (user_id, activity_id)
SELECT u.id, a.id FROM users u JOIN activities a ON a.client_id = u.client_id
WHERE u.email = 'valentina.cabrera@fitexa.com' AND a.name IN ('Crossfit', 'Funcional', 'Musculacion');

INSERT INTO user_activities (user_id, activity_id)
SELECT u.id, a.id FROM users u JOIN activities a ON a.client_id = u.client_id
WHERE u.email = 'federico.alonso@fitexa.com' AND a.name IN ('Musculacion', 'Spinning');

INSERT INTO user_activities (user_id, activity_id)
SELECT u.id, a.id FROM users u JOIN activities a ON a.client_id = u.client_id
WHERE u.email = 'agustina.torres@fitexa.com' AND a.name IN ('Pilates', 'Funcional');

INSERT INTO user_activities (user_id, activity_id)
SELECT u.id, a.id FROM users u JOIN activities a ON a.client_id = u.client_id
WHERE u.email = 'bruno.sosa@fitexa.com' AND a.name IN ('Musculacion', 'Crossfit');

INSERT INTO user_activities (user_id, activity_id)
SELECT u.id, a.id FROM users u JOIN activities a ON a.client_id = u.client_id
WHERE u.email = 'elena.paz@powerhouse.com' AND a.name IN ('Musculacion', 'Yoga');

INSERT INTO user_activities (user_id, activity_id)
SELECT u.id, a.id FROM users u JOIN activities a ON a.client_id = u.client_id
WHERE u.email = 'tomas.borda@powerhouse.com' AND a.name IN ('Musculacion', 'Funcional');

SET @teacher_sofia = (SELECT id FROM users WHERE email = 'sofia.benitez@fitexa.com');
SET @teacher_diego = (SELECT id FROM users WHERE email = 'diego.ferreyra@fitexa.com');
SET @teacher_paula = (SELECT id FROM users WHERE email = 'paula.mendez@fitexa.com');
SET @teacher_martin = (SELECT id FROM users WHERE email = 'martin.rios@powerhouse.com');

INSERT INTO gym_classes (client_id, class_name, activity_id, max_capacity, enrolled, teacher_id, date, all_day, start_time, end_time) VALUES
    (@client_fitexa, 'Crossfit AM', @act_fitexa_crossfit, 16, 3, @teacher_sofia, '2026-05-04', 0, '07:00:00', '08:00:00'),
    (@client_fitexa, 'Musculacion Guiada', @act_fitexa_muscu, NULL, 3, @teacher_diego, '2026-05-04', 0, '09:00:00', '10:30:00'),
    (@client_fitexa, 'Pilates Recovery', @act_fitexa_pilates, 12, 0, @teacher_paula, '2026-05-05', 0, '18:00:00', '19:00:00'),
    (@client_fitexa, 'Spinning Night', @act_fitexa_spinning, 20, 3, @teacher_paula, '2026-05-06', 0, '19:30:00', '20:30:00'),
    (@client_fitexa, 'Funcional Express', @act_fitexa_funcional, 18, 3, @teacher_sofia, '2026-05-07', 0, '08:30:00', '09:15:00'),
    (@client_powerhouse, 'Yoga Matinal', @act_powerhouse_yoga, 10, 1, @teacher_martin, '2026-05-05', 0, '08:00:00', '09:00:00'),
    (@client_powerhouse, 'Fuerza Base', @act_powerhouse_muscu, NULL, 1, @teacher_martin, '2026-05-06', 0, '19:00:00', '20:00:00');

SET @class_crossfit = (SELECT id FROM gym_classes WHERE client_id = @client_fitexa AND class_name = 'Crossfit AM');
SET @class_muscu = (SELECT id FROM gym_classes WHERE client_id = @client_fitexa AND class_name = 'Musculacion Guiada');
SET @class_pilates = (SELECT id FROM gym_classes WHERE client_id = @client_fitexa AND class_name = 'Pilates Recovery');
SET @class_spinning = (SELECT id FROM gym_classes WHERE client_id = @client_fitexa AND class_name = 'Spinning Night');
SET @class_funcional = (SELECT id FROM gym_classes WHERE client_id = @client_fitexa AND class_name = 'Funcional Express');
SET @class_yoga = (SELECT id FROM gym_classes WHERE client_id = @client_powerhouse AND class_name = 'Yoga Matinal');
SET @class_fuerza = (SELECT id FROM gym_classes WHERE client_id = @client_powerhouse AND class_name = 'Fuerza Base');

INSERT INTO gym_class_days (gym_class_id, day) VALUES
    (@class_crossfit, 'LUNES'), (@class_crossfit, 'MIERCOLES'), (@class_crossfit, 'VIERNES'),
    (@class_muscu, 'LUNES'), (@class_muscu, 'MARTES'), (@class_muscu, 'JUEVES'),
    (@class_pilates, 'MARTES'), (@class_pilates, 'JUEVES'),
    (@class_spinning, 'MIERCOLES'), (@class_spinning, 'VIERNES'),
    (@class_funcional, 'LUNES'), (@class_funcional, 'JUEVES'),
    (@class_yoga, 'MARTES'), (@class_yoga, 'JUEVES'),
    (@class_fuerza, 'MIERCOLES'), (@class_fuerza, 'VIERNES');

INSERT INTO gym_class_subscriptions (client_id, gym_class_id, user_id, target_date, subscribed_at, attendance_present, attendance_marked_at)
SELECT @client_fitexa, @class_crossfit, u.id, '2026-05-04', '2026-05-03 20:00:00', 1, '2026-05-04 08:05:00' FROM users u WHERE u.email IN ('lucia.perez@fitexa.com', 'nicolas.suarez@fitexa.com', 'bruno.sosa@fitexa.com');

INSERT INTO gym_class_subscriptions (client_id, gym_class_id, user_id, target_date, subscribed_at, attendance_present, attendance_marked_at)
SELECT @client_fitexa, @class_muscu, u.id, '2026-05-05', '2026-05-04 20:00:00', 1, '2026-05-05 09:35:00' FROM users u WHERE u.email IN ('matias.lopez@fitexa.com', 'maria.gomez@fitexa.com', 'camila.ruiz@fitexa.com');

INSERT INTO gym_class_subscriptions (client_id, gym_class_id, user_id, target_date, subscribed_at, attendance_present, attendance_marked_at)
SELECT @client_fitexa, @class_spinning, u.id, '2026-05-06', '2026-05-05 18:00:00', 0, '2026-05-06 20:35:00' FROM users u WHERE u.email IN ('maria.gomez@fitexa.com', 'camila.ruiz@fitexa.com', 'federico.alonso@fitexa.com');

INSERT INTO gym_class_subscriptions (client_id, gym_class_id, user_id, target_date, subscribed_at, attendance_present, attendance_marked_at)
SELECT @client_fitexa, @class_funcional, u.id, '2026-05-07', '2026-05-06 19:00:00', 1, '2026-05-07 09:20:00' FROM users u WHERE u.email IN ('matias.lopez@fitexa.com', 'valentina.cabrera@fitexa.com', 'agustina.torres@fitexa.com');

INSERT INTO gym_class_subscriptions (client_id, gym_class_id, user_id, target_date, subscribed_at, attendance_present, attendance_marked_at)
SELECT @client_powerhouse, @class_yoga, u.id, '2026-05-05', '2026-05-04 17:00:00', 1, '2026-05-05 08:50:00' FROM users u WHERE u.email = 'elena.paz@powerhouse.com';

INSERT INTO gym_class_subscriptions (client_id, gym_class_id, user_id, target_date, subscribed_at, attendance_present, attendance_marked_at)
SELECT @client_powerhouse, @class_fuerza, u.id, '2026-05-06', '2026-05-05 17:00:00', 1, '2026-05-06 19:55:00' FROM users u WHERE u.email = 'tomas.borda@powerhouse.com';

INSERT INTO routine_templates (client_id, name, created_at) VALUES
    (@client_fitexa, 'Torso inicial', '2026-04-01'),
    (@client_fitexa, 'Piernas base', '2026-04-01'),
    (@client_fitexa, 'Metcon corto', '2026-04-10'),
    (@client_powerhouse, 'Full body norte', '2026-04-12');

SET @rt_torso = (SELECT id FROM routine_templates WHERE client_id = @client_fitexa AND name = 'Torso inicial');
SET @rt_piernas = (SELECT id FROM routine_templates WHERE client_id = @client_fitexa AND name = 'Piernas base');
SET @rt_metcon = (SELECT id FROM routine_templates WHERE client_id = @client_fitexa AND name = 'Metcon corto');
SET @rt_powerhouse = (SELECT id FROM routine_templates WHERE client_id = @client_powerhouse AND name = 'Full body norte');

INSERT INTO routine_template_exercises (routine_template_id, exercise_id) VALUES
    (@rt_torso, @fitexa_ex_press), (@rt_torso, @fitexa_ex_jalon), (@rt_torso, @fitexa_ex_remo),
    (@rt_piernas, @fitexa_ex_prensa), (@rt_piernas, @fitexa_ex_swing),
    (@rt_metcon, @fitexa_ex_burpees), (@rt_metcon, @fitexa_ex_squat),
    (@rt_powerhouse, @powerhouse_ex_squat), (@rt_powerhouse, @powerhouse_ex_row), (@rt_powerhouse, @powerhouse_ex_yoga);

INSERT INTO routine_assignments (client_id, user_id, routine_template_id, custom_name, start_date, end_date)
SELECT @client_fitexa, id, @rt_torso, NULL, '2026-05-01', '2026-05-31' FROM users WHERE email = 'matias.lopez@fitexa.com';
INSERT INTO routine_assignments (client_id, user_id, routine_template_id, custom_name, start_date, end_date)
SELECT @client_fitexa, id, @rt_piernas, NULL, '2026-05-01', '2026-05-28' FROM users WHERE email = 'maria.gomez@fitexa.com';
INSERT INTO routine_assignments (client_id, user_id, routine_template_id, custom_name, start_date, end_date)
SELECT @client_fitexa, id, @rt_metcon, NULL, '2026-05-03', '2026-05-24' FROM users WHERE email = 'lucia.perez@fitexa.com';
INSERT INTO routine_assignments (client_id, user_id, routine_template_id, custom_name, start_date, end_date)
SELECT @client_powerhouse, id, @rt_powerhouse, NULL, '2026-05-02', '2026-05-29' FROM users WHERE email = 'tomas.borda@powerhouse.com';

SET @ra_matias = (SELECT id FROM routine_assignments WHERE client_id = @client_fitexa AND user_id = (SELECT id FROM users WHERE email = 'matias.lopez@fitexa.com') LIMIT 1);
SET @ra_maria = (SELECT id FROM routine_assignments WHERE client_id = @client_fitexa AND user_id = (SELECT id FROM users WHERE email = 'maria.gomez@fitexa.com') LIMIT 1);
SET @ra_lucia = (SELECT id FROM routine_assignments WHERE client_id = @client_fitexa AND user_id = (SELECT id FROM users WHERE email = 'lucia.perez@fitexa.com') LIMIT 1);
SET @ra_tomas = (SELECT id FROM routine_assignments WHERE client_id = @client_powerhouse AND user_id = (SELECT id FROM users WHERE email = 'tomas.borda@powerhouse.com') LIMIT 1);

INSERT INTO routine_assignment_days (routine_assignment_id, day) VALUES
    (@ra_matias, 'LUNES'), (@ra_matias, 'MIERCOLES'), (@ra_matias, 'VIERNES'),
    (@ra_maria, 'MARTES'), (@ra_maria, 'JUEVES'),
    (@ra_lucia, 'LUNES'), (@ra_lucia, 'JUEVES'),
    (@ra_tomas, 'MIERCOLES'), (@ra_tomas, 'VIERNES');

INSERT INTO routine_assignment_exercises (client_id, assignment_id, exercise_id, exercise_name, sets, prescription_mode, reps, weight, min_reps, max_reps, min_weight, max_weight) VALUES
    (@client_fitexa, @ra_matias, @fitexa_ex_press, 'Press plano con barra', 4, 'FIJO', 10, 60, NULL, NULL, NULL, NULL),
    (@client_fitexa, @ra_matias, @fitexa_ex_jalon, 'Jalon al pecho', 4, 'DINAMICO', NULL, NULL, 10, 14, 40, 55),
    (@client_fitexa, @ra_matias, @fitexa_ex_remo, 'Remo sentado', 3, 'FIJO', 12, 45, NULL, NULL, NULL, NULL),
    (@client_fitexa, @ra_maria, @fitexa_ex_prensa, 'Sentadilla en prensa', 4, 'FIJO', 12, 95, NULL, NULL, NULL, NULL),
    (@client_fitexa, @ra_maria, @fitexa_ex_swing, 'Swing con mancuerna', 3, 'DINAMICO', NULL, NULL, 12, 16, 10, 16),
    (@client_fitexa, @ra_lucia, @fitexa_ex_burpees, 'Burpees', 5, 'FIJO', 12, 0, NULL, NULL, NULL, NULL),
    (@client_fitexa, @ra_lucia, @fitexa_ex_squat, 'Air squat', 4, 'FIJO', 20, 0, NULL, NULL, NULL, NULL),
    (@client_powerhouse, @ra_tomas, @powerhouse_ex_squat, 'Sentadilla trasera', 4, 'FIJO', 8, 80, NULL, NULL, NULL, NULL),
    (@client_powerhouse, @ra_tomas, @powerhouse_ex_row, 'Remo continuo', 3, 'DINAMICO', NULL, NULL, 8, 12, 20, 35),
    (@client_powerhouse, @ra_tomas, @powerhouse_ex_yoga, 'Saludo al sol', 3, 'FIJO', 10, 0, NULL, NULL, NULL, NULL);

INSERT INTO user_payment_movements (client_id, user_id, plan_id, amount, detail, paid_at)
SELECT u.client_id, u.id, u.plan_id, p.price, 'Renovacion de cuota', '2026-05-01 10:00:00'
FROM users u JOIN plans p ON p.id = u.plan_id
WHERE u.email IN ('matias.lopez@fitexa.com', 'maria.gomez@fitexa.com', 'camila.ruiz@fitexa.com', 'nicolas.suarez@fitexa.com', 'tomas.borda@powerhouse.com');

INSERT INTO user_payment_movements (client_id, user_id, plan_id, amount, detail, paid_at)
SELECT u.client_id, u.id, u.plan_id, p.price, 'Renovacion de cuota', '2026-04-01 10:00:00'
FROM users u JOIN plans p ON p.id = u.plan_id
WHERE u.email IN ('lucia.perez@fitexa.com', 'julian.martinez@fitexa.com', 'agustina.torres@fitexa.com', 'elena.paz@powerhouse.com');

INSERT INTO exercise_personal_records (client_id, user_id, exercise_id, max_weight, recorded_at)
SELECT @client_fitexa, (SELECT id FROM users WHERE email = 'matias.lopez@fitexa.com'), @fitexa_ex_press, 70, '2026-05-03'
UNION ALL
SELECT @client_fitexa, (SELECT id FROM users WHERE email = 'maria.gomez@fitexa.com'), @fitexa_ex_prensa, 110, '2026-05-04'
UNION ALL
SELECT @client_powerhouse, (SELECT id FROM users WHERE email = 'tomas.borda@powerhouse.com'), @powerhouse_ex_squat, 95, '2026-05-05';

INSERT INTO exercise_personal_record_entries (client_id, user_id, exercise_id, weight, recorded_at)
SELECT @client_fitexa, (SELECT id FROM users WHERE email = 'matias.lopez@fitexa.com'), @fitexa_ex_press, 60, '2026-04-20'
UNION ALL
SELECT @client_fitexa, (SELECT id FROM users WHERE email = 'matias.lopez@fitexa.com'), @fitexa_ex_press, 67.5, '2026-04-27'
UNION ALL
SELECT @client_fitexa, (SELECT id FROM users WHERE email = 'matias.lopez@fitexa.com'), @fitexa_ex_press, 70, '2026-05-03'
UNION ALL
SELECT @client_fitexa, (SELECT id FROM users WHERE email = 'maria.gomez@fitexa.com'), @fitexa_ex_prensa, 90, '2026-04-18'
UNION ALL
SELECT @client_fitexa, (SELECT id FROM users WHERE email = 'maria.gomez@fitexa.com'), @fitexa_ex_prensa, 100, '2026-04-28'
UNION ALL
SELECT @client_fitexa, (SELECT id FROM users WHERE email = 'maria.gomez@fitexa.com'), @fitexa_ex_prensa, 110, '2026-05-04'
UNION ALL
SELECT @client_powerhouse, (SELECT id FROM users WHERE email = 'tomas.borda@powerhouse.com'), @powerhouse_ex_squat, 80, '2026-04-21'
UNION ALL
SELECT @client_powerhouse, (SELECT id FROM users WHERE email = 'tomas.borda@powerhouse.com'), @powerhouse_ex_squat, 95, '2026-05-05';
