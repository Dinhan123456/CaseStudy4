-- =========================================
-- RESET & CREATE DATABASE
-- =========================================
CREATE DATABASE student_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE student_db;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =========================================
-- CORE TABLES
-- =========================================

-- users: tài khoản đăng nhập cho cả ADMIN/TEACHER/STUDENT
CREATE TABLE users (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       username VARCHAR(50) NOT NULL UNIQUE,
                       password VARCHAR(100) NOT NULL,
                       full_name VARCHAR(100),
                       email VARCHAR(100),
                       phone VARCHAR(20),
                       role ENUM('ADMIN', 'TEACHER', 'STUDENT') NOT NULL,
                       enabled BOOLEAN DEFAULT TRUE,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- classes: thêm capacity để ràng buộc số lượng
CREATE TABLE classes (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         name VARCHAR(100) NOT NULL,
                         description TEXT,
                         capacity INT NOT NULL DEFAULT 40,
                         status BOOLEAN DEFAULT TRUE,
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                         UNIQUE KEY uk_classes_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- subjects
CREATE TABLE subjects (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          name VARCHAR(100) NOT NULL,
                          description TEXT,
                          credits INT DEFAULT 3,
                          active BOOLEAN DEFAULT TRUE,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          UNIQUE KEY uk_subjects_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- rooms
CREATE TABLE rooms (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       room_code VARCHAR(20) NOT NULL,
                       room_name VARCHAR(100),
                       building VARCHAR(100),
                       floor INT,
                       capacity INT,
                       equipment TEXT,
                       room_type VARCHAR(50),
                       active BOOLEAN DEFAULT TRUE,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       UNIQUE KEY uk_rooms_room_code (room_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- time_slots
CREATE TABLE time_slots (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            period_name VARCHAR(50),
                            period_number INT NOT NULL,
                            start_time TIME NOT NULL,
                            end_time TIME NOT NULL,
                            is_break BOOLEAN DEFAULT FALSE,
                            active BOOLEAN DEFAULT TRUE,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                            UNIQUE KEY uk_timeslot_period (period_number),
                            UNIQUE KEY uk_timeslot_time (start_time, end_time),
                            CONSTRAINT chk_timeslot_time CHECK (start_time < end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- registration_periods
CREATE TABLE registration_periods (
                                      id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                      period_name VARCHAR(100) NOT NULL,
                                      description TEXT,
                                      start_time DATETIME NOT NULL,
                                      end_time DATETIME NOT NULL,
                                      max_credits_per_student INT,
                                      max_subjects_per_student INT,
                                      status ENUM('SCHEDULED','OPEN','CLOSED') DEFAULT 'SCHEDULED',
                                      active BOOLEAN DEFAULT TRUE,
                                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                      updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                      created_by BIGINT,
                                      CONSTRAINT fk_reg_period_created_by FOREIGN KEY (created_by) REFERENCES users(id),
                                      CONSTRAINT chk_reg_period_time CHECK (start_time < end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- students: 1-1 với users(id) có role=STUDENT, class_id là "lớp chủ quản" (home class)
CREATE TABLE students (
                          id BIGINT PRIMARY KEY, -- == users.id
                          full_name VARCHAR(100),
                          email VARCHAR(100),
                          phone VARCHAR(20),
                          student_code VARCHAR(20) UNIQUE,
                          gender ENUM('M', 'F'),
                          date_of_birth DATE,
                          address TEXT,
                          class_id BIGINT,
                          active BOOLEAN DEFAULT TRUE,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          CONSTRAINT fk_students_user FOREIGN KEY (id) REFERENCES users(id) ON DELETE CASCADE,
                          CONSTRAINT fk_students_class FOREIGN KEY (class_id) REFERENCES classes(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- teacher_class: giáo viên phụ trách một lớp (có thể là GVCN hoặc quản lý)
CREATE TABLE teacher_class (
                               teacher_id BIGINT,
                               class_id BIGINT,
                               PRIMARY KEY (teacher_id, class_id),
                               CONSTRAINT fk_tc_teacher FOREIGN KEY (teacher_id) REFERENCES users(id),
                               CONSTRAINT fk_tc_class FOREIGN KEY (class_id) REFERENCES classes(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- class_subject: gán GV dạy môn cho lớp (trọng tâm)
CREATE TABLE class_subject (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               class_id BIGINT NOT NULL,
                               subject_id BIGINT NOT NULL,
                               teacher_id BIGINT NOT NULL,
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               UNIQUE KEY uk_class_subject_teacher (class_id, subject_id, teacher_id),
                               CONSTRAINT fk_cs_class FOREIGN KEY (class_id) REFERENCES classes(id),
                               CONSTRAINT fk_cs_subject FOREIGN KEY (subject_id) REFERENCES subjects(id),
                               CONSTRAINT fk_cs_teacher FOREIGN KEY (teacher_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- schedules: lịch học chính (nên dùng thay cho timetables)
CREATE TABLE schedules (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                           class_id BIGINT NOT NULL,
                           subject_id BIGINT NOT NULL,
                           teacher_id BIGINT NOT NULL,
                           room_id BIGINT NOT NULL,
                           time_slot_id BIGINT NOT NULL,
                           day_of_week ENUM('MON','TUE','WED','THU','FRI','SAT','SUN') NOT NULL,
                           start_date DATE NOT NULL,
                           end_date DATE NOT NULL,
                           notes TEXT,
                           active BOOLEAN DEFAULT TRUE,
                           status ENUM('PENDING','CONFIRMED','COMPLETED','CANCELLED') DEFAULT 'PENDING',
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                           created_by BIGINT,
                           CONSTRAINT fk_sch_class FOREIGN KEY (class_id) REFERENCES classes(id),
                           CONSTRAINT fk_sch_subject FOREIGN KEY (subject_id) REFERENCES subjects(id),
                           CONSTRAINT fk_sch_teacher FOREIGN KEY (teacher_id) REFERENCES users(id),
                           CONSTRAINT fk_sch_room FOREIGN KEY (room_id) REFERENCES rooms(id),
                           CONSTRAINT fk_sch_timeslot FOREIGN KEY (time_slot_id) REFERENCES time_slots(id),
                           CONSTRAINT fk_sch_created_by FOREIGN KEY (created_by) REFERENCES users(id),
                           CONSTRAINT chk_schedule_dates CHECK (start_date <= end_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- student_class: enrollment thực tế theo lớp (nhiều-nhiều)
CREATE TABLE student_class (
                               student_id BIGINT,
                               class_id BIGINT,
                               PRIMARY KEY (student_id, class_id),
                               CONSTRAINT fk_sc_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
                               CONSTRAINT fk_sc_class FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- student_registrations: đăng ký lịch theo kỳ
CREATE TABLE student_registrations (
                                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                       student_id BIGINT NOT NULL,
                                       schedule_id BIGINT NOT NULL,
                                       registration_period_id BIGINT NOT NULL,
                                       status ENUM('PENDING','APPROVED','REJECTED','CANCELLED') DEFAULT 'APPROVED',
                                       active BOOLEAN DEFAULT TRUE,
                                       registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                       notes TEXT,
                                       CONSTRAINT fk_sr_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
                                       CONSTRAINT fk_sr_schedule FOREIGN KEY (schedule_id) REFERENCES schedules(id) ON DELETE CASCADE,
                                       CONSTRAINT fk_sr_period FOREIGN KEY (registration_period_id) REFERENCES registration_periods(id),
                                       UNIQUE KEY uk_sr_unique (student_id, schedule_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- grades: gắn theo student + subject + class + kỳ (để không trùng giữa các kỳ)
CREATE TABLE grades (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        student_id BIGINT NOT NULL,
                        subject_id BIGINT NOT NULL,
                        class_id BIGINT NOT NULL,
                        registration_period_id BIGINT,
                        score DECIMAL(4,2) CHECK (score >= 0 AND score <= 10),
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        CONSTRAINT fk_grade_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
                        CONSTRAINT fk_grade_subject FOREIGN KEY (subject_id) REFERENCES subjects(id),
                        CONSTRAINT fk_grade_class FOREIGN KEY (class_id) REFERENCES classes(id),
                        CONSTRAINT fk_grade_period FOREIGN KEY (registration_period_id) REFERENCES registration_periods(id),
                        UNIQUE KEY uk_grade_unique (student_id, subject_id, class_id, registration_period_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- (TÙY CHỌN) timetables: nếu vẫn muốn giữ, chuẩn hóa room_id & (có thể) time_slot_id
CREATE TABLE timetables (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            class_id BIGINT NOT NULL,
                            subject_id BIGINT NOT NULL,
                            teacher_id BIGINT NOT NULL,
                            room_id BIGINT NOT NULL,
                            day_of_week ENUM('MON','TUE','WED','THU','FRI','SAT','SUN') NOT NULL,
                            start_time TIME NOT NULL,
                            end_time TIME NOT NULL,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            CONSTRAINT fk_tt_class FOREIGN KEY (class_id) REFERENCES classes(id),
                            CONSTRAINT fk_tt_subject FOREIGN KEY (subject_id) REFERENCES subjects(id),
                            CONSTRAINT fk_tt_teacher FOREIGN KEY (teacher_id) REFERENCES users(id),
                            CONSTRAINT fk_tt_room FOREIGN KEY (room_id) REFERENCES rooms(id),
                            CONSTRAINT chk_tt_time CHECK (start_time < end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================
-- HỖ TRỢ TRUY VẤN NHANH (INDEXES)
-- =========================================

-- Schedules hay lọc theo teacher/day/slot/active
CREATE INDEX idx_sch_teacher_day_slot ON schedules(teacher_id, day_of_week, time_slot_id, active);
CREATE INDEX idx_sch_class_day_slot   ON schedules(class_id, day_of_week, time_slot_id, active);
CREATE INDEX idx_sch_room_day_slot    ON schedules(room_id, day_of_week, time_slot_id, active);

-- Tìm grade theo student/subject/kỳ
CREATE INDEX idx_grade_student_subject ON grades(student_id, subject_id);
CREATE INDEX idx_grade_student_class   ON grades(student_id, class_id);

-- Tìm đăng ký theo student/kỳ
CREATE INDEX idx_sr_student_period ON student_registrations(student_id, registration_period_id);

SET FOREIGN_KEY_CHECKS = 1;










USE student_db;

DELIMITER $$

DROP PROCEDURE IF EXISTS apply_patch $$
CREATE PROCEDURE apply_patch()
BEGIN
  -- ===== classes.capacity =====
  IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'classes' AND column_name = 'capacity'
  ) THEN
ALTER TABLE classes ADD COLUMN capacity INT NOT NULL DEFAULT 40;
END IF;

  -- ===== students FKs (1-1 users, home class) =====
  IF NOT EXISTS (
      SELECT 1 FROM information_schema.table_constraints
      WHERE constraint_schema = DATABASE() AND table_name = 'students' AND constraint_name = 'fk_students_user'
  ) THEN
ALTER TABLE students
    ADD CONSTRAINT fk_students_user FOREIGN KEY (id) REFERENCES users(id) ON DELETE CASCADE;
END IF;

  IF NOT EXISTS (
      SELECT 1 FROM information_schema.table_constraints
      WHERE constraint_schema = DATABASE() AND table_name = 'students' AND constraint_name = 'fk_students_class'
  ) THEN
ALTER TABLE students
    ADD CONSTRAINT fk_students_class FOREIGN KEY (class_id) REFERENCES classes(id);
END IF;

  -- ===== schedules.created_by → BIGINT + FK users(id) =====
  IF EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'schedules' AND column_name = 'created_by' AND data_type <> 'bigint'
  ) THEN
ALTER TABLE schedules MODIFY COLUMN created_by BIGINT NULL;
END IF;

  IF NOT EXISTS (
      SELECT 1 FROM information_schema.table_constraints
      WHERE constraint_schema = DATABASE() AND table_name = 'schedules' AND constraint_name = 'fk_schedules_created_by'
  ) THEN
ALTER TABLE schedules
    ADD CONSTRAINT fk_schedules_created_by FOREIGN KEY (created_by) REFERENCES users(id);
END IF;

  -- ===== registration_periods.created_by → BIGINT + FK users(id) =====
  IF EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'registration_periods' AND column_name = 'created_by' AND data_type <> 'bigint'
  ) THEN
ALTER TABLE registration_periods MODIFY COLUMN created_by BIGINT NULL;
END IF;

  IF NOT EXISTS (
      SELECT 1 FROM information_schema.table_constraints
      WHERE constraint_schema = DATABASE() AND table_name = 'registration_periods' AND constraint_name = 'fk_regperiod_created_by'
  ) THEN
ALTER TABLE registration_periods
    ADD CONSTRAINT fk_regperiod_created_by FOREIGN KEY (created_by) REFERENCES users(id);
END IF;

  -- ===== student_registrations: chặn đăng ký trùng =====
  IF NOT EXISTS (
      SELECT 1 FROM information_schema.statistics
      WHERE table_schema = DATABASE() AND table_name = 'student_registrations' AND index_name = 'uk_sr_unique'
  ) THEN
ALTER TABLE student_registrations
    ADD UNIQUE KEY uk_sr_unique (student_id, schedule_id);
END IF;

  -- ===== class_subject: chặn gán trùng =====
  IF NOT EXISTS (
      SELECT 1 FROM information_schema.statistics
      WHERE table_schema = DATABASE() AND table_name = 'class_subject' AND index_name = 'uk_class_subject_teacher'
  ) THEN
ALTER TABLE class_subject
    ADD UNIQUE KEY uk_class_subject_teacher (class_id, subject_id, teacher_id);
END IF;

  -- ===== grades: bổ sung cột + ràng buộc duy nhất =====
  IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'grades' AND column_name = 'class_id'
  ) THEN
ALTER TABLE grades ADD COLUMN class_id BIGINT;
END IF;

  IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'grades' AND column_name = 'registration_period_id'
  ) THEN
ALTER TABLE grades ADD COLUMN registration_period_id BIGINT;
END IF;

  -- chuẩn hoá kiểu điểm -> DECIMAL(4,2) nếu khác
  IF EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'grades' AND column_name = 'score'
        AND (data_type <> 'decimal' OR numeric_precision <> 4 OR numeric_scale <> 2)
  ) THEN
ALTER TABLE grades MODIFY COLUMN score DECIMAL(4,2);
END IF;

  IF NOT EXISTS (
      SELECT 1 FROM information_schema.table_constraints
      WHERE constraint_schema = DATABASE() AND table_name = 'grades' AND constraint_name = 'fk_grade_class'
  ) THEN
ALTER TABLE grades ADD CONSTRAINT fk_grade_class FOREIGN KEY (class_id) REFERENCES classes(id);
END IF;

  IF NOT EXISTS (
      SELECT 1 FROM information_schema.table_constraints
      WHERE constraint_schema = DATABASE() AND table_name = 'grades' AND constraint_name = 'fk_grade_period'
  ) THEN
ALTER TABLE grades ADD CONSTRAINT fk_grade_period FOREIGN KEY (registration_period_id) REFERENCES registration_periods(id);
END IF;

  IF NOT EXISTS (
      SELECT 1 FROM information_schema.statistics
      WHERE table_schema = DATABASE() AND table_name = 'grades' AND index_name = 'uk_grade_unique'
  ) THEN
ALTER TABLE grades
    ADD UNIQUE KEY uk_grade_unique (student_id, subject_id, class_id, registration_period_id);
END IF;

  -- ===== Index tối ưu cho schedules =====
  IF NOT EXISTS (
      SELECT 1 FROM information_schema.statistics
      WHERE table_schema = DATABASE() AND table_name = 'schedules' AND index_name = 'idx_sch_teacher_day_slot'
  ) THEN
CREATE INDEX idx_sch_teacher_day_slot ON schedules(teacher_id, day_of_week, time_slot_id, active);
END IF;

  IF NOT EXISTS (
      SELECT 1 FROM information_schema.statistics
      WHERE table_schema = DATABASE() AND table_name = 'schedules' AND index_name = 'idx_sch_class_day_slot'
  ) THEN
CREATE INDEX idx_sch_class_day_slot ON schedules(class_id, day_of_week, time_slot_id, active);
END IF;

  IF NOT EXISTS (
      SELECT 1 FROM information_schema.statistics
      WHERE table_schema = DATABASE() AND table_name = 'schedules' AND index_name = 'idx_sch_room_day_slot'
  ) THEN
CREATE INDEX idx_sch_room_day_slot ON schedules(room_id, day_of_week, time_slot_id, active);
END IF;

END $$
DELIMITER ;

CALL apply_patch();
DROP PROCEDURE apply_patch;
