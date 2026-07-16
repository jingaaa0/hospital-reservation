const dialogs = document.querySelectorAll('dialog');
const reservationDateInput = document.getElementById('reservation-date');
const reservationTimeSlot = document.getElementById('reservation-time-slot');
const reservationDepartment = document.getElementById('reservation-department');
const reservationDoctor = document.getElementById('reservation-doctor');
const scheduleMessage = document.getElementById('schedule-message');
const calendarToggle = document.getElementById('calendar-toggle');
const calendarPicker = document.getElementById('calendar-picker');
const calendarGrid = document.getElementById('calendar-grid');
const calendarMonth = document.getElementById('calendar-month');
const calendarPrevious = document.getElementById('calendar-previous');
const calendarNext = document.getElementById('calendar-next');
const today = new Date();
today.setHours(0, 0, 0, 0);
let calendarCursor = new Date(today.getFullYear(), today.getMonth(), 1);

function resetDoctorOptions(message = '진료과를 먼저 선택해 주세요') {
  reservationDoctor.replaceChildren(new Option(message, ''));
  reservationDoctor.disabled = true;
}

async function loadDepartments() {
  reservationDepartment.disabled = true;
  try {
    const response = await fetch('/api/departments');
    if (!response.ok) throw new Error();
    const departments = await response.json();
    reservationDepartment.replaceChildren(new Option('진료과를 선택해 주세요', ''));
    departments.forEach((department) => {
      reservationDepartment.add(new Option(department.name, department.code));
    });
    reservationDepartment.disabled = departments.length === 0;
    if (departments.length === 0) {
      reservationDepartment.replaceChildren(new Option('등록된 진료과가 없습니다', ''));
    }
  } catch (error) {
    reservationDepartment.replaceChildren(new Option('진료과를 불러오지 못했습니다', ''));
  }
}

async function updateDoctorOptions() {
  const department = reservationDepartment.value;
  if (!department) {
    resetDoctorOptions();
    return;
  }

  resetDoctorOptions('의료진을 불러오는 중입니다');
  try {
    const response = await fetch(`/api/doctors?department=${encodeURIComponent(department)}`);
    if (!response.ok) throw new Error();
    const doctors = await response.json();
    reservationDoctor.replaceChildren(new Option('의료진을 선택해 주세요', ''));
    doctors.forEach((doctor) => reservationDoctor.add(new Option(doctor.name, doctor.doctorId)));
    reservationDoctor.disabled = doctors.length === 0;
    if (doctors.length === 0) {
      resetDoctorOptions('등록된 의료진이 없습니다');
    }
  } catch (error) {
    resetDoctorOptions('의료진을 불러오지 못했습니다');
  }
}

reservationDepartment.addEventListener('change', updateDoctorOptions);
loadDepartments();

function formatLocalDate(date) {
  const offsetDate = new Date(date.getTime() - date.getTimezoneOffset() * 60_000);
  return offsetDate.toISOString().slice(0, 10);
}

function createTimeOptions(endHour) {
  const options = [];
  for (let hour = 9; hour < endHour; hour += 1) {
    for (const minute of [0, 30]) {
      options.push(`${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}`);
    }
  }
  return options;
}

function resetTimeSlot(message) {
  reservationTimeSlot.replaceChildren(new Option(message, ''));
  reservationTimeSlot.disabled = true;
  scheduleMessage.textContent = message;
}

function isBeforeToday(date) {
  return date.getTime() < today.getTime();
}

function renderCalendar() {
  const year = calendarCursor.getFullYear();
  const month = calendarCursor.getMonth();
  const firstDay = new Date(year, month, 1).getDay();
  const lastDate = new Date(year, month + 1, 0).getDate();
  const selectedDate = reservationDateInput.value;

  calendarMonth.textContent = `${year}년 ${month + 1}월`;
  calendarPrevious.disabled = year === today.getFullYear() && month === today.getMonth();
  calendarGrid.replaceChildren();

  for (let index = 0; index < firstDay; index += 1) {
    calendarGrid.append(document.createElement('span'));
  }

  for (let dateNumber = 1; dateNumber <= lastDate; dateNumber += 1) {
    const date = new Date(year, month, dateNumber);
    const dateValue = formatLocalDate(date);
    const isSunday = date.getDay() === 0;
    const unavailable = isSunday || isBeforeToday(date);
    const dayButton = document.createElement('button');
    dayButton.type = 'button';
    dayButton.className = `calendar-day${isSunday ? ' sunday' : ''}${selectedDate === dateValue ? ' selected' : ''}`;
    dayButton.textContent = dateNumber;
    dayButton.disabled = unavailable;
    dayButton.setAttribute('aria-label', `${year}년 ${month + 1}월 ${dateNumber}일${isSunday ? ' 휴진' : ''}`);
    dayButton.addEventListener('click', () => {
      reservationDateInput.value = dateValue;
      calendarToggle.textContent = `${year}. ${String(month + 1).padStart(2, '0')}. ${String(dateNumber).padStart(2, '0')}`;
      calendarPicker.hidden = true;
      calendarToggle.setAttribute('aria-expanded', 'false');
      updateAvailableTimeSlots();
    });
    calendarGrid.append(dayButton);
  }
}

