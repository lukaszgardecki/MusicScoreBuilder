package org.example.musicscorebuilder.components.music;

import java.util.HashMap;
import java.util.Map;

public class SMeasure {
    private final Map<Integer, Voice> voices = new HashMap<>();
    private Clef clef;

    SMeasure(Clef clef) {
        this.clef = clef;
    }

    public void addNewVoice() {
        int nextId = voices.size() + 1;
        voices.put(nextId, new Voice(nextId));
    }

    public Voice getVoice(int id) { return voices.get(id); }
    public Map<Integer, Voice> getVoices() { return voices; }
    public  Clef getClef() { return clef; }

    public void setClef(Clef clef) { this.clef = clef; }
}
