import api from "@/lib/axios";
import type { ConversationResponse } from "@/types/chat";
import type { Message } from "@/types/chat";

export interface FetchMessageProps {
  messages: Message[];
  cursor?: string | null;
}

const pageLimit = 50;

export const chatService = {
  async fetchConversations(): Promise<ConversationResponse> {
    const res = await api.get("/conversations/");
    return res.data.result || res.data;
  },

  async fetchMessages(id: string, cursor?: string): Promise<FetchMessageProps> {
    const res = await api.get(`/messages/${id}/messages`, {
      params: {
        limit: pageLimit,
        cursor: cursor || undefined,
      },
    });

    const rawData = res.data;
    const messageList = Array.isArray(rawData)
      ? rawData
      : (rawData?.messages || rawData?.result || rawData?.content || []);

    return {
      messages: messageList,
      cursor: rawData?.nextCursor ?? null,
    };
  },
};
