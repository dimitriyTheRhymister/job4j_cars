package ru.job4j.cars.filter;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Component
@Order(1)  // Выполняется первым
public class AuthorizationFilter extends HttpFilter {

    @Override
    protected void doFilter(HttpServletRequest request,
                            HttpServletResponse response,
                            FilterChain chain)
            throws IOException, ServletException {

        String uri = request.getRequestURI();

        // Страницы, доступные без авторизации
        if (isAlwaysPermitted(uri)) {
            chain.doFilter(request, response);
            return;
        }

        // Проверяем авторизацию для остальных страниц
        HttpSession session = request.getSession(false);
        boolean userLoggedIn = session != null && session.getAttribute("user") != null;

        if (!userLoggedIn) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean isAlwaysPermitted(String uri) {
        // Статические ресурсы
        if (uri.startsWith("/auth/")
                || uri.startsWith("/css/")
                || uri.startsWith("/js/")
                || uri.startsWith("/images/")
                || uri.startsWith("/uploads/")) {
            return true;
        }

        // Главная страница
        if (uri.equals("/") || uri.equals("/index") || uri.equals("/favicon.ico")) {
            return true;
        }

        // Просмотр конкретного объявления (например: /posts/7)
        if (uri.matches("/posts/\\d+")) {
            return true;
        }

        // H2 консоль (для разработки)
        if (uri.equals("/h2-console") || uri.startsWith("/h2-console/")) {
            return true;
        }

        return false;
    }
}