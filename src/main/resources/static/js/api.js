/**
 * api.js – Centralised fetch helper for IronPulse.
 * All API calls go through this module. On 401 it redirects to /login.
 */
const API = (() => {
    const BASE = '';  // same origin

    async function request(method, path, body) {
        const opts = {
            method,
            headers: { 'Content-Type': 'application/json' },
            credentials: 'same-origin'
        };
        if (body !== undefined && body !== null) {
            opts.body = JSON.stringify(body);
        }
        const res = await fetch(BASE + path, opts);

        // Redirect to login on 401 (session expired / not logged in)
        if (res.status === 401 && !path.startsWith('/api/auth/')) {
            window.location.href = '/login';
            throw new Error('Unauthorized');
        }

        return res;
    }

    return {
        get:    (path)       => request('GET', path),
        post:   (path, body) => request('POST', path, body),
        put:    (path, body) => request('PUT', path, body),
        del:    (path)       => request('DELETE', path),

        /** Convenience: returns parsed JSON or throws with the error message. */
        async json(method, path, body) {
            const res = await request(method, path, body);
            const data = await res.json();
            if (!res.ok) {
                throw new Error(data.message || `Error ${res.status}`);
            }
            return data;
        }
    };
})();
