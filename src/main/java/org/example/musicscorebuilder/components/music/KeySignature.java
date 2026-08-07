package org.example.musicscorebuilder.components.music;

import com.fasterxml.jackson.annotation.*;

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonIdentityInfo(generator = ObjectIdGenerators.None.class)
public class KeySignature extends Element {
    private final KeySigType type;
    private final int fifths;

    @JsonCreator
    public KeySignature(@JsonProperty("fifths") int fifths) {
        super(null);
        this.fifths = fifths;
        this.type = KeySigType.of(fifths);
    }

    public KeySignature(int fifths, Measure parent) {
        super(parent);
        this.fifths = fifths;
        this.type = KeySigType.of(fifths);
    }

    @JsonIgnore
    public KeySigType getType() { return type; }
    public int getFifths() { return fifths; }

    public int getAlterForStep(PitchStep step) {
        return type == null ? 0 : type.getAlterForStep(step);
    }
}