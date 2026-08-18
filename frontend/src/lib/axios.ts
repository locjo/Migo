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

api.interceptors.response.use((res) => res, async (error) => {
  const originalRequest = error.config;

  // nhung api k can check
  if (originalRequest.url.includes("auth/signin") || 
      originalRequest.url.includes("auth/signup") ||
      originalRequest.url.includes("auth/refresh")
    ){
      return Promise.reject(error);
    }

    originalRequest._retryCount = originalRequest._retryCount || 0;
    
      if (error.response?.status === 403 && originalRequest._retryCount < 4){
        originalRequest._retryCount += 1;

        console.log("refresh");
        try{
          const res = await api.post("/auth/refresh", {withCredentials: true})
          const newAccessToken = res.data.accessToken;
          useAuthStore.getState().setAccessToken(newAccessToken);

          originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
          return api(originalRequest);
        }catch (refreshError){
          useAuthStore.getState().clearState();
          return Promise.reject(refreshError);
        }
      }
      return Promise.reject(error);
  })

export default api;