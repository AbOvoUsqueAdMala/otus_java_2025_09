package ru.otus.servlet;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import ru.otus.crm.model.Address;
import ru.otus.crm.model.Client;
import ru.otus.crm.model.Phone;
import ru.otus.crm.service.DBServiceClient;
import ru.otus.services.TemplateProcessor;

public class ClientsServlet extends HttpServlet {
    private static final String CLIENTS_PAGE_TEMPLATE = "clients.html";

    private final transient TemplateProcessor templateProcessor;
    private final transient DBServiceClient dbServiceClient;

    public ClientsServlet(TemplateProcessor templateProcessor, DBServiceClient dbServiceClient) {
        this.templateProcessor = templateProcessor;
        this.dbServiceClient = dbServiceClient;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, Object> params = new HashMap<>();
        params.put("clients", dbServiceClient.findAll());
        params.put("errorMessage", emptyToNull(request.getParameter("error")));
        params.put("successMessage", request.getParameter("saved") == null ? null : "Client has been saved");

        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().println(templateProcessor.getPage(CLIENTS_PAGE_TEMPLATE, params));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            var client = new Client(
                    null,
                    extractRequiredParameter(request, "name"),
                    new Address(null, extractRequiredParameter(request, "street")),
                    Arrays.stream(extractRequiredParameter(request, "phones").split("[,\\r\\n]+"))
                            .map(String::trim)
                            .filter(phone -> !phone.isEmpty())
                            .map(phone -> new Phone(null, phone))
                            .toList());

            if (client.getPhones().isEmpty()) {
                throw new IllegalArgumentException("At least one phone is required");
            }

            dbServiceClient.saveClient(client);
            response.sendRedirect("/clients?saved=true");
        } catch (IllegalArgumentException e) {
            response.sendRedirect("/clients?error=" + encode(e.getMessage()));
        }
    }

    private static String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private static String extractRequiredParameter(HttpServletRequest request, String parameterName) {
        var value = request.getParameter(parameterName);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Fields name, street and phones are required");
        }
        return value.trim();
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
