CREATE DATABASE IF NOT EXISTS fitexa CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE fitexa;

CREATE TABLE IF NOT EXISTS exercise_personal_record_entries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    exercise_id BIGINT NOT NULL,
    weight DOUBLE NOT NULL,
    recorded_at DATE NOT NULL,
    CONSTRAINT fk_exercise_personal_record_entries_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_exercise_personal_record_entries_exercise FOREIGN KEY (exercise_id) REFERENCES exercises(id) ON DELETE CASCADE
);

CREATE INDEX idx_exercise_pr_entries_user_exercise_date
    ON exercise_personal_record_entries(user_id, exercise_id, recorded_at);

INSERT INTO exercise_personal_record_entries (user_id, exercise_id, weight, recorded_at)
SELECT epr.user_id, epr.exercise_id, epr.max_weight, epr.recorded_at
FROM exercise_personal_records epr
LEFT JOIN exercise_personal_record_entries epre
    ON epre.user_id = epr.user_id
    AND epre.exercise_id = epr.exercise_id
    AND epre.weight = epr.max_weight
    AND epre.recorded_at = epr.recorded_at
WHERE epre.id IS NULL;
