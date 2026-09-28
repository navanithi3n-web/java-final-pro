/**
 * IronPulse Gym Tracker - Single Page Application Logic
 * Vanilla JavaScript utilizing Fetch API
 */

// Application State
const state = {
    currentTab: 'dashboard',
    members: [],
    plans: [],
    memberships: [],
    attendance: []
};

// DOM Content Loaded Handler
document.addEventListener('DOMContentLoaded', () => {
    initNavigation();
    initEventHandlers();
    loadDashboardData();
    loadPlans();
    loadMembers();
    loadMemberships();
    loadAttendance();
});

// Toast notification helper
function showToast(message, type = 'success') {
    const container = document.getElementById('toast-container');
    if (!container) return;

    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    const icon = type === 'success' ? '✅' : '⚠️';
    toast.innerHTML = `<span>${icon}</span><span>${message}</span>`;
    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        setTimeout(() => toast.remove(), 250);
    }, 3500);
}

// --------------------------------------------------------------------------
// Navigation & Tab Switching
// --------------------------------------------------------------------------
function initNavigation() {
    const navButtons = document.querySelectorAll('.nav-item');
    const sections = document.querySelectorAll('.tab-section');

    const pageTitles = {
        dashboard: { title: 'Dashboard Overview', sub: 'Real-time statistics and gym activity' },
        checkin: { title: 'Attendance Terminal', sub: 'Fast verification and gym entrance check-in' },
        members: { title: 'Members Directory', sub: 'Register and manage athlete profiles' },
        memberships: { title: 'Membership Management', sub: 'Manage passes, dates and plan renewals' },
        attendance: { title: 'Attendance Records', sub: 'Historical and daily check-in log' }
    };

    function switchTab(tabId) {
        state.currentTab = tabId;

        navButtons.forEach(btn => {
            btn.classList.toggle('active', btn.dataset.tab === tabId);
        });

        sections.forEach(sec => {
            sec.classList.toggle('active', sec.id === `${tabId}-section`);
        });

        const meta = pageTitles[tabId] || pageTitles.dashboard;
        const pageTitleEl = document.getElementById('page-title');
        const pageSubEl = document.getElementById('page-subtitle');
        if (pageTitleEl) pageTitleEl.textContent = meta.title;
        if (pageSubEl) pageSubEl.textContent = meta.sub;

        // Auto-refresh data on tab switch
        if (tabId === 'dashboard') loadDashboardData();
        if (tabId === 'members') loadMembers();
        if (tabId === 'memberships') { loadMemberships(); loadMembers(); loadPlans(); }
        if (tabId === 'attendance') loadAttendance();
        if (tabId === 'checkin') {
            const input = document.getElementById('main-checkin-input');
            if (input) setTimeout(() => input.focus(), 100);
        }
    }

    navButtons.forEach(btn => {
        btn.addEventListener('click', () => switchTab(btn.dataset.tab));
    });

    const headerCheckinBtn = document.getElementById('quick-checkin-header-btn');
    if (headerCheckinBtn) {
        headerCheckinBtn.addEventListener('click', () => switchTab('checkin'));
    }
}

