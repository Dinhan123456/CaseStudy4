-- Migration script để chuẩn hóa database theo yêu cầu
-- Thực hiện từng bước cẩn thận

-- 1. Backup dữ liệu hiện tại (nếu có)
-- CREATE TABLE schedules_backup AS SELECT * FROM schedules;
-- CREATE TABLE grades_backup AS SELECT * FROM grades;

-- 2. Cập nhật schedules table
-- Thay đổi day_of_week từ ENUM sang TINYINT
ALTER TABLE schedules 
    MODIFY COLUMN day_of_week TINYINT NOT NULL CHECK (day_of_week >= 0 AND day_of_week <= 6);

-- Cập nhật dữ liệu day_of_week từ string sang number (nếu cần)
-- UPDATE schedules SET day_of_week = 1 WHERE day_of_week = 'MON';
-- UPDATE schedules SET day_of_week = 2 WHERE day_of_week = 'TUE';
-- UPDATE schedules SET day_of_week = 3 WHERE day_of_week = 'WED';
-- UPDATE schedules SET day_of_week = 4 WHERE day_of_week = 'THU';
-- UPDATE schedules SET day_of_week = 5 WHERE day_of_week = 'FRI';
-- UPDATE schedules SET day_of_week = 6 WHERE day_of_week = 'SAT';
-- UPDATE schedules SET day_of_week = 0 WHERE day_of_week = 'SUN';

-- Thay đổi created_by từ VARCHAR sang BIGINT FK
ALTER TABLE schedules 
    MODIFY COLUMN created_by BIGINT,
    ADD CONSTRAINT fk_schedules_created_by 
        FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL;

-- 3. Thêm unique constraints cho conflict checking
ALTER TABLE schedules 
    ADD CONSTRAINT unique_teacher_schedule 
        UNIQUE (teacher_id, day_of_week, time_slot_id, start_date, end_date);

ALTER TABLE schedules 
    ADD CONSTRAINT unique_room_schedule 
        UNIQUE (room_id, day_of_week, time_slot_id, start_date, end_date);

ALTER TABLE schedules 
    ADD CONSTRAINT unique_class_schedule 
        UNIQUE (class_id, day_of_week, time_slot_id, start_date, end_date);

-- 4. Cập nhật grades table với CHECK constraints
ALTER TABLE grades 
    MODIFY COLUMN grade_15 DECIMAL(4,2) CHECK (grade_15 >= 0 AND grade_15 <= 10),
    MODIFY COLUMN grade_midterm DECIMAL(4,2) CHECK (grade_midterm >= 0 AND grade_midterm <= 10),
    MODIFY COLUMN grade_attendance DECIMAL(4,2) CHECK (grade_attendance >= 0 AND grade_attendance <= 10),
    MODIFY COLUMN grade_final DECIMAL(4,2) CHECK (grade_final >= 0 AND grade_final <= 10),
    MODIFY COLUMN average_grade DECIMAL(4,2) CHECK (average_grade >= 0 AND average_grade <= 10);

-- Thêm unique constraint cho grades
ALTER TABLE grades 
    ADD CONSTRAINT unique_student_subject_grades 
        UNIQUE (student_id, subject_id, class_id);

-- 5. Thêm capacity field cho classes table
ALTER TABLE classes 
    ADD COLUMN capacity INT NOT NULL DEFAULT 30;

-- 6. Tạo bảng registration_periods nếu chưa có
CREATE TABLE IF NOT EXISTS registration_periods (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    period_name VARCHAR(100) NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    description TEXT,
    max_subjects_per_student INT DEFAULT 10,
    max_credits_per_student INT DEFAULT 25,
    status VARCHAR(20) DEFAULT 'SCHEDULED',
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    active BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (created_by) REFERENCES users(id),
    CHECK (status IN ('SCHEDULED', 'OPEN', 'CLOSED', 'CANCELLED')),
    CHECK (end_time > start_time)
);

-- 7. Tạo dữ liệu mẫu cho registration_periods
INSERT INTO registration_periods (period_name, start_time, end_time, description, status) VALUES
    ('Đăng ký môn học Kỳ 1 năm 2024', 
     '2024-08-01 08:00:00', 
     '2024-08-31 17:00:00', 
     'Kỳ đăng ký môn học cho học kỳ 1 năm học 2024-2025', 
     'OPEN'),
    ('Đăng ký môn học Kỳ 2 năm 2024', 
     '2025-01-01 08:00:00', 
     '2025-01-31 17:00:00', 
     'Kỳ đăng ký môn học cho học kỳ 2 năm học 2024-2025', 
     'SCHEDULED');

