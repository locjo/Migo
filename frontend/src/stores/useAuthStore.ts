import {create} from "zustand";
import {toast} from "sonner";
import { authService } from "@/service/authService";
import type { AuthState } from "@/types/store";

export const userAuthStore = create<AuthState>((set, get) => ({
    accessToken: null,
    user: null,
    loading: false,
    
    signUp: async (username, password, email, firstName, lastName) => {
        try {
            set({loading: true});

            // goi api
            await authService.signUp(username, password, email, firstName, lastName);
            

            toast.success("Đăng ký thành công. Vui lòng đăng nhập.");

        }catch (error) {
            console.error(error);
            toast.error("Đăng ký thất bại. Vui lòng thử lại.");
        } finally{
            set({loading: false});
        }
    },
    
    signIn: async (username, password) => {
        try {
            set({loading: true});

            // goi api
            const {accessToken} = await authService.signIn(username, password);
            set({accessToken});

            toast.success("Chào mừng bạn quay lại với Migo.");

        }catch (error) {
            console.error(error);
            toast.error("Đăng nhập thất bại. Vui lòng thử lại.");
        } finally{
            set({loading: false});
        }
    } 
}));