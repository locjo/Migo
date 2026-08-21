import { SidebarInset } from "../ui/sidebar"




export const ChatWelcomeScreen = () => {
  return (
    <SidebarInset className="flex w-full h-full bg-transparent">

        <div className="flex bg-primary-foreground rounded-2xl flex-1 items-center justify-center">
            <div className="text-center">
                <div className="size-24 mx-auto mb-6 rounded-full flex items-center justify-center shadow-glow pulse-ring">
                    <span className="text-3xl">
                        <img src="/logo.svg" alt="Logo" />
                    </span>
                </div>
                <h2 className="text-2xl font-bold mb-2 bg-gradient-chat bg-clip-text text-transparent">Chào mừng bạn đến với Migo</h2>
                <p className="text-muted-foreground">Chọn 1 cuộc hội thoại để bắt đầu chat</p>
            </div>
        </div>

    </SidebarInset>
  )
}

