CREATE DATABASE IF NOT EXISTS fitexa CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE fitexa;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS routine_assignment_exercises;
DROP TABLE IF EXISTS routine_assignment_days;
DROP TABLE IF EXISTS routine_assignments;
DROP TABLE IF EXISTS routine_template_exercises;
DROP TABLE IF EXISTS routine_templates;
DROP TABLE IF EXISTS gym_class_subscriptions;
DROP TABLE IF EXISTS user_payment_movements;
DROP TABLE IF EXISTS gym_class_days;
DROP TABLE IF EXISTS gym_classes;
DROP TABLE IF EXISTS exercises;
DROP TABLE IF EXISTS machines;
DROP TABLE IF EXISTS activities;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS plans;
SET FOREIGN_KEY_CHECKS = 1;

