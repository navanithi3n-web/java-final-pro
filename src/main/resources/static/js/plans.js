/** plans.js – Plans list + plan detail */
function esc(s) { const d = document.createElement('div'); d.textContent = s; return d.innerHTML; }

async function initPlansList() {
    Layout.init(); await Auth.init();
    const tbody = document.getElementById('plans-tbody');
    const plans = await API.json('GET', '/api/plans');
    tbody.innerHTML = plans.map(p => `
        <tr>
            <td><strong>#${p.planId}</strong></td>
            <td>${esc(p.planName)}</td>
            <td>${p.durationInDays} days</td>
            <td>₹${p.price.toLocaleString()}</td>
            <td><a href="/plans/${p.planId}" class="btn btn-primary btn-sm">View</a></td>
        </tr>
    `).join('') || '<tr><td colspan="5" class="text-center text-muted">No plans</td></tr>';
}

async function initPlanDetail() {
    Layout.init(); await Auth.init();
    const id = window.location.pathname.split('/')[2];
    const container = document.getElementById('plan-detail');

    try {
        const plan = await API.json('GET', `/api/plans/${id}`);
        document.getElementById('breadcrumb-name').textContent = plan.planName;
        document.getElementById('detail-name').textContent = plan.planName;
        document.getElementById('detail-duration').textContent = plan.durationInDays + ' days';
        document.getElementById('detail-price').textContent = '₹' + plan.price.toLocaleString();

        // Count memberships using this plan
        const memberships = await API.json('GET', '/api/memberships');
        const count = memberships.filter(m => m.planId === plan.planId).length;
        document.getElementById('detail-usage-count').textContent = count;

        // Edit form
        document.getElementById('edit-name').value = plan.planName;
        document.getElementById('edit-duration').value = plan.durationInDays;
        document.getElementById('edit-price').value = plan.price;

        document.getElementById('plan-edit-form').addEventListener('submit', async (e) => {
            e.preventDefault();
            const errEl = document.getElementById('edit-error');
            errEl.style.display = 'none';
            try {
                await API.json('PUT', `/api/plans/${id}`, {
                    planName: document.getElementById('edit-name').value,
                    durationInDays: parseInt(document.getElementById('edit-duration').value),
                    price: parseFloat(document.getElementById('edit-price').value)
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
                    <h2 style="color: var(--red);">⛔ Plan Not Found</h2>
                    <p class="text-muted" style="margin:1rem 0;">Plan #${esc(id)} does not exist.</p>
                    <a href="/plans" class="btn btn-primary">← Back to Plans</a>
                </div>
            </div>`;
    }
}
