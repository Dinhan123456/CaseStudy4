package com.codegym.module4casestudy.service;

import com.codegym.module4casestudy.model.Grade;
import com.codegym.module4casestudy.model.Class;
import com.codegym.module4casestudy.model.Subject;
import com.codegym.module4casestudy.model.User;
import com.codegym.module4casestudy.repository.GradeRepository;
import com.codegym.module4casestudy.repository.ClassRepository;
import com.codegym.module4casestudy.repository.SubjectRepository;
import com.codegym.module4casestudy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class GradeServiceImpl implements IGradeService {
    
    @Autowired
    private GradeRepository gradeRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SubjectRepository subjectRepository;
    
    @Autowired
    private ClassRepository classRepository;
    
    @Override
    public List<Grade> findAll() {
        return gradeRepository.findAll();
    }
    
    @Override
    public List<Grade> findAllActive() {
        return gradeRepository.findAll().stream()
                .filter(grade -> grade.getActive())
                .toList();
    }
    
    @Override
    public Optional<Grade> findById(Long id) {
        return gradeRepository.findById(id);
    }
    
    @Override
    public Grade save(Grade grade) {
        grade.setUpdatedAt(LocalDateTime.now());
        return gradeRepository.save(grade);
    }
    
    @Override
    public Grade update(Long id, Grade grade) {
        Optional<Grade> existingGrade = gradeRepository.findById(id);
        if (existingGrade.isPresent()) {
            Grade updatedGrade = existingGrade.get();
            updatedGrade.setGrade15(grade.getGrade15());
            updatedGrade.setGradeMidterm(grade.getGradeMidterm());
            updatedGrade.setGradeAttendance(grade.getGradeAttendance());
            updatedGrade.setGradeFinal(grade.getGradeFinal());
            updatedGrade.setNotes(grade.getNotes());
            updatedGrade.setUpdatedAt(LocalDateTime.now());
            updatedGrade.calculateAverageGrade();
            return gradeRepository.save(updatedGrade);
        }
        throw new RuntimeException("Grade not found with id: " + id);
    }
    
    @Override
    public void deleteById(Long id) {
        Optional<Grade> grade = gradeRepository.findById(id);
        if (grade.isPresent()) {
            grade.get().setActive(false);
            grade.get().setUpdatedAt(LocalDateTime.now());
            gradeRepository.save(grade.get());
        }
    }
    
    @Override
    public List<Grade> findByStudentId(Long studentId) {
        return gradeRepository.findByStudentIdAndActiveTrue(studentId);
    }
    
    @Override
    public List<Grade> findBySubjectId(Long subjectId) {
        return gradeRepository.findBySubjectIdAndActiveTrue(subjectId);
    }
    
    @Override
    public List<Grade> findByClassId(Long classId) {
        return gradeRepository.findByClassEntityIdAndActiveTrue(classId);
    }
    
    @Override
    public Optional<Grade> findByStudentIdAndSubjectId(Long studentId, Long subjectId) {
        return gradeRepository.findByStudentIdAndSubjectIdAndActiveTrue(studentId, subjectId);
    }
    
    @Override
    public List<Grade> findByClassIdAndSubjectId(Long classId, Long subjectId) {
        return gradeRepository.findByClassEntityIdAndSubjectIdAndActiveTrue(classId, subjectId);
    }
    
    @Override
    public List<Grade> findBySemesterAndAcademicYear(String semester, String academicYear) {
        return gradeRepository.findBySemesterAndAcademicYearAndActiveTrue(semester, academicYear);
    }
    
    @Override
    public List<Grade> findByGradeRank(String gradeRank) {
        return gradeRepository.findByGradeRankAndActiveTrue(gradeRank);
    }
    
    @Override
    public List<Grade> searchByKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return findAllActive();
        }
        return gradeRepository.searchByKeyword(keyword.trim());
    }
    
    @Override
    public List<Object[]> getGradeStatisticsByClass(Long classId) {
        return gradeRepository.getGradeStatisticsByClass(classId);
    }
    
    @Override
    public List<Object[]> getGradeStatisticsBySubject(Long subjectId) {
        return gradeRepository.getGradeStatisticsBySubject(subjectId);
    }
    
    @Override
    public Double getAverageGradeByClass(Long classId) {
        return gradeRepository.getAverageGradeByClass(classId);
    }
    
    @Override
    public Double getAverageGradeBySubject(Long subjectId) {
        return gradeRepository.getAverageGradeBySubject(subjectId);
    }
    
    @Override
    public boolean existsByStudentIdAndSubjectId(Long studentId, Long subjectId) {
        return gradeRepository.existsByStudentIdAndSubjectIdAndActiveTrue(studentId, subjectId);
    }
    
    @Override
    public Grade createGrade(Long studentId, Long subjectId, Long classId, 
                           Double grade15, Double gradeMidterm, Double gradeAttendance, Double gradeFinal,
                           String semester, String academicYear, String notes, Long createdById) {
        
        // Kiểm tra xem sinh viên đã có điểm cho môn học này chưa
        if (existsByStudentIdAndSubjectId(studentId, subjectId)) {
            throw new RuntimeException("Sinh viên đã có điểm cho môn học này!");
        }
        
        // Lấy thông tin sinh viên, môn học, lớp học
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sinh viên!"));
        
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy môn học!"));
        
        Class classEntity = null;
        if (classId != null) {
            classEntity = classRepository.findById(classId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy lớp học!"));
        }
        
        User createdBy = null;
        if (createdById != null) {
            createdBy = userRepository.findById(createdById)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người tạo!"));
        }
        
        // Tạo điểm mới
        Grade grade = new Grade(student, subject, classEntity);
        grade.setGrade15(grade15);
        grade.setGradeMidterm(gradeMidterm);
        grade.setGradeAttendance(gradeAttendance);
        grade.setGradeFinal(gradeFinal);
        grade.setSemester(semester);
        grade.setAcademicYear(academicYear);
        grade.setNotes(notes);
        grade.setCreatedBy(createdBy);
        
        // Tính điểm trung bình và xếp hạng
        grade.calculateAverageGrade();
        
        return gradeRepository.save(grade);
    }
    
    @Override
    public Grade updateGrade(Long gradeId, Double grade15, Double gradeMidterm, 
                           Double gradeAttendance, Double gradeFinal, String notes) {
        
        Optional<Grade> existingGrade = gradeRepository.findById(gradeId);
        if (existingGrade.isPresent()) {
            Grade grade = existingGrade.get();
            grade.setGrade15(grade15);
            grade.setGradeMidterm(gradeMidterm);
            grade.setGradeAttendance(gradeAttendance);
            grade.setGradeFinal(gradeFinal);
            grade.setNotes(notes);
            grade.setUpdatedAt(LocalDateTime.now());
            
            // Tính lại điểm trung bình và xếp hạng
            grade.calculateAverageGrade();
            
            return gradeRepository.save(grade);
        }
        throw new RuntimeException("Không tìm thấy điểm cần cập nhật!");
    }
    
    @Override
    public Grade saveGrade(Long studentId, Long subjectId, Long classId, 
                          Double midtermScore, Double finalScore, Double attendanceScore, 
                          Double assignmentScore, String notes) {
        
        // Kiểm tra xem đã có điểm cho sinh viên và môn học này chưa
        Optional<Grade> existingGrade = findByStudentIdAndSubjectId(studentId, subjectId);
        
        if (existingGrade.isPresent()) {
            // Cập nhật điểm hiện có
            Grade grade = existingGrade.get();
            grade.setGradeMidterm(midtermScore);
            grade.setGradeFinal(finalScore);
            grade.setGradeAttendance(attendanceScore);
            grade.setGrade15(assignmentScore); // Sử dụng grade15 cho assignment score
            grade.setNotes(notes);
            grade.setUpdatedAt(LocalDateTime.now());
            
            // Tính lại điểm trung bình và xếp hạng
            grade.calculateAverageGrade();
            
            return gradeRepository.save(grade);
        } else {
            // Tạo điểm mới
            User student = userRepository.findById(studentId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sinh viên!"));
            
            Subject subject = subjectRepository.findById(subjectId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy môn học!"));
            
            Class classEntity = null;
            if (classId != null) {
                classEntity = classRepository.findById(classId)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy lớp học!"));
            }
            
            Grade grade = new Grade(student, subject, classEntity);
            grade.setGradeMidterm(midtermScore);
            grade.setGradeFinal(finalScore);
            grade.setGradeAttendance(attendanceScore);
            grade.setGrade15(assignmentScore); // Sử dụng grade15 cho assignment score
            grade.setSemester("2024-2025"); // Default semester
            grade.setAcademicYear("2024-2025"); // Default academic year
            grade.setNotes(notes);
            
            // Tính điểm trung bình và xếp hạng
            grade.calculateAverageGrade();
            
            return gradeRepository.save(grade);
        }
    }
}
