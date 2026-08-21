import type { Message } from "@/types/chat";
import type { Conversation } from "./chat";
import type { User } from "./user";

export interface AuthState {
    accessToken: string | null;
    user: User | null;
    loading: boolean;

    setAccessToken: (accessToken: string) => void;
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
    refresh: () => Promise<void>;
}

export interface ThemeState {
    isDark: boolean;
    toggleTheme: () => void;
    setTheme: (dark: boolean) => void;

}

export interface ChatState {
    conversations: Conversation[];
    messages: Record<string, {
        items: Message[];
        hasMore: boolean; // infinitie-scroll
        nextCursor?: string | null, 
    }>;
    activeConversationId: string | null;
    convoLoading: boolean;
    messageLoading: boolean;
    reset: () => void;

    setActiveConversation: (id: string | null) => void;
    fetchConversations:() => Promise<void>;
    fetchMessages: (conversationId?: string) => Promise<void>;

}