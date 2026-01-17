package com.diplom.cloudstorage.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.diplom.cloudstorage.entity.FileEntity;
import com.diplom.cloudstorage.entity.User;
import com.diplom.cloudstorage.repository.FileRepository;
import com.diplom.cloudstorage.repository.UserRepository;
import com.diplom.cloudstorage.util.PasswordUtil;

@Component
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final FileRepository fileRepository;

    @PostConstruct
    public void init() {
        if (!userRepository.existsByLogin("root")) {
            User user = new User("root", PasswordUtil.hash("112233"));
            userRepository.save(user);

            createFile(user, "document.pdf", "Это пример PDF файла.".getBytes());
            createFile(user, "photo.png", generateSampleImageBytes());
            createFile(user, "text.txt", "Пример заметок для теста.".getBytes());
        }

        if (!userRepository.existsByLogin("user1")) {
            User user = new User("user1", PasswordUtil.hash("123123"));
            userRepository.save(user);

            createFile(user, "doc_by_user1.pdf", "Это пример PDF файла.".getBytes());
            createFile(user, "photo_by_user1.png", generateSampleImageBytes());
            createFile(user, "text_by_user1.txt", "Пример заметок для теста.".getBytes());
        }

    }

    private void createFile(User user, String filename, byte[] content) {
        FileEntity file = new FileEntity();
        file.setOwner(user);
        file.setFilename(filename);
        file.setContent(content);
        fileRepository.save(file);
    }

    private byte[] generateSampleImageBytes() {
        byte[] image = new byte[1024]; // 1 KB
        for (int i = 0; i < image.length; i++) {
            image[i] = (byte) (i % 256);
        }
        return image;
    }
}
