
import { useAuthStore } from '@/stores/useAuthStore';
import { Button } from '@base-ui/react/button'
import React from 'react'
import { useNavigate } from 'react-router-dom';

export const Logout = () => {
    const {signOut} = useAuthStore();
    const navigate = useNavigate();
    const handleLogout = async () => {
        try {
            await signOut();
            navigate("/signin");

        } catch (error) {
            console.error(error);
        }
    }
    return (
        <Button onClick={handleLogout}>Đăng xuất</Button>
    )
}