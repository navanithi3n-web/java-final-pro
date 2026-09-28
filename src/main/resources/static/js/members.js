/** members.js – Members list, member detail, member form */
function esc(s) { const d = document.createElement('div'); d.textContent = s; return d.innerHTML; }

// ── Members List Page ─────────────────────────────────────────────────────
async function initMembersList() {
    Layout.init(); await Auth.init();
    const searchInput = document.getElementById('member-search');
    const tbody = document.getElementById('members-tbody');

    async function load(query) {
        const url = query ? `/api/members?search=${encodeURIComponent(query)}` : '/api/members';
        const members = await API.json('GET', url);
        tbody.innerHTML = members.map(m => `
            <tr>
                <td><strong>#${m.memberId}</strong></td>
                <td>${esc(m.name)}</td>
                <td>${m.phone}</td>
                <td>${m.email}</td>
                <td>${m.joinDate}</td>
                <td><a href="/members/${m.memberId}" class="btn btn-primary btn-sm">View</a></td>
            </tr>
        `).join('') || '<tr><td colspan="6" class="text-center text-muted">No members found</td></tr>';
    }

    await load();
    if (searchInput) {
        let timer;
        searchInput.addEventListener('input', () => {
            clearTimeout(timer);
            timer = setTimeout(() => load(searchInput.value), 300);
        });
    }
}

// ── Member Detail Page ────────────────────────────────────────────────────
async function initMemberDetail() {
    Layout.init(); await Auth.init();
    const parts = window.location.pathname.split('/');
    const id = parts[2];
    const container = document.getElementById('member-detail');

    try {
        const member = await API.json('GET', `/api/members/${id}`);
        document.getElementById('breadcrumb-name').textContent = member.name;

        document.getElementById('detail-name').textContent = member.name;
        document.getElementById('detail-phone').textContent = member.phone;
        document.getElementById('detail-email').textContent = member.email;
        document.getElementById('detail-join-date').textContent = member.joinDate;

        // Load memberships
        const memberships = await API.json('GET', `/api/memberships/member/${id}`);
        const mBody = document.getElementById('memberships-tbody');
        if (mBody) {
            mBody.innerHTML = memberships.map(ms => `
                <tr>
                    <td><a href="/memberships/${ms.membershipId}">#${ms.membershipId}</a></td>
                    <td><span class="badge badge-primary">${esc(ms.planName)}</span></td>
                    <td>${ms.startDate}</td>
                    <td>${ms.expiryDate}</td>
                    <td><span class="badge ${ms.status === 'ACTIVE' ? 'badge-active' : 'badge-expired'}">${ms.status}</span></td>
                </tr>
            `).join('') || '<tr><td colspan="5" class="text-center text-muted">No memberships</td></tr>';
        }

        // Load last 10 check-ins
        const checkins = await API.json('GET', `/api/checkins/member/${id}`);
        const cBody = document.getElementById('checkins-tbody');
        if (cBody) {
            cBody.innerHTML = checkins.slice(0, 10).map(c => `
                <tr>
                    <td>#${c.checkinId}</td>
                    <td>${new Date(c.checkinTime).toLocaleString()}</td>
                </tr>
            `).join('') || '<tr><td colspan="2" class="text-center text-muted">No check-ins</td></tr>';
        }

        // Delete button (ADMIN only)
        const deleteBtn = document.getElementById('delete-member-btn');
        if (deleteBtn) {
            deleteBtn.addEventListener('click', async () => {
                if (!confirm(`Delete member "${member.name}"? This cannot be undone.`)) return;
                await API.del(`/api/members/${id}`);
                window.location.href = '/members';
            });
        }
    } catch (e) {
        container.innerHTML = `
            <div class="card" style="border-top-color: var(--red);">
                <div class="card-body text-center" style="padding: 3rem;">
                    <h2 style="color: var(--red);">⛔ Member Not Found</h2>
                    <p class="text-muted" style="margin: 1rem 0;">The member with ID #${esc(id)} does not exist.</p>
                    <a href="/members" class="btn btn-primary">← Back to Members</a>
                </div>
            </div>`;
    }
}

// ── Member Form (Create & Edit) ───────────────────────────────────────────
async function initMemberForm() {
    Layout.init(); await Auth.init();
    const parts = window.location.pathname.split('/');
    const isEdit = parts.includes('edit');
    const id = isEdit ? parts[2] : null;
    const form = document.getElementById('member-form');
    const title = document.getElementById('form-title');

    if (isEdit && id) {
        title.textContent = 'Edit Member';
        document.getElementById('breadcrumb-action').textContent = 'Edit';
        try {
            const member = await API.json('GET', `/api/members/${id}`);
            document.getElementById('field-name').value = member.name;
            document.getElementById('field-phone').value = member.phone;
            document.getElementById('field-email').value = member.email;
        } catch (e) {
            document.getElementById('form-error').textContent = 'Member not found';
            document.getElementById('form-error').style.display = 'block';
        }
    }

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const errorEl = document.getElementById('form-error');
        errorEl.style.display = 'none';
        const body = {
            name: document.getElementById('field-name').value,
            phone: document.getElementById('field-phone').value,
            email: document.getElementById('field-email').value
        };
        try {
            let result;
            if (isEdit) {
                result = await API.json('PUT', `/api/members/${id}`, body);
            } else {
                result = await API.json('POST', '/api/members', body);
            }
            window.location.href = `/members/${result.memberId}`;
        } catch (err) {
            errorEl.textContent = err.message;
            errorEl.style.display = 'block';
        }
    });
}
