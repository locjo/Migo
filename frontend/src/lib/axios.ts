import { useAuthStore } from '@/stores/useAuthStore';
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080/api', // 👈 Trỏ về backend Spring Boot
  withCredentials: true,             // Bắt buộc nếu bạn dùng Session / Cookie
});

// gan access token vao req header
api.interceptors.request.use(
  (config) => {
    const {accessToken} = useAuthStore.getState();
    if (accessToken) {
      config.headers.Authorization = `Bearer ${accessToken}`;
    }
    return config;
  }
);
export default api;