/** users.js – ADMIN-only user management page */
function esc(s) { const d = document.createElement('div'); d.textContent = s; return d.innerHTML; }

document.addEventListener('DOMContentLoaded', async () => {
    Layout.init(); await Auth.init();

    if (!Auth.isAdmin()) {
        document.getElementById('users-content').innerHTML =
            '<div class="card"><div class="card-body text-center" style="padding:3rem;"><h2 style="color:var(--red);">⛔ Access Denied</h2><p class="text-muted">Only administrators can manage users.</p><a href="/dashboard" class="btn btn-primary">← Dashboard</a></div></div>';
        return;
    }

    await loadUsers();

    document.getElementById('user-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const errEl = document.getElementById('form-error');
        errEl.style.display = 'none';
        try {
            await API.json('POST', '/api/users', {
                username: document.getElementById('field-username').value,
                password: document.getElementById('field-password').value,
                fullName: document.getElementById('field-fullname').value,
                role: document.getElementById('field-role').value
            });
            document.getElementById('user-form').reset();
            await loadUsers();
        } catch (err) {
            errEl.textContent = err.message;
            errEl.style.display = 'block';
        }
    });
});

async function loadUsers() {
    const tbody = document.getElementById('users-tbody');
    const users = await API.json('GET', '/api/users');
    tbody.innerHTML = users.map(u => `
        <tr>
            <td><strong>#${u.userId}</strong></td>
            <td>${esc(u.username)}</td>
            <td>${esc(u.fullName)}</td>
            <td><span class="badge ${u.role === 'ADMIN' ? 'badge-active' : 'badge-info'}">${u.role}</span></td>
            <td><span class="badge ${u.enabled ? 'badge-active' : 'badge-expired'}">${u.enabled ? 'Yes' : 'No'}</span></td>
            <td>${new Date(u.createdAt).toLocaleDateString()}</td>
        </tr>
    `).join('');
}
