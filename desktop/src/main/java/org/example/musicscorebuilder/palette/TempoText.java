package org.example.musicscorebuilder.palette;

public enum TempoText {

    GRAVE("Grave"),
    LARGO("Largo"),
    LENT("Lento"),
    LARGHETTO("Larghetto"),
    ADAGIO("Adagio"),
    ANDANTE("Andante"),
    ANDANTINO("Andantino"),
    MODERATO("Moderato"),
    ALLEGRETTO("Allegretto"),
    ALLEGRO("Allegro"),
    VIVACE("Vivace"),
    PREST("Presto"),
    PRESTISSIMO("Prestissimo"),
    ACCEL("accel."),
    ALLARG("allarg."),
    RALL("rall."),
    RIT("rit."),
    ATEMPO("a tempo"),
    TEMPO_PRIMO("tempo primo"),
    SWING("Swing"),
    CUSTOM_TEXT("Tekst własny");

    private final String text;

    TempoText(String text) {
        this.text = text;
    }

    public String getText() { return text; }
}
