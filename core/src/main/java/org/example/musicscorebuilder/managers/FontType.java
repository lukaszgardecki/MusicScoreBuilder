package org.example.musicscorebuilder.managers;

public enum FontType {
    LELAND("/fonts/Leland.otf"),
    FREE_SERIF("/fonts/FreeSerif.ttf");

    private final String resourcePath;

    FontType(String resourcePath) { this.resourcePath = resourcePath; }
    public String getResourcePath() { return resourcePath; }
}
