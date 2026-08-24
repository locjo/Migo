import type { Conversation } from "@/types/chat";
import ChatCard from "./ChatCard";
import { useAuthStore } from "@/stores/useAuthStore";
import { cn } from "@/lib/utils";
import { useChatStore } from "@/stores/useChatStore";
import UserAvatar from "./UserAvatar";
import { useSocketStore } from "@/stores/useSocketStore";
import StatusBadge from "./StatusBadge";

const DirectMessageCard = ({ convo }: { convo: Conversation }) => {
  const { user } = useAuthStore();
  const { activeConversationId, setActiveConversation, messages, fetchMessages } =
    useChatStore();
    const {onlineUser} = useSocketStore();


  if (!user) return null;
  const currentUsername = user?.username || "";
  const currentUserId = user?.id || "";

  const otherUser = convo.participants.find((p) => {
    // Lấy ID và Username của người tham gia
    const pId = p.userId|| p.id;
    const pUsername = p.username;

    // Loại bỏ bản thân bằng ID (nếu có) hoặc bằng username
    if (currentUserId && pId) {
      return pId !== currentUserId;
    }
    return pUsername && pUsername !== currentUsername;
  });
  if (!otherUser) return null;
  const unreadCount = convo.unreadCount?.[user.id ] ?? 0;
  const lastMessage = convo.lastMessage ?? "Chưa có tin nhắn nào";



  const handleSelectConversation = async (id: string) => {
    setActiveConversation(id);
    if (!messages[id]) {
      await fetchMessages();
    }
    console.log("otherUser ID:", otherUser?.userId);
        console.log("Đang có trong onlineUser không:", onlineUser.includes(String( otherUser.userId))); 
  };
  return (
    <ChatCard
      convoId={convo.id || ""}
      name={otherUser?.displayName || otherUser?.username || "Người dùng"}
      isActive={activeConversationId === (convo.id)}
      onSelect={handleSelectConversation}
      unreadCount={unreadCount}
      leftSection={
        <>
        <UserAvatar
          type="sidebar"
          name={otherUser.displayName ?? ""}
          avatarUrl={otherUser?.avatarUrl}
        />
        
        <StatusBadge 
        status={
            (otherUser?.userId && onlineUser.includes(String(otherUser.userId)))
            ? "online" 
            : "offline"
        } 
      />
        </>
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
