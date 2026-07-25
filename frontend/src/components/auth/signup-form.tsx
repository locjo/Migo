import { cn } from "@/lib/utils"
import { Button } from "@/components/ui/button"
import { Card, CardContent } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "../ui/label"
import {z} from "zod";
import {useForm} from "react-hook-form";
import {zodResolver} from "@hookform/resolvers/zod";
import { userAuthStore } from "@/stores/useAuthStore"
import { useNavigate } from "react-router-dom"


const signUpSchema = z.object({
  firstName: z.string().min(1, "Vui lòng nhập tên của bạn" ),
  lastName: z.string().min(1,  "Vui lòng nhập họ của bạn" ),
  username: z.string().min(3,  "Tên đăng nhập phải có ít nhất 3 ký tự" ),
  email: z.email( "Vui lòng nhập email hợp lệ" ),
  password: z.string().min(6,  "Mật khẩu phải có ít nhất 6 ký tự" ),
});

type SignUpFormValues = z.infer<typeof signUpSchema>;

export function SignupForm({
  className,
  ...props
}: React.ComponentProps<"div">) {
  const {signUp} = userAuthStore();
  const navigate = useNavigate();
  const {register, handleSubmit, formState: {errors, isSubmitting}} = useForm<SignUpFormValues>({
    resolver: zodResolver(signUpSchema)
  });

  const onSubmit = async (data: SignUpFormValues) => {
    const {firstName, lastName, username, email, password} = data;
    // goi backend de signup
    await signUp(username, password, email, firstName, lastName);

    navigate("/signin");
  };
  return (
    <div className={cn("flex flex-col gap-6", className)} {...props}>
      <Card className="overflow-hidden p-0 border-border">
        <CardContent className="grid p-0 md:grid-cols-2">
          <form className="p-6 md:p-8" onSubmit={handleSubmit(onSubmit)}>
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
                  <Input type="text" id="lastName" {...register("lastName")} />
                  {errors.lastName && (
                    <p className="text-red-500 text-sm">
                      {errors.lastName.message}
                    </p>
                  )}
                </div>     
                <div className="space-y-2">
                  <Label htmlFor="firstName" className="block text-sm">
                    Tên
                  </Label>
                  <Input type="text" id="firstName" {...register("firstName")} />
                  {errors.firstName && (
                    <p className="text-red-500 text-sm">
                      {errors.firstName.message}
                    </p>
                  )}  
                </div>
              </div>
              {/* username */}
              <div className="flex flex-col gap-3">
                <div className="space-y-2">
                  <Label htmlFor="username" className="block text-sm">
                    Tên đăng nhập
                  </Label>
                  <Input type="text" id="username" {...register("username")} />
                  {errors.username && (
                    <p className="text-red-500 text-sm">
                      {errors.username.message}
                    </p>
                  )}
                </div>
              </div>
              {/* email */}
              <div className="flex flex-col gap-3">
                <div className="space-y-2">
                  <Label htmlFor="email" className="block text-sm">
                    Email
                  </Label>
                  <Input type="email" id="email" {...register("email")} />
                  {errors.email && (
                    <p className="text-red-500 text-sm">
                      {errors.email.message}
                    </p>
                  )}
                </div>
              </div>
              {/* password */}
              <div className="flex flex-col gap-3">
                <div className="space-y-2">
                  <Label htmlFor="password" className="block text-sm">
                    Mật khẩu
                  </Label>
                  <Input type="password" id="password" {...register("password")} />
                  {errors.password && (
                    <p className="text-red-500 text-sm">
                      {errors.password.message}
                    </p>
                  )}
                </div>
              </div>    

              {/* button */}
              <Button type="submit" className="w-full" disabled={isSubmitting}>
                Tạo tài khoản
              </Button>

              <div className="text-center text-sm">
                Bạn đã có tài khoản?{" "}
                <a href="/login" className="underline underline-offset-4">
                  Đăng nhập
                </a>
              </div>
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
      <div className="text-xs text-balancepx-6 text-center *:[a]:hover:text-primary text-sm text-muted-foreground *:[a]:underline *:[a]:underline-offset-4" >
        Bằng cách tiếp tục bạn đồng ý với <a href="#">Chính sách dịch vụ</a>{" "}
        và <a href="#">Điều khoản bảo mật</a> của chúng tôi.
      </div>
    </div>
  )
}
