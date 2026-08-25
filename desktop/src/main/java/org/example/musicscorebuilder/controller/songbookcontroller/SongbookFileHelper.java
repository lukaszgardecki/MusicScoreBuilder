package org.example.musicscorebuilder.controller.songbookcontroller;

import org.example.musicscorebuilder.data.FileService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class SongbookFileHelper {

    public record FileInfo(String baseName, String extension) {}

    public static FileInfo extractFileInfo(File file, boolean isDirectory) {
        if (isDirectory) {
            return new FileInfo(file.getName(), "");
        }

        String baseName = file.getName();
        String extension = "";
        String lowerName = baseName.toLowerCase();

        if (lowerName.endsWith(FileService.GZ_EXTENSION)) {
            extension = FileService.GZ_EXTENSION;
            baseName = baseName.substring(0, baseName.length() - FileService.GZ_EXTENSION.length());
        } else if (lowerName.endsWith(FileService.JSON_EXTENSION)) {
            extension = ".json";
            baseName = baseName.substring(0, baseName.length() - FileService.JSON_EXTENSION.length());
        } else {
            int lastDotIndex = baseName.lastIndexOf('.');
            if (lastDotIndex > 0) {
                extension = baseName.substring(lastDotIndex);
                baseName = baseName.substring(0, lastDotIndex);
            }
        }

        return new FileInfo(baseName, extension);
    }

    public static File generateUniqueCopyFile(File targetDir, File sourceFile) {
        String originalName = sourceFile.getName();
        File dest = new File(targetDir, originalName);

        if (!dest.exists()) {
            return dest;
        }

        boolean isDirectory = sourceFile.isDirectory();
        FileInfo info = extractFileInfo(sourceFile, isDirectory);
        String baseName = info.baseName();
        String ext = isDirectory ? "" : info.extension();

        String copyName = baseName + " - kopia" + ext;
        dest = new File(targetDir, copyName);
        int counter = 2;

        while (dest.exists()) {
            copyName = baseName + " - kopia (" + counter + ")" + ext;
            dest = new File(targetDir, copyName);
            counter++;
        }

        return dest;
    }

    public static void copyRecursively(File source, File target) throws IOException {
        if (source.isDirectory()) {
            if (!target.exists() && !target.mkdirs()) {
                throw new IOException("Nie udało się utworzyć folderu: " + target.getAbsolutePath());
            }
            File[] files = source.listFiles();
            if (files != null) {
                for (File file : files) {
                    copyRecursively(file, new File(target, file.getName()));
                }
            }
        } else {
            File parentDir = target.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }
}