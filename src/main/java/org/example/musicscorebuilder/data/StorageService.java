package org.example.musicscorebuilder.data;

import org.example.musicscorebuilder.components.music.Score;
import org.example.musicscorebuilder.components.music.util.ScoreFactory;
import org.example.musicscorebuilder.managers.ScoreStateManager;

import java.io.File;
import java.io.IOException;

public class StorageService {
    private static StorageService instance;
    private Score score;
    private File currentFile;
    private String initialSnapshot = "";
    private final FileService fileService = FileService.getInstance();

    private StorageService() {}

    public static synchronized StorageService getInstance() {
        if (instance == null) {
            instance = new StorageService();
        }
        return instance;
    }

    public void loadScoreFile(File file) throws IOException {
        var loadedScore = fileService.loadScore(file);
        this.currentFile = file;
        setScore(loadedScore);
        this.initialSnapshot = takeSnapshot();
    }

    public void saveCurrentScoreFile() throws IOException {
        Score currentScore = getScore();
        File destDir = (currentFile != null && currentFile.getParentFile() != null)
                ? currentFile.getParentFile()
                : PreferencesService.getDirectoryFile().orElseThrow(IOException::new);

        fileService.saveToFile(currentScore, destDir);
        this.initialSnapshot = takeSnapshot();
    }

    public void saveScoreFile(Score scoreToSave, File destDir) throws IOException {
        fileService.saveToFile(scoreToSave, destDir);
        if (scoreToSave == this.score) {
            this.initialSnapshot = takeSnapshot();
        }
    }

    public Score getScore() {
        if (score == null) {
            this.score = ScoreFactory.createScoreTemplate();
            this.initialSnapshot = takeSnapshot();
        }
        return score;
    }

    private void setScore(Score score) {
        this.score = score;
        ScoreStateManager.getInstance().notifyScoreChanged();
    }

    public boolean hasUnsavedChanges() {
        if (score == null) return false;
        String currentSnapshot = takeSnapshot();
        return !initialSnapshot.equals(currentSnapshot);
    }

    private String takeSnapshot() {
        if (score == null) return "";
        try {
            return fileService.serializeToString(score);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public File getCurrentFile() {
        return currentFile;
    }

    public void setCurrentFile(File currentFile) {
        this.currentFile = currentFile;
    }
}