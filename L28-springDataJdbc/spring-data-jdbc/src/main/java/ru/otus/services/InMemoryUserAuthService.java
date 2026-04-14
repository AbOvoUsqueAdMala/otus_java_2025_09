package ru.otus.services;

import java.util.Map;

public class InMemoryUserAuthService implements UserAuthService {
    private static final String ADMIN_LOGIN = "admin";
    private static final String ADMIN_PASSWORD = "admin";

    private final Map<String, String> users = Map.of(ADMIN_LOGIN, ADMIN_PASSWORD);

    @Override
    public boolean authenticate(String login, String password) {
        return password != null && password.equals(users.get(login));
    }
}
