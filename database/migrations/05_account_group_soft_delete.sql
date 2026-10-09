-- Add soft-delete metadata while preserving existing Account rows.
USE TestingSystem;
SET @migration_sql = IF(
    (SELECT COUNT(*) FROM information_schema.columns
     WHERE table_schema = DATABASE() AND table_name = 'Account' AND column_name = 'DeletedAt') = 0,
    'ALTER TABLE `Account` ADD COLUMN DeletedAt DATETIME NULL',
    'SELECT 1'
);
PREPARE migration_statement FROM @migration_sql;
EXECUTE migration_statement;
DEALLOCATE PREPARE migration_statement;

-- Add soft-delete metadata while preserving existing Group rows and membership links.
USE TestingSystem;
SET @migration_sql = IF(
    (SELECT COUNT(*) FROM information_schema.columns
     WHERE table_schema = DATABASE() AND table_name = 'Group' AND column_name = 'DeletedAt') = 0,
    'ALTER TABLE `Group` ADD COLUMN DeletedAt DATETIME NULL',
    'SELECT 1'
);
PREPARE migration_statement FROM @migration_sql;
EXECUTE migration_statement;
DEALLOCATE PREPARE migration_statement;
