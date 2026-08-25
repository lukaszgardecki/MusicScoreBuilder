package org.example.musicscorebuilder.managers;

import javafx.application.Platform;
import org.example.musicscorebuilder.components.dialog.CustomConfirmationDialog;
import org.example.musicscorebuilder.controller.songbookcontroller.SongbookDialogHelper;
import org.example.musicscorebuilder.controller.util.audio.PianoPlayer;
import org.example.musicscorebuilder.data.StorageService;

import java.io.IOException;

public class ClosingManager {
    private static ClosingManager instance;
    private final StorageService storageService = StorageService.getInstance();

    private ClosingManager() {}

    public synchronized static ClosingManager getInstance() {
        if (instance == null) {
            instance = new ClosingManager();
        }
        return instance;
    }

    public boolean closeScore() {
        boolean[] isClosed = {false};

        confirmAndProceed(() -> {
            StorageService.getInstance().setScore(null);
            isClosed[0] = true;
        });

        return isClosed[0];
    }

    public void closeApp() {
        confirmAndProceed(() -> {
            PianoPlayer.getInstance().close();
            Platform.exit();
            System.exit(0);
        });
    }

    private void confirmAndProceed(Runnable onProceed) {
        if (!storageService.hasUnsavedChanges()) {
            onProceed.run();
            return;
        }

        String scoreTitle = (storageService.getScore() != null && storageService.getScore().getTitle() != null)
                ? storageService.getScore().getTitle()
                : "Bez tytułu";

        new CustomConfirmationDialog()
                .setTitle("MusicScore Builder")
                .setHeader("Chcesz zapisać zmiany w partyturze „" + scoreTitle + "” przed opuszczeniem?")
                .setContent("Twoje zmiany zostaną utracone, jeśli ich nie zapiszesz.")
                .setConfirmButton("Zapisz", () -> {
                    try {
                        storageService.saveCurrentScoreFile();
                        onProceed.run();
                    } catch (IOException e) {
                        e.printStackTrace();
                        SongbookDialogHelper.showErrorAlert("Błąd zapisu", "Nie udało się zapisać partytury: " + e.getMessage());
                    }
                })
                .setDenyButton("Nie zapisuj", onProceed)
                .setCancelButton("Anuluj", null)
                .showAndWait();
    }
}