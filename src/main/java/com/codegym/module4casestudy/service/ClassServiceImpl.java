package com.codegym.module4casestudy.service;

import com.codegym.module4casestudy.model.Class;
import com.codegym.module4casestudy.model.ClassSubject;
import com.codegym.module4casestudy.model.Schedule;
import com.codegym.module4casestudy.model.Subject;
import com.codegym.module4casestudy.model.User;
import com.codegym.module4casestudy.repository.ClassRepository;
import com.codegym.module4casestudy.repository.ClassSubjectRepository;
import com.codegym.module4casestudy.repository.ScheduleRepository;
import com.codegym.module4casestudy.repository.SubjectRepository;
import com.codegym.module4casestudy.repository.UserRepository;
import com.codegym.module4casestudy.model.StudentRegistration;
import com.codegym.module4casestudy.model.Grade;
import com.codegym.module4casestudy.repository.StudentRegistrationRepository;
import com.codegym.module4casestudy.repository.GradeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

//Nhi thêm để hoàn thiện chức năng crud
@Service
@Transactional
public class ClassServiceImpl implements IClassService {

    @Autowired
    private ClassRepository classRepository;

    @Autowired
    private ClassSubjectRepository classSubjectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private StudentRegistrationRepository studentRegistrationRepository;

    @Autowired
    private GradeRepository gradeRepository;

    @Override
    public List<Class> findAll() {
        return classRepository.findAll();
    }

    @Override
    public List<Class> findAllWithStudentsAndTeachers() {
        return classRepository.findAllWithStudentsAndTeachers();
    }

    @Override
    public Optional<Class> findById(Long id) {
        return classRepository.findById(id);
    }

    @Override
    public Class findByIdWithStudentsAndTeachers(Long id) {
        return classRepository.findByIdWithStudentsAndTeachers(id);
    }

    @Override
    public Class save(Class classEntity) {
        return classRepository.save(classEntity);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Class classEntity = classRepository.findById(id).orElse(null);
        if (classEntity == null) {
            throw new RuntimeException("Không tìm thấy lớp học với ID: " + id);
        }
        
        // Xóa các bản ghi liên quan trước khi xóa lớp học
        
        // 1. Xóa các bản ghi trong bảng class_subject
        List<ClassSubject> classSubjects = classSubjectRepository.findByClassEntityId(id);
        if (classSubjects != null && !classSubjects.isEmpty()) {
            classSubjectRepository.deleteAll(classSubjects);
        }
        
        // 2. Xóa các bản ghi trong bảng schedules
        List<Schedule> schedules = scheduleRepository.findByClassId(id);
        if (schedules != null && !schedules.isEmpty()) {
            scheduleRepository.deleteAll(schedules);
        }

        // 3. Xóa các bản ghi điểm (grades) liên quan tới lớp
        List<Grade> grades = gradeRepository.findByClassEntityIdAndActiveTrue(id);
        if (grades != null && !grades.isEmpty()) {
            gradeRepository.deleteAll(grades);
        }

        // 4. Xóa các đăng ký học (student_registrations) liên quan tới lớp
        List<StudentRegistration> registrations = studentRegistrationRepository.findByClassIdAndActiveTrue(id);
        if (registrations != null && !registrations.isEmpty()) {
            studentRegistrationRepository.deleteAll(registrations);
        }

        // 5. Gỡ liên kết Many-to-Many với students và teachers để dọn bảng trung gian
        if (classEntity.getStudents() != null && !classEntity.getStudents().isEmpty()) {
            classEntity.getStudents().clear();
        }
        if (classEntity.getTeachers() != null && !classEntity.getTeachers().isEmpty()) {
            classEntity.getTeachers().clear();
        }
        classRepository.save(classEntity);

        // 6. Xóa lớp học
        classRepository.deleteById(id);
    }

    @Override
    public List<Class> searchByKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return findAll();
        }
        return classRepository.findByNameContainingIgnoreCase(keyword);
    }

    @Override
    public boolean existsByName(String name) {
        return classRepository.existsByName(name);
    }

    @Override
    public boolean existsByNameAndIdNot(String name, Long id) {
        return classRepository.existsByNameAndIdNot(name, id);
    }

    @Override
    public void addStudentToClass(Long classId, Long studentId) {
        Class classEntity = classRepository.findById(classId).orElse(null);
        User student = userRepository.findById(studentId).orElse(null);

        if (classEntity != null && student != null) {
            classEntity.getStudents().add(student);
            classRepository.save(classEntity);
        }
    }

    @Override
    public void removeStudentFromClass(Long classId, Long studentId) {
        Class classEntity = classRepository.findById(classId).orElse(null);
        User student = userRepository.findById(studentId).orElse(null);

        if (classEntity != null && student != null) {
            classEntity.getStudents().remove(student);
            classRepository.save(classEntity);
        }
    }

    @Override
    public void addTeacherToClass(Long classId, Long teacherId) {
        Class classEntity = classRepository.findById(classId).orElse(null);
        User teacher = userRepository.findById(teacherId).orElse(null);

        if (classEntity != null && teacher != null) {
            classEntity.getTeachers().add(teacher);
            classRepository.save(classEntity);
        }
    }

    @Override
    public void removeTeacherFromClass(Long classId, Long teacherId) {
        Class classEntity = classRepository.findById(classId).orElse(null);
        User teacher = userRepository.findById(teacherId).orElse(null);

        if (classEntity != null && teacher != null) {
            classEntity.getTeachers().remove(teacher);
            classRepository.save(classEntity);
        }
    }

    @Override
    public List<ClassSubject> getClassSubjects(Long classId) {
        return classSubjectRepository.findByClassIdWithSubjectAndTeacher(classId);
    }

    @Override
    public void assignTeacherToSubject(Long classId, Long subjectId, Long teacherId) {
        Class classEntity = classRepository.findById(classId).orElse(null);
        Subject subject = subjectRepository.findById(subjectId).orElse(null);
        User teacher = userRepository.findById(teacherId).orElse(null);

        if (classEntity != null && subject != null && teacher != null) {
            ClassSubject classSubject = new ClassSubject(classEntity, subject, teacher);
            classSubjectRepository.save(classSubject);
        }
    }

    @Override
    public void removeTeacherFromSubject(Long classId, Long subjectId, Long teacherId) {
        classSubjectRepository.deleteByClassEntityIdAndSubjectIdAndTeacherId(classId, subjectId, teacherId);
    }

    @Override
    public List<Class> findClassesByStudentId(Long studentId) {
        return classRepository.findClassesByStudentId(studentId);
    }

    @Override
    public List<Class> findClassesByTeacherId(Long teacherId) {
        return classRepository.findClassesByTeacherId(teacherId);
    }
}
