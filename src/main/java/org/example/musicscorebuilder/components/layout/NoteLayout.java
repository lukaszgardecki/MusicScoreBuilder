package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.music.*;

public class NoteLayout extends ElementLayout {
    private final Leland fontData = Leland.NOTE_BLACK;
    private final Note note;
    private final double y;

    public NoteLayout(Note note, SegmentLayout parent, StaffLayout staff) {
        super(false, parent);
        this.note = note;
        Clef clef = staff.getStaff().getDefaultClef();

        // Liczy względem zera pieciolinii (górna linia), trzeba dodać Y pięciolinii
        this.y = calculateY(clef) + staff.getY();
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

    private double calculateY(Clef clef) {
        Pitch pitch = note.getPitch();
        ClefType clefType = clef.getType();

        int noteDiatonicStep = (pitch.getOctave() * 7) + pitch.getStepValue();
        int stepDifference = noteDiatonicStep - clefType.getDiatonicStep();
        double referenceY = clefType.getOffsetY() * style.getStaffLineSpacing();
        double halfSpacing = 0.5 * style.getStaffLineSpacing();
        return referenceY - (stepDifference * halfSpacing);
    }
}
