// 백엔드 직원 API가 연결되기 전 화면 확인용 데이터입니다.
const employees = [
  { employeeId: 1, type: 'DOCTOR', name: '김늘봄', department: '내과', position: '원장', birthDate: '1980-03-12', employmentStatus: 'Y' },
  { employeeId: 2, type: 'DOCTOR', name: '이한결', department: '이비인후과', position: '원장', birthDate: '1984-08-21', employmentStatus: 'L' },
  { employeeId: 3, type: 'DOCTOR', name: '박다온', department: '정형외과', position: '원장', birthDate: '1979-11-05', employmentStatus: 'N' },
  { employeeId: 4, type: 'STAFF', name: '최서윤', department: '간호팀', position: '간호사', birthDate: '1992-06-17', employmentStatus: 'Y' },
  { employeeId: 5, type: 'STAFF', name: '정하람', department: '원무팀', position: '원무 담당', birthDate: '1995-01-28', employmentStatus: 'L' },
  { employeeId: 6, type: 'STAFF', name: '윤지안', department: '검사실', position: '임상병리사', birthDate: '1990-09-09', employmentStatus: 'Y' }
];

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
let currentFilter = 'ALL';

function formatBirthDate(value) {
  return new Intl.DateTimeFormat('ko-KR', { dateStyle: 'medium' }).format(new Date(`${value}T00:00:00`));
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

function renderEmployees() {
  const visibleEmployees = currentFilter === 'ALL'
    ? employees
    : employees.filter((employee) => employee.type === currentFilter);

  employeeList.replaceChildren();
  employeeListTitle.textContent = filterLabels[currentFilter];
  listMessage.textContent = `총 ${visibleEmployees.length}명`;

  if (visibleEmployees.length === 0) {
    const row = document.createElement('tr');
    row.className = 'employee-empty';
    const cell = createCell('해당하는 직원이 없습니다.');
    cell.colSpan = 6;
    row.append(cell);
    employeeList.append(row);
    return;
  }

  visibleEmployees.forEach((employee) => {
    const row = document.createElement('tr');
    const type = createBadgeCell(
      employee.type === 'DOCTOR' ? '의사' : '일반 직원',
      `employee-type ${employee.type === 'DOCTOR' ? 'doctor' : 'staff'}`
    );
    const status = createBadgeCell(
      employmentStatusLabels[employee.employmentStatus],
      `employment-status status-${employee.employmentStatus.toLowerCase()}`
    );

    row.append(
      type,
      createCell(employee.name, 'employee-name'),
      createCell(employee.department),
      createCell(employee.position),
      createCell(formatBirthDate(employee.birthDate)),
      status
    );
    employeeList.append(row);
  });
}

async function loadEmployees(message = '') {
  listMessage.className = '';
  listMessage.textContent = '불러오는 중…';
  try {
    const response = await fetch(`/api/admin/employee?page=${currentPage}&size=20`);
    ensureAuthenticated(response);
    if (!response.ok) throw new Error('직원 목록을 불러오지 못했습니다.');
    const result = await response.json();
    currentPage = result.page;
    renderEmployees(result.items);
    // updateSummary(result);
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
    renderEmployees();
  });
});

document.getElementById('refresh-button').addEventListener('click', renderEmployees);

renderEmployees();
