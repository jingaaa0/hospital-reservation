const dashboardDate = document.getElementById("dashboard-date");
const dashboardMessage = document.getElementById("dashboard-message");
const reservationList = document.getElementById("dashboard-reservations");
const refreshButton = document.getElementById("refresh-button");

const countElements = {
  totalReservations: document.getElementById("total-reservations"),
  requestedReservations: document.getElementById("requested-reservations"),
  confirmedReservations: document.getElementById("confirmed-reservations"),
  pendingInquiries: document.getElementById("pending-inquiries"),
};

const statusLabels = {
  REQUESTED: "신청",
  CONFIRMED: "확정",
  COMPLETED: "진료 완료",
  CANCELLED: "취소",
  NO_SHOW: "미방문",
};

function ensureAuthenticated(response) {
  if (
    response.redirected &&
    new URL(response.url).pathname === "/admin/login"
  ) {
    window.location.assign(response.url);
    throw new Error("로그인이 필요합니다.");
  }
}

function showTableMessage(message) {
  const row = document.createElement("tr");
  row.className = "empty-row";

  const cell = document.createElement("td");
  cell.colSpan = 4;
  cell.textContent = message;

  row.append(cell);
  reservationList.replaceChildren(row);
}

function renderReservations(reservations) {
  if (reservations.length === 0) {
    showTableMessage("오늘 진료 예약이 없습니다.");
    return;
  }

  reservationList.replaceChildren();

  reservations.forEach((reservation) => {
    const row = document.createElement("tr");

    const values = [
      reservation.appointmentTime.slice(0, 5),
      reservation.patientName,
      reservation.doctorName,
      statusLabels[reservation.status] ?? reservation.status,
    ];

    values.forEach((value) => {
      const cell = document.createElement("td");
      cell.textContent = value;
      row.append(cell);
    });

    reservationList.append(row);
  });
}

async function loadDashboard() {
  refreshButton.disabled = true;
  dashboardMessage.className = "";
  dashboardMessage.textContent = "불러오는 중…";
  dashboardDate.textContent = "기준 날짜를 불러오는 중입니다.";

  Object.values(countElements).forEach((element) => {
    element.textContent = "—";
  });
  showTableMessage("예약 정보를 불러오는 중입니다.");

  try {
    const response = await fetch("/api/admin/dashboard");
    ensureAuthenticated(response);

    if (!response.ok) {
      throw new Error("대시보드를 불러오지 못했습니다.");
    }

    const result = await response.json();

    dashboardDate.textContent = `진료 예약일 기준: ${result.date} (한국 시간)`;

    Object.entries(countElements).forEach(([key, element]) => {
      element.textContent = result[key];
    });

    renderReservations(result.reservations);
    dashboardMessage.textContent = "조회 완료";
  } catch (error) {
    dashboardDate.textContent = "기준 날짜를 확인하지 못했습니다.";
    dashboardMessage.className = "error";
    dashboardMessage.textContent =
      error.message || "조회에 실패했습니다. 다시 시도해 주세요.";
    showTableMessage("예약 정보를 표시할 수 없습니다. 새로고침해 주세요.");
  } finally {
    refreshButton.disabled = false;
  }
}

refreshButton.addEventListener("click", loadDashboard);
loadDashboard();

const logoutButton = document.getElementById("logout-button");

logoutButton.addEventListener("click", async () => {
  logoutButton.disabled = true;

  try {
    const tokenResponse = await fetch("/api/admin/csrf");
    ensureAuthenticated(tokenResponse);

    if (!tokenResponse.ok) {
      throw new Error("보안 토큰을 가져오지 못했습니다.");
    }

    const csrf = await tokenResponse.json();

    const response = await fetch("/admin/logout", {
      method: "POST",
      headers: { [csrf.headerName]: csrf.token },
    });

    if (!response.ok) {
      throw new Error("로그아웃하지 못했습니다. 다시 시도해 주세요.");
    }

    window.location.assign("/admin/login?logout");
  } catch (error) {
    dashboardMessage.className = "error";
    dashboardMessage.textContent =
      error.message || "로그아웃 요청에 실패했습니다.";
  } finally {
    logoutButton.disabled = false;
  }
});