calendarToggle.addEventListener('click', () => {
  calendarPicker.hidden = !calendarPicker.hidden;
  calendarToggle.setAttribute('aria-expanded', String(!calendarPicker.hidden));
  if (!calendarPicker.hidden) renderCalendar();
});

calendarPrevious.addEventListener('click', () => {
  calendarCursor = new Date(calendarCursor.getFullYear(), calendarCursor.getMonth() - 1, 1);
  renderCalendar();
});

calendarNext.addEventListener('click', () => {
  calendarCursor = new Date(calendarCursor.getFullYear(), calendarCursor.getMonth() + 1, 1);
  renderCalendar();
});

function updateAvailableTimeSlots() {
  const selectedDate = reservationDateInput.value;
  if (!selectedDate) {
    resetTimeSlot('날짜를 먼저 선택해 주세요.');
    return;
  }

  const day = new Date(`${selectedDate}T00:00:00`).getDay();
  if (day === 0) {
    reservationDateInput.value = '';
    resetTimeSlot('일요일은 휴진입니다.');
    return;
  }

  const isSaturday = day === 6;
  const endHour = isSaturday ? 13 : 18;
  const officeHours = isSaturday ? '토요일 진료시간은 09:00 - 13:00입니다.' : '평일 진료시간은 09:00 - 18:00입니다.';
  reservationTimeSlot.replaceChildren(new Option('시간을 선택해 주세요', ''));
  createTimeOptions(endHour).forEach((time) => reservationTimeSlot.add(new Option(time, time)));
  reservationTimeSlot.disabled = false;
  scheduleMessage.textContent = `${officeHours} 30분 단위로 선택할 수 있습니다.`;
}

document.addEventListener('click', (event) => {
  if (!event.target.closest('.date-field') && !calendarPicker.hidden) {
    calendarPicker.hidden = true;
    calendarToggle.setAttribute('aria-expanded', 'false');
  }
});

function formatMobilePhone(value) {
  const digits = value.replace(/\D/g, '').slice(0, 11);
  if (digits.length <= 3) return digits;
  if (digits.length <= 7) return `${digits.slice(0, 3)}-${digits.slice(3)}`;
  return `${digits.slice(0, 3)}-${digits.slice(3, 7)}-${digits.slice(7)}`;
}

document.querySelectorAll('input[name="phoneNumber"]').forEach((input) => {
  input.addEventListener('input', () => {
    input.value = formatMobilePhone(input.value);
  });
});

document.querySelectorAll('[data-open]').forEach((button) => {
  button.addEventListener('click', () => document.getElementById(button.dataset.open).showModal());
});

document.querySelectorAll('[data-close]').forEach((button) => {
  button.addEventListener('click', () => button.closest('dialog').close());
});

document.querySelectorAll('[data-switch]').forEach((button) => {
  button.addEventListener('click', () => {
    button.closest('dialog').close();
    document.getElementById(button.dataset.switch).showModal();
  });
});

dialogs.forEach((dialog) => {
  dialog.addEventListener('click', (event) => {
    if (event.target === dialog) dialog.close();
  });
});



// ------------------------------------------------------------------------------------------------------------------------

// FORM 전송 기능
async function submitForm(form, endpoint) {
  const message = form.querySelector('.form-message');
  const submitButton = form.querySelector('[type="submit"]');
  const payload = Object.fromEntries(new FormData(form));
  if (payload.phoneNumber) {
    payload.phoneNumber = payload.phoneNumber.replace(/\D/g, '');
  }
  payload.privacyAgreed = form.elements.privacyAgreed?.checked;

  message.className = 'form-message';
  message.textContent = '';
  submitButton.disabled = true;
  submitButton.textContent = '전송 중입니다…';

  try {
    const response = await fetch(endpoint, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    const body = await response.json();
    if (!response.ok) throw new Error(body.message || '입력 내용을 다시 확인해 주세요.');
    message.classList.add('success');
    message.textContent = body.message;
    form.reset();
    if (endpoint.includes('reservations')) {
      resetDoctorOptions();
      calendarToggle.textContent = '날짜를 선택해 주세요';
      calendarCursor = new Date(today.getFullYear(), today.getMonth(), 1);
      updateAvailableTimeSlots();
    }
  } catch (error) {
    message.textContent = error.message || '전송에 실패했습니다. 잠시 후 다시 시도해 주세요.';
  } finally {
    submitButton.disabled = false;
    submitButton.textContent = endpoint.includes('inquiries') ? '문의 전송하기' : '예약 신청하기';
  }
}

// 문의하기 FORM 전송
document.getElementById('inquiry-form').addEventListener('submit', (event) => {
  event.preventDefault();
  submitForm(event.currentTarget, '/api/inquiries');
});

document.getElementById('reservation-form').addEventListener('submit', (event) => {
  event.preventDefault();
  if (!reservationDateInput.value) {
    const message = event.currentTarget.querySelector('.form-message');
    message.textContent = '예약 날짜를 선택해 주세요.';
    calendarToggle.focus();
    return;
  }
  submitForm(event.currentTarget, '/api/reservations');
});
