USE fitexa;

SET @has_target_date := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'gym_class_subscriptions'
      AND COLUMN_NAME = 'target_date'
);

SET @sql := IF(
    @has_target_date = 0,
    'ALTER TABLE gym_class_subscriptions ADD COLUMN target_date DATE NULL AFTER user_id',
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

SET @has_class_index := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'gym_class_subscriptions'
      AND INDEX_NAME = 'idx_gym_class_subscriptions_class'
);

SET @sql := IF(
    @has_class_index = 0,
    'ALTER TABLE gym_class_subscriptions ADD INDEX idx_gym_class_subscriptions_class (gym_class_id)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_old_unique := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'gym_class_subscriptions'
      AND INDEX_NAME = 'uq_gym_class_subscriptions_class_user'
);

SET @sql := IF(
    @has_old_unique > 0,
    'ALTER TABLE gym_class_subscriptions DROP INDEX uq_gym_class_subscriptions_class_user',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_new_unique := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'gym_class_subscriptions'
      AND INDEX_NAME = 'uq_gym_class_subscriptions_class_user_date'
);

SET @sql := IF(
    @has_new_unique = 0,
    'ALTER TABLE gym_class_subscriptions ADD CONSTRAINT uq_gym_class_subscriptions_class_user_date UNIQUE (gym_class_id, user_id, target_date)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
