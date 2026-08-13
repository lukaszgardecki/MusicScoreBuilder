package org.example.musicscorebuilder.data;

import org.example.musicscorebuilder.components.music.Score;
import org.example.musicscorebuilder.controller.SongbookItem;

import java.io.*;
import java.util.*;

public class FileService {
    public static final String JSON_EXTENSION = ".json";
    public static final String GZ_EXTENSION = ".json.gz";

    private static final String DEFAULT_TITLE = "utwor";
    private static final String FILE_NAME_FORMAT = "%s_%s" + JSON_EXTENSION;
    private static FileService instance;
    private final JsonFileService jsonFileService = new JsonFileService();

    private FileService() {}

    public static synchronized FileService getInstance() {
        if (instance == null) {
            instance = new FileService();
        }
        return instance;
    }

    public void saveToFile(Score score, File directory) throws IOException {
        File saveFile = getTargetFile(score, directory);
        jsonFileService.saveToJson(score, saveFile);
    }

    public Score loadScore(File file) throws IOException {
        if (file.exists() && file.isFile()) {
            return jsonFileService.loadFromJson(file);
        } else throw new IOException("Plik nie istnieje.");
    }

    public List<SongbookItem> getDirectoryContent(File folder, boolean compressed) {
        if (folder == null || !folder.isDirectory()) {
            return Collections.emptyList();
        }

        List<SongbookItem> result = new ArrayList<>();

        if (folder.getParentFile() != null) {
            result.add(new SongbookItem(SongbookItem.Type.PARENT_DIR, folder.getParentFile(), ".."));
        }

        File[] files = folder.listFiles();
        if (files != null) {
            Arrays.stream(files)
                    .filter(File::isDirectory)
                    .sorted(Comparator.comparing(File::getName))
                    .map(f -> new SongbookItem(SongbookItem.Type.DIRECTORY, f, f.getName()))
                    .forEach(result::add);

            Arrays.stream(files)
                    .filter(File::isFile)
                    .filter(f -> f.getName().toLowerCase().endsWith(JSON_EXTENSION) || f.getName().toLowerCase().endsWith(GZ_EXTENSION))
                    .filter(f -> jsonFileService.isGzipCompressed(f) == compressed)
                    .sorted(Comparator.comparing(File::getName))
                    .map(f -> new SongbookItem(SongbookItem.Type.FILE, f, f.getName()))
                    .forEach(result::add);
        }

        return result;
    }

    public Optional<File> getCurrentProjectFile() {
        return Optional.ofNullable(System.getProperty("user.dir")).map(File::new);
    }

    public File getTargetFile(Score score, File directory) {
        String rawTitle = score.getTitle();
        String formattedTitle = (rawTitle != null && !rawTitle.isBlank())
                ? rawTitle.trim().replaceAll(" ", "-")
                : DEFAULT_TITLE;

        String fileName = String.format(FILE_NAME_FORMAT, score.getNumberNew(), formattedTitle);
        return new File(directory, fileName);
    }
}