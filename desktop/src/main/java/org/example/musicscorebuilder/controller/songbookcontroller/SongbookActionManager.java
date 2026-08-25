package org.example.musicscorebuilder.controller.songbookcontroller;

import org.example.musicscorebuilder.components.SongbookItem;
import org.example.musicscorebuilder.components.music.Score;
import org.example.musicscorebuilder.components.music.util.ScoreFactory;
import org.example.musicscorebuilder.data.FileService;
import org.example.musicscorebuilder.data.StorageService;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;

public class SongbookActionManager {
    private final FileService fileService = FileService.getInstance();
    private final StorageService storageService = StorageService.getInstance();
    private final SongbookExplorerManager songbookExplorerManager = SongbookExplorerManager.getInstance();
    private File copiedFile = null;

    public File getCopiedFile() {
        return copiedFile;
    }

    public boolean isPasteDisabled() {
        return copiedFile == null || !copiedFile.exists();
    }

    public void handleCopy(SongbookItem selected) {
        if (selected == null || selected.type() == SongbookItem.Type.PARENT_DIR) return;
        File fileToCopy = selected.file();
        if (fileToCopy != null && fileToCopy.exists()) {
            this.copiedFile = fileToCopy;
        }
    }

    public void handlePaste(Runnable refreshCallback, Consumer<File> selectFileCallback) {
        if (isPasteDisabled()) {
            SongbookDialogHelper.showErrorAlert("Błąd wklejania", "Brak skopiowanego pliku lub folderu.");
            return;
        }

        Optional<File> currentDirOpt = songbookExplorerManager.getCurrentLocation();
        if (currentDirOpt.isEmpty()) {
            SongbookDialogHelper.showErrorAlert("Błąd zapisu", "Najpierw wybierz folder śpiewnika!");
            return;
        }

        File currentDir = currentDirOpt.get();
        File targetFile = SongbookFileHelper.generateUniqueCopyFile(currentDir, copiedFile);

        try {
            if (copiedFile.isDirectory() && targetFile.getCanonicalPath().startsWith(copiedFile.getCanonicalPath() + File.separator)) {
                SongbookDialogHelper.showErrorAlert("Błąd kopiowania", "Nie można skopiować folderu do jego własnego podfolderu!");
                return;
            }

            SongbookFileHelper.copyRecursively(copiedFile, targetFile);
            refreshCallback.run();
            selectFileCallback.accept(targetFile);
        } catch (IOException e) {
            e.printStackTrace();
            SongbookDialogHelper.showErrorAlert("Błąd kopiowania", "Nie udało się wkleić elementu: " + e.getMessage());
        }
    }

    public void handleDuplicate(SongbookItem selected, Runnable refreshCallback, Consumer<File> selectFileCallback) {
        if (selected == null || selected.type() == SongbookItem.Type.PARENT_DIR) return;

        File sourceFile = selected.file();
        if (sourceFile == null || !sourceFile.exists()) return;

        File parentDir = sourceFile.getParentFile();
        if (parentDir == null || !parentDir.exists()) return;

        File targetFile = SongbookFileHelper.generateUniqueCopyFile(parentDir, sourceFile);

        try {
            SongbookFileHelper.copyRecursively(sourceFile, targetFile);
            refreshCallback.run();
            selectFileCallback.accept(targetFile);
        } catch (IOException e) {
            e.printStackTrace();
            SongbookDialogHelper.showErrorAlert("Błąd duplikowania", "Nie udało się zduplikować elementu: " + e.getMessage());
        }
    }

    public void handleRename(SongbookItem selected, File currentOpenedFile, Runnable refreshCallback, Consumer<File> selectFileCallback, Consumer<File> onOpenedFileRenamed) {
        if (selected == null || selected.type() == SongbookItem.Type.PARENT_DIR) return;

        File oldFile = selected.file();
        if (oldFile == null || !oldFile.exists()) return;

        boolean isDirectory = selected.type() == SongbookItem.Type.DIRECTORY;
        SongbookFileHelper.FileInfo fileInfo = SongbookFileHelper.extractFileInfo(oldFile, isDirectory);

        String currentInput = fileInfo.baseName();
        String iconPath = isDirectory ? SongbookDialogHelper.SVG_FOLDER : SongbookDialogHelper.SVG_RENAME;
        String iconColor = isDirectory ? SongbookDialogHelper.COLOR_AMBER : SongbookDialogHelper.COLOR_BLUE;
        String itemType = isDirectory ? "folderu" : "pliku";

        while (true) {
            Optional<String> result = SongbookDialogHelper.showInputDialog(
                    "Zmiana nazwy",
                    "Zmiana nazwy " + itemType,
                    "Wprowadź nową nazwę dla wybranego elementu:",
                    currentInput,
                    iconPath,
                    iconColor,
                    "Zmień"
            );

            if (result.isEmpty()) break;

            currentInput = result.get();
            if (currentInput.isEmpty()) {
                SongbookDialogHelper.showErrorAlert("Błąd", "Nazwa nie może być pusta!");
                continue;
            }

            String finalFileName = isDirectory ? currentInput : (currentInput + fileInfo.extension());
            File targetFile = new File(oldFile.getParentFile(), finalFileName);

            if (targetFile.equals(oldFile)) break;

            if (targetFile.exists()) {
                SongbookDialogHelper.showErrorAlert("Błąd", "Element o nazwie '" + targetFile.getName() + "' już istnieje!");
                continue;
            }

            if (oldFile.renameTo(targetFile)) {
                if (oldFile.equals(currentOpenedFile)) {
                    onOpenedFileRenamed.accept(targetFile);
                }
                refreshCallback.run();
                selectFileCallback.accept(targetFile);
                break;
            } else {
                SongbookDialogHelper.showErrorAlert("Błąd", "Nie udało się zmienić nazwy na dysku.");
                break;
            }
        }
    }

