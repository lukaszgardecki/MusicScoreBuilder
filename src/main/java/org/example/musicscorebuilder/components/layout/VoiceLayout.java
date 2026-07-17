package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.music.Voice;

import java.util.ArrayList;
import java.util.List;

public class VoiceLayout extends ElementLayout {
    private final Voice voice;
    private final List<ChordLayout> chords = new ArrayList<>();

    public VoiceLayout(Voice voice) {
        super(true);
        this.voice = voice;
    }

    public void add(ChordLayout chord) {
        chords.add(chord);
    }

    @Override public double getY() { return chords.stream().mapToDouble(ChordLayout::getY).min().orElse(0.0); }
    @Override public double getBoxY() { return getY(); }
    @Override
    public double getWidth() {
        if (chords.isEmpty()) return 0.0;
        double minX = Double.MAX_VALUE;
        double maxXWithWidth = 0.0;

        for (ChordLayout chord : chords) {
            if (chord.getX() < minX) minX = chord.getX();

            double rightEdge = chord.getX() + chord.getWidth();
            if (rightEdge > maxXWithWidth) {
                maxXWithWidth = rightEdge;
            }
        }
        return maxXWithWidth - minX;
    }
    @Override public double getHeight() {
        if (chords.isEmpty()) return 0.0;
        double highestBoxY = Double.MAX_VALUE;
        double lowestBoxBottom = -Double.MAX_VALUE;

        for (ChordLayout chord : chords) {
            double currentBoxY = chord.getBoxY();
            if (currentBoxY < highestBoxY) highestBoxY = currentBoxY;

            double currentBoxBottom = currentBoxY + chord.getHeight();
            if (currentBoxBottom > lowestBoxBottom) lowestBoxBottom = currentBoxBottom;
        }
        return lowestBoxBottom - highestBoxY;
    }

    public List<ChordLayout> getChords() { return chords; }
}