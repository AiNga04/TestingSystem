-- Add soft-delete metadata without deleting existing positions or accounts.
USE TestingSystem;
SET @migration_sql = IF(
    (SELECT COUNT(*) FROM information_schema.columns
     WHERE table_schema = DATABASE() AND table_name = 'Position' AND column_name = 'DeletedAt') = 0,
    'ALTER TABLE `Position` ADD COLUMN DeletedAt DATETIME NULL',
    'SELECT 1'
);
PREPARE migration_statement FROM @migration_sql;
EXECUTE migration_statement;
DEALLOCATE PREPARE migration_statement;
