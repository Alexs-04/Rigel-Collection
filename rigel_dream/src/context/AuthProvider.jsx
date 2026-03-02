import React, { useState, useEffect } from 'react';
import api from '../services/api';
import { AuthContext } from './AuthContext';

function parseJwt(token) {
  try {
    const payload = token.split('.')[1];
    const decoded = JSON.parse(atob(payload));
    return decoded;
  } catch (e) {
    return null;
  }
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [accessToken, setAccessToken] = useState(localStorage.getItem('access_token'));

  useEffect(() => {
    if (accessToken) {
      const payload = parseJwt(accessToken);
      setUser(payload ? { username: payload.sub || payload.name || payload.email } : null);
    }
  }, [accessToken]);

  const login = async (email, password) => {
    const res = await api.post('/auth/login', { email, password });
    const data = res.data;
    if (data?.access_token) {
      localStorage.setItem('access_token', data.access_token);
      localStorage.setItem('refresh_token', data.refresh_token);
      setAccessToken(data.access_token);
      const payload = parseJwt(data.access_token);
      setUser(payload ? { username: payload.sub || payload.name || payload.email } : null);
      return { success: true };
    }
    return { success: false };
  };

  const logout = () => {
    localStorage.removeItem('access_token');
    localStorage.removeItem('refresh_token');
    setAccessToken(null);
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, accessToken, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}
