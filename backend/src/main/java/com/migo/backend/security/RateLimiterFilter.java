package com.migo.backend.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimiterFilter extends OncePerRequestFilter {

    // Bộ nhớ lưu trữ Bucket cho từng IP (Dùng ConcurrentHashMap để an toàn khi nhiều request gọi cùng lúc)
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    // Hàm tạo Bucket cấu hình luật (Ví dụ: Tối đa 10 request / 1 phút)
    private Bucket createNewBucket() {
        Bandwidth limit = Bandwidth.classic(10, Refill.greedy(10, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Chỉ áp dụng Rate Limit cho các route nhạy cảm (như Login/Register) để tránh bị Brute Force
        String path = request.getRequestURI();
        if (path.startsWith("/api/auth/login") || path.startsWith("/api/auth/register")) {

            // Lấy địa chỉ IP của Client
            String clientIp = getClientIP(request);

            // Tìm hoặc tạo mới Bucket cho IP này
            Bucket bucket = buckets.computeIfAbsent(clientIp, k -> createNewBucket());

            // Thử lấy 1 token từ thùng
            if (bucket.tryConsume(1)) {
                // Nếu còn token -> Cho phép đi tiếp vào Controller
                filterChain.doFilter(request, response);
            } else {
                // Nếu hết token -> Trả về lỗi 429 Too Many Requests ngay lập tức
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("""
                    {
                        "code": 429,
                        "message": "Bạn đã gửi quá nhiều yêu cầu! Vui lòng thử lại sau 1 phút."
                    }
                """);
            }
        } else {
            // Các API khác tạm thời không giới hạn
            filterChain.doFilter(request, response);
        }
    }

    // Hàm bổ trợ lấy đúng IP người dùng (xử lý trường hợp chạy sau Proxy/Nginx/Cloudflare)
    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}