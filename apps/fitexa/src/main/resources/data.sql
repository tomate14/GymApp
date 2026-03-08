INSERT IGNORE INTO plans (id, name, price, max_days_per_week, active) VALUES
    (1, '2 veces por semana', 18000.00, 2, 1),
    (2, '3 veces por semana', 22000.00, 3, 1),
    (3, 'Libre', 28000.00, 7, 1);

INSERT IGNORE INTO users (id, first_name, last_name, dni, address, phone, email, birth_date, user_type, plan_id, registration_date, renewal_date, payment_status) VALUES
    (1, 'Lucia', 'Gimenez', '32450987', 'Belgrano 145', '3415011002', 'lucia.gimenez@gymapp.com', '1992-09-05', 'PROFESOR', NULL, '2025-11-02', '2026-03-10', 'AL_DIA'),
    (2, 'Matias', 'Lopez', '38221009', 'Brown 210', '3415667733', 'matias.lopez@email.com', '1998-01-20', 'ALUMNO', 2, '2025-10-18', '2026-02-20', 'ADEUDA'),
    (3, 'Diego', 'Ferrero', '29877441', 'San Luis 932', '3416120044', 'diego.ferrero@gymapp.com', '1987-04-14', 'PROFESOR', NULL, '2025-09-10', '2026-03-05', 'AL_DIA'),
    (4, 'Maria', 'Torres', '40155672', 'Italia 455', '3415233344', 'maria.torres@email.com', '2000-11-02', 'ALUMNO', 1, '2025-12-03', '2026-03-03', 'AL_DIA'),
    (5, 'Agustina', 'Ruiz', '31244876', 'Mitre 1880', '3415772200', 'agustina.ruiz@gymapp.com', '1990-06-25', 'PROFESOR', NULL, '2025-08-15', '2026-02-28', 'AL_DIA'),
    (6, 'Juan', 'Perez', '38992011', 'Mendoza 640', '3415448877', 'juan.perez@email.com', '1997-02-18', 'ALUMNO', 1, '2025-11-21', '2026-03-12', 'ADEUDA'),
    (7, 'Camila', 'Navarro', '41588231', 'Roca 310', '3415099877', 'camila.navarro@email.com', '2001-07-09', 'ALUMNO', 3, '2026-01-07', '2026-03-20', 'AL_DIA');

INSERT IGNORE INTO user_payment_movements (id, user_id, plan_id, amount, detail, paid_at) VALUES
    (1, 2, 2, 22000.00, 'Renovacion de cuota', '2025-12-20 10:15:00'),
    (2, 2, 2, 22000.00, 'Renovacion de cuota', '2026-01-20 10:20:00'),
    (3, 2, 2, 22000.00, 'Renovacion de cuota', '2026-02-20 10:25:00'),
    (4, 4, 1, 18000.00, 'Renovacion de cuota', '2025-12-03 18:35:00'),
    (5, 4, 1, 18000.00, 'Renovacion de cuota', '2026-01-03 18:40:00'),
    (6, 4, 1, 18000.00, 'Renovacion de cuota', '2026-02-03 18:45:00'),
    (7, 6, 1, 18000.00, 'Renovacion de cuota', '2026-01-12 09:10:00'),
    (8, 6, 1, 18000.00, 'Renovacion de cuota', '2026-02-12 09:15:00'),
    (9, 7, 3, 28000.00, 'Renovacion de cuota', '2026-01-20 08:30:00'),
    (10, 7, 3, 28000.00, 'Renovacion de cuota', '2026-02-20 08:35:00');

INSERT IGNORE INTO activities (id, name, description) VALUES
    (1, 'Musculacion', 'Sala libre con seguimiento mensual'),
    (2, 'Crossfit', 'Entrenamiento funcional de alta intensidad'),
    (3, 'Pilates', 'Clases grupales de movilidad y control corporal'),
    (4, 'Spinning', 'Trabajo cardiovascular guiado en bicicleta fija'),
    (5, 'Funcional', 'Circuitos de fuerza, coordinacion y resistencia'),
    (6, 'Yoga', 'Practica de movilidad, respiracion y flexibilidad');

INSERT IGNORE INTO machines (id, name, photo_url) VALUES
    (1, 'Smith Machine', 'https://images.unsplash.com/photo-1583454110551-21f2fa2afe61?auto=format&fit=crop&w=900&q=80'),
    (2, 'Remo bajo', 'https://images.unsplash.com/photo-1584466977773-e625c37cdd50?auto=format&fit=crop&w=900&q=80'),
    (3, 'Prensa 45', 'https://images.unsplash.com/photo-1518611012118-696072aa579a?auto=format&fit=crop&w=900&q=80'),
    (4, 'Polea alta', 'https://images.unsplash.com/photo-1571019613576-2b22c76fd955?auto=format&fit=crop&w=900&q=80');

