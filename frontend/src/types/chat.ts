// src/types/chat.ts

export interface Participant {
  userId: string | undefined;
  _id?: string;
  id?: string;
  displayName?: string;
  username?: string;
  avatarUrl?: string;
  avatar?: string;
}

export interface Conversation {
  name?: string;
  _id?: string;
  id?: string;
  unreadCount?: Record<string, number>;
  lastMessage?: {
    content?: string;
    createdAt?: string;
  } | null;
  participants: Participant[];
  type: "Group" | "Direct"
}

export interface SeenUser {
  _id: string;
  displayName?: string;
  avatarUrl?: string | null;
}

export interface LastMessage {
  _id: string;
  content: string;
  createdAt: string;
  sender: {
    _id: string;
    displayName: string;
    avatarUrl?: string | null;
  };
}

export interface ConversationResponse {
  conversations: Conversation[];
}

export interface Message {
  _id: string;
  conversationId: string;
  senderId: string;
  content: string | null;
  imgUrl?: string | null;
  updatedAt?: string | null;
  createdAt: string;
  isOwn?: boolean;
}
