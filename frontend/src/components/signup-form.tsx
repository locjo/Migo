import { cn } from "@/lib/utils"
import { Button } from "@/components/ui/button"
import { Card, CardContent } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "./ui/label"

export function SignupForm({
  className,
  ...props
}: React.ComponentProps<"div">) {
  return (
    <div className={cn("flex flex-col gap-6", className)} {...props}>
      <Card className="overflow-hidden p-0">
        <CardContent className="grid p-0 md:grid-cols-2">
          <form className="p-6 md:p-8">
            <div className="flex flex-col gap-6">
              {/* header -logo */}  
                <div className="flex flex-col items-center text-center gap-2">
                  <a href="/" className="mx-auto block w-fit text-center">
                    <img src="/logo.svg" alt="Logo" />
                  </a>
                  <h1 className="text-2xl font-bold">Tạo tài khoản Migo</h1>
                  <p className="text-muted-foreground text-balance">
                    Chào mừng bạn đến với Migo! Hãy tạo tài khoản để bắt đầu
                  </p>
                </div>
              {/* họ tên */}
              <div className="flex flex-col-2 gap-3">
                <div className="space-y-2">
                  <Label htmlFor="lastName" className="block text-sm">
                    Họ
                  </Label>
                  <Input type="text" id="lastname"></Input>
                </div>
              </div>

              {/* username */}

              {/* email */}

              {/* password */}

              {/* confirm password */}

              {/* button */}
            </div>
          </form>
          <div className="relative hidden bg-muted md:block">
            <img
              src="/placeholderSignUp.png"
              alt="Image"
              className="absolute top-1/2 -translate-y-1/2 object-cover"
            />
          </div>
        </CardContent>
      </Card>
      <div className="px-6 text-center">
        Bằng cách tiếp tục bạn đồng ý với <a href="#">Chính sách dịch vụ</a>{" "}
        và <a href="#">Điều khoản bảo mật</a> của chúng tôi.
      </div>
    </div>
  )
}
