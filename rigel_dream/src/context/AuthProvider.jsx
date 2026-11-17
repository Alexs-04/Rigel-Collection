import React, {useCallback, useEffect, useMemo, useState} from 'react';
import api, {
    AUTH_INVALIDATED_EVENT,
    AUTH_REFRESHED_EVENT,
    getAccessToken,
    logoutRequest,
    storeTokens,
} from '../services/api';
import {decodeJwtPayload} from '../utils/jwt';
import {AuthContext} from './AuthContext';

function mapPayloadToUser(payload) {
    if (!payload) return null;
    return {
        username: payload.sub,
        name: payload.name,
        role: payload.role || 'USER',
        active: payload.active !== false,
    };
}

export function AuthProvider({children}) {
    const [accessToken, setAccessToken] = useState(getAccessToken);

    useEffect(() => {
        const syncSession = () => setAccessToken(getAccessToken());
        window.addEventListener(AUTH_INVALIDATED_EVENT, syncSession);
        window.addEventListener(AUTH_REFRESHED_EVENT, syncSession);
        window.addEventListener('storage', syncSession); // other tabs

        return () => {
            window.removeEventListener(AUTH_INVALIDATED_EVENT, syncSession);
            window.removeEventListener(AUTH_REFRESHED_EVENT, syncSession);
            window.removeEventListener('storage', syncSession);
        };
    }, []);

    const user = useMemo(() => mapPayloadToUser(decodeJwtPayload(accessToken)), [accessToken]);

    const hasRole = useCallback((...roles) => Boolean(user?.role) && roles.includes(user.role), [user]);

    const login = useCallback(async (email, password) => {
        const {data} = await api.post('/auth/login', {email, password});
        if (data?.access_token && data?.refresh_token) {
            storeTokens(data.access_token, data.refresh_token);
            setAccessToken(data.access_token);
            return {success: true};
        }
        return {success: false};
    }, []);

    const logout = useCallback(async () => {
        setAccessToken(null);
        await logoutRequest();
    }, []);

    const value = useMemo(
        () => ({user, accessToken, hasRole, login, logout}),
        [user, accessToken, hasRole, login, logout]
    );

    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
