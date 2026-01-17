package com.diplom.cloudstorage.service;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.diplom.cloudstorage.entity.AuthToken;
import com.diplom.cloudstorage.entity.User;
import com.diplom.cloudstorage.exception.UnauthorizedException;
import com.diplom.cloudstorage.repository.AuthTokenRepository;
import com.diplom.cloudstorage.repository.FileRepository;
import com.diplom.cloudstorage.repository.UserRepository;
import com.diplom.cloudstorage.util.PasswordUtil;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthServiceTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("user")
            .withPassword("password");

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private AuthTokenRepository authTokenRepository;

    private String testLogin;
    private final String TEST_PASSWORD = "password";

    @BeforeEach
    void setup() {
        authTokenRepository.deleteAll();
        fileRepository.deleteAll();
        userRepository.deleteAll();

        testLogin = "auth-" + UUID.randomUUID() + "@example.com";
        User user = new User(testLogin, PasswordUtil.hash(TEST_PASSWORD));
        userRepository.save(user);
    }

    @Test
    @Order(1)
    void loginGeneratesToken() {
        String token = authService.login(testLogin, TEST_PASSWORD);
        assertNotNull(token);

        List<AuthToken> tokens = authTokenRepository.findAll();
        assertEquals(1, tokens.size());
        assertEquals(testLogin, tokens.get(0).getUser().getLogin());
        assertEquals(token, tokens.get(0).getToken());
    }

    @Test
    @Order(2)
    void loginWithWrongPasswordThrows() {
        UnauthorizedException ex = assertThrows(UnauthorizedException.class,
                () -> authService.login(testLogin, "wrong"));
        assertTrue(ex.getErrors().containsKey("password"));
        assertArrayEquals(new String[]{"Неверный пароль"}, ex.getErrors().get("password"));
    }

    @Test
    @Order(3)
    void loginWithUnknownLoginThrows() {
        UnauthorizedException ex = assertThrows(UnauthorizedException.class,
                () -> authService.login("unknown@example.com", TEST_PASSWORD));
        assertTrue(ex.getErrors().containsKey("login"));
        assertArrayEquals(new String[]{"Пользователь не найден"}, ex.getErrors().get("login"));
    }

    @Test
    @Order(4)
    void logoutDeletesToken() {
        String token = authService.login(testLogin, TEST_PASSWORD);
        authService.logout(token);
        assertTrue(authTokenRepository.findByToken(token).isEmpty());
    }

    @Test
    @Order(5)
    void getUserByTokenReturnsUser() {
        String token = authService.login(testLogin, TEST_PASSWORD);
        User user = authService.getUserByToken(token);
        assertEquals(testLogin, user.getLogin());
    }

    @Test
    @Order(6)
    void getUserByInvalidTokenThrows() {
        UnauthorizedException ex = assertThrows(UnauthorizedException.class,
                () -> authService.getUserByToken("invalid"));
        assertTrue(ex.getErrors().containsKey("token"));
        assertArrayEquals(new String[]{"Неверный токен"}, ex.getErrors().get("token"));
    }
}
