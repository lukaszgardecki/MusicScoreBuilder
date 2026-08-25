package org.example.musicscorebuilder.controller.songbookcontroller;

import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
import org.example.musicscorebuilder.components.SongbookItem;
import org.example.musicscorebuilder.data.StorageService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class SongbookDragAndDropHandler {
    private final SongbookExplorerManager songbookExplorerManager = SongbookExplorerManager.getInstance();
    private final StorageService storageService = StorageService.getInstance();
    private final Runnable refreshDirectoryCallback;

    public SongbookDragAndDropHandler(Runnable refreshDirectoryCallback) {
        this.refreshDirectoryCallback = refreshDirectoryCallback;
    }

    public void setupCellDragAndDrop(SongbookListCell cell) {
        cell.setOnDragDetected(event -> {
            SongbookItem item = cell.getItem();
            if (item != null && item.type() != SongbookItem.Type.PARENT_DIR && item.file() != null) {
                Dragboard db = cell.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.putFiles(List.of(item.file()));
                db.setContent(content);

                SnapshotParameters params = new SnapshotParameters();
                params.setFill(Color.TRANSPARENT);
                WritableImage snapshot = cell.snapshot(params, null);

                db.setDragView(snapshot, event.getX(), event.getY());
                event.consume();
            }
        });

        cell.setOnDragOver(event -> {
            if (event.getDragboard().hasFiles()) {
                File draggedFile = event.getDragboard().getFiles().getFirst();
                SongbookItem targetItem = cell.getItem();

                if (isDropValid(draggedFile, targetItem)) {
                    event.acceptTransferModes(TransferMode.MOVE);
                }
            }
            event.consume();
        });

        cell.setOnDragEntered(event -> {
            if (event.getDragboard().hasFiles()) {
                File draggedFile = event.getDragboard().getFiles().getFirst();
                SongbookItem targetItem = cell.getItem();

                if (isDropValid(draggedFile, targetItem)) {
                    cell.setStyle("-fx-background-color: #e3f2fd; " +
                            "-fx-border-color: #2196f3; " +
                            "-fx-border-width: 1.5px; " +
                            "-fx-border-radius: 4px; " +
                            "-fx-background-radius: 4px;");
                }
            }
        });

        cell.setOnDragExited(event -> cell.setStyle(null));

        cell.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;

            if (db.hasFiles()) {
                File draggedFile = db.getFiles().getFirst();
                SongbookItem targetItem = cell.getItem();

                if (isDropValid(draggedFile, targetItem)) {
                    File targetDir = getTargetDirectory(targetItem);
                    if (targetDir != null) {
                        moveItem(draggedFile, targetDir);
                        success = true;
                    }
                }
            }

            event.setDropCompleted(success);
            event.consume();
        });

        cell.setOnDragDone(event -> {
            cell.setStyle(null);
            event.consume();
        });
    }

    private boolean isDropValid(File draggedFile, SongbookItem targetItem) {
        if (draggedFile == null || targetItem == null) return false;

        File targetDir = getTargetDirectory(targetItem);
        if (targetDir == null || !targetDir.exists() || !targetDir.isDirectory()) return false;

        if (draggedFile.getParentFile() != null && draggedFile.getParentFile().equals(targetDir)) {
            return false;
        }

        if (draggedFile.equals(targetDir)) return false;

        if (draggedFile.isDirectory() && isSubdirectory(draggedFile, targetDir)) {
            return false;
        }

        return true;
    }

    private File getTargetDirectory(SongbookItem targetItem) {
        if (targetItem == null) return null;

        if (targetItem.type() == SongbookItem.Type.DIRECTORY) {
            return targetItem.file();
        } else if (targetItem.type() == SongbookItem.Type.PARENT_DIR) {
            return songbookExplorerManager.getCurrentLocation()
                    .map(File::getParentFile)
                    .orElse(null);
        }

        return null;
    }

    private boolean isSubdirectory(File baseDir, File childDir) {
        try {
            Path basePath = baseDir.toPath().toRealPath();
            Path childPath = childDir.toPath().toRealPath();
            return childPath.startsWith(basePath);
        } catch (IOException e) {
            return false;
        }
    }

    private void moveItem(File sourceFile, File targetDir) {
        File destinationFile = new File(targetDir, sourceFile.getName());

        if (destinationFile.exists()) {
            SongbookDialogHelper.showErrorAlert(
                    "Błąd przenoszenia",
                    "Plik lub folder o nazwie „" + sourceFile.getName() + "” już istnieje w lokalizacji docelowej."
            );
            return;
        }

        try {
            Files.move(sourceFile.toPath(), destinationFile.toPath());

            File currentFile = storageService.getCurrentFile();
            if (currentFile != null && currentFile.equals(sourceFile)) {
                storageService.setCurrentFile(destinationFile);
            }

            refreshDirectoryCallback.run();
        } catch (IOException e) {
            e.printStackTrace();
            SongbookDialogHelper.showErrorAlert("Błąd przenoszenia", "Nie udało się przenieść elementu: " + e.getMessage());
        }
    }
}