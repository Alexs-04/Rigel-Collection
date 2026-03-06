import axios from 'axios';

const API_BASE = import.meta.env.VITE_API_BASE || 'http://localhost:8080';

const api = axios.create({
    baseURL: API_BASE,
    headers: {
        'Content-Type': 'application/json',
    },
    withCredentials: false,
});

function readJwtPayload(token) {
    try {
        return JSON.parse(atob(token.split('.')[1]));
    } catch {
        return null;
    }
}

function invalidateSession() {
    localStorage.removeItem('access_token');
    localStorage.removeItem('refresh_token');
    window.dispatchEvent(new Event('auth:invalidated'));
}

api.interceptors.request.use((config) => {
    const token = localStorage.getItem('access_token');
    if (!token || !config.headers) {
        return config;
    }

    const payload = readJwtPayload(token);
    const isExpired = !payload?.exp || (payload.exp * 1000) <= Date.now();
    if (isExpired) {
        invalidateSession();
        return config;
    }

    config.headers.Authorization = `Bearer ${token}`;
    return config;
});

api.interceptors.response.use(
    (response) => response,
    async (error) => {
        const status = error?.response?.status;
        if (status === 401 || status === 403) {
            invalidateSession();
        }
        return Promise.reject(error);
    }
);

export default api;

