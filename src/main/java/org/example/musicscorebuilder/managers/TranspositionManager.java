package org.example.musicscorebuilder.managers;

import org.example.musicscorebuilder.components.music.*;

public class TranspositionManager {
    private static TranspositionManager instance;

    private TranspositionManager() {}

    public static synchronized TranspositionManager getInstance() {
        if (instance == null) {
            instance = new TranspositionManager();
        }
        return instance;
    }

    public void transposeUp(ScoreMode mode) {
        transposeRelative(mode, 1);
    }

    public void transposeDown(ScoreMode mode) {
        transposeRelative(mode, -1);
    }

    private void transposeRelative(ScoreMode mode, int semitoneDelta) {
        if (mode == null || mode.getMeasures().isEmpty()) return;

        Measure firstMeasure = mode.getMeasures().get(0);
        int currentFifths = (firstMeasure.getKeySignature() != null) ? firstMeasure.getKeySignature().getFifths() : 0;

        Key currentKey = Key.fromFifths(currentFifths, false);
        Key targetKey = Key.getNextKeyBySemitone(currentKey, semitoneDelta);

        boolean preferDown = semitoneDelta < 0;
        transposeToKey(mode, currentKey, targetKey, preferDown);
    }

    public void transposeToKey(ScoreMode mode, Key currentKey, Key targetKey) {
        transposeToKey(mode, currentKey, targetKey, false);
    }

    public void transposeToKey(ScoreMode mode, Key currentKey, Key targetKey, boolean preferDown) {
        if (mode == null || mode.getScore() == null || currentKey == targetKey) return;

        int diatonicShift = targetKey.getRootStep().getValue() - currentKey.getRootStep().getValue();

        int[] BASE_SEMITONES = {0, 2, 4, 5, 7, 9, 11};
        int currentSemitones = BASE_SEMITONES[currentKey.getRootStep().getValue()] + currentKey.getRootAlter();
        int targetSemitones = BASE_SEMITONES[targetKey.getRootStep().getValue()] + targetKey.getRootAlter();

        int chromaticShift = targetSemitones - currentSemitones;

        if (preferDown) {
            if (diatonicShift > 0) diatonicShift -= 7;
            if (chromaticShift > 0) chromaticShift -= 12;
        } else {
            if (diatonicShift < 0) diatonicShift += 7;
            if (chromaticShift < 0) chromaticShift += 12;
        }

        applyTransposition(mode, diatonicShift, chromaticShift, targetKey.getFifths());
    }

    private void applyTransposition(ScoreMode mode, int diatonicShift, int chromaticShift, int targetFifths) {
        for (Measure measure : mode.getMeasures()) {

            measure.setKeySignature(new KeySignature(targetFifths, measure));

            for (Segment segment : measure.getSegments()) {
                segment.getStaffElements().values().forEach(staffEls -> {
                    staffEls.forEach(element -> {
                        if (element instanceof Note note && note.getPitch() != null) {
                            note.getPitch().transpose(diatonicShift, chromaticShift);
                        }
                    });
                });
            }
        }
    }
}