package org.example.musicscorebuilder.data;

import org.example.musicscorebuilder.components.music.Score;
import org.example.musicscorebuilder.components.music.util.ScoreFactory;
import org.example.musicscorebuilder.managers.ScoreStateManager;

import java.io.File;
import java.io.IOException;

public class StorageService {
    private static StorageService instance;
    private Score score;
    private final FileService fileService = FileService.getInstance();

    private StorageService() {}

    public static synchronized StorageService getInstance() {
        if (instance == null) {
            instance = new StorageService();
        }
        return instance;
    }

    public void loadScoreFile(File file) throws IOException {
        var score = fileService.loadScore(file);
        setScore(score);
    }

    public void saveCurrentScoreFile() throws IOException {
        Score score = getScore();
        File dest = PreferencesService.getDirectoryFile().orElseThrow(IOException::new);
        fileService.saveToFile(score, dest);
    }

    public void saveScoreFile(Score score, File dest) throws IOException {
        fileService.saveToFile(score, dest);
    }

    public Score getScore() {
        if (score != null) return score;
        this.score = ScoreFactory.createScoreTemplate();
        return score;
    }

    private void setScore(Score score) {
        this.score = score;
        ScoreStateManager.getInstance().notifyScoreChanged();
    }
}