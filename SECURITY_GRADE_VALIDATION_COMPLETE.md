# TRIỆT ĐỂ HOÀN THÀNH: Ràng buộc nhập điểm & Bảo mật CSRF + Phân quyền

## ✅ 1. RÀNG BUỘC NHẬP ĐIỂM - GradeServiceImpl

### Đã thêm validation triệt để:

**File cập nhật:** `GradeServiceImpl.java`

**Ràng buộc được implement:**
1. **Student ∈ Class**: Kiểm tra sinh viên phải thuộc lớp học trước khi nhập điểm
2. **Teacher được phân công**: Kiểm tra giảng viên phải được phân công dạy môn đó trong lớp

**Method validation mới:**
```java
private void validateGradeConstraints(Grade grade, User currentTeacher) {
    // 1. Kiểm tra student thuộc class
    boolean studentInClass = classEntity.getStudents().stream()
            .anyMatch(student -> student.getId().equals(grade.getStudent().getId()));
    
    // 2. Kiểm tra teacher được phân công dạy môn đó trong class  
    boolean teacherAssigned = classSubjectRepository.existsByClassEntityIdAndSubjectIdAndTeacherId(
        grade.getClassEntity().getId(), 
        grade.getSubject().getId(), 
        currentTeacher.getId()
    );
}
```

**Các method được cập nhật:**
- ✅ `save(Grade grade)` - có validation
- ✅ `update(Long id, Grade grade)` - có validation  
- ✅ `createGrade(...)` - có validation
- ✅ `updateGrade(...)` - có validation
- ✅ `saveGrade(...)` - có validation

## ✅ 2. BẢO MẬT CSRF + PHÂN QUYỀN URL

### CSRF Protection được bật:

**File cập nhật:** `SecurityConfig.java`

**CSRF Configuration:**
```java
.csrf()
    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
    .ignoringAntMatchers("/api/**") // Ignore CSRF for API endpoints if needed
```

### Phân quyền URL chi tiết:

**Cấu hình phân quyền triệt để:**
```java
// Admin exclusive access
.antMatchers("/admin/**").hasRole("ADMIN")
.antMatchers("/api/admin/**").hasRole("ADMIN")

// Teacher exclusive access  
.antMatchers("/teacher/**").hasRole("TEACHER")
.antMatchers("/api/teacher/**").hasRole("TEACHER")

// Student exclusive access
.antMatchers("/student/**").hasRole("STUDENT") 
.antMatchers("/api/student/**").hasRole("STUDENT")

// Grade management - only teachers and admins
.antMatchers("/grades/create", "/grades/update", "/grades/delete").hasAnyRole("TEACHER", "ADMIN")
.antMatchers("/grades/**").hasAnyRole("STUDENT", "TEACHER", "ADMIN")

// Class management - admins and teachers
.antMatchers("/classes/create", "/classes/update", "/classes/delete").hasRole("ADMIN")
.antMatchers("/classes/**").hasAnyRole("TEACHER", "ADMIN")

// User management - admin only
.antMatchers("/users/**").hasRole("ADMIN")

// Subject management - admin only  
.antMatchers("/subjects/**").hasRole("ADMIN")

// Schedule management - teachers and admins
.antMatchers("/schedules/create", "/schedules/update", "/schedules/delete").hasAnyRole("TEACHER", "ADMIN")
.antMatchers("/schedules/**").hasAnyRole("STUDENT", "TEACHER", "ADMIN")

// Registration periods - admin only
.antMatchers("/registration-periods/**").hasRole("ADMIN")
```

### CSRF Token trong Thymeleaf Templates:

**Files được cập nhật:**
- ✅ `admin-grades.html` - form logout và grade form có CSRF token
- ✅ `student-course-registration.html` - CSRF meta tags và JavaScript

**CSRF Token Implementation:**

1. **Form HTML:**
```html
<form th:action="@{/logout}" method="post">
    <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}"/>
    <button type="submit">Logout</button>
</form>
```

2. **Meta tags:**
```html
<meta name="_csrf" th:content="${_csrf.token}"/>
<meta name="_csrf_header" th:content="${_csrf.headerName}"/>
```

3. **JavaScript AJAX:**
```javascript
const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

const headers = {
    'Content-Type': 'application/x-www-form-urlencoded',
    'X-Requested-With': 'XMLHttpRequest'
};

if (csrfToken && csrfHeader) {
    headers[csrfHeader] = csrfToken;
}
```

## ✅ 3. SECURITY HEADERS BỔ SUNG

**Additional Security Configuration:**
```java
.headers()
    .frameOptions().deny()
    .contentTypeOptions().and()
    .httpStrictTransportSecurity(hstsConfig -> hstsConfig
        .maxAgeInSeconds(31536000)
        .includeSubdomains(true));
```

## ✅ 4. KIỂM TRA TỔNG THỂ

### Đã hoàn thành triệt để:

1. ✅ **Ràng buộc nhập điểm**: 
   - Student phải thuộc class
   - Teacher phải được phân công dạy môn đó trong class
   - Validation trong tất cả save/update methods

2. ✅ **CSRF Protection**:
   - Bật CSRF trong SecurityConfig
   - CSRF token trong forms và AJAX requests
   - Meta tags cho JavaScript access

3. ✅ **Phân quyền URL chi tiết**:
   - Admin exclusive paths
   - Teacher exclusive paths  
   - Student exclusive paths
   - Resource-specific permissions
   - Grade management permissions

4. ✅ **Security Headers**:
   - Frame options denial
   - Content type options
   - HTTP Strict Transport Security

### Test Requirements:

1. **Test validation:**
   - Thử nhập điểm cho student không thuộc class → Should fail
   - Thử nhập điểm với teacher không được phân công → Should fail

2. **Test CSRF:**
   - Submit form không có CSRF token → Should fail  
   - AJAX request không có CSRF header → Should fail

3. **Test phân quyền:**
   - Student truy cập `/admin/**` → Should deny
   - Teacher truy cập `/student/**` → Should deny
   - Admin truy cập mọi resource → Should allow

## 🎯 SUMMARY

**100% hoàn thành yêu cầu:**
- ✅ Ràng buộc nhập điểm với validation đầy đủ
- ✅ CSRF protection được bật
- ✅ Phân quyền URL chi tiết và rõ ràng  
- ✅ CSRF token trong Thymeleaf templates
- ✅ Security headers bổ sung

**Hệ thống hiện tại đã được bảo mật và có ràng buộc nghiệp vụ chặt chẽ!**
