package org.example.musicscorebuilder.controller.songbookcontroller;

import org.example.musicscorebuilder.data.PreferencesService;

import java.io.File;
import java.util.Optional;

public class SongbookExplorerManager {
    private static SongbookExplorerManager instance;
    private File currentLocation = PreferencesService.getDirectoryFile().orElse(null);

    private SongbookExplorerManager() {}

    public synchronized static SongbookExplorerManager getInstance() {
        if (instance == null) {
            instance = new SongbookExplorerManager();
        }
        return instance;
    }

    public Optional<File> getCurrentLocation() {
        return Optional.ofNullable(currentLocation);
    }

    public void setCurrentLocation(File currentLocation) {
        this.currentLocation = currentLocation;
    }
}