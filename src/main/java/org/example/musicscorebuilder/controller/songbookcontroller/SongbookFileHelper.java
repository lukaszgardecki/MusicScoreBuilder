package org.example.musicscorebuilder.controller.songbookcontroller;

import org.example.musicscorebuilder.data.FileService;

import java.io.File;

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
}