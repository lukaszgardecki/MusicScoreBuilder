package org.example.musicscorebuilder.data;

import org.example.musicscorebuilder.controller.songbookcontroller.SongbookController;

import java.io.File;
import java.util.Optional;
import java.util.prefs.Preferences;

public class PreferencesService {
    private static final String PREF_FOLDER_KEY = "songbook_folder_path";
    private static final Preferences prefs = Preferences.userNodeForPackage(SongbookController.class);

    public static void saveDirectoryPath(String path) {
        if (path != null && !path.isBlank()) {
            File absoluteFile = new File(path).getAbsoluteFile();
            prefs.put(PREF_FOLDER_KEY, absoluteFile.getAbsolutePath());
        }
    }

    public static Optional<File> getDirectoryFile() {
        String savedPath = prefs.get(PREF_FOLDER_KEY, null);
        if (savedPath == null || savedPath.isBlank()) {
            return Optional.empty();
        }

        File folder = new File(savedPath).getAbsoluteFile();

        if (folder.exists() && folder.isDirectory()) {
            return Optional.of(folder);
        }

        return Optional.empty();
    }
}