// --------------------------------------------------------------------------
// Event Handlers Setup
// --------------------------------------------------------------------------
function initEventHandlers() {
    // 1. Dashboard quick check-in form
    const dashCheckinForm = document.getElementById('dash-checkin-form');
    if (dashCheckinForm) {
        dashCheckinForm.addEventListener('submit', (e) => {
            e.preventDefault();
            const val = document.getElementById('dash-checkin-input').value.trim();
            if (val) executeCheckin(val, 'dash-checkin-result', () => {
                document.getElementById('dash-checkin-input').value = '';
                loadDashboardData();
            });
        });
    }

    // 2. Main checkin form
    const mainCheckinForm = document.getElementById('main-checkin-form');
    if (mainCheckinForm) {
        mainCheckinForm.addEventListener('submit', (e) => {
            e.preventDefault();
            const val = document.getElementById('main-checkin-input').value.trim();
            if (val) executeCheckin(val, 'checkin-feedback-display', () => {
                document.getElementById('main-checkin-input').value = '';
                loadDashboardData();
            });
        });
    }

    // 3. Member registration & edit form
    const memberForm = document.getElementById('member-form');
    if (memberForm) {
        memberForm.addEventListener('submit', handleMemberFormSubmit);
    }
    const cancelEditBtn = document.getElementById('cancel-member-edit-btn');
    if (cancelEditBtn) {
        cancelEditBtn.addEventListener('click', resetMemberForm);
    }

    // 4. Member search
    const searchBtn = document.getElementById('member-search-btn');
    const searchInput = document.getElementById('member-search-input');
    const searchResetBtn = document.getElementById('member-search-reset-btn');
    if (searchBtn && searchInput) {
        searchBtn.addEventListener('click', () => loadMembers(searchInput.value.trim()));
        searchInput.addEventListener('keydown', (e) => {
            if (e.key === 'Enter') {
                e.preventDefault();
                loadMembers(searchInput.value.trim());
            }
        });
    }
    if (searchResetBtn && searchInput) {
        searchResetBtn.addEventListener('click', () => {
            searchInput.value = '';
            loadMembers();
        });
    }

    // 5. Membership assignment form
    const membershipForm = document.getElementById('membership-form');
    if (membershipForm) {
        membershipForm.addEventListener('submit', handleMembershipAssignSubmit);
    }
    const refreshMembershipsBtn = document.getElementById('refresh-memberships-btn');
    if (refreshMembershipsBtn) {
        refreshMembershipsBtn.addEventListener('click', () => loadMemberships());
    }

    // 6. Renewal modal handling
    const renewModal = document.getElementById('renew-modal');
    const renewCloseBtn = document.getElementById('renew-modal-close');
    const renewCancelBtn = document.getElementById('renew-modal-cancel');
    const renewForm = document.getElementById('renew-form');
    if (renewCloseBtn) renewCloseBtn.addEventListener('click', closeRenewModal);
    if (renewCancelBtn) renewCancelBtn.addEventListener('click', closeRenewModal);
    if (renewForm) renewForm.addEventListener('submit', handleRenewSubmit);

    // 7. Attendance filters
    const filterBtn = document.getElementById('attendance-filter-btn');
    const filterAllBtn = document.getElementById('attendance-filter-all-btn');
    const dateInput = document.getElementById('attendance-date-filter');
    if (filterBtn && dateInput) {
        filterBtn.addEventListener('click', () => {
            loadAttendance(dateInput.value);
        });
    }
    if (filterAllBtn && dateInput) {
        filterAllBtn.addEventListener('click', () => {
            dateInput.value = '';
            loadAttendance();
        });
    }

    // Refresh dashboard button
    const refreshDashBtn = document.getElementById('refresh-dashboard-btn');
    if (refreshDashBtn) {
        refreshDashBtn.addEventListener('click', () => loadDashboardData());
    }
}

