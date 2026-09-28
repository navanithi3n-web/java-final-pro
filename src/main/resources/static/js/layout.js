/**
 * layout.js – Injects the shared sidebar and header on every authenticated page.
 * Call Layout.init() after DOMContentLoaded.
 */
const Layout = (() => {
    const NAV_ITEMS = [
        { href: '/dashboard',   icon: '📊', label: 'Dashboard' },
        { href: '/checkin',     icon: '🔑', label: 'Quick Check-In' },
        { href: '/members',     icon: '👥', label: 'Members' },
        { href: '/plans',       icon: '📋', label: 'Plans' },
        { href: '/memberships', icon: '🏷️', label: 'Memberships' },
        { href: '/attendance',  icon: '📅', label: 'Attendance' },
        { href: '/users',       icon: '🔒', label: 'Users', admin: true },
    ];

    function init() {
        injectSidebar();
        markActive();
    }

    function injectSidebar() {
        const sidebar = document.getElementById('sidebar');
        if (!sidebar) return;

        sidebar.innerHTML = `
            <div class="brand">
                <div class="brand-icon">🏋️</div>
                <div class="brand-text">
                    <h2>IronPulse</h2>
                    <div class="brand-tagline">Fitness Tracker</div>
                </div>
            </div>
            <nav class="nav-menu">
                ${NAV_ITEMS.map(n => `
                    <a href="${n.href}" class="nav-item" ${n.admin ? 'data-role="ADMIN"' : ''}>
                        <span class="nav-icon">${n.icon}</span>
                        ${n.label}
                    </a>
                `).join('')}
            </nav>
            <div class="sidebar-footer">
                <div class="server-status">
                    <span class="status-indicator online"></span> Server Online
                </div>
                <div class="version-label">IronPulse v2.0</div>
            </div>
        `;
    }

    function markActive() {
        const path = window.location.pathname;
        document.querySelectorAll('.nav-item').forEach(a => {
            const href = a.getAttribute('href');
            if (path === href || (href !== '/' && path.startsWith(href + '/'))) {
                a.classList.add('active');
            } else if (href === '/dashboard' && (path === '/' || path === '')) {
                a.classList.add('active');
            }
        });
    }

    return { init };
})();