-- 8. Cập nhật capacity cho các lớp hiện có (nếu có dữ liệu)
UPDATE classes SET capacity = 30 WHERE capacity IS NULL OR capacity = 0;

-- 9. Validation sau migration
-- Kiểm tra dữ liệu sau khi migration
SELECT 'schedules day_of_week validation' as check_name, 
       COUNT(*) as total_records,
       COUNT(CASE WHEN day_of_week BETWEEN 0 AND 6 THEN 1 END) as valid_records
FROM schedules;

SELECT 'grades validation' as check_name,
       COUNT(*) as total_records,
       COUNT(CASE WHEN grade_15 BETWEEN 0 AND 10 AND grade_midterm BETWEEN 0 AND 10 
                   AND grade_attendance BETWEEN 0 AND 10 AND grade_final BETWEEN 0 AND 10 
                   AND average_grade BETWEEN 0 AND 10 THEN 1 END) as valid_records
FROM grades;

SELECT 'classes capacity validation' as check_name,
       COUNT(*) as total_records,
       COUNT(CASE WHEN capacity > 0 THEN 1 END) as valid_records  
FROM classes;

SELECT 'registration_periods validation' as check_name,
       COUNT(*) as total_records,
       COUNT(CASE WHEN end_time > start_time THEN 1 END) as valid_records
FROM registration_periods;
ALTER TABLE grades 
    ADD CONSTRAINT unique_student_subject_class 
        UNIQUE (student_id, subject_id, class_id);

-- 5. Cập nhật student_registrations và student_class để reference users thay vì students
-- (Nếu bảng students vẫn tồn tại và có dữ liệu)

-- Migrate dữ liệu từ students sang users (nếu cần)
-- INSERT INTO users (username, password, full_name, email, phone, role, enabled)
-- SELECT student_code, 'defaultpass', full_name, email, phone, 'STUDENT', active
-- FROM students 
-- WHERE id NOT IN (SELECT id FROM users);

-- Migrate student_class references
-- UPDATE student_class sc 
-- JOIN students s ON sc.student_id = s.id 
-- JOIN users u ON s.student_code = u.username 
-- SET sc.student_id = u.id;

-- Migrate student_registrations references  
-- UPDATE student_registrations sr 
-- JOIN students s ON sr.student_id = s.id 
-- JOIN users u ON s.student_code = u.username 
-- SET sr.student_id = u.id;

-- Migrate grades references
-- UPDATE grades g 
-- JOIN students s ON g.student_id = s.id 
-- JOIN users u ON s.student_code = u.username 
-- SET g.student_id = u.id;

-- 6. Sau khi migrate xong, có thể rename/drop bảng students (optional)
-- RENAME TABLE students TO students_deprecated;
-- hoặc 
-- DROP TABLE students; -- (Cẩn thận!)

-- 7. Cập nhật foreign key constraints trong các bảng liên quan
ALTER TABLE student_class 
    DROP FOREIGN KEY student_class_ibfk_1,
    ADD CONSTRAINT fk_student_class_student 
        FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE student_registrations 
    DROP FOREIGN KEY student_registrations_ibfk_1,
    ADD CONSTRAINT fk_student_registrations_student 
        FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE grades 
    DROP FOREIGN KEY grades_ibfk_1,
    ADD CONSTRAINT fk_grades_student 
        FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE;

-- 8. Thêm indexes để tối ưu performance
CREATE INDEX idx_schedules_teacher_day ON schedules(teacher_id, day_of_week);
CREATE INDEX idx_schedules_room_day ON schedules(room_id, day_of_week);  
CREATE INDEX idx_schedules_class_day ON schedules(class_id, day_of_week);
CREATE INDEX idx_grades_student_subject ON grades(student_id, subject_id);
CREATE INDEX idx_student_class_class ON student_class(class_id);

-- 9. Validate dữ liệu sau migration
-- SELECT COUNT(*) as invalid_grades FROM grades 
-- WHERE grade_15 < 0 OR grade_15 > 10 
--    OR grade_midterm < 0 OR grade_midterm > 10
--    OR grade_attendance < 0 OR grade_attendance > 10  
--    OR grade_final < 0 OR grade_final > 10;

-- SELECT COUNT(*) as invalid_day_of_week FROM schedules 
-- WHERE day_of_week < 0 OR day_of_week > 6;

COMMIT;
