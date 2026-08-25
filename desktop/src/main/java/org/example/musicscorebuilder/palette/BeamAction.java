package org.example.musicscorebuilder.palette;

public enum BeamAction {
    AUTO("Automatycznie"),
    NONE("Bez belki"),
    BREAK_LEFT("Złam belkę po lewej"),
    BREAK_INNER_8TH("Złam belki wewnętrzne (ósemki)"),
    BREAK_INNER_16TH("Złam belki wewnętrzne (szesnastki)"),
    CONNECT("Połącz belki");

    private final String title;

    BeamAction(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}