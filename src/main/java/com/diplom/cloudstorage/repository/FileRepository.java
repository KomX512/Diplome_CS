package com.diplom.cloudstorage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.diplom.cloudstorage.entity.FileEntity;
import com.diplom.cloudstorage.entity.User;

import java.util.List;
import java.util.Optional;

public interface FileRepository extends JpaRepository<FileEntity, Long> {
    Optional<FileEntity> findByOwnerAndFilename(User owner, String filename);
    List<FileEntity> findAllByOwner(User owner);
}
