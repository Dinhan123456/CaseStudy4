-- FIX DATABASE SCHEMA TO MATCH JAVA CODE
-- =========================================
USE student_db;

-- =========================================
-- 1. FIX student_class TABLE - Change FK to reference users instead of students
-- =========================================
-- Drop existing foreign key constraint
ALTER TABLE student_class DROP FOREIGN KEY fk_sc_student;

-- Add new foreign key constraint to reference users directly
ALTER TABLE student_class 
ADD CONSTRAINT fk_sc_student FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE;

-- =========================================
-- 2. FIX student_registrations TABLE (if needed)
-- =========================================
-- Thêm các cột còn thiếu
ALTER TABLE student_registrations
ADD COLUMN subject_id BIGINT NULL AFTER student_id,
ADD COLUMN class_id BIGINT NULL AFTER subject_id;

-- Thêm foreign key constraints
ALTER TABLE student_registrations
ADD CONSTRAINT fk_sr_subject FOREIGN KEY (subject_id) REFERENCES subjects(id),
ADD CONSTRAINT fk_sr_class FOREIGN KEY (class_id) REFERENCES classes(id);

-- Sửa enum status để khớp với Java code
ALTER TABLE student_registrations
MODIFY COLUMN status ENUM('REGISTERED','CONFIRMED','WAITLISTED','CANCELLED','COMPLETED') DEFAULT 'REGISTERED';

-- Sửa schedule_id thành nullable (vì có thể đăng ký theo môn, không nhất thiết có lịch)
ALTER TABLE student_registrations
MODIFY COLUMN schedule_id BIGINT NULL;

-- =========================================
-- 3. ADD MISSING INDEXES
-- =========================================
-- Index cho student_registrations
CREATE INDEX idx_sr_student_subject ON student_registrations(student_id, subject_id);
CREATE INDEX idx_sr_student_class ON student_registrations(student_id, class_id);
CREATE INDEX idx_sr_subject_class ON student_registrations(subject_id, class_id);

-- =========================================
-- 4. VERIFY CHANGES
-- =========================================
-- Kiểm tra foreign keys
SELECT
    TABLE_NAME,
    COLUMN_NAME,
    CONSTRAINT_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM information_schema.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'student_db'
AND TABLE_NAME IN ('student_class', 'teacher_class', 'student_registrations')
AND REFERENCED_TABLE_NAME IS NOT NULL;

-- =========================================
-- 5. SAMPLE DATA INSERT (if needed)
-- =========================================
-- Insert test users with BCrypt passwords (password: 123456)
INSERT IGNORE INTO users (username, password, full_name, email, phone, role, enabled) VALUES
('admin', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'Quản trị viên', 'admin@codegym.vn', '0901234567', 'ADMIN', TRUE),
('teacher01', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'Nguyễn Văn Giáo', 'teacher01@codegym.vn', '0902345678', 'TEACHER', TRUE),
('teacher02', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'Trần Thị Giảng', 'teacher02@codegym.vn', '0903456789', 'TEACHER', TRUE),
('student01', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'Lê Văn Học', 'student01@codegym.vn', '0904567890', 'STUDENT', TRUE),
('student02', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'Phạm Thị Sinh', 'student02@codegym.vn', '0905678901', 'STUDENT', TRUE),
('student03', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'Hoàng Văn Tài', 'student03@codegym.vn', '0906789012', 'STUDENT', TRUE);

-- Insert sample classes
INSERT IGNORE INTO classes (name, description, capacity, status) VALUES
('C0924G1', 'Lớp Java Backend cơ bản', 25, TRUE),
('C0924G2', 'Lớp Java Backend nâng cao', 30, TRUE),
('C0924G3', 'Lớp Frontend React', 20, TRUE);

-- Insert sample subjects
INSERT IGNORE INTO subjects (name, description, credits, active) VALUES
('Lập trình Java cơ bản', 'Học các khái niệm cơ bản về Java', 4, TRUE),
('Spring Boot', 'Framework Spring Boot cho Java', 3, TRUE),
('MySQL Database', 'Cơ sở dữ liệu MySQL', 2, TRUE),
('React Frontend', 'Thư viện React cho Frontend', 3, TRUE);

-- Tạo kỳ đăng ký mẫu nếu chưa có
INSERT IGNORE INTO registration_periods (
    period_name,
    description,
    start_time,
    end_time,
    status,
    active
) VALUES (
    'Kỳ đăng ký 2024-2025',
    'Kỳ đăng ký môn học cho năm học 2024-2025',
    NOW(),
    DATE_ADD(NOW(), INTERVAL 6 MONTH),
    'OPEN',
    TRUE
);
