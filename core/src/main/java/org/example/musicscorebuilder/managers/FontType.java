package org.example.musicscorebuilder.managers;

public enum FontType {
    LELAND("/fonts/Leland.otf"),
    FREE_SERIF("/fonts/FreeSerif.ttf"),
    FREE_SERIF_BOLD("/fonts/FreeSerifBold.ttf"),
    FREE_SERIF_ITALIC("/fonts/FreeSerifItalic.ttf"),
    FREE_SERIF_BOLD_ITALIC("/fonts/FreeSerifBoldItalic.ttf");

    private final String resourcePath;

    FontType(String resourcePath) { this.resourcePath = resourcePath; }
    public String getResourcePath() { return resourcePath; }
}
