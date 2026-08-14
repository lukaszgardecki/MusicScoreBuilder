package org.example.musicscorebuilder.components.music;

public enum LayoutAction {
    SYSTEM_BREAK("Podział systemu");

    private final String title;

    LayoutAction(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}