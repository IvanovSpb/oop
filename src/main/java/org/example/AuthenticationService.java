package org.example;

import java.util.HashMap;
import java.util.Map;

public class AuthenticationService {
    private Map<String, User> users = new HashMap<>();

    public void registerUser(User user) {
        users.put(user.getLogin(), user);
    }

    public User authenticateUser(String login, String password) {
        User user = users.get(login);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }
}