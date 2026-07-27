import { useAuthStore } from '@/stores/useAuthStore';
import { Navigate } from 'react-router-dom';
import { Outlet } from 'react-router-dom';

const ProtectedRoute = () => {
    const {accessToken, user, loading} = useAuthStore();
    if(!accessToken){
        return (
            <Navigate 
              to="/signin" 
              replace
            />
        )
    }
  return (
    <Outlet></Outlet>
  )
}

export default ProtectedRoute