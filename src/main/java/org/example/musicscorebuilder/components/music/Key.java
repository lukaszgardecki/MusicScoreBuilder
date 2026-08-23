package org.example.musicscorebuilder.components.music;

public enum Key {
    C_MAJOR(PitchStep.C, 0, 0, "C-dur"),

    G_MAJOR(PitchStep.G, 0, 1, "G-dur"),
    D_MAJOR(PitchStep.D, 0, 2, "D-dur"),
    A_MAJOR(PitchStep.A, 0, 3, "A-dur"),
    E_MAJOR(PitchStep.E, 0, 4, "E-dur"),
    B_MAJOR(PitchStep.B, 0, 5, "H-dur"),
    FS_MAJOR(PitchStep.F, 1, 6, "Fis-dur"),
    CS_MAJOR(PitchStep.C, 1, 7, "Cis-dur"),

    F_MAJOR(PitchStep.F, 0, -1, "F-dur"),
    BF_MAJOR(PitchStep.B, -1, -2, "B-dur"),
    EF_MAJOR(PitchStep.E, -1, -3, "Es-dur"),
    AF_MAJOR(PitchStep.A, -1, -4, "As-dur"),
    DF_MAJOR(PitchStep.D, -1, -5, "Des-dur"),
    GF_MAJOR(PitchStep.G, -1, -6, "Ges-dur"),
    CF_MAJOR(PitchStep.C, -1, -7, "Ces-dur"),


    A_MINOR(PitchStep.A, 0, 0, "a-moll"),

    E_MINOR(PitchStep.E, 0, 1, "e-moll"),
    B_MINOR(PitchStep.B, 0, 2, "h-moll"),
    FS_MINOR(PitchStep.F, 1, 3, "fis-moll"),
    CS_MINOR(PitchStep.C, 1, 4, "cis-moll"),
    GS_MINOR(PitchStep.G, 1, 5, "gis-moll"),
    DS_MINOR(PitchStep.D, 1, 6, "dis-moll"),
    AS_MINOR(PitchStep.A, 1, 7, "ais-moll"),

    D_MINOR(PitchStep.D, 0, -1, "d-moll"),
    G_MINOR(PitchStep.G, 0, -2, "g-moll"),
    C_MINOR(PitchStep.C, 0, -3, "c-moll"),
    F_MINOR(PitchStep.F, 0, -4, "f-moll"),
    BF_MINOR(PitchStep.B, -1, -5, "b-moll"),
    EF_MINOR(PitchStep.E, -1, -6, "es-moll"),
    AF_MINOR(PitchStep.A, -1, -7, "as-moll");

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

    public boolean isMinor() {
        return displayName.endsWith("-moll");
    }

    public static Key fromFifths(int fifths, boolean isMinor) {
        for (Key k : values()) {
            if (k.getFifths() == fifths && k.isMinor() == isMinor) {
                return k;
            }
        }
        return C_MAJOR;
    }

    public static Key getNextKeyBySemitone(Key currentKey, int semitoneDelta) {
        int[] BASE_SEMITONES = {0, 2, 4, 5, 7, 9, 11};
        int currentSemitones = BASE_SEMITONES[currentKey.getRootStep().getValue()] + currentKey.getRootAlter();
        int targetSemitones = Math.floorMod(currentSemitones + semitoneDelta, 12);

        Key bestMatch = null;
        int minAccidentals = Integer.MAX_VALUE;

        for (Key candidate : values()) {
            if (candidate.isMinor() == currentKey.isMinor()) {
                int candSemitones = Math.floorMod(BASE_SEMITONES[candidate.getRootStep().getValue()] + candidate.getRootAlter(), 12);

                if (candSemitones == targetSemitones) {
                    int accidentals = Math.abs(candidate.getFifths());

                    // Najmniejsza liczba znaków przykluczowych
                    if (accidentals < minAccidentals) {
                        minAccidentals = accidentals;
                        bestMatch = candidate;
                    }
                    // Taka sama liczba znaków - wybierz bemole
                    else if (accidentals == minAccidentals && candidate.getFifths() < 0) {
                        bestMatch = candidate;
                    }
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