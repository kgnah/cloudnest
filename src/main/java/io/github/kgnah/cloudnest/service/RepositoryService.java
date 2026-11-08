package io.github.kgnah.cloudnest.service;

import org.eclipse.jgit.api.Git;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.FileVisitResult;
import java.nio.file.attribute.BasicFileAttributes;


@Service
public class RepositoryService {

    // clone repository
    public Path cloneRepository(String repositoryUrl) {
        Path repositoryDirectory = null;

        try {
            repositoryDirectory = Files.createTempDirectory("cloudnest-repo-");

            try (Git git = Git.cloneRepository()
                    .setURI(repositoryUrl)
                    .setDirectory(repositoryDirectory.toFile())
                    .call()) {

                // repository cloned successfully
            }

            return repositoryDirectory;

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to clone repository: " + repositoryUrl, e
            );
        }
    }

    // delete repository
    public void deleteRepository(Path repositoryPath) {
        int maxAttempts = 3;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                deleteRepositoryOnce(repositoryPath);
                return;
            }
            catch (IOException e) {
                if (attempt == maxAttempts) {
                    throw new RuntimeException("Failed to delete repository: " + repositoryPath, e);
                }
            }

            try {
                Thread.sleep(500);
            }
            catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while retrying repository cleanup", interruptedException);
            }
        }


    }

    private void deleteRepositoryOnce(Path repositoryPath) throws IOException {
        Files.walkFileTree(repositoryPath, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(
                    Path file,
                    BasicFileAttributes attrs
            ) throws IOException {

                // clear the read-only attribute before deletion
                file.toFile().setWritable(true);
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(
                    Path directory,
                    IOException exception
            ) throws IOException {
                if (exception != null) {
                    throw exception;
                }

                Files.delete(directory);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
