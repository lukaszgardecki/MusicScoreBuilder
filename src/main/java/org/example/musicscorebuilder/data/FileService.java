package org.example.musicscorebuilder.data;

import org.example.musicscorebuilder.components.music.Score;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class FileService {
    private static FileService instance;
    private final JsonFileService jsonFileService = new JsonFileService();

    private FileService() {}

    public static synchronized FileService getInstance() {
        if (instance == null) {
            instance = new FileService();
        }
        return instance;
    }

    public void saveToFile(Score score) throws IOException {
//        String fileName = String.format("%s_%s_c.json", score.getNumberNew(), score.getTitle().replaceAll(" ", "-"));
        String fileName = String.format("%s_%s.json", score.getNumberNew(), score.getTitle().replaceAll(" ", "-"));
        File saveFile = new File(fileName);
//            jsonFileService.saveToCompressedJson(score, saveFile);
        jsonFileService.saveToJson(score, saveFile);
    }

    public Score loadScore(File file) throws IOException {
        if (file.exists() && file.isFile()) {
            return jsonFileService.loadFromJson(file);
        } else throw new IOException("Plik nie istnieje.");
    }

    public List<String> getJsonFileNames(File folder, boolean compressed) {
        if (folder == null || !folder.isDirectory()) {
            return Collections.emptyList();
        }

        FilenameFilter jsonFilter = (dir, name) -> {
            String lower = name.toLowerCase();
            return lower.endsWith(".json") || lower.endsWith(".json.gz");
        };

        File[] files = folder.listFiles(jsonFilter);
        if (files == null) {
            return Collections.emptyList();
        }

        return Arrays.stream(files)
                .filter(file -> jsonFileService.isGzipCompressed(file) == compressed)
                .map(File::getName)
                .toList();
    }

    public Optional<File> getFileFromSongbookDir(String fileName) {
        return PreferencesService.getDirectoryFile().map(directory -> new File(directory, fileName));
    }

    public Optional<File> getCurrentProjectFile() {
        return Optional.ofNullable(System.getProperty("user.dir")).map(File::new);
    }
}