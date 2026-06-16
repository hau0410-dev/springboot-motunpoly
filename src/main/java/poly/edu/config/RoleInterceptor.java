package poly.edu.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import poly.edu.entity.User;

/**
 * Chặn truy cập dựa trên role:
 *  - /admin/**  → chỉ ADMIN
 *  - /shipper/** → chỉ SHIPPER
 *  - /profile, /orders, /reviews, /promotions → phải đăng nhập (bất kỳ role)
 *  - ADMIN / SHIPPER không được vào trang khách hàng
 */
@Component
public class RoleInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        String uri = request.getRequestURI();

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        String role = (user != null) ? user.getRole().toUpperCase() : null;

        // ===== /admin/** → chỉ ADMIN =====
        if (uri.startsWith("/admin")) {
            if (!"ADMIN".equals(role)) {
                response.sendRedirect("/login");
                return false;
            }
            return true;
        }

        // ===== /shipper/** → chỉ SHIPPER =====
        if (uri.startsWith("/shipper")) {
            if (!"SHIPPER".equals(role)) {
                response.sendRedirect("/login");
                return false;
            }
            return true;
        }

        // ===== Trang cần đăng nhập (USER) =====
        boolean needLogin =
            uri.startsWith("/profile")
            || uri.startsWith("/orders")
            || uri.startsWith("/reviews")
            || uri.startsWith("/promotions")
            || uri.startsWith("/cart/checkout")
            || uri.startsWith("/cart/pay")
            || uri.startsWith("/cart/buynow")
            || uri.startsWith("/change-password");

        if (needLogin && user == null) {
            response.sendRedirect("/login");
            return false;
        }

        // ===== ADMIN / SHIPPER không được vào trang USER =====
        if (user != null && ("ADMIN".equals(role) || "SHIPPER".equals(role))) {
            boolean userOnlyPage =
                uri.startsWith("/profile")
                || uri.startsWith("/orders")
                || uri.startsWith("/reviews")
                || uri.startsWith("/promotions")
                || uri.equals("/");

            if (userOnlyPage) {
                if ("ADMIN".equals(role)) {
                    response.sendRedirect("/admin");
                } else {
                    response.sendRedirect("/shipper/index");
                }
                return false;
            }
        }

        return true;
    }
}