CREATE TABLE IF NOT EXISTS plans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(120) NOT NULL UNIQUE,
    price DECIMAL(12, 2) NOT NULL,
    max_days_per_week INT NOT NULL,
    active TINYINT(1) NOT NULL
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
    plan_id BIGINT NULL,
    registration_date DATE NOT NULL,
    renewal_date DATE NOT NULL,
    payment_status VARCHAR(40) NOT NULL,
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

CREATE TABLE IF NOT EXISTS gym_classes (
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
    subscribed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_class_subscriptions_class FOREIGN KEY (gym_class_id) REFERENCES gym_classes(id) ON DELETE CASCADE,
    CONSTRAINT fk_class_subscriptions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_gym_class_subscriptions_class_user UNIQUE (gym_class_id, user_id)
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

##CREATE INDEX idx_ra_user_dates ON routine_assignments(user_id, start_date, end_date);
