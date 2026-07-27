import api from '@/lib/axios';

export const authService = {
    signUp: async (
        username: string,
        password: string,
        email: string,
        firstName: string,
        lastName: string,
    ) => {
        const res = await api.post('/auth/signup', {
            username,
            password,
            email,
            firstName,
            lastName},
            {withCredentials: true}
        );
        return res.data;
    },

    signIn: async (username: string, password: string) => {
        const res = await api.post('/auth/signin', {
            username,
            password
        },
        {withCredentials: true}
        );
        return res.data;
    },

    signOut: async () => {
        const res = await api.post('/auth/signout', 
            {}, 
            {  
                withCredentials: true,
            });
        return res.data;
    },

    fetchMe: async (token?: string) => {
    // Nếu có token truyền trực tiếp thì ưu tiên dùng ngay, không thì để Interceptor tự lấy
    const config = token 
        ? { headers: { Authorization: `Bearer ${token}` } } 
        : {};

    const res = await api.get('/users/me', config);
    
    // Nhớ return đúng object result từ Spring Boot
    return res.data.result || res.data; 
    }
};