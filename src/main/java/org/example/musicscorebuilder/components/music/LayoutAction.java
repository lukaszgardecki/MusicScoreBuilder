package org.example.musicscorebuilder.components.music;

public enum LayoutAction {
    SYSTEM_BREAK("Podział systemu"),
    VERTICAL_FRAME("Wstaw ramkę pionową");

    private final String title;

    LayoutAction(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}