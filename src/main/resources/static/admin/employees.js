const filterLabels = {
  ALL: '전체 직원',
  DOCTOR: '의사',
  STAFF: '일반 직원'
};

const employmentStatusLabels = {
  Y: '재직 중',
  L: '휴직 중',
  N: '퇴사'
};

const employeeList = document.getElementById('employee-list');
const employeeListTitle = document.getElementById('employee-list-title');
const listMessage = document.getElementById('list-message');
const filterButtons = document.querySelectorAll('[data-employee-filter]');
const employeeDialog = document.getElementById('employee-dialog');
const employeeForm = document.getElementById('employee-form');
const typeInput = document.getElementById('employee-type');
const departmentInput = document.getElementById('employee-department');
const formMessage = document.getElementById('employee-form-message');
let currentFilter = 'ALL';
let loadedEmployees = [];
let csrfToken = '';

const departmentOptions = {
  DOCTOR: {
    ENT: '이비인후과', INTERNAL_MEDICINE: '내과', ORTHOPEDICS: '정형외과',
    GENERAL_SURGERY: '외과', NEUROSURGERY: '신경외과', REHABILITATION_MEDICINE: '재활의학과'
  },
  STAFF: { NURSING: '간호팀', ADMINISTRATION: '원무팀', LABORATORY: '검사실' }
};

const dayOptions = {
  MONDAY: '월', TUESDAY: '화', WEDNESDAY: '수', THURSDAY: '목', FRIDAY: '금', SATURDAY: '토', SUNDAY: '일'
};

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
  if (!response.ok) {
    throw new Error('보안 토큰을 발급하지 못했습니다. 페이지를 새로고침해 주세요.');
  }
  const result = await response.json();
  csrfToken = result.token;
}

function ensureAuthenticated(response) {
  if (response.redirected && new URL(response.url).pathname === '/admin/login') {
    window.location.assign(response.url);
    throw new Error('로그인이 필요합니다.');
  }
}

function formatBirthDate(value) {
  return new Intl.DateTimeFormat('ko-KR', { dateStyle: 'medium' })
    .format(new Date(`${value}T00:00:00`));
}

function createCell(text, className = '') {
  const cell = document.createElement('td');
  cell.textContent = text;
  if (className) cell.className = className;
  return cell;
}

function createBadgeCell(text, className) {
  const cell = document.createElement('td');
  const badge = document.createElement('span');
  badge.className = className;
  badge.textContent = text;
  cell.append(badge);
  return cell;
}

function renderEmployees(employees) {
  employeeList.replaceChildren();
  employeeListTitle.textContent = filterLabels[currentFilter];

  if (employees.length === 0) {
    const row = document.createElement('tr');
    row.className = 'employee-empty';
    const cell = createCell('해당하는 직원이 없습니다.');
    cell.colSpan = 7;
    row.append(cell);
    employeeList.append(row);
    return;
  }

  employees.forEach((employee) => {
    const row = document.createElement('tr');
    const type = createBadgeCell(
      employee.type === 'DOCTOR' ? '의사' : '일반 직원',
      `employee-type ${employee.type === 'DOCTOR' ? 'doctor' : 'staff'}`
    );
    const status = createBadgeCell(
      employmentStatusLabels[employee.employmentStatus] ?? employee.employmentStatus,
      `employment-status status-${employee.employmentStatus.toLowerCase()}`
    );
    const actions = document.createElement('td');
    actions.className = 'row-actions';
    const editButton = document.createElement('button');
    editButton.type = 'button';
    editButton.textContent = '수정';
    editButton.addEventListener('click', () => openEmployeeDialog(employee));
    const deleteButton = document.createElement('button');
    deleteButton.type = 'button';
    deleteButton.className = 'delete-button';
    deleteButton.textContent = '삭제';
    deleteButton.addEventListener('click', () => deleteEmployee(employee));
    actions.append(editButton, deleteButton);

    row.append(
      type,
      createCell(employee.name, 'employee-name'),
      createCell(employee.department),
      createCell(employee.position),
      createCell(formatBirthDate(employee.birthDate)),
      status,
      actions
    );
    employeeList.append(row);
  });
}

