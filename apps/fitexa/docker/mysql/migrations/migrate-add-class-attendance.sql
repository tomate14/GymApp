USE fitexa;

ALTER TABLE gym_class_subscriptions
    ADD COLUMN IF NOT EXISTS attendance_present TINYINT(1) NULL AFTER subscribed_at,
    ADD COLUMN IF NOT EXISTS attendance_marked_at DATETIME NULL AFTER attendance_present;
