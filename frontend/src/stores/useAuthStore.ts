import {create} from "zustand";
import {toast} from "sonner";
import { authService } from "@/service/authService";
import type { AuthState } from "@/types/store";

export const userAuthStore = create<AuthState>((set, get) => ({
    accressToken: null,
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
    } 
}));