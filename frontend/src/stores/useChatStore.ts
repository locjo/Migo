import { chatService } from "@/service/chatService";
import type { ChatState } from "@/types/store";
import type { Conversation, Message } from "@/types/chat";
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
            : data?.conversations || [];
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
            cursorToSend,
          );

          const currentUserId = user?._id || user?.id;
          const processed = fetched.map((m: any) => ({
            ...m,
            isOwn:
              (m.senderId?._id || m.senderId?.id || m.senderId) ===
              currentUserId,
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

      addMessage: (conversationId: string, newMessage: Message) => {
        const { user } = useAuthStore.getState();
        const currentUserId =  user?.id;

        const formattedMsg: Message = {
          ...newMessage,
          isOwn:
            newMessage.senderId === currentUserId,
        };

        set((state) => {
          const prevConvoMessages = state.messages[conversationId] || {
            items: [],
            hasMore: false,
            nextCursor: null,
          };

          const currentItems = prevConvoMessages.items || [];
          const isExisted = currentItems.some((m) => m.id === newMessage.id);
          const updatedItems = isExisted
            ? currentItems
            : [...currentItems, formattedMsg];

          // Ép kiểu Conversation cho từng phần tử khi map
          const updatedConversations: Conversation[] = state.conversations.map(
            (c) => {
              if (c.id === conversationId) {
                return {
                  ...c,
                  lastMessage: newMessage.content ?? "",
                  updatedAt: newMessage.createdAt,
                };
              }
              return c;
            },
          );

          return {
            conversations: updatedConversations,
            messages: {
              ...state.messages,
              [conversationId]: {
                ...prevConvoMessages,
                items: updatedItems,
                hasMore: prevConvoMessages.hasMore ?? false,
              },
            },
          };
        });
      },

      sendDirectMessage: async (recipientId, content, imgUrl) => {
        try {
          const { activeConversationId } = get();
          await chatService.sendDirectMessage(
            recipientId,
            content,
            imgUrl,
            activeConversationId || undefined,
          );

          set((state) => ({
            conversations: state.conversations.map((c) =>
              c.id === activeConversationId ? { ...c, seenBy: [] } : c,
            ),
          }));
        } catch (error) {
          console.error("Lỗi xảy ra khi gửi directMessage", error);
        }
      },

      sendGroupMessage: async (conversationId, content, imgUrl) => {
        try {
          await chatService.sendGroupMessage(conversationId, content, imgUrl);
          set((state) => ({
            conversations: state.conversations.map((c) =>
              c.id === get().activeConversationId ? { ...c, seenBy: [] } : c,
            ),
          }));
        } catch (error) {
          console.log("Lỗi xảy ra khi gửi groupMessage", error);
        }
      },
    }),
    {
      name: "chat-storage",
      partialize: (state) => ({ conversations: state.conversations }),
    },
  ),
);
