CREATE DATABASE IF NOT EXISTS fitexa CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE fitexa;

CREATE TABLE clients (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(180) NOT NULL,
    slug VARCHAR(120) NOT NULL UNIQUE,
    active TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE billing_plans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(120) NOT NULL UNIQUE,
    name VARCHAR(160) NOT NULL,
    description VARCHAR(500) NOT NULL,
    price DECIMAL(12, 2) NOT NULL,
    currency_id VARCHAR(3) NOT NULL DEFAULT 'ARS',
    frequency INT NOT NULL,
    frequency_type VARCHAR(20) NOT NULL,
    active TINYINT(1) NOT NULL DEFAULT 1,
    mercado_pago_plan_id VARCHAR(120) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE mercado_pago_accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(120) NOT NULL UNIQUE,
    encrypted_access_token TEXT NULL,
    encrypted_public_key TEXT NULL,
    encrypted_webhook_secret TEXT NULL,
    public_base_url VARCHAR(500) NOT NULL,
    app_base_url VARCHAR(500) NOT NULL,
    sandbox_mode TINYINT(1) NOT NULL DEFAULT 1,
    active TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE client_subscriptions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    client_id BIGINT NOT NULL,
    billing_plan_id BIGINT NOT NULL,
    status VARCHAR(40) NOT NULL,
    mercado_pago_preapproval_id VARCHAR(120) NULL,
    mercado_pago_plan_id VARCHAR(120) NULL,
    payer_email VARCHAR(220) NULL,
    checkout_url VARCHAR(1000) NULL,
    next_payment_at DATETIME NULL,
    current_period_start DATETIME NULL,
    current_period_end DATETIME NULL,
    activated_at DATETIME NULL,
    cancelled_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_client_subscriptions_client FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE,
    CONSTRAINT fk_client_subscriptions_plan FOREIGN KEY (billing_plan_id) REFERENCES billing_plans(id),
    CONSTRAINT uq_client_subscriptions_client UNIQUE (client_id),
    CONSTRAINT uq_client_subscriptions_mp_preapproval UNIQUE (mercado_pago_preapproval_id)
);

CREATE TABLE client_subscription_payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    client_subscription_id BIGINT NOT NULL,
    client_id BIGINT NOT NULL,
    mercado_pago_authorized_payment_id VARCHAR(120) NULL,
    mercado_pago_payment_id VARCHAR(120) NULL,
    status VARCHAR(60) NOT NULL,
    status_detail VARCHAR(60) NULL,
    amount DECIMAL(12, 2) NOT NULL,
    currency_id VARCHAR(3) NOT NULL,
    paid_at DATETIME NULL,
    period_start DATETIME NULL,
    period_end DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_client_subscription_payments_subscription FOREIGN KEY (client_subscription_id) REFERENCES client_subscriptions(id) ON DELETE CASCADE,
    CONSTRAINT fk_client_subscription_payments_client FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE,
    CONSTRAINT uq_csp_mp_authorized_payment UNIQUE (mercado_pago_authorized_payment_id),
    CONSTRAINT uq_csp_mp_payment UNIQUE (mercado_pago_payment_id)
);

CREATE TABLE billing_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_key VARCHAR(220) NOT NULL UNIQUE,
    provider VARCHAR(80) NULL,
    event_type VARCHAR(80) NULL,
    provider_resource_id VARCHAR(120) NULL,
    client_id BIGINT NULL,
    resulting_status VARCHAR(40) NULL,
    payload TEXT NOT NULL,
    received_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_billing_events_client FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE SET NULL
);

CREATE TABLE plans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    client_id BIGINT NOT NULL,
    name VARCHAR(120) NOT NULL,
    price DECIMAL(12, 2) NOT NULL,
    max_days_per_week INT NOT NULL,
    active TINYINT(1) NOT NULL,
    CONSTRAINT fk_plans_client FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE,
    CONSTRAINT uq_plans_client_name UNIQUE (client_id, name)
);

CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    client_id BIGINT NOT NULL,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(255) NOT NULL,
    system_role TINYINT(1) NOT NULL DEFAULT 0,
    CONSTRAINT fk_roles_client FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE,
    CONSTRAINT uq_roles_client_name UNIQUE (client_id, name)
);

CREATE TABLE role_menu_permissions (
    role_id BIGINT NOT NULL,
    menu_item VARCHAR(80) NOT NULL,
    PRIMARY KEY (role_id, menu_item),
    CONSTRAINT fk_role_menu_permissions_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    client_id BIGINT NOT NULL,
    first_name VARCHAR(120) NOT NULL,
    last_name VARCHAR(120) NOT NULL,
    dni VARCHAR(40) NOT NULL,
    address VARCHAR(250) NOT NULL,
    phone VARCHAR(80) NOT NULL,
    email VARCHAR(180) NOT NULL,
    password VARCHAR(120) NOT NULL,
    must_change_password TINYINT(1) NOT NULL DEFAULT 0,
    birth_date DATE NOT NULL,
    user_type VARCHAR(40) NOT NULL,
    role_id BIGINT NULL,
    plan_id BIGINT NULL,
    registration_date DATE NOT NULL,
    renewal_date DATE NOT NULL,
    CONSTRAINT fk_users_client FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE,
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE SET NULL,
    CONSTRAINT fk_users_plan FOREIGN KEY (plan_id) REFERENCES plans(id) ON DELETE SET NULL,
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT uq_users_client_dni UNIQUE (client_id, dni)
);

