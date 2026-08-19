package org.example.musicscorebuilder.controller.songbookcontroller;

import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class SongbookKeyHandler {

    public interface KeyActions {
        void onCopy();
        void onPaste();
        void onDuplicate();
        void onRename();
        void onDelete();
        void onOpenSelected();
        void onNavigateUp();
    }

    public static void attachKeyBindings(ListView<SongbookItem> listView, KeyActions actions) {
        listView.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                    Node focusOwner = newScene.getFocusOwner();

                    if (!isDescendantOf(listView, focusOwner)) return;
                    if (focusOwner instanceof TextInputControl) return;

                    int currentIndex = listView.getSelectionModel().getSelectedIndex();
                    int totalItems = listView.getItems().size();
                    boolean isShortcut = event.isControlDown() || event.isShortcutDown();

                    if (isShortcut && event.getCode() == KeyCode.C) {
                        actions.onCopy();
                        event.consume();
                    } else if (isShortcut && event.getCode() == KeyCode.V) {
                        actions.onPaste();
                        event.consume();
                    } else if (isShortcut && event.getCode() == KeyCode.D) {
                        actions.onDuplicate();
                        event.consume();
                    } else if (event.getCode() == KeyCode.UP) {
                        if (currentIndex > 0) {
                            int nextIndex = currentIndex - 1;
                            listView.getSelectionModel().select(nextIndex);
                            listView.scrollTo(nextIndex);
                            listView.requestFocus();
                        }
                        event.consume();
                    } else if (event.getCode() == KeyCode.DOWN) {
                        if (currentIndex < totalItems - 1) {
                            int nextIndex = currentIndex + 1;
                            listView.getSelectionModel().select(nextIndex);
                            listView.scrollTo(nextIndex);
                            listView.requestFocus();
                        }
                        event.consume();
                    } else if (event.getCode() == KeyCode.ENTER) {
                        actions.onOpenSelected();
                        event.consume();
                    } else if (event.getCode() == KeyCode.BACK_SPACE) {
                        actions.onNavigateUp();
                        event.consume();
                    } else if (event.getCode() == KeyCode.DELETE) {
                        SongbookItem selected = listView.getSelectionModel().getSelectedItem();
                        if (selected != null && selected.type() != SongbookItem.Type.PARENT_DIR) {
                            actions.onDelete();
                        }
                        event.consume();
                    } else if (event.getCode() == KeyCode.F2) {
                        SongbookItem selected = listView.getSelectionModel().getSelectedItem();
                        if (selected != null && selected.type() != SongbookItem.Type.PARENT_DIR) {
                            actions.onRename();
                        }
                        event.consume();
                    }
                });
            }
        });
    }

    private static boolean isDescendantOf(Node parent, Node child) {
        while (child != null) {
            if (child == parent) return true;
            child = child.getParent();
        }
        return false;
    }
}