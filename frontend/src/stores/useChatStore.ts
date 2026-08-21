import { chatService } from "@/service/chatService";
import type { ChatState } from "@/types/store";
import { create } from "zustand";
import { persist } from "zustand/middleware";
import { useAuthStore } from "./useAuthStore";


export const useChatStore = create<ChatState>()(
  persist(
    (set, get) => ({
      conversations: [],
      messages: {},
      activeConversationId: null,
      convoLoading: false,
      messageLoading: false,

      setActiveConversation: (id) => {
        set({ activeConversationId: id });
      },

      reset: () => {
        set({
          conversations: [],
          messages: {},
          activeConversationId: null,
          convoLoading: false,
          messageLoading: false,
        });
      },

      fetchConversations: async () => {
        try {
          set({ convoLoading: true });
          const data = await chatService.fetchConversations();
          const conversations = Array.isArray(data)
            ? data
            : (data)?.conversations || [];
          set({ conversations, convoLoading: false });
        } catch (error) {
          console.error("Lỗi xảy ra khi fetch conversations", error);
          set({ convoLoading: false });
        }
      },

      fetchMessages: async (conversationId) => {
        const { activeConversationId, messages } = get();
        const { user } = useAuthStore.getState();
        const convoId = conversationId ?? activeConversationId;
        
        if (!convoId) return;

        const current = messages?.[convoId];
        if (current?.nextCursor === null) return;

        const cursorToSend = current?.nextCursor || undefined;

        set({ messageLoading: true });
        try {
          const { messages: fetched, cursor } = await chatService.fetchMessages(
            convoId,
            cursorToSend
          );

          const processed = fetched.map((m) => ({
            ...m,
            isOwn: m.senderId === user?._id, 
          }));

          set((state) => {
            const prev = state.messages[convoId]?.items ?? [];
            const merged =
              prev.length > 0 ? [...processed, ...prev] : processed;

            return {
              messages: {
                ...state.messages,
                [convoId]: { 
                  items: merged,
                  hasMore: !!cursor,
                  nextCursor: cursor ?? null,
                },
              },
            };
          });
        } catch (error) {
          console.error("Lỗi xảy ra khi fetch messages", error);
        } finally {
          set({ messageLoading: false });
        }
      },
    }),
    {
      name: "chat-storage",
      partialize: (state) => ({ conversations: state.conversations }),
    }
  )
);