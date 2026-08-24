import { useChatStore } from "@/stores/useChatStore";
import type { Conversation } from "@/types/chat";
import { SidebarTrigger } from "../ui/sidebar";
import { useAuthStore } from "@/stores/useAuthStore";
import { Separator } from "@base-ui/react";
import UserAvatar from "./UserAvatar";
import GroupChatAvatar from "./GroupChatAvatar";
import StatusBadge from "./StatusBadge";
import { useSocketStore } from "@/stores/useSocketStore";

export const ChatWindowHeader = ({ chat }: { chat?: Conversation }) => {
  const { conversations, activeConversationId } = useChatStore();
  const { user } = useAuthStore();
  const { onlineUser } = useSocketStore();

  let otherUser;
  chat = chat ?? conversations.find((c) => c.id === activeConversationId);
  if (!chat) {
    return (
      <header className="md:hidden sticky top-0 z-10 flex items-center gap-2 px-4 py-2 w-full">
        <SidebarTrigger className="-ml-1 text-foreground" />
      </header>
    );
  }

  if (chat.type === "Direct") {
    // Lấy ID tài khoản hiện tại

    // Tìm người có userId khác với mình
    otherUser =
      chat.participants.find((p) => p.username !== user?.username) ?? null;
  }
  const otherUserId = String(otherUser?.userId);

  const isOnline = otherUserId ? onlineUser.includes(otherUserId) : false;
  return (
    <header className="sticky top-0 z-10 px-4 py-2 flex items-center bg-background">
      <div className="flex items-center gap-2 w-full">
        <SidebarTrigger className="-ml-1 text-foreground" />
        <Separator
          orientation="vertical"
          className="mr-2 data-[orientation=vertical]:h-4"
        />

        <div className="p-2 w-full flex items-center gap-3">
          {/* avatar */}
          <div className="relative">
            {chat.type === "Direct" ? (
              <>
                <UserAvatar
                  type={"sidebar"}
                  name={otherUser?.displayName || "Migo"}
                  avatarUrl={otherUser?.avatarUrl || undefined}
                />
                {/* todo socketio */}
                <StatusBadge status={isOnline ? "online" : "offline"} />
              </>
            ) : (
              <GroupChatAvatar
                participants={chat.participants}
                type="sidebar"
              />
            )}
          </div>
          <h2 className="font-semibold text-foreground">
            {chat.type === "Direct" ? otherUser?.displayName : chat.name}
          </h2>
        </div>
      </div>
    </header>
  );
};
