package org.example.musicscorebuilder.components.music;

import java.util.ArrayList;
import java.util.List;

public class Segment {
    private final List<Element> elements = new ArrayList<>();

    public void addElement(Element element) { elements.add(element); }

    public List<Element> getElements() { return elements; }
}
