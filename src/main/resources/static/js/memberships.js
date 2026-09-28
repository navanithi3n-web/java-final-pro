/** memberships.js – Memberships list, detail, create form */
function esc(s) { const d = document.createElement('div'); d.textContent = s; return d.innerHTML; }

async function initMembershipsList() {
    Layout.init(); await Auth.init();
    const tbody = document.getElementById('memberships-tbody');
    const memberships = await API.json('GET', '/api/memberships');
    tbody.innerHTML = memberships.map(m => `
        <tr>
            <td><strong>#${m.membershipId}</strong></td>
            <td><a href="/members/${m.memberId}">${esc(m.memberName)}</a></td>
            <td><span class="badge badge-primary">${esc(m.planName)}</span></td>
            <td>${m.startDate}</td>
            <td>${m.expiryDate}</td>
            <td><span class="badge ${m.status === 'ACTIVE' ? 'badge-active' : 'badge-expired'}">${m.status}</span></td>
            <td><a href="/memberships/${m.membershipId}" class="btn btn-primary btn-sm">View</a></td>
        </tr>
    `).join('') || '<tr><td colspan="7" class="text-center text-muted">No memberships</td></tr>';
}

async function initMembershipDetail() {
    Layout.init(); await Auth.init();
    const id = window.location.pathname.split('/')[2];
    const container = document.getElementById('membership-detail');

    try {
        const ms = await API.json('GET', `/api/memberships/${id}`);
        document.getElementById('breadcrumb-name').textContent = '#' + ms.membershipId;
        document.getElementById('detail-member').innerHTML = `<a href="/members/${ms.memberId}">${esc(ms.memberName)}</a>`;
        document.getElementById('detail-plan').textContent = ms.planName;
        document.getElementById('detail-start').textContent = ms.startDate;
        document.getElementById('detail-expiry').textContent = ms.expiryDate;

        const statusEl = document.getElementById('detail-status');
        statusEl.className = `badge ${ms.status === 'ACTIVE' ? 'badge-active' : 'badge-expired'}`;
        statusEl.textContent = ms.status;

        // Days remaining
        const today = new Date();
        const expiry = new Date(ms.expiryDate);
        const days = Math.ceil((expiry - today) / (1000 * 60 * 60 * 24));
        document.getElementById('detail-days').textContent = days > 0 ? days + ' days' : 'Expired';

        // Renew form
        const plans = await API.json('GET', '/api/plans');
        const planSelect = document.getElementById('renew-plan');
        planSelect.innerHTML = plans.map(p =>
            `<option value="${p.planId}" ${p.planId === ms.planId ? 'selected' : ''}>${esc(p.planName)} (${p.durationInDays} days – ₹${p.price})</option>`
        ).join('');

        document.getElementById('renew-form').addEventListener('submit', async (e) => {
            e.preventDefault();
            const errEl = document.getElementById('renew-error');
            errEl.style.display = 'none';
            try {
                await API.json('PUT', `/api/memberships/${id}/renew`, {
                    planId: parseInt(planSelect.value)
                });
                window.location.reload();
            } catch (err) {
                errEl.textContent = err.message;
                errEl.style.display = 'block';
            }
        });
    } catch (e) {
        container.innerHTML = `
            <div class="card" style="border-top-color: var(--red);">
                <div class="card-body text-center" style="padding: 3rem;">
                    <h2 style="color: var(--red);">⛔ Membership Not Found</h2>
                    <p class="text-muted" style="margin:1rem 0;">Membership #${esc(id)} does not exist.</p>
                    <a href="/memberships" class="btn btn-primary">← Back to Memberships</a>
                </div>
            </div>`;
    }
}

async function initMembershipForm() {
    Layout.init(); await Auth.init();
    const form = document.getElementById('membership-form');

    // Load members and plans for dropdowns
    const [members, plans] = await Promise.all([
        API.json('GET', '/api/members'),
        API.json('GET', '/api/plans')
    ]);

    const memberSelect = document.getElementById('field-member');
    memberSelect.innerHTML = '<option value="">Select a member…</option>' +
        members.map(m => `<option value="${m.memberId}">${esc(m.name)} (${m.phone})</option>`).join('');

    const planSelect = document.getElementById('field-plan');
    planSelect.innerHTML = '<option value="">Select a plan…</option>' +
        plans.map(p => `<option value="${p.planId}">${esc(p.planName)} (${p.durationInDays} days – ₹${p.price})</option>`).join('');

    // Pre-fill from query param ?memberId=X
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.get('memberId')) memberSelect.value = urlParams.get('memberId');

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const errEl = document.getElementById('form-error');
        errEl.style.display = 'none';
        try {
            const result = await API.json('POST', '/api/memberships', {
                memberId: parseInt(memberSelect.value),
                planId: parseInt(planSelect.value),
                startDate: document.getElementById('field-start-date').value || null
            });
            window.location.href = `/memberships/${result.membershipId}`;
        } catch (err) {
            errEl.textContent = err.message;
            errEl.style.display = 'block';
        }
    });
}
