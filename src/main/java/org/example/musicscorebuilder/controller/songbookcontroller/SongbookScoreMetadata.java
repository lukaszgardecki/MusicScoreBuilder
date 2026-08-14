package org.example.musicscorebuilder.controller.songbookcontroller;

public record SongbookScoreMetadata(
        String numberNew,
        String numberOld,
        String title,
        String subtitle,
        String composer
) {}