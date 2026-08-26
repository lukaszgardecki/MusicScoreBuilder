package org.example.musicscorebuilder.managers;

import org.example.musicscorebuilder.components.music.*;

import java.util.List;

public class TranspositionManager {
    private static TranspositionManager instance;

    private TranspositionManager() {}

    public static synchronized TranspositionManager getInstance() {
        if (instance == null) {
            instance = new TranspositionManager();
        }
        return instance;
    }

    public boolean transposeUp(ScoreMode mode) {
        return transposeRelative(mode, 1);
    }

    public boolean transposeDown(ScoreMode mode) {
        return transposeRelative(mode, -1);
    }

    private boolean transposeRelative(ScoreMode mode, int semitoneDelta) {
        if (mode == null || mode.getScore() == null || mode.getMeasures().isEmpty()) {
            return false;
        }

        Score score = mode.getScore();

        if (!score.canTransposeBy(semitoneDelta)) {
            return false;
        }

        Measure firstMeasure = mode.getMeasures().get(0);
        int currentFifths = (firstMeasure.getKeySignature() != null) ? firstMeasure.getKeySignature().getFifths() : 0;

        Key currentKey = Key.fromFifths(currentFifths);
        Key targetKey = Key.getNextKeyBySemitone(currentKey, semitoneDelta);

        boolean preferDown = semitoneDelta < 0;

        List<ScoreMode> modes = score.getModes();
        if (modes != null && !modes.isEmpty()) {
            for (ScoreMode m : modes) {
                transposeToKeyInternal(m, currentKey, targetKey, preferDown);
            }
        } else {
            transposeToKeyInternal(mode, currentKey, targetKey, preferDown);
        }

        score.applyTranspositionDelta(semitoneDelta);
        return true;
    }

    public boolean transposeToKey(ScoreMode mode, Key currentKey, Key targetKey) {
        return transposeToKey(mode, currentKey, targetKey, false);
    }

    public boolean transposeToKey(ScoreMode mode, Key currentKey, Key targetKey, boolean preferDown) {
        if (mode == null || mode.getScore() == null || currentKey == targetKey) return false;

        Score score = mode.getScore();

        int[] BASE_SEMITONES = {0, 2, 4, 5, 7, 9, 11};
        int currentSemitones = BASE_SEMITONES[currentKey.getRootStep().getValue()] + currentKey.getRootAlter();
        int targetSemitones = BASE_SEMITONES[targetKey.getRootStep().getValue()] + targetKey.getRootAlter();
        int semitoneDelta = targetSemitones - currentSemitones;

        if (preferDown && semitoneDelta > 0) semitoneDelta -= 12;
        if (!preferDown && semitoneDelta < 0) semitoneDelta += 12;

        if (!score.canTransposeBy(semitoneDelta)) {
            return false;
        }

        transposeToKeyInternal(mode, currentKey, targetKey, preferDown);
        score.applyTranspositionDelta(semitoneDelta);
        return true;
    }

    private void transposeToKeyInternal(ScoreMode mode, Key currentKey, Key targetKey, boolean preferDown) {
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
        int fifthsDelta = (chromaticShift * 7) - (diatonicShift * 12);

        for (Measure measure : mode.getMeasures()) {
            int currentMeasureFifths = (measure.getKeySignature() != null)
                    ? measure.getKeySignature().getFifths()
                    : (targetFifths - fifthsDelta);

            int newMeasureFifths = normalizeFifths(currentMeasureFifths + fifthsDelta);
            measure.setKeySignature(new KeySignature(newMeasureFifths, measure));

            for (Segment segment : measure.getSegments()) {
                segment.getStaffElements().values().forEach(staffEls -> {
                    for (int i = 0; i < staffEls.size(); i++) {
                        Element element = staffEls.get(i);

                        if (element instanceof Note note && note.getPitch() != null) {
                            note.getPitch().transpose(diatonicShift, chromaticShift);
                        } else if (element instanceof KeySignature keySignature) {
                            int newFifths = normalizeFifths(keySignature.getFifths() + fifthsDelta);
                            staffEls.set(i, new KeySignature(newFifths, measure));
                        }
                    }
                });
            }
        }
    }

    private int normalizeFifths(int fifths) {
        while (fifths > 7) fifths -= 12;
        while (fifths < -7) fifths += 12;
        return fifths;
    }
}