INSERT IGNORE INTO exercises (id, name, activity_id, machine_id, video_url) VALUES
    (1, 'Sentadilla en Smith', 1, 1, 'https://www.youtube.com/watch?v=SW_C1A-rejs'),
    (2, 'Burpee con salto', 2, NULL, 'https://www.youtube.com/watch?v=TU8QYVW0gDU'),
    (3, 'Remo en polea baja', 1, 2, 'https://www.youtube.com/watch?v=GZbfZ033f74'),
    (4, 'Prensa de piernas', 1, 3, 'https://www.youtube.com/watch?v=IZxyjW7MPJQ'),
    (5, 'Plancha frontal', 5, NULL, 'https://www.youtube.com/watch?v=pSHjTRCQxIw'),
    (6, 'Sprint en bicicleta', 4, NULL, 'https://www.youtube.com/watch?v=RjQf3Q9M9jE'),
    (7, 'Bird-dog controlado', 6, NULL, 'https://www.youtube.com/watch?v=wiFNA3sqjCA');

INSERT IGNORE INTO gym_classes (id, class_name, activity_id, max_capacity, enrolled, teacher_id, date, all_day, start_time, end_time) VALUES
    (1, 'WOD Morning', 2, 16, 12, 1, '2026-03-02', 0, '08:00:00', '09:00:00'),
    (2, 'Sala Musculacion Libre', 1, NULL, 34, NULL, '2026-03-02', 1, NULL, NULL),
    (3, 'Pilates suelo intermedio', 3, 20, 14, 3, '2026-03-03', 0, '09:30:00', '10:30:00'),
    (4, 'Spinning HIIT', 4, 18, 17, 5, '2026-03-03', 0, '19:00:00', '20:00:00'),
    (5, 'Funcional express', 5, 22, 19, 3, '2026-03-04', 0, '07:15:00', '08:00:00'),
    (6, 'Yoga jornada bienestar', 6, 25, 11, 5, '2026-03-04', 1, NULL, NULL);

INSERT IGNORE INTO gym_class_days (gym_class_id, day) VALUES
    (1, 'LUNES'), (1, 'MIERCOLES'), (1, 'VIERNES'),
    (2, 'LUNES'), (2, 'MARTES'), (2, 'MIERCOLES'), (2, 'JUEVES'), (2, 'VIERNES'),
    (3, 'MARTES'), (3, 'JUEVES'),
    (4, 'MARTES'), (4, 'JUEVES'),
    (5, 'LUNES'), (5, 'MIERCOLES'), (5, 'VIERNES'),
    (6, 'MIERCOLES'), (6, 'SABADO');

INSERT IGNORE INTO routine_templates (id, name, created_at) VALUES
    (1, 'Hipertrofia inicial', '2026-01-12'),
    (2, 'Resistencia metabolica', '2026-01-20'),
    (3, 'Fuerza tren inferior', '2026-01-27'),
    (4, 'Movilidad y core', '2026-02-03');

INSERT IGNORE INTO routine_template_exercises (routine_template_id, exercise_id) VALUES
    (1, 1), (1, 2),
    (2, 2), (2, 5), (2, 6),
    (3, 1), (3, 4), (3, 3),
    (4, 5), (4, 7), (4, 2);

INSERT IGNORE INTO routine_assignments (id, user_id, routine_template_id, start_date, end_date) VALUES
    (1, 2, 1, '2026-03-01', '2026-05-01'),
    (2, 4, 2, '2026-03-03', '2026-05-03'),
    (3, 6, 3, '2026-03-01', '2026-04-30'),
    (4, 7, 4, '2026-03-05', '2026-05-10');

INSERT IGNORE INTO routine_assignment_days (routine_assignment_id, day) VALUES
    (1, 'LUNES'), (1, 'MIERCOLES'), (1, 'VIERNES'),
    (2, 'MARTES'), (2, 'JUEVES'), (2, 'SABADO'),
    (3, 'LUNES'), (3, 'JUEVES'),
    (4, 'MIERCOLES'), (4, 'VIERNES');

INSERT IGNORE INTO routine_assignment_exercises (
    id, assignment_id, exercise_id, exercise_name, sets, prescription_mode,
    reps, weight, min_reps, max_reps, min_weight, max_weight
) VALUES
    (1, 1, 1, 'Sentadilla en Smith', 4, 'FIJO', 10, 45, NULL, NULL, NULL, NULL),
    (2, 1, 2, 'Burpee con salto', 3, 'DINAMICO', NULL, NULL, 10, 14, 0, 0),
    (3, 2, 2, 'Burpee con salto', 4, 'DINAMICO', NULL, NULL, 10, 16, 0, 0),
    (4, 2, 5, 'Plancha frontal', 4, 'FIJO', 45, 0, NULL, NULL, NULL, NULL),
    (5, 2, 6, 'Sprint en bicicleta', 6, 'DINAMICO', NULL, NULL, 20, 30, 0, 0),
    (6, 3, 1, 'Sentadilla en Smith', 5, 'FIJO', 8, 60, NULL, NULL, NULL, NULL),
    (7, 3, 4, 'Prensa de piernas', 4, 'FIJO', 10, 120, NULL, NULL, NULL, NULL),
    (8, 3, 3, 'Remo en polea baja', 4, 'DINAMICO', NULL, NULL, 8, 12, 35, 50),
    (9, 4, 5, 'Plancha frontal', 4, 'DINAMICO', NULL, NULL, 30, 60, 0, 0),
    (10, 4, 7, 'Bird-dog controlado', 3, 'FIJO', 12, 0, NULL, NULL, NULL, NULL),
    (11, 4, 2, 'Burpee con salto', 3, 'DINAMICO', NULL, NULL, 8, 12, 0, 0);
