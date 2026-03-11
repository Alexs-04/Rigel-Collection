import React, {useEffect, useMemo, useState} from 'react';
import api from '../services/api';
import {AuthContext} from './AuthContext';

function parseJwt(token) {
    try {
        const payload = token.split('.')[1];
        return JSON.parse(atob(payload));
    } catch {
        return null;
    }
}

function mapPayloadToUser(payload) {
    if (!payload) return null;
    return {
        username: payload.sub || payload.name || payload.email,
        role: payload.role || 'USER',
        active: payload.active !== false,
    };
}

export function AuthProvider({children}) {
    const [accessToken, setAccessToken] = useState(localStorage.getItem('access_token'));

    useEffect(() => {
        const syncSession = () => setAccessToken(localStorage.getItem('access_token'));
        window.addEventListener('auth:invalidated', syncSession);
        window.addEventListener('storage', syncSession);

        return () => {
            window.removeEventListener('auth:invalidated', syncSession);
            window.removeEventListener('storage', syncSession);
        };
    }, []);

    const user = useMemo(() => mapPayloadToUser(parseJwt(accessToken)), [accessToken]);

    const hasRole = (...roles) => {
        if (!user?.role) return false;
        return roles.includes(user.role);
    };

    const login = async (email, password) => {
        const res = await api.post('/auth/login', {email, password});
        const data = res.data;
        if (data?.access_token) {
            localStorage.setItem('access_token', data.access_token);
            localStorage.setItem('refresh_token', data.refresh_token);
            setAccessToken(data.access_token);
            return {success: true};
        }
        return {success: false};
    };

    const logout = () => {
        localStorage.removeItem('access_token');
        localStorage.removeItem('refresh_token');
        setAccessToken(null);
    };

    return (
        <AuthContext.Provider value={{user, accessToken, hasRole, login, logout}}>
            {children}
        </AuthContext.Provider>
    );
}
