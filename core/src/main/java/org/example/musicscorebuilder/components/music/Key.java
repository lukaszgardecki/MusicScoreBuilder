package org.example.musicscorebuilder.components.music;

public enum Key {
    C_MAJOR(PitchStep.C, 0, 0, "C-dur / a-moll"),

    G_MAJOR(PitchStep.G, 0, 1, "G-dur / e-moll"),
    D_MAJOR(PitchStep.D, 0, 2, "D-dur / h-moll"),
    A_MAJOR(PitchStep.A, 0, 3, "A-dur / fis-moll"),
    E_MAJOR(PitchStep.E, 0, 4, "E-dur / cis-moll"),
    B_MAJOR(PitchStep.B, 0, 5, "H-dur / gis-moll"),
    FS_MAJOR(PitchStep.F, 1, 6, "Fis-dur / dis-moll"),
    CS_MAJOR(PitchStep.C, 1, 7, "Cis-dur / ais-moll"),

    F_MAJOR(PitchStep.F, 0, -1, "F-dur / d-moll"),
    BF_MAJOR(PitchStep.B, -1, -2, "B-dur / g-moll"),
    EF_MAJOR(PitchStep.E, -1, -3, "Es-dur / c-moll"),
    AF_MAJOR(PitchStep.A, -1, -4, "As-dur / f-moll"),
    DF_MAJOR(PitchStep.D, -1, -5, "Des-dur / b-moll"),
    GF_MAJOR(PitchStep.G, -1, -6, "Ges-dur / es-moll"),
    CF_MAJOR(PitchStep.C, -1, -7, "Ces-dur / as-moll");

    private final PitchStep rootStep;
    private final int rootAlter;
    private final int fifths;
    private final String displayName;

    Key(PitchStep rootStep, int rootAlter, int fifths, String displayName) {
        this.rootStep = rootStep;
        this.rootAlter = rootAlter;
        this.fifths = fifths;
        this.displayName = displayName;
    }

    public PitchStep getRootStep() { return rootStep; }
    public int getRootAlter() { return rootAlter; }
    public int getFifths() { return fifths; }
    public String getDisplayName() { return displayName; }

    public int getRootSemitones() {
        return rootStep.getBaseSemitones() + rootAlter;
    }

    public static Key fromFifths(int fifths) {
        for (Key k : values()) {
            if (k.fifths == fifths) return k;
        }
        return C_MAJOR;
    }

    public static Key getNextKeyBySemitone(Key currentKey, int semitoneDelta) {
        int targetSemitones = Math.floorMod(currentKey.getRootSemitones() + semitoneDelta, 12);

        Key bestMatch = null;
        int minAccidentals = Integer.MAX_VALUE;

        for (Key candidate : values()) {
            int candSemitones = Math.floorMod(candidate.getRootSemitones(), 12);

            if (candSemitones == targetSemitones) {
                int accidentals = Math.abs(candidate.getFifths());

                if (accidentals < minAccidentals) {
                    minAccidentals = accidentals;
                    bestMatch = candidate;
                } else if (accidentals == minAccidentals && candidate.getFifths() < 0) {
                    bestMatch = candidate;
                }
            }
        }
        return bestMatch != null ? bestMatch : currentKey;
    }

    @Override
    public String toString() {
        return displayName;
    }
}