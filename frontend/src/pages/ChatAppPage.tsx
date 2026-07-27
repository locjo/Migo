import { Logout } from '@/components/auth/Logout';
import { useAuthStore } from '@/stores/useAuthStore';
import React from 'react'

export const ChatAppPage = () => {
  const user = useAuthStore((s) => s.user);
  return (
    <div>
      {user?.username}
      <Logout />
    </div>
  );
};

