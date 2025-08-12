-- Migration Script: Add capacity to classes and create registration_periods table
-- Chạy script này để cập nhật database hiện có

USE student_db;

-- 1. Thêm field capacity vào bảng classes (nếu chưa có)
ALTER TABLE classes 
ADD COLUMN IF NOT EXISTS capacity INT NOT NULL DEFAULT 30 
AFTER description;

-- 2. Tạo bảng registration_periods (nếu chưa có)
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

-- 3. Tạo một kỳ đăng ký mẫu (OPEN để test)
INSERT INTO registration_periods (
    period_name, 
    start_time, 
    end_time, 
    description, 
    status, 
    created_by
) VALUES (
    'Đăng ký môn học Kỳ 1 năm 2025', 
    '2025-08-01 08:00:00', 
    '2025-08-31 17:00:00', 
    'Kỳ đăng ký môn học cho học kỳ 1 năm học 2025', 
    'OPEN',
    (SELECT id FROM users WHERE role = 'ADMIN' LIMIT 1)
) ON DUPLICATE KEY UPDATE 
    period_name = VALUES(period_name);

-- 4. Cập nhật capacity cho các lớp hiện có (nếu chưa có giá trị)
UPDATE classes 
SET capacity = 30 
WHERE capacity IS NULL OR capacity = 0;

-- 5. Thêm index cho performance
CREATE INDEX IF NOT EXISTS idx_registration_periods_status ON registration_periods(status);
CREATE INDEX IF NOT EXISTS idx_registration_periods_dates ON registration_periods(start_time, end_time);
CREATE INDEX IF NOT EXISTS idx_classes_capacity ON classes(capacity);

-- 6. Kiểm tra dữ liệu sau migration
SELECT 'Classes with capacity' as info, COUNT(*) as count FROM classes WHERE capacity > 0
UNION ALL
SELECT 'Registration periods' as info, COUNT(*) as count FROM registration_periods WHERE active = true
UNION ALL  
SELECT 'Open registration periods' as info, COUNT(*) as count FROM registration_periods WHERE status = 'OPEN' AND active = true;

COMMIT;
