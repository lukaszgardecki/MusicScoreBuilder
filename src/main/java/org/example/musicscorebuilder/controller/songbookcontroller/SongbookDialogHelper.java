package org.example.musicscorebuilder.controller.songbookcontroller;

import org.example.musicscorebuilder.components.dialog.CustomConfirmationDialog;
import org.example.musicscorebuilder.components.dialog.CustomInputDialog;

import java.util.Optional;

public class SongbookDialogHelper {

    public static final String SVG_FOLDER = "M10 4H2a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2V8a2 2 0 0 0-2-2h-8l-2-2z";
    public static final String SVG_RENAME = "M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z";
    public static final String SVG_ADD_FOLDER = "M10 4H2a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2V8a2 2 0 0 0-2-2h-8l-2-2zm3 9h-2v2h-2v-2H7v-2h2V9h2v2h2v2z";
    public static final String SVG_ADD_FILE = "M6 2a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8l-6-6H6zm7 7V3.5L18.5 9H13zm0 7h-2v2h-2v-2H7v-2h2v-2h2v2h2v2z";
    public static final String SVG_TRASH = "M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z";
    public static final String SVG_ERROR = "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z";

    public static final String COLOR_AMBER = "#F59E0B";
    public static final String COLOR_BLUE = "#3B82F6";
    public static final String COLOR_RED = "#DC2626";
    public static final String COLOR_ERROR_RED = "#EF4444";

    public static Optional<String> showInputDialog(String title, String header, String content, String defaultValue, String iconSvg, String iconColor, String confirmButtonText) {
        return new CustomInputDialog()
                .setTitle(title)
                .setHeader(header)
                .setContent(content)
                .setDefaultValue(defaultValue)
                .setIconSvg(iconSvg, iconColor)
                .setConfirmButton(confirmButtonText)
                .setCancelButton("Anuluj")
                .showAndWait()
                .map(String::trim);
    }

    public static void showDeleteConfirmation(String itemType, String displayName, boolean isDirectory, Runnable onConfirm) {
        String warningText = isDirectory ? "\n\nUWAGA: Folder zostanie usunięty wraz z całą zawartością!" : "";

        new CustomConfirmationDialog()
                .setTitle("Potwierdzenie usunięcia")
                .setHeader("Czy na pewno chcesz usunąć ten " + itemType + "?")
                .setContent(displayName + warningText)
                .setIconSvg(SVG_TRASH, COLOR_RED)
                .setConfirmButton("Usuń", onConfirm)
                .setCancelButton("Anuluj", null)
                .showAndWait();
    }

    public static void showErrorAlert(String title, String message) {
        new CustomConfirmationDialog()
                .setTitle(title)
                .setHeader(title)
                .setContent(message)
                .setIconSvg(SVG_ERROR, COLOR_ERROR_RED)
                .setConfirmButton("OK", null)
                .showAndWait();
    }
}