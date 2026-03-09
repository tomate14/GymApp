SET @fk_exists = (
    SELECT COUNT(*)
    FROM information_schema.referential_constraints
    WHERE constraint_schema = DATABASE()
      AND table_name = 'routine_assignments'
      AND constraint_name = 'fk_ra_template'
);
SET @drop_fk_sql = IF(@fk_exists > 0, 'ALTER TABLE routine_assignments DROP FOREIGN KEY fk_ra_template', 'SELECT 1');
PREPARE stmt FROM @drop_fk_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

ALTER TABLE routine_assignments
    MODIFY COLUMN routine_template_id BIGINT NULL,
    ADD COLUMN IF NOT EXISTS custom_name VARCHAR(180) NULL AFTER routine_template_id;

SET @fk_exists = (
    SELECT COUNT(*)
    FROM information_schema.referential_constraints
    WHERE constraint_schema = DATABASE()
      AND table_name = 'routine_assignments'
      AND constraint_name = 'fk_ra_template'
);
SET @add_fk_sql = IF(
    @fk_exists = 0,
    'ALTER TABLE routine_assignments ADD CONSTRAINT fk_ra_template FOREIGN KEY (routine_template_id) REFERENCES routine_templates(id) ON DELETE CASCADE',
    'SELECT 1'
);
PREPARE stmt FROM @add_fk_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
