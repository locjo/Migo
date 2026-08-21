import { useAuthStore } from "@/stores/useAuthStore";
import type { Conversation } from "@/types/chat";
import React, { useState } from "react";
import { Button } from "../ui/button";
import { ImagePlus, SendIcon } from "lucide-react";
import { Input } from "../ui/input";
import EmojiPicker from "./EmojiPicker";

export const MessageInput = ({selectedConvo}: {
  selectedConvo: Conversation;
}) => {
  const { user } = useAuthStore();
  const [value, setValue] = useState("");

  if (!user) return;

  return (
    <div className="flex items-center gap-2 p-3 min-h-[56px]">
      <Button variant="ghost" className="hover:bg-primary/10">
        <ImagePlus className="size-4" />
      </Button>

      <div className="flex-1 relative">
        {/* 1. Đóng thẻ Input ngay tại đây (tự đóng />) */}
        <Input
          value={value}
          onChange={(e) => setValue(e.target.value)}
          placeholder="Soạn tin nhắn..."
          className="pr-20 h-9 bg-white border-border/50 focus:border-primary"
        />

        {/* 2. Đặt nút bấm nằm đè lên Input nhờ class absolute */}
        <div className="absolute right-2 top-1/2 -translate-y-1/2">
          <Button
            type="submit"
            variant="ghost"
            size="icon"
            className="size-8 hover:bg-primary/10 transition-smooth"
          >
            <div><EmojiPicker onChange={(emoji:string) => setValue(`${value}${emoji}`)}/></div>
          </Button>
          
        </div>

      </div>
       <Button
              className="bg-gradient-chat hover:shadow glow-transition-smooth hover:scale-105"
              disabled={!value.trim()}
            >
              {/* Đặt Icon gửi hoặc Emoji vào đây */}
              <SendIcon className="size-4" />
            </Button>
    </div>
  );
};
