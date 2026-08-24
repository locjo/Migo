// src/types/chat.ts

export interface Participant {
  userId: string | undefined;
  id?: string;
  displayName?: string;
  username?: string;
  avatarUrl?: string;
  avatar?: string;
}

export interface Conversation {
  name?: string;
  id?: string;
  unreadCount?: Record<string, number>;
  lastMessage?: string;
  participants: Participant[];
  type: "Group" | "Direct"
}

export interface SeenUser {
  id: string;
  displayName?: string;
  avatarUrl?: string | null;
}

export interface LastMessage {
  id: string;
  content: string;
  createdAt: string;
  sender: {
    id: string;
    displayName: string;
    avatarUrl?: string | null;
  };
}

export interface ConversationResponse {
  conversations: Conversation[];
}

export interface Message {
  id: string;
  conversationId: string;
  senderId: string;
  content: string | null;
  imgUrl?: string | null;
  updatedAt?: string | null;
  createdAt: string;
  isOwn?: boolean;
}

