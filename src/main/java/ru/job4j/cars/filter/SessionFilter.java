package ru.job4j.cars.filter;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import ru.job4j.cars.model.User;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Component
@Order(2)  // Порядок выполнения
public class SessionFilter extends HttpFilter {

    @Override
    protected void doFilter(HttpServletRequest request,
                            HttpServletResponse response,
                            FilterChain chain)
            throws IOException, ServletException {

        HttpSession session = request.getSession(false);

        // Автоматически добавляем пользователя в атрибуты запроса
        setUserToRequest(session, request);

        chain.doFilter(request, response);
    }

    private void setUserToRequest(HttpSession session, HttpServletRequest request) {
        User userFromSession = null;
        if (session != null) {
            Object attr = session.getAttribute("user");
            if (attr instanceof User) {
                userFromSession = (User) attr;
            }
        }

        // Кладем пользователя в атрибуты запроса
        // В Thymeleaf можно будет обращаться через ${user}
        request.setAttribute("user", userFromSession);
    }
}