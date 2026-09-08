package org.example.musicscorebuilder.palette;

public enum LayoutAction {
    SYSTEM_BREAK("Podział systemu"),
    VERTICAL_FRAME("Wstaw ramkę pionową"),
    LYRICS_CONTAINER("Wstaw tekst zwrotek");

    private final String title;

    LayoutAction(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}