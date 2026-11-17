import axios from 'axios';
import {isJwtExpired} from '../utils/jwt';

const API_BASE = import.meta.env.VITE_API_BASE || 'http://localhost:8080';

export const ACCESS_TOKEN_KEY = 'access_token';
export const REFRESH_TOKEN_KEY = 'refresh_token';
export const AUTH_INVALIDATED_EVENT = 'auth:invalidated';
export const AUTH_REFRESHED_EVENT = 'auth:refreshed';

/** Endpoints that must never carry a token nor trigger a refresh. */
const PUBLIC_PATHS = ['/auth/login', '/auth/refresh', '/auth/logout', '/account/'];

const api = axios.create({
    baseURL: API_BASE,
    headers: {'Content-Type': 'application/json'},
    withCredentials: false,
});

// Separate client for refresh calls so they bypass the interceptors below.
const authClient = axios.create({
    baseURL: API_BASE,
    headers: {'Content-Type': 'application/json'},
});

function readStorage(key) {
    try {
        return localStorage.getItem(key);
    } catch {
        return null;
    }
}

export function getAccessToken() {
    return readStorage(ACCESS_TOKEN_KEY);
}

export function getRefreshToken() {
    return readStorage(REFRESH_TOKEN_KEY);
}

export function storeTokens(accessToken, refreshToken) {
    localStorage.setItem(ACCESS_TOKEN_KEY, accessToken);
    localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
    window.dispatchEvent(new Event(AUTH_REFRESHED_EVENT));
}

export function clearTokens() {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
}

function invalidateSession() {
    clearTokens();
    window.dispatchEvent(new Event(AUTH_INVALIDATED_EVENT));
}

function isPublicRequest(config) {
    const url = config?.url || '';
    return PUBLIC_PATHS.some((path) => url.startsWith(path));
}

let refreshInFlight = null;

/**
 * Exchanges the refresh token for a new pair. Concurrent callers share the same request,
 * because the backend rotates refresh tokens and a second parallel refresh would be rejected.
 *
 * @returns {Promise<string>} the new access token
 */
export function refreshAccessToken() {
    if (refreshInFlight) return refreshInFlight;

    const refreshToken = getRefreshToken();
    if (!refreshToken || isJwtExpired(refreshToken, 0)) {
        invalidateSession();
        return Promise.reject(new Error('No hay una sesión que renovar'));
    }

    refreshInFlight = authClient
        .post('/auth/refresh', {refresh_token: refreshToken})
        .then(({data}) => {
            if (!data?.access_token || !data?.refresh_token) {
                throw new Error('Respuesta de renovación inválida');
            }
            storeTokens(data.access_token, data.refresh_token);
            return data.access_token;
        })
        .catch((error) => {
            invalidateSession();
            throw error;
        })
        .finally(() => {
            refreshInFlight = null;
        });

    return refreshInFlight;
}

api.interceptors.request.use(async (config) => {
    if (isPublicRequest(config)) return config;

    let token = getAccessToken();
    if (!token) return config;

    if (isJwtExpired(token)) {
        try {
            token = await refreshAccessToken();
        } catch {
            return config; // continues unauthenticated; the 401 handler below has nothing left to do
        }
    }

    config.headers.Authorization = `Bearer ${token}`;
    return config;
});

api.interceptors.response.use(
    (response) => response,
    async (error) => {
        const status = error?.response?.status;
        const original = error?.config;

        // 401 = no valid session: try one refresh, then give up and log out.
        // 403 = valid session without permission: keep the user logged in.
        if (status === 401 && original && !original._retried && !isPublicRequest(original) && getRefreshToken()) {
            original._retried = true;
            try {
                const token = await refreshAccessToken();
                original.headers.Authorization = `Bearer ${token}`;
                return api(original);
            } catch {
                return Promise.reject(error);
            }
        }

        if (status === 401 && !isPublicRequest(original)) {
            invalidateSession();
        }
        return Promise.reject(error);
    }
);

/**
 * Revokes the session on the server (best effort) and clears local tokens.
 * Sends the current access token even if expired: the server still uses it to find the session.
 */
export async function logoutRequest() {
    const token = getAccessToken();
    clearTokens();
    if (!token) return;
    try {
        await authClient.post('/auth/logout', null, {headers: {Authorization: `Bearer ${token}`}});
    } catch {
        // Ignore: local tokens are already gone and the server tokens expire on their own.
    }
}

export default api;
