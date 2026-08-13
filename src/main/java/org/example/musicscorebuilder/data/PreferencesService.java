package org.example.musicscorebuilder.data;

import org.example.musicscorebuilder.controller.SongbookController;

import java.io.File;
import java.util.Optional;
import java.util.prefs.Preferences;

public class PreferencesService {
    private static final String PREF_FOLDER_KEY = "songbook_folder_path";
    private static final Preferences prefs = Preferences.userNodeForPackage(SongbookController.class);

    public static void saveDirectoryPath(String path) {
        prefs.put(PREF_FOLDER_KEY, path);
    }

    public static Optional<File> getDirectoryFile() {
        String savedPath = prefs.get(PREF_FOLDER_KEY, null);
        if (savedPath != null) return Optional.of(new File(savedPath));
        return Optional.empty();
    }
}