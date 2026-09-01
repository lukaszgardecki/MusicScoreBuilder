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

    public boolean transposeUp(Score score) {
        return transposeRelative(score, 1);
    }

    public boolean transposeDown(Score score) {
        return transposeRelative(score, -1);
    }

    private boolean transposeRelative(Score score, int semitoneDelta) {
        if (!score.canTransposeBy(semitoneDelta)) return false;

        Key currentKey = Key.fromFifths(score.getKeyFifths());
        Key targetKey = Key.getNextKeyBySemitone(currentKey, semitoneDelta);

        boolean preferDown = semitoneDelta < 0;
        score.getModes().forEach(m -> transposeToKeyInternal(m, currentKey, targetKey, preferDown));
        score.applyTranspositionDelta(semitoneDelta);
        return true;
    }

    public boolean transposeToKeyTransposition(Score score, KeyTransposition targetKey) {
        if (score == null || targetKey == null) return false;

        int sUp = 0;
        while (score.canTransposeBy(sUp + 1)) {
            sUp++;
        }

        int semitoneDelta = sUp - targetKey.maxUp();
        if (semitoneDelta == 0) return true;

        Key currentKey = Key.fromFifths(score.getKeyFifths());
        Key desiredKey = Key.fromFifths(targetKey.fifths());

        int chromaticShift = semitoneDelta;
        int diatonicShift = desiredKey.getRootStep().getValue() - currentKey.getRootStep().getValue();

        if (chromaticShift < 0) {
            while (diatonicShift > 0) diatonicShift -= 7;
            if (diatonicShift == 0 && chromaticShift <= -12) diatonicShift -= 7;
        } else if (chromaticShift > 0) {
            while (diatonicShift < 0) diatonicShift += 7;
            if (diatonicShift == 0 && chromaticShift >= 12) diatonicShift += 7;
        }

        final int finalDiatonic = diatonicShift;
        final int finalChromatic = chromaticShift;
        score.getModes().forEach(mode -> applyTransposition(mode, finalDiatonic, finalChromatic, targetKey.fifths()));
        score.applyTranspositionDelta(semitoneDelta);

        return true;
    }

    public KeyTransposition getTransposedFifths(KeyTransposition key, boolean isUp) {
        if (key == null) return new KeyTransposition(0, 0, 0);

        Key targetKey = Key.fromFifths(key.fifths());
        int maxUp = key.maxUp();
        int maxDown = key.maxDown();

        int newMaxUp = maxUp;
        int newMaxDown = maxDown;

        if (isUp) {
            if (maxUp > 0) {
                targetKey = Key.getNextKeyBySemitone(targetKey, 1);
                newMaxUp = maxUp - 1;
                newMaxDown = maxDown + 1;
            } else if (maxDown > 0) {
                for (int i = 0; i < maxDown; i++) {
                    targetKey = Key.getNextKeyBySemitone(targetKey, -1);
                }
                newMaxUp = maxUp + maxDown;
                newMaxDown = 0;
            }
        } else {
            if (maxDown > 0) {
                targetKey = Key.getNextKeyBySemitone(targetKey, -1);
                newMaxUp = maxUp + 1;
                newMaxDown = maxDown - 1;
            } else if (maxUp > 0) {
                for (int i = 0; i < maxUp; i++) {
                    targetKey = Key.getNextKeyBySemitone(targetKey, 1);
                }
                newMaxUp = 0;
                newMaxDown = maxUp + maxDown;
            }
        }

        return new KeyTransposition(targetKey.getFifths(), newMaxUp, newMaxDown);
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

    public record KeyTransposition(int fifths, int maxUp, int maxDown) {}
}