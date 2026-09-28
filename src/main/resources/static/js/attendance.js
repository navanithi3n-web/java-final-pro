/** attendance.js – Attendance list (with date filter) + member attendance */
function esc(s) { const d = document.createElement('div'); d.textContent = s; return d.innerHTML; }

async function initAttendance() {
    Layout.init(); await Auth.init();
    const tbody = document.getElementById('attendance-tbody');
    const dateInput = document.getElementById('attendance-date');

    async function load(date) {
        const url = date ? `/api/checkins?date=${date}` : '/api/checkins';
        const checkins = await API.json('GET', url);
        tbody.innerHTML = checkins.map(c => `
            <tr>
                <td>#${c.checkinId}</td>
                <td><a href="/members/${c.memberId}"><strong>#${c.memberId}</strong> ${esc(c.memberName)}</a></td>
                <td>${c.memberPhone}</td>
                <td>${new Date(c.checkinTime).toLocaleString()}</td>
            </tr>
        `).join('') || '<tr><td colspan="4" class="text-center text-muted">No check-ins found</td></tr>';
    }

    dateInput.value = new Date().toISOString().slice(0, 10);
    await load(dateInput.value);
    dateInput.addEventListener('change', () => load(dateInput.value));
}

async function initMemberAttendance() {
    Layout.init(); await Auth.init();
    const id = window.location.pathname.split('/')[3];
    const tbody = document.getElementById('member-attendance-tbody');

    try {
        const member = await API.json('GET', `/api/members/${id}`);
        document.getElementById('breadcrumb-name').textContent = member.name;

        const checkins = await API.json('GET', `/api/checkins/member/${id}`);
        tbody.innerHTML = checkins.map(c => `
            <tr>
                <td>#${c.checkinId}</td>
                <td>${new Date(c.checkinTime).toLocaleDateString()}</td>
                <td>${new Date(c.checkinTime).toLocaleTimeString()}</td>
            </tr>
        `).join('') || '<tr><td colspan="3" class="text-center text-muted">No attendance records</td></tr>';

        document.getElementById('total-checkins').textContent = checkins.length;
    } catch (e) {
        tbody.parentElement.parentElement.innerHTML = `
            <div class="card-body text-center" style="padding:3rem;">
                <h2 style="color:var(--red);">⛔ Member Not Found</h2>
                <a href="/attendance" class="btn btn-primary">← Back</a>
            </div>`;
    }
}