    public void handleDelete(SongbookItem selected, File currentOpenedFile, Runnable refreshCallback, Runnable clearMetadataCallback) {
        if (selected == null || selected.type() == SongbookItem.Type.PARENT_DIR) return;

        File fileToDelete = selected.file();
        if (fileToDelete == null || !fileToDelete.exists()) return;

        boolean isDirectory = selected.type() == SongbookItem.Type.DIRECTORY;
        String itemType = isDirectory ? "folder" : "plik";

        SongbookDialogHelper.showDeleteConfirmation(
                itemType,
                selected.displayName(),
                isDirectory,
                () -> {
                    if (fileService.deleteRecursively(fileToDelete)) {
                        if (fileToDelete.equals(currentOpenedFile)) {
                            StorageService.getInstance().setScore(null);
                            clearMetadataCallback.run();
                        }
                        refreshCallback.run();
                    } else {
                        SongbookDialogHelper.showErrorAlert("Błąd usuwania", "Nie udało się usunąć elementu: " + selected.displayName());
                    }
                }
        );
    }

    public void handleAddFolder(Runnable refreshCallback, Consumer<File> selectFileCallback) {
        Optional<File> parentDirOpt = songbookExplorerManager.getCurrentLocation();
        if (parentDirOpt.isEmpty()) {
            SongbookDialogHelper.showErrorAlert("Brak folderu", "Najpierw wybierz folder śpiewnika!");
            return;
        }

        File parentDir = parentDirOpt.get();
        String currentInput = "";

        while (true) {
            Optional<String> result = SongbookDialogHelper.showInputDialog(
                    "Nowy folder",
                    "Tworzenie nowego podfolderu w śpiewniku",
                    "Podaj nazwę nowego folderu:",
                    currentInput,
                    SongbookDialogHelper.SVG_ADD_FOLDER,
                    SongbookDialogHelper.COLOR_AMBER,
                    "Utwórz"
            );

            if (result.isEmpty()) break;

            currentInput = result.get();
            if (currentInput.isEmpty()) {
                SongbookDialogHelper.showErrorAlert("Błąd", "Nazwa folderu nie może być pusta!");
                continue;
            }

            File newDir = new File(parentDir, currentInput);
            if (newDir.exists()) {
                SongbookDialogHelper.showErrorAlert("Błąd", "Folder o nazwie '" + currentInput + "' już istnieje!");
            } else if (newDir.mkdirs()) {
                refreshCallback.run();
                selectFileCallback.accept(newDir);
                break;
            } else {
                SongbookDialogHelper.showErrorAlert("Błąd", "Nie udało się utworzyć folderu na dysku.");
            }
        }
    }

    public void handleAddFile(Runnable refreshCallback, Consumer<File> selectFileCallback, Consumer<File> openFileCallback) {
        Optional<File> parentDirOpt = songbookExplorerManager.getCurrentLocation();
        if (parentDirOpt.isEmpty()) {
            SongbookDialogHelper.showErrorAlert("Brak folderu", "Najpierw wybierz folder śpiewnika!");
            return;
        }

        File parentDir = parentDirOpt.get();

        Optional<SongbookScoreMetadata> result = SongbookDialogHelper.showNewFileDialog();
        if (result.isEmpty()) return;

        SongbookScoreMetadata metadata = result.get();
        createAndSaveScore(parentDir, metadata, refreshCallback, selectFileCallback, openFileCallback);
    }

    private boolean createAndSaveScore(
            File parentDir,
            SongbookScoreMetadata metadata,
            Runnable refreshCallback,
            Consumer<File> selectFileCallback,
            Consumer<File> openFileCallback
    ) {
        Score score = new Score(
                metadata.numberOld(),
                metadata.numberNew(),
                metadata.title(),
                metadata.subtitle(),
                metadata.composer()
        );
        Score defaultScore = ScoreFactory.createEmptySoloTemplate(score);

        File targetFile = fileService.getTargetFile(defaultScore, parentDir);
        if (targetFile.exists()) {
            SongbookDialogHelper.showErrorAlert("Błąd", "Plik o nazwie '" + targetFile.getName() + "' już istnieje!");
            return false;
        }

        try {
            storageService.saveScoreFile(defaultScore, parentDir);
            refreshCallback.run();
            selectFileCallback.accept(targetFile);
            openFileCallback.accept(targetFile);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            SongbookDialogHelper.showErrorAlert("Błąd zapisu", "Nie udało się utworzyć nowego pliku: " + e.getMessage());
            return false;
        }
    }
}