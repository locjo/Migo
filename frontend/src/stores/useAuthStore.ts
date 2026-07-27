import {create} from "zustand";
import {toast} from "sonner";
import { authService } from "@/service/authService";
import type { AuthState } from "@/types/store";

export const useAuthStore = create<AuthState>((set, get) => ({
    accessToken: null,
    user: null,
    loading: false,

    clearState: () => {
        set({accessToken: null, user: null, loading: false});
    },
    
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
        set({ loading: true });
        const response = await authService.signIn(username, password);
        const token = response.result?.accessToken || response.accessToken;

        console.log("Token sau khi bóc tách:", token); // 👈 Đã ra đúng eyJhbG...

        set({ accessToken: token });

        // ⚠️ PHẢI TRUYỀN `token` VÀO ĐÂY:
        await get().fetchMe(token); 

        toast.success("Chào mừng bạn quay lại với Migo.");
        } catch (error) {
            console.error("Lỗi signIn:", error);
            toast.error("Đăng nhập thất bại.");
            throw error;
        } finally {
            set({ loading: false });
        }
    },

    signOut: async () => {
        try {
            get().clearState();
            await authService.signOut();
            toast.success("Đăng xuất thành công.");
        } catch (error) {
            console.error(error);
            toast.error("Đăng xuất thất bại. Vui lòng thử lại.");
        }
    },

    fetchMe: async (customToken?: string) => {
    try {
        set({ loading: true });
        const user = await authService.fetchMe(customToken); // Truyền tiếp sang authService
        set({ user });
    } catch (error) {
        set({ user: null, accessToken: null });
        throw error;
    } finally {
        set({ loading: false });
    }
    }
}));