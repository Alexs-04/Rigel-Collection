import axios from 'axios';

const API_BASE = import.meta.env.VITE_API_BASE || 'http://localhost:8080';

const api = axios.create({
  baseURL: API_BASE,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: false, // cambiar a true si usas cookies de refresh
});

// Attach token if present in localStorage
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('access_token');
  if (token && config.headers) {
    config.headers['Authorization'] = `Bearer ${token}`;
  }
  return config;
});

// Response interceptor para manejar 401/403 globalmente
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const status = error?.response?.status;
    if (status === 401 || status === 403) {
      // aquí podrías intentar refresh token
      localStorage.removeItem('access_token');
      localStorage.removeItem('refresh_token');
      // opcional: window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;

