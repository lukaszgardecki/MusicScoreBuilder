package org.example.musicscorebuilder.components;

import java.io.File;

public record SongbookItem(Type type, File file, String displayName) {
    public enum Type {
        PARENT_DIR,
        DIRECTORY,
        FILE
    }
}