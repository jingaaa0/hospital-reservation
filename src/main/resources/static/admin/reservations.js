const statusLabels = {
  REQUESTED: '신청',
  CONFIRMED: '확정',
  COMPLETED: '진료 완료',
  CANCELLED: '취소',
  NO_SHOW: '미방문'
};

const filterForm = document.getElementById('reservation-filter');
const departmentFilter = document.getElementById('filter-department');
const doctorFilter = document.getElementById('filter-doctor');
const reservationList = document.getElementById('reservation-list');
const listMessage = document.getElementById('list-message');
const previousPage = document.getElementById('previous-page');
const nextPage = document.getElementById('next-page');
const pageInfo = document.getElementById('page-info');
const symptomDialog = document.getElementById('symptom-dialog');
const memoDialog = document.getElementById('memo-dialog');
const memoForm = document.getElementById('memo-form');
const adminMemo = document.getElementById('admin-memo');
const saveMemoButton = document.getElementById('save-memo');
const memoMessage = document.getElementById('memo-message');
let doctors = [];
let currentPage = 0;
let totalPages = 0;
let memoReservationId = null;
let csrfToken = '';

function getCookie(name) {
  const cookie = document.cookie.split('; ').find((item) => item.startsWith(`${name}=`));
  return cookie ? decodeURIComponent(cookie.substring(name.length + 1)) : '';
}

function csrfHeaders() {
  const token = csrfToken || getCookie('XSRF-TOKEN');
  return token ? { 'X-XSRF-TOKEN': token } : {};
}

async function loadCsrfToken() {
  const response = await fetch('/api/admin/csrf');
  ensureAuthenticated(response);
  if (!response.ok) throw new Error('보안 토큰을 발급하지 못했습니다. 페이지를 새로고침해 주세요.');
  const result = await response.json();
  csrfToken = result.token;
}

function ensureAuthenticated(response) {
  if (response.redirected && new URL(response.url).pathname === '/admin/login') {
    window.location.assign(response.url);
    throw new Error('로그인이 필요합니다.');
  }
}

function formatPhoneNumber(value) {
  return value?.replace(/^(\d{3})(\d{3,4})(\d{4})$/, '$1-$2-$3') || '';
}

function formatDate(value) {
  return new Intl.DateTimeFormat('ko-KR', { dateStyle: 'medium' }).format(new Date(`${value}T00:00:00`));
}

function createText(className, text) {
  const element = document.createElement('p');
  element.className = className;
  element.textContent = text;
  return element;
}

function createDetailButton(label, hasContent, onClick) {
  const button = document.createElement('button');
  button.type = 'button';
  button.className = `detail-button${hasContent ? ' has-content' : ''}`;
  button.textContent = label;
  button.addEventListener('click', onClick);
  return button;
}

function openSymptomDialog(reservation) {
  document.getElementById('symptom-dialog-patient').textContent =
    `${reservation.patientName} · ${reservation.reservationNumber}`;
  document.getElementById('symptom-dialog-content').textContent = reservation.symptom || '입력된 증상이 없습니다.';
  symptomDialog.showModal();
}

function openMemoDialog(reservation) {
  memoReservationId = reservation.reservationId;
  document.getElementById('memo-dialog-patient').textContent =
    `${reservation.patientName} · ${reservation.reservationNumber}`;
  adminMemo.value = reservation.adminMemo || '';
  memoMessage.textContent = '';
  memoDialog.showModal();
  adminMemo.focus();
}

function populateDoctorFilter() {
  const selectedDoctor = doctorFilter.value;
  const department = departmentFilter.value;
  const filteredDoctors = department
    ? doctors.filter((doctor) => doctor.departmentCode === department)
    : doctors;
  doctorFilter.replaceChildren(new Option('전체 원장', ''));
  filteredDoctors.forEach((doctor) => {
    doctorFilter.add(new Option(`${doctor.departmentName} · ${doctor.name}`, doctor.doctorId));
  });
  if ([...doctorFilter.options].some((option) => option.value === selectedDoctor)) {
    doctorFilter.value = selectedDoctor;
  }
}

async function loadFilterOptions() {
  const [departmentResponse, doctorResponse] = await Promise.all([
    fetch('/api/departments'),
    fetch('/api/doctors')
  ]);
  ensureAuthenticated(departmentResponse);
  ensureAuthenticated(doctorResponse);
  if (!departmentResponse.ok || !doctorResponse.ok) throw new Error('필터 정보를 불러오지 못했습니다.');
  const departments = await departmentResponse.json();
  doctors = await doctorResponse.json();
  departments.forEach((department) => departmentFilter.add(new Option(department.name, department.code)));
  populateDoctorFilter();
}

function createStatusControl(reservation) {
  const wrapper = document.createElement('div');
  wrapper.className = 'status-control';
  const select = document.createElement('select');
  Object.entries(statusLabels).forEach(([value, label]) => {
    select.add(new Option(label, value, false, value === reservation.status));
  });
  const button = document.createElement('button');
  button.type = 'button';
  button.textContent = '상태 저장';
  button.addEventListener('click', () => updateStatus(reservation.reservationId, select.value, button));
  wrapper.append(select, button);
  return wrapper;
}

