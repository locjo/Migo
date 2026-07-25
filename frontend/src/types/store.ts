export interface AuthState {
    accressToken: string | null;
    user: User | null;
    loading: boolean;
    signUp: (
        username: string,
        password: string,
        email: string,
        firstName: string,
        lastName: string
    ) => Promise<void>;
}