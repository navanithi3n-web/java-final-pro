/**
 * auth.js – Loads the current user, updates the header, handles logout.
 * Include this on every page (except login.html).
 */
const Auth = (() => {
    let currentUser = null;

    async function init() {
        try {
            const res = await API.get('/api/auth/me');
            if (!res.ok) { window.location.href = '/login'; return; }
            currentUser = await res.json();
            renderUserBadge();
            applyRoleVisibility();
        } catch (e) {
            window.location.href = '/login';
        }
    }

    function renderUserBadge() {
        const el = document.getElementById('user-badge');
        if (el && currentUser) {
            el.innerHTML = `
                <span class="user-info">
                    <span class="user-name">${esc(currentUser.fullName)}</span>
                    <span class="user-role badge ${currentUser.role === 'ADMIN' ? 'badge-active' : 'badge-info'}">${currentUser.role}</span>
                </span>
                <button class="btn btn-danger btn-sm" id="logout-btn">Logout</button>
            `;
            document.getElementById('logout-btn').addEventListener('click', logout);
        }
    }

    function applyRoleVisibility() {
        if (!currentUser) return;
        document.querySelectorAll('[data-role="ADMIN"]').forEach(el => {
            el.style.display = currentUser.role === 'ADMIN' ? '' : 'none';
        });
    }

    async function logout() {
        await API.post('/api/auth/logout');
        window.location.href = '/login';
    }

    function user() { return currentUser; }
    function isAdmin() { return currentUser && currentUser.role === 'ADMIN'; }

    function esc(s) {
        const d = document.createElement('div');
        d.textContent = s;
        return d.innerHTML;
    }

    return { init, user, isAdmin, logout };
})();