async function loadEmployees() {
  listMessage.className = '';
  listMessage.textContent = '불러오는 중…';
  employeeList.replaceChildren();
  const loadingRow = document.createElement('tr');
  loadingRow.className = 'employee-empty';
  const loadingCell = createCell('직원 목록을 불러오는 중입니다.');
  loadingCell.colSpan = 7;
  loadingRow.append(loadingCell);
  employeeList.append(loadingRow);
  try {
    const response = await fetch(`/api/admin/employees?type=${currentFilter}`);
    ensureAuthenticated(response);
    if (!response.ok) throw new Error('직원 목록을 불러오지 못했습니다.');

    loadedEmployees = await response.json();
    renderEmployees(loadedEmployees);
    listMessage.textContent = `총 ${loadedEmployees.length}명`;
  } catch (error) {
    employeeList.replaceChildren();
    const errorRow = document.createElement('tr');
    errorRow.className = 'employee-empty';
    const errorCell = createCell('직원 목록을 표시할 수 없습니다. 새로고침해 주세요.');
    errorCell.colSpan = 7;
    errorRow.append(errorCell);
    employeeList.append(errorRow);
    listMessage.className = 'error';
    listMessage.textContent = error.message;
  }
}

function renderDepartmentOptions(type, selectedValue = '') {
  departmentInput.replaceChildren();
  Object.entries(departmentOptions[type]).forEach(([value, label]) => {
    departmentInput.add(new Option(label, value, false, value === selectedValue));
  });
  document.getElementById('department-label').textContent = type === 'DOCTOR' ? '진료과' : '부서';
  document.getElementById('position-field').hidden = type === 'DOCTOR';
  document.getElementById('available-days-field').hidden = type !== 'DOCTOR';
}

function renderDayOptions(selectedDays = []) {
  const container = document.getElementById('available-days');
  container.replaceChildren();
  Object.entries(dayOptions).forEach(([value, label]) => {
    const wrapper = document.createElement('label');
    const checkbox = document.createElement('input');
    checkbox.type = 'checkbox';
    checkbox.name = 'available-day';
    checkbox.value = value;
    checkbox.checked = selectedDays.includes(value);
    wrapper.append(checkbox, label);
    container.append(wrapper);
  });
}

function departmentCode(employee) {
  return Object.entries(departmentOptions[employee.type])
    .find(([, label]) => label === employee.department)?.[0] ?? '';
}

