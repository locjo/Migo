import { BrowserRouter, Route, Routes } from "react-router-dom";
import { SignInPage } from "./pages/SignInPage";
import { SignUpPage } from "./pages/SignUpPage";
import { ChatAppPage } from "./pages/ChatAppPage";
import { Toaster } from "sonner";
import ProtectedRoute from "./components/auth/ProtectedRoute";

function App() { 
  return (
    <>
      <Toaster richColors />
      <BrowserRouter>
        <Routes>
          <Route path="/signin" element={<SignInPage />} />
          <Route path="/signup" element={<SignUpPage />} />

          {/* Protected Routes */}
          <Route element={<ProtectedRoute />}>
            <Route path="/" element={<ChatAppPage />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </>
  );
}  

export default App;