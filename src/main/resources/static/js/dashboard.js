/** dashboard.js – Loads the dashboard stats and recent check-ins */
document.addEventListener('DOMContentLoaded', async () => {
    Layout.init();
    await Auth.init();

    try {
        const stats = await API.json('GET', '/api/dashboard/stats');
        document.getElementById('stat-total-members').textContent = stats.totalMembers ?? 0;
        document.getElementById('stat-active-memberships').textContent = stats.activeMemberships ?? 0;
        document.getElementById('stat-today-checkins').textContent = stats.todayCheckins ?? 0;
    } catch (e) {
        console.error('Failed to load stats', e);
    }

    try {
        const checkins = await API.json('GET', '/api/checkins?date=' + new Date().toISOString().slice(0,10));
        const tbody = document.getElementById('recent-checkins-body');
        if (tbody) {
            tbody.innerHTML = checkins.slice(0, 10).map(c => `
                <tr>
                    <td><strong>#${c.memberId}</strong></td>
                    <td>${esc(c.memberName)}</td>
                    <td>${c.memberPhone}</td>
                    <td>${new Date(c.checkinTime).toLocaleTimeString()}</td>
                </tr>
            `).join('') || '<tr><td colspan="4" class="text-center text-muted">No check-ins today</td></tr>';
        }
    } catch (e) {
        console.error('Failed to load recent checkins', e);
    }
});

function esc(s) { const d = document.createElement('div'); d.textContent = s; return d.innerHTML; }
