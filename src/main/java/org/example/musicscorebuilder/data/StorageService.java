package org.example.musicscorebuilder.data;

import org.example.musicscorebuilder.components.music.Score;
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
        setCurrentFile(file);
        setScore(loadedScore);
        this.initialSnapshot = takeSnapshot();
    }

    public void saveCurrentScoreFile() throws IOException {
        Score currentScore = getScore();
        if (currentScore == null || currentFile == null || currentFile.getParentFile() == null) {
            throw new IOException("Błąd zapisywania pliku.");
        }

        fileService.saveToFile(currentScore, currentFile.getParentFile());
        this.initialSnapshot = takeSnapshot();
    }

    public void saveScoreFile(Score scoreToSave, File destDir) throws IOException {
        fileService.saveToFile(scoreToSave, destDir);
        if (scoreToSave == this.score) {
            this.initialSnapshot = takeSnapshot();
        }
    }

    public Score getScore() { return score; }
    public File getCurrentFile() { return currentFile; }
    public boolean hasUnsavedChanges() {
        if (score == null) return false;
        String currentSnapshot = takeSnapshot();
        return !initialSnapshot.equals(currentSnapshot);
    }

    public void setScore(Score score) {
        this.score = score;
        if (score == null) {
            this.currentFile = null;
            this.initialSnapshot = "";
        }
        ScoreStateManager.getInstance().notifyScoreChanged();
    }

    public void setCurrentFile(File currentFile) {
        this.currentFile = currentFile;
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
}