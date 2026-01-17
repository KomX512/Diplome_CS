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
            createFile(user, "notes.txt", "Пример заметок для теста.".getBytes());
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
