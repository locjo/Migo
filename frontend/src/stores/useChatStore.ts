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
          // Nếu Backend trả về mảng trực tiếp hoặc bọc trong object
          const conversations = Array.isArray(data)
            ? data
            : data?.conversations || [];
          set({ conversations,convoLoading: false });
        } catch (error) {
          console.error("Lỗi xảy ra khi fetch conversations", error);
          set({ convoLoading: false });
        }
      },
      fetchMessages: async (conversationId) => {
        const {activeConversationId, messages} = get();
        const {user} = useAuthStore.getState();
        const convoId = conversationId ?? activeConversationId;
        if(!convoId) return;

        const current = messages?.[convoId];
        const nextCursor = current?.nextCursor === undefined ? "" : current?.nextCursor;
        if(nextCursor === null) return;

        set({messageLoading: true});
        try {
          const {messages: fetched, cursor} = await chatService.fetchMessages(convoId, nextCursor);
          set({messageLoading: false});
        } catch (error) {
          console.error("Lỗi xảy ra khi fetch messages", error);
          set({messageLoading: false});
        }
      }

    }),
    {
      name: "chat-storage",
      partialize: (state) => ({ conversations: state.conversations }),
    },
  ),
);
