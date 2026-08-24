import { useAuthStore } from "@/stores/useAuthStore";
import type { Conversation } from "@/types/chat";
import React, { useState } from "react";
import { Button } from "../ui/button";
import { ImagePlus, SendIcon } from "lucide-react";
import { Input } from "../ui/input";
import EmojiPicker from "./EmojiPicker";
import { useChatStore } from "@/stores/useChatStore";
import { toast } from "sonner";

export const MessageInput = ({
  selectedConvo,
}: {
  selectedConvo: Conversation;
}) => {
  const { user } = useAuthStore();
  const [value, setValue] = useState("");
  const { sendDirectMessage, sendGroupMessage } = useChatStore();

  const sendMessage = async () => {
    if (!value.trim()) return;

    try {
      const participants = selectedConvo.participants;
      const otherUser = participants.filter(
        (p) => (p.userId || p.id) !== user?.id,
      )[0];

      if (selectedConvo.type === "Direct") {
        await sendDirectMessage((otherUser.userId || otherUser.id)!, value);
      } else {
        await sendGroupMessage(selectedConvo.id!, value);
      }
    } catch (error) {
      console.log(error);
      toast.error("Loi xay ra khi gui tin nhan. Ban hay thu lai");
    } finally {
      setValue("");
    }
  };
  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === "Enter") {
      e.preventDefault();
      sendMessage();
    }
  };

  return (
    <div className="flex items-center gap-2 p-3 min-h-[56px]">
      <Button variant="ghost" className="hover:bg-primary/10">
        <ImagePlus className="size-4" />
      </Button>

      <div className="flex-1 relative">
        {/* 1. Đóng thẻ Input ngay tại đây (tự đóng />) */}
        <Input
          onKeyDown={handleKeyPress}
          value={value}
          onChange={(e) => setValue(e.target.value)}
          placeholder="Soạn tin nhắn..."
          className="pr-20 h-9 bg-white border-border/50 focus:border-primary"
        />

        {/* 2. Đặt nút bấm nằm đè lên Input nhờ class absolute */}
        <div className="absolute right-2 top-1/2 -translate-y-1/2">
          <EmojiPicker
            onChange={(emoji: string) => setValue(`${value}${emoji}`)}
          />
        </div>
      </div>
      <Button
        onClick={sendMessage}
        className="bg-gradient-chat hover:shadow glow-transition-smooth hover:scale-105"
        disabled={!value.trim()}
      >
        {/* Đặt Icon gửi hoặc Emoji vào đây */}
        <SendIcon className="size-4" />
      </Button>
    </div>
  );
};