function renderReservations(reservations) {
  reservationList.replaceChildren();
  if (reservations.length === 0) {
    const row = document.createElement('tr');
    row.className = 'empty-row';
    const cell = document.createElement('td');
    cell.colSpan = 6;
    cell.textContent = '조건에 맞는 예약이 없습니다.';
    row.append(cell);
    reservationList.append(row);
    return;
  }

  reservations.forEach((reservation) => {
    const row = document.createElement('tr');
    const patient = document.createElement('td');
    patient.append(
      createText('primary', reservation.patientName),
      createText('secondary', formatPhoneNumber(reservation.phoneNumber)),
      createText('secondary', reservation.reservationNumber)
    );
    const appointment = document.createElement('td');
    appointment.append(
      createText('primary', formatDate(reservation.appointmentDate)),
      createText('secondary', reservation.appointmentTime.slice(0, 5))
    );
    const medical = document.createElement('td');
    medical.append(
      createText('primary', reservation.departmentName),
      createText('secondary', reservation.doctorName)
    );
    const symptom = document.createElement('td');
    symptom.append(createDetailButton('증상 보기', Boolean(reservation.symptom), () => openSymptomDialog(reservation)));
    const memo = document.createElement('td');
    memo.append(createDetailButton(
      reservation.adminMemo ? '메모 보기' : '메모 작성',
      Boolean(reservation.adminMemo),
      () => openMemoDialog(reservation)
    ));
    const status = document.createElement('td');
    status.append(createStatusControl(reservation));
    row.append(patient, appointment, medical, symptom, memo, status);
    reservationList.append(row);
  });
}

function buildQuery() {
  const params = new URLSearchParams();
  new FormData(filterForm).forEach((value, key) => {
    if (value) params.set(key, value);
  });
  params.set('page', currentPage);
  params.set('size', '20');
  return params;
}

async function loadReservations() {
  listMessage.className = '';
  listMessage.textContent = '불러오는 중…';
  try {
    const response = await fetch(`/api/admin/reservations?${buildQuery()}`);
    ensureAuthenticated(response);
    if (!response.ok) throw new Error('예약 목록을 불러오지 못했습니다.');
    const result = await response.json();
    renderReservations(result.items);
    totalPages = result.totalPages;
    listMessage.textContent = `총 ${result.totalElements}건`;
    pageInfo.textContent = totalPages === 0 ? '0 / 0' : `${result.page + 1} / ${totalPages}`;
    previousPage.disabled = result.page <= 0;
    nextPage.disabled = result.page + 1 >= totalPages;
  } catch (error) {
    listMessage.className = 'error';
    listMessage.textContent = error.message;
  }
}

async function updateStatus(reservationId, status, button) {
  button.disabled = true;
  button.textContent = '저장 중…';
  try {
    const response = await fetch(`/api/admin/reservations/${reservationId}/status`, {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json', ...csrfHeaders() },
      body: JSON.stringify({ status })
    });
    ensureAuthenticated(response);
    const body = await response.json();
    if (!response.ok) throw new Error(body.message || '예약 상태를 변경하지 못했습니다.');
    await loadReservations();
  } catch (error) {
    listMessage.className = 'error';
    listMessage.textContent = error.message;
    button.disabled = false;
    button.textContent = '상태 저장';
  }
}

async function updateAdminMemo() {
  memoMessage.textContent = '';
  saveMemoButton.disabled = true;
  saveMemoButton.textContent = '저장 중…';
  try {
    const response = await fetch(`/api/admin/reservations/${memoReservationId}/memo`, {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json', ...csrfHeaders() },
      body: JSON.stringify({ adminMemo: adminMemo.value })
    });
    ensureAuthenticated(response);
    const responseText = await response.text();
    let body = {};
    try {
      body = responseText ? JSON.parse(responseText) : {};
    } catch {
      body = {};
    }
    if (!response.ok) {
      const fallback = response.status === 403
        ? '보안 토큰이 만료되었습니다. 페이지를 새로고침한 후 다시 시도해 주세요.'
        : '관리자 메모를 저장하지 못했습니다.';
      throw new Error(body.message || fallback);
    }
    memoDialog.close();
    memoReservationId = null;
    await loadReservations();
    listMessage.textContent = '관리자 메모를 저장했습니다.';
  } catch (error) {
    memoMessage.textContent = error.message;
  } finally {
    saveMemoButton.disabled = false;
    saveMemoButton.textContent = '메모 저장';
  }
}

departmentFilter.addEventListener('change', populateDoctorFilter);
filterForm.addEventListener('submit', (event) => {
  event.preventDefault();
  currentPage = 0;
  loadReservations();
});
document.getElementById('reset-filter').addEventListener('click', () => {
  filterForm.reset();
  populateDoctorFilter();
  currentPage = 0;
  loadReservations();
});
document.getElementById('refresh-button').addEventListener('click', loadReservations);
previousPage.addEventListener('click', () => { currentPage -= 1; loadReservations(); });
nextPage.addEventListener('click', () => { currentPage += 1; loadReservations(); });
memoForm.addEventListener('submit', (event) => {
  event.preventDefault();
  updateAdminMemo();
});
document.querySelectorAll('[data-close-dialog]').forEach((button) => {
  button.addEventListener('click', () => document.getElementById(button.dataset.closeDialog).close());
});
[symptomDialog, memoDialog].forEach((dialog) => {
  dialog.addEventListener('click', (event) => {
    if (event.target === dialog) dialog.close();
  });
});
document.getElementById('logout-button').addEventListener('click', async () => {
  const response = await fetch('/admin/logout', { method: 'POST', headers: csrfHeaders() });
  window.location.assign(response.redirected ? response.url : '/admin/login?logout');
});

loadCsrfToken()
  .then(loadFilterOptions)
  .then(loadReservations)
  .catch((error) => {
    listMessage.className = 'error';
    listMessage.textContent = error.message;
  });
