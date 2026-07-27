export interface AuthState {
    accessToken: string | null;
    user: User | null;
    loading: boolean;
    signUp: (
        username: string,
        password: string,
        email: string,
        firstName: string,
        lastName: string
    ) => Promise<void>;

    clearState: () => void;
    signIn: (
        username: string, 
        password: string
    ) => Promise<void>;
    signOut: () => Promise<void>;
    fetchMe: () => Promise<void>;
}