CREATE TABLE user_payment_movements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    client_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    plan_id BIGINT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    detail VARCHAR(255) NOT NULL,
    paid_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_payment_movements_client FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_payment_movements_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_payment_movements_plan FOREIGN KEY (plan_id) REFERENCES plans(id) ON DELETE SET NULL
);

CREATE TABLE activities (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    client_id BIGINT NOT NULL,
    name VARCHAR(140) NOT NULL,
    description VARCHAR(500) NOT NULL,
    CONSTRAINT fk_activities_client FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE,
    CONSTRAINT uq_activities_client_name UNIQUE (client_id, name)
);

CREATE TABLE machines (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    client_id BIGINT NOT NULL,
    name VARCHAR(140) NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    photo_url TEXT NOT NULL,
    CONSTRAINT fk_machines_client FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE,
    CONSTRAINT uq_machines_client_name UNIQUE (client_id, name)
);

CREATE TABLE exercises (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    client_id BIGINT NOT NULL,
    name VARCHAR(140) NOT NULL,
    activity_id BIGINT NULL,
    machine_id BIGINT NULL,
    video_url TEXT NOT NULL,
    CONSTRAINT fk_exercises_client FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE,
    CONSTRAINT fk_exercises_activity FOREIGN KEY (activity_id) REFERENCES activities(id) ON DELETE SET NULL,
    CONSTRAINT fk_exercises_machine FOREIGN KEY (machine_id) REFERENCES machines(id) ON DELETE SET NULL
);

CREATE TABLE user_activities (
    user_id BIGINT NOT NULL,
    activity_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, activity_id),
    CONSTRAINT fk_user_activities_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_activities_activity FOREIGN KEY (activity_id) REFERENCES activities(id) ON DELETE CASCADE
);

CREATE TABLE gym_classes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    client_id BIGINT NOT NULL,
    class_name VARCHAR(180) NOT NULL,
    activity_id BIGINT NULL,
    max_capacity INT NULL,
    enrolled INT NOT NULL DEFAULT 0,
    teacher_id BIGINT NULL,
    date DATE NOT NULL,
    all_day TINYINT(1) NOT NULL DEFAULT 0,
    start_time TIME NULL,
    end_time TIME NULL,
    CONSTRAINT fk_classes_client FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE,
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
    client_id BIGINT NOT NULL,
    gym_class_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    target_date DATE NOT NULL,
    subscribed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    attendance_present TINYINT(1) NULL,
    attendance_marked_at DATETIME NULL,
    CONSTRAINT fk_class_subscriptions_client FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE,
    CONSTRAINT fk_class_subscriptions_class FOREIGN KEY (gym_class_id) REFERENCES gym_classes(id) ON DELETE CASCADE,
    CONSTRAINT fk_class_subscriptions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_gym_class_subscriptions_class_user_date UNIQUE (client_id, gym_class_id, user_id, target_date)
);

CREATE TABLE exercise_personal_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    client_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    exercise_id BIGINT NOT NULL,
    max_weight DOUBLE NOT NULL,
    recorded_at DATE NOT NULL,
    CONSTRAINT fk_exercise_personal_records_client FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE,
    CONSTRAINT fk_exercise_personal_records_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_exercise_personal_records_exercise FOREIGN KEY (exercise_id) REFERENCES exercises(id) ON DELETE CASCADE,
    CONSTRAINT uq_exercise_personal_records_user_exercise UNIQUE (client_id, user_id, exercise_id)
);

CREATE TABLE exercise_personal_record_entries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    client_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    exercise_id BIGINT NOT NULL,
    weight DOUBLE NOT NULL,
    recorded_at DATE NOT NULL,
    CONSTRAINT fk_exercise_personal_record_entries_client FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE,
    CONSTRAINT fk_exercise_personal_record_entries_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_exercise_personal_record_entries_exercise FOREIGN KEY (exercise_id) REFERENCES exercises(id) ON DELETE CASCADE
);

CREATE TABLE routine_templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    client_id BIGINT NOT NULL,
    name VARCHAR(180) NOT NULL,
    created_at DATE NOT NULL,
    CONSTRAINT fk_routine_templates_client FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE
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
    client_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    routine_template_id BIGINT NULL,
    custom_name VARCHAR(180) NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    CONSTRAINT fk_ra_client FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE,
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
    client_id BIGINT NOT NULL,
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
    CONSTRAINT fk_rae_client FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE,
    CONSTRAINT fk_rae_assignment FOREIGN KEY (assignment_id) REFERENCES routine_assignments(id) ON DELETE CASCADE,
    CONSTRAINT fk_rae_exercise FOREIGN KEY (exercise_id) REFERENCES exercises(id) ON DELETE CASCADE
);

CREATE INDEX idx_users_client_type ON users(client_id, user_type);
CREATE INDEX idx_users_client_registration ON users(client_id, registration_date);
CREATE INDEX idx_users_client_renewal ON users(client_id, renewal_date);
CREATE INDEX idx_client_subscriptions_status ON client_subscriptions(client_id, status);
CREATE INDEX idx_client_subscription_payments_period ON client_subscription_payments(client_id, period_end);
CREATE INDEX idx_gym_classes_client_date ON gym_classes(client_id, date);
CREATE INDEX idx_gym_class_subscriptions_client_date ON gym_class_subscriptions(client_id, target_date);
CREATE INDEX idx_payment_movements_client_paid_at ON user_payment_movements(client_id, paid_at);
CREATE INDEX idx_ra_user_dates ON routine_assignments(client_id, user_id, start_date, end_date);
