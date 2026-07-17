package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.music.Leland;
import org.example.musicscorebuilder.components.music.Note;
import org.example.musicscorebuilder.components.music.Pitch;

public class NoteLayout extends ElementLayout {
    private final Leland fontData = Leland.NOTE_BLACK;
    private final Note note;
    private final double y;

    public NoteLayout(Note note, SegmentLayout parent) {
        super(false, parent);
        this.note = note;
        this.y = calculateY();
    }

    @Override public double getX() { return style.getNoteSideSpace(); }
    @Override public double getY() { return y; }
    @Override public double getBoxY() { return y - (0.5 * style.getStaffLineSpacing()); }
    @Override public double getWidth() { return getBoxWidth(); }
    @Override public double getHeight() { return style.getStaffLineSpacing(); }

    public double getBoxX() { return getX() - style.getNoteSideSpace(); }
    public double getFontWidth() { return (fontData.getHeight() * fontData.getRatio()) * style.getStaffLineSpacing(); }
    public double getBoxWidth() { return getFontWidth() + 2 * style.getNoteSideSpace(); }

    public double getFontSize() { return 4 * style.getStaffLineSpacing(); }
    public String getCode() { return fontData.getCode(); }

    private double calculateY() {
        Pitch pitch = note.getPitch();

        // 1. Pobieramy numeryczną wartość stopnia dźwięku
        int stepValue = switch (pitch.getStep()) {
            case C -> 0; case D -> 1; case E -> 2; case F -> 3; case G -> 4; case A -> 5; case H -> 6;
        };

        // 2. Liczymy bezwzględny stopień diatoniczny nuty
        int noteDiatonicStep = (pitch.getOctave() * 7) + stepValue;

        // 3. Punkt odniesienia: G4 (klucz wiolinowy) ma stopień 32
        int referenceStep = 32;

        // 4. Ile kroków (pól/linii) dzieli naszą nutę od dźwięku G4?
        int stepDifference = noteDiatonicStep - referenceStep;

        // 5. Druga linia od dołu (G4) w układzie pięciolinii (gdzie 0.0 to górna linia, a odległość między liniami to lineSpacing)
        // Dla 5-linii, linie od góry do dołu są na pozycjach: 0, 1, 2, 3, 4 * lineSpacing.
        // Druga linia od dołu to indeks 3.
        double referenceY = 3.0 * style.getStaffLineSpacing();

        // 6. Jeden stopień diatoniczny w górę/dół to dokładnie PÓŁ odległości między liniami (0.5 * lineSpacing)
        // W grafice komputerowej Y rośnie w dół, więc jeśli nuta jest WYŻEJ niż G4 (stepDifference > 0), to musimy ODJĄĆ Y.
        double halfSpacing = 0.5 * style.getStaffLineSpacing();

        return referenceY - (stepDifference * halfSpacing);
    }
}
