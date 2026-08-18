import type { Conversation } from "@/types/chat";
import ChatCard from "./ChatCard";
import { useAuthStore } from "@/stores/useAuthStore";
import { cn } from "@/lib/utils";
import { useChatStore } from "@/stores/useChatStore";
import UserAvatar from "./UserAvatar";

const DirectMessageCard = ({ convo }: { convo: Conversation }) => {
  const { user } = useAuthStore();
  const { activeConversationId, setActiveConversation, messages } =
    useChatStore();


  if (!user) return null;
  const currentUsername = user?.username || "";
  const currentUserId = user?._id || user?.id || "";

  const otherUser = convo.participants.find((p) => {
    // Lấy ID và Username của người tham gia
    const pId = p.userId || p._id || p.id;
    const pUsername = p.username;

    // Loại bỏ bản thân bằng ID (nếu có) hoặc bằng username
    if (currentUserId && pId) {
      return pId !== currentUserId;
    }
    return pUsername && pUsername !== currentUsername;
  });
  if (!otherUser) return null;
  const unreadCount = convo.unreadCount?.[user._id || user.id] ?? 0;
  const lastMessage = convo.lastMessage?.content ?? "Chưa có tin nhắn nào";

  // 2. Chuyển đổi ngày an toàn (nếu có tin nhắn mới tạo Date, không có thì để undefined)
  const timestamp = convo.lastMessage?.createdAt
    ? new Date(convo.lastMessage.createdAt)
    : undefined;

  const handleSelectConversation = async (id: string) => {
    setActiveConversation(id);
    if (!messages[id]) {
      // todo: fetch messages
    }
  };
  return (
    <ChatCard
      convoId={convo._id || convo.id || ""}
      name={otherUser?.displayName || otherUser?.username || "Người dùng"}
      timestamp={timestamp}
      isActive={activeConversationId === (convo._id || convo.id)}
      onSelect={handleSelectConversation}
      unreadCount={unreadCount}
      leftSection={
        <UserAvatar
          type="sidebar"
          name={otherUser.displayName ?? ""}
          avatarUrl={otherUser?.avatarUrl}
        />
      }
      subtitle={
        <p
          className={cn(
            "truncate text-xs text-muted-foreground",
            unreadCount > 0 && "font-medium text-foreground",
          )}
        >
          {lastMessage}
        </p>
      }
    />
  );
};

export default DirectMessageCard;
