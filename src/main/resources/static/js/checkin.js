/** checkin.js – Quick Check-In terminal */
function esc(s) { const d = document.createElement('div'); d.textContent = s; return d.innerHTML; }

document.addEventListener('DOMContentLoaded', async () => {
    Layout.init(); await Auth.init();

    const form = document.getElementById('checkin-form');
    const input = document.getElementById('checkin-input');
    const feedback = document.getElementById('checkin-feedback');

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        feedback.innerHTML = '';
        const val = input.value.trim();
        if (!val) return;

        const body = /^\d{10}$/.test(val) ? { phone: val } : { memberId: parseInt(val) };

        try {
            const res = await API.post('/api/checkins', body);
            const data = await res.json();

            if (res.ok) {
                const time = new Date(data.checkinTime).toLocaleString();
                feedback.className = 'checkin-feedback-box success';
                feedback.innerHTML = `
                    <div class="feedback-header"><span>✅</span> Check-In Successful!</div>
                    <div class="feedback-details">
                        <strong>Member:</strong> ${esc(data.memberName)} (ID: ${data.memberId})<br>
                        <strong>Phone:</strong> ${data.memberPhone}<br>
                        <strong>Time:</strong> ${time}<br>
                        <span style="color: var(--blue,#1E4FD8); font-weight: 700;">Access Granted • Enjoy your workout!</span>
                    </div>`;
            } else {
                feedback.className = 'checkin-feedback-box error';
                feedback.innerHTML = `
                    <div class="feedback-header"><span>⛔</span> Access Denied</div>
                    <div class="feedback-details">
                        <strong>Reason:</strong> ${esc(data.message || 'Check-in failed')}<br>
                        <span style="color: var(--red,#D62828); font-weight: 700;">Please renew your membership at the desk.</span>
                    </div>`;
            }
        } catch (err) {
            feedback.className = 'checkin-feedback-box error';
            feedback.innerHTML = `<div class="feedback-header"><span>⚠️</span> Error</div><p>${esc(err.message)}</p>`;
        }
        input.value = '';
        input.focus();
    });
});
