const statusLabels = {
  RECEIVED: '접수됨',
  IN_PROGRESS: '처리 중',
  ANSWERED: '답변 완료'
};

const inquiryList = document.getElementById('inquiry-list');
const listMessage = document.getElementById('list-message');
const previousPage = document.getElementById('previous-page');
const nextPage = document.getElementById('next-page');
const pageInfo = document.getElementById('page-info');
let currentPage = 0;
const counts = {
  total: document.getElementById('total-count'),
  received: document.getElementById('received-count'),
  progress: document.getElementById('progress-count'),
  answered: document.getElementById('answered-count')
};

function getCookie(name) {
  const cookie = document.cookie.split('; ').find((item) => item.startsWith(`${name}=`));
  return cookie ? decodeURIComponent(cookie.substring(name.length + 1)) : '';
}

function csrfHeaders() {
  const token = getCookie('XSRF-TOKEN');
  return token ? { 'X-XSRF-TOKEN': token } : {};
}

function ensureAuthenticated(response) {
  if (response.redirected && new URL(response.url).pathname === '/admin/login') {
    window.location.assign(response.url);
    throw new Error('로그인이 필요합니다.');
  }
}

function formatDateTime(value) {
  return new Intl.DateTimeFormat('ko-KR', {
    dateStyle: 'medium',
    timeStyle: 'short'
  }).format(new Date(value));
}

function formatPhoneNumber(value) {
  if (!value) return '';
  if (value.startsWith('02')) {
    return value.replace(/^(02)(\d{3,4})(\d{4})$/, '$1-$2-$3');
  }
  return value.replace(/^(\d{3})(\d{3,4})(\d{4})$/, '$1-$2-$3');
}

function createStatusBadge(status) {
  const badge = document.createElement('span');
  badge.className = `status-badge ${status.toLowerCase().replace('_', '')}`;
  badge.textContent = statusLabels[status];
  return badge;
}

function createStatusSelect(status) {
  const select = document.createElement('select');
  Object.entries(statusLabels).forEach(([value, label]) => {
    select.add(new Option(label, value, value === status, value === status));
  });
  return select;
}

function updateSummary(result) {
  counts.total.textContent = result.totalElements;
  counts.received.textContent = result.receivedCount;
  counts.progress.textContent = result.inProgressCount;
  counts.answered.textContent = result.answeredCount;
}

async function saveStatus(inquiryId, status, button) {
  button.disabled = true;
  button.textContent = '저장 중…';
  try {
    const response = await fetch(`/api/admin/inquiries/${inquiryId}/status`, {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json', ...csrfHeaders() },
      body: JSON.stringify({ status })
    });
    ensureAuthenticated(response);
    if (!response.ok) throw new Error('상태 변경에 실패했습니다.');
    await loadInquiries('상태를 변경했습니다.');
  } catch (error) {
    listMessage.className = 'error';
    listMessage.textContent = error.message;
    button.disabled = false;
    button.textContent = '상태 저장';
  }
}

function renderInquiries(inquiries) {
  inquiryList.replaceChildren();
  if (inquiries.length === 0) {
    const empty = document.createElement('p');
    empty.className = 'empty';
    empty.textContent = '아직 접수된 문의가 없습니다.';
    inquiryList.append(empty);
    return;
  }

  inquiries.forEach((inquiry) => {
    const card = document.createElement('article');
    card.className = 'inquiry-card';

    const person = document.createElement('div');
    const name = document.createElement('h3');
    name.className = 'person-name';
    name.textContent = inquiry.name;
    person.append(name);
    [formatPhoneNumber(inquiry.phoneNumber), inquiry.email].filter(Boolean).forEach((value) => {
      const contact = document.createElement('p');
      contact.className = 'contact';
      contact.textContent = value;
      person.append(contact);
    });

    const detail = document.createElement('div');
    const content = document.createElement('p');
    content.className = 'inquiry-content';
    content.textContent = inquiry.content;
    const createdAt = document.createElement('time');
    createdAt.className = 'created-at';
    createdAt.dateTime = inquiry.createdAt;
    createdAt.textContent = formatDateTime(inquiry.createdAt);
    detail.append(content, createdAt);

    const statusArea = document.createElement('div');
    statusArea.className = 'status-area';
    statusArea.append(createStatusBadge(inquiry.status));
    const select = createStatusSelect(inquiry.status);
    const button = document.createElement('button');
    button.type = 'button';
    button.textContent = '상태 저장';
    button.addEventListener('click', () => saveStatus(inquiry.inquiryId, select.value, button));
    statusArea.append(select, button);

    card.append(person, detail, statusArea);
    inquiryList.append(card);
  });
}

async function loadInquiries(message = '') {
  listMessage.className = '';
  listMessage.textContent = '불러오는 중…';
  try {
    const response = await fetch(`/api/admin/inquiries?page=${currentPage}&size=20`);
    ensureAuthenticated(response);
    if (!response.ok) throw new Error('문의 목록을 불러오지 못했습니다.');
    const result = await response.json();
    currentPage = result.page;
    renderInquiries(result.items);
    updateSummary(result);
    listMessage.textContent = message || `총 ${result.totalElements}건`;
    pageInfo.textContent = result.totalPages === 0 ? '0 / 0' : `${result.page + 1} / ${result.totalPages}`;
    previousPage.disabled = result.page <= 0;
    nextPage.disabled = result.page + 1 >= result.totalPages;
  } catch (error) {
    listMessage.className = 'error';
    listMessage.textContent = error.message;
  }
}

document.getElementById('refresh-button').addEventListener('click', () => loadInquiries());
previousPage.addEventListener('click', () => { currentPage -= 1; loadInquiries(); });
nextPage.addEventListener('click', () => { currentPage += 1; loadInquiries(); });
document.getElementById('logout-button').addEventListener('click', async () => {
  const response = await fetch('/admin/logout', { method: 'POST', headers: csrfHeaders() });
  window.location.assign(response.redirected ? response.url : '/admin/login?logout');
});
loadInquiries();