// --------------------------------------------------------------------------
// Check-In Processing Core Logic
// --------------------------------------------------------------------------
async function executeCheckin(inputVal, feedbackContainerId, onSuccess) {
    const feedbackBox = document.getElementById(feedbackContainerId);
    if (feedbackBox) {
        feedbackBox.style.display = 'block';
        feedbackBox.className = 'checkin-feedback-box';
        feedbackBox.innerHTML = '<p class="text-muted">Verifying membership eligibility...</p>';
    }

    const payload = {};
    if (/^\d{10}$/.test(inputVal)) {
        payload.phone = inputVal;
    } else if (!isNaN(inputVal)) {
        payload.memberId = Number(inputVal);
    } else {
        payload.phone = inputVal;
    }

    try {
        const res = await fetch('/api/checkins', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        const data = await res.json();

        if (res.ok) {
            const timeStr = new Date(data.checkinTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' });
            if (feedbackBox) {
                feedbackBox.className = 'checkin-feedback-box success';
                feedbackBox.innerHTML = `
                    <div class="feedback-header">
                        <span>✅</span> Check-In Successful!
                    </div>
                    <div class="feedback-details">
                        <strong>Member:</strong> ${escapeHtml(data.memberName)} (ID: ${data.memberId})<br>
                        <strong>Phone:</strong> ${escapeHtml(data.memberPhone)}<br>
                        <strong>Timestamp:</strong> ${timeStr}<br>
                        <span style="color: var(--blue,#1E4FD8); font-weight: 700;">Access Granted &bull; Enjoy your workout!</span>
                    </div>
                `;
            }
            showToast(`Check-in verified for ${data.memberName}`, 'success');
            if (onSuccess) onSuccess();
        } else {
            const errorMsg = data.message || 'Check-in failed';
            if (feedbackBox) {
                feedbackBox.className = 'checkin-feedback-box error';
                feedbackBox.innerHTML = `
                    <div class="feedback-header">
                        <span>⛔</span> Access Denied
                    </div>
                    <div class="feedback-details">
                        <strong>Reason:</strong> ${escapeHtml(errorMsg)}<br>
                        <span style="color: var(--red,#D62828); font-weight: 700;">Please renew your membership at the desk to continue access.</span>
                    </div>
                `;
            }
            showToast(errorMsg, 'error');
        }
    } catch (err) {
        if (feedbackBox) {
            feedbackBox.className = 'checkin-feedback-box error';
            feedbackBox.innerHTML = `<div class="feedback-header"><span>⚠️</span> Connection Error</div><p>${err.message}</p>`;
        }
        showToast('Network error while checking in', 'error');
    }
}

// --------------------------------------------------------------------------
// Dashboard Operations
// --------------------------------------------------------------------------
async function loadDashboardData() {
    try {
        const statsRes = await fetch('/api/dashboard/stats');
        if (statsRes.ok) {
            const stats = await statsRes.json();
            const totalEl = document.getElementById('stat-total-members');
            const activeEl = document.getElementById('stat-active-memberships');
            const checkinsEl = document.getElementById('stat-today-checkins');

            if (totalEl) totalEl.textContent = stats.totalMembers ?? 0;
            if (activeEl) activeEl.textContent = stats.activeMemberships ?? 0;
            if (checkinsEl) checkinsEl.textContent = stats.todayCheckins ?? 0;
        }

        // Load today's checkins feed
        const todayStr = new Date().toISOString().split('T')[0];
        const checkinsRes = await fetch(`/api/checkins?date=${todayStr}`);
        if (checkinsRes.ok) {
            const list = await checkinsRes.json();
            renderDashboardRecent(list);
        }
    } catch (err) {
        console.error('Error loading dashboard data:', err);
    }
}

function renderDashboardRecent(checkins) {
    const tbody = document.getElementById('dashboard-recent-tbody');
    if (!tbody) return;

    if (!checkins || checkins.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" class="text-center text-muted" style="padding: 2rem;">No check-ins logged today yet.</td></tr>';
        return;
    }

    tbody.innerHTML = checkins.slice(0, 10).map(c => {
        const timeStr = new Date(c.checkinTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' });
        return `
            <tr>
                <td><strong>${timeStr}</strong></td>
                <td>#${c.memberId}</td>
                <td><strong>${escapeHtml(c.memberName)}</strong></td>
                <td>${escapeHtml(c.memberPhone)}</td>
                <td><span class="badge badge-active">VERIFIED</span></td>
            </tr>
        `;
    }).join('');
}

// --------------------------------------------------------------------------
// Plans Catalog
// --------------------------------------------------------------------------
async function loadPlans() {
    try {
        const res = await fetch('/api/plans');
        if (res.ok) {
            state.plans = await res.json();
            renderPlansCatalog();
            populatePlanSelects();
        }
    } catch (err) {
        console.error('Error loading plans:', err);
    }
}

function renderPlansCatalog() {
    const container = document.getElementById('plans-catalog-list');
    if (!container) return;

    if (state.plans.length === 0) {
        container.innerHTML = '<p class="text-muted">No plans configured.</p>';
        return;
    }

    container.innerHTML = state.plans.map(p => `
        <div class="plan-item">
            <div>
                <div class="plan-title">${escapeHtml(p.planName)} Plan</div>
                <div class="plan-meta">Duration: ${p.durationInDays} days &bull; Full Gym Access</div>
            </div>
            <div class="plan-price">₹${p.price.toFixed(0)}</div>
        </div>
    `).join('');
}

function populatePlanSelects() {
    const assignSelect = document.getElementById('membership-plan-select');
    const renewSelect = document.getElementById('renew-plan-select');

    const options = state.plans.map(p =>
        `<option value="${p.planId}">${escapeHtml(p.planName)} (${p.durationInDays} days - ₹${p.price.toFixed(0)})</option>`
    ).join('');

    if (assignSelect) {
        assignSelect.innerHTML = '<option value="">-- Choose Plan --</option>' + options;
    }
    if (renewSelect) {
        renewSelect.innerHTML = options;
    }
}

// --------------------------------------------------------------------------
// Members Operations
// --------------------------------------------------------------------------
async function loadMembers(search = '') {
    try {
        let url = '/api/members';
        if (search) {
            url += `?search=${encodeURIComponent(search)}`;
        }
        const res = await fetch(url);
        if (res.ok) {
            state.members = await res.json();
            renderMembersTable();
            populateMemberSelect();
        }
    } catch (err) {
        console.error('Error loading members:', err);
    }
}

function populateMemberSelect() {
    const select = document.getElementById('membership-member-select');
    if (!select) return;

    if (state.members.length === 0) {
        select.innerHTML = '<option value="">-- No members found (Register first) --</option>';
        return;
    }

    select.innerHTML = '<option value="">-- Choose Member --</option>' +
        state.members.map(m =>
            `<option value="${m.memberId}">#${m.memberId} - ${escapeHtml(m.name)} (${escapeHtml(m.phone)})</option>`
        ).join('');
}

function renderMembersTable() {
    const tbody = document.getElementById('members-tbody');
    if (!tbody) return;

    if (state.members.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" class="text-center text-muted" style="padding: 2rem;">No members found.</td></tr>';
        return;
    }

    tbody.innerHTML = state.members.map(m => `
        <tr>
            <td><strong>#${m.memberId}</strong></td>
            <td>${escapeHtml(m.name)}</td>
            <td>${escapeHtml(m.phone)}</td>
            <td>${escapeHtml(m.email)}</td>
            <td>${m.joinDate || '-'}</td>
            <td>
                <div class="form-actions" style="margin: 0;">
                    <button class="btn btn-outline btn-sm" onclick="editMember(${m.memberId})">Edit</button>
                    <button class="btn btn-danger btn-sm" onclick="deleteMember(${m.memberId})">Delete</button>
                </div>
            </td>
        </tr>
    `).join('');
}

async function handleMemberFormSubmit(e) {
    e.preventDefault();
    const id = document.getElementById('member-form-id').value;
    const name = document.getElementById('member-name').value.trim();
    const phone = document.getElementById('member-phone').value.trim();
    const email = document.getElementById('member-email').value.trim();
    const joinDate = document.getElementById('member-joindate').value || null;

    const payload = { name, phone, email, joinDate };

    try {
        let res;
        if (id) {
            res = await fetch(`/api/members/${id}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
        } else {
            res = await fetch('/api/members', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
        }

        const data = await res.json();
        if (res.ok) {
            showToast(id ? 'Member updated successfully' : 'Member registered successfully', 'success');
            resetMemberForm();
            loadMembers();
            loadDashboardData();
        } else {
            showToast(data.message || 'Operation failed', 'error');
        }
    } catch (err) {
        showToast(err.message, 'error');
    }
}

function editMember(id) {
    const member = state.members.find(m => m.memberId === id);
    if (!member) return;

    document.getElementById('member-form-id').value = member.memberId;
    document.getElementById('member-name').value = member.name;
    document.getElementById('member-phone').value = member.phone;
    document.getElementById('member-email').value = member.email;
    document.getElementById('member-joindate').value = member.joinDate || '';

    document.getElementById('member-form-title').textContent = `✏️ Edit Member #${member.memberId}`;
    document.getElementById('save-member-btn').textContent = 'Update Member';
    document.getElementById('cancel-member-edit-btn').style.display = 'inline-flex';

    document.getElementById('member-name').scrollIntoView({ behavior: 'smooth' });
}

function resetMemberForm() {
    const form = document.getElementById('member-form');
    if (form) form.reset();
    document.getElementById('member-form-id').value = '';
    document.getElementById('member-form-title').textContent = '➕ Register New Member';
    document.getElementById('save-member-btn').textContent = 'Register Member';
    document.getElementById('cancel-member-edit-btn').style.display = 'none';
}

async function deleteMember(id) {
    if (!confirm(`Are you sure you want to delete member #${id}? This will also delete all their memberships and check-in history.`)) {
        return;
    }

    try {
        const res = await fetch(`/api/members/${id}`, { method: 'DELETE' });
        if (res.ok) {
            showToast('Member removed successfully', 'success');
            loadMembers();
            loadMemberships();
            loadDashboardData();
        } else {
            const data = await res.json();
            showToast(data.message || 'Failed to delete member', 'error');
        }
    } catch (err) {
        showToast(err.message, 'error');
    }
}

// --------------------------------------------------------------------------
// Memberships Operations
// --------------------------------------------------------------------------
async function loadMemberships() {
    try {
        const res = await fetch('/api/memberships');
        if (res.ok) {
            state.memberships = await res.json();
            renderMembershipsTable();
        }
    } catch (err) {
        console.error('Error loading memberships:', err);
    }
}

function renderMembershipsTable() {
    const tbody = document.getElementById('memberships-tbody');
    if (!tbody) return;

    if (state.memberships.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="text-center text-muted" style="padding: 2rem;">No memberships assigned yet.</td></tr>';
        return;
    }

    tbody.innerHTML = state.memberships.map(m => {
        const isAct = m.status === 'ACTIVE';
        const badgeClass = isAct ? 'badge-active' : 'badge-expired';
        const badgeText = isAct ? 'ACTIVE' : 'EXPIRED';

        return `
            <tr>
                <td><strong>#${m.membershipId}</strong></td>
                <td>
                    <strong>${escapeHtml(m.memberName || 'Member #' + m.memberId)}</strong>
                    <div class="text-muted text-sm">${escapeHtml(m.memberPhone || '')}</div>
                </td>
                <td>
                    <span class="badge badge-primary">${escapeHtml(m.planName)}</span>
                    <span class="text-muted text-sm">(${m.durationInDays}d)</span>
                </td>
                <td>${m.startDate}</td>
                <td><strong>${m.expiryDate}</strong></td>
                <td><span class="badge ${badgeClass}">${badgeText}</span></td>
                <td>
                    <button class="btn btn-outline btn-sm" onclick="openRenewModal(${m.membershipId})">
                        🔄 Renew
                    </button>
                </td>
            </tr>
        `;
    }).join('');
}

async function handleMembershipAssignSubmit(e) {
    e.preventDefault();
    const memberId = Number(document.getElementById('membership-member-select').value);
    const planId = Number(document.getElementById('membership-plan-select').value);
    const startDate = document.getElementById('membership-start-date').value || null;

    if (!memberId || !planId) {
        showToast('Please select both a member and a plan', 'error');
        return;
    }

    const payload = { memberId, planId, startDate };

    try {
        const res = await fetch('/api/memberships', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        const data = await res.json();
        if (res.ok) {
            showToast('Membership assigned successfully', 'success');
            document.getElementById('membership-form').reset();
            loadMemberships();
            loadDashboardData();
        } else {
            showToast(data.message || 'Failed to assign membership', 'error');
        }
    } catch (err) {
        showToast(err.message, 'error');
    }
}

function openRenewModal(membershipId) {
    const mem = state.memberships.find(m => m.membershipId === membershipId);
    if (!mem) return;

    document.getElementById('renew-membership-id').value = membershipId;
    document.getElementById('renew-member-desc').innerHTML = `
        Renewing pass for <strong>${escapeHtml(mem.memberName)}</strong> (Current: ${escapeHtml(mem.planName)}, Expiry: <strong>${mem.expiryDate}</strong>)
    `;

    const select = document.getElementById('renew-plan-select');
    if (select && mem.planId) {
        select.value = mem.planId;
    }

    const modal = document.getElementById('renew-modal');
    if (modal) modal.classList.add('show');
}

function closeRenewModal() {
    const modal = document.getElementById('renew-modal');
    if (modal) modal.classList.remove('show');
}

async function handleRenewSubmit(e) {
    e.preventDefault();
    const id = document.getElementById('renew-membership-id').value;
    const planId = document.getElementById('renew-plan-select').value;

    const payload = { planId: planId ? Number(planId) : null };

    try {
        const res = await fetch(`/api/memberships/${id}/renew`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        const data = await res.json();
        if (res.ok) {
            showToast(`Renewed! New expiry date: ${data.expiryDate}`, 'success');
            closeRenewModal();
            loadMemberships();
            loadDashboardData();
        } else {
            showToast(data.message || 'Failed to renew membership', 'error');
        }
    } catch (err) {
        showToast(err.message, 'error');
    }
}

// --------------------------------------------------------------------------
// Attendance Operations
// --------------------------------------------------------------------------
async function loadAttendance(dateFilter = '') {
    try {
        let url = '/api/checkins';
        if (dateFilter) {
            url += `?date=${encodeURIComponent(dateFilter)}`;
        }
        const res = await fetch(url);
        if (res.ok) {
            state.attendance = await res.json();
            renderAttendanceTable();
        }
    } catch (err) {
        console.error('Error loading attendance:', err);
    }
}

function renderAttendanceTable() {
    const tbody = document.getElementById('attendance-tbody');
    if (!tbody) return;

    if (state.attendance.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" class="text-center text-muted" style="padding: 2rem;">No check-in logs found.</td></tr>';
        return;
    }

    tbody.innerHTML = state.attendance.map(a => {
        const dateObj = new Date(a.checkinTime);
        const formattedDate = dateObj.toLocaleDateString() + ' ' + dateObj.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

        return `
            <tr>
                <td><strong>#${a.checkinId}</strong></td>
                <td>${formattedDate}</td>
                <td>#${a.memberId}</td>
                <td><strong>${escapeHtml(a.memberName)}</strong></td>
                <td>${escapeHtml(a.memberPhone)}</td>
            </tr>
        `;
    }).join('');
}

// --------------------------------------------------------------------------
// Utility Functions
// --------------------------------------------------------------------------
function escapeHtml(text) {
    if (!text) return '';
    return String(text)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

// Global scope attachment for inline event handlers (onclick)
window.editMember = editMember;
window.deleteMember = deleteMember;
window.openRenewModal = openRenewModal;
