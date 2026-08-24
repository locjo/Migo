import { create } from "zustand";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { useAuthStore } from "./useAuthStore";

// Cổng mặc định của Spring Boot (thay vì 8085 của Socket.IO cũ)
const socketUrl = import.meta.env.VITE_WS_URL || "http://localhost:8080/ws";

interface SocketState {
  stompClient: Client | null;
  onlineUser: string[];
  connectSocket: () => void;
  disconnectSocket: () => void;
}
 
export const useSocketStore = create<SocketState>((set, get) => ({
  stompClient: null,
  onlineUser: [],

  connectSocket: () => {
    const accessToken = useAuthStore.getState().accessToken;
    const existingClient = get().stompClient;

    // Nếu không có token hoặc client đã kết nối/đang kích hoạt thì bỏ qua
    if (!accessToken || existingClient?.active) return;

    // Khởi tạo STOMP Client
    const client = new Client({
      webSocketFactory: () => new SockJS(socketUrl),
      connectHeaders: {
        Authorization: `Bearer ${accessToken}`,
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,

      onConnect: () => {
        console.log("⚡ STOMP WebSocket đã kết nối thành công!");

        // Lắng nghe danh sách Online Users từ Backend WebSocketEventListener
        client.subscribe("/topic/online-users", (message) => {
          const userIds: string[] = JSON.parse(message.body);
          console.log("Danh sách online nhận được:", userIds);
          set({ onlineUser: userIds });
        });
      },

      onDisconnect: () => {
        console.log("❌ STOMP WebSocket đã ngắt kết nối");
        set({ onlineUser: [] });
      },

      onStompError: (frame) => {
        console.error("Broker lỗi:", frame.headers["message"]);
      },
    });

    client.activate();
    set({ stompClient: client });
  },

  disconnectSocket: () => {
    const client = get().stompClient;
    if (client) {
      client.deactivate();
      set({ stompClient: null, onlineUser: [] });
    }
  },
}));