package com.diplom.cloudstorage.integration;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import com.diplom.cloudstorage.dto.LoginResponse;
import com.diplom.cloudstorage.entity.User;
import com.diplom.cloudstorage.repository.AuthTokenRepository;
import com.diplom.cloudstorage.repository.FileRepository;
import com.diplom.cloudstorage.repository.UserRepository;
import com.diplom.cloudstorage.util.PasswordUtil;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ApiIntegrationTest {

    private static final String CONTEXT = "/cloud";

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("user")
            .withPassword("password");

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthTokenRepository authTokenRepository;

    @Autowired
    private FileRepository fileRepository;

    private String token;
    private String testLogin;
    private final String TEST_PASSWORD = "pwd123";

    @BeforeEach
    void setup() {
        authTokenRepository.deleteAll();
        fileRepository.deleteAll();
        userRepository.deleteAll();

        testLogin = "user-" + UUID.randomUUID() + "test";
        User user = new User(testLogin, PasswordUtil.hash(TEST_PASSWORD));
        userRepository.save(user);

        HttpEntity<String> request = new HttpEntity<>(jsonLogin(testLogin, TEST_PASSWORD), headersJson());
        ResponseEntity<LoginResponse> response = restTemplate.postForEntity(CONTEXT + "/login", request, LoginResponse.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        token = response.getBody().authToken();
        assertNotNull(token);
    }

    private HttpHeaders headersJson() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("auth-token", token);
        return headers;
    }

    private String jsonLogin(String login, String password) {
        return "{\"login\":\"" + login + "\",\"password\":\"" + password + "\"}";
    }

    private String jsonRename(String oldName, String newName) {
        return "{\"oldFilename\":\"" + oldName + "\",\"newFilename\":\"" + newName + "\"}";
    }

    private void uploadFile(String filename, byte[] content) {
        HttpEntity<byte[]> request = new HttpEntity<>(content, authHeaders());
        ResponseEntity<Void> response = restTemplate.exchange(CONTEXT + "/upload/" + filename, HttpMethod.POST, request, Void.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    private byte[] downloadFile(String filename) {
        ResponseEntity<byte[]> response = restTemplate.exchange(CONTEXT + "/download/" + filename, HttpMethod.GET,
                new HttpEntity<>(authHeaders()), byte[].class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        return response.getBody();
    }

    @Test
    @Order(1)
    void loginWithWrongPassword_ReturnsUnauthorized() {
        HttpEntity<String> request = new HttpEntity<>(jsonLogin(testLogin, "wrongpass"), headersJson());
        ResponseEntity<String> response = restTemplate.postForEntity(CONTEXT + "/login", request, String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody().contains("password"));
        assertTrue(response.getBody().contains("Неверный пароль"));
    }

    @Test
    @Order(2)
    void uploadAndDownloadFile() {
        String filename = "test.txt";
        byte[] content = "Hello world".getBytes();
        uploadFile(filename, content);

        byte[] downloaded = downloadFile(filename);
        assertArrayEquals(content, downloaded);
    }

    @Test
    @Order(3)
    void renameAndCheckFile() {
        String oldName = "old.txt";
        String newName = "new.txt";
        byte[] content = "Rename test".getBytes();

        uploadFile(oldName, content);

        HttpEntity<String> renameRequest = new HttpEntity<>(jsonRename(oldName, newName), authHeaders());
        ResponseEntity<Void> renameResponse = restTemplate.exchange(CONTEXT + "/file", HttpMethod.PUT, renameRequest, Void.class);
        assertEquals(HttpStatus.OK, renameResponse.getStatusCode());

        byte[] downloaded = downloadFile(newName);
        assertArrayEquals(content, downloaded);
    }

    @Test
    @Order(4)
    void listFiles_ReturnsAllFiles() {
        uploadFile("file1.txt", "A".getBytes());
        uploadFile("file2.txt", "B".getBytes());

        ResponseEntity<String> response = restTemplate.exchange(CONTEXT + "/list", HttpMethod.GET,
                new HttpEntity<>(authHeaders()), String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("file1.txt"));
        assertTrue(response.getBody().contains("file2.txt"));
    }
}
