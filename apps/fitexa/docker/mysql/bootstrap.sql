CREATE DATABASE IF NOT EXISTS fitexa CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER DATABASE fitexa CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE fitexa;

CREATE TABLE IF NOT EXISTS plans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(120) NOT NULL UNIQUE,
    price DECIMAL(12, 2) NOT NULL,
    max_days_per_week INT NOT NULL,
    active TINYINT(1) NOT NULL
);

CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(120) NOT NULL UNIQUE,
    description VARCHAR(255) NOT NULL,
    system_role TINYINT(1) NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS role_menu_permissions (
    role_id BIGINT NOT NULL,
    menu_item VARCHAR(80) NOT NULL,
    PRIMARY KEY (role_id, menu_item),
    CONSTRAINT fk_role_menu_permissions_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS users (
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
    role_id BIGINT NULL,
    plan_id BIGINT NULL,
    registration_date DATE NOT NULL,
    renewal_date DATE NOT NULL,
    payment_status VARCHAR(40) NOT NULL,
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE SET NULL,
    CONSTRAINT fk_users_plan FOREIGN KEY (plan_id) REFERENCES plans(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS user_payment_movements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    plan_id BIGINT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    detail VARCHAR(255) NOT NULL,
    paid_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_payment_movements_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_payment_movements_plan FOREIGN KEY (plan_id) REFERENCES plans(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS activities (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(140) NOT NULL UNIQUE,
    description VARCHAR(500) NOT NULL
);

CREATE TABLE IF NOT EXISTS machines (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(140) NOT NULL UNIQUE,
    quantity INT NOT NULL DEFAULT 1,
    photo_url TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS exercises (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(140) NOT NULL,
    activity_id BIGINT NULL,
    machine_id BIGINT NULL,
    video_url TEXT NOT NULL,
    CONSTRAINT fk_exercises_activity FOREIGN KEY (activity_id) REFERENCES activities(id) ON DELETE SET NULL,
    CONSTRAINT fk_exercises_machine FOREIGN KEY (machine_id) REFERENCES machines(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS user_activities (
    user_id BIGINT NOT NULL,
    activity_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, activity_id),
    CONSTRAINT fk_user_activities_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_activities_activity FOREIGN KEY (activity_id) REFERENCES activities(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS gym_classes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    class_name VARCHAR(180) NOT NULL,
    activity_id BIGINT NULL,
    max_capacity INT NULL,
    enrolled INT NOT NULL DEFAULT 0,
    teacher_id BIGINT NULL,
    date DATE NOT NULL,
    all_day TINYINT(1) NOT NULL DEFAULT 0,
    start_time TIME NULL,
    end_time TIME NULL,
    CONSTRAINT fk_classes_activity FOREIGN KEY (activity_id) REFERENCES activities(id) ON DELETE SET NULL,
    CONSTRAINT fk_classes_teacher FOREIGN KEY (teacher_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS gym_class_days (
    gym_class_id BIGINT NOT NULL,
    day VARCHAR(32) NOT NULL,
    PRIMARY KEY (gym_class_id, day),
    CONSTRAINT fk_class_days_class FOREIGN KEY (gym_class_id) REFERENCES gym_classes(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS gym_class_subscriptions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    gym_class_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    target_date DATE NOT NULL,
    subscribed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    attendance_present TINYINT(1) NULL,
    attendance_marked_at DATETIME NULL,
    CONSTRAINT fk_class_subscriptions_class FOREIGN KEY (gym_class_id) REFERENCES gym_classes(id) ON DELETE CASCADE,
    CONSTRAINT fk_class_subscriptions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_gym_class_subscriptions_class_user_date UNIQUE (gym_class_id, user_id, target_date)
);

CREATE TABLE IF NOT EXISTS exercise_personal_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    exercise_id BIGINT NOT NULL,
    max_weight DOUBLE NOT NULL,
    recorded_at DATE NOT NULL,
    CONSTRAINT fk_exercise_personal_records_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_exercise_personal_records_exercise FOREIGN KEY (exercise_id) REFERENCES exercises(id) ON DELETE CASCADE,
    CONSTRAINT uq_exercise_personal_records_user_exercise UNIQUE (user_id, exercise_id)
);

CREATE TABLE IF NOT EXISTS exercise_personal_record_entries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    exercise_id BIGINT NOT NULL,
    weight DOUBLE NOT NULL,
    recorded_at DATE NOT NULL,
    CONSTRAINT fk_exercise_personal_record_entries_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_exercise_personal_record_entries_exercise FOREIGN KEY (exercise_id) REFERENCES exercises(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS routine_templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(180) NOT NULL,
    created_at DATE NOT NULL
);

CREATE TABLE IF NOT EXISTS routine_template_exercises (
    routine_template_id BIGINT NOT NULL,
    exercise_id BIGINT NOT NULL,
    PRIMARY KEY (routine_template_id, exercise_id),
    CONSTRAINT fk_rt_ex_template FOREIGN KEY (routine_template_id) REFERENCES routine_templates(id) ON DELETE CASCADE,
    CONSTRAINT fk_rt_ex_exercise FOREIGN KEY (exercise_id) REFERENCES exercises(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS routine_assignments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    routine_template_id BIGINT NULL,
    custom_name VARCHAR(180) NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    CONSTRAINT fk_ra_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_ra_template FOREIGN KEY (routine_template_id) REFERENCES routine_templates(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS routine_assignment_days (
    routine_assignment_id BIGINT NOT NULL,
    day VARCHAR(32) NOT NULL,
    PRIMARY KEY (routine_assignment_id, day),
    CONSTRAINT fk_ra_days_assignment FOREIGN KEY (routine_assignment_id) REFERENCES routine_assignments(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS routine_assignment_exercises (
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

SET @users_has_must_change_password := (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'users'
      AND column_name = 'must_change_password'
);
SET @sql := IF(
    @users_has_must_change_password = 0,
    'ALTER TABLE users ADD COLUMN must_change_password TINYINT(1) NOT NULL DEFAULT 0 AFTER password',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @machines_has_quantity := (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'machines'
      AND column_name = 'quantity'
);
SET @sql := IF(
    @machines_has_quantity = 0,
    'ALTER TABLE machines ADD COLUMN quantity INT NOT NULL DEFAULT 1 AFTER name',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @users_has_role_id := (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'users'
      AND column_name = 'role_id'
);
SET @sql := IF(
    @users_has_role_id = 0,
    'ALTER TABLE users ADD COLUMN role_id BIGINT NULL AFTER user_type',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @users_has_role_fk := (
    SELECT COUNT(*)
    FROM information_schema.table_constraints
    WHERE table_schema = DATABASE()
      AND table_name = 'users'
      AND constraint_name = 'fk_users_role'
      AND constraint_type = 'FOREIGN KEY'
);
SET @sql := IF(
    @users_has_role_fk = 0,
    'ALTER TABLE users ADD CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE SET NULL',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @gym_classes_has_all_day := (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'gym_classes'
      AND column_name = 'all_day'
);
SET @sql := IF(
    @gym_classes_has_all_day = 0,
    'ALTER TABLE gym_classes ADD COLUMN all_day TINYINT(1) NOT NULL DEFAULT 0 AFTER date',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @gym_classes_has_start_time := (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'gym_classes'
      AND column_name = 'start_time'
);
SET @sql := IF(
    @gym_classes_has_start_time = 0,
    'ALTER TABLE gym_classes ADD COLUMN start_time TIME NULL AFTER all_day',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @gym_classes_has_end_time := (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'gym_classes'
      AND column_name = 'end_time'
);
SET @sql := IF(
    @gym_classes_has_end_time = 0,
    'ALTER TABLE gym_classes ADD COLUMN end_time TIME NULL AFTER start_time',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @subscriptions_has_target_date := (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'gym_class_subscriptions'
      AND column_name = 'target_date'
);
SET @sql := IF(
    @subscriptions_has_target_date = 0,
    'ALTER TABLE gym_class_subscriptions ADD COLUMN target_date DATE NULL AFTER user_id',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @subscriptions_has_attendance_present := (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'gym_class_subscriptions'
      AND column_name = 'attendance_present'
);
SET @sql := IF(
    @subscriptions_has_attendance_present = 0,
    'ALTER TABLE gym_class_subscriptions ADD COLUMN attendance_present TINYINT(1) NULL AFTER subscribed_at',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @subscriptions_has_attendance_marked_at := (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'gym_class_subscriptions'
      AND column_name = 'attendance_marked_at'
);
SET @sql := IF(
    @subscriptions_has_attendance_marked_at = 0,
    'ALTER TABLE gym_class_subscriptions ADD COLUMN attendance_marked_at DATETIME NULL AFTER attendance_present',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE gym_class_subscriptions gcs
JOIN gym_classes gc ON gc.id = gcs.gym_class_id
SET gcs.target_date = gc.date
WHERE gcs.target_date IS NULL;

ALTER TABLE gym_class_subscriptions
    MODIFY COLUMN target_date DATE NOT NULL;

SET @has_old_gym_class_subscription_uq := (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'gym_class_subscriptions'
      AND index_name = 'uq_gym_class_subscriptions_class_user'
);
SET @sql := IF(
    @has_old_gym_class_subscription_uq > 0,
    'ALTER TABLE gym_class_subscriptions DROP INDEX uq_gym_class_subscriptions_class_user',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_daily_gym_class_subscription_uq := (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'gym_class_subscriptions'
      AND index_name = 'uq_gym_class_subscriptions_class_user_date'
);
SET @sql := IF(
    @has_daily_gym_class_subscription_uq = 0,
    'ALTER TABLE gym_class_subscriptions ADD CONSTRAINT uq_gym_class_subscriptions_class_user_date UNIQUE (gym_class_id, user_id, target_date)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

ALTER TABLE routine_assignments
    MODIFY COLUMN routine_template_id BIGINT NULL;

SET @routine_assignments_has_custom_name := (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'routine_assignments'
      AND column_name = 'custom_name'
);
SET @sql := IF(
    @routine_assignments_has_custom_name = 0,
    'ALTER TABLE routine_assignments ADD COLUMN custom_name VARCHAR(180) NULL AFTER routine_template_id',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_idx_ra_user_dates := (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'routine_assignments'
      AND index_name = 'idx_ra_user_dates'
);
SET @sql := IF(
    @has_idx_ra_user_dates = 0,
    'CREATE INDEX idx_ra_user_dates ON routine_assignments(user_id, start_date, end_date)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE roles
SET
    name = 'Administrador',
    description = 'Acceso completo al panel de administracion.',
    system_role = 1
WHERE upper(name) = 'ADMIN'
  AND NOT EXISTS (
      SELECT 1
      FROM (
          SELECT id
          FROM roles
          WHERE upper(name) = 'ADMINISTRADOR'
      ) existing_admin_role
  );

INSERT INTO roles (name, description, system_role)
SELECT
    'Administrador',
    'Acceso completo al panel de administracion.',
    1
WHERE NOT EXISTS (
    SELECT 1
    FROM roles
    WHERE upper(name) = 'ADMINISTRADOR'
);

INSERT INTO roles (name, description, system_role)
SELECT
    'Profesor',
    'Acceso al panel operativo excepto dashboard y roles y permisos.',
    1
WHERE NOT EXISTS (
    SELECT 1
    FROM roles
    WHERE upper(name) = 'PROFESOR'
);

INSERT INTO roles (name, description, system_role)
SELECT
    'Alumno',
    'Rol base sin acceso visible al panel de administracion.',
    1
WHERE NOT EXISTS (
    SELECT 1
    FROM roles
    WHERE upper(name) = 'ALUMNO'
);

UPDATE roles
SET
    description = 'Acceso completo al panel de administracion.',
    system_role = 1
WHERE upper(name) = 'ADMINISTRADOR';

UPDATE roles
SET
    name = 'Profesor',
    description = 'Acceso al panel operativo excepto dashboard y roles y permisos.',
    system_role = 1
WHERE upper(name) = 'PROFESOR';

UPDATE roles
SET
    description = 'Acceso al panel operativo excepto dashboard y roles y permisos.',
    system_role = 1
WHERE upper(name) = 'PROFESOR';

UPDATE roles
SET
    name = 'Alumno',
    description = 'Rol base sin acceso visible al panel de administracion.',
    system_role = 1
WHERE upper(name) = 'ALUMNO';

UPDATE roles
SET
    description = 'Rol base sin acceso visible al panel de administracion.',
    system_role = 1
WHERE upper(name) = 'ALUMNO';

SET @admin_role_id := (
    SELECT id
    FROM roles
    WHERE upper(name) = 'ADMINISTRADOR'
    LIMIT 1
);

SET @teacher_role_id := (
    SELECT id
    FROM roles
    WHERE upper(name) = 'PROFESOR'
    LIMIT 1
);

SET @student_role_id := (
    SELECT id
    FROM roles
    WHERE upper(name) = 'ALUMNO'
    LIMIT 1
);

DELETE FROM role_menu_permissions
WHERE role_id IN (@admin_role_id, @teacher_role_id, @student_role_id);

INSERT INTO role_menu_permissions (role_id, menu_item)
VALUES
    (@admin_role_id, 'DASHBOARD'),
    (@admin_role_id, 'AGENDA'),
    (@admin_role_id, 'USERS'),
    (@admin_role_id, 'PLANS'),
    (@admin_role_id, 'ACTIVITIES'),
    (@admin_role_id, 'CLASSES'),
    (@admin_role_id, 'ROUTINES'),
    (@admin_role_id, 'EXERCISES'),
    (@admin_role_id, 'MACHINES'),
    (@admin_role_id, 'ROLES_PERMISSIONS'),
    (@teacher_role_id, 'AGENDA'),
    (@teacher_role_id, 'USERS'),
    (@teacher_role_id, 'PLANS'),
    (@teacher_role_id, 'ACTIVITIES'),
    (@teacher_role_id, 'CLASSES'),
    (@teacher_role_id, 'ROUTINES'),
    (@teacher_role_id, 'EXERCISES'),
    (@teacher_role_id, 'MACHINES');

INSERT INTO users (
    first_name,
    last_name,
    dni,
    address,
    phone,
    email,
    password,
    must_change_password,
    birth_date,
    user_type,
    role_id,
    plan_id,
    registration_date,
    renewal_date,
    payment_status
)
SELECT
    'Admin',
    'Fitexa',
    '00000000',
    'Sin direccion',
    '0000000000',
    'admin@fitexa.com',
    '$2a$10$qZnbpykMIKuUiGahe6hzTu8JISGpAAHGMEiE9a6lipXjvR9QgnR8O',
    0,
    '1990-01-01',
    'ADMIN',
    @admin_role_id,
    NULL,
    CURDATE(),
    '2099-12-31',
    'AL_DIA'
WHERE NOT EXISTS (
    SELECT 1
    FROM users
    WHERE email = 'admin@fitexa.com'
);

UPDATE users
SET
    user_type = 'ADMIN',
    role_id = @admin_role_id,
    must_change_password = 0
WHERE email = 'admin@fitexa.com';
