    function toggleLogoutDropdown() {
    const dropdown = document.getElementById("logoutDropdown");
    if (dropdown) {
    dropdown.style.display = (dropdown.style.display === "block") ? "none" : "block";
}
}

    function toggleSidebar() {
    const sidebar = document.getElementById("sidebar");
    if (sidebar) {
    sidebar.classList.toggle("show");
}
}

    document.addEventListener("click", function (event) {
    const userInfo = document.querySelector(".user-info");
    const dropdown = document.getElementById("logoutDropdown");
    const sidebar = document.getElementById("sidebar");
    const toggle = document.querySelector(".menu-toggle");

    if (dropdown && userInfo && !userInfo.contains(event.target)) {
    dropdown.style.display = "none";
}

    if (sidebar && !sidebar.contains(event.target) && !toggle.contains(event.target)) {
    sidebar.classList.remove("show");
}
});
    // Current view state
    let currentView = 'month';
    let currentDate = new Date();

    // Sample schedule data
    const sampleSchedules = [
    {
        id: 1,
        subject: 'Toán cao cấp',
        class: 'CNTT1 - K15',
        teacher: 'Nguyễn Văn A',
        room: 'P.301 - Nhà A1',
        date: '2023-10-16',
        startTime: '07:30',
        endTime: '09:30',
        status: 'confirmed',
        note: 'Buổi học đầu tiên của môn Toán cao cấp'
    },
    {
        id: 2,
        subject: 'Vật lý đại cương',
        class: 'CNTT2 - K15',
        teacher: 'Trần Thị B',
        room: 'P.302 - Nhà A1',
        date: '2023-10-17',
        startTime: '09:30',
        endTime: '11:30',
        status: 'confirmed',
        note: 'Thực hành phòng lab'
    },
    {
        id: 3,
        subject: 'Hóa học đại cương',
        class: 'CNTT3 - K15',
        teacher: 'Lê Văn C',
        room: 'P.401 - Nhà A2',
        date: '2023-10-18',
        startTime: '13:30',
        endTime: '15:30',
        status: 'pending',
        note: 'Kiểm tra giữa kỳ'
    },
    {
        id: 4,
        subject: 'Lập trình C++',
        class: 'CNTT1 - K15',
        teacher: 'Nguyễn Văn A',
        room: 'P.402 - Nhà A2',
        date: '2023-10-19',
        startTime: '15:30',
        endTime: '17:30',
        status: 'confirmed',
        note: 'Bài tập nhóm'
    },
    {
        id: 5,
        subject: 'Cơ sở dữ liệu',
        class: 'CNTT2 - K15',
        teacher: 'Trần Thị B',
        room: 'P.301 - Nhà A1',
        date: '2023-10-20',
        startTime: '07:30',
        endTime: '09:30',
        status: 'confirmed',
        note: 'Thi cuối kỳ'
    }
    ];

    // Initialize the page
    document.addEventListener('DOMContentLoaded', function() {
    generateMonthView();
    generateScheduleList();
    setActiveTab();
});

    // Change view between day, week, month, list
    function changeView(view) {
    currentView = view;
    setActiveTab();

    // Hide all views
    document.getElementById('dayView').classList.add('hidden');
    document.getElementById('weekView').classList.add('hidden');
    document.getElementById('monthView').classList.add('hidden');
    document.getElementById('listView').classList.add('hidden');

    // Show selected view
    switch(view) {
    case 'day':
    document.getElementById('dayView').classList.remove('hidden');
    generateDayView();
    document.getElementById('currentPeriod').textContent = formatDate(currentDate, 'day');
    break;
    case 'week':
    document.getElementById('weekView').classList.remove('hidden');
    generateWeekView();
    document.getElementById('currentPeriod').textContent = getWeekRange(currentDate);
    break;
    case 'month':
    document.getElementById('monthView').classList.remove('hidden');
    generateMonthView();
    document.getElementById('currentPeriod').textContent = formatDate(currentDate, 'month');
    break;
    case 'list':
    document.getElementById('listView').classList.remove('hidden');
    generateScheduleList();
    document.getElementById('currentPeriod').textContent = 'Danh sách lịch học';
    break;
}
}

    // Set active tab style
    function setActiveTab() {
    const tabs = document.querySelectorAll('#scheduleTabs button');
    tabs.forEach(tab => {
    tab.classList.remove('border-blue-600', 'text-blue-600');
    tab.classList.add('border-transparent');
});

    let activeTab;
    switch(currentView) {
    case 'day': activeTab = tabs[0]; break;
    case 'week': activeTab = tabs[1]; break;
    case 'month': activeTab = tabs[2]; break;
    case 'list': activeTab = tabs[3]; break;
}

    activeTab.classList.add('border-blue-600', 'text-blue-600');
    activeTab.classList.remove('border-transparent');
}

    // Generate day view
    function generateDayView() {
    const dayView = document.querySelector('#dayView .space-y-2');
    dayView.innerHTML = '';

    // Generate time slots from 7:00 to 17:00
    for (let hour = 7; hour <= 17; hour++) {
    for (let minute = 0; minute < 60; minute += 30) {
    const time = `${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}`;
    const timeSlot = document.createElement('div');
    timeSlot.className = 'time-slot bg-white p-3 border rounded-lg flex justify-between items-center';

    const timeElement = document.createElement('span');
    timeElement.className = 'font-medium text-gray-700';
    timeElement.textContent = time;

    const scheduleElement = document.createElement('div');
    scheduleElement.className = 'flex-1 ml-4';

    // Check if there's a schedule at this time
    const schedule = sampleSchedules.find(s => {
    const scheduleDate = new Date(s.date);
    const currentDateStr = currentDate.toISOString().split('T')[0];
    return s.date === currentDateStr && s.startTime <= time && s.endTime > time;
});

    if (schedule) {
    const scheduleDiv = document.createElement('div');
    scheduleDiv.className = 'px-3 py-2 rounded bg-blue-100 border border-blue-200 text-blue-800 flex justify-between items-center';
    scheduleDiv.innerHTML = `
                        <div>
                            <span class="font-medium">${schedule.subject}</span>
                            <span class="text-sm ml-2">${schedule.class} - ${schedule.room}</span>
                        </div>
                        <button onclick="viewScheduleDetail(${schedule.id})" class="text-blue-600 hover:text-blue-800">
                            <i class="fas fa-chevron-right"></i>
                        </button>
                    `;
    scheduleElement.appendChild(scheduleDiv);
} else {
    scheduleElement.innerHTML = '<span class="text-gray-400 text-sm">Trống</span>';
}

    timeSlot.appendChild(timeElement);
    timeSlot.appendChild(scheduleElement);
    dayView.appendChild(timeSlot);
}
}
}

    // Generate week view
    function generateWeekView() {
    const weekTimeSlots = document.getElementById('weekTimeSlots');
    weekTimeSlots.innerHTML = '';

    // Generate time slots from 7:00 to 17:00
    for (let hour = 7; hour <= 17; hour++) {
    for (let minute = 0; minute < 60; minute += 60) {
    const time = `${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}`;
    const row = document.createElement('tr');

    // Time cell
    const timeCell = document.createElement('td');
    timeCell.className = 'p-2 border text-center text-sm font-medium';
    timeCell.textContent = time;
    row.appendChild(timeCell);

    // Generate cells for each day of the week (Monday to Saturday)
    for (let day = 0; day < 6; day++) {
    const date = new Date(currentDate);
    date.setDate(currentDate.getDate() - currentDate.getDay() + 1 + day); // Monday to Saturday
    const dateStr = date.toISOString().split('T')[0];

    const cell = document.createElement('td');
    cell.className = 'p-1 border h-20 align-top';

    // Check if there's a schedule at this time
    const schedule = sampleSchedules.find(s => {
    return s.date === dateStr && s.startTime <= time && s.endTime > time;
});

    if (schedule) {
    const scheduleDiv = document.createElement('div');
    scheduleDiv.className = 'text-xs p-1 rounded bg-blue-100 border border-blue-200 text-blue-800 cursor-pointer';
    scheduleDiv.innerHTML = `
                            <div class="font-medium truncate">${schedule.subject}</div>
                            <div class="truncate">${schedule.teacher}</div>
                            <div class="truncate">${schedule.room}</div>
                        `;
    scheduleDiv.onclick = () => viewScheduleDetail(schedule.id);
    cell.appendChild(scheduleDiv);
}

    row.appendChild(cell);
}

    weekTimeSlots.appendChild(row);
}
}
}

    // Generate month view
    function generateMonthView() {
    const monthDays = document.getElementById('monthDays');
    monthDays.innerHTML = '';

    const year = currentDate.getFullYear();
    const month = currentDate.getMonth();

    // Get first day of month and last day of month
    const firstDay = new Date(year, month, 1);
    const lastDay = new Date(year, month + 1, 0);

    // Get days from previous month to fill the grid
    const prevMonthDays = firstDay.getDay() === 0 ? 6 : firstDay.getDay() - 1; // Adjust for Monday start

    // Get days from next month to fill the grid
    const nextMonthDays = 7 - (lastDay.getDay() === 0 ? 7 : lastDay.getDay());

    // Total days to show (prev month + current month + next month)
    const totalDays = prevMonthDays + lastDay.getDate() + nextMonthDays;

    // Generate days
    for (let i = 1 - prevMonthDays; i <= totalDays - prevMonthDays; i++) {
    const dayElement = document.createElement('div');
    dayElement.className = 'min-h-24 p-1 border';

    const date = new Date(year, month, i);

    // Day number
    const dayNumber = document.createElement('div');
    dayNumber.className = 'text-right text-sm mb-1';

    if (i > 0 && i <= lastDay.getDate()) {
    // Current month
    dayNumber.textContent = i;
    if (i === new Date().getDate() && month === new Date().getMonth() && year === new Date().getFullYear()) {
    dayNumber.className += ' font-bold text-blue-600';
}

    // Check if there are schedules for this day
    const dateStr = `${year}-${(month + 1).toString().padStart(2, '0')}-${i.toString().padStart(2, '0')}`;
    const daySchedules = sampleSchedules.filter(s => s.date === dateStr);

    if (daySchedules.length > 0) {
    const schedulesContainer = document.createElement('div');
    schedulesContainer.className = 'space-y-1';

    daySchedules.slice(0, 2).forEach(schedule => {
    const scheduleDiv = document.createElement('div');
    scheduleDiv.className = 'text-xs p-1 rounded bg-blue-100 border border-blue-200 text-blue-800 cursor-pointer truncate';
    scheduleDiv.title = `${schedule.subject} - ${schedule.teacher} (${schedule.startTime}-${schedule.endTime})`;
    scheduleDiv.textContent = `${schedule.subject} (${schedule.startTime})`;
    scheduleDiv.onclick = () => viewScheduleDetail(schedule.id);
    schedulesContainer.appendChild(scheduleDiv);
});

    if (daySchedules.length > 2) {
    const moreDiv = document.createElement('div');
    moreDiv.className = 'text-xs text-center text-gray-500';
    moreDiv.textContent = `+${daySchedules.length - 2} nữa`;
    schedulesContainer.appendChild(moreDiv);
}

    dayElement.appendChild(schedulesContainer);
}
} else {
    // Other month
    dayNumber.textContent = date.getDate();
    dayNumber.className += ' text-gray-400';
    dayElement.className += ' bg-gray-50';
}

    dayElement.appendChild(dayNumber);
    monthDays.appendChild(dayElement);
}
}

    // Generate schedule list
    function generateScheduleList() {
    const scheduleList = document.getElementById('scheduleList');
    scheduleList.innerHTML = '';

    sampleSchedules.forEach(schedule => {
    const row = document.createElement('tr');
    row.className = 'hover:bg-gray-50';
    row.innerHTML = `
                <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">${schedule.subject}</td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">${schedule.class}</td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">${schedule.teacher}</td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    ${formatDate(new Date(schedule.date), 'day')}, ${schedule.startTime}-${schedule.endTime}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">${schedule.room}</td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    ${getStatusBadge(schedule.status)}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    <button onclick="viewScheduleDetail(${schedule.id})" class="text-blue-600 hover:text-blue-900 mr-2">
                        <i class="fas fa-eye"></i>
                    </button>
                    <button onclick="editSchedule(${schedule.id})" class="text-yellow-600 hover:text-yellow-900 mr-2">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button onclick="deleteSchedule(${schedule.id})" class="text-red-600 hover:text-red-900">
                        <i class="fas fa-trash"></i>
                    </button>
                </td>
            `;
    scheduleList.appendChild(row);
});
}

    // Get status badge
    function getStatusBadge(status) {
    switch(status) {
    case 'confirmed':
    return '<span class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-green-100 text-green-800">Đã xác nhận</span>';
    case 'pending':
    return '<span class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-yellow-100 text-yellow-800">Chờ duyệt</span>';
    case 'cancelled':
    return '<span class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-red-100 text-red-800">Đã hủy</span>';
    default:
    return '<span class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-gray-100 text-gray-800">Không xác định</span>';
}
}

    // Format date
    function formatDate(date, type) {
    const days = ['Chủ nhật', 'Thứ 2', 'Thứ 3', 'Thứ 4', 'Thứ 5', 'Thứ 6', 'Thứ 7'];
    const months = ['Tháng 1', 'Tháng 2', 'Tháng 3', 'Tháng 4', 'Tháng 5', 'Tháng 6', 'Tháng 7', 'Tháng 8', 'Tháng 9', 'Tháng 10', 'Tháng 11', 'Tháng 12'];

    if (type === 'day') {
    return `${days[date.getDay()]}, ${date.getDate()} ${months[date.getMonth()]} ${date.getFullYear()}`;
} else if (type === 'month') {
    return `${months[date.getMonth()]}, ${date.getFullYear()}`;
} else {
    return `${date.getDate()}/${date.getMonth() + 1}/${date.getFullYear()}`;
}
}

    // Get week range
    function getWeekRange(date) {
    const startOfWeek = new Date(date);
    startOfWeek.setDate(date.getDate() - date.getDay() + 1); // Monday

    const endOfWeek = new Date(date);
    endOfWeek.setDate(date.getDate() + (6 - date.getDay())); // Sunday

    return `Tuần từ ${startOfWeek.getDate()}/${startOfWeek.getMonth() + 1} đến ${endOfWeek.getDate()}/${endOfWeek.getMonth() + 1}, ${endOfWeek.getFullYear()}`;
}

    // Navigation functions
    function prevPeriod() {
    if (currentView === 'day') {
    currentDate.setDate(currentDate.getDate() - 1);
} else if (currentView === 'week') {
    currentDate.setDate(currentDate.getDate() - 7);
} else if (currentView === 'month') {
    currentDate.setMonth(currentDate.getMonth() - 1);
}
    changeView(currentView);
}

    function nextPeriod() {
    if (currentView === 'day') {
    currentDate.setDate(currentDate.getDate() + 1);
} else if (currentView === 'week') {
    currentDate.setDate(currentDate.getDate() + 7);
} else if (currentView === 'month') {
    currentDate.setMonth(currentDate.getMonth() + 1);
}
    changeView(currentView);
}

    function goToToday() {
    currentDate = new Date();
    changeView(currentView);
}

    // Modal functions
    function openCreateModal() {
    document.getElementById('createModal').classList.remove('hidden');
}

    function closeCreateModal() {
    document.getElementById('createModal').classList.add('hidden');
}

    function viewScheduleDetail(id) {
    document.getElementById('detailModal').classList.remove('hidden');
    // In a real app, you would fetch the schedule details by ID
}

    function closeDetailModal() {
    document.getElementById('detailModal').classList.add('hidden');
}

    function editSchedule(id) {
    openCreateModal();
    // In a real app, you would populate the form with the schedule data
}

    function deleteSchedule(id) {
    if (confirm('Bạn có chắc chắn muốn xóa lịch học này?')) {
    // In a real app, you would send a delete request to the server
    alert('Lịch học đã được xóa');
    closeDetailModal();
}
}

    // Check for schedule conflicts
    function checkConflict() {
    // In a real app, you would check for conflicts with existing schedules
    document.getElementById('conflictAlert').classList.remove('hidden');
    document.getElementById('conflictDetails').textContent = 'Giảng viên Nguyễn Văn A đã có lịch dạy vào thời gian này';
}

    function closeConflictAlert() {
    document.getElementById('conflictAlert').classList.add('hidden');
}

    // Form submission
    document.getElementById('scheduleForm').addEventListener('submit', function(e) {
    e.preventDefault();
    alert('Lịch học đã được lưu thành công');
    closeCreateModal();
    // In a real app, you would send the form data to the server
});