# Capacity & Registration Period Implementation Guide

## Tóm tắt tính năng đã implement

### 1. Class Capacity Management
- ✅ **Database**: Thêm field `capacity INT NOT NULL DEFAULT 30` vào bảng `classes`
- ✅ **Model**: Cập nhật `Class.java` với capacity field và helper methods
- ✅ **Service**: Cập nhật `ClassService` với capacity checking trong `addStudentToClass()`
- ✅ **Validation**: Ném `IllegalStateException` khi lớp đã đầy
- ✅ **UI**: Hiển thị capacity info và warning trong template

### 2. Registration Period Control
- ✅ **Database**: Bảng `registration_periods` với status control
- ✅ **Model**: `RegistrationPeriod.java` với enum status (SCHEDULED, OPEN, CLOSED, CANCELLED)
- ✅ **Service**: `RegistrationPeriodService` với backend validation
- ✅ **Controller**: `StudentController` kiểm tra registration period trước mọi đăng ký/hủy
- ✅ **Chặn Backend**: Validation trong controller, không phụ thuộc UI

## Cách sử dụng

### 1. Migration Database
```sql
-- Chạy script migration
source src/main/resources/migration.sql;
```

### 2. Tạo Registration Period
```java
RegistrationPeriod period = new RegistrationPeriod(
    "Đăng ký Kỳ 1 2024", 
    LocalDateTime.of(2024, 8, 1, 8, 0),  // Start
    LocalDateTime.of(2024, 8, 31, 17, 0) // End
);
period.setStatus(RegistrationStatus.OPEN);
registrationPeriodService.save(period);
```

### 3. Kiểm tra Capacity
```java
// Trong ClassService
boolean canAdd = classService.canAddStudentToClass(classId);
int availableSlots = classService.getAvailableSlots(classId);
boolean isFull = classService.isClassFull(classId);

// Trong Class model
class.getCurrentStudentCount(); // Số sinh viên hiện tại
class.getAvailableSlots();      // Số chỗ còn lại
class.isFull();                 // Kiểm tra đã đầy
class.canAddStudent();          // Có thể thêm sinh viên
```

### 4. Backend Validation Flow
```java
// Trong StudentController.registerClass()
1. Kiểm tra registration period: registrationPeriodService.validateRegistrationAction()
2. Kiểm tra capacity: classService.canAddStudentToClass()
3. Thực hiện đăng ký: classService.addStudentToClass() (có built-in validation)
```

## API Endpoints Được Cập Nhật

### Registration Validation
- **POST** `/student/classes/{id}/register` - Đăng ký lớp (có validation)
- **POST** `/student/classes/{id}/unregister` - Hủy đăng ký (có validation)
- **GET** `/student/course-registration` - Hiển thị trạng thái đăng ký

### Capacity Information
- Template hiển thị: `Số sinh viên: 20/30`
- Warning khi còn ít chỗ: "Chỉ còn lại 3 chỗ!"
- Error khi đầy: "Lớp đã đầy!"
- Button disabled khi không thể đăng ký

## Database Schema Changes

### Classes Table
```sql
ALTER TABLE classes ADD COLUMN capacity INT NOT NULL DEFAULT 30;
```

### Registration Periods Table
```sql
CREATE TABLE registration_periods (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    period_name VARCHAR(100) NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    status VARCHAR(20) DEFAULT 'SCHEDULED',
    -- ... other fields
    CHECK (status IN ('SCHEDULED', 'OPEN', 'CLOSED', 'CANCELLED')),
    CHECK (end_time > start_time)
);
```

## Validation Rules Implemented

### Capacity Rules
1. ❌ **Cannot register** nếu `class.getCurrentStudentCount() >= class.getCapacity()`
2. ⚠️ **Warning** nếu `class.getAvailableSlots() <= 5`
3. ✅ **Success** nếu capacity cho phép

### Registration Period Rules
1. ❌ **Cannot register/unregister** nếu `status != 'OPEN'`
2. ❌ **Cannot register/unregister** nếu `currentTime < startTime || currentTime > endTime`
3. ✅ **Can register/unregister** chỉ khi `status = 'OPEN'` và trong thời gian cho phép

### Error Messages
- `"Lớp học đã đầy! Sức chứa: 30 sinh viên. Hiện tại có: 30 sinh viên."`
- `"Không thể đăng ký lớp học! Kỳ đăng ký đã đóng."`
- `"Không thể hủy đăng ký lớp học! Kỳ đăng ký đã đóng."`

## Files Modified

### Backend
- `src/main/Database` - Schema updates
- `src/main/java/com/codegym/module4casestudy/model/Class.java` - Capacity field
- `src/main/java/com/codegym/module4casestudy/service/ClassServiceImpl.java` - Capacity checking
- `src/main/java/com/codegym/module4casestudy/service/RegistrationPeriodServiceImpl.java` - Period validation
- `src/main/java/com/codegym/module4casestudy/controller/StudentController.java` - Backend validation

### Frontend
- `src/main/resources/templates/student/student-course-registration.html` - Capacity UI

### Migration
- `src/main/resources/migration.sql` - Database migration script

## Testing

### Test Cases Đã Cover
1. ✅ Đăng ký khi lớp chưa đầy + kỳ đăng ký mở
2. ❌ Đăng ký khi lớp đã đầy
3. ❌ Đăng ký khi kỳ đăng ký đóng
4. ❌ Hủy đăng ký khi kỳ đăng ký đóng
5. ⚠️ Warning hiển thị khi lớp gần đầy
6. 🔄 Auto-update registration period status theo thời gian

## Production Deployment Checklist

- [ ] Backup database trước khi chạy migration
- [ ] Chạy migration script: `migration.sql`
- [ ] Verify capacity data: `SELECT name, capacity FROM classes;`
- [ ] Tạo registration period đầu tiên
- [ ] Test đăng ký/hủy đăng ký với user thật
- [ ] Monitor error logs cho capacity violations
- [ ] Setup automatic period status updates (optional)

---

**Lưu ý**: Tất cả validation đều được thực hiện ở backend, đảm bảo tính bảo mật và không thể bypass qua UI.
