package org.example.musicscorebuilder.components.music;

public class KeySig extends Element {
    private KeySigType type;

    public KeySig(KeySigType type) {
        this.type = type;
    }

    public  KeySigType getType() { return type; }
}