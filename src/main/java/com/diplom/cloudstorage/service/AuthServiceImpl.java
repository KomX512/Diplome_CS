package com.diplom.cloudstorage.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.diplom.cloudstorage.entity.AuthToken;
import com.diplom.cloudstorage.entity.User;
import com.diplom.cloudstorage.exception.UnauthorizedException;
import com.diplom.cloudstorage.repository.AuthTokenRepository;
import com.diplom.cloudstorage.repository.UserRepository;
import com.diplom.cloudstorage.util.PasswordUtil;
import com.diplom.cloudstorage.util.TokenUtil;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final AuthTokenRepository authTokenRepository;

    @Override
    @Transactional
    public String login(String login, String password) {

        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new UnauthorizedException(
                        Map.of("login", new String[]{"Пользователь не найден"})
                ));

        if (!user.getPassword().equals(PasswordUtil.hash(password))) {
            throw new UnauthorizedException(
                    Map.of("password", new String[]{"Неверный пароль"})
            );
        }

        authTokenRepository.deleteByUser(user);

        String token = TokenUtil.generateToken();
        AuthToken authToken = new AuthToken();
        authToken.setToken(token);
        authToken.setUser(user);
        authTokenRepository.save(authToken);

        return token;
    }

    @Override
    public User getUserByToken(String token) {
        return authTokenRepository.findByToken(token)
                .map(AuthToken::getUser)
                .orElseThrow(() -> new UnauthorizedException(
                        Map.of("token", new String[]{"Неверный токен"})
                ));
    }

    @Override
    @Transactional
    public void logout(String token) {
        authTokenRepository.deleteByToken(token);
    }
}
