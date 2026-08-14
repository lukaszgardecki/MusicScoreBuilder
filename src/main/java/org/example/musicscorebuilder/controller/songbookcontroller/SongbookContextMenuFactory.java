package org.example.musicscorebuilder.controller.songbookcontroller;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCombination;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

import java.util.function.Supplier;

public class SongbookContextMenuFactory {
    private static final String SVG_COPY = "M16 1H4c-1.1 0-2 .9-2 2v14h2V3h12V1zm3 4H8c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h11c1.1 0 2-.9 2-2V7c0-1.1-.9-2-2-2zm0 16H8V7h11v14z";
    private static final String SVG_PASTE = "M19 2h-4.18C14.4.84 13.3 0 12 0S9.6.84 9.18 2H5c-1.1 0-2 .9-2 2v16c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm-7 0c.55 0 1 .45 1 1s-.45 1-1 1-1-.45-1-1 .45-1 1-1zM5 20V4h2v3h10V4h2v16H5z";
    private static final String SVG_DUPLICATE = "M16 1H4c-1.1 0-2 .9-2 2v14h2V3h12V1zm-1 7H8c-1.1 0-2 .9-2 2v11c0 1.1.9 2 2 2h7c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm0 13H8V10h7v11z";
    private static final String SVG_RENAME = "M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z";
    private static final String SVG_DELETE = "M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z";

    public static ContextMenu createEmptyAreaContextMenu(Runnable onPaste, Supplier<Boolean> isPasteDisabled) {
        ContextMenu menu = createBaseContextMenu();
        MenuItem pasteItem = createMenuItem("Wklej", SVG_PASTE, "Shortcut+V", onPaste);
        menu.getItems().add(pasteItem);
        menu.setOnShowing(e -> pasteItem.setDisable(isPasteDisabled.get()));
        return menu;
    }

    public static ContextMenu createItemContextMenu(
            Runnable onCopy,
            Runnable onPaste,
            Runnable onDuplicate,
            Runnable onRename,
            Runnable onDelete,
            Supplier<Boolean> isPasteDisabled) {

        ContextMenu menu = createBaseContextMenu();
        MenuItem copyMenuItem = createMenuItem("Kopiuj", SVG_COPY, "Shortcut+C", onCopy);
        MenuItem pasteMenuItem = createMenuItem("Wklej", SVG_PASTE, "Shortcut+V", onPaste);
        MenuItem duplicateMenuItem = createMenuItem("Duplikuj", SVG_DUPLICATE, "Shortcut+D", onDuplicate);
        MenuItem renameMenuItem = createMenuItem("Zmień nazwę", SVG_RENAME, "F2", onRename);
        MenuItem deleteMenuItem = createMenuItem("Usuń", SVG_DELETE, "Delete", onDelete);

        menu.getItems().addAll(
                copyMenuItem,
                pasteMenuItem,
                duplicateMenuItem,
                new SeparatorMenuItem(),
                renameMenuItem,
                deleteMenuItem
        );

        menu.setOnShowing(e -> pasteMenuItem.setDisable(isPasteDisabled.get()));
        return menu;
    }

    public static ContextMenu createParentDirContextMenu(Runnable onPaste, Supplier<Boolean> isPasteDisabled) {
        ContextMenu menu = createBaseContextMenu();
        MenuItem pasteItem = createMenuItem("Wklej", SVG_PASTE, "Shortcut+V", onPaste);
        pasteItem.setDisable(isPasteDisabled.get());
        menu.getItems().add(pasteItem);
        return menu;
    }

    private static ContextMenu createBaseContextMenu() {
        ContextMenu menu = new ContextMenu();
        menu.getStyleClass().add("score-context-menu");
        return menu;
    }

    private static MenuItem createMenuItem(String text, String svgPathData, String accelerator, Runnable action) {
        MenuItem item = new MenuItem(text);
        if (accelerator != null) {
            item.setAccelerator(KeyCombination.keyCombination(accelerator));
        }
        item.setOnAction(e -> action.run());

        if (svgPathData != null) {
            SVGPath svg = new SVGPath();
            svg.setContent(svgPathData);
            svg.setFill(Color.web("#4a4a4a"));
            svg.setScaleX(0.65);
            svg.setScaleY(0.65);
            svg.getStyleClass().add("svg-path");
            item.setGraphic(svg);
        }
        return item;
    }
}