package ru.otus.server;

import jakarta.servlet.DispatcherType;
import java.util.EnumSet;
import org.eclipse.jetty.ee10.servlet.FilterHolder;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;
import ru.otus.crm.service.DBServiceClient;
import ru.otus.services.TemplateProcessor;
import ru.otus.services.UserAuthService;
import ru.otus.servlet.AuthorizationFilter;
import ru.otus.servlet.ClientsServlet;
import ru.otus.servlet.LoginServlet;

public class ClientsWebServer {
    private final UserAuthService userAuthService;
    private final DBServiceClient dbServiceClient;
    private final TemplateProcessor templateProcessor;
    private final Server server;

    public ClientsWebServer(
            int port,
            UserAuthService userAuthService,
            DBServiceClient dbServiceClient,
            TemplateProcessor templateProcessor) {
        this.userAuthService = userAuthService;
        this.dbServiceClient = dbServiceClient;
        this.templateProcessor = templateProcessor;
        this.server = new Server(port);
    }

    public void start() throws Exception {
        if (server.getHandler() == null) {
            initContext();
        }
        server.start();
    }

    public void join() throws InterruptedException {
        server.join();
    }

    public void stop() throws Exception {
        if (server.isStarted() || server.isStarting() || server.isStopping()) {
            server.stop();
        }
    }

    private void initContext() {
        var servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        servletContextHandler.addServlet(new ServletHolder(new LoginServlet(templateProcessor, userAuthService)), "/");
        servletContextHandler.addServlet(
                new ServletHolder(new LoginServlet(templateProcessor, userAuthService)), "/login");
        servletContextHandler.addServlet(
                new ServletHolder(new ClientsServlet(templateProcessor, dbServiceClient)), "/clients");

        var authFilter = new AuthorizationFilter();
        var dispatcherTypes = EnumSet.of(DispatcherType.REQUEST);
        servletContextHandler.addFilter(new FilterHolder(authFilter), "/clients", dispatcherTypes);

        server.setHandler(servletContextHandler);
    }
}
