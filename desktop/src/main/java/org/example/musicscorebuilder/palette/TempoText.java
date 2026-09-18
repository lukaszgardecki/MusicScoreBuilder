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
    ACCEL("accel.", true),
    ALLARG("allarg.", true),
    RALL("rall.", true),
    RIT("rit.", true),
    ATEMPO("a tempo"),
    TEMPO_PRIMO("tempo primo"),
    REFRAIN("Refren", true),
    CUSTOM_TEXT("Tekst własny");

    private final String text;
    private final boolean bold;
    private final boolean italic;

    TempoText(String text) {
        this(text, true, false);
    }

    TempoText(String text, boolean italic) {
        this(text, true, italic);
    }

    TempoText(String text, boolean bold, boolean italic) {
        this.text = text;
        this.bold = bold;
        this.italic = italic;
    }

    public String getText() { return text; }
    public boolean isBold() { return bold; }
    public boolean isItalic() { return italic; }
}