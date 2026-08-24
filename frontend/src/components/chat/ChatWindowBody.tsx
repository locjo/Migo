import { useEffect } from 'react';
import { useChatStore } from '@/stores/useChatStore';
import { useSocketStore } from '@/stores/useSocketStore';
import { ChatWelcomeScreen } from './ChatWelcomeScreen';
import MessageItem from './MessageItem';

export const ChatWindowBody = () => {
  const { activeConversationId, conversations, messages: allMessages, addMessage } = useChatStore();
  const { stompClient } = useSocketStore();

  const messages = allMessages[activeConversationId!]?.items ?? [];
  const selectedConvo = conversations.find((c) => c.id === activeConversationId);

  // LẮNG NGHE TIN NHẮN REALTIME CHO PHÒNG ĐANG MỞ
  useEffect(() => {
    if (!stompClient || !stompClient.connected || !activeConversationId) return;

    // Đăng ký nhận tin nhắn từ topic của conversation hiện tại
    const subscription = stompClient.subscribe(
      `/topic/conversation/${activeConversationId}`,
      (payload) => {
        const newMessage = JSON.parse(payload.body);
        console.log("Tin nhắn Realtime nhận được:", newMessage);

        // Gọi action thêm tin nhắn vào store (hoặc cập nhật state tương ứng của bạn)
        if (addMessage) {
          addMessage(activeConversationId, newMessage);
        }
      }
    );

    return () => {
      // Hủy subscribe khi chuyển sang phòng khác
      subscription.unsubscribe();
    };
  }, [stompClient, activeConversationId, addMessage]);

  if (!selectedConvo) {
    return <ChatWelcomeScreen />;
  }

  if (!messages?.length) {
    return (
      <div className='flex h-full items-center justify-center text-muted-foreground'>
        Chưa có tin nhắn nào trong cuộc trò chuyện này
      </div>
    );
  }

  return (
    <div className='p-4 bg-primary-foreground h-full flex flex-col overflow-hidden'>
      <div className="flex flex-col overflow-y-auto overflow-x-hidden beautiful-scrollbar">
        {messages.map((message, index) => (
          <MessageItem 
            key={message.id ?? index}
            message={message}
            index={index}
            messages={messages}
            selectedConvo={selectedConvo}
            lastMessageStatus='delivered'
          />
        ))}
      </div>
    </div>
  );
};