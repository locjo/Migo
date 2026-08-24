import type { Conversation } from '@/types/chat'
import ChatCard from './ChatCard';
import { useAuthStore } from '@/stores/useAuthStore';
import { useChatStore } from '@/stores/useChatStore';
import UnreadCountBadge from './UnreadCountBadge';
import GroupChatAvatar from './GroupChatAvatar';

const GroupChatCard = ({convo }: { convo: Conversation}) => {
  const { user } = useAuthStore();
  const { activeConversationId, setActiveConversation, messages, fetchMessages } = useChatStore();

  if(!user) return null;

  const unreadCount = convo.unreadCount?.[user.id] ?? 0;
  const name = convo.name ?? "";
  const handleSelectConversation = async (id: string) => {
    setActiveConversation(id);
    if(!messages[id]){
      await fetchMessages();
    }
  };
  return (
    <ChatCard 
      convoId={convo.id}
      name={name}
      isActive={activeConversationId === convo.id}
      unreadCount={unreadCount}
      onSelect={handleSelectConversation}
      leftSection ={<>
        {unreadCount > 0 && <UnreadCountBadge unreadCount={unreadCount}/>}
        <GroupChatAvatar participants={convo.participants} type="chat"/>
      </>}
      subtitle={
        <p className="text-sm truncate text-muted-foreground">{convo.participants.length} thành viên</p>
      }
      />
  )
}

export default GroupChatCard