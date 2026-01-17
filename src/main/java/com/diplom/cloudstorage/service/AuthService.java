package com.diplom.cloudstorage.service;

import com.diplom.cloudstorage.entity.User;

public interface AuthService {
    String login(String login, String password);
    void logout(String token);
    User getUserByToken(String token);
}
