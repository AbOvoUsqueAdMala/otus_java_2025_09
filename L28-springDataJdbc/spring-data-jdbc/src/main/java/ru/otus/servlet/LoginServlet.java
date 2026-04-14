package ru.otus.servlet;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import ru.otus.services.TemplateProcessor;
import ru.otus.services.UserAuthService;

public class LoginServlet extends HttpServlet {
    private static final String LOGIN_PAGE_TEMPLATE = "login.html";
    private static final String PARAM_LOGIN = "login";
    private static final String PARAM_PASSWORD = "password";
    private static final String DEFAULT_LOGIN = "admin";
    private static final int MAX_INACTIVE_INTERVAL_SECONDS = 300;

    private final transient TemplateProcessor templateProcessor;
    private final transient UserAuthService userAuthService;

    public LoginServlet(TemplateProcessor templateProcessor, UserAuthService userAuthService) {
        this.templateProcessor = templateProcessor;
        this.userAuthService = userAuthService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute(AuthorizationFilter.AUTH_SESSION_ATTR_NAME) != null) {
            response.sendRedirect("/clients");
            return;
        }

        renderPage(response, request.getParameter(PARAM_LOGIN), null);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        var login = request.getParameter(PARAM_LOGIN);
        var password = request.getParameter(PARAM_PASSWORD);

        if (userAuthService.authenticate(login, password)) {
            HttpSession session = request.getSession();
            session.setAttribute(AuthorizationFilter.AUTH_SESSION_ATTR_NAME, login);
            session.setMaxInactiveInterval(MAX_INACTIVE_INTERVAL_SECONDS);
            response.sendRedirect("/clients");
            return;
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        renderPage(response, login, "Invalid credentials");
    }

    private void renderPage(HttpServletResponse response, String login, String errorMessage) throws IOException {
        Map<String, Object> params = new HashMap<>();
        params.put(PARAM_LOGIN, isBlank(login) ? DEFAULT_LOGIN : login);
        params.put("errorMessage", errorMessage);

        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().println(templateProcessor.getPage(LOGIN_PAGE_TEMPLATE, params));
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