function openEmployeeDialog(employee = null) {
  employeeForm.reset();
  formMessage.textContent = '';
  document.getElementById('employee-dialog-title').textContent = employee ? '직원 정보 수정' : '직원 등록';
  document.getElementById('employee-id').value = employee?.employeeId ?? '';
  typeInput.value = employee?.type ?? (currentFilter === 'DOCTOR' ? 'DOCTOR' : 'STAFF');
  typeInput.disabled = Boolean(employee);
  renderDepartmentOptions(typeInput.value, employee ? departmentCode(employee) : '');
  renderDayOptions(employee?.availableDays ?? ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY']);
  document.getElementById('employee-name').value = employee?.name ?? '';
  document.getElementById('employee-position').value = employee?.type === 'STAFF' ? employee.position : '';
  document.getElementById('employee-birth-date').value = employee?.birthDate ?? '';
  document.getElementById('employee-status').value = employee?.employmentStatus ?? 'Y';
  document.getElementById('employee-display-order').value = employee?.displayOrder ?? 1;
  employeeDialog.showModal();
}

function requestBody() {
  const type = typeInput.value;
  return {
    type,
    name: document.getElementById('employee-name').value.trim(),
    department: departmentInput.value,
    position: type === 'STAFF' ? document.getElementById('employee-position').value.trim() : null,
    birthDate: document.getElementById('employee-birth-date').value,
    employmentStatus: document.getElementById('employee-status').value,
    displayOrder: Number(document.getElementById('employee-display-order').value),
    availableDays: type === 'DOCTOR'
      ? [...document.querySelectorAll('[name="available-day"]:checked')].map((input) => input.value)
      : []
  };
}

async function errorMessage(response, fallback) {
  try {
    const error = await response.json();
    return error.message || fallback;
  } catch {
    return fallback;
  }
}

async function saveEmployee(event) {
  event.preventDefault();
  const employeeId = document.getElementById('employee-id').value;
  const body = requestBody();
  if (body.type === 'DOCTOR' && body.availableDays.length === 0) {
    formMessage.textContent = '진료 가능 요일을 한 개 이상 선택해 주세요.';
    return;
  }
  const saveButton = document.getElementById('employee-save-button');
  saveButton.disabled = true;
  formMessage.textContent = '저장 중…';
  try {
    const url = employeeId ? `/api/admin/employees/${body.type}/${employeeId}` : '/api/admin/employees';
    const response = await fetch(url, {
      method: employeeId ? 'PUT' : 'POST',
      headers: { 'Content-Type': 'application/json', ...csrfHeaders() },
      body: JSON.stringify(body)
    });
    ensureAuthenticated(response);
    if (!response.ok) {
      const fallback = response.status === 403
        ? '보안 토큰이 만료되었습니다. 페이지를 새로고침해 주세요.'
        : '직원 정보를 저장하지 못했습니다.';
      throw new Error(await errorMessage(response, fallback));
    }
    employeeDialog.close();
    await loadEmployees();
    listMessage.textContent = employeeId ? '직원 정보를 수정했습니다.' : '직원을 등록했습니다.';
  } catch (error) {
    formMessage.textContent = error.message;
  } finally {
    saveButton.disabled = false;
  }
}

async function deleteEmployee(employee) {
  if (!window.confirm(`${employee.name} 직원을 삭제하시겠습니까?`)) return;
  try {
    const response = await fetch(`/api/admin/employees/${employee.type}/${employee.employeeId}`, {
      method: 'DELETE', headers: csrfHeaders()
    });
    ensureAuthenticated(response);
    if (!response.ok) {
      const fallback = response.status === 403
        ? '보안 토큰이 만료되었습니다. 페이지를 새로고침해 주세요.'
        : '직원을 삭제하지 못했습니다.';
      throw new Error(await errorMessage(response, fallback));
    }
    await loadEmployees();
    listMessage.textContent = '직원 정보를 삭제했습니다.';
  } catch (error) {
    listMessage.className = 'error';
    listMessage.textContent = error.message;
  }
}

filterButtons.forEach((button) => {
  button.addEventListener('click', () => {
    currentFilter = button.dataset.employeeFilter;
    filterButtons.forEach((filterButton) => {
      const selected = filterButton === button;
      filterButton.classList.toggle('active', selected);
      filterButton.setAttribute('aria-pressed', String(selected));
    });
    loadEmployees();
  });
});

document.getElementById('refresh-button').addEventListener('click', loadEmployees);
document.getElementById('add-employee-button').addEventListener('click', () => openEmployeeDialog());
typeInput.addEventListener('change', () => {
  renderDepartmentOptions(typeInput.value);
  renderDayOptions(['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY']);
});
employeeForm.addEventListener('submit', saveEmployee);
document.querySelectorAll('[data-close-dialog]').forEach((button) => {
  button.addEventListener('click', () => employeeDialog.close());
});
employeeDialog.addEventListener('click', (event) => {
  if (event.target === employeeDialog) employeeDialog.close();
});
document.getElementById('logout-button').addEventListener('click', async () => {
  const response = await fetch('/admin/logout', { method: 'POST', headers: csrfHeaders() });
  window.location.assign(response.redirected ? response.url : '/admin/login?logout');
});

loadCsrfToken()
  .then(loadEmployees)
  .catch((error) => {
    listMessage.className = 'error';
    listMessage.textContent = error.message;
  });
