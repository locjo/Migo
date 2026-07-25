import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080/api', // 👈 Trỏ về backend Spring Boot
  withCredentials: true,             // Bắt buộc nếu bạn dùng Session / Cookie
});

export